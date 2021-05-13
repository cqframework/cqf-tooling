package org.opencds.cqf.tooling.operation;

import org.testng.annotations.Test;
import org.opencds.cqf.tooling.Operation;

public class GenerateCQLFromDroolOperationTest {
    @Test
    public void test_worked() {
        String operation = "GenerateCQLFromDrool";
        String inputFilePath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        String outputPath = "../CQLGenerationDocs/GeneratedDocs/elm";
        String encoding = "json";
        String[] args = { "-" + operation, "-ifp=" + inputFilePath, "-op=" + outputPath, "-e=" + encoding };
        Operation generateCQLFromDroolOperation = new GenerateCQLFromDroolOperation();
        generateCQLFromDroolOperation.execute(args);
    }
}
