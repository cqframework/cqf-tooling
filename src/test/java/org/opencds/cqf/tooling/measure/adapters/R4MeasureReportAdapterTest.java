package org.opencds.cqf.tooling.measure.adapters;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.Test;
import org.opencds.cqf.tooling.utilities.FhirContextCache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class R4MeasureReportAdapterTest {

    private void Setup() {
        FhirContext fhirContext = FhirContextCache.getContext(FhirVersionEnum.R4);
    }

    // R4 Tests
    private org.hl7.fhir.r4.model.MeasureReport GetR4BasicMeasureReportWithId(String id) {
        org.hl7.fhir.r4.model.MeasureReport report = new org.hl7.fhir.r4.model.MeasureReport();
        report.setId(id);
        report.setMeasure("Measure/" + id);
        report.setSubject(new org.hl7.fhir.r4.model.Reference("Patient/" + id));
        report.setType(org.hl7.fhir.r4.model.MeasureReport.MeasureReportType.INDIVIDUAL);
        List<MeasureReport.MeasureReportGroupComponent> groupList = new ArrayList<>();
        org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent group1 = new org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent();
        group1.setId("group-1");
        group1.setMeasureScore(new Quantity(1.0));
        groupList.add(group1);
        report.setGroup(groupList);

        return report;
    }

    private ParametersParameter getTestParameter(String name, Boolean passed) {
        ParametersParameter parameter = new ParametersParameter();
        parameter.setName(new org.hl7.fhir.String().withValue(name));
        parameter.setValueBoolean(new org.hl7.fhir.Boolean().withValue(passed));
        return parameter;
    }

    @Test
    public void TestGetReportType() {
        String testCaseId = "R4MeasureReportAdapterTest";
        org.hl7.fhir.r4.model.MeasureReport report = GetR4BasicMeasureReportWithId(testCaseId);
        R4MeasureReportAdapter adapter = new R4MeasureReportAdapter(report);

        String reportType = adapter.getReportType();
        Boolean reportTypeMatches = reportType.equals("INDIVIDUAL");
        assertTrue(reportTypeMatches);
    }

    @Test
    public void TestGetMeasureId() {
        String testCaseId = "R4MeasureReportAdapterTest";
        org.hl7.fhir.r4.model.MeasureReport report = GetR4BasicMeasureReportWithId(testCaseId);
        R4MeasureReportAdapter adapter = new R4MeasureReportAdapter(report);

        String measureId = adapter.getMeasureId();
        Boolean measureIdMatches = measureId.equals("R4MeasureReportAdapterTest");
        assertTrue(measureIdMatches);
    }

    @Test
    public void TestGetPatientId() {
        String testCaseId = "R4MeasureReportAdapterTest";
        org.hl7.fhir.r4.model.MeasureReport report = GetR4BasicMeasureReportWithId(testCaseId);
        R4MeasureReportAdapter adapter = new R4MeasureReportAdapter(report);

        String patientId = adapter.getMeasureId();
        Boolean patientIdMatches = patientId.equals("R4MeasureReportAdapterTest");
        assertTrue(patientIdMatches);
    }

    @Test
    public void TestGetGroupScore() {
        String testCaseId = "R4MeasureReportAdapterTest";
        org.hl7.fhir.r4.model.MeasureReport report = GetR4BasicMeasureReportWithId(testCaseId);
        R4MeasureReportAdapter adapter = new R4MeasureReportAdapter(report);
        BigDecimal group1Score = adapter.getGroupScore("group-1");

        assertTrue(group1Score.equals(BigDecimal.valueOf(1.0)));
    }

    @Test
    public void TestGetGroupScoreMultipleGroups() {
        String testCaseId = "R4MeasureReportAdapterTest";
        org.hl7.fhir.r4.model.MeasureReport report = GetR4BasicMeasureReportWithId(testCaseId);

        org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent group2 = new org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent();
        group2.setId("group-2");
        group2.setMeasureScore(new Quantity(2.0));
        report.getGroup().add(group2);

        R4MeasureReportAdapter adapter = new R4MeasureReportAdapter(report);
        BigDecimal group1Score = adapter.getGroupScore("group-1");

        assertTrue(group1Score.equals(BigDecimal.valueOf(1.0)));
    }
}