package org.opencds.cqf.tooling.operations.stripcontent;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_50;
import org.hl7.fhir.dstu3.model.Resource;
import ca.uhn.fhir.context.FhirContext;

class StripContentDstu3 extends BaseStripContent<Resource> {
    @Override
    protected FhirContext context() {
        return FhirContext.forDstu3Cached();
    }

    @Override
    protected org.hl7.fhir.r5.model.Resource convertToR5(Resource resource) {
       return VersionConvertorFactory_30_50.convertResource(resource);
    }

    @Override
    protected Resource convertFromR5(org.hl7.fhir.r5.model.Resource resource) {
        return VersionConvertorFactory_30_50.convertResource(resource);
    }
}
