package org.opencds.cqf.tooling.measure.comparer;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

public class MeasureReportComparer {

    private FhirContext fhirContext;

    public MeasureReportComparer(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    public IBaseResource compare(IBaseResource left, IBaseResource right) {
        return null;
    }
    
}