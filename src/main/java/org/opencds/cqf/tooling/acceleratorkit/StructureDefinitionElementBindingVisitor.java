package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class StructureDefinitionElementBindingVisitor extends StructureDefinitionBaseVisitor {
    private FhirContext fc;
    private CanonicalResourceAtlas canonicalResourceAtlas;
    private CanonicalResourceAtlas canonicalResourceDependenciesAtlas;


    public StructureDefinitionElementBindingVisitor(CanonicalResourceAtlas atlas, CanonicalResourceAtlas dependencyAtlas) {
        fc = FhirContext.forCached(FhirVersionEnum.R4);
        this.canonicalResourceAtlas = atlas;
        this.canonicalResourceDependenciesAtlas = dependencyAtlas;
    }

    public Map<String, StructureDefinitionBindingObject> visitStructureDefinition(StructureDefinition sd) {
        if (sd == null) {
            throw new IllegalArgumentException("sd required");
        }
        if (sd.getType() == null) {
            throw new IllegalArgumentException("type required");
        }
        String sdURL = sd.getUrl();
        String sdVersion = sd.getVersion();
        String sdName = sd.getName();
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        List<ElementDefinition> eds = super.visitSnapshot(sd);
        if (eds != null && !eds.isEmpty()) {
            getBindings(sdName, eds, sdURL, sdVersion, bindingObjects);
        }
        eds = super.visitDifferential(sd);
        if (eds != null && !eds.isEmpty()) {
            getBindings(sdName, eds, sdURL, sdVersion, bindingObjects);
        }
        if (sd.hasBaseDefinition()) {
            bindingObjects.putAll(visitStructureDefinition(this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrlWithVersion(sd.getBaseDefinition())));
        }
        return bindingObjects;
    }

    public Map<String, StructureDefinitionBindingObject> visitCanonicalAtlasStructureDefinitions() {
        Iterable<StructureDefinition> iterableStructureDefinitions = canonicalResourceAtlas.getStructureDefinitions().get();
        Map<String, StructureDefinition> sdMap = new HashMap<>();
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        iterableStructureDefinitions.forEach((structureDefinition) -> {
            Map<String, StructureDefinitionBindingObject> newBindingObjects = visitStructureDefinition(structureDefinition);
            if (null != newBindingObjects) {
                bindingObjects.putAll(newBindingObjects);
            }
        });
        return bindingObjects;
    }

    private void getBindings(String sdName, List<ElementDefinition> eds, String sdURL, String sdVersion, Map<String, StructureDefinitionBindingObject> bindingObjects) {
        AtomicReference<Integer> index = new AtomicReference<Integer>(0);
        while (index.get() < eds.size()) {
            ElementDefinition ed = eds.get(index.get());
            if (ed.hasBinding() && null != ed.getBinding().getValueSet()) {
                StructureDefinitionBindingObject sdbo = new StructureDefinitionBindingObject();
                sdbo.setSdName(sdName);
                sdbo.setSdURL(sdURL);
                sdbo.setSdVersion(sdVersion);
                sdbo.setBindingStrength(ed.getBinding().getStrength().toString().toLowerCase());
                String bindingValueSet = ed.getBinding().getValueSet();
                String pipeVersion = "";
                if (bindingValueSet.contains("|")) {
                    pipeVersion = bindingValueSet.substring(bindingValueSet.indexOf("|") + 1);
                    bindingValueSet = bindingValueSet.substring(0, bindingValueSet.indexOf("|"));
                }
                sdbo.setBindingValueSetURL(bindingValueSet);
                sdbo.setElementPath(ed.getPath());
                String valueSetVersion = "";
                if (null != this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL())) {
                    valueSetVersion = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getVersion();
                    sdbo.setBindingValueSetName(this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getName());
                } else if (null != this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL())) {
                    valueSetVersion = this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getVersion();
                    sdbo.setBindingValueSetName(this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getName());
                } else if (valueSetVersion.length() < 1 && bindingValueSet.contains("|")) {
                    valueSetVersion = pipeVersion;
                }
                if (ed.getMustSupport()) {
                    sdbo.setMustSupport("Y");
                } else {
                    sdbo.setMustSupport("N");
                }
                sdbo.setBindingValueSetVersion(valueSetVersion);
                bindingObjects.put(sdName + "." + sdbo.getElementPath(), sdbo);
            }
            index.set(index.get() + 1);
        }
    }
}
