package org.opencds.cqf.utilities;

import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;

public class BundleUtils {

    //TODO: consider providing utilities to mask stu3 vs r4 from user
    public static Bundle bundleStu3Artifacts(List<IAnyResource> list, String id) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        for (IAnyResource anyResource : list) {
            Resource resource = (Resource) anyResource;
            bundle.addEntry(
            new Bundle.BundleEntryComponent()
                    .setResource((Resource) resource)
                    .setRequest(
                            new Bundle.BundleEntryRequestComponent()
                                    .setMethod(Bundle.HTTPVerb.PUT)
                                    .setUrl(((Resource) resource).getId())
                    )
            );
        }
        bundle.setId(id);
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
