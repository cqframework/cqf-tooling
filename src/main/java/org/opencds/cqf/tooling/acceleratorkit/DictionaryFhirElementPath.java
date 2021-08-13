package org.opencds.cqf.tooling.acceleratorkit;

import java.util.Arrays;

import org.hl7.fhir.r4.model.Enumerations;

/**
 * Created by Bryn on 8/18/2019.
 */
public class DictionaryFhirElementPath {
    private String resourceType;
    public String getResourceType() {
        return this.resourceType;
    }
    private String resourcePath;
    public String getResourcePath() {
        return this.resourcePath;
    }

    private String resourceTypeAndPath;
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

    private String fhirElementType;
    public String getFhirElementType() {
        return this.fhirElementType;
    }
    public void setFhirElementType(String fhirElementType) { this.fhirElementType = fhirElementType; }
}
