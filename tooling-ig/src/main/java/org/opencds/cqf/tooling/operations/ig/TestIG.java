package org.opencds.cqf.tooling.operations.ig;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.TestIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.IGTestProcessor;

@Operation(name = "TestIG")
public class TestIG implements ExecutableOperation {

    @OperationParam(
            alias = {"ini"},
            setter = "setIni",
            description = "IG ini file")
    private String ini;

    @OperationParam(
            alias = {"root-dir"},
            setter = "setRootDir",
            description = "Root directory of the IG")
    private String rootDir;

    @OperationParam(
            alias = {"ip", "ig-path"},
            setter = "setIgPath",
            description = "Path to the IG, relative to the root directory")
    private String igPath;

    @OperationParam(
            alias = {"fv", "fhir-version"},
            setter = "setFhirVersion",
            description = "Limited to a single version of FHIR")
    private String fhirVersion;

    @OperationParam(
            alias = {"tests", "testsPath", "testCasesPath", "tp", "tcp"},
            setter = "setTestCasesPath",
            required = true,
            description = "Path to the directory containing test cases")
    private String testCasesPath;

    @OperationParam(
            alias = {"fs", "fhir-uri"},
            setter = "setFhirServerUri",
            required = true,
            description = "URI of the FHIR server to test on")
    private String fhirServerUri;

    @Override
    public void execute() {
        TestIGParameters params = new TestIGParameters();
        params.ini = ini;
        params.rootDir = rootDir;
        params.igPath = igPath;
        params.testCasesPath = testCasesPath;
        params.fhirServerUri = fhirServerUri;
        params.fhirContext = IGProcessor.getIgFhirContext(fhirVersion);

        try {
            new IGTestProcessor().testIg(params);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error testing IG: " + e.getMessage(), e);
        }
    }

    public void setIni(String ini) {
        this.ini = ini;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void setIgPath(String igPath) {
        this.igPath = igPath;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }

    public void setTestCasesPath(String testCasesPath) {
        this.testCasesPath = testCasesPath;
    }

    public void setFhirServerUri(String fhirServerUri) {
        this.fhirServerUri = fhirServerUri;
    }
}
