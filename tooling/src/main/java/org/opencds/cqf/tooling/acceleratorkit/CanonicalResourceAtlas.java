package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

public class CanonicalResourceAtlas {
    private CanonicalResourceProvider<StructureDefinition> structureDefinitions;
    public CanonicalResourceProvider<StructureDefinition> getStructureDefinitions() {
        return structureDefinitions;
    }
    public CanonicalResourceAtlas setStructureDefinitions(CanonicalResourceProvider<StructureDefinition> structureDefinitions) {
        this.structureDefinitions = structureDefinitions;
        return this;
    }

    private CanonicalResourceProvider<CodeSystem> codeSystems;
    public CanonicalResourceProvider<CodeSystem> getCodeSystems() {
        return codeSystems;
    }
    public CanonicalResourceAtlas setCodeSystems(CanonicalResourceProvider<CodeSystem> codeSystems) {
        this.codeSystems = codeSystems;
        return this;
    }

    private CanonicalResourceProvider<ValueSet> valueSets;
    public CanonicalResourceProvider<ValueSet> getValueSets() {
        return valueSets;
    }
    public CanonicalResourceAtlas setValueSets(CanonicalResourceProvider<ValueSet> valueSets) {
        this.valueSets = valueSets;
        return this;
    }

    private CanonicalResourceProvider<ConceptMap> conceptMaps;
    public CanonicalResourceProvider<ConceptMap> getConceptMaps() {
        return conceptMaps;
    }
    public CanonicalResourceAtlas setConceptMaps(CanonicalResourceProvider<ConceptMap> conceptMaps) {
        this.conceptMaps = conceptMaps;
        return this;
    }
}
