package org.opencds.cqf.utilities;

public class BundleUtils {

    public static void addDstu3Artifact(org.hl7.fhir.dstu3.model.Resource resource, org.hl7.fhir.dstu3.model.Bundle bundle)
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

    public static void addR4Artifact(org.hl7.fhir.r4.model.Resource resource, org.hl7.fhir.r4.model.Bundle bundle)
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
}
