package org.opencds.cqf.modelinfo.qicore;


import java.util.Collection;
import java.util.Map;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r4.model.StructureDefinition.TypeDerivationRule;
import org.opencds.cqf.modelinfo.ClassInfoBuilder;

public class QICoreClassInfoBuilder extends ClassInfoBuilder {

    public QICoreClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new QICoreClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        this.buildFor("QICore", "qicore-adverseevent");
        this.buildFor("QICore", "qicore-allergyintolerance");
        this.buildFor("QICore", "qicore-bodystructure");
        this.buildFor("QICore", "qicore-claim");
        this.buildFor("QICore", "qicore-careplan");
        this.buildFor("QICore", "qicore-careteam");
        this.buildFor("QICore", "qicore-communication");
        this.buildFor("QICore", "qicore-communicationnotdone");
        this.buildFor("QICore", "qicore-communicationrequest");
        this.buildFor("QICore", "qicore-condition");
        this.buildFor("QICore", "qicore-coverage");
        this.buildFor("QICore", "qicore-device");
        this.buildFor("QICore", "qicore-devicenotrequested");
        this.buildFor("QICore", "qicore-devicerequest");
        this.buildFor("QICore", "qicore-deviceusestatement");
        this.buildFor("QICore", "qicore-diagnosticreport-lab");
        this.buildFor("QICore", "qicore-diagnosticreport-note");
        this.buildFor("QICore", "qicore-encounter");
        this.buildFor("QICore", "qicore-familymemberhistory");
        this.buildFor("QICore", "qicore-flag");
        this.buildFor("QICore", "qicore-goal");
        this.buildFor("QICore", "qicore-imagingstudy");
        this.buildFor("QICore", "qicore-immunization");
        this.buildFor("QICore", "qicore-immunizationevaluation");
        this.buildFor("QICore", "qicore-immunizationnotdone");
        this.buildFor("QICore", "qicore-immunizationrec");
        this.buildFor("QICore", "us-core-implantable-device");
        this.buildFor("QICore", "qicore-location");
        this.buildFor("QICore", "qicore-medication");
        this.buildFor("QICore", "qicore-medicationadministration");
        this.buildFor("QICore", "qicore-mednotadministered");
        this.buildFor("QICore", "qicore-medicationdispense");
        this.buildFor("QICore", "qicore-medicationnotdispensed");
        this.buildFor("QICore", "qicore-medicationnotrequested");
        this.buildFor("QICore", "qicore-medicationrequest");
        this.buildFor("QICore", "qicore-medicationstatement");
        this.buildFor("QICore", "qicore-observation");
        this.buildFor("QICore", "qicore-observationnotdone");
        this.buildFor("QICore", "qicore-patient");
        this.buildFor("QICore", "vitalspanel");
        this.buildFor("QICore", "resprate");
        this.buildFor("QICore", "heartrate");
        this.buildFor("QICore", "oxygensat");
        this.buildFor("QICore", "bodytemp");
        this.buildFor("QICore", "bodyheight");
        this.buildFor("QICore", "headcircum");
        this.buildFor("QICore", "bodyweight");
        this.buildFor("QICore", "bmi");
        this.buildFor("QICore", "bp");
        this.buildFor("QICore", "us-core-smokingstatus");
        this.buildFor("QICore", "us-core-pulse-oximetry");
        this.buildFor("QICore", "us-core-observation-lab");
        this.buildFor("QICore", "pediatric-bmi-for-age");
        this.buildFor("QICore", "pediatric-weight-for-height");
        this.buildFor("QICore", "qicore-organization");
        this.buildFor("QICore", "qicore-patient");
        this.buildFor("QICore", "qicore-practitioner");
        this.buildFor("QICore", "qicore-practitionerrole");
        this.buildFor("QICore", "qicore-procedure");
        this.buildFor("QICore", "qicore-procedurenotdone");
        this.buildFor("QICore", "qicore-relatedperson");
        this.buildFor("QICore", "qicore-servicerequest");
        this.buildFor("QICore", "qicore-servicenotrequested");
        this.buildFor("QICore", "qicore-specimen");
        this.buildFor("QICore", "qicore-substance");
        this.buildFor("QICore", "qicore-task");
        this.buildFor("QICore", "Questionnaire");
        this.buildFor("QICore", "QuestionnaireResponse");
    }
}