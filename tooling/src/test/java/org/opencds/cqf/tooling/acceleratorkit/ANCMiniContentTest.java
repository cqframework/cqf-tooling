package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.hl7.fhir.r4.model.CodeSystem;
import org.testng.annotations.Test;

public class ANCMiniContentTest extends BaseContentTest {

    public ANCMiniContentTest() {
        super(new Spreadsheet() {
            {
                path = "acceleratorkit/ANC Test Cases-mini.xlsx";
                scope = "ANCM";
                dataElementPages = "ANC.A. Registration,ANC.B5 Quick check,ANC.End End";
            }
        });
    }

    @Test
    public void validateContentCount() {
        assertFalse(profilesPath().toFile().exists());
        assertEquals(cqlPath().toFile().listFiles().length, 4);
        assertFalse(examplesPath().toFile().exists());
        assertFalse(extensionsPath().toFile().exists());
        assertFalse(resourcesPath().toFile().exists());
        assertFalse(testsPath().toFile().exists());
        assertEquals(vocabularyPath().resolve("codesystem").toFile().listFiles().length, 1);
    }

    @Test
    public void validateCQLContent() throws IOException {
        assertTrue(cqlPath().resolve("ANCMConcepts.cql").toFile().exists());
        assertTrue(cqlPath().resolve("ANCMContactDataElements.cql").toFile().exists());
        assertTrue(cqlPath().resolve("ANCMDataElements.cql").toFile().exists());

        var cqlLines = Files.readAllLines(cqlPath().resolve("ANCMConcepts.cql"));
        assertEquals(cqlLines.get(6), "codesystem \"RxNorm\": 'http://www.nlm.nih.gov/research/umls/rxnorm'");
    }

    @Test
    public void validateCodeSystem() {
        var codeSystem = resourceAtPath(
                CodeSystem.class,
                vocabularyPath().resolve("codesystem/codesystem-activity-codes.json"));
        assertEquals(codeSystem.getTitle(), "ANCM Activity Codes");
    }

    @Test
    public void exampleIssue628() throws IOException {
        // Link the github issue here
        // Description of the issue (e.g. "The CQL was missing a comment at line 235")
        // var cqlLines = Files.readAllLines(cqlPath().resolve("FhirHelpers.cql"));
        // assertEquals(cqlLines.get(20), "// @fluentFunction");
    }

    @Test
    public void validateElm() {
        // TODO: Helpers to compile CQL to ELM and validate
    }
}