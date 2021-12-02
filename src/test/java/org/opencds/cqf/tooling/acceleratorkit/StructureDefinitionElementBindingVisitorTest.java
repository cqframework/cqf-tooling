package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.BiConsumer;

public class StructureDefinitionElementBindingVisitorTest {
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
    @Test
    public void createAtlas(){
        String inputPath = "/Users/bryantaustin/Projects/FHIR-Spec";
        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);
        this.codeSystems = new ArrayList<>();
        atlas.getCodeSystems().forEach((key, codeSystem)->{
            this.codeSystems.add(codeSystem);
        });
        this.conceptMaps = atlas.getConceptMaps();
        this.valueSets = new ArrayList<>();
        atlas.getValueSets().forEach((key, valueSet)->{
            this.valueSets.add(valueSet);
        });

        CanonicalResourceAtlas canonicalResourceAtlas = getAtlas();

        StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas);
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        Map<String, StructureDefinition>scMap = atlas.getStructureDefinitions();
        scMap.forEach((key, sd)->{
            Map<String, StructureDefinitionBindingObject> newBindingObjects = sdbv.visitStructureDefinition(sd);
            if(null != newBindingObjects){
                bindingObjects.putAll(newBindingObjects);
            }
        });
    }


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