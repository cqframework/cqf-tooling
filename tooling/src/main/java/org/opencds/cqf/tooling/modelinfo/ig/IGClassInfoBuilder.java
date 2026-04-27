package org.opencds.cqf.tooling.modelinfo.ig;

import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.tooling.modelinfo.ClassInfoBuilder;

import java.util.Map;

public class IGClassInfoBuilder extends ClassInfoBuilder {

    protected final ImplementationGuide ig;

    public IGClassInfoBuilder(ImplementationGuide ig, Map<String, StructureDefinition> structureDefinitions) {
        super(new IGClassInfoSettings(), structureDefinitions);
        this.ig = ig;
    }

    @Override
    protected void innerBuild() {
        // TODO: Should also use the modelinfo settings here...
        // For each structure definition resource defined in the ig
        for (var resource : ig.getDefinition().getResource()) {
            if (resource.getReference().getReferenceElement().getResourceType().equals("StructureDefinition")) {
                this.buildFor(this.settings.modelName, resource.getReference().getReferenceElement().getIdPart());
            }
        }
    }
}
