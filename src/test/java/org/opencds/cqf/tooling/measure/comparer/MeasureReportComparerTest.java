package org.opencds.cqf.tooling.measure.comparer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.dstu3.model.MeasureReport;
import org.hl7.fhir.r4.model.Quantity;
import org.testng.annotations.Test;
import org.opencds.cqf.tooling.measure.adapters.Dstu3MeasureReportAdapter;
import org.opencds.cqf.tooling.measure.adapters.IMeasureReportAdapter;
import org.opencds.cqf.tooling.measure.adapters.R4MeasureReportAdapter;
import org.opencds.cqf.tooling.utilities.FhirContextCache;

import java.util.List;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class MeasureReportComparerTest {

    private MeasureReportComparer comparer;
    private IMeasureReportAdapter actualMeasureReportAdapter;
    private IMeasureReportAdapter expectedMeasureReportAdapter;

    private void Setup() {
        FhirContext fhirContext = FhirContextCache.getContext(FhirVersionEnum.DSTU3);
        this.comparer = new MeasureReportComparer(fhirContext);
    }

    // Dstu3 Tests
    private org.hl7.fhir.dstu3.model.MeasureReport GetDstu3BasicMeasureReportWithId(String id) {
        org.hl7.fhir.dstu3.model.MeasureReport report = new org.hl7.fhir.dstu3.model.MeasureReport();
        report.setId(id);
        org.hl7.fhir.dstu3.model.Reference measureReference = new org.hl7.fhir.dstu3.model.Reference("Measure/" + id);
        report.setMeasure(measureReference);
        report.setPatient(new org.hl7.fhir.dstu3.model.Reference("Patient/" + id));
        report.setType(org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportType.INDIVIDUAL);
        List<org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent> groupList = new ArrayList<>();
        org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent group1 = new org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent();
        group1.setId("group-1");
        group1.setMeasureScore(1.0);
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
    public void TestDstu3BasicCompare() {
        Setup();

        String testCaseId = "DSTU3ComparerTestMeasureReport";
        org.hl7.fhir.dstu3.model.MeasureReport actualReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.actualMeasureReportAdapter = new Dstu3MeasureReportAdapter(actualReport);

        org.hl7.fhir.dstu3.model.MeasureReport expectedReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new Dstu3MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", true));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters);
    }

    @Test
    public void TestDstu3BasicCompareMismatchedMeasureId() {
        Setup();

        String testCaseId = "DSTU3ComparerTestMeasureReport";
        org.hl7.fhir.dstu3.model.MeasureReport actualReport = GetDstu3BasicMeasureReportWithId(testCaseId);

        org.hl7.fhir.dstu3.model.Reference measureReference = new org.hl7.fhir.dstu3.model.Reference("Measure/thisshouldresultinamismatch");
        actualReport.setMeasure(measureReference);

        this.actualMeasureReportAdapter = new Dstu3MeasureReportAdapter(actualReport);

        org.hl7.fhir.dstu3.model.MeasureReport expectedReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new Dstu3MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", false));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched measureId test");
    }

    @Test
    public void TestDstu3BasicCompareMismatchedPatientId() {
        Setup();

        String testCaseId = "DSTU3ComparerTestMeasureReport";
        org.hl7.fhir.dstu3.model.MeasureReport actualReport = GetDstu3BasicMeasureReportWithId(testCaseId);

        org.hl7.fhir.dstu3.model.Reference patientReference = new org.hl7.fhir.dstu3.model.Reference("Patient/thisshouldresultinamismatch");
        actualReport.setPatient(patientReference);

        this.actualMeasureReportAdapter = new Dstu3MeasureReportAdapter(actualReport);

        org.hl7.fhir.dstu3.model.MeasureReport expectedReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new Dstu3MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", false));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched patientId test");
    }

    @Test
    public void TestDstu3BasicCompareMismatchedReportType() {
        Setup();

        String testCaseId = "DSTU3ComparerTestMeasureReport";
        org.hl7.fhir.dstu3.model.MeasureReport actualReport = GetDstu3BasicMeasureReportWithId(testCaseId);

        actualReport.setType(MeasureReport.MeasureReportType.SUMMARY);

        this.actualMeasureReportAdapter = new Dstu3MeasureReportAdapter(actualReport);

        org.hl7.fhir.dstu3.model.MeasureReport expectedReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new Dstu3MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", false));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched reportType test");
    }

    @Test
    public void TestDstu3BasicCompareMismatchedScore() {
        Setup();

        String testCaseId = "DSTU3ComparerTestMeasureReport";
        org.hl7.fhir.dstu3.model.MeasureReport actualReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        org.hl7.fhir.dstu3.model.MeasureReport.MeasureReportGroupComponent group1 = actualReport.getGroupFirstRep();
        group1.setMeasureScore(0.0); // Intentional Score mismatch
        this.actualMeasureReportAdapter = new Dstu3MeasureReportAdapter(actualReport);

        org.hl7.fhir.dstu3.model.MeasureReport expectedReport = GetDstu3BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new Dstu3MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", false));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched measureScore test");
    }

    // R4 Test Cases
    private org.hl7.fhir.r4.model.MeasureReport GetR4BasicMeasureReportWithId(String id) {
        org.hl7.fhir.r4.model.MeasureReport report = new org.hl7.fhir.r4.model.MeasureReport();
        report.setId(id);
        report.setMeasure("Measure/" + id);
        report.setSubject(new org.hl7.fhir.r4.model.Reference("Patient/" + id));
        report.setType(org.hl7.fhir.r4.model.MeasureReport.MeasureReportType.INDIVIDUAL);
        List<org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent> groupList = new ArrayList<>();
        org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent group1 = new org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent();
        group1.setId("group-1");
        group1.setMeasureScore(new Quantity(1.0));
        groupList.add(group1);
        report.setGroup(groupList);

        return report;
    }

    @Test
    public void TestR4BasicCompare() {
        Setup();

        String testCaseId = "R4ComparerTestMeasureReport";
        org.hl7.fhir.r4.model.MeasureReport actualReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.actualMeasureReportAdapter = new R4MeasureReportAdapter(actualReport);

        org.hl7.fhir.r4.model.MeasureReport expectedReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new R4MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", true));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters);
    }

    @Test
    public void TestR4BasicCompareMismatchedMeasureId() {
        Setup();

        String testCaseId = "R4ComparerTestMeasureReport";
        org.hl7.fhir.r4.model.MeasureReport actualReport = GetR4BasicMeasureReportWithId(testCaseId);

        actualReport.setMeasure("Measure/thisshouldresultinamismatch");

        this.actualMeasureReportAdapter = new R4MeasureReportAdapter(actualReport);

        org.hl7.fhir.r4.model.MeasureReport expectedReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new R4MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", false));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched measureId test");
    }

    @Test
    public void TestR4BasicCompareMismatchedPatientId() {
        Setup();

        String testCaseId = "R4ComparerTestMeasureReport";
        org.hl7.fhir.r4.model.MeasureReport actualReport = GetR4BasicMeasureReportWithId(testCaseId);

        org.hl7.fhir.r4.model.Reference patientReference = new org.hl7.fhir.r4.model.Reference("Patient/thisshouldresultinamismatch");
        actualReport.setSubject(patientReference);

        this.actualMeasureReportAdapter = new R4MeasureReportAdapter(actualReport);

        org.hl7.fhir.r4.model.MeasureReport expectedReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new R4MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", false));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched patientId test");
    }

    @Test
    public void TestR4BasicCompareMismatchedReportType() {
        Setup();

        String testCaseId = "R4ComparerTestMeasureReport";
        org.hl7.fhir.r4.model.MeasureReport actualReport = GetR4BasicMeasureReportWithId(testCaseId);

        actualReport.setType(org.hl7.fhir.r4.model.MeasureReport.MeasureReportType.SUMMARY);

        this.actualMeasureReportAdapter = new R4MeasureReportAdapter(actualReport);

        org.hl7.fhir.r4.model.MeasureReport expectedReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new R4MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", false));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", true));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched reportType test");
    }

    @Test
    public void TestR4BasicCompareMismatchedScore() {
        Setup();

        String testCaseId = "R4ComparerTestMeasureReport";
        org.hl7.fhir.r4.model.MeasureReport actualReport = GetR4BasicMeasureReportWithId(testCaseId);
        org.hl7.fhir.r4.model.MeasureReport.MeasureReportGroupComponent group1 = actualReport.getGroupFirstRep();
        group1.setMeasureScore(new Quantity(0.0)); // Intentional Score mismatch
        this.actualMeasureReportAdapter = new R4MeasureReportAdapter(actualReport);

        org.hl7.fhir.r4.model.MeasureReport expectedReport = GetR4BasicMeasureReportWithId(testCaseId);
        this.expectedMeasureReportAdapter = new R4MeasureReportAdapter(expectedReport);

        Parameters results = comparer.compare(this.actualMeasureReportAdapter, this.expectedMeasureReportAdapter);

        List<ParametersParameter> resultParameters = results.getParameter();

        Boolean containsMeasureIdResult = resultParameters.contains(getTestParameter("MeasureId", true));
        Boolean containsPatientIdResult = resultParameters.contains(getTestParameter("PatientId", true));
        Boolean containsReportTypeResult = resultParameters.contains(getTestParameter("ReportType", true));
        Boolean groupScoreMatches = resultParameters.contains(getTestParameter("Group[group-1].score", false));
        Boolean containsTestPassesResult = resultParameters.contains(getTestParameter("Measure '" + testCaseId + "' Test Passed", false));

        Boolean resultContainsAllExpectedParameters = containsMeasureIdResult & containsPatientIdResult & containsReportTypeResult & containsTestPassesResult & groupScoreMatches;

        assertTrue(resultContainsAllExpectedParameters, "Mismatched measureScore test");
    }
}