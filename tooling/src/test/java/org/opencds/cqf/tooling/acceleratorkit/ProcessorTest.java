package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;


public class ProcessorTest extends BaseProcessorTest{
    static final String whoInputFileName = "WHO-ANC-mini.xlsx";
    static final String testCasesFileName = "ANC Test Cases-mini.xlsx";
    static final String dataDictionarySheets = "ANC.A. Registration,ANC.B5 Quick check,ANC.End End";
    //default
    private String[] fileTypes = new String[]{"json", "cql", "md"};

    @Test
    public void validateWHOContent(){
       String whoInputFilePath = java.nio.file.Path.of(resourcesPath, whoInputFileName).toString();
       assertNotNull(whoInputFilePath);
       StringBuilder expectedFilePath = new StringBuilder(resourcesPath).append("/").append(whoInputFileName);
       assertEquals(whoInputFilePath, expectedFilePath.toString());

        String whoTestCaseInputFilePath = java.nio.file.Path.of(resourcesPath, testCasesFileName).toString();
        assertNotNull(whoTestCaseInputFilePath);
        StringBuilder expectedWhoTestCaseFilePath = new StringBuilder(resourcesPath).append("/").append(testCasesFileName);
        assertEquals(whoTestCaseInputFilePath, expectedWhoTestCaseFilePath.toString());

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
        compareFiles(resourcesPath+"/out/dd/input/profiles", resourcesPath+"/dd/input/profiles");
    }

    private void compareCql() {
        compareFiles(resourcesPath + "/out/dd/input/cql", resourcesPath + "/dd/input/cql");
    }

    private void compareExamples() {
        compareFiles(resourcesPath + "/out/dd/input/examples", resourcesPath + "/dd/input/examples");
    }

    private void compareExtensions() {
        compareFiles(resourcesPath + "/out/dd/input/extensions", resourcesPath + "/dd/input/extensions");
    }

    private void compareResources() {
        compareFiles(resourcesPath + "/out/dd/input/resources", resourcesPath + "/dd/input/resources");
    }

    private void compareTests() {
        compareFiles(resourcesPath + "/out/dd/input/tests", resourcesPath + "/dd/input/tests");
    }

    private void compareVocabulary() {
        String vocabularyInputPath = resourcesPath + "/out/dd/input/vocabulary";
        String vocabularyComparePath = resourcesPath + "/dd/input/vocabulary";
        compareFiles(vocabularyInputPath, vocabularyComparePath);
    }

}