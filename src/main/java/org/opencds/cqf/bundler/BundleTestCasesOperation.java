package org.opencds.cqf.bundler;

import org.opencds.cqf.Operation;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.ResourceUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

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

