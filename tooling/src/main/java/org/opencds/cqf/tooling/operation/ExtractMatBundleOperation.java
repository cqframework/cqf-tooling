package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtractMatBundleOperation extends Operation {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String ERROR_BUNDLE_OUTPUT_INVALID = "When specifying the output folder using -op for ExtractMatBundle, the output directory name must contain the word 'bundle' (all lowercase.)";
    private static final String ERROR_BUNDLE_FILE_IS_REQUIRED = "The path to a bundle file is required";
    private static final String ERROR_DIR_IS_NOT_A_DIRECTORY = "The path specified with -dir is not a directory.";
    private static final String ERROR_OP_IS_NOT_A_DIRECTORY = "The path specified with -op is not a directory.";
    private static final String ERROR_DIR_IS_EMPTY = "The path specified with -dir is empty.";
    private static final String ERROR_BUNDLE_LOCATION_NONEXISTENT = "The path specified for the bundle doesn't exist on your system.";
    private static final String ERROR_OUTPUT_LOCATION_NONEXISTENT = "The path specified for the output folder doesn't exist on your system.";
    private static final String INFO_EXTRACTION_SUCCESSFUL = "Extraction completed successfully";
    private static final String VERSION_R4 = "r4";
    private static final String VERSION_STU3 = "stu3";
    private static final String ERROR_NOT_JSON_OR_XML = "The path to a bundle file of type json or xml is required.";
    private static final String ERROR_NOT_VALID = "Unable to translate the file. The resource appears invalid.";
    private static final String ERROR_NOT_VALID_BUNDLE = "Not a recognized transaction Bundle: ";


    private List<String> processedBundleCollection;

    @Override
    public void execute(String[] args) {

        processedBundleCollection = new CopyOnWriteArrayList<>();

        boolean directoryFlagPresent = false;
        boolean suppressNarrative = true;

        String version = VERSION_R4;
        String inputLocation = null;

        //loop through args and prepare approach:
        for (int i = 0; i < args.length; i++) {
            if (i == 0 && args[i].equalsIgnoreCase("-ExtractMatBundle")) {
                continue; //
            } else if (i == 0 && !args[i].equalsIgnoreCase("-ExtractMatBundle")) {
                throw new IllegalArgumentException("Insufficient argument structure. " +
                        "Usage Example: mvn exec:java -Dexec.args=\"-ExtractMatBundle " +
                        File.separator + "Development" + File.separator + "ecqm-content-r4" +
                        File.separator + "bundles" + File.separator +  "mat" +
                        File.separator + "EXM124" + File.separator + "EXM124.json -v=r4");
            }

            //position 1 is the location of file or directory. Determine which:
            if (i == 1) {
                inputLocation = args[i];
                inputLocation = inputLocation.replace("%20", " "); // TODO: use URI instead?
                continue;
            }

            if (args[i].equalsIgnoreCase("-dir")) {
                directoryFlagPresent = true;
                continue;
            }

            String[] flagAndValue = args[i].split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + args[i]);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "sn":
                    if (value.equalsIgnoreCase("false")) {
                        suppressNarrative = false;
                    }
                    break;
                case "op":
                    File userSuppliedOutputDir = new File(value);
                    if (!userSuppliedOutputDir.exists()) {
                        throw new IllegalArgumentException(ERROR_OUTPUT_LOCATION_NONEXISTENT);
                    } else if (!userSuppliedOutputDir.isDirectory()) {
                        throw new IllegalArgumentException(ERROR_OP_IS_NOT_A_DIRECTORY);
                    } else if (!value.toLowerCase().contains("bundle")) {
                        throw new IllegalArgumentException(ERROR_BUNDLE_OUTPUT_INVALID);
                    }

                    setOutputPath(value);
                    break;
                case "v":
                    version = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }//end arg loop

        //determine location is provided by user, if not, throw argument exception:
        if (inputLocation == null) {
            throw new IllegalArgumentException(ERROR_BUNDLE_FILE_IS_REQUIRED);
        } else {
            File bundleFile = new File(inputLocation);
            if (!bundleFile.exists()) {
                throw new IllegalArgumentException(ERROR_BUNDLE_LOCATION_NONEXISTENT);
            }
            if (directoryFlagPresent) {
                //ensure the input is a directory before trying to return a list of its files:
                if (!bundleFile.isDirectory()) {
                    throw new IllegalArgumentException(ERROR_DIR_IS_NOT_A_DIRECTORY);
                }
            } else {
                if (bundleFile.isDirectory()) {
                    throw new IllegalArgumentException(ERROR_BUNDLE_FILE_IS_REQUIRED);
                }
            }
        }


        //if -dir was found, treat inputLocation as directory:
        if (directoryFlagPresent) {
            File[] filesInDir = new File(inputLocation).listFiles();
            if (filesInDir != null && filesInDir.length > 0) {
                //use recursive calls to build up task list:
                ThreadUtils.executeTasks(processFilesInDir(filesInDir, version, suppressNarrative));
            } else {
                logger.info(ERROR_DIR_IS_EMPTY);
                return;
            }
        } else {
            //single file, allow processSingleFile() to run with currently assigned inputFile:
            processSingleFile(new File(inputLocation), version, suppressNarrative);
        }

        if (!processedBundleCollection.isEmpty()) {
            logger.info("Successfully extracted " + processedBundleCollection.size() + " resource(s): \n" + String.join("\n", processedBundleCollection));
        } else {
            logger.info("ExtractMatBundleOperation ended with no resources extracted!");
        }
    }

    /**
     * This method uses paralellism to deploy multiple threads and speed up ExtractMatBundleOperation on entire directories
     *
     * @param filesInDir
     * @param version
     * @param suppressNarrative
     */
    private List<Callable<Void>> processFilesInDir(File[] filesInDir, String version, boolean suppressNarrative) {
        List<Callable<Void>> tasks = new ArrayList<>();
        if (filesInDir != null) {
            for (File file : filesInDir) {
                //process only 1 layer of subdirectories (should they exist)
                if (file.isDirectory()) {
                    tasks.addAll(processFilesInDir(file.listFiles(), version, suppressNarrative));
                } else {
                    tasks.add(() -> {
                        processSingleFile(file, version, suppressNarrative);
                        return null;
                    });
                }
            }
        }
        return tasks;
    }

    void processSingleFile(File bundleFile, String version, boolean suppressNarrative) {
        String inputFileLocation = bundleFile.getAbsolutePath();

        logger.info(String.format("Extracting MAT bundle from %s", inputFileLocation));

        // Set the FhirContext based on the version specified
        FhirContext context;
        if (version == null) {
            context = FhirContext.forR4Cached();
            version = VERSION_R4;
        } else {
            switch (version.toLowerCase()) {
                case VERSION_STU3:
                    context = FhirContext.forDstu3Cached();
                    break;
                case VERSION_R4:
                    context = FhirContext.forR4Cached();
                    break;
                default:
                    logger.error("processSingleFile", new IllegalArgumentException("Unknown fhir version: " + version));
                    return;
            }
        }


        // Read in the Bundle, override encoding
        IBaseResource bundle;
        String encoding;
        if (bundleFile.getPath().endsWith(".xml")) {
            encoding = "xml";
            try (FileReader reader = new FileReader(bundleFile)) {
                bundle = context.newXmlParser().parseResource(reader);
            } catch (Exception e) {
                logger.info(ERROR_NOT_VALID + "\n" + inputFileLocation);
                return;
            }

        } else if (bundleFile.getPath().endsWith(".json")) {
            encoding = "json";
            try (FileReader reader = new FileReader(bundleFile)) {
                bundle = context.newJsonParser().parseResource(reader);
            } catch (Exception e) {
                logger.info(ERROR_NOT_VALID + "\n" + inputFileLocation);
                return;
            }

        } else {
            logger.info(ERROR_NOT_JSON_OR_XML + "\n" + inputFileLocation);
            return;
        }

        //sometimes tests leave library or measure files behind, so we want to make sure we only iterate over bundle files:
        if (!(bundle instanceof org.hl7.fhir.dstu3.model.Bundle || bundle instanceof org.hl7.fhir.r4.model.Bundle)) {
            logger.info(ERROR_NOT_VALID_BUNDLE + inputFileLocation);
            return;
        }

        //ensure the xml and json files are transaction Bundle types:
        if (bundle instanceof org.hl7.fhir.dstu3.model.Bundle) {
            if (!((org.hl7.fhir.dstu3.model.Bundle) bundle).getType().equals(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION)) {
                logger.info("Invalid Bundle type in " + encoding + " file: " + inputFileLocation);
                return;
            }

        } else if (bundle instanceof org.hl7.fhir.r4.model.Bundle) {
            if (!((org.hl7.fhir.r4.model.Bundle) bundle).getType().equals(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION)) {
                logger.info("Invalid Bundle type in " + encoding + " file: " + inputFileLocation);
                return;
            }
        } else {
            logger.info("Not a recognized bundle in " + encoding + "file: " + inputFileLocation);
            return;
        }

        //call the Bundle utilities to extract the bundle
        String outputDir = bundleFile.getAbsoluteFile().getParent();


        //ensure output path assigned by user is utilized:
        if (getOutputPath() != null && !getOutputPath().isEmpty()) {
            outputDir = getOutputPath();
        }

        processedBundleCollection.addAll(BundleUtils.extractResources(bundle, encoding, outputDir, suppressNarrative, version));

        //move and properly rename the files
        moveAndRenameFiles(outputDir, context, version);

        logger.info(INFO_EXTRACTION_SUCCESSFUL + ": " + inputFileLocation);
    }


    private Path getParentBundleDir(String directory){
        Path parent = Paths.get(directory);
        // Traverse to the parent of 'bundles'
        while (parent != null &&
                parent.getFileName() != null &&
                !parent.getFileName().toString().equalsIgnoreCase("bundles")) {
            parent = parent.getParent();
        }

        // Move one level up to get the directory before 'bundles'
        if (parent != null) {
            return parent.getParent();
        } else {
            return null;
        }
    }

    /**
     * Iterates through the files and properly renames and moves them to the proper place
     *
     * @param outputDir
     */
    private void moveAndRenameFiles(String outputDir, FhirContext context, String version) {
        File[] extractedFiles = new File(outputDir).listFiles();
        assert extractedFiles != null;
        for (File extractedFile : extractedFiles) {
            IBaseResource theResource = null;
            if (extractedFile.getPath().endsWith(".xml")) {
                try {
                    theResource = context.newXmlParser().parseResource(new FileReader(extractedFile));
                } catch (Exception e) {
                    logger.error("moveAndRenameFiles: " + extractedFile + ": " + e.getMessage());
                    continue;
                }
            } else if (extractedFile.getPath().endsWith(".json")) {
                try {
                    theResource = context.newJsonParser().parseResource(new FileReader(extractedFile));
                } catch (Exception e) {
                    logger.error("moveAndRenameFiles: " + extractedFile + ": " + e.getMessage());
                    continue;
                }
            }

            // The extractor code names them using the resource type and ID
            // We want to name them without the resource type, use name, and if needed version
            String resourceName;

            // https://github.com/cqframework/cqf-tooling/issues/537
            // if there's no bundle directory (getParentBundleDir returned null) we'll use resourceOutputDir (original outputDir):
            Path resourceOutputDir = Paths.get(outputDir).toAbsolutePath();

            Path newOutputDirectory = Paths.get(
                    Objects.requireNonNullElse(getParentBundleDir(outputDir)
                            , resourceOutputDir)
                            + File.separator + "input");

            Path newLibraryDirectory = Paths.get(newOutputDirectory.toString(), "resources" + File.separator + "library");
            Path newCqlDirectory = Paths.get(newOutputDirectory.toString(), "cql");
            Path newMeasureDirectory = Paths.get(newOutputDirectory.toString(), "resources" + File.separator + "measure");

            if (version.equals(VERSION_STU3)) {
                if (theResource instanceof org.hl7.fhir.dstu3.model.Library) {
                    org.hl7.fhir.dstu3.model.Library theLibrary = (org.hl7.fhir.dstu3.model.Library) theResource;
                    resourceName = theLibrary.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theLibrary.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context, newLibraryDirectory.toString(), resourceName);

                    // Now extract the CQL from the library file
                    String cqlFilename = Paths.get(newCqlDirectory.toString(), resourceName) + ".cql";
                    extractStu3CQL(theLibrary, cqlFilename);
                } else if (theResource instanceof org.hl7.fhir.dstu3.model.Measure) {
                    org.hl7.fhir.dstu3.model.Measure theMeasure = (org.hl7.fhir.dstu3.model.Measure) theResource;
                    resourceName = theMeasure.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theMeasure.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context, newMeasureDirectory.toString(), resourceName);
                }
            } else if (version.equals(VERSION_R4)) {
                if (theResource instanceof org.hl7.fhir.r4.model.Library) {
                    org.hl7.fhir.r4.model.Library theLibrary = (org.hl7.fhir.r4.model.Library) theResource;
                    resourceName = theLibrary.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theLibrary.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context, newLibraryDirectory.toString(), resourceName);

                    // Now extract the CQL from the library file
                    String cqlFilename = Paths.get(newCqlDirectory.toString(), resourceName) + ".cql";
                    extractR4CQL(theLibrary, cqlFilename);
                } else if (theResource instanceof org.hl7.fhir.r4.model.Measure) {
                    org.hl7.fhir.r4.model.Measure theMeasure = (org.hl7.fhir.r4.model.Measure) theResource;
                    resourceName = theMeasure.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theMeasure.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context, newMeasureDirectory.toString(), resourceName);
                }
            }
        }
    }

    /**
     * Looks at the content of the Library passed in and if the type is texl/cql extracts it and decodes
     * it and writes it to the filename passed in
     *
     * @param theLibrary
     * @param cqlFilename
     */
    private void extractStu3CQL(org.hl7.fhir.dstu3.model.Library theLibrary, String cqlFilename) {
        List<org.hl7.fhir.dstu3.model.Attachment> contents = theLibrary.getContent();
        for (org.hl7.fhir.dstu3.model.Attachment content : contents) {
            if (content.getContentType().equals("text/cql")) {
                byte[] encodedBytes = content.getData();
                String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
                try {
                    File outputFile = new File(cqlFilename);
                    // Ensure the parent directory exists
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
                } catch (IOException e) {
                    throw new RuntimeException(cqlFilename + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Looks at the content of the Library passed in and if the type is texl/cql extracts it and decodes
     * it and writes it to the filename passed in
     *
     * @param theLibrary
     * @param cqlFilename
     */
    private void extractR4CQL(org.hl7.fhir.r4.model.Library theLibrary, String cqlFilename) {
        List<org.hl7.fhir.r4.model.Attachment> contents = theLibrary.getContent();
        for (org.hl7.fhir.r4.model.Attachment content : contents) {
            if (content.getContentType().equals("text/cql")) {
                byte[] encodedBytes = content.getData();
                String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
                try {
                    File outputFile = new File(cqlFilename);
                    // Ensure the parent directory exists
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
                } catch (IOException e) {
                    throw new RuntimeException(cqlFilename + ": " + e.getMessage());
                }
            }
        }
    }

}
