package org.opencds.cqf.tooling.measure.adapters;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.util.BundleUtil;

public abstract class MeasureTestAdapter {

    protected String testPath;
    protected FhirContext fhirContext;
    protected IBaseResource testBundle;
    protected IMeasureReportAdapter expectedReportAdapter;
    protected IMeasureReportAdapter actualReportAdapter;

    public MeasureTestAdapter(FhirContext fhirContext, String testPath) {
        this(fhirContext, IOUtils.readResource(Objects.requireNonNull(testPath), fhirContext));
    }

    public MeasureTestAdapter(FhirContext fhirContext, IBaseResource testBundle) {
        this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext can not be null.");
        this.testBundle = Objects.requireNonNull(testBundle, "testBundle can not be null.");
        var expectedReport = loadExpectedReportFromBundle();
        this.expectedReportAdapter = getMeasureReportAdapter(fhirContext, expectedReport);
    }

    protected static IMeasureReportAdapter getMeasureReportAdapter(FhirContext fhirContext, IBaseResource measureReport) {
        //TODO: R5?
        IMeasureReportAdapter measureReportAdapter;
        if (fhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
            measureReportAdapter = new Dstu3MeasureReportAdapter((org.hl7.fhir.dstu3.model.MeasureReport)measureReport);
        } else if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
            measureReportAdapter = new R4MeasureReportAdapter((org.hl7.fhir.r4.model.MeasureReport)measureReport);
        } else {
            throw new IllegalArgumentException("Unsupported or unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        return measureReportAdapter;
    }

    private IBaseResource loadExpectedReportFromBundle() {
        if (this.testBundle == null) {
            throw new IllegalArgumentException("testBundle can not be null");
        }

        if (!this.testBundle.fhirType().equals("Bundle") || !(this.testBundle instanceof IBaseBundle)) {
            throw new IllegalArgumentException("testBundle is not a Bundle Resource");
        }

        IBaseBundle bundle = (IBaseBundle)this.testBundle;

        List<? extends IBaseResource> measureReports = BundleUtil.toListOfResourcesOfType(this.fhirContext, bundle,
            this.fhirContext.getResourceDefinition("MeasureReport").getImplementingClass());

        if (measureReports == null || measureReports.size() == 0 || measureReports.size() > 1) {
            throw new IllegalArgumentException("Bundle is not a valid Measure Test Bundle. It must contain exactly 1 MeasureReport");
        }

        return measureReports.get(0);
    }

    protected abstract IMeasureReportAdapter evaluate();

    public abstract IMeasureReportAdapter getActualMeasureReportAdapter();

    public IMeasureReportAdapter getExpectedMeasureReportAdapter() {
        return this.expectedReportAdapter;
    }

    protected Date getPeriodStart() {
        Date periodStart = this.expectedReportAdapter.getPeriodStart();
        return periodStart;
    }

    protected Date getPeriodEnd() {
        Date periodEnd = this.expectedReportAdapter.getPeriodEnd();
        return periodEnd;
    }

    protected String getMeasureId() {
        String measureId = this.expectedReportAdapter.getMeasureId();
        return measureId;
    }

    protected String getReportType() {
        String reportType = this.expectedReportAdapter.getReportType();
        return reportType;
    }

    protected String getPatientId () {
        String patientId = this.expectedReportAdapter.getPatientId();
        return patientId;
    }

//    protected String getPractitioner() {
//        return null;
//    }

//    protected String getLastReceivedOn() {
//        return null;
//    }
}