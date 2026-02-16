package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.*;

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
                bindingObjects.putAll(visitStructureDefinition(this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrlWithVersion(sd.getBaseDefinition()), snapshotOnly));
            }
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
                if (ed.hasMin() && ed.hasMax()) {
                    String edCardinality = ed.getMin() + "..." + ed.getMax();
                    sdbo.setCardinality(edCardinality);
                    if(ed.getMin() > 0){sdbo.setCardinalityMin(ed.getMin());}
                }
                if(getBindingObjectExtension(ed).equalsIgnoreCase("qicore-keyelement")){
                    sdbo.setBindingObjectExtension("qicore-keyelement");
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
                    StringBuilder codeSystemURLs = new StringBuilder();
                    Map<String, String> codeSystemsMap = new HashMap<>();
                    getValueSetCodeSystems(elementValueSet, codeSystemsMap);
                    if (null != codeSystemsMap && !codeSystemsMap.isEmpty()) {
                        AtomicReference<Integer> valueCount = new AtomicReference<>(0);
                        codeSystemsMap.values().forEach((url) -> {
                            codeSystemURLs.append(url);
                            valueCount.set(valueCount.get() + 1);
                            if (valueCount.get() > 0 &&
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
            else if (ed.hasExtension()) {
                visitExtensions(ed, bindingObjects, sdName, sdURL, sdVersion);
            }
            index.set(index.get() + 1);
        }
    }

    private String getBindingObjectExtension(ElementDefinition ed) {
        for (Extension ext : ed.getExtension()) {
            if (!ext.getUrl().isEmpty()) {
                if (ext.getUrl().contains("qicore-keyelement")) {
                    return "qicore-keyelement";
                }
            }
        }
        return "";
    }

    private void visitExtensions(ElementDefinition ed, Map<String, StructureDefinitionBindingObject> bindingObjects, String sdName, String sdURL, String sdVersion) {
        StructureDefinitionBindingObject sdbo = new StructureDefinitionBindingObject();
        sdbo.setSdName(sdName);
        sdbo.setSdURL(sdURL);
        sdbo.setSdVersion(sdVersion);
        sdbo.setElementId(ed.getId());
        if (ed.getMustSupport()) {
            sdbo.setMustSupport("Y");
        } else {
            sdbo.setMustSupport("N");
        }
        if (ed.hasMin()) {
            String edCardinality = ed.getMin() + "..." + ed.getMax();
            sdbo.setCardinality(edCardinality);
        }
        CanonicalType canonicalType = new CanonicalType();
        Iterable<StructureDefinition> sdList = null;
        try {
            if (ed.getType().get(0).getProfile().size() != 0) {
                canonicalType.setValue(String.valueOf(ed.getType().get(0).getProfile().get(0)));
                sdList = this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrl(ed.getType().get(0).getProfile().get(0).getValueAsString());
            } else if (ed.getType().get(0).getTargetProfile().size() != 0) {
                canonicalType.setValue(String.valueOf(ed.getType().get(0).getTargetProfile()));
                sdList = this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrl(ed.getType().get(0).getTargetProfile().get(0).getValueAsString());
            }
            else{
                return;
            }
        } catch (Exception ex) {
            return;
        }
        if (sdList != null) {
            sdList.forEach((structDef) -> {
                List<ElementDefinition> edsds = structDef.getDifferential().getElement();
                edsds.forEach(edsd -> {
                    if (edsd.hasBinding()) {
                        sdbo.setBindingStrength(edsd.getBinding().getStrength().toString().toLowerCase());
                        String bindingValueSet = edsd.getBinding().getValueSet();
                        String pipeVersion = "";
                        if (bindingValueSet.contains("|")) {
                            pipeVersion = bindingValueSet.substring(bindingValueSet.indexOf("|") + 1);
                            bindingValueSet = bindingValueSet.substring(0, bindingValueSet.indexOf("|"));
                        }
                        sdbo.setBindingValueSetURL(bindingValueSet);
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
                        } else if (valueSetVersion.isEmpty() && bindingValueSet.contains("|")) {
                            valueSetVersion = pipeVersion;
                        }
                        sdbo.setBindingValueSetVersion(valueSetVersion);
                        if (null != elementValueSet) {
                            StringBuilder codeSystemURLs = new StringBuilder();
                            Map<String, String> codeSystemsMap = new HashMap<>();
                            getValueSetCodeSystems(elementValueSet, codeSystemsMap);
                            if (!codeSystemsMap.isEmpty()) {
                                AtomicReference<Integer> valueCount = new AtomicReference<>(0);
                                codeSystemsMap.values().forEach((url) -> {
                                    codeSystemURLs.append(url);
                                    valueCount.set(valueCount.get() + 1);
                                    if (valueCount.get() > 0 &&
                                            valueCount.get() < codeSystemsMap.size()) {
                                        codeSystemURLs.append(";");
                                    }
                                });
                                sdbo.setCodeSystemsURLs(codeSystemURLs.toString());
                            }
                        }
                    }
                });
            });
        }
        bindingObjects.put(sdName + "." + sdbo.getElementId(), sdbo);
    }

    private void getValueSetCodeSystems(ValueSet elementValueSet, Map<String, String> codeSystemsMap) {
        ValueSet.ValueSetComposeComponent compose = elementValueSet.getCompose();
        if (null != compose) {
            for (ValueSet.ConceptSetComponent include : compose.getInclude()) {
                if (include.hasSystem()) {
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
