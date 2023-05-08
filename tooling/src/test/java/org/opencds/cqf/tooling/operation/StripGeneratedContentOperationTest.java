package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

public class StripGeneratedContentOperationTest {

    @Test
    public void test_strip_generated_content() throws URISyntaxException {
        String dataInputPath = "strip-resources";
        String operation = "StripGeneratedContent";
        String inputFilePath = StripGeneratedContentOperationTest.class.getResource(dataInputPath).toURI().getPath();
        String outputPath = "target/test-output/strip-generated-content";
        String version = "r4";
        String[] args = { "-" + operation, "-ptr=" + inputFilePath, "-op=" + outputPath, "-v=" + version };
        Operation stripGeneratedContentOperation = new StripGeneratedContentOperation();
        stripGeneratedContentOperation.execute(args);
    }
}
