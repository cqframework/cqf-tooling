package org.opencds.cqf.tooling.measure.adapters;

import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public abstract class FhirServerMeasureTestAdapter extends MeasureTestAdapter {

    protected IGenericClient fhirServer;

    public FhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, IBaseResource testBundle) {
        super(fhirContext, testBundle);

        this.fhirServer = Objects.requireNonNull(fhirServer, "fhirServer can not be null");
    }

    public FhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, String testPath) {
        super(fhirContext, testPath);

        this.fhirServer = Objects.requireNonNull(fhirServer, "fhirServer can not be null");
    }

    protected void postBundle(IBaseBundle resource) {
        this.fhirServer.transaction().withBundle(resource).execute();
    }

    @Override
    protected IBaseResource evaluate() {
        return null;
    }
}