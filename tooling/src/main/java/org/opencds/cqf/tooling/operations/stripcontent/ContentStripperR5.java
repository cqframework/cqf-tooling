package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.r5.model.Resource;

import ca.uhn.fhir.context.FhirContext;

class ContentStripperR5 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR5Cached();
    }

    // NOTE: These two methods appear to be identical, but they are not. It's the
    // types of the input and output parameters that are different.
    @Override
    protected Resource convertToR5(Resource resource) {
        return resource;
    }

    @Override
    protected Resource convertFromR5(Resource resource) {
        return resource;
    }
}
