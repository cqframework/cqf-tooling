package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ExtractMatBundleOperation extends Operation {

    private String inputFile;
    private String version = "r4";
    private FhirContext context;
    private String encoding;
    private boolean suppressNarrative = true;

    @Override
    public void execute(String[] args) {
        boolean isDirFlagPresent = false;
        String inputDir = null;

        //loop through args and prepare approach:
        for (int i = 0; i < args.length; i++) {
            if (i == 0 && args[i].equalsIgnoreCase("-ExtractMatBundle")) {
                continue; //
            }

            if (args[i].equalsIgnoreCase("-dir")) {
                isDirFlagPresent = true;
                // The next argument should be the directory path
                if (i + 1 < args.length) {
                    inputDir = args[i + 1];
                    inputDir = inputDir.replace("%20", " "); // TODO: use URI instead?
                }
                continue;
            }

            if (i == 1 && !isDirFlagPresent) {
                inputFile = args[i];
                inputFile = inputFile.replace("%20", " "); // TODO: use URI instead?
                if (inputFile == null) {
                    throw new IllegalArgumentException("The path to a bundle file is required");
                }
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
        }

        //args processed, is this using -dir arg? Treat as group of files, extract each individually,
        // set inputFile member variable as loop proceeds:
        if (isDirFlagPresent) {
            // Process files in the specified directory
            File inputDirectory = new File(inputDir);
            if (!inputDirectory.isDirectory()) {
                throw new IllegalArgumentException("The path specified with -dir is not a directory.");
            }

            File[] filesInDir = inputDirectory.listFiles();
            if (filesInDir != null) {
                for (File file : filesInDir) {
                    if (file.isFile()) {
                        inputFile = file.getAbsolutePath();
                        // Call your existing processing logic for inputFile
                        processSingleFile();
                    }
                }
            }

            //single file, allow processSingleFile() to run with currently assigned inputFile:
        } else {
            // Process a single file as before
            processSingleFile();
        }

        LogUtils.info("Extraction completed successfully");
    }


    private void processSingleFile() {
        LogUtils.info(String.format("Extracting MAT bundle from %s", inputFile));

        // Open the file to validate it
        File bundleFile = new File(inputFile);
        if (bundleFile.isDirectory()) {
            throw new IllegalArgumentException("The path to a bundle file is required");
        }

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
        } else if (bundleFile.getPath().endsWith(".json")) {
            encoding = "json";
            try {
                bundle = context.newJsonParser().parseResource(new FileReader(bundleFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("The path to a bundle file of type json or xml is required");
        }

        // Now call the Bundle utilities to extract the bundle
        String outputDir = bundleFile.getAbsoluteFile().getParent();
        if (version.equals("stu3")) {
            BundleUtils.extractStu3Resources((org.hl7.fhir.dstu3.model.Bundle) bundle, encoding, outputDir,
                    suppressNarrative);
        } else if (version.equals("r4")) {
            BundleUtils.extractR4Resources((org.hl7.fhir.r4.model.Bundle) bundle, encoding, outputDir,
                    suppressNarrative);
        }

        // Now move and properly rename the files
        moveAndRenameFiles(outputDir);

        LogUtils.info("Extraction completed successfully");
    }


    /**
     * Iterates through the files and properly renames and moves them to the proper place
     *
     * @param outputDir
     */
    private void moveAndRenameFiles(String outputDir) {
        File[] extractedFiles = new File(outputDir).listFiles();
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
                    ResourceUtils.outputResourceByName(theResource, "json", context,
                            newLibraryDirectory.toString(), resourceName);

                    // Now extract the CQL from the library file
                    String cqlFilename = Paths.get(newCqlDirectory.toString(), resourceName) + ".cql";
                    extractStu3CQL(theLibrary, cqlFilename);
                } else if (theResource instanceof org.hl7.fhir.dstu3.model.Measure) {
                    org.hl7.fhir.dstu3.model.Measure theMeasure = (org.hl7.fhir.dstu3.model.Measure) theResource;
                    resourceName = theMeasure.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theMeasure.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context,
                            newMeasureDirectory.toString(), resourceName);
                }
            } else if (version == "r4") {
                if (theResource instanceof org.hl7.fhir.r4.model.Library) {
                    org.hl7.fhir.r4.model.Library theLibrary = (org.hl7.fhir.r4.model.Library) theResource;
                    resourceName = theLibrary.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theLibrary.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context,
                            newLibraryDirectory.toString(), resourceName);

                    // Now extract the CQL from the library file
                    String cqlFilename = Paths.get(newCqlDirectory.toString(), resourceName) + ".cql";
                    extractR4CQL(theLibrary, cqlFilename);
                } else if (theResource instanceof org.hl7.fhir.r4.model.Measure) {
                    org.hl7.fhir.r4.model.Measure theMeasure = (org.hl7.fhir.r4.model.Measure) theResource;
                    resourceName = theMeasure.getName();

                    // Set the id to the name regardless of what it is now for publishing
                    theMeasure.setId(resourceName);

                    // Forcing the encoding to JSON here to make everything the same in input directory
                    ResourceUtils.outputResourceByName(theResource, "json", context,
                            newMeasureDirectory.toString(), resourceName);
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
