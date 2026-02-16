package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.dstu3.model.Resource;
import ca.uhn.fhir.context.FhirContext;

class ContentStripperDstu3 extends BaseContentStripper<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forDstu3Cached();
    }
}