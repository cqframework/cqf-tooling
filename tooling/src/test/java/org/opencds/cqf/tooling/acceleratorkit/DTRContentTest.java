package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.CodeSystem;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

public class DTRContentTest extends BaseContentTest {

    public DTRContentTest() {
        super(new Spreadsheet()
            .path("acceleratorkit/DTR.xlsx")
            .scope("ASLP")
            .dataElementPages("ASLP.A1 Adult Sleep Studies"));;
    }

    @Test
    @Ignore
    public void validateContentCount() {
        assertEquals(cqlPath().toFile().listFiles().length, 4);
        assertEquals(examplesPath().toFile().listFiles().length, 8);
        assertFalse(extensionsPath().toFile().exists());
        assertEquals(profilesPath().toFile().listFiles().length, 8);
        assertEquals(resourcesPath().toFile().listFiles().length, 1);
        assertFalse(testsPath().toFile().exists());
        assertEquals(vocabularyPath().resolve("conceptmap").toFile().listFiles().length, 2);
        assertEquals(vocabularyPath().resolve("valueset").toFile().listFiles().length, 4);
    }

    @Test
    public void validateCQLContent() throws IOException {

        assertTrue(cqlPath().resolve("ASLPContactDataElements.cql").toFile().exists());
        assertTrue(cqlPath().resolve("ASLPDataElements.cql").toFile().exists());

        var cqlLines = Files.readAllLines(cqlPath().resolve("ASLPConcepts.cql"));
        assertEquals(cqlLines.get(0), "library ASLPConcepts");
    }

    @Test
    public void validateConceptsCqlContentIssueKALM62() throws IOException {
        assertTrue(cqlPath().resolve("ASLPConcepts.cql").toFile().exists());
        var cqlLines = Files.readAllLines(cqlPath().resolve("ASLPConcepts.cql"));
        assertEquals(cqlLines.get(0), "library ASLPConcepts");
        assertEquals(cqlLines.get(2), "// Code Systems");
        assertEquals(cqlLines.get(15), "codesystem \"ASLP Codes\": 'http://example.org/sdh/dtr/aslp/CodeSystem/aslp-codes'");
        assertEquals(cqlLines.get(16), "");
        assertEquals(cqlLines.get(17), "// Value Sets");
        assertEquals(cqlLines.get(23), "// Codes");
        assertEquals(cqlLines.get(26), "code \"Neck Circumference\": 'ASLP.A1.DE20' from \"ASLP Codes\" display 'Neck Circumference'");
        assertEquals(cqlLines.get(27), "code \"Height\": 'ASLP.A1.DE20' from \"ASLP Codes\" display 'Height'");
    }

    @Test
    @Ignore
    public void validateCodeSystem() {
        var codeSystemActivity = resourceAtPath(
                CodeSystem.class,
                vocabularyPath().resolve("codesystem/codesystem-activity-codes.json"));
        assertEquals(codeSystemActivity.getTitle(), "ASLP Activity Codes");
        var codeSystemConcept = resourceAtPath(
                CodeSystem.class,
                vocabularyPath().resolve("codesystem/codesystem-concept-codes.json"));
        assertEquals(codeSystemConcept.getTitle(), "ASLP Concept Codes");
    }
}