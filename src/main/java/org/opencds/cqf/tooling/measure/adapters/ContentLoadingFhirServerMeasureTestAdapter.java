package org.opencds.cqf.tooling.measure.adapters;

import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class ContentLoadingFhirServerMeasureTestAdapter extends FhirServerMeasureTestAdapter {

    private IBaseResource contentBundle;

    public ContentLoadingFhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, IBaseResource testBundle, String contentPath) {
        super (fhirContext, fhirServer, testBundle);
        
        Objects.requireNonNull(contentPath, "contentPath can not be null");

        this.contentBundle = IOUtils.readResource(contentPath, fhirContext);

        if (contentBundle == null) {
            throw new IllegalArgumentException(String.format("FHIR Resource does not exist at %s", contentPath));
        }

        validateContentBundle();
    }


    public ContentLoadingFhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, IBaseResource testBundle, IBaseResource contentBundle) {
        super (fhirContext, fhirServer, testBundle);
        this.contentBundle = contentBundle;

        validateContentBundle();
    }

    public ContentLoadingFhirServerMeasureTestAdapter(FhirContext fhirContext, IGenericClient fhirServer, String testPath, String contentPath) {
        super (fhirContext, fhirServer, testPath);
        Objects.requireNonNull(contentPath, "contentPath can not be null");

        this.contentBundle = IOUtils.readResource(contentPath, fhirContext);

        if (contentBundle == null) {
            throw new IllegalArgumentException(String.format("FHIR Resource does not exist at %s", contentPath));
        }

        validateContentBundle();
    }

    private void validateContentBundle() {
        // TODO: Make sure there's content and whatnot
    }

    @Override
    public IBaseResource getActual() {
        this.ensureContentAndData();
        return this.evaluate();
    }

    private void ensureContentAndData() {
        this.postBundle((IBaseBundle)this.testBundle);
        this.postBundle((IBaseBundle)this.contentBundle); 
    }
}