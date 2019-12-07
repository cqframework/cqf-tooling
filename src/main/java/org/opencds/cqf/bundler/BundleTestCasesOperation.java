package org.opencds.cqf.bundler;

import org.opencds.cqf.Operation;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.ArgUtils;
import org.opencds.cqf.utilities.ResourceUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class BundleTestCasesOperation extends Operation {

    private String path;
    private String fhirVersion;

    public BundleTestCasesOperation() {
    }

    @Override
    public void execute(String[] args) {
        initializeArgs(args);
        FhirContext fhirContext = ResourceUtils.getFhirContext(ResourceUtils.FhirVersion.parse(fhirVersion));
        TestCaseProcessor.refreshTestCases(path, Encoding.JSON, fhirContext);
    }

    private void initializeArgs(String[] args) {
        ArgUtils.ensure("bundleTests", args);

        path = ArgUtils.getValue("path", args, true);
        fhirVersion = ArgUtils.getValue("fhirVersion", args, true);
    }
}

