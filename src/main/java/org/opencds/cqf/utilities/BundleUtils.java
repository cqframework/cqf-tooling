package org.opencds.cqf.utilities;

import java.util.List;

import org.hl7.fhir.instance.model.api.IAnyResource;

import ca.uhn.fhir.context.FhirContext;

public class BundleUtils {
    
    public static Object bundleArtifacts(String id, List<IAnyResource> resources, FhirContext fhirContext) {
       
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                return bundleStu3Artifacts(resources);
            case R4:
                return bundleR4Artifacts(resources);
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static org.hl7.fhir.dstu3.model.Bundle bundleStu3Artifacts(List<IAnyResource> resources)
    {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
        for (IAnyResource resource : resources)
        {
            bundle.addEntry(
            new org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent()
                    .setResource((org.hl7.fhir.dstu3.model.Resource) resource)
                    .setRequest(
                            new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent()
                                    .setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT)
                                    .setUrl(((org.hl7.fhir.dstu3.model.Resource) resource).getId())
                    )
            );
        }
        return bundle;
    }

    public static org.hl7.fhir.r4.model.Bundle bundleR4Artifacts(List<IAnyResource> resources)
    {
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
        bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        for (IAnyResource resource : resources)
        {
            bundle.addEntry(
            new org.hl7.fhir.r4.model.Bundle.BundleEntryComponent()
                    .setResource((org.hl7.fhir.r4.model.Resource) resource)
                    .setRequest(
                            new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent()
                                    .setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT)
                                    .setUrl(((org.hl7.fhir.r4.model.Resource) resource).getId())
                    )
            );
        }
        return bundle;
    }
}
