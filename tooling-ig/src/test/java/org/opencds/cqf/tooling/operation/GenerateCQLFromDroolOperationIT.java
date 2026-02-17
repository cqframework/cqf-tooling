package org.opencds.cqf.tooling.operation;

import java.net.URISyntaxException;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.cql.GenerateCQLFromDrool;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class GenerateCQLFromDroolOperationIT {
    @Test
    public void test_worked() throws URISyntaxException {
        String dataInputPath = "default.json";
        String operation = "GenerateCQLFromDrool";
        String inputFilePath = GenerateCQLFromDroolOperationIT.class
                .getResource(dataInputPath)
                .toURI()
                .getPath();
        String outputPath = "target/test-output/cql-from-drool";
        String encoding = "json";
        String[] args = {"-" + operation, "-ifp=" + inputFilePath, "-op=" + outputPath, "-e=" + encoding};
        Operation generateCQLFromDroolOperation = new ExecutableOperationAdapter(new GenerateCQLFromDrool());
        generateCQLFromDroolOperation.execute(args);
    }
}
