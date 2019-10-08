package org.opencds.cqf.modelinfo.fhir;

import java.util.Map;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r4.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;

public class FHIRClassInfoBuilder extends ClassInfoBuilder {

    public FHIRClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new FHIRClassInfoSettings(), structureDefinitions);
    }
    
	@Override
    protected void innerBuild() {
        System.out.println("Building Primitives");
        this.buildFor("FHIR", (x -> x.getKind() == StructureDefinitionKind.PRIMITIVETYPE));

        System.out.println("Building ComplexTypes");
        this.buildFor("FHIR",
        (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))));

        System.out.println("Building Resources");
        this.buildFor("FHIR", (x -> x.getKind() == StructureDefinitionKind.RESOURCE
            && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.SPECIALIZATION)));
    }
}