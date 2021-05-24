package org.opencds.cqf.tooling.measure.adapters;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;

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
        Objects.requireNonNull(contentBundle, "contentBundle can not be null");
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
        if (this.contentBundle == null) {
            throw new IllegalArgumentException("contentBundle can not be null if a contentPath was specified");
        }

        if (!this.contentBundle.fhirType().equals("Bundle") || !(this.contentBundle instanceof IBaseBundle)) {
            throw new IllegalArgumentException("contentBundle is not a Bundle Resource");
        }

        IBaseBundle bundle = (IBaseBundle)this.contentBundle;

        List<? extends IBaseResource> measures = BundleUtil.toListOfResourcesOfType(this.fhirContext, bundle,
                this.fhirContext.getResourceDefinition("Measure").getImplementingClass());

        //TODO: Ideally we should be ensuring that the measure being tested is included in the bundle (by measure ID).
        if (measures == null || measures.size() == 0) {
            throw new IllegalArgumentException("Content bundle does not contain a Measure.");
        }
    }

    @Override
    public IMeasureReportAdapter getActualMeasureReportAdapter() {
        this.ensureContentAndData();
        return this.evaluate();
    }

    private void ensureContentAndData() {
        this.postBundle((IBaseBundle)this.contentBundle);
        this.postBundle((IBaseBundle)this.testBundle);
    }
}