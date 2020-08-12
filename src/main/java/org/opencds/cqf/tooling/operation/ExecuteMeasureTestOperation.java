package org.opencds.cqf.tooling.operation;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.parameter.MeasureTestParameters;
import org.opencds.cqf.tooling.processor.argument.ExecuteMeasureTestArgumentProcessor;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class ExecuteMeasureTestOperation extends Operation {

    @Override
    public void execute(String[] args) {
        MeasureTestParameters params = null;
        try {
            params = new ExecuteMeasureTestArgumentProcessor().parseAndConvert(args);

            FhirContext fhirContext = FhirContextCache.getContext(params.fhirVersion);

            MeasureTestProcessor processor = new MeasureTestProcessor(fhirContext);

            IBaseResource result = processor.executeMeasureTest(params.testPath, params.contentPath, params.fhirServer);

            String resource = IOUtils.encodeResourceAsString(result, params.encoding, fhirContext);

            System.out.println(resource);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        ExecuteMeasureTestOperation op = new ExecuteMeasureTestOperation();

        op.execute(new String[] {
        "-ExecuteMeasureTest", 
        "-test-path", 
        "/home/jp/repos/connectathon/fhir401/input/tests/EXM104-9.1.000/tests-numer-EXM104-bundle.json",
        "-content-path",
        "/home/jp/repos/connectathon/fhir401/input/bundles/EXM104-9.1.000-bundle.json",
        "-fhir-server",
        "https://cqm-sandbox.alphora.com/cqf-ruler-r4/fhir"});
    }
    
}