package org.opencds.cqf.tooling.operations.stripcontent;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Resource;

class ContentStripperR4 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forR4Cached();
    }
}
