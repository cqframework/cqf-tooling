package org.opencds.cqf.tooling.operations.cql;

import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.processor.GenerateCQLFromDroolProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

@Operation(name = "GenerateCQLFromDrool")
public class GenerateCQLFromDrool implements ExecutableOperation {

    @OperationParam(
            alias = {"op", "outputPath", "outputpath", "o", "output"},
            setter = "setOutputPath",
            required = true,
            description = "Path to desired CQL generation output")
    private String outputPath;

    @OperationParam(
            alias = {"ip", "inputPath", "input-path", "ifp", "inputFilePath", "input-file-path", "input-filePath"},
            setter = "setInputFilePath",
            required = true,
            description = "Path to the encoded logic export file required for CQL generation")
    private String inputFilePath;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "Input encoding (default json)")
    private String encoding;

    @OperationParam(
            alias = {"fv", "fhirVersion"},
            setter = "setFhirVersion",
            defaultValue = "4.0.0",
            description = "FHIR Model Version to map ELM to (default 4.0.0)")
    private String fhirVersion;

    @OperationParam(
            alias = {"type", "t"},
            setter = "setCqlType",
            defaultValue = "CONDITION",
            description = "ELM granularity option { CONDITION, CONDITIONREL } (default CONDITION)")
    private String cqlType;

    @Override
    public void execute() {
        IOUtils.Encoding encodingEnum = IOUtils.Encoding.parse(encoding != null ? encoding.toLowerCase() : "json");

        String version = fhirVersion != null ? fhirVersion : "4.0.0";

        String typeString = cqlType != null ? cqlType : "CONDITION";
        CQLTYPES cqlTypeEnum;
        switch (typeString.toUpperCase()) {
            case "CONDITION":
                cqlTypeEnum = CQLTYPES.CONDITION;
                break;
            case "CONDITIONREL":
                cqlTypeEnum = CQLTYPES.CONDITIONREL;
                break;
            default:
                throw new IllegalArgumentException("Unknown CQL type: " + typeString);
        }

        GenerateCQLFromDroolParameters params = new GenerateCQLFromDroolParameters();
        params.outputPath = outputPath;
        params.inputFilePath = inputFilePath;
        params.encoding = encodingEnum;
        params.fhirVersion = version;
        params.type = cqlTypeEnum;

        GenerateCQLFromDroolProcessor.generate(params);
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

    public void setCqlType(String cqlType) {
        this.cqlType = cqlType;
    }
}
