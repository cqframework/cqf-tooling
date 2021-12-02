package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

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
            if(ed.hasBinding() && null != ed.getBinding().getValueSet()){
                StructureDefinitionBindingObject sdbo = new StructureDefinitionBindingObject();
                sdbo.sdURL = sdURL;
                sdbo.sdVersion = sdVersion;
                sdbo.bindingStrength= ed.getBinding().getStrength().toString();
                String bindingValueSet = ed.getBinding().getValueSet();
                String pipeVersion = "";
                if(bindingValueSet.contains("|")){
                    pipeVersion = bindingValueSet.substring(bindingValueSet.indexOf("|"));
                    bindingValueSet = bindingValueSet.substring(0, bindingValueSet.indexOf("|"));
                }
                sdbo.bindingValueSetURL = bindingValueSet;
                sdbo.elementPath = ed.getPath();
                String valueSetVersion = "";
                if (null != this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.bindingValueSetURL)){
                    valueSetVersion = this.canonicalResourceAtlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.bindingValueSetURL).getVersion();
                } else if(valueSetVersion.length() < 1 && bindingValueSet.contains("|")){
                    valueSetVersion = bindingValueSet.substring(bindingValueSet.indexOf("|"));
                }
                sdbo.bindingValueSetVersion = valueSetVersion;
                bindingObjects.put(sdbo.elementPath, sdbo);
            }
            index.set(index.get() + 1);
        }

    }
    protected void visitDifferential(){}
}
