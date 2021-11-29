package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureDefinitionBaseVisitorTest {
    private CanonicalResourceAtlas atlas;
    private List<ValueSet> valueSets = new ArrayList<>();
    private List<CodeSystem> codeSystems = new ArrayList<>();
    private Map<String, ConceptMap> conceptMaps = new LinkedHashMap<>();


    /*
    Create an atlas
    then call StructureDefinitionBaseVisitor(atlas);
    then read in structuredefinition
    then call visitStructureDefinition(StructureDefinition sd)
    on return
     */


    private CanonicalResourceAtlas getAtlas() {
        if (atlas == null) {
            atlas =
                    new CanonicalResourceAtlas()
                            .setValueSets(new InMemoryCanonicalResourceProvider<>(this.valueSets))
                            .setCodeSystems(new InMemoryCanonicalResourceProvider<>(this.codeSystems))
                            .setConceptMaps(new InMemoryCanonicalResourceProvider<>(this.conceptMaps.values()));
        }
        return atlas;
    }
}