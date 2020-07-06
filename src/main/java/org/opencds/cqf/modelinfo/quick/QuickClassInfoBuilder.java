package org.opencds.cqf.modelinfo.quick;


import java.util.Collection;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;

public class QuickClassInfoBuilder extends ClassInfoBuilder {

    public QuickClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new QuickClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        /*
        System.out.println("Building ComplexTypes");
        this.buildFor("QUICK",
            (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                    || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))
                    && !this.settings.cqlTypeMappings.containsKey("QUICK." + x.getName()))
            );
        
        System.out.println("Building Quick Resources");
        this.buildFor("QUICK", 
            (x -> x.getKind() == StructureDefinitionKind.RESOURCE 
                && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.CONSTRAINT)
                && (x.getUrl().startsWith("http://hl7.org/fhir/us/qicore")))
        );
        */

        //this.buildFor("QUICK", "Resource");
        //this.buildFor("QUICK", "DomainResource");
        this.buildFor("QUICK", "qicore-adverseevent");
        this.buildFor("QUICK", "qicore-patient");
        this.buildFor("QUICK", "qicore-allergyintolerance");
        this.buildFor("QUICK", "qicore-bodystructure");
        this.buildFor("QUICK", "qicore-claim");
        this.buildFor("QUICK", "qicore-communication");
        this.buildFor("QUICK", "qicore-communicationrequest");
        this.buildFor("QUICK", "qicore-condition");
        this.buildFor("QUICK", "qicore-coverage");
        this.buildFor("QUICK", "qicore-device");
        this.buildFor("QUICK", "qicore-deviceusestatement");
        this.buildFor("QUICK", "qicore-diagnosticreport-lab");
        this.buildFor("QUICK", "qicore-diagnosticreport-note");
        this.buildFor("QUICK", "qicore-encounter");
        this.buildFor("QUICK", "qicore-familymemberhistory");
        this.buildFor("QUICK", "qicore-flag");
        this.buildFor("QUICK", "qicore-goal");
        this.buildFor("QUICK", "qicore-imagingstudy");
        this.buildFor("QUICK", "qicore-immunization");
        this.buildFor("QUICK", "qicore-immunizationrec");
        this.buildFor("QUICK", "qicore-location");
        this.buildFor("QUICK", "qicore-medication");
        this.buildFor("QUICK", "qicore-medicationadministration");
        this.buildFor("QUICK", "qicore-medicationdispense");
        this.buildFor("QUICK", "qicore-medicationrequest");
        this.buildFor("QUICK", "qicore-medicationstatement");
        this.buildFor("QUICK", "qicore-observation");
        this.buildFor("QUICK", "vitalspanel");
        this.buildFor("QUICK", "resprate");
        this.buildFor("QUICK", "heartrate");
        this.buildFor("QUICK", "oxygensat");
        this.buildFor("QUICK", "bodytemp");
        this.buildFor("QUICK", "bodyheight");
        this.buildFor("QUICK", "headcircum");
        this.buildFor("QUICK", "bodyweight");
        this.buildFor("QUICK", "bmi");
        this.buildFor("QUICK", "bp");
        this.buildFor("QUICK", "us-core-smokingstatus");
        this.buildFor("QUICK", "us-core-observation-lab");
        this.buildFor("QUICK", "pediatric-bmi-for-age");
        this.buildFor("QUICK", "pediatric-weight-for-height");
        this.buildFor("QUICK", "qicore-organization");
        this.buildFor("QUICK", "qicore-patient");
        this.buildFor("QUICK", "qicore-practitioner");
        this.buildFor("QUICK", "qicore-practitionerrole");
        this.buildFor("QUICK", "qicore-procedure");
        this.buildFor("QUICK", "qicore-relatedperson");
        this.buildFor("QUICK", "qicore-servicerequest");
        this.buildFor("QUICK", "qicore-specimen");
        this.buildFor("QUICK", "qicore-substance");
        this.buildFor("QUICK", "qicore-task");
        this.buildFor("QUICK", "Questionnaire");
        this.buildFor("QUICK", "QuestionnaireResponse");
    }
}