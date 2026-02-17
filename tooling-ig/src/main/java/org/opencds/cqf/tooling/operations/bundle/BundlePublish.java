package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.UUID;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.IOUtils;

@Operation(name = "BundlePublish")
public class BundlePublish implements ExecutableOperation {

    @OperationParam(
            alias = {"ptb", "pathtobundle"},
            setter = "setPathToBundle",
            required = true,
            description = "Path to the Bundle resource file to be published (required)")
    private String pathToBundle;

    @OperationParam(
            alias = {"bid", "bundleid"},
            setter = "setBundleId",
            description = "A valid FHIR ID to assign to the Bundle before publishing (optional)")
    private String bundleId;

    @OperationParam(
            alias = {"v", "version"},
            setter = "setVersion",
            description = "FHIR version { stu3, r4, r5 } (default r4)")
    private String version;

    @OperationParam(
            alias = {"fs", "fhirserver"},
            setter = "setFhirServer",
            required = true,
            description = "The base URL of the FHIR server to publish the Bundle to (required)")
    private String fhirServer;

    @OperationParam(
            alias = {"pt", "posttype"},
            setter = "setPostType",
            defaultValue = "transaction",
            description =
                    "The post type: 'transaction' to execute as a transaction bundle (default), or 'resource' to post the Bundle resource itself")
    private String postType;

    private FhirContext context;

    @Override
    public void execute() {
        if (fhirServer == null) {
            throw new IllegalArgumentException("The -fhirserver (-fs) flag is required!");
        }

        if (version == null) {
            context = FhirContext.forR4Cached();
        } else {
            switch (version.toLowerCase()) {
                case "stu3":
                    context = FhirContext.forDstu3Cached();
                    break;
                case "r5":
                    context = FhirContext.forR5Cached();
                    break;
                default:
                    context = FhirContext.forR4Cached();
                    break;
            }
        }

        IBaseResource bundle = IOUtils.readResource(pathToBundle, context);

        if (bundle instanceof IBaseBundle) {
            postBundle((IBaseBundle) bundle);
        }
    }

    public void postBundle(IBaseBundle bundle) {
        IGenericClient client = context.newRestfulGenericClient(fhirServer);
        if (bundleId != null) {
            bundle.setId(bundleId);
        } else if (!bundle.getIdElement().hasIdPart()) {
            bundle.setId(UUID.randomUUID().toString());
        }
        if (postType == null || postType.equals("transaction")) {
            client.transaction().withBundle(bundle).execute();
        } else {
            client.create().resource(bundle).execute();
        }
    }

    public void setPathToBundle(String pathToBundle) {
        this.pathToBundle = pathToBundle;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setFhirServer(String fhirServer) {
        this.fhirServer = fhirServer;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}
