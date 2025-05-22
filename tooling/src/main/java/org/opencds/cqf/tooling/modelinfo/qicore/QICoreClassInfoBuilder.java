package org.opencds.cqf.tooling.modelinfo.qicore;


import java.util.Map;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.tooling.modelinfo.ClassInfoBuilder;

public class QICoreClassInfoBuilder extends ClassInfoBuilder {

    public QICoreClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new QICoreClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        /*
        Need modelVersion as an input, or switch to using modelInfo extensions...
        this.buildFor("QICore", "qicore-adverseevent");
        this.buildFor("QICore", "qicore-allergyintolerance");
        this.buildFor("QICore", "qicore-bodystructure");
        this.buildFor("QICore", "qicore-claim");
        this.buildFor("QICore", "qicore-claimresponse");
        this.buildFor("QICore", "qicore-careplan");
        this.buildFor("QICore", "qicore-careteam");
        this.buildFor("QICore", "qicore-communication");
        this.buildFor("QICore", "qicore-communicationnotdone");
        this.buildFor("QICore", "qicore-communicationrequest");
        //this.buildFor("QICore", "qicore-condition"); // v4.1.1
        this.buildFor("QICore", "qicore-condition-encounter-diagnosis"); // v5.0.0
        this.buildFor("QICore", "qicore-condition-problems-health-concerns"); // v5.0.0
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
        //this.buildFor("QICore", "qicore-immunizationrec"); // v4.1.1
        this.buildFor("QICore", "qicore-immunizationrecommendation"); // v5.0.0
        this.buildFor("QICore", "us-core-implantable-device");
        this.buildFor("QICore", "qicore-location");
        this.buildFor("QICore", "qicore-medication");
        this.buildFor("QICore", "qicore-medicationadministration");
        // this.buildFor("QICore", "qicore-mednotadministered"); // v4.1.1
        this.buildFor("QICore", "qicore-medicationadministrationnotdone"); // v5.0.0
        this.buildFor("QICore", "qicore-medicationdispense");
        //this.buildFor("QICore", "qicore-mednotdispensed");  // v4.1.1
        this.buildFor("QICore", "qicore-medicationdispensedeclined"); // v5.0.0
        //this.buildFor("QICore", "qicore-mednotrequested"); // v4.1.1
        this.buildFor("QICore", "qicore-medicationnotrequested"); // v5.0.0
        this.buildFor("QICore", "qicore-medicationrequest");
        this.buildFor("QICore", "qicore-medicationstatement");
        this.buildFor("QICore", "qicore-observation");
        //this.buildFor("QICore", "qicore-observationnotdone"); // v4.1.1
        this.buildFor("QICore", "qicore-observationcancelled"); // v5.0.0
        //this.buildFor("QICore", "vitalspanel"); // v4.1.1
        this.buildFor("QICore", "us-core-vital-signs"); // v5.0.0

        //this.buildFor("QICore", "resprate"); // v4.1.1
        this.buildFor("QICore", "us-core-respiratory-rate"); // v5.0.0
        //this.buildFor("QICore", "heartrate"); // v4.1.1
        this.buildFor("QICore", "us-core-heart-rate");
        this.buildFor("QICore", "head-occipital-frontal-circumference-percentile"); // v5.0.0
        //this.buildFor("QICore", "oxygensat"); // v4.1.1
        this.buildFor("QICore", "us-core-oxygen-saturation");
        //this.buildFor("QICore", "bodytemp"); // v4.1.1
        this.buildFor("QICore", "us-core-body-temperature");
        //this.buildFor("QICore", "bodyheight"); // v4.1.1
        this.buildFor("QICore", "us-core-body-height");
        //this.buildFor("QICore", "headcircum");
        this.buildFor("QICore", "us-core-head-circumference"); // v5.0.0
        //this.buildFor("QICore", "bodyweight"); // v4.1.1
        this.buildFor("QICore", "us-core-body-weight");
        //this.buildFor("QICore", "bmi"); // v4.1.1
        this.buildFor("QICore", "us-core-bmi"); // v5.0.0
        //this.buildFor("QICore", "bp"); // v4.1.1
        this.buildFor("QICore", "us-core-blood-pressure"); // v5.0.0
        this.buildFor("QICore", "us-core-smokingstatus");
        this.buildFor("QICore", "us-core-pulse-oximetry");
        //this.buildFor("QICore", "us-core-observation-lab"); // v4.1.1
        this.buildFor("QICore", "qicore-observation-lab"); // v5.0.0
        this.buildFor("QICore", "qicore-observation-clinical-test"); // v5.0.0
        this.buildFor("QICore", "qicore-observation-imaging"); // v5.0.0
        this.buildFor("QICore", "qicore-observation-survey"); // v5.0.0
        this.buildFor("QICore", "pediatric-bmi-for-age");
        this.buildFor("QICore", "pediatric-weight-for-height");
        this.buildFor("QICore", "us-core-observation-sexual-orientation"); // v5.0.0
        this.buildFor("QICore", "us-core-observation-social-history"); // v5.0.0
        this.buildFor("QICore", "us-core-observation-sdoh-assessment"); // v5.0.0
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
        //this.buildFor("QICore", "qicore-tasknotdone"); // v4.1.1
        this.buildFor("QICore", "qicore-taskrejected"); // v5.0.0
        this.buildFor("QICore", "Questionnaire");
        //this.buildFor("QICore", "QuestionnaireResponse"); // v4.1.1
        this.buildFor("QICore", "qicore-questionnaireresponse"); // v5.0.0
        */

        /*
        // v6.0.0
        this.buildFor("QICore", "qicore-adverseevent");
        this.buildFor("QICore", "qicore-allergyintolerance");
        this.buildFor("QICore", "qicore-bodystructure");
        this.buildFor("QICore", "qicore-claim");
        this.buildFor("QICore", "qicore-claimresponse");
        this.buildFor("QICore", "qicore-careplan");
        this.buildFor("QICore", "qicore-careteam");
        this.buildFor("QICore", "qicore-communication");
        this.buildFor("QICore", "qicore-communicationnotdone");
        this.buildFor("QICore", "qicore-communicationrequest");
        this.buildFor("QICore", "qicore-condition-encounter-diagnosis");
        this.buildFor("QICore", "qicore-condition-problems-health-concerns");
        this.buildFor("QICore", "qicore-coverage");
        this.buildFor("QICore", "qicore-device");
        this.buildFor("QICore", "us-core-implantable-device");
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
        this.buildFor("QICore", "qicore-immunizationrecommendation");
        this.buildFor("QICore", "qicore-location");
        this.buildFor("QICore", "qicore-medication");
        this.buildFor("QICore", "qicore-medicationadministration");
        this.buildFor("QICore", "qicore-medicationadministrationnotdone");
        this.buildFor("QICore", "qicore-medicationdispense");
        this.buildFor("QICore", "qicore-medicationdispensedeclined");
        this.buildFor("QICore", "qicore-medicationnotrequested");
        this.buildFor("QICore", "qicore-medicationrequest");
        this.buildFor("QICore", "qicore-medicationstatement");
        this.buildFor("QICore", "qicore-nutritionorder");
        this.buildFor("QICore", "qicore-simple-observation");
        this.buildFor("QICore", "qicore-observationcancelled");
        this.buildFor("QICore", "qicore-nonpatient-observation");
        this.buildFor("QICore", "qicore-observation-lab");
        this.buildFor("QICore", "qicore-observation-clinical-result");
        this.buildFor("QICore", "qicore-observation-screening-assessment");

        this.buildFor("QICore", "us-core-vital-signs");
        this.buildFor("QICore", "us-core-blood-pressure");
        this.buildFor("QICore", "us-core-bmi");
        this.buildFor("QICore", "us-core-body-height");
        this.buildFor("QICore", "us-core-body-temperature");
        this.buildFor("QICore", "us-core-body-weight");
        this.buildFor("QICore", "us-core-head-circumference");
        this.buildFor("QICore", "us-core-heart-rate");
        this.buildFor("QICore", "pediatric-bmi-for-age");
        this.buildFor("QICore", "head-occipital-frontal-circumference-percentile");
        this.buildFor("QICore", "pediatric-weight-for-height");
        this.buildFor("QICore", "us-core-pulse-oximetry");
        this.buildFor("QICore", "us-core-respiratory-rate");
        this.buildFor("QICore", "us-core-smokingstatus");
        this.buildFor("QICore", "us-core-observation-occupation");
        this.buildFor("QICore", "us-core-observation-sexual-orientation");
        this.buildFor("QICore", "us-core-observation-pregnancyintent");
        this.buildFor("QICore", "us-core-observation-pregnancystatus");

        this.buildFor("QICore", "qicore-organization");
        this.buildFor("QICore", "qicore-patient");
        this.buildFor("QICore", "qicore-practitioner");
        this.buildFor("QICore", "qicore-practitionerrole");
        this.buildFor("QICore", "qicore-procedure");
        this.buildFor("QICore", "qicore-procedurenotdone");
        this.buildFor("QICore", "qicore-questionnaireresponse");
        this.buildFor("QICore", "qicore-relatedperson");
        this.buildFor("QICore", "qicore-servicerequest");
        this.buildFor("QICore", "qicore-servicenotrequested");
        this.buildFor("QICore", "us-core-specimen");
        this.buildFor("QICore", "qicore-substance");
        this.buildFor("QICore", "qicore-task");
        this.buildFor("QICore", "qicore-taskrejected");
        this.buildFor("QICore", "Questionnaire");
        */

        // v7.0.0
        this.buildFor("QICore", "qicore-adverseevent");
        this.buildFor("QICore", "qicore-allergyintolerance");
        this.buildFor("QICore", "qicore-bodystructure");
        this.buildFor("QICore", "qicore-claim");
        this.buildFor("QICore", "qicore-claimresponse");
        this.buildFor("QICore", "qicore-careplan");
        this.buildFor("QICore", "qicore-careteam");
        this.buildFor("QICore", "qicore-communication");
        this.buildFor("QICore", "qicore-communicationdone");
        this.buildFor("QICore", "qicore-communicationnotdone");
        this.buildFor("QICore", "qicore-communicationrequest");
        this.buildFor("QICore", "qicore-condition-encounter-diagnosis");
        this.buildFor("QICore", "qicore-condition-problems-health-concerns");
        this.buildFor("QICore", "qicore-coverage");
        this.buildFor("QICore", "qicore-device");
        this.buildFor("QICore", "qicore-devicerequest");
        this.buildFor("QICore", "qicore-devicerequested");
        this.buildFor("QICore", "qicore-deviceprohibited");
        this.buildFor("QICore", "qicore-deviceusestatement");
        this.buildFor("QICore", "qicore-diagnosticreport-lab");
        this.buildFor("QICore", "qicore-diagnosticreport-note");
        this.buildFor("QICore", "qicore-encounter");
        this.buildFor("QICore", "qicore-familymemberhistory");
        this.buildFor("QICore", "qicore-flag");
        this.buildFor("QICore", "qicore-goal");
        this.buildFor("QICore", "qicore-imagingstudy");
        this.buildFor("QICore", "qicore-immunization");
        this.buildFor("QICore", "qicore-immunizationdone");
        this.buildFor("QICore", "qicore-immunizationnotdone");
        this.buildFor("QICore", "qicore-immunizationevaluation");
        this.buildFor("QICore", "qicore-immunizationrecommendation");
        this.buildFor("QICore", "qicore-location");
        this.buildFor("QICore", "qicore-medication");
        this.buildFor("QICore", "qicore-medicationadministration");
        this.buildFor("QICore", "qicore-medicationadministrationdone");
        this.buildFor("QICore", "qicore-medicationadministrationnotdone");
        this.buildFor("QICore", "qicore-medicationdispense");
        this.buildFor("QICore", "qicore-medicationdispensedone");
        this.buildFor("QICore", "qicore-medicationdispensedeclined");
        this.buildFor("QICore", "qicore-medicationrequest");
        this.buildFor("QICore", "qicore-medicationrequested");
        this.buildFor("QICore", "qicore-medicationprohibited");
        this.buildFor("QICore", "qicore-medicationstatement");
        this.buildFor("QICore", "qicore-nutritionorder");
        this.buildFor("QICore", "qicore-simple-observation");
        this.buildFor("QICore", "qicore-nonpatient-observation");
        this.buildFor("QICore", "qicore-observation-lab");
        this.buildFor("QICore", "qicore-observation-clinical-result");
        this.buildFor("QICore", "qicore-observation-screening-assessment");
        this.buildFor("QICore", "qicore-organization");
        this.buildFor("QICore", "qicore-patient");
        this.buildFor("QICore", "qicore-practitioner");
        this.buildFor("QICore", "qicore-practitionerrole");
        this.buildFor("QICore", "qicore-procedure");
        this.buildFor("QICore", "qicore-proceduredone");
        this.buildFor("QICore", "qicore-procedurenotdone");
        this.buildFor("QICore", "qicore-questionnaireresponse");
        this.buildFor("QICore", "qicore-relatedperson");
        this.buildFor("QICore", "qicore-servicerequest");
        this.buildFor("QICore", "qicore-servicerequested");
        this.buildFor("QICore", "qicore-serviceprohibited");
        this.buildFor("QICore", "qicore-substance");
        this.buildFor("QICore", "qicore-task");
        this.buildFor("QICore", "qicore-taskdone");
        this.buildFor("QICore", "qicore-taskrejected");
    }
}