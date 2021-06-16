package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.StructureDefinition;


public class DictionaryProfileElementExtension {
    private String profileId;
    public String getProfileId() {
        return this.profileId;
    }
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    private String resourcePath;
    public String getResourcePath() { return this.resourcePath; }
    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }

    private DictionaryElement element;
    public DictionaryElement getElement() { return this.element; }
    public void setElement(DictionaryElement element) { this.element = element; }

    private StructureDefinition extension;
    public StructureDefinition getExtension() { return this.extension; }
    public void setExtension(StructureDefinition extension) { this.extension = extension; }
}
