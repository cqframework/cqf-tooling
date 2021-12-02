package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class StructureDefinitionElementBindingVisitor extends StructureDefinitionBaseVisitor {
    private FhirContext fc;
    private CanonicalResourceAtlas canonicalResourceAtlas;


    public StructureDefinitionElementBindingVisitor(CanonicalResourceAtlas atlas){
        fc = FhirContext.forCached(FhirVersionEnum.R4);
        this.canonicalResourceAtlas = atlas;
    }
    public Map<String, StructureDefinitionBindingObject>  visitStructureDefinition(StructureDefinition sd){
        if (sd == null) {
            throw new IllegalArgumentException("sd required");
        }
        if (sd.getType() == null) {
            throw new IllegalArgumentException("type required");
        }
        Resource r = (Resource)fc.getResourceDefinition(sd.getType()).newInstance();
        String sdURL = sd.getUrl();
        String sdVersion = sd.getVersion();
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        List<ElementDefinition> eds = super.visitSnapshot(sd);
        if(eds != null && !eds.isEmpty()){
            getBindings(eds, sdURL, sdVersion, bindingObjects);
         }
        eds = super.visitDifferential(sd);
        if(eds != null && !eds.isEmpty()){
            getBindings(eds, sdURL, sdVersion, bindingObjects);
        }
        return bindingObjects;
    }

    private void getBindings(List<ElementDefinition> eds, String sdURL, String sdVersion, Map<String, StructureDefinitionBindingObject> bindingObjects){
        AtomicReference<Integer> index = new AtomicReference<Integer>(1);
        while(index.get() < eds.size()){
            ElementDefinition ed = eds.get(index.get());
            if(ed.hasBinding()){
                StructureDefinitionBindingObject sdbo = new StructureDefinitionBindingObject();
                sdbo.sdURL = sdURL;
                sdbo.sdVersion = sdVersion;
                sdbo.bindingStrength= ed.getBinding().getStrength().toString();
                sdbo.bindingValueSetURL = ed.getBinding().getValueSet();
                sdbo.elementPath = ed.getPath();
                sdbo.bindingValueSetVersion = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.bindingValueSetURL).getVersion();
                bindingObjects.put(sdURL, sdbo);
            }
            index.set(index.get() + 1);
        }

    }
    protected void visitDifferential(){}
}
