package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.r5.model.Resource;

import ca.uhn.fhir.context.FhirContext;

class StripContentR5 extends BaseStripContent<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR5Cached();
    }
    @Override
    protected Resource convertToR5(Resource resource) {
        return resource;
    }

    @Override
    protected Resource convertFromR5(Resource resource) {
        return resource;
    }
}
