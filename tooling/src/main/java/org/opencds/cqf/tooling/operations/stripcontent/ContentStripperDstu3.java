package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_50;
import org.hl7.fhir.dstu3.model.Resource;
import ca.uhn.fhir.context.FhirContext;

class ContentStripperDstu3 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forDstu3Cached();
    }

    // NOTE: These two methods appear to be identical, but they are not. It's the
    // types of the input and output parameters that are different.
    @Override
    protected org.hl7.fhir.r5.model.Resource convertToR5(Resource resource) {
       return VersionConvertorFactory_30_50.convertResource(resource);
    }

    @Override
    protected Resource convertFromR5(org.hl7.fhir.r5.model.Resource resource) {
        return VersionConvertorFactory_30_50.convertResource(resource);
    }
}
