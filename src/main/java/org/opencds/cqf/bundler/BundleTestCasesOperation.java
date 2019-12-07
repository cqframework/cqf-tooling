package org.opencds.cqf.bundler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencds.cqf.Operation;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.ArgUtils;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class BundleTestCasesOperation extends Operation {

    private String path;
    private String fhirVersion;

    public BundleTestCasesOperation() {
    }

    @Override
    public void execute(String[] args) {
        initializeArgs(args);
        FhirContext fhirContext = ResourceUtils.getFhirContext(fhirVersion);
        TestCaseProcessor.refreshTestCases(fhirContext, path);
    }

    private void initializeArgs(String[] args) {
        ArgUtils.ensure("bundleTests", args);

        path = ArgUtils.getValue("path", args, true);
        fhirVersion = ArgUtils.getValue("fhirVersion", args, true);
    }
}

