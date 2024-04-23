package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.r5.model.Resource;

import ca.uhn.fhir.context.FhirContext;

class ContentStripperR5 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR5Cached();
    }
}