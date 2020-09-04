package org.opencds.cqf.tooling.common.stu3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.tooling.Main;
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.common.CqfmSoftwareSystem;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;

public class CqfmSoftwareSystemHelper extends BaseCqfmSoftwareSystemHelper {

//    private Device createCqfToolingDeviceOLD() {
//        Device device = new Device();
//        device.setId(this.getCqfToolingDeviceName());
//
//        /* meta.profile */
//        Meta meta = new Meta();
//        meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm");
//        device.setMeta(meta);
//
//        /* type */
//        Coding typeCoding = new Coding();
//        typeCoding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
//        typeCoding.setCode("tooling");
//
//        List<Coding> typeCodingList = new ArrayList<>();
//        typeCodingList.add(typeCoding);
//
//        CodeableConcept type = new CodeableConcept();
//        type.setCoding(typeCodingList);
//        device.setType(type);
//
//        /* version */
//        String version = Main.class.getPackage().getImplementationVersion();
//        device.setVersion(version);
//
//        return device;
//    }

    public <T extends DomainResource> void ensureToolingExtensionAndDevice(final T resource, FhirContext fhirContext) {
        final String fhirType = resource.fhirType();
        if (!fhirType.equals("Library") && !fhirType.equals("Measure")) {
            throw new IllegalArgumentException(String.format(
                    "cqfm-softwaresystem extension is only supported for Library and Measure resources, not %s",
                    fhirType));
        }

        Device device = null;
        for (String path : IOUtils.getDevicePaths(fhirContext)) {
            DomainResource resourceInPath;
            try {
                if (path.endsWith("xml")) {
                    XmlParser xmlParser = (XmlParser)fhirContext.newXmlParser();
                    resourceInPath = (DomainResource) xmlParser.parseResource(new FileReader(new File(path)));
                }
                else {
                    JsonParser jsonParser = (JsonParser)fhirContext.newJsonParser();
                    resourceInPath = (DomainResource) jsonParser.parseResource(new FileReader(new File(path)));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
            }

            if (resourceInPath.getResourceType().toString().toLowerCase().equals("device") && resourceInPath.getIdElement().getIdPart().equals(this.getCqfToolingDeviceName())) {
                device = (Device) resourceInPath;
            }
        }

        /* Extension */
        final List<Extension> extensions = resource.getExtension();
        Extension cqfToolingExtension = null;
        for (final Extension ext : extensions) {
            if (ext.getValue().fhirType().equals("Reference")
                    && ((Reference) ext.getValue()).getReference().equals(this.getCqfToolingDeviceReferenceID())) {
                cqfToolingExtension = ext;
                ((Reference) cqfToolingExtension.getValue()).setResource(null);
            }
        }

        // Add Extension if device exists in IG.
        if (cqfToolingExtension == null && device != null) {
            cqfToolingExtension = new Extension();
            cqfToolingExtension.setUrl(this.getCqfmSoftwareSystemExtensionUrl());
            final Reference reference = new Reference();
            reference.setReference(this.getCqfToolingDeviceReferenceID());
            cqfToolingExtension.setValue(reference);

            resource.addExtension(cqfToolingExtension);
        }
        // Remove Extension if device does not exist in IG.
        else if (device == null) {
            if (resource.hasExtension()) {
                resource.setExtension(extensions.stream()
                    .filter(extension -> !extension.getUrl().equals(this.getCqfmSoftwareSystemExtensionUrl()))
                    .collect(Collectors.toList()));
            }
        }

        /* Contained Device Resource */
//        Device cqfToolingDevice = null;
//        for (final Resource containedResource : resource.getContained()) {
//            if (containedResource.getId().equals(this.getCqfToolingDeviceReferenceID())
//                    && containedResource.getResourceType() == ResourceType.Device) {
//                cqfToolingDevice = (Device) containedResource;
//            }
//        }
//
//        if (cqfToolingDevice == null) {
//            cqfToolingDevice = createCqfToolingDevice();
//            resource.addContained(cqfToolingDevice);
//        } else {
//            final String version = Main.class.getPackage().getImplementationVersion();
//            cqfToolingDevice.setVersion(version);
//        }

        if (resource.hasContained()) {
            List<Resource> containedWithoutDevice = resource.getContained().stream()
                .filter(containedResource -> !(containedResource.getId().equals(this.getCqfToolingDeviceReferenceID())
                        && containedResource.getResourceType() == ResourceType.Device))
                .filter(containedResource -> !(containedResource.getId().equals(this.getCqfToolingDeviceName())
                        && containedResource.getResourceType() == ResourceType.Device))
                .collect(Collectors.toList());

            if (!containedWithoutDevice.isEmpty())
                resource.setContained(containedWithoutDevice);
            else resource.setContained(null);
        }
    }


    /*****************************************************/
    private <T extends DomainResource> void validateResourceForSoftwareSystemExtension(T resource) {
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

    public <T extends DomainResource> void ensureSoftwareSystemExtensionAndDevice(T resource, List<CqfmSoftwareSystem> softwareSystems, FhirContext fhirContext) {
        validateResourceForSoftwareSystemExtension(resource);

        if (softwareSystems != null && !softwareSystems.isEmpty()) {
            for (CqfmSoftwareSystem system : softwareSystems) {
                ensureSoftwareSystemExtensionAndDevice(resource, system, fhirContext);
            }
        }
    }

    public <T extends DomainResource> void ensureSoftwareSystemExtensionAndDevice(T resource, CqfmSoftwareSystem system, FhirContext fhirContext) {
        validateResourceForSoftwareSystemExtension(resource);

        if (this.getSystemIsValid(system)) {
            // NOTE: No longer adding as contained as Publisher doesn't extract/respect them so the publishing fails.
            //String systemReferenceID = "#" + system.getName();
            String systemReferenceID = system.getName();

            // Is a device defined in devicePaths? If so, get it.
            Device device = null;
            for (String path : IOUtils.getDevicePaths(fhirContext)) {
                DomainResource resourceInPath;
                try {
                    if (path.endsWith("xml")) {
                        XmlParser xmlParser = (XmlParser)fhirContext.newXmlParser();
                        resourceInPath = (DomainResource) xmlParser.parseResource(new FileReader(new File(path)));
                    }
                    else {
                        JsonParser jsonParser = (JsonParser)fhirContext.newJsonParser();
                        resourceInPath = (DomainResource) jsonParser.parseResource(new FileReader(new File(path)));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
                }

                // NOTE: Takes the first device that matches on ID and Version.
                if (resourceInPath.getResourceType().toString().toLowerCase().equals("device")) {
                    Device prospectDevice = (Device)resourceInPath;
                    if (prospectDevice.getIdElement().getIdPart().equals(systemReferenceID) && prospectDevice.getVersion().equals(system.getVersion())) {
                        device = (Device) resourceInPath;
                        break;
                    }
                }
            }

            /* Extension */
            final List<Extension> extensions = resource.getExtension();
            Extension softwareSystemExtension = null;
            for (Extension ext : extensions) {
                if (ext.getValue().fhirType().equals("Reference") && ((Reference) ext.getValue()).getReference().equals(systemReferenceID)) {
                    softwareSystemExtension = ext;
                    ((Reference)softwareSystemExtension.getValue()).setResource(null);
                }
            }

            if (softwareSystemExtension == null && device != null) {
                softwareSystemExtension = new Extension();
                softwareSystemExtension.setUrl(this.getCqfmSoftwareSystemExtensionUrl());
                final Reference reference = new Reference();
                reference.setReference(systemReferenceID);
                softwareSystemExtension.setValue(reference);

                resource.addExtension(softwareSystemExtension);
            }
            // Remove Extension if device does not exist in IG.
            else if (device == null) {
                if (resource.hasExtension()) {
                    resource.setExtension(extensions.stream()
                            .filter(extension -> !extension.getUrl().equals(this.getCqfmSoftwareSystemExtensionUrl()))
                            .collect(Collectors.toList()));
                }
            }

            /* Contained Device Resource */
            Device softwareDevice = null;
            for (Resource containedResource : resource.getContained()) {
                if (containedResource.getId().equals(systemReferenceID) && containedResource.getResourceType() == ResourceType.Device) {
                    softwareDevice = (Device)containedResource;
                }
            }

            // If Contained only contains the proposed device then set it to null, else set it to include the other
            // entries that it contained. i.e. remove any Contained entries for the proposed device/system
            if (resource.hasContained()) {
                List<Resource> containedWithoutDevice = resource.getContained().stream()
                        .filter(containedResource -> !(containedResource.getId().equals(systemReferenceID)
                                && containedResource.getResourceType() == ResourceType.Device))
                        .collect(Collectors.toList());

                if (!containedWithoutDevice.isEmpty())
                    resource.setContained(containedWithoutDevice);
                else resource.setContained(null);
            }

            // TODO: We need to decide how we're handling this if the Device isn't going ot be contained.
            // e.g., If there is an existing device with a different version, we can't just update the version, can we?
            // Need to design what should happen and then instead of removing references we should be ensuring that the
            // device exists in the IG
//            if (softwareDevice == null) {
//                softwareDevice = createSoftwareSystemDevice(system);
//                resource.addContained(softwareDevice);
//            }
//            else {
//                addVersion(softwareDevice, system.getVersion());
//            }
        }
    }

//    private Device createSoftwareSystemDevice(CqfmSoftwareSystem system) {
//        Device device = null;
//
//        if (this.getSystemIsValid(system)) {
//            device = new Device();
//            device.setId(system.getName());
//
//            /* meta.profile */
//            Meta meta = new Meta();
//            meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm");
//            device.setMeta(meta);
//
//            device.setManufacturer(system.getName());
//
//            /* type */
//            Coding typeCoding = new Coding();
//            typeCoding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
//            typeCoding.setCode("tooling");
//
//            List<Coding> typeCodingList = new ArrayList();
//            typeCodingList.add(typeCoding);
//
//            CodeableConcept type = new CodeableConcept();
//            type.setCoding(typeCodingList);
//            device.setType(type);
//
//            /* version */
//            String version = system.getVersion();
//            device.setVersion(version);
//        }
//
//        return device;
//    }

    /* cqf-tooling specific logic */
//    private Device createCqfToolingDevice() {
//        CqfmSoftwareSystem softwareSystem = new CqfmSoftwareSystem(this.getCqfToolingDeviceName(), Main.class.getPackage().getImplementationVersion());
//        Device device = createSoftwareSystemDevice(softwareSystem);
//
//        return device;
//    }

    public <T extends DomainResource> void ensureCQFToolingExtensionAndDevice(T resource, FhirContext fhirContext) {
        CqfmSoftwareSystem cqfToolingSoftwareSystem = new CqfmSoftwareSystem(this.getCqfToolingDeviceName(), Main.class.getPackage().getImplementationVersion());
        ensureSoftwareSystemExtensionAndDevice(resource, cqfToolingSoftwareSystem, fhirContext);
    }
}
