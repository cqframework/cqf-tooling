package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class ExtractMatBundleOperation extends Operation {
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
    public static final String INFO_RESOURCE_ALREADY_PROCESSED = "This Resource has already been processed: ";

    private List<String> processedBundleCollection;

    @Override
    public void execute(String[] args) {

        processedBundleCollection = new ArrayList<>();

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
                        "/Development/ecqm-content-r4/bundles/mat/EXM124/EXM124.json -v=r4");
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

        List<Callable<Void>> tasks = new ArrayList<>();


        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            //if -dir was found, treat inputLocation as directory:
            if (directoryFlagPresent) {
                File[] filesInDir = new File(inputLocation).listFiles();
                if (filesInDir != null) {
                    //use recursive calls to build up task list:
                    tasks.addAll(processFilesInDir(filesInDir, version, suppressNarrative));

                    // Submit tasks and obtain futures
                    List<Future<Void>> futures = new ArrayList<>();
                    for (Callable<Void> task : tasks) {
                        futures.add(executorService.submit(task));
                    }

                    // Wait for all tasks to complete
                    for (Future<Void> future : futures) {
                        try {
                            future.get();
                        } catch (Exception e) {
                            LogUtils.putException("ExtractMatBundleOperation.execute", e);
                        }
                    }
                }
            } else {
                //single file, allow processSingleFile() to run with currently assigned inputFile:
                processSingleFile(new File(inputLocation), version, suppressNarrative);
            }


        } finally {
            executorService.shutdown();
        }
        LogUtils.info("Successfully extracted the following resources: " + processedBundleCollection);
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

        LogUtils.info(String.format("Extracting MAT bundle from %s", inputFileLocation));

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
                    LogUtils.putException("processSingleFile", new IllegalArgumentException("Unknown fhir version: " + version));
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
                LogUtils.info(ERROR_NOT_VALID + "\n" + inputFileLocation);
                return;
            }

        } else if (bundleFile.getPath().endsWith(".json")) {
            encoding = "json";
            try (FileReader reader = new FileReader(bundleFile)) {
                bundle = context.newJsonParser().parseResource(reader);
            } catch (Exception e) {
                LogUtils.info(ERROR_NOT_VALID + "\n" + inputFileLocation);
                return;
            }

        } else {
            LogUtils.info(ERROR_NOT_JSON_OR_XML + "\n" + inputFileLocation);
            return;
        }

        //sometimes tests leave library or measure files behind so we want to make sure we only iterate over bundle files:
        if (!(bundle instanceof org.hl7.fhir.dstu3.model.Bundle || bundle instanceof org.hl7.fhir.r4.model.Bundle)) {
            LogUtils.info(ERROR_NOT_VALID_BUNDLE + inputFileLocation);
            return;
        }

        IBaseResource processedBundle;

        //ensure the xml and json files are transaction Bundle types:
        if (bundle instanceof org.hl7.fhir.dstu3.model.Bundle) {
            if (!((org.hl7.fhir.dstu3.model.Bundle) bundle).getType().equals(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION)) {
                LogUtils.info("Invalid Bundle type in " + encoding + " file: " + inputFileLocation);
                return;
            } else {
                //Bundle is a valid transaction Bundle, now ensure its entries aren't duplicate to what has been
                //processed already:
                processedBundle = processDSTU3BundleResources((org.hl7.fhir.dstu3.model.Bundle) bundle);
            }

        } else if (bundle instanceof org.hl7.fhir.r4.model.Bundle) {
            if (!((org.hl7.fhir.r4.model.Bundle) bundle).getType().equals(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION)) {
                LogUtils.info("Invalid Bundle type in " + encoding + " file: " + inputFileLocation);
                return;
            } else {
                //Bundle is a valid transaction Bundle, now ensure its entries aren't duplicate to what has been
                //processed already:
                processedBundle = processR4BundleResources((org.hl7.fhir.r4.model.Bundle) bundle);
            }
        } else {
            LogUtils.info("Not a recognized bundle in " + encoding + "file: " + inputFileLocation);
            return;
        }

        //call the Bundle utilities to extract the bundle
        String outputDir = bundleFile.getAbsoluteFile().getParent();


        //ensure output path assigned by user is utilized:
        if (getOutputPath() != null && !getOutputPath().isEmpty()) {
            outputDir = getOutputPath();
        }

        BundleUtils.extractResources(processedBundle, encoding, outputDir, suppressNarrative, version);

        //move and properly rename the files
        moveAndRenameFiles(outputDir, context, version);
        LogUtils.info(INFO_EXTRACTION_SUCCESSFUL + ": " + inputFileLocation);
    }

    /**
     * This method returns an Bundle instance where certain entries are stripped should their resource ID turn up in our
     * collection of processed resource IDs
     *
     * @param bundle
     * @return
     */
    private org.hl7.fhir.dstu3.model.Bundle processDSTU3BundleResources(org.hl7.fhir.dstu3.model.Bundle bundle) {
        Iterator<org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent> entryIterator = bundle.getEntry().iterator();

        while (entryIterator.hasNext()) {
            org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry = entryIterator.next();
            if (entry.hasResource() && entry.getResource().hasIdElement()) {
                String resourceID = entry.getResource().getIdElement().getIdPart();
                if (processedBundleCollection.contains(resourceID)) {
                    LogUtils.info(INFO_RESOURCE_ALREADY_PROCESSED + resourceID);
                    entryIterator.remove();
                } else {
                    processedBundleCollection.add(resourceID);
                }
            }
        }

        return bundle;
    }

    /**
     * This method returns an Bundle instance where certain entries are stripped should their resource ID turn up in our
     * collection of processed resource IDs
     *
     * @param bundle
     * @return
     */
    private org.hl7.fhir.r4.model.Bundle processR4BundleResources(org.hl7.fhir.r4.model.Bundle bundle) {
        Iterator<org.hl7.fhir.r4.model.Bundle.BundleEntryComponent> entryIterator = bundle.getEntry().iterator();

        while (entryIterator.hasNext()) {
            org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry = entryIterator.next();
            if (entry.hasResource() && entry.getResource().hasId()) {
                String resourceID = entry.getResource().getId();
                if (processedBundleCollection.contains(resourceID)) {
                    LogUtils.info(INFO_RESOURCE_ALREADY_PROCESSED + resourceID);
                    entryIterator.remove();
                } else {
                    processedBundleCollection.add(resourceID);
                }
            }
        }

        return bundle;
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
//                    e.printStackTrace();
                    LogUtils.putException("moveAndRenameFiles", new RuntimeException(e.getMessage()));
                    continue;
                }
            } else if (extractedFile.getPath().endsWith(".json")) {
                try {
                    theResource = context.newJsonParser().parseResource(new FileReader(extractedFile));
                } catch (Exception e) {
//                    e.printStackTrace();
                    LogUtils.putException("moveAndRenameFiles", new RuntimeException(e.getMessage()));
                    continue;
                }
            }

            // The extractor code names them using the resource type and ID
            // We want to name them without the resource type, use name, and if needed version
            String resourceName;
            Path newOutputDirectory = Paths.get(outputDir.substring(0, outputDir.indexOf("bundles")), "input");
            Path newLibraryDirectory = Paths.get(newOutputDirectory.toString(), "resources/library");
            newLibraryDirectory.toFile().mkdirs();
            Path newCqlDirectory = Paths.get(newOutputDirectory.toString(), "cql");
            newCqlDirectory.toFile().mkdirs();
            Path newMeasureDirectory = Paths.get(newOutputDirectory.toString(), "resources/measure");
            newMeasureDirectory.toFile().mkdirs();
            if (version == "stu3) ") {
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
            } else if (version == VERSION_R4) {
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
                    FileUtils.writeByteArrayToFile(new File(cqlFilename), decodedBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
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
                    FileUtils.writeByteArrayToFile(new File(cqlFilename), decodedBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }

}
