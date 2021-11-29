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

public class StructureDefinitionBaseVisitor {
    private FhirContext fc;
    private CanonicalResourceAtlas atlas;


    public StructureDefinitionBaseVisitor(CanonicalResourceAtlas atlas){
        fc = FhirContext.forCached(FhirVersionEnum.R4);
        this.atlas = atlas;
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
        StructureDefinition.StructureDefinitionSnapshotComponent snapshot = sd.getSnapshot();
        if(snapshot != null && snapshot.hasElement()){
            List<ElementDefinition> eds = snapshot.getElement();
            getBindings(eds, sdURL, sdVersion, bindingObjects);
         }
        StructureDefinition.StructureDefinitionDifferentialComponent sddc = sd.getDifferential();
        if(sddc != null && sddc.hasElement()){
            List<ElementDefinition> eds = sddc.getElement();
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
                sdbo.bindingValueSetVersion = this.atlas.getValueSets().getByCanonicalUrlWithVersion(sdbo.bindingValueSetURL).getVersion();
                bindingObjects.put(sdURL, sdbo);
            }
            index.set(index.get() + 1);
        }

    }
}
