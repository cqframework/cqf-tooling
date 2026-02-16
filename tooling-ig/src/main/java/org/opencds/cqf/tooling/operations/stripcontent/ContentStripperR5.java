package org.opencds.cqf.tooling.operations.stripcontent;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r5.model.Resource;

class ContentStripperR5 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR5Cached();
    }
}
