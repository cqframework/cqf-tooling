package org.opencds.cqf.tooling.operations.measure;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;

@Operation(name = "ExecuteMeasureTest")
public class ExecuteMeasureTest implements ExecutableOperation {

    @OperationParam(
            alias = {"test-path", "tp"},
            setter = "setTestPath",
            required = true,
            description = "Path to the test data bundle")
    private String testPath;

    @OperationParam(
            alias = {"content-path", "cp"},
            setter = "setContentPath",
            description = "Path to the measure content bundle. Required if running locally")
    private String contentPath;

    @OperationParam(
            alias = {"fhir-version", "fv"},
            setter = "setFhirVersion",
            defaultValue = "R4",
            description = "FHIR version (default R4)")
    private String fhirVersion;

    @OperationParam(
            alias = {"fhir-server", "fs"},
            setter = "setFhirServer",
            description = "URL of the FHIR server to use for evaluation")
    private String fhirServer;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "Desired output encoding for resources (default JSON)")
    private String encoding;

    @Override
    public void execute() {
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.R4;
        if (fhirVersion != null && !fhirVersion.isEmpty()) {
            fhirVersionEnum = FhirVersionEnum.forVersionString(fhirVersion);
            if (fhirVersionEnum == null) {
                fhirVersionEnum = FhirVersionEnum.R4;
            }
        }

        FhirContext fhirContext = FhirContextCache.getContext(fhirVersionEnum);

        MeasureTestProcessor processor = new MeasureTestProcessor(fhirContext);
        processor.executeTest(testPath, contentPath, fhirServer);
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }

    public void setFhirServer(String fhirServer) {
        this.fhirServer = fhirServer;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
