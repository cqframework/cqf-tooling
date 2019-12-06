package org.opencds.cqf.utilities;

import org.hl7.fhir.instance.model.api.IBaseResource;

public class Bundle {
    public static void addArtifact(IBaseResource resource, Bundle bundle)
    {
        

        // bundle.addEntry(
        //         new Bundle.BundleEntryComponent()
        //                 .setResource((Resource) resource)
        //                 .setRequest(
        //                         new Bundle.BundleEntryRequestComponent()
        //                                 .setMethod(Bundle.HTTPVerb.PUT)
        //                                 .setUrl(((Resource) resource).getId())
        //                 )
        // );
    }
}
