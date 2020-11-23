package org.opencds.cqf.tooling.operation;

import org.junit.Test;
import org.opencds.cqf.tooling.Operation;

public class GenerateCQLFromDroolOperationTest {
    @Test
    public void test_worked() {
        String operation = "GenerateCQLFromDrool";
        String encodingPath = "C:\\Users\\jreys\\Documents\\src\\CQLGenerationDocs\\ChlamydiaConditionCriteriaRels.json";
        String outputPath = "C:\\Users\\jreys\\Documents\\src\\CQLGenerationDocs\\generatedCQL.cql";
        String[] args = { "-" + operation, "-op=" + outputPath, "-efp=" + encodingPath };
        Operation generateCQLFromDroolOperation = new GenerateCQLFromDroolOperation();
        generateCQLFromDroolOperation.execute(args);
    }
}
