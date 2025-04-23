package org.opencds.cqf.tooling.common.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.tooling.common.BaseSoftwareSystemHelper;
import org.opencds.cqf.tooling.common.SoftwareSystem;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.constants.CqfConstants;
import org.opencds.cqf.tooling.utilities.constants.CrmiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SoftwareSystemHelper extends BaseSoftwareSystemHelper {

    private static final Logger logger = LoggerFactory.getLogger(SoftwareSystemHelper.class);

    public SoftwareSystemHelper() { }

    public SoftwareSystemHelper(String rootDir) {
        super(rootDir);
    }

    private <T extends DomainResource> void validateResourceForSoftwareSystemExtension(T resource) {
        if (resource == null) {
            throw new IllegalArgumentException("No resource provided.");
        }
    }

    public <T extends DomainResource> void ensureSoftwareSystemExtensionAndDevice(T resource, List<SoftwareSystem> softwareSystems, FhirContext fhirContext) {
        validateResourceForSoftwareSystemExtension(resource);

        if (softwareSystems != null && !softwareSystems.isEmpty()) {
            for (SoftwareSystem system : softwareSystems) {
                ensureSoftwareSystemExtensionAndDevice(resource, system, fhirContext);
            }
        }
    }

    public <T extends DomainResource> void ensureSoftwareSystemExtensionAndDevice(T resource, SoftwareSystem system, FhirContext fhirContext) {
        validateResourceForSoftwareSystemExtension(resource);

        if (this.getSystemIsValid(system)) {
            // NOTE: No longer adding as contained as Publisher doesn't extract/respect them so the publishing fails.
            //String systemReferenceID = "#" + system.getName();
            String systemDeviceId = system.getName();
            String systemReference = "Device/" + systemDeviceId;

            // Is a device defined in devicePaths? If so, get it.
            Device device = null;
            String deviceOutputPath = getRootDir() + devicePath;
            IOUtils.Encoding deviceOutputEncoding = IOUtils.Encoding.JSON;
            for (String path : IOUtils.getDevicePaths(fhirContext)) {
                DomainResource resourceInPath;
                if (path.endsWith("xml")) {
                    deviceOutputEncoding = IOUtils.Encoding.XML;
                    XmlParser xmlParser = (XmlParser)fhirContext.newXmlParser();
                    try (FileReader reader = new FileReader(path)) {
                        resourceInPath = (DomainResource) xmlParser.parseResource(reader);
                    } catch (IOException e) {
                        logger.warn("Error parsing " + e.getLocalizedMessage(), e);
                        throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
                    }
                }
                else {
                    JsonParser jsonParser = (JsonParser)fhirContext.newJsonParser();
                    try (FileReader reader = new FileReader(path)) {
                        resourceInPath = (DomainResource) jsonParser.parseResource(reader);
                    } catch (IOException e) {
                        logger.warn("Error parsing " + e.getLocalizedMessage(), e);
                        throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
                    }
                }

                // NOTE: Takes the first device that matches on ID and Version.
                if (resourceInPath.getResourceType().toString().equalsIgnoreCase("device")) {
                    Device prospectDevice = (Device)resourceInPath;
                    if (prospectDevice.getIdElement().getIdPart().equals(systemDeviceId)) {
                        device = (Device) resourceInPath;
                        deviceOutputPath = path;
                        break;
                    }
                }
            }

            /* Create the device if one doesn't already exist */
            if (device == null) {
                device = createSoftwareSystemDevice(system);
            }

            /* Ensure that device has the current/proposed version */
            device.setVersion(system.getVersion());

            /* Persist the new/updated Device */
            EnsureDevicePath();
            IOUtils.writeResource(device, deviceOutputPath, deviceOutputEncoding, fhirContext);

            /* Extension */
            final List<Extension> extensions = resource.getExtension();
            Extension softwareSystemExtension = null;
            for (Extension ext : extensions) {
                if (ext.getValue().fhirType().equals("Reference") && ((Reference) ext.getValue()).getReference().endsWith(systemDeviceId)) {
                    softwareSystemExtension = ext;
                    ((Reference)softwareSystemExtension.getValue()).setResource(null);
                }
            }

            if (softwareSystemExtension == null) {
                softwareSystemExtension = new Extension();
                softwareSystemExtension.setUrl(CrmiConstants.SOFTWARE_SYSTEM_EXT_URL);
                final Reference reference = new Reference();
                reference.setReference(systemReference);
                softwareSystemExtension.setValue(reference);

                resource.addExtension(softwareSystemExtension);
            }
            else if (!systemReference.equals(((Reference)softwareSystemExtension.getValue()).getReference())) {
                ((Reference)softwareSystemExtension.getValue()).setReference(systemReference);
            }
            // Remove Extension if device does not exist in IG and we have not been able to create it for some reason.
            else {
                if (resource.hasExtension()) {
                    resource.setExtension(extensions.stream()
                        .filter(extension -> !extension.getUrl().equals(CrmiConstants.SOFTWARE_SYSTEM_EXT_URL))
                        .collect(Collectors.toList()));
                }
            }

            // If Contained only contains the proposed device then set it to null, else set it to include the other
            // entries that it contained. i.e. remove any Contained entries for the proposed device/system
            if (resource.hasContained()) {
                List<Resource> containedWithoutDevice = resource.getContained().stream()
                    .filter(containedResource -> !(containedResource.getId().equals(systemDeviceId)
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

//            /* Contained Device Resource */
//            Device softwareDevice = null;
//            for (Resource containedResource : resource.getContained()) {
//                if (containedResource.getId().equals(systemReferenceID) && containedResource.getResourceType() == ResourceType.Device) {
//                    softwareDevice = (Device)containedResource;
//                }
//            }

//            if (softwareDevice == null) {
//                softwareDevice = createSoftwareSystemDevice(system);
//                resource.addContained(softwareDevice);
//            }
//            else {
//                addVersion(softwareDevice, system.getVersion());
//            }
        }
    }

    public Device createSoftwareSystemDevice(SoftwareSystem system) {
        Device device = null;

        if (this.getSystemIsValid(system)) {
            device = new Device();
            device.setId(system.getName());

            /* meta.profile */
            Meta meta = new Meta();
            meta.addProfile(CrmiConstants.SOFTWARE_SYSTEM_DEVICE_PROFILE_URL);
            device.setMeta(meta);

            device.setManufacturer(system.getManufacturer());
            device.setModel(system.getName());

            /* type */
            Coding typeCoding = new Coding();
            typeCoding.setSystem(CrmiConstants.SOFTWARE_SYSTEM_DEVICE_TYPE_SYSTEM_URL);
            typeCoding.setCode("tooling");

            List<Coding> typeCodingList = new ArrayList<>();
            typeCodingList.add(typeCoding);

            CodeableConcept type = new CodeableConcept();
            type.setCoding(typeCodingList);
            device.setType(type);

            /* version */
            String version = system.getVersion();
            device.setVersion(version);
        }

        return device;
    }

    /* cqf-tooling specific logic */
//    private Device createCqfToolingDevice() {
//        CqfmSoftwareSystem softwareSystem = new CqfmSoftwareSystem(this.getCqfToolingDeviceName(), Main.class.getPackage().getImplementationVersion());
//        Device device = createSoftwareSystemDevice(softwareSystem);
//
//        return device;
//    }

    public <T extends DomainResource> void ensureCQFToolingExtensionAndDevice(T resource, FhirContext fhirContext) {
        SoftwareSystem cqfToolingSoftwareSystem = new SoftwareSystem(CqfConstants.CQF_TOOLING_DEVICE_NAME, SoftwareSystemHelper.class.getPackage().getImplementationVersion(), SoftwareSystemHelper.class.getPackage().getImplementationVendor());
        ensureSoftwareSystemExtensionAndDevice(resource, cqfToolingSoftwareSystem, fhirContext);
    }
}
