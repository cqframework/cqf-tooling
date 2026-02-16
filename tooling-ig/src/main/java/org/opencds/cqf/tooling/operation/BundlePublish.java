package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.UUID;

public class BundlePublish extends Operation {
    private String pathToBundle; // -pathtobundle (-ptb)
    private String bundleId; // -bundleid (-bid)
    private String version; // -version (-v) Can be stu3, r4, or r5
    private String fhirServer; // -fhirserver (-fs)
    private String postType; // -posttype (-pt) This can be transaction (default) or "resource" to post the Bundle resource itself
    private FhirContext context;

    // TODO: Authentication

    @Override
    public void execute(String[] args) {

        for (String arg : args) {
            if (arg.equals("-PublishBundle")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtobundle":
                case "ptb":
                    pathToBundle = value;
                    break;
                case "bundleid":
                case "bid":
                    bundleId = value;
                    break;
                case "version": case "v":
                    version = value;
                    break;
                case "fhirserver":
                case "fs":
                    fhirServer = value;
                    break;
                case "posttype":
                case "pt":
                    postType = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }

            if (fhirServer == null) {
                throw new IllegalArgumentException("The -fhirserver (-fs) flag is required!");
            }

            if (version == null) {
                context = FhirContext.forR4Cached();
            }
            else {
                switch (version.toLowerCase()) {
                    case "stu3":
                        context = FhirContext.forDstu3Cached();
                        break;
                    case "r5":
                        context = FhirContext.forR5Cached();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown fhir version: " + version);
                }
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
}
