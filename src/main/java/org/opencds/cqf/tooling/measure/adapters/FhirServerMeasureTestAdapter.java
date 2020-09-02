package org.opencds.cqf.tooling.measure.adapters;

import java.text.SimpleDateFormat;
import java.util.Objects;

import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;

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
    protected IMeasureReportAdapter evaluate() {
        String measureId = this.getMeasureId();
        String patientId = this.getPatientId();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        String periodStart = simpleFormat.format(this.getPeriodStart());
        String periodEnd = simpleFormat.format(this.getPeriodEnd());

        IMeasureReportAdapter expectedAdapter = this.getExpectedMeasureReportAdapter();
        String path = fhirServer.getServerBase() + "/Measure/" + measureId + "/$evaluate-measure?patient=" + patientId + "&periodStart=" + periodStart + "&periodEnd=" + periodEnd;
        try {
            String response = HttpClientUtils.get(path);
            IMeasureReportAdapter measureReportAdapter = parseEvaluatedResponse(response);
            return measureReportAdapter;
        }
        catch (Exception ex) {
            LogUtils.putException(path, ex);
            //TODO: Error/Message handling
            return null;
        }
        //GET http://localhost:8080/cqf-ruler-dstu3/fhir/Measure/measure-drr/$evaluate-measure?patient=drr-in-2&periodStart=2018-01-01&periodEnd=2018-12-31
    }

    private IMeasureReportAdapter parseEvaluatedResponse(String response) {
        IMeasureReportAdapter measureReportAdapter;
        IBaseResource measureReport;
        IParser parser = fhirContext.newJsonParser();
        measureReport = parser.parseResource(response);

        measureReportAdapter = this.getMeasureReportAdapter(measureReport);
        return measureReportAdapter;
    }
}