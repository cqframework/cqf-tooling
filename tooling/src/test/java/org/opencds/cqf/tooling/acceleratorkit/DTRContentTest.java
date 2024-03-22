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
        assertTrue(cqlPath().resolve("ASLPConcepts.cql").toFile().exists());
        assertTrue(cqlPath().resolve("ASLPDataElementsByActivity.md").toFile().exists());

        var cqlLines = Files.readAllLines(cqlPath().resolve("ASLPConcepts.cql"));
        assertEquals(cqlLines.get(0), "library ASLPConcepts");
    }

    @Test
    public void testContactDataElements() throws IOException {
        assertTrue(cqlPath().resolve("ASLPContactDataElements.cql").toFile().exists());
        var contactDataElementLines = Files.readAllLines(cqlPath().resolve("ASLPContactDataElements.cql"));
        assertEquals(contactDataElementLines.get(0), "library ASLPContactDataElements");
        assertEquals(contactDataElementLines.get(2), "using QICore version '4.1.1'");
        assertEquals(contactDataElementLines.get(5), "include QICoreCommon called QC");
        assertEquals(contactDataElementLines.get(10), "context Encounter");
        assertEquals(contactDataElementLines.get(12), "/*");
        assertEquals(contactDataElementLines.get(13), "  @dataElement: ASLP.A1.DE22 BMI");
        assertEquals(contactDataElementLines.get(14), "  @activity: ASLP.A1 Adult Sleep Studies");
        assertEquals(contactDataElementLines.get(15), "  @description: Body mass index (BMI)");
        assertEquals(contactDataElementLines.get(16), "*/");
        assertEquals(contactDataElementLines.get(17), "define \"BMI\":");
        assertEquals(contactDataElementLines.get(18), "  WC.MostRecent(");
        assertEquals(contactDataElementLines.get(19), "  [Observation: Cx.\"BMI\"] O");
        assertEquals(contactDataElementLines.get(20), "    where O.status in { 'final', 'amended', 'corrected' }");
        assertEquals(contactDataElementLines.get(21), "      and Last(Split(O.encounter.reference, '/')) = Encounter.id");
        assertEquals(contactDataElementLines.get(22), "  ).value as FHIR.Quantity");

        assertEquals(contactDataElementLines.get(117), "/*");
        assertEquals(contactDataElementLines.get(118), "  @dataElement: ASLP.A1.DE21 Weight");
        assertEquals(contactDataElementLines.get(119), "  @activity: ASLP.A1 Adult Sleep Studies");
        assertEquals(contactDataElementLines.get(120), "  @description: Weight (in pounds)");
        assertEquals(contactDataElementLines.get(121), "*/");
        assertEquals(contactDataElementLines.get(122), "define \"Weight\":");
        assertEquals(contactDataElementLines.get(123), "  WC.MostRecent(");
        assertEquals(contactDataElementLines.get(124), "  [Observation: Cx.\"Weight\"] O");
        assertEquals(contactDataElementLines.get(125), "    where O.status in { 'final', 'amended', 'corrected' }");
        assertEquals(contactDataElementLines.get(126), "      and Last(Split(O.encounter.reference, '/')) = Encounter.id");
        assertEquals(contactDataElementLines.get(127), "  ).value as FHIR.Quantity");
    }

    @Test
    public void testDataElement() throws IOException {
        assertTrue(cqlPath().resolve("ASLPDataElements.cql").toFile().exists());
        var dataElementLines = Files.readAllLines(cqlPath().resolve("ASLPDataElements.cql"));
        assertEquals(dataElementLines.get(0), "library ASLPDataElements");
        assertEquals(dataElementLines.get(2), "using QICore version '4.1.1'");
        assertEquals(dataElementLines.get(5), "include QICoreCommon called QC");
        assertEquals(dataElementLines.get(7), "include SDHCommon called SC");
        assertEquals(dataElementLines.get(8), "include ASLPConcepts called Cs");
        assertEquals(dataElementLines.get(12), "/*");
        assertEquals(dataElementLines.get(13), "  @dataElement: ASLP.A1.DE22 BMI");
        assertEquals(dataElementLines.get(14), "  @activity: ASLP.A1 Adult Sleep Studies");
        assertEquals(dataElementLines.get(15), "  @description: Body mass index (BMI)");
        assertEquals(dataElementLines.get(16), "*/");
        assertEquals(dataElementLines.get(17), "define \"BMI\":");
        assertEquals(dataElementLines.get(18), "  [Observation: Cx.\"BMI\"] O");
        assertEquals(dataElementLines.get(19), "    where O.status in { 'final', 'amended', 'corrected' }");
        assertEquals(dataElementLines.get(23), "  @dataElement: ASLP.A1.DE16 Diagnosis of Obstructive Sleep Apnea");

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


}