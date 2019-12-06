package org.opencds.cqf.utilities;

import java.util.List;

public class BundleUtils {

    public static org.hl7.fhir.dstu3.model.Bundle bundleStu3Artifacts(List<org.hl7.fhir.dstu3.model.Resource> resources)
    {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
        for (org.hl7.fhir.dstu3.model.Resource resource : resources)
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

    public static org.hl7.fhir.r4.model.Bundle bundleR4Artifacts(List<org.hl7.fhir.r4.model.Resource> resources)
    {
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
        bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
        for (org.hl7.fhir.r4.model.Resource resource : resources)
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
