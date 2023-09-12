package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
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
import java.util.Base64;
import java.util.List;

public class ExtractMatBundleOperation extends Operation {

    public static final String ERROR_BUNDLE_FILE_IS_REQUIRED = "The path to a bundle file is required";
    public static final String ERROR_DIR_IS_NOT_A_DIRECTORY = "The path specified with -dir is not a directory.";
    public static final String ERROR_DIR_IS_EMPTY = "The path specified with -dir is empty.";
    public static final String INFO_EXTRACTION_SUCCESSFUL = "Extraction completed successfully";
    private String version = "r4";
    private FhirContext context;
    private String encoding;
    private boolean suppressNarrative = true;

    @Override
    public void execute(String[] args) {

        boolean directoryFlagPresent = false;
        String inputLocation = null;
        //loop through args and prepare approach:
        for (int i = 0; i < args.length; i++) {
            System.out.println("ExtractMatBundle: " + args[i]);
            if (i == 0 && args[i].equalsIgnoreCase("-ExtractMatBundle")) {
                continue; //
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
                case "encoding":
                case "e":
                    encoding = value.toLowerCase();
                    break;
                case "supressNarrative":
                case "sn":
                    if (value.equalsIgnoreCase("false")) {
                        suppressNarrative = false;
                    }
                    break;
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "version":
                case "v":
                    version = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }//end arg loop

        System.out.println("ExtractMatBundle, inputLocation: " + inputLocation);
        //determine location is provided by user, if not, throw arugment exception:
        if (inputLocation == null) {
            throw new IllegalArgumentException(ERROR_BUNDLE_FILE_IS_REQUIRED);
        }

        System.out.println("ExtractMatBundle, directoryFlagPresent: " + directoryFlagPresent);
        //if -dir was found, treat inputLocation as directory:
        if (directoryFlagPresent) {
            File[] filesInDir = getFiles(inputLocation);

            System.out.println("ExtractMatBundle, filesInDir.length: " + filesInDir.length);
            if (filesInDir.length == 0) {
                throw new IllegalArgumentException(ERROR_DIR_IS_EMPTY);
            } else {

                for (File file : filesInDir) {
                    processSingleFile(file.getAbsolutePath());
                }
            }
        } else {
            //single file, allow processSingleFile() to run with currently assigned inputFile:
            processSingleFile(inputLocation);
        }

        LogUtils.info(INFO_EXTRACTION_SUCCESSFUL);
    }

    private static File[] getFiles(String inputLocation) {
        //ensure the input is a directory before trying to return a list of its files:
        if (!new File(inputLocation).isDirectory()) {
            throw new IllegalArgumentException(ERROR_DIR_IS_NOT_A_DIRECTORY);
        }
        // Process files in the specified directory
        return new File(inputLocation).listFiles();
    }


    private void processSingleFile(String inputFileLocation) {
        System.out.println("ExtractMatBundle, processSingleFile, inputFile: " + inputFileLocation);
        LogUtils.info(String.format("Extracting MAT bundle from %s", inputFileLocation));

        // Open the file to validate it
        File bundleFile = new File(inputFileLocation);

        System.out.println("ExtractMatBundle, processSingleFile, bundleFile.isDirectory(): " + bundleFile.isDirectory());
        if (bundleFile.isDirectory()) {
            throw new IllegalArgumentException(ERROR_BUNDLE_FILE_IS_REQUIRED);
        }

        System.out.println("ExtractMatBundle, processSingleFile, version: " + version);
        // Set the FhirContext based on the version specified
        if (version == null) {
            context = FhirContext.forR4Cached();
        } else {
            switch (version.toLowerCase()) {
                case "stu3":
                    context = FhirContext.forDstu3Cached();
                    break;
                case "r4":
                    context = FhirContext.forR4Cached();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }


        // Read in the Bundle
        IBaseResource bundle;
        if (bundleFile.getPath().endsWith(".xml")) {
            encoding = "xml";
            try {
                bundle = context.newXmlParser().parseResource(new FileReader(bundleFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            System.out.println("ExtractMatBundle, processSingleFile, bundle: " + bundle.toString());
        } else if (bundleFile.getPath().endsWith(".json")) {
            encoding = "json";
            try {
                bundle = context.newJsonParser().parseResource(new FileReader(bundleFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            System.out.println("ExtractMatBundle, processSingleFile, bundle: " + bundle.toString());

        } else {
            throw new IllegalArgumentException("The path to a bundle file of type json or xml is required");
        }


        //sometimes tests leave library or measure files behind so we want to make sure we only iterate over bundle files:
        if (!(bundle instanceof org.hl7.fhir.dstu3.model.Bundle || bundle instanceof org.hl7.fhir.r4.model.Bundle)){
            LogUtils.info("Not a recognized bundle: " + inputFileLocation);
            System.out.println("Not a recognized bundle: " + inputFileLocation);
            return;
        }

        // Now call the Bundle utilities to extract the bundle
        String outputDir = bundleFile.getAbsoluteFile().getParent();
        if (version.equals("stu3") && bundle instanceof org.hl7.fhir.dstu3.model.Bundle) {
            try {
                BundleUtils.extractStu3Resources((org.hl7.fhir.dstu3.model.Bundle) bundle, encoding, outputDir, suppressNarrative);
                System.out.println("ExtractMatBundle, BundleUtils.extractStu3Resources: success");
            } catch (Exception e) {
                System.out.println("ExtractMatBundle, BundleUtils.extractStu3Resources: failure: " + e.getMessage());
            }
        } else if (version.equals("r4") && bundle instanceof org.hl7.fhir.r4.model.Bundle) {
            try {
                BundleUtils.extractR4Resources((org.hl7.fhir.r4.model.Bundle) bundle, encoding, outputDir, suppressNarrative);
                System.out.println("ExtractMatBundle, BundleUtils.extractR4Resources: success");
            } catch (Exception e) {
                System.out.println("ExtractMatBundle, BundleUtils.extractR4Resources: failure: " + e.getMessage());
            }
        }

        // Now move and properly rename the files
        moveAndRenameFiles(outputDir);
        System.out.println("ExtractMatBundle, moveAndRenameFiles: success");
        LogUtils.info(INFO_EXTRACTION_SUCCESSFUL);

        System.out.println(INFO_EXTRACTION_SUCCESSFUL);
    }


    /**
     * Iterates through the files and properly renames and moves them to the proper place
     *
     * @param outputDir
     */
    private void moveAndRenameFiles(String outputDir) {
        File[] extractedFiles = new File(outputDir).listFiles();
        assert extractedFiles != null;
        for (File extractedFile : extractedFiles) {
            IBaseResource theResource = null;
            if (extractedFile.getPath().endsWith(".xml")) {
                try {
                    theResource = context.newXmlParser().parseResource(new FileReader(extractedFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            } else if (extractedFile.getPath().endsWith(".json")) {
                try {
                    theResource = context.newJsonParser().parseResource(new FileReader(extractedFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
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
            } else if (version == "r4") {
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
