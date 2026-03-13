package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.nio.file.Paths;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.spreadsheet.QICoreElementsToSpreadsheet;
import org.testng.Assert;

public class QICoreElementsToSpreadsheetTest {
    // TODO: Fails on Windows
    // @Test
    public void verifySpreadsheetWithDataElementsFromProfile() {
        String inputPath =
                System.getenv("PWD") + "/src/test/resources/org/opencds/cqf/tooling/operation/profiles/FHIR-Spec";
        String resourcePaths = "QI-Core/4.1.0";
        String operation = "QICoreElementsToSpreadsheet";
        String modelName = "QICore";
        String modelVersion = "4.1.0";
        String outputPath = Paths.get("target", "test-output", "profileToSpreadsheet",
                "QI Core Element Analysis 11").toString();
        String[] args = {
            "-" + operation,
            "-ip=" + inputPath,
            "-op=" + outputPath,
            "-resourcepaths=" + resourcePaths,
            "-mn=" + modelName,
            "-mv=" + modelVersion
        };
        Operation qiCoreElementsToSpreadsheet = new ExecutableOperationAdapter(new QICoreElementsToSpreadsheet());
        qiCoreElementsToSpreadsheet.execute(args);
        File spreadsheet = Paths.get(outputPath, modelName + modelVersion + ".xlsx").toFile();
        Assert.assertTrue(spreadsheet.exists());
    }
}
