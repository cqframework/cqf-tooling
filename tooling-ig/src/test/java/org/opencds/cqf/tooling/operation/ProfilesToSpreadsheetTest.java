package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.spreadsheet.ProfilesToSpreadsheet;
import org.testng.Assert;

public class ProfilesToSpreadsheetTest {
    // TODO: Fails on Windows...
    // @Test
    public void verifySpreadsheetFromProfile() throws URISyntaxException {
        String inputPath = Objects.requireNonNull(ProfilesToSpreadsheetTest.class
                        .getClassLoader()
                        .getResource("org/opencds/cqf/tooling/operation/profiles/FHIR-Spec"))
                .toURI()
                .getRawPath();
        String resourcePaths = "QI-Core/4.1.1";
        String operation = "ProfilesToSpreadsheet";
        String modelName = "QICore";
        String modelVersion = "4.1.1";
        String outputPath = Paths.get("target", "test-output", "profileToSpreadsheet",
                "QI Core Valueset Analysis 11").toString();
        String[] args = {
            "-" + operation,
            "-ip=" + inputPath,
            "-op=" + outputPath,
            "-resourcepaths=" + resourcePaths,
            "-mn=" + modelName,
            "-mv=" + modelVersion
        };
        Operation profilesToSpreadsheet = new ExecutableOperationAdapter(new ProfilesToSpreadsheet());
        profilesToSpreadsheet.execute(args);
        File spreadsheet = Paths.get(outputPath, modelName + modelVersion + ".xlsx").toFile();
        Assert.assertTrue(spreadsheet.exists());
    }
}
