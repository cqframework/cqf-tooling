package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;

import javax.annotation.Nonnull;
import java.util.List;

@Operation(name = "BundleToResources")
public class BundleToResources implements ExecutableOperation {
    @OperationParam(alias = { "ptb", "pathtobundle" }, setter = "setPathToBundle", required = true)
    private String pathToBundle;
    @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json")
    private String encoding;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4")
    private String version;
    @OperationParam(alias = { "od", "outputDir" }, setter = "setOutputDirectory")
    private String outputDirectory;

    @Override
    public void execute() {
        FhirContext context = FhirContextCache.getContext(version);
        IBaseResource possibleBundle = IOUtils.readResource(pathToBundle, context, true);
        if (possibleBundle == null) {
            throw new IllegalArgumentException("Could not find Bundle at path: " + pathToBundle);
        }
        if (possibleBundle instanceof IBaseBundle) {
            IOUtils.writeResources(bundleToResources(context, (IBaseBundle) possibleBundle),
                    outputDirectory == null ? pathToBundle : outputDirectory,
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

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
