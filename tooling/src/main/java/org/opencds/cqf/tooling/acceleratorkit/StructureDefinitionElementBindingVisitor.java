package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StructureDefinitionElementBindingVisitor extends StructureDefinitionBaseVisitor {
    private FhirContext fc;
    private CanonicalResourceAtlas canonicalResourceAtlas;
    private CanonicalResourceAtlas canonicalResourceDependenciesAtlas;


    public StructureDefinitionElementBindingVisitor(CanonicalResourceAtlas atlas, CanonicalResourceAtlas dependencyAtlas) {
        fc = FhirContext.forCached(FhirVersionEnum.R4);
        this.canonicalResourceAtlas = atlas;
        this.canonicalResourceDependenciesAtlas = dependencyAtlas;
    }

    public Map<String, StructureDefinitionBindingObject> visitStructureDefinition(StructureDefinition sd, boolean snapshotOnly) {
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
        if (!snapshotOnly) {
            eds = super.visitDifferential(sd);
            if (eds != null && !eds.isEmpty()) {
                getBindings(sdName, eds, sdURL, sdVersion, bindingObjects);
            }
            if (sd.hasBaseDefinition()) {
                bindingObjects.putAll(visitStructureDefinition(this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrlWithVersion(sd.getBaseDefinition()), snapshotOnly));            }
        }
        return bindingObjects;
    }

    public Map<String, StructureDefinitionBindingObject> visitCanonicalAtlasStructureDefinitions(boolean snapshotOnly) {
        Iterable<StructureDefinition> iterableStructureDefinitions = canonicalResourceAtlas.getStructureDefinitions().get();
        Map<String, StructureDefinition> sdMap = new HashMap<>();
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        iterableStructureDefinitions.forEach((structureDefinition) -> {
            Map<String, StructureDefinitionBindingObject> newBindingObjects = visitStructureDefinition(structureDefinition, snapshotOnly);
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
                if(ed.hasMin() && ed.hasMin()){
                    String edCardinality = ed.getMin() + "..." + ed.getMax();
                    sdbo.setCardinality(edCardinality);
                }
                String bindingValueSet = ed.getBinding().getValueSet();
                String pipeVersion = "";
                if (bindingValueSet.contains("|")) {
                    pipeVersion = bindingValueSet.substring(bindingValueSet.indexOf("|") + 1);
                    bindingValueSet = bindingValueSet.substring(0, bindingValueSet.indexOf("|"));
                }
                sdbo.setBindingValueSetURL(bindingValueSet);
                sdbo.setElementId(ed.getId());
                String valueSetVersion = "";
                ValueSet elementValueSet = null;
                if (null != this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL())) {
                    valueSetVersion = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getVersion();
                    sdbo.setBindingValueSetName(this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getName());
                    elementValueSet = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL());
                } else if (null != this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL())) {
                    valueSetVersion = this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getVersion();
                    sdbo.setBindingValueSetName(this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL()).getName());
                    elementValueSet = this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.getBindingValueSetURL());
                } else if (valueSetVersion.length() < 1 && bindingValueSet.contains("|")) {
                    valueSetVersion = pipeVersion;
                }
                if (null != elementValueSet) {
                    StringBuilder codeSystemURLs = new StringBuilder();;
                    Map<String, String> codeSystemsMap = new HashMap<>();
                    getValueSetCodeSystems(elementValueSet, codeSystemsMap);
                    if(null != codeSystemsMap && !codeSystemsMap.isEmpty()) {
                        AtomicReference <Integer> valueCount = new AtomicReference<>(0);
                        codeSystemsMap.values().forEach((url)->{
                            codeSystemURLs.append(url);
                            valueCount.set(valueCount.get() + 1);
                            if(valueCount.get() > 0 &&
                                valueCount.get() < codeSystemsMap.size()) {
                                codeSystemURLs.append(";");
                            }
                        });
                        sdbo.setCodeSystemsURLs(codeSystemURLs.toString());
                    }
                }
                if (ed.getMustSupport()) {
                    sdbo.setMustSupport("Y");
                } else {
                    sdbo.setMustSupport("N");
                }
                sdbo.setBindingValueSetVersion(valueSetVersion);
                bindingObjects.put(sdName + "." + sdbo.getElementId(), sdbo);
            }
            index.set(index.get() + 1);
        }
    }

    private void getValueSetCodeSystems(ValueSet elementValueSet, Map<String, String> codeSystemsMap) {
        ValueSet.ValueSetComposeComponent compose = elementValueSet.getCompose();
        if (null != compose) {
            for (ValueSet.ConceptSetComponent include : compose.getInclude()) {
                if(include.hasSystem()){
                    codeSystemsMap.put(include.getSystem(), include.getSystem());
                }
                for (CanonicalType r : include.getValueSet()) {
                    ValueSet svs = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(r.getValue());
                    if (null == svs) {
                        svs = this.canonicalResourceDependenciesAtlas.getValueSets().getByCanonicalUrlWithVersion(r.getValue());
                    }
                    if (null != svs) {
                        getValueSetCodeSystems(svs, codeSystemsMap);
                    }
                }
            }
        }
    }
}
