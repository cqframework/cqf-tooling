package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class StructureDefinitionElementVisitor extends StructureDefinitionBaseVisitor {
    private FhirContext fc;
    private CanonicalResourceAtlas canonicalResourceAtlas;
    private CanonicalResourceAtlas canonicalResourceDependenciesAtlas;


    public StructureDefinitionElementVisitor(CanonicalResourceAtlas atlas, CanonicalResourceAtlas dependencyAtlas) {
        fc = FhirContext.forCached(FhirVersionEnum.R4);
        this.canonicalResourceAtlas = atlas;
        this.canonicalResourceDependenciesAtlas = dependencyAtlas;
    }

    public Map<String, StructureDefinitionElementObject> visitStructureDefinition(StructureDefinition sd, boolean snapshotOnly) {
        if (sd == null) {
            throw new IllegalArgumentException("sd required");
        }
        if (sd.getType() == null) {
            throw new IllegalArgumentException("type required");
        }
        String sdURL = sd.getUrl();
        String sdVersion = sd.getVersion();
        String sdName = sd.getName();
        Map<String, StructureDefinitionElementObject> elementObjectsMap = new HashMap<>();
        List<ElementDefinition> eds = super.visitSnapshot(sd);
        if (eds != null && !eds.isEmpty()) {
            getElements(sdName, eds, sdURL, sdVersion, elementObjectsMap);
        }
        if (!snapshotOnly) {
            eds = super.visitDifferential(sd);
            if (eds != null && !eds.isEmpty()) {
                getElements(sdName, eds, sdURL, sdVersion, elementObjectsMap);
            }
            if (sd.hasBaseDefinition()) {
                elementObjectsMap.putAll(visitStructureDefinition(this.canonicalResourceDependenciesAtlas.getStructureDefinitions().getByCanonicalUrlWithVersion(sd.getBaseDefinition()), snapshotOnly));
            }
        }
        return elementObjectsMap;
    }

    public Map<String, StructureDefinitionElementObject> visitCanonicalAtlasStructureDefinitions(boolean snapshotOnly) {
        Iterable<StructureDefinition> iterableStructureDefinitions = canonicalResourceAtlas.getStructureDefinitions().get();
        Map<String, StructureDefinition> sdMap = new HashMap<>();
        Map<String, StructureDefinitionElementObject> elementObjects = new HashMap<>();
        iterableStructureDefinitions.forEach((structureDefinition) -> {
            Map<String, StructureDefinitionElementObject> newElementObjects = visitStructureDefinition(structureDefinition, snapshotOnly);
            if (null != newElementObjects) {
                elementObjects.putAll(newElementObjects);
            }
        });
        return elementObjects;
    }

    private void getElements(String sdName, List<ElementDefinition> eds, String sdURL, String sdVersion, Map<String, StructureDefinitionElementObject> elementObjects) {
        AtomicReference<Integer> index = new AtomicReference<Integer>(0);
        while (index.get() < eds.size()) {
            ElementDefinition ed = eds.get(index.get());
            StructureDefinitionElementObject sdeo = new StructureDefinitionElementObject();
            sdeo.setSdName(sdName);
            sdeo.setSdURL(sdURL);
            sdeo.setSdVersion(sdVersion);
            if (ed.hasMin() && ed.hasMax()) {
                String edCardinality = ed.getMin() + "..." + ed.getMax();
                sdeo.setCardinality(edCardinality);
            }
            if(ed.getType() != null && ed.getType().size() > 0 && ed.getType().get(0).getCode() != null) {
                String elementType = ed.getType().get(0).getCode();
                if(elementType.equalsIgnoreCase("http://hl7.org/fhirpath/System.String")){
                    sdeo.setElementType("System.String");
                } else {
                    sdeo.setElementType(elementType);
                }
            }
            if(ed.getShort() != null && ed.getShort().length() > 0) {
                sdeo.setElementDescription(ed.getShort());
            }
            if (ed.getConstraint() != null && ed.getConstraint().size() > 0) {
                StringBuilder combinedConstraints = new StringBuilder();
                ed.getConstraint().forEach(constraint -> {
                    combinedConstraints.append(constraint.getHuman() + ";\n");
                });
                combinedConstraints.replace(combinedConstraints.lastIndexOf(";\n"), combinedConstraints.length(), "");
                sdeo.setConstraint(combinedConstraints.toString());
            }
            sdeo.setElementId(ed.getId());
            if (ed.getMustSupport()) {
                sdeo.setMustSupport("Y");
            } else {
                sdeo.setMustSupport("N");
            }
            elementObjects.put(sdName + "." + sdeo.getElementId(), sdeo);

            index.set(index.get() + 1);
        }
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
