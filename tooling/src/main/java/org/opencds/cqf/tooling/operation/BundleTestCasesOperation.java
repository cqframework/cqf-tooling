package org.opencds.cqf.tooling.operation;

import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.parameter.BundleTestCasesParameters;
import org.opencds.cqf.tooling.processor.TestCaseProcessor;
import org.opencds.cqf.tooling.processor.argument.BundleTestCasesArgumentProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

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
        TestCaseProcessor testCaseProcessor = new TestCaseProcessor();
        testCaseProcessor.refreshTestCases(params.path, Encoding.JSON, fhirContext, true);
    }
}

