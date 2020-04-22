package org.opencds.cqf.common;

import org.hl7.fhir.dstu3.model.Device;

public abstract class BaseCqfmSoftwareSystemHelper {
    private static String cqfToolingDeviceID = "cqf-tooling";
    private static String cqfToolingDeviceReferenceID = "#" + cqfToolingDeviceID;
    private static String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";

    protected String getCqfToolingDeviceID() { return cqfToolingDeviceID; }
    protected String getCqfToolingDeviceReferenceID() { return cqfToolingDeviceReferenceID; }
    protected String getCqfmSoftwareSystemExtensionUrl() { return cqfmSoftwareSystemExtensionUrl; }
}
