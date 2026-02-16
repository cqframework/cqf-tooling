package org.opencds.cqf.tooling.measure.adapters;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ReadOnlyFhirServerMeasureTestAdapter extends FhirServerMeasureTestAdapter {

    public ReadOnlyFhirServerMeasureTestAdapter(
            FhirContext fhirContext, IGenericClient fhirServer, IBaseResource testBundle) {
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
