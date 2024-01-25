package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;

class ContentStripperR4 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR4Cached();
    }

    // NOTE: These two methods appear to be identical, but they are not. It's the
    // types of the input and output parameters that are different.
    @Override
    protected org.hl7.fhir.r5.model.Resource convertToR5(Resource resource) {
       return VersionConvertorFactory_40_50.convertResource(resource);
    }

    @Override
    protected Resource convertFromR5(org.hl7.fhir.r5.model.Resource resource) {
        return VersionConvertorFactory_40_50.convertResource(resource);
    }
}
