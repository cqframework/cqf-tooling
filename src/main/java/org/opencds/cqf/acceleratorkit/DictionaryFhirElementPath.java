package org.opencds.cqf.acceleratorkit;

import org.hl7.fhir.r4.model.Enumerations;

import java.util.Arrays;

/**
 * Created by Bryn on 8/18/2019.
 */
public class DictionaryFhirElementPath {
    private String resourceType = "Observation";
    public String getResourceType() {
        return this.resourceType;
    }
    private String resourcePath = "value[x]";
    public String getResourcePath() {
        return this.resourcePath;
    }

    private String resourceTypeAndPath = "Observation.value[x]";
    public String getResourceTypeAndPath() {
        return this.resourceTypeAndPath;
    }

    public void setResource(String resource) {
        this.resourceTypeAndPath = resource;
        if (resource.contains(".")) {
            String[] elements = resource.split("\\.");
            if (elements.length >= 2) {
                switch (elements[0].toLowerCase()) {
                    case "observation": this.resourceType = "Observation"; break;
                    case "encounter": this.resourceType = "Encounter"; break;
                    case "patient": this.resourceType = "Patient"; break;
                    case "coverage": this.resourceType = "Coverage"; break;
                    case "medicationstatement": this.resourceType = "MedicationStatement"; break;
                    default: this.resourceType = elements[0]; break;
                }
                this.resourcePath = String.join(".", Arrays.copyOfRange(elements, 1, elements.length));
            }
        }
    }

    private String baseProfile;
    public String getBaseProfile() {
        //TODO: Naive check for a URL may need to be improved.
        if (this.baseProfile != null && !this.baseProfile.isEmpty() && !this.baseProfile.toLowerCase().equals("fhir")) {
            return this.baseProfile;
        }
        return String.format("http://hl7.org/fhir/StructureDefinition/%s", this.getResourceType());
    }
    public void setBaseProfile(String baseProfile) {
        this.baseProfile = baseProfile;
    }

    private String masterDataElementPath;
    public String getMasterDataElementPath() {
        return this.masterDataElementPath;
    }
    public void setMasterDataElementPath(String masterDataElementPath) { this.masterDataElementPath = masterDataElementPath; }

    private String fhirElementType;
    public String getFhirElementType() {
        return this.fhirElementType;
    }
    public void setFhirElementType(String fhirElementType) { this.fhirElementType = fhirElementType; }

    private String customProfileId;
    public String getCustomProfileId() { return this.customProfileId; }
    public void setCustomProfileId(String customProfileId) { this.customProfileId = customProfileId; }

    private String additionalFHIRMappingDetails;
    public String getAdditionalFHIRMappingDetails() { return this.additionalFHIRMappingDetails; }
    public void setAdditionalFHIRMappingDetails(String additionalFHIRMappingDetails) { this.additionalFHIRMappingDetails = additionalFHIRMappingDetails; }

    private String customValueSetName;
    public String getCustomValueSetName() { return this.customValueSetName; }
    public void setCustomValueSetName(String customValueSetName) { this.customValueSetName = customValueSetName; }

    private String bindingStrength;
    public Enumerations.BindingStrength getBindingStrength() {
        if (this.bindingStrength == null || this.bindingStrength.isEmpty()) {
            return Enumerations.BindingStrength.REQUIRED;
        }

        switch (this.bindingStrength.toLowerCase()) {
            case "example":
                return Enumerations.BindingStrength.EXAMPLE;
            case "extensible":
                return Enumerations.BindingStrength.EXTENSIBLE;
            default:
                return Enumerations.BindingStrength.REQUIRED;
        }
    }
    public void setBindingStrength(String bindingStrength) { this.bindingStrength = bindingStrength; }

    private String unitOfMeasure;
    public String getUnitOfMeasure() { return this.unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    private String extensionNeeded;
    public String getExtensionNeeded() { return this.extensionNeeded; }
    public void setExtensionNeeded(String extensionNeeded) { this.extensionNeeded = extensionNeeded; }

    private String version;
    public String getVersion() {
        if (this.version != null && !this.version.isEmpty()) {
            return this.version;
        }
        return "4.0.1";
    }
    public void setVersion(String version) {
        this.version = version;
    }

}
