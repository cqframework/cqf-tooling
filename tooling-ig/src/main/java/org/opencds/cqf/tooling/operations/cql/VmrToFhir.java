package org.opencds.cqf.tooling.operations.cql;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.VmrToFhirParameters;
import org.opencds.cqf.tooling.processor.VmrToFhirProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

@Operation(name = "VmrToFhir")
public class VmrToFhir implements ExecutableOperation {

    @OperationParam(
            alias = {"op", "outputPath", "outputpath", "o", "output"},
            setter = "setOutputPath",
            required = true,
            description = "Path to FHIR data output")
    private String outputPath;

    @OperationParam(
            alias = {"ip", "inputPath", "input-path", "ifp", "inputFilePath", "input-file-path", "input-filePath"},
            setter = "setInputFilePath",
            required = true,
            description = "Path to the VMR data file")
    private String inputFilePath;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "xml",
            description = "Input encoding (default xml)")
    private String encoding;

    @OperationParam(
            alias = {"fv", "fhirVersion"},
            setter = "setFhirVersion",
            defaultValue = "4.0.0",
            description = "FHIR Model Version (default 4.0.0)")
    private String fhirVersion;

    @Override
    public void execute() {
        IOUtils.Encoding encodingEnum = IOUtils.Encoding.parse(encoding != null ? encoding.toLowerCase() : "xml");

        String version = fhirVersion != null ? fhirVersion : "4.0.0";

        VmrToFhirParameters params = new VmrToFhirParameters();
        params.vmrDataPath = inputFilePath;
        params.fhirOutputPath = outputPath;
        params.encoding = encodingEnum;
        params.fhirVersion = version;

        VmrToFhirProcessor.transform(params);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }
}
