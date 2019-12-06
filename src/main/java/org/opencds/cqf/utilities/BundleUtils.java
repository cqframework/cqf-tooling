package org.opencds.cqf.utilities;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;

public class BundleUtils {

    // public static void addArtifact(IAnyResource resource, Bundle bundle)
    // {
    //     bundle.addEntry(
    //         new Bundle.BundleEntryComponent()
    //                 .setResource((Resource) resource)
    //                 .setRequest(
    //                         new Bundle.BundleEntryRequestComponent()
    //                                 .setMethod(Bundle.HTTPVerb.PUT)
    //                                 .setUrl(((Resource) resource).getId())
    //                 )
    //     );
    // }
}
