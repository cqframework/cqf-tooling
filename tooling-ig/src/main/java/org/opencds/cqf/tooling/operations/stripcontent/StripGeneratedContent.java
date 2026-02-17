package org.opencds.cqf.tooling.operations.stripcontent;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;

@Operation(name = "StripGeneratedContent")
public class StripGeneratedContent implements ExecutableOperation {

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            description = "Path to directory where stripped content will be written (optional)")
    private String outputPath;

    @OperationParam(
            alias = {"ptr", "pathtores"},
            setter = "setPathToResources",
            required = true,
            description = "Path to the directory containing the resources to strip (required)")
    private String pathToResources;

    @OperationParam(
            alias = {"v", "version"},
            setter = "setVersion",
            description = "FHIR version { dstu3, r4, r5 } (optional)")
    private String version;

    @OperationParam(
            alias = {"cql"},
            setter = "setCqlExportDirectory",
            description = "Path to directory where CQL content should be exported (optional)")
    private String cqlExportDirectory;

    @Override
    public void execute() {
        var params = new StripContentParams();

        if (outputPath != null) {
            params.outputDirectory(outputPath);
        }
        if (pathToResources != null) {
            params.inputDirectory(pathToResources);
        }
        if (version != null) {
            params.fhirVersion(version);
        }
        if (cqlExportDirectory != null) {
            params.cqlExportDirectory(cqlExportDirectory);
        }

        new StripContentExecutor(params).execute();
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setPathToResources(String pathToResources) {
        this.pathToResources = pathToResources;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCqlExportDirectory(String cqlExportDirectory) {
        this.cqlExportDirectory = cqlExportDirectory;
    }
}
