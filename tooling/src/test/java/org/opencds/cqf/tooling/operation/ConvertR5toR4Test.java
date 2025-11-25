package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConvertR5toR4Test {

    private final String ENCODING_ARGUMENT = "-e=";
    private final String OUTPUT_FILENAME_ARGUMENT = "-ofn=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String PATH_TO_DIRECTORY_ARGUMENT = "-ptd=";

    private static String getFullPath(String relativePath) {
        return System.getProperty("user.dir") + File.separator + relativePath;
    }

    private Bundle parseR4BundleFromFile(File bundleFile) {
        try {
            var bundleJson = Files.readString(bundleFile.toPath());
            var r4Context = FhirContext.forR4();
            var parsed = r4Context.newJsonParser().parseResource(bundleJson);

            Assert.assertTrue(parsed instanceof Bundle,
                    "Output resource should be an R4 Bundle.");

            return (Bundle) parsed;
        } catch (IOException e) {
            Assert.fail("Failed to read or parse converted bundle: " + e.getMessage());
            return null; // unreachable, but required by compiler
        }
    }

    private void convertSingleResource(String sourceDirectory, String outputPath, String outputFile) {
        var args = new String[5];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + sourceDirectory;
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + outputPath;
        args[4] = OUTPUT_FILENAME_ARGUMENT + outputFile;

        IOUtils.initializeDirectory(outputPath);
        var converter = new ConvertR5toR4();
        try {
            converter.execute(args);

            var resultDir = new File(outputPath);
            var actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
            Assert.assertNotNull(actualFiles, "Conversion folder should not be null.");
            Assert.assertEquals(actualFiles.length, 1, "Conversion folder should only have a single file.");
            var expectedFileName = outputFile + ".json";
            Assert.assertEquals(actualFiles[0].getName(), expectedFileName,
                    "Converted file name should match the expected output file name.");

            var bundle = parseR4BundleFromFile(actualFiles[0]);
            Assert.assertNotNull(bundle);
            Assert.assertEquals(bundle.getEntry().size(), 1,
                    "Bundle should contain a single entry for a single source resource.");
        } finally {
            try {
                IOUtils.deleteDirectory(outputPath);
            } catch (IOException e) {
                Assert.fail("Failed to delete output directory: " + e.getMessage());
            }
        }
    }

    private void convertMultipleResources(String sourceDirectory, String outputPath) {
        var args = new String[4];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + sourceDirectory;
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + outputPath;

        IOUtils.initializeDirectory(outputPath);
        var converter = new ConvertR5toR4();
        try {
            converter.execute(args);

            var sourceDir = new File(sourceDirectory);
            var sourceFiles = sourceDir.listFiles((dir, name) -> name.endsWith(".json"));
            var expectedCount = sourceFiles == null ? 0 : sourceFiles.length;

            var resultDir = new File(outputPath);
            var actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
            Assert.assertNotNull(actualFiles, "Conversion folder should not be null.");
            Assert.assertEquals(actualFiles.length, 1,
                    "Conversion should produce a single R4 bundle file.");

            var bundleFile = actualFiles[0];
            var bundle = parseR4BundleFromFile(bundleFile);
            Assert.assertNotNull(bundle);
            Assert.assertEquals(bundle.getEntry().size(), expectedCount,
                    "Bundle should contain one entry per source resource.");
        } finally {
            try {
                IOUtils.deleteDirectory(outputPath);
            } catch (IOException e) {
                Assert.fail("Failed to delete output directory: " + e.getMessage());
            }
        }
    }

    @Test
    public void testExecute_ConvertSingleResourceCodeSystem() {
        convertSingleResource(
                getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/code-system"),
                getFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/code-system"),
                "r4-code-system-action-code"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceConceptMap() {
        convertSingleResource(
                getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/concept-map"),
                getFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/concept-map"),
                "r4-concept-map-observation-status"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceStructuredDefinition() {
        convertSingleResource(
                getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/structured-definition"),
                getFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/structured-definition"),
                "r4-structured-definition-code"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceValueset() {
        convertSingleResource(
                getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/valueset"),
                getFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/valueset"),
                "r4-valueset-event-status"
        );
    }

    @Test
    public void testExecute_ConvertMultipleResources() {
        convertMultipleResources(
                getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/multiple-resources"),
                getFullPath("target/test-output/convertR5toR4SingleResourceResults/multiple-resources")
        );
    }

    @Test
    public void testExecute_R5BundleConvertedToR4BundleWithR4Entries() {
        var inputDir = getFullPath("target/test-output/convertR5toR4SingleResourceResults/r5-bundle-input");
        var outputDir = getFullPath("target/test-output/convertR5toR4SingleResourceResults/r5-bundle-output");

        IOUtils.initializeDirectory(inputDir);
        IOUtils.initializeDirectory(outputDir);

        try {
            // Build an R5 Bundle with a Patient and an Observation entry
            var r5Bundle = new org.hl7.fhir.r5.model.Bundle();
            r5Bundle.setType(org.hl7.fhir.r5.model.Bundle.BundleType.COLLECTION);

            var r5Patient = new org.hl7.fhir.r5.model.Patient();
            r5Patient.setId("patient-1");
            r5Bundle.addEntry().setResource(r5Patient);

            var r5Observation = new org.hl7.fhir.r5.model.Observation();
            r5Observation.setId("observation-1");
            r5Bundle.addEntry().setResource(r5Observation);

            // Write the R5 Bundle as a JSON file into the input directory
            var r5Context = FhirContext.forR5();
            var r5Json = r5Context.newJsonParser().encodeResourceToString(r5Bundle);
            var inputFile = new File(inputDir, "r5-bundle.json");
            Files.writeString(inputFile.toPath(), r5Json);

            // Reuse convertSingleResource (using "r5-bundle" as the output file name)
            convertSingleResource(inputDir, outputDir, "r5-bundle");
        } catch (IOException e) {
            Assert.fail("Failed to write R5 bundle input file: " + e.getMessage());
        } finally {
            try {
                IOUtils.deleteDirectory(inputDir);
            } catch (IOException e) {
                Assert.fail("Failed to delete input directory: " + e.getMessage());
            }
        }
    }

    @Test(expectedExceptions = Exception.class)
    public void testExecute_InvalidEncodingThrowsException() {
        var sourceDirectory = getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/code-system");
        var outputDirectory = getFullPath("target/test-output/convertR5toR4SingleResourceResults/invalid-encoding");

        IOUtils.initializeDirectory(outputDirectory);

        var args = new String[4];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + sourceDirectory;
        args[2] = ENCODING_ARGUMENT + "invalid-encoding";
        args[3] = OUTPUT_PATH_ARGUMENT + outputDirectory;

        try {
            var converter = new ConvertR5toR4();
            converter.execute(args);
        } finally {
            try {
                IOUtils.deleteDirectory(outputDirectory);
            } catch (IOException e) {
                Assert.fail("Failed to delete output directory: " + e.getMessage());
            }
        }
    }

    @Test(expectedExceptions = Exception.class)
    public void testExecute_InvalidPathToDirectoryThrowsException() {
        var invalidDirectory = getFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/does-not-exist");

        var args = new String[3];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + invalidDirectory;
        args[2] = ENCODING_ARGUMENT + "json";

        var converter = new ConvertR5toR4();
        converter.execute(args);
    }
}