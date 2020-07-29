package org.opencds.cqf.tooling.common.r4;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.tooling.Main;
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;

public class CqfmSoftwareSystemHelper extends BaseCqfmSoftwareSystemHelper {

    private Device createCqfToolingDevice() {
        final Device device = new Device();

        device.setId(this.getCqfToolingDeviceID());

        /* meta.profile */
        final Meta meta = new Meta();
        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm");
        device.setMeta(meta);

        /* type */
        final Coding typeCoding = new Coding();
        typeCoding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
        typeCoding.setCode("tooling");

        final List<Coding> typeCodingList = new ArrayList<>();
        typeCodingList.add(typeCoding);

        final CodeableConcept type = new CodeableConcept();
        type.setCoding(typeCodingList);
        device.setType(type);

        /* version */
        final String version = Main.class.getPackage().getImplementationVersion();
        final Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent(
                new StringType(version));
        device.addVersion(versionComponent);

        return device;
    }

    public <T extends DomainResource> void ensureToolingExtensionAndDevice(final T resource) {
        final String fhirType = resource.fhirType();
        if (!fhirType.equals("Library") && !fhirType.equals("Measure")) {
            throw new IllegalArgumentException(String.format(
                    "cqfm-softwaresystem extension is only supported for Library and Measure resources, not %s",
                    fhirType));
        }

        /* Extension */
        final List<Extension> extensions = resource.getExtension();
        Extension cqfToolingExtension = null;
        for (final Extension ext : extensions) {
            if (ext.getValue().fhirType().equals("Reference")
                    && ((Reference) ext.getValue()).getReference().equals(this.getCqfToolingDeviceReferenceID())) {
                cqfToolingExtension = ext;
            }
        }

        if (cqfToolingExtension == null) {
            cqfToolingExtension = new Extension();
            cqfToolingExtension.setUrl(this.getCqfmSoftwareSystemExtensionUrl());
            final Reference reference = new Reference();
            reference.setReference(this.getCqfToolingDeviceReferenceID());
            cqfToolingExtension.setValue(reference);

            resource.addExtension(cqfToolingExtension);
        }

        /* Contained Device Resource */
        Device cqfToolingDevice = null;
        for (final Resource containedResource : resource.getContained()) {
            if (containedResource.getId().equals(this.getCqfToolingDeviceReferenceID())
                    && containedResource.getResourceType() == ResourceType.Device) {
                cqfToolingDevice = (Device) containedResource;
            }
        }

        if (cqfToolingDevice == null) {
            cqfToolingDevice = createCqfToolingDevice();
            resource.addContained(cqfToolingDevice);
        } else {
            final String version = Main.class.getPackage().getImplementationVersion();
            final Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent(
                    new StringType(version));

            if (cqfToolingDevice.hasVersion()) {
                cqfToolingDevice.getVersion().clear();
            }

            cqfToolingDevice.addVersion(versionComponent);
        }
    }
}
