package org.opencds.cqf.tooling.measure.adapters;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class ReadOnlyFhirServerMeasureTestAdapter extends FhirServerMeasureTestAdapter {

    public ReadOnlyFhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, IBaseResource testBundle) {
        super(fhirContext, fhirServer, testBundle);
    }

    public ReadOnlyFhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, String testPath) {
        super(fhirContext, fhirServer, testPath);
    }

    @Override
    public IMeasureReportAdapter getActualMeasureReportAdapter() {
        return this.evaluate();
    }
}