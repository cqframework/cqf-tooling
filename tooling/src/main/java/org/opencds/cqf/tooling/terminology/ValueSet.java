package org.opencds.cqf.tooling.terminology;

import java.util.ArrayList;
import java.util.List;

public class ValueSet {

    private String profile;
    private String getProfile() { return profile; }
    private ValueSet setProfile(String in) {
        profile = in;
        return this;
    }

    private String system;
    public String getSystem() {
        return system;
    }
    public ValueSet setSystem(String system) {
        this.system = system;
        return this;
    }

    private String version;
    public String getVersion() {
        return version;
    }
    public ValueSet setVersion(String version) {
        this.version = version;
        return this;
    }

    private List<org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent> codes;
    public List<org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent> getCodes() {
        return codes;
    }
    public ValueSet setCodes(List<org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent> codes) {
        this.codes = codes;
        return this;
    }
    public void addCode(org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent code) {
        if (codes == null) {
            codes = new ArrayList<>();
        }
        codes.add(code);
    }

    public int getHashCode() {
        return system.hashCode() * (version != null ? version.hashCode() : 1);
    }
}
