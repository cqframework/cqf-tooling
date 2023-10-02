package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;


public class ProcessorTest extends BaseProcessorTest{
    static final String whoInputFileName = "WHO-ANC-mini.xlsx";
    static final String testCasesFileName = "ANC Test Cases-mini.xlsx";
    static final String dataDictionarySheets = "ANC.A. Registration,ANC.B5 Quick check,ANC.End End";

    static final String ddGeneratedInputDirectory = separator + "out" + separator + "dd" +
                                                    separator + "input" + separator;
    static final String ddGeneratedOutputDirectory = separator + "dd" + separator + "input" + separator;

    static final String resourcePathInputDirectory = resourcesPath + ddGeneratedInputDirectory;
    static final String resourcePathOutputDirectory = resourcesPath + ddGeneratedOutputDirectory;

    @Test
    public void validateWHOContent(){
       String whoInputFilePath = java.nio.file.Path.of(resourcesPath, whoInputFileName).toString();
       assertNotNull(whoInputFilePath);

        String whoTestCaseInputFilePath = java.nio.file.Path.of(resourcesPath, testCasesFileName).toString();
        assertNotNull(whoTestCaseInputFilePath);

        String[] args = {"-ProcessAcceleratorKit", "-s=ANCM", "-pts="+whoInputFilePath,
                "-dep="+dataDictionarySheets, "-op="+resourcesPath+"/out/dd", "-tc="+whoTestCaseInputFilePath};

        Processor acceleratorKitProcessor = new Processor();
        //execute to generate the data dictionary files
        acceleratorKitProcessor.execute(args);
        //structure definitions
        compareProfilesStructureDefinitions();
        //vocabulary
        compareVocabulary();
        //resources
        compareResources();
        //cql
        compareCql();
        //examples
        compareExamples();
        //extensions
        compareExtensions();
        //tests
        compareTests();
    }

    private void compareProfilesStructureDefinitions() {
        compareFiles(resourcePathInputDirectory+"profiles",
                resourcePathOutputDirectory+"profiles");
    }

    private void compareCql() {
        compareFiles(resourcePathInputDirectory + "cql",
                resourcePathOutputDirectory + "cql");
    }

    private void compareExamples() {
        compareFiles(resourcePathInputDirectory + "examples",
                resourcePathOutputDirectory + "examples");
    }

    private void compareExtensions() {
        compareFiles(resourcePathInputDirectory + "extensions",
                resourcePathOutputDirectory + "extensions");
    }

    private void compareResources() {
        compareFiles(resourcePathInputDirectory + "resources",
                resourcePathOutputDirectory + "resources");
    }

    private void compareTests() {
        compareFiles(resourcePathInputDirectory + "tests",
                resourcePathOutputDirectory + "tests");
    }

    private void compareVocabulary() {
        String vocabularyInputPath = resourcePathInputDirectory + "vocabulary";
        String vocabularyComparePath = resourcePathOutputDirectory + "vocabulary";
        compareFiles(vocabularyInputPath, vocabularyComparePath);
    }

}