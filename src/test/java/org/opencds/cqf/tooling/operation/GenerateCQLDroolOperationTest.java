package org.opencds.cqf.tooling.operation;

import org.junit.Test;
import org.opencds.cqf.tooling.Operation;

public class GenerateCQLDroolOperationTest {
    @Test
    public void test_worked() {
        String operation = "GenerateCQLDrool";
        String encodingPath = "C:\\Users\\jreys\\Documents\\src\\CQLGenerationDocs\\ChlamydiaConditionCriteriaRels.json";
        String outputPath = "C:\\Users\\jreys\\Documents\\src\\CQLGenerationDocs\\generatedCQL.cql";
        String[] args = { "-" + operation, "-op=" + outputPath, "-efp=" + encodingPath };
        Operation generateCQLDroolOperation = new GenerateCQLDroolOperation();
        generateCQLDroolOperation.execute(args);
    }
}
