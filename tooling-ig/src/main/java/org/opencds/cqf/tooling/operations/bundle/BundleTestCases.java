package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.processor.TestCaseProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

@Operation(name = "BundleTestCases")
public class BundleTestCases implements ExecutableOperation {

    @OperationParam(
            alias = {"p", "path"},
            setter = "setPath",
            required = true,
            description = "Root directory of the test cases")
    private String path;

    @OperationParam(
            alias = {"iv", "ig-version"},
            setter = "setIgVersion",
            required = true,
            description = "IG FHIR version")
    private String igVersion;

    @Override
    public void execute() {
        FhirContext fhirContext = ResourceUtils.getFhirContext(ResourceUtils.FhirVersion.parse(igVersion));
        TestCaseProcessor testCaseProcessor = new TestCaseProcessor();
        testCaseProcessor.refreshTestCases(path, Encoding.JSON, fhirContext, true);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setIgVersion(String igVersion) {
        this.igVersion = igVersion;
    }
}
