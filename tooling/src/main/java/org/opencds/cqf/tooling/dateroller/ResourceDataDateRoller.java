package org.opencds.cqf.tooling.dateroller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.dateroller.dstu3.RollDatesDstu3;
import org.opencds.cqf.tooling.dateroller.r4.RollDatesR4;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResourceDataDateRoller {
    private static Logger logger = LoggerFactory.getLogger(ResourceDataDateRoller.class);

    private ResourceDataDateRoller() {

    }

    public static void rollBundleDates(FhirContext fhirContext, IBaseResource iBaseResource) {
        switch (fhirContext.getVersion().getVersion()) {
            case R4:
                List<Resource> r4ResourceArrayList = BundleUtils.getR4ResourcesFromBundle((org.hl7.fhir.r4.model.Bundle) iBaseResource);
                r4ResourceArrayList.forEach(RollDatesR4::rollDatesInResource);
                break;
            case DSTU3:
                List<org.hl7.fhir.dstu3.model.Resource> stu3resourceArrayList = BundleUtils.getStu3ResourcesFromBundle((org.hl7.fhir.dstu3.model.Bundle) iBaseResource);
                stu3resourceArrayList.forEach(RollDatesDstu3::rollDatesInResource);
                break;
            default: throw new UnsupportedOperationException("Date Roller not supported for version "
                    + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void rollResourceDates(FhirContext fhirContext, IBaseResource resource) {
        switch (fhirContext.getVersion().getVersion()) {
            case R4:
                RollDatesR4.rollDatesInResource(resource);
                break;
            case DSTU3:
                RollDatesDstu3.rollDatesInResource(resource);
                break;
            default: throw new UnsupportedOperationException("Date Roller not supported for version "
                    + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }
}
