package org.opencds.cqf.tooling.measure;

import java.io.File;
import java.util.Objects;

import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.opencds.cqf.tooling.measure.adapters.*;
import org.opencds.cqf.tooling.measure.comparer.MeasureReportComparer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureTestProcessor {

    private FhirContext fhirContext;

    public MeasureTestProcessor(FhirContext fhirContext)
    {
        this.fhirContext = fhirContext;
    }

    public Parameters executeMeasureTest(String testPath, String contentPath, String fhirServer)
    {
        MeasureTestAdapter adapter = getMeasureTestAdapter(testPath, contentPath, fhirServer);
        MeasureReportComparer comparer = new MeasureReportComparer(this.fhirContext);

        IMeasureReportAdapter expected = adapter.getExpectedMeasureReportAdapter();
        String measureId = expected.getMeasureId();
        System.out.println("Testing Measure '" + measureId + "'");

        IMeasureReportAdapter actual = adapter.getActualMeasureReportAdapter();

        Parameters results = comparer.compare(actual, expected);
        logTestResults(measureId, results);
        return results;
    }

    private void logTestResults(String measureId, Parameters results) {
        //TODO: Can do whatever we want here, just printing to out for now - just hacked together console output.
        System.out.println("Test results for Measure '" + measureId + "':");
        for (ParametersParameter parameter : results.getParameter()) {
            String assertionString = "";

            if (parameter.getName().getValue().indexOf("Test Passed") >= 0) {
                assertionString = ": ";
            }
            else {
                assertionString = " matched expected value: ";
            }
            System.out.println(parameter.getName().getValue() + assertionString + parameter.getValueBoolean().isValue().toString());
        }
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