package org.opencds.cqf.tooling.measure;

import java.io.File;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.measure.adapters.ContentLoadingFhirServerMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.CqlEvaluatorMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.MeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.ReadOnlyFhirServerMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.comparer.MeasureReportComparer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureTestProcessor {

    private FhirContext fhirContext;

    public MeasureTestProcessor(FhirContext fhirContext)
    {
        this.fhirContext = fhirContext;
    }

    public IBaseResource executeMeasureTest(String testPath, String contentPath, String fhirServer)
    {
        MeasureTestAdapter adapter = getMeasureTestAdapter(testPath, contentPath, fhirServer);
        MeasureReportComparer comparer = new MeasureReportComparer(this.fhirContext);

        IBaseResource actual = adapter.getActual();
        IBaseResource expected = adapter.getExpected();

        return comparer.compare(actual, expected);
    }

    public MeasureTestAdapter getMeasureTestAdapter(String testPath, String contentPath, String fhirServer) {
        Objects.requireNonNull(testPath, "testPath can not be null");

        File testFile = new File(testPath);
        if(!testFile.exists())
        {
            throw new IllegalArgumentException(String.format("testPath file not found: %s", testPath));
        }
        
        if ((fhirServer == null || fhirServer.trim().isEmpty()) && (contentPath == null || contentPath.trim().isEmpty())) {
            throw new IllegalArgumentException("If fhirServer is not specified, contentPath can not be null.");
        }

        if (fhirServer == null) {
            return new CqlEvaluatorMeasureTestAdapter(this.fhirContext, testPath, contentPath);
        }
        
        IGenericClient fhirClient = this.fhirContext.newRestfulGenericClient(fhirServer);
        
        if (contentPath == null) {
            return new ReadOnlyFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testPath);
        }
        else {
            return new ContentLoadingFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testPath, contentPath);
        }

    }
    
}