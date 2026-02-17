package org.opencds.cqf.tooling.operations.bundle;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.PostBundlesInDirParameters;
import org.opencds.cqf.tooling.processor.PostBundlesInDirProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

@Operation(name = "PostBundlesInDir")
public class PostBundles implements ExecutableOperation {

    @OperationParam(
            alias = {"dp", "dirpath"},
            setter = "setDirectoryPath",
            required = true,
            description = "Path to directory containing bundle resources")
    private String directoryPath;

    @OperationParam(
            alias = {"fv", "fhir-version"},
            setter = "setFhirVersion",
            required = true,
            description = "FHIR version (fhir3 or fhir4)")
    private String fhirVersion;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "If omitted, output will be generated using JSON encoding")
    private String encoding;

    @OperationParam(
            alias = {"fs", "fhir-uri"},
            setter = "setFhirUri",
            required = true,
            description = "URI of the FHIR server")
    private String fhirUri;

    @Override
    public void execute() {
        IOUtils.Encoding outputEncodingEnum = IOUtils.Encoding.JSON;
        if (encoding != null) {
            outputEncodingEnum = IOUtils.Encoding.parse(encoding.toLowerCase());
        }

        PostBundlesInDirParameters params = new PostBundlesInDirParameters();
        params.directoryPath = directoryPath;
        params.fhirVersion = PostBundlesInDirProcessor.FHIRVersion.parse(fhirVersion);
        params.encoding = outputEncodingEnum;
        params.fhirUri = fhirUri;

        PostBundlesInDirProcessor.PostBundlesInDir(params);
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setFhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setFhirUri(String fhirUri) {
        this.fhirUri = fhirUri;
    }
}
