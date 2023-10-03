package org.opencds.cqf.tooling.acceleratorkit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

public class DTProcessorTest extends BaseProcessorTest{
    static final String resourcesPath = "src/test/resources/acceleratorkit";
    static final String whoDecisionTableInputFileName = "WHO-SRH-21.2-mini-eng.xlsx";
    //default
    private String[] fileTypes = new String[]{"json"};

    static final String dtGeneratedInputDirectory = separator + "out" + separator + "dt" +
            separator + "input" + separator;
    static final String dtGeneratedOutputDirectory = separator + "dt" + separator + "input" + separator;

    static final String resourcePathInputDirectory = resourcesPath + dtGeneratedInputDirectory;
    static final String resourcePathOutputDirectory = resourcesPath + dtGeneratedOutputDirectory;

    static final Logger logger = LoggerFactory.getLogger(DTProcessorTest.class);

    @Test
    public void validateWHOContent() {
        //execute to generate the decision table files
        String whoDecisionTableInputFilePath = java.nio.file.Path.of(resourcesPath, whoDecisionTableInputFileName).toString();
        assertNotNull(whoDecisionTableInputFilePath);

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
        compareFiles(resourcePathInputDirectory + "cql",
                resourcePathOutputDirectory + "cql");
        logger.info("End of CQL comparision");
    }

    private void comparePageContent() {
        compareFiles(resourcePathInputDirectory + "pagecontent",
                resourcePathOutputDirectory + "pagecontent");
        logger.info("End of Page Content comparision");
    }

    private void compareResources() {
        compareFiles(resourcePathInputDirectory + "resources",
                resourcePathOutputDirectory + "resources");
        logger.info("End of Resources comparision");
    }
}