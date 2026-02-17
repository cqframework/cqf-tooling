package org.opencds.cqf.tooling.operations.spreadsheet;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;

@SuppressWarnings("checkstyle:AbstractClassName")
public abstract class SpreadsheetBase implements ExecutableOperation {

    @OperationParam(
            alias = {"ip", "inputpath"},
            setter = "setInputPath",
            required = true,
            description = "Path to spec files directory (required)")
    protected String inputPath;

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            required = true,
            description =
                    "Directory to save output to; the file name is the modelName + modelVersion + .xlsx (required)")
    protected String outputPath;

    @OperationParam(
            alias = {"rp", "resourcepaths"},
            setter = "setResourcePaths",
            required = true,
            description =
                    "Path to the individual specs and versions to use (e.g. '4.0.1;US-Core/3.1.0;QI-Core/4.1.0') (required)")
    protected String resourcePaths;

    @OperationParam(
            alias = {"mn", "modelname"},
            setter = "setModelName",
            required = true,
            description = "Name of the model to parse (required)")
    protected String modelName;

    @OperationParam(
            alias = {"mv", "modelversion"},
            setter = "setModelVersion",
            required = true,
            description = "Version of the model to parse (required)")
    protected String modelVersion;

    @OperationParam(
            alias = {"sp", "snapshotonly"},
            setter = "setSnapshotOnly",
            defaultValue = "true",
            description =
                    "Flag to determine if the differential should be traversed; false means traverse the differential (default true)")
    protected boolean snapshotOnly = true;

    protected CanonicalResourceAtlas canonicalResourceAtlas;
    protected CanonicalResourceAtlas canonicalResourceDependenciesAtlas;

    protected CreationHelper helper;
    protected XSSFCellStyle linkStyle;

    protected boolean isParameterListComplete() {
        if (null == inputPath
                || inputPath.length() < 1
                || null == modelName
                || modelName.length() < 1
                || null == modelVersion
                || modelName.length() < 1
                || null == resourcePaths
                || resourcePaths.length() < 1) {
            System.out.println("These parameters are required: ");
            System.out.println("-modelName/-mn");
            System.out.println("-modelVersion/-mv");
            System.out.println("-outputpath/-op");
            System.out.println("-resourcePaths/-rp");
            return false;
        }
        return true;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setResourcePaths(String resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public void setSnapshotOnly(String snapshotOnly) {
        this.snapshotOnly = snapshotOnly.equalsIgnoreCase("true");
    }
}
