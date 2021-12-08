package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class ProfilesToSpreadsheetTest {
    public static final String separator = System.getProperty("file.separator");
    @Test
    public void testWorked(){
        String inputPath = System.getenv("PWD") + "/src/test/resources/org/opencds/cqf/tooling/operation/profiles/FHIR-Spec";
        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        String operation = "ProfilesToSpreadsheet";
        String modelName="QICore";
        String modelVersion="4.0.0";
        String outputPath = "target" + separator + "test-output" + separator + "profileToSpreadsheet" + separator + "QI Core Valueset Analysis5";//"/Users/bryantaustin/Projects/QI Core Valueset Analysis4";
        String[] args = { "-" + operation, "-ip=" + inputPath, "-op=" + outputPath, "-resourcepaths=" + resourcePaths, "-mn=" + modelName, "-mv=" + modelVersion};
        Operation profilesToSpreadsheet = new ProfilesToSpreadsheet();
        profilesToSpreadsheet.execute(args);
        File spreadsheet = new File(outputPath + ".xlsx");
        Assert.assertTrue(spreadsheet.exists());
    }
}
