package org.opencds.cqf.tooling.operations.ig;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.IOUtils;

@SuppressWarnings("checkstyle:AbstractClassName")
@Operation(name = "RefreshGeneratedContent")
public abstract class RefreshGeneratedContent extends org.opencds.cqf.tooling.Operation implements ExecutableOperation {

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            description = "Path to directory where refreshed content will be written (optional)")
    private String outputPathParam;

    @OperationParam(
            alias = {"ptm", "pathtomeasures"},
            setter = "setPathToMeasures",
            description = "Path to the directory containing Measure resources (optional)")
    private String pathToMeasures;

    @OperationParam(
            alias = {"ptl", "pathtolibraries"},
            setter = "setPathToLibraries",
            description = "Path to the directory containing Library resources (optional)")
    private String pathToLibraries;

    @OperationParam(
            alias = {"ss", "stamp"},
            setter = "setShouldApplySoftwareSystemStamp",
            defaultValue = "true",
            description = "Whether to apply the software system stamp (default true)")
    protected Boolean shouldApplySoftwareSystemStamp = true;

    @OperationParam(
            alias = {"ts", "timestamp"},
            setter = "setAddBundleTimestamp",
            defaultValue = "false",
            description = "Whether to add a timestamp to bundles (default false)")
    private Boolean addBundleTimestamp = false;

    protected FhirContext fhirContext;
    private String operationName;

    protected RefreshGeneratedContent() {}

    protected RefreshGeneratedContent(String outputPath, String operationName, FhirContext fhirContext) {
        this(outputPath, operationName, fhirContext, null, null);
    }

    @SuppressWarnings("this-escape")
    protected RefreshGeneratedContent(
            String outputPath,
            String operationName,
            FhirContext fhirContext,
            String pathToLibraries,
            String pathToMeasures) {
        setOutputPath(outputPath);
        this.operationName = operationName;
        this.fhirContext = fhirContext;
        this.pathToLibraries = pathToLibraries;
        this.pathToMeasures = pathToMeasures;
    }

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals(operationName)) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }

            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "pathtomeasures":
                case "ptm":
                    pathToMeasures = value;
                    break;
                case "pathtolibraries":
                case "ptl":
                    pathToLibraries = value;
                    break;
                case "stamp":
                case "ss":
                    shouldApplySoftwareSystemStamp = Boolean.parseBoolean(value);
                    break;
                case "timestamp":
                case "ts":
                    addBundleTimestamp = Boolean.parseBoolean(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        refreshGeneratedContent();
    }

    @Override
    public void execute() {
        refreshGeneratedContent();
    }

    public abstract void refreshGeneratedContent();

    public void output(IBaseResource resource, IOUtils.Encoding encoding) {
        if (Strings.isNullOrEmpty(getOutputPath())) {
            IOUtils.writeResource(resource, pathToMeasures, encoding, fhirContext);
        } else {
            IOUtils.writeResource(resource, getOutputPath(), encoding, fhirContext);
        }
    }

    public String getPathToMeasures() {
        return pathToMeasures;
    }

    public void setPathToMeasures(String pathToMeasures) {
        this.pathToMeasures = pathToMeasures;
    }

    public String getPathToLibraries() {
        return pathToLibraries;
    }

    public void setPathToLibraries(String pathToLibraries) {
        this.pathToLibraries = pathToLibraries;
    }

    public Boolean getShouldApplySoftwareSystemStamp() {
        return shouldApplySoftwareSystemStamp;
    }

    public void setShouldApplySoftwareSystemStamp(String shouldApplySoftwareSystemStamp) {
        this.shouldApplySoftwareSystemStamp = Boolean.parseBoolean(shouldApplySoftwareSystemStamp);
    }

    public Boolean getAddBundleTimestamp() {
        return addBundleTimestamp;
    }

    public void setAddBundleTimestamp(String addBundleTimestamp) {
        this.addBundleTimestamp = Boolean.parseBoolean(addBundleTimestamp);
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    public void setFhirContext(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }
}
