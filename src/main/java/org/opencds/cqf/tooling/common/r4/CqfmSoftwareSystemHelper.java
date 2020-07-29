package org.opencds.cqf.tooling.common.r4;

import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.Main;
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;

import java.util.ArrayList;
import java.util.List;

public class CqfmSoftwareSystemHelper extends BaseCqfmSoftwareSystemHelper {

    private Device createCqfToolingDevice() {
        Device device = new Device();

        device.setId(this.getCqfToolingDeviceID());

        /* meta.profile */
        Meta meta = new Meta();
        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm");
        device.setMeta(meta);

        /* type */
        Coding typeCoding = new Coding();
        typeCoding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
        typeCoding.setCode("tooling");

        List<Coding> typeCodingList = new ArrayList();
        typeCodingList.add(typeCoding);

        CodeableConcept type = new CodeableConcept();
        type.setCoding(typeCodingList);
        device.setType(type);

        /* version */
        String version = Main.class.getPackage().getImplementationVersion();
        Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent(new StringType(version));
        device.addVersion(versionComponent);

        return device;
    }

    public <T extends DomainResource> void ensureToolingExtensionAndDevice(T resource) {
        String fhirType = resource.fhirType();
        if (!fhirType.equals("Library") && !fhirType.equals("Measure")) {
            throw new IllegalArgumentException(String.format("cqfm-softwaresystem extension is only supported for Library and Measure resources, not %s", fhirType));
        }

        /* Extension */
        List<Extension> extensions = resource.getExtension();
        Extension cqfToolingExtension = null;
        for (Extension ext : extensions) {
            if (ext.getValue().fhirType().equals("Reference") && ((Reference)ext.getValue()).getReference().equals(this.getCqfToolingDeviceReferenceID())) {
                cqfToolingExtension = ext;
            }
        }

        if (cqfToolingExtension == null) {
            cqfToolingExtension = new Extension();
            cqfToolingExtension.setUrl(this.getCqfmSoftwareSystemExtensionUrl());
            Reference reference = new Reference();
            reference.setReference(this.getCqfToolingDeviceReferenceID());
            cqfToolingExtension.setValue(reference);

            resource.addExtension(cqfToolingExtension);
        }

        /* Contained Device Resource */
        Device cqfToolingDevice = null;
        for (Resource containedResource : resource.getContained()) {
            if (containedResource.getId().equals(this.getCqfToolingDeviceReferenceID()) && containedResource.getResourceType() == ResourceType.Device) {
                cqfToolingDevice = (Device)containedResource;
            }
        }

        if (cqfToolingDevice == null) {
            cqfToolingDevice = createCqfToolingDevice();
            resource.addContained(cqfToolingDevice);
        }
        else {
            String version = Main.class.getPackage().getImplementationVersion();
            Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent(new StringType(version));

            if (cqfToolingDevice.hasVersion()) {
                cqfToolingDevice.getVersion().clear();
            }

            cqfToolingDevice.addVersion(versionComponent);
        }
    }
}
