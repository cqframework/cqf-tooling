package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class ProfilesToSpreadsheetTest {
    public static final String separator = System.getProperty("file.separator");
    @Test
    public void verifySpreadsheetFromProfile(){
        String inputPath = System.getenv("PWD") + "/src/test/resources/org/opencds/cqf/tooling/operation/profiles/FHIR-Spec";
        String resourcePaths = "QI-Core/4.1.1";
        String operation = "ProfilesToSpreadsheet";
        String modelName="QICore";
        String modelVersion="4.1.1";
        String outputPath = "target" + separator + "test-output" + separator + "profileToSpreadsheet" + separator + "QI Core Valueset Analysis 11";//"/Users/bryantaustin/Projects/QI Core Valueset Analysis4";
        String[] args = { "-" + operation, "-ip=" + inputPath, "-op=" + outputPath, "-resourcepaths=" + resourcePaths, "-mn=" + modelName, "-mv=" + modelVersion};
        Operation profilesToSpreadsheet = new ProfilesToSpreadsheet();
        profilesToSpreadsheet.execute(args);
        File spreadsheet = new File(outputPath + separator + modelName + modelVersion + ".xlsx");
        Assert.assertTrue(spreadsheet.exists());
    }
}
