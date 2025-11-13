package org.opencds.cqf.tooling.operation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ConvertR5toR4Test {

    private final String ENCODING_ARGUMENT = "-e=";
    private final String OUTPUT_FILENAME_ARGUMENT = "-ofn=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String PATH_TO_DIRECTORY_ARGUMENT = "-ptd=";
    private final String TYPE_ARGUMENT = "-t=";

    private static String GetFullPath(String relativePath) {
        return System.getProperty("user.dir") + File.separator + relativePath;
    }

    private void ClearDirectory(String directoryPath){
        try (Stream<Path> stream = Files.walk(Paths.get(directoryPath))) {
            stream.filter(Files::isRegularFile) // Filter for regular files only
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            System.err.println("Failed to delete file: " + file + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking directory: " + e.getMessage());
        }
    }

    private void ConvertSingleResource(String sourceDirectory, String outputPath, String outputFile) {
        String[] args = new String[5];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + sourceDirectory;
        args[2] = ENCODING_ARGUMENT+ "json";
        args[3] = OUTPUT_PATH_ARGUMENT + outputPath;
        args[4] = OUTPUT_FILENAME_ARGUMENT + outputFile;

        ClearDirectory(outputPath);
        ConvertR5toR4 converter = new ConvertR5toR4();
        converter.execute(args);

        File resultDir = new File(outputPath);
        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Conversion folder should not be null.");
        Assert.assertEquals(actualFiles.length,1, "Conversion folder should only have a single file.");
    }

    private void ConvertMulitpleResources(String sourceDirectory, String outputPath) {
        String[] args = new String[4];
        args[0] = "-ConvertR5toR4";
        args[1] = PATH_TO_DIRECTORY_ARGUMENT + sourceDirectory;
        args[2] = ENCODING_ARGUMENT+ "json";
        args[3] = OUTPUT_PATH_ARGUMENT + outputPath;

        ClearDirectory(outputPath);
        ConvertR5toR4 converter = new ConvertR5toR4();
        converter.execute(args);

        File resultDir = new File(outputPath);
        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Conversion folder should not be null.");
        //Assert.assertEquals(actualFiles.length,1, "Conversion folder should only have a single file.");
    }

    @Test
    public void testExecute_ConvertSingleResourceCodeSystem() {
        ConvertSingleResource(
                GetFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/code-system"),
                GetFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/code-system"),
                "r4-code-system-action-code"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceConceptMap() {
        ConvertSingleResource(
                GetFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/concept-map"),
                GetFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/concept-map"),
                "r4-concept-map-observation-status"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceStructuredDefinition() {
        ConvertSingleResource(
                GetFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/structured-definition"),
                GetFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/structured-definition"),
                "r4-structured-definition-code"
        );
    }

    @Test
    public void testExecute_ConvertSingleResourceValueset() {
        ConvertSingleResource(
                GetFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/single-resource/valueset"),
                GetFullPath("target/test-output/convertR5toR4SingleResourceResults/single-resource/valueset"),
                "r4-valueset-event-status"
        );
    }

    @Test
    public void testExecute_ConvertMultipleResources() {
        ConvertMulitpleResources(
                GetFullPath("src/test/resources/org/opencds/cqf/tooling/operation/convertR5toR4/multiple-resources"),
                GetFullPath("target/test-output/convertR5toR4SingleResourceResults/multiple-resources")
        );
    }
}