package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.util.ClasspathUtil;
import org.opencds.cqf.tooling.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

public class ProfilesToSpreadsheetTest {
    public static final String separator = System.getProperty("file.separator");
    // TODO: Fails on Windows...
    //@Test
    public void verifySpreadsheetFromProfile() throws URISyntaxException {
        String inputPath = Objects.requireNonNull(
                ProfilesToSpreadsheetTest.class.getClassLoader().getResource(
                        "org/opencds/cqf/tooling/operation/profiles/FHIR-Spec")).toURI().getRawPath();
        String resourcePaths = "QI-Core/4.1.1";
        String operation = "ProfilesToSpreadsheet";
        String modelName="QICore";
        String modelVersion="4.1.1";
        String outputPath = "target" + separator + "test-output" + separator + "profileToSpreadsheet" + separator + "QI Core Valueset Analysis 11";
        String[] args = { "-" + operation, "-ip=" + inputPath, "-op=" + outputPath, "-resourcepaths=" + resourcePaths, "-mn=" + modelName, "-mv=" + modelVersion};
        Operation profilesToSpreadsheet = new ProfilesToSpreadsheet();
        profilesToSpreadsheet.execute(args);
        File spreadsheet = new File(outputPath + separator + modelName + modelVersion + ".xlsx");
        Assert.assertTrue(spreadsheet.exists());
    }
}
