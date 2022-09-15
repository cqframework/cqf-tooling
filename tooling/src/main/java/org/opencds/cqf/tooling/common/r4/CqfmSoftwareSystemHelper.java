package org.opencds.cqf.tooling.common.r4;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.cqframework.cql.Main;
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
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.common.CqfmSoftwareSystem;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;


public class CqfmSoftwareSystemHelper extends BaseCqfmSoftwareSystemHelper {

    private static Logger logger = LoggerFactory.getLogger(CqfmSoftwareSystemHelper.class);

    public CqfmSoftwareSystemHelper() { }

    public CqfmSoftwareSystemHelper(String rootDir) {
        super(rootDir);
    }

    @SuppressWarnings("serial")
    protected <T extends DomainResource> void validateResourceForSoftwareSystemExtension(T resource) {
        if (resource == null) {
            throw new IllegalArgumentException("No resource provided.");
        }

        List<String> eligibleResourceTypes = new ArrayList<String>() { {
            add("Library");
            add("Measure");
        } };

        String eligibleResourceTypesList = String.join(", ", eligibleResourceTypes);
        String fhirType = "Library";//resource.get resource.fhirType();
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
                    try (FileReader reader = new FileReader(new File(path))) {
                        resourceInPath = (DomainResource) xmlParser.parseResource(reader);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
                    }
                }
                else {
                    JsonParser jsonParser = (JsonParser)fhirContext.newJsonParser();
                    try (FileReader reader = new FileReader(new File(path))) {
                        resourceInPath = (DomainResource) jsonParser.parseResource(reader);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error parsing " + e.getLocalizedMessage());
                    }
                }

                // NOTE: Takes the first device that matches on ID.
                if (resourceInPath.getResourceType().toString().toLowerCase().equals("device")) {
                    Device prospectDevice = (Device)resourceInPath;
                    if (prospectDevice.getIdElement().getIdPart().equals(systemDeviceId)) {
                        device = (Device)resourceInPath;
                        deviceOutputPath = path;
                        break;
                    }
                }
            }

            /* Create the device if one doesn't already exist */
            if (device == null) {
                logger.debug("Creating Device");
                device = createSoftwareSystemDevice(system);
            }

            /* Ensure that device has the current/proposed version */
            Device.DeviceVersionComponent proposedVersion = new Device.DeviceVersionComponent(new StringType(system.getVersion()));
            List<Device.DeviceVersionComponent> proposedVersionList = new ArrayList<Device.DeviceVersionComponent>();
            proposedVersionList.add(proposedVersion);
            device.setVersion(proposedVersionList);

            /* Ensure that device has a name */
            Device.DeviceDeviceNameComponent proposedName = new Device.DeviceDeviceNameComponent();
            proposedName.setName(system.getName());
            proposedName.setType(Device.DeviceNameType.MANUFACTURERNAME);
            device.getDeviceName().clear();
            device.addDeviceName(proposedName);

            /* Ensure that device has a manufacturer */
            device.setManufacturer(system.getManufacturer());

            /* Persist the new/updated Device */
            EnsureDevicePath();
            IOUtils.writeResource(device, deviceOutputPath, deviceOutputEncoding, fhirContext);

            /* Extension */
            final List<Extension> extensions = resource.getExtension();
            Extension softwareSystemExtension = null;
            for (Extension ext : extensions) {
                if(ext.getValue() != null
                    && ext.getValue().fhirType() != null
                    && ext.getValue().fhirType().equals("Reference")
                    && ((Reference)ext.getValue()).getReference() != null
                    && ((Reference) ext.getValue()).getReference().endsWith(systemDeviceId)) {
                        softwareSystemExtension = ext;
                        ((Reference) softwareSystemExtension.getValue()).setResource(null);
                }
            }

            if (softwareSystemExtension == null && device != null) {
                softwareSystemExtension = new Extension();
                softwareSystemExtension.setUrl(this.getCqfmSoftwareSystemExtensionUrl());
                final Reference reference = new Reference();
                reference.setReference(systemReference);
                softwareSystemExtension.setValue(reference);

                resource.addExtension(softwareSystemExtension);
            }
            else if (softwareSystemExtension != null && !systemReference.equals(((Reference)softwareSystemExtension.getValue()).getReference())) {
                ((Reference)softwareSystemExtension.getValue()).setReference(systemReference);
            }
            else if (device == null) {
                if (resource.hasExtension(this.getCqfmSoftwareSystemExtensionUrl())) {
                    resource.setExtension(extensions.stream()
                        .filter(extension -> !extension.getUrl().equals(this.getCqfmSoftwareSystemExtensionUrl()))
                        .collect(Collectors.toList()));
                }
            }

            // NOTE: This seems like a cleanup from a previous implementation. Should it remain? If so, for how long?
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

    public Device createSoftwareSystemDevice(CqfmSoftwareSystem system) {
        Device device = null;

        if (this.getSystemIsValid(system)) {
            device = new Device();
            device.setId(system.getName());

            /* meta.profile */
            Meta meta = new Meta();
            meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm");
            device.setMeta(meta);

            device.addDeviceName().setName(system.getName()).setType(Device.DeviceNameType.MANUFACTURERNAME);
            device.setManufacturer(system.getName());

            /* type */
            Coding typeCoding = new Coding();
            typeCoding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
            typeCoding.setCode("tooling");

            List<Coding> typeCodingList = new ArrayList<>();
            typeCodingList.add(typeCoding);

            CodeableConcept type = new CodeableConcept();
            type.setCoding(typeCodingList);
            device.setType(type);

            /* version */
            addVersion(device, system.getVersion());
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
        CqfmSoftwareSystem cqfToolingSoftwareSystem = new CqfmSoftwareSystem(this.getCqfToolingDeviceName(), CqfmSoftwareSystemHelper.class.getPackage().getImplementationVersion(), Main.class.getPackage().getImplementationVendor());
        ensureSoftwareSystemExtensionAndDevice(resource, cqfToolingSoftwareSystem, fhirContext);
    }

    private void addVersion(Device device, String version) {
        // NOTE: The cqfm-softwaresystem extension restricts the cardinality of version to 0..1, so we overwrite any existing version entries each time
        Device.DeviceVersionComponent versionComponent = new Device.DeviceVersionComponent(new StringType(version));
        List<Device.DeviceVersionComponent> versionList = new ArrayList<Device.DeviceVersionComponent>();
        versionList.add(versionComponent);

        device.setVersion(versionList);
    }
}
