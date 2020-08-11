package org.opencds.cqf.tooling.common.stu3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.opencds.cqf.tooling.Main;
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;

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

        List<Coding> typeCodingList = new ArrayList<>();
        typeCodingList.add(typeCoding);

        CodeableConcept type = new CodeableConcept();
        type.setCoding(typeCodingList);
        device.setType(type);

        /* version */
        String version = Main.class.getPackage().getImplementationVersion();
        device.setVersion(version);

        return device;
    }

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

            if (resourceInPath.getResourceType().toString().toLowerCase().equals("device") && resourceInPath.getIdElement().getIdPart().equals(this.getCqfToolingDeviceID())) {
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
                .collect(Collectors.toList());

            if (!containedWithoutDevice.isEmpty())
                resource.setContained(containedWithoutDevice);
            else resource.setContained(null);
        }
    }
}
