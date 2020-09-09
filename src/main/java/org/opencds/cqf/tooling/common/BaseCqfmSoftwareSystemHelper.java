package org.opencds.cqf.tooling.common;

import org.hl7.fhir.r4.model.DomainResource;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCqfmSoftwareSystemHelper {
    private static String cqfToolingDeviceName = "cqf-tooling";
    private static String cqfToolingDeviceReferenceID = "#" + cqfToolingDeviceName;
    private static String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";

    protected String getCqfToolingDeviceName() { return cqfToolingDeviceName; }
    protected String getCqfToolingDeviceReferenceID() { return cqfToolingDeviceReferenceID; }
    protected String getCqfmSoftwareSystemExtensionUrl() { return cqfmSoftwareSystemExtensionUrl; }

    protected <T extends DomainResource> void validateResourceForSoftwareSystemExtension(T resource) {
        if (resource == null) {
            throw new IllegalArgumentException("No resource provided.");
        }

        List<String> eligibleResourceTypes = new ArrayList<String>() { {
            add("Library");
            add("Measure");
        } };

        String eligibleResourceTypesList = String.join(", ", eligibleResourceTypes);
        String fhirType = resource.fhirType();
        if (!eligibleResourceTypes.contains(fhirType)) {
            throw new IllegalArgumentException(String.format("cqfm-softwaresystem extension is only supported for the following resources: { %s }, not %s", eligibleResourceTypesList, fhirType));
        }
    }

    protected Boolean getSystemIsValid(CqfmSoftwareSystem system) {
        Boolean isValid = false;

        if (system != null) {
            String softwareSystemName = null;
            Boolean hasSoftwareSystemName = false;
            if (system.getName() != null && !system.getName().isEmpty()) {
                softwareSystemName = system.getName();
                hasSoftwareSystemName = true;
            }

            String version = null;
            Boolean hasVersion = false;
            if (system.getVersion() != null && !system.getVersion().isEmpty()) {
                version = system.getVersion();
                hasVersion = true;
            }

            isValid = hasSoftwareSystemName && hasVersion;
        }

        return isValid;
    }
}
