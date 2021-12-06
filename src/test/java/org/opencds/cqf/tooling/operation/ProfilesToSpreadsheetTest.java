package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.testng.annotations.Test;

public class ProfilesToSpreadsheetTest {

    @Test
    public void testWorked(){
        String inputPath = "/Users/bryantaustin/Projects/FHIR-Spec";
        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        String operation = "ProfilesToSpreadsheet";
        String outputPath = "/Users/bryantaustin/Projects/QI Core Valueset Analysis3";
        String[] args = { "-" + operation, "-ip=" + inputPath, "-op=" + outputPath, "-resourcepaths=" + resourcePaths };
        Operation profilesToSpreadsheet = new ProfilesToSpreadsheet();
        profilesToSpreadsheet.execute(args);
    }
}
