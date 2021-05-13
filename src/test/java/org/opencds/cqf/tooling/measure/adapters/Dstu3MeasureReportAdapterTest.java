package org.opencds.cqf.tooling.measure.adapters;

import org.hl7.fhir.dstu3.model.MeasureReport;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class Dstu3MeasureReportAdapterTest {

    // Dstu3 Tests
    private org.hl7.fhir.dstu3.model.MeasureReport GetDstu3BasicMeasureReportWithId(String id) {
        org.hl7.fhir.dstu3.model.MeasureReport report = new org.hl7.fhir.dstu3.model.MeasureReport();
        report.setId(id);
        org.hl7.fhir.dstu3.model.Reference measureReference = new org.hl7.fhir.dstu3.model.Reference("Measure/" + id);
        report.setMeasure(measureReference);
        report.setPatient(new org.hl7.fhir.dstu3.model.Reference("Patient/" + id));
        report.setType(org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportType.INDIVIDUAL);
        List<MeasureReport.MeasureReportGroupComponent> groupList = new ArrayList<>();
        org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent group1 = new org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent();
        group1.setId("group-1");
        group1.setMeasureScore(1.0);
        groupList.add(group1);
        report.setGroup(groupList);

        return report;
    }

    @Test
    public void TestGetReportType() {
        String testCaseId = "DSTU3MeasureReportAdapterTest";
        org.hl7.fhir.dstu3.model.MeasureReport report = GetDstu3BasicMeasureReportWithId(testCaseId);
        Dstu3MeasureReportAdapter adapter = new Dstu3MeasureReportAdapter(report);

        String reportType = adapter.getReportType();
        Boolean reportTypeMatches = reportType.equals("INDIVIDUAL");
        assertTrue(reportTypeMatches);
    }

    @Test
    public void TestGetMeasureId() {
        String testCaseId = "DSTU3MeasureReportAdapterTest";
        org.hl7.fhir.dstu3.model.MeasureReport report = GetDstu3BasicMeasureReportWithId(testCaseId);
        Dstu3MeasureReportAdapter adapter = new Dstu3MeasureReportAdapter(report);

        String measureId = adapter.getMeasureId();
        Boolean measureIdMatches = measureId.equals("DSTU3MeasureReportAdapterTest");
        assertTrue(measureIdMatches);
    }

    @Test
    public void TestGetPatientId() {
        String testCaseId = "DSTU3MeasureReportAdapterTest";
        org.hl7.fhir.dstu3.model.MeasureReport report = GetDstu3BasicMeasureReportWithId(testCaseId);
        Dstu3MeasureReportAdapter adapter = new Dstu3MeasureReportAdapter(report);

        String patientId = adapter.getMeasureId();
        Boolean patientIdMatches = patientId.equals("DSTU3MeasureReportAdapterTest");
        assertTrue(patientIdMatches);
    }

    @Test
    public void TestGetGroupScore() {
        String testCaseId = "DSTU3MeasureReportAdapterTest";
        org.hl7.fhir.dstu3.model.MeasureReport report = GetDstu3BasicMeasureReportWithId(testCaseId);
        Dstu3MeasureReportAdapter adapter = new Dstu3MeasureReportAdapter(report);
        BigDecimal group1Score = adapter.getGroupScore("group-1");

        assertTrue(group1Score.equals(BigDecimal.valueOf(1.0)));
    }

    @Test
    public void TestGetGroupScoreMultipleGroups() {
        String testCaseId = "DSTU3MeasureReportAdapterTest";
        org.hl7.fhir.dstu3.model.MeasureReport report = GetDstu3BasicMeasureReportWithId(testCaseId);

        org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent group2 = new org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent();
        group2.setId("group-2");
        group2.setMeasureScore(2.0);
        report.getGroup().add(group2);

        Dstu3MeasureReportAdapter adapter = new Dstu3MeasureReportAdapter(report);
        BigDecimal group1Score = adapter.getGroupScore("group-1");

        assertTrue(group1Score.equals(BigDecimal.valueOf(1.0)));
    }
}