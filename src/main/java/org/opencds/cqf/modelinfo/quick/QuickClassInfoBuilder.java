package org.opencds.cqf.modelinfo.quick;

import java.util.Map;

import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.dstu3.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;

public class QuickClassInfoBuilder extends ClassInfoBuilder {

    public QuickClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new QuickClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        System.out.println("Building ComplexTypes");
        this.buildFor("QUICK",
            (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                    || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))
                    && !this.settings.cqlTypeMappings.containsKey(this.unQualify(x.getName())))
            );

        //delete fhir profile (duplicates)
        //filter out all that is FHIR Resource this.structureDefinitions
        System.out.println("Building Base FHIR Resources");
        this.buildFor("QUICK", 
            (x -> x.getKind() == StructureDefinitionKind.RESOURCE
            && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.SPECIALIZATION)
                && x.getUrl().startsWith("http://hl7.org/fhir") && !x.getUrl().startsWith("http://hl7.org/fhir/us/qicore")
                && !x.getUrl().startsWith("http://hl7.org/fhir/us/core"))
        );

        System.out.println("Building Quick Extension Resources");
        this.buildFor("QUICK", 
            (x -> x.getKind() == StructureDefinitionKind.RESOURCE 
                && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.CONSTRAINT)
                && (x.getUrl().startsWith("http://hl7.org/fhir/us/qicore") || x.getUrl().startsWith("http://hl7.org/fhir/us/core")))
        );
    }
}