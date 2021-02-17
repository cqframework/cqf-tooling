package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.parameter.MeasureTestParameters;
import org.opencds.cqf.tooling.processor.argument.ExecuteMeasureTestArgumentProcessor;
import org.opencds.cqf.tooling.utilities.FhirContextCache;

import ca.uhn.fhir.context.FhirContext;

public class ExecuteMeasureTestOperation extends Operation {

    @Override
    public void execute(String[] args) {
        MeasureTestParameters params = null;
        try {
            params = new ExecuteMeasureTestArgumentProcessor().parseAndConvert(args);

            FhirContext fhirContext = FhirContextCache.getContext(params.fhirVersion);

            MeasureTestProcessor processor = new MeasureTestProcessor(fhirContext);

            processor.executeTest(params.testPath, params.contentPath, params.fhirServer);

            //TODO: Need a proper ToString() for Parameters. The following results in: "org.hl7.fhir.Parameters cannot be cast to org.hl7.fhir.instance.model.api.IBaseResource"
//            String resource = IOUtils.encodeResourceAsString((IBaseResource)result, params.encoding, fhirContext);
//            System.out.println(resource);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
//
//    public static void main(String[] args) {
//        ExecuteMeasureTestOperation op = new ExecuteMeasureTestOperation();
//
//        op.execute(new String[] {
//        "-ExecuteMeasureTest",
//        "-test-path",
//        "/Users/Adam/Src/DBCG/connectathon/fhir401/input/tests/EXM104-8.2.000/tests-numer-EXM104-bundle.json",
//        "-content-path",
//        "/Users/Adam/Src/DBCG/connectathon/fhir401/bundles/EXM104-8.2.000/EXM104-8.2.000-bundle.json",
//        "-fhir-server",
//        "http://localhost:8080/cqf-ruler-r4/fhir"});
//    }
    
}