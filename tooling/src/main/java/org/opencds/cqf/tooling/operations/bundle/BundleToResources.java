package org.opencds.cqf.tooling.operations.bundle;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import jakarta.annotation.Nonnull;

@Operation(name = "BundleToResources")
public class BundleToResources implements ExecutableOperation {
    @OperationParam(alias = { "ptb", "pathtobundle" }, setter = "setPathToBundle", required = true,
            description = "Path to the bundle to decompose (required)")
    private String pathToBundle;
    @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
            description = "The file format to be used for representing the resulting resources { json, xml } (default json)")
    private String encoding;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
            description = "FHIR version { stu3, r4, r5 } (default r4)")
    private String version;
    @OperationParam(alias = { "op", "outputPath" }, setter = "setOutputPath",
            defaultValue = "src/main/resources/org/opencds/cqf/tooling/bundle/output",
            description = "The directory path to which the resource files should be written (default src/main/resources/org/opencds/cqf/tooling/bundle/output)")
    private String outputPath;

    @Override
    public void execute() {
        FhirContext context = FhirContextCache.getContext(version);
        IBaseResource possibleBundle = IOUtils.readResource(pathToBundle, context, true);
        if (possibleBundle == null) {
            throw new IllegalArgumentException("Could not find Bundle at path: " + pathToBundle);
        }
        if (possibleBundle instanceof IBaseBundle) {
            IOUtils.writeResources(bundleToResources(context, (IBaseBundle) possibleBundle),
                    outputPath == null ? pathToBundle : outputPath,
                    IOUtils.Encoding.parse(encoding), context);
        }
        else {
            throw new IllegalArgumentException("Expected a Bundle, found " + possibleBundle.fhirType());
        }
    }

    public static List<IBaseResource> bundleToResources(@Nonnull FhirContext fhirContext, @Nonnull IBaseBundle bundle) {
        return BundleUtil.toListOfResources(fhirContext, bundle);
    }

    public String getPathToBundle() {
        return pathToBundle;
    }

    public void setPathToBundle(String pathToBundle) {
        this.pathToBundle = pathToBundle;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputDirectory) {
        this.outputPath = outputDirectory;
    }
}
