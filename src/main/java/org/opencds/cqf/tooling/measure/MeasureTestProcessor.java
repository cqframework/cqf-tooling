package org.opencds.cqf.tooling.measure;

import java.io.File;
import java.util.Objects;

import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.measure.adapters.ContentLoadingFhirServerMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.CqlEvaluatorMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.IMeasureReportAdapter;
import org.opencds.cqf.tooling.measure.adapters.MeasureTestAdapter;
import org.opencds.cqf.tooling.measure.adapters.ReadOnlyFhirServerMeasureTestAdapter;
import org.opencds.cqf.tooling.measure.comparer.MeasureReportComparer;
import org.opencds.cqf.tooling.processor.ITestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class MeasureTestProcessor implements ITestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MeasureTestProcessor.class);

    //TODO: Should probably introduce a BaseTestProcessor and this would belong there.
    public static final String TestPassedKey  = "Test Passed";

    private FhirContext fhirContext;

    public MeasureTestProcessor(FhirContext fhirContext)
    {
        this.fhirContext = fhirContext;
    }

    public Parameters executeTest(String testPath, String contentBundlePath, String fhirServer)
    {
        MeasureTestAdapter adapter = getMeasureTestAdapter(testPath, contentBundlePath, fhirServer);
        MeasureReportComparer comparer = new MeasureReportComparer(this.fhirContext);

        IMeasureReportAdapter expected = adapter.getExpectedMeasureReportAdapter();
        String measureId = expected.getMeasureId();
        logger.info("Testing Measure '{}'", measureId);

        IMeasureReportAdapter actual = adapter.getActualMeasureReportAdapter();

        Parameters results = comparer.compare(actual, expected);
        logTestResults(measureId, results);
        return results;
    }

    public Parameters executeTest(IBaseResource testBundle, IBaseResource contentBundle, String fhirServer)
    {
        MeasureTestAdapter adapter = getMeasureTestAdapter(testBundle, contentBundle, fhirServer);
        MeasureReportComparer comparer = new MeasureReportComparer(this.fhirContext);

        IMeasureReportAdapter expected = adapter.getExpectedMeasureReportAdapter();
        String measureId = expected.getMeasureId();
        logger.info("Testing Measure '{}'", measureId);

        IMeasureReportAdapter actual = adapter.getActualMeasureReportAdapter();

        Parameters results = comparer.compare(actual, expected);
        logTestResults(measureId, results);
        return results;
    }

    private void logTestResults(String artifactId, Parameters results) {
        logger.info("Test results for Measure '{}':", artifactId);

        for (ParametersParameter parameter : results.getParameter()) {
            String assertionString = "";

            if (parameter.getName().getValue().indexOf(TestPassedKey) >= 0) {
                assertionString = ": ";
            }
            else {
                assertionString = " matched expected value: ";
            }
            logger.info(parameter.getName().getValue() + assertionString + parameter.getValueBoolean().isValue().toString());
        }
    }

    public MeasureTestAdapter getMeasureTestAdapter(IBaseResource testBundle, IBaseResource contentBundle, String fhirServer) {
        Objects.requireNonNull(testBundle, "            testBundle can not be null");

        if ((fhirServer == null || fhirServer.trim().isEmpty()) && (contentBundle == null)) {
            throw new IllegalArgumentException("If fhirServer is not specified, contentBundle can not be null or empty.");
        }

        if (fhirServer == null) {
            return new CqlEvaluatorMeasureTestAdapter(this.fhirContext, testBundle, contentBundle);
        }

        IGenericClient fhirClient = this.fhirContext.newRestfulGenericClient(fhirServer);

        if (contentBundle == null) {
            return new ReadOnlyFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testBundle);
        }
        else {
            return new ContentLoadingFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testBundle, contentBundle);
        }
    }

    public MeasureTestAdapter getMeasureTestAdapter(String testPath, String contentBundlePath, String fhirServer) {
        Objects.requireNonNull(testPath, "          testPath can not be null");

        File testFile = new File(testPath);
        if(!testFile.exists())
        {
            throw new IllegalArgumentException(String.format("          testPath file not found: %s", testPath));
        }

        if ((fhirServer == null || fhirServer.trim().isEmpty()) && (contentBundlePath == null || contentBundlePath.trim().isEmpty())) {
            throw new IllegalArgumentException("If fhirServer is not specified, contentBundlePath can not be null.");
        }

        if (fhirServer == null) {
            return new CqlEvaluatorMeasureTestAdapter(this.fhirContext, testPath, contentBundlePath);
        }

        IGenericClient fhirClient = this.fhirContext.newRestfulGenericClient(fhirServer);

        if (contentBundlePath == null) {
            return new ReadOnlyFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testPath);
        }
        else {
            return new ContentLoadingFhirServerMeasureTestAdapter(this.fhirContext, fhirClient, testPath, contentBundlePath);
        }
    }
}