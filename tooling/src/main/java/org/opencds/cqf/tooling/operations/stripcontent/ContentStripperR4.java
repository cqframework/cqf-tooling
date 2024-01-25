package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.r4.model.Resource;
import ca.uhn.fhir.context.FhirContext;

class ContentStripperR4 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR4Cached();
    }
}