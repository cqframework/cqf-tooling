package org.opencds.cqf.tooling.processor;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.parameter.PostBundlesInDirParameters;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class PostBundlesInDirProcessor {
    
    public enum FHIRVersion {
        FHIR3("fhir3"), FHIR4("fhir4");

        private String string;

        public String toString() {
            return this.string;
        }

        private FHIRVersion(String string) {
            this.string = string;
        }

        public static FHIRVersion parse(String value) {
            switch (value) {
            case "fhir3":
                return FHIR3;
            case "fhir4":
                return FHIR4;
            default:
                throw new RuntimeException("Unable to parse FHIR version value:" + value);
            }
        }
    }

    public static FhirContext getFhirContext(FHIRVersion fhirVersion)
        {
            switch (fhirVersion) {
                case FHIR3:
                    return FhirContext.forDstu3Cached();
                case FHIR4:
                    return FhirContext.forR4Cached();
                default:
                    throw new IllegalArgumentException("Unknown IG version: " + fhirVersion);
            }     
        }

    public static void postBundlesInDir(PostBundlesInDirParameters params) {
        String fhirUri = params.fhirUri;
        FHIRVersion fhirVersion = params.fhirVersion;
        Encoding encoding = params.encoding;
        FhirContext fhirContext = getFhirContext(fhirVersion);

        List<Map.Entry<String, IBaseResource>> resources = BundleUtils.getBundlesInDir(params.directoryPath, fhirContext);
        resources.forEach(entry -> postBundleToFhirUri(fhirUri, encoding, fhirContext, entry.getValue()));
    }

	private static void postBundleToFhirUri(String fhirUri, Encoding encoding, FhirContext fhirContext, IBaseResource bundle) {
        if (fhirUri != null && !fhirUri.equals("")) {  
            try {
                HttpClientUtils.post(fhirUri, bundle, encoding, fhirContext);
                System.out.println("Resource successfully posted to FHIR server (" + fhirUri + "): " + bundle.getIdElement().getIdPart());
            } catch (Exception e) {
                System.out.println(bundle.getIdElement().getIdPart() + e);             
            }  
        }
    }
}