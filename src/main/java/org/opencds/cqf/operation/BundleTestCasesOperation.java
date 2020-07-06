package org.opencds.cqf.operation;

import org.opencds.cqf.Operation;
import org.opencds.cqf.parameter.BundleTestCasesParameters;
import org.opencds.cqf.processor.TestCaseProcessor;
import org.opencds.cqf.processor.argument.BundleTestCasesArgumentProcessor;
import org.opencds.cqf.utilities.IOUtils.Encoding;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class BundleTestCasesOperation extends Operation {

    public BundleTestCasesOperation() {
    }

    @Override
    public void execute(String[] args) {

        BundleTestCasesParameters params = null;
        try {
            params = new BundleTestCasesArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
 
        FhirContext fhirContext = ResourceUtils.getFhirContext(ResourceUtils.FhirVersion.parse(params.igVersion.toString()));
        TestCaseProcessor.refreshTestCases(params.path, Encoding.JSON, fhirContext);
    }
}

