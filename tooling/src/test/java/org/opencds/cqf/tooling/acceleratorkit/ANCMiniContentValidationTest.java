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

        String outPath = outDir.getAbsolutePath();

        String spreadSheetPath = Path.of(resourcesPath, spreadSheet).toString();
        String[] args = { "-ProcessAcceleratorKit", "-s=ANCM", "-pts=" + spreadSheetPath,
                "-dep=" + dataDictionarySheets, "-op=" + outPath };

        Processor acceleratorKitProcessor = new Processor();
        // execute to generate the data dictionary files
        acceleratorKitProcessor.execute(args);
        // structure definitions
        compareProfilesStructureDefinitions(outPath);
        // vocabulary
        compareVocabulary(outPath);
        // resources
        compareResources(outPath);
        // cql
        compareCql(outPath);
        // examples
        compareExamples(outPath);
        // extensions
        compareExtensions(outPath);
        // tests
        compareTests(outPath);
    }

    private void compareProfilesStructureDefinitions(String root) {
        countFiles(Path.of(root, "input", "profiles").toString(), 0);
    }

    private void compareCql(String root) {
        countFiles(Path.of(root, "input", "cql").toString(), 4);
    }

    private void compareExamples(String root) {
        countFiles(Path.of(root, "input", "examples").toString(), 0);
    }

    private void compareExtensions(String root) {
        countFiles(Path.of(root, "input", "extensions").toString(), 0);
    }

    private void compareResources(String root) {
        countFiles(Path.of(root, "input", "resources").toString(), 0);
    }

    private void compareTests(String root) {
        countFiles(Path.of(root, "input", "tests").toString(), 0);
    }

    private void compareVocabulary(String root) {
        countFiles(Path.of(root, "input", "vocabulary").toString(), 1);
    }

}