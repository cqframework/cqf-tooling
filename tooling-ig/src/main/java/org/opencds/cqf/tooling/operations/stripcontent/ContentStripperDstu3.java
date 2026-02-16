package org.opencds.cqf.tooling.operations.stripcontent;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Resource;

class ContentStripperDstu3 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forDstu3Cached();
    }
}
