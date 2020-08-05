package org.opencds.cqf.tooling.measure.adapters;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;

public abstract class MeasureTestAdapter {

    protected String testPath;
    protected FhirContext fhirContext;
    protected IBaseResource testBundle;

    private IBaseResource expectedReport;

    public MeasureTestAdapter(FhirContext fhirContext, String testPath) {
        this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext can not be null.");
        Objects.requireNonNull(testPath, "testPath can not be null.");

        this.testBundle = IOUtils.readResource(testPath, fhirContext);

        if (testBundle == null) {
            throw new IllegalArgumentException(String.format("FHIR Resource does not exist at %s", testPath));
        }

        validateTestBundle();
    }

    public MeasureTestAdapter(FhirContext fhirContext, IBaseResource testBundle) {
        this.fhirContext = Objects.requireNonNull(fhirContext, "fhirContext can not be null.");
        this.testBundle = Objects.requireNonNull(testBundle, "testBundle can not be null.");

        validateTestBundle();
    }

    private void validateTestBundle() {
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

        this.expectedReport = measureReports.get(0);
    }

    protected abstract IBaseResource evaluate();

    public abstract IBaseResource getActual();

    public IBaseResource getExpected() {
        return this.expectedReport;
    }

    protected String getPeriodStart() {
        return null;
    }

    protected String getPeroidEnd() {
        return null;
    }

    protected String getMeasureId() {
        return null;
    }

    protected String getReportType() {
        return null;
    }

    protected String getPatientId () {
        return null;
    }

    protected String getPractitioner() {
        return null;
    }

    protected String getlastReceivedOn() {
        return null;
    }
}