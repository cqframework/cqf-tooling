package org.opencds.cqf.tooling.measure.adapters;

import org.apache.commons.lang.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

public class CqlEvaluatorMeasureTestAdapter extends MeasureTestAdapter {

    private String contentPath;

    /// This guy will eventually run the the cql-evaluator to get the results...

    public CqlEvaluatorMeasureTestAdapter(FhirContext fhirContext, String testPath, String contentPath) {
        super(fhirContext, testPath);

        this.contentPath = contentPath;
    }

    @Override
    public IBaseResource getActual() {
        // 1. Get Measure and Patient Ids from Expected
        // 2. Run evaluator with Measure, Patient, Content context
        // 3. Parse the result
        throw new NotImplementedException();
    }

    @Override
    protected IBaseResource evaluate() {
        throw new NotImplementedException();
    }
}