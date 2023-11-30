package org.opencds.cqf.tooling.acceleratorkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.Test;

public class ANCMiniContentValidationTest extends BaseProcessorTest {
    private static final String resourcesPath = "src/test/resources";
    private static final String spreadSheet = "acceleratorkit/ANC Test Cases-mini.xlsx";
    private static final String dataDictionarySheets = "ANC.A. Registration,ANC.B5 Quick check,ANC.End End";

    @Test
    public void validateANCMiniContent() {
        File outDir = null;
        try {
            outDir = Files.createTempDirectory("mini").toFile();
            outDir.deleteOnExit();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        String spreadSheetPath = Path.of(resourcesPath, spreadSheet).toString();
        String[] args = { "-ProcessAcceleratorKit", "-s=ANCM", "-pts=" + spreadSheetPath,
                "-dep=" + dataDictionarySheets, "-op=" + outDir.getAbsolutePath() };

        Processor acceleratorKitProcessor = new Processor();
        // execute to generate the data dictionary files
        acceleratorKitProcessor.execute(args);
        // structure definitions
        // compareProfilesStructureDefinitions();
        // // vocabulary
        // compareVocabulary();
        // // resources
        // compareResources();
        // // cql
        // compareCql();
        // // examples
        // compareExamples();
        // // extensions
        // compareExtensions();
        // // tests
        // compareTests();
    }

    // private void compareProfilesStructureDefinitions() {
    //     compareFiles(resourcePathInputDirectory + "profiles",
    //             resourcePathOutputDirectory + "profiles");
    // }

    // private void compareCql() {
    //     compareFiles(resourcePathInputDirectory + "cql",
    //             resourcePathOutputDirectory + "cql");
    // }

    // private void compareExamples() {
    //     compareFiles(resourcePathInputDirectory + "examples",
    //             resourcePathOutputDirectory + "examples");
    // }

    // private void compareExtensions() {
    //     compareFiles(resourcePathInputDirectory + "extensions",
    //             resourcePathOutputDirectory + "extensions");
    // }

    // private void compareResources() {
    //     compareFiles(resourcePathInputDirectory + "resources",
    //             resourcePathOutputDirectory + "resources");
    // }

    // private void compareTests() {
    //     compareFiles(resourcePathInputDirectory + "tests",
    //             resourcePathOutputDirectory + "tests");
    // }

    // private void compareVocabulary() {
    //     String vocabularyInputPath = resourcePathInputDirectory + "vocabulary";
    //     String vocabularyComparePath = resourcePathOutputDirectory + "vocabulary";
    //     compareFiles(vocabularyInputPath, vocabularyComparePath);
    // }

}