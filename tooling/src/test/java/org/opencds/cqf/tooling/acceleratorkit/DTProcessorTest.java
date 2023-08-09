package org.opencds.cqf.tooling.acceleratorkit;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class DTProcessorTest extends BaseProcessorTest{
    static final String resourcesPath = "src/test/resources/acceleratorkit";
    static final String whoDecisionTableInputFileName = "WHO-SRH-21.2-mini-eng.xlsx";
    //default
    private String[] fileTypes = new String[]{"json"};

    @Test
    public void validateWHOContent() {
        //execute to generate the decision table files
        String whoDecisionTableInputFilePath = java.nio.file.Path.of(resourcesPath, whoDecisionTableInputFileName).toString();
        assertNotNull(whoDecisionTableInputFilePath);
        StringBuilder expectedWhoDecisionTableInputFileName = new StringBuilder(resourcesPath).append("/").append(whoDecisionTableInputFileName);
        assertEquals(whoDecisionTableInputFilePath, expectedWhoDecisionTableInputFileName.toString());
        String[] args = new String[]{"-ProcessDecisionTables", "-dtpf=ANC.DT",
                "-pts=" + whoDecisionTableInputFilePath, "-op=" + resourcesPath + "/out/dt"};
        DTProcessor dtProcessor = new DTProcessor();
        dtProcessor.execute(args);
        //cql
        compareCql();
        //page content
        comparePageContent();
        //resources
        compareResources();
    }

    private void compareCql() {
        compareFiles(resourcesPath + "/out/dt/input/cql", resourcesPath + "/dt/input/cql");
    }

    private void comparePageContent() {
        compareFiles(resourcesPath + "/out/dt/input/pagecontent", resourcesPath + "/dt/input/pagecontent");
    }

    private void compareResources() {
        compareFiles(resourcesPath + "/out/dt/input/resources", resourcesPath + "/dt/input/resources");
    }
}