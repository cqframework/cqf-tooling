package org.opencds.cqf.tooling.modelinfo.uscore;


import java.util.Map;

import org.hl7.elm_modelinfo.r1.TypeSpecifier;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.tooling.modelinfo.ClassInfoBuilder;

public class USCoreClassInfoBuilder extends ClassInfoBuilder {

    public USCoreClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new USCoreClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        // TODO: Need a modelVersion here, or drive this entirely off modelInfo-isIncluded extension...
        /*
        USCore 3.1.1 build...
        this.buildFor("USCore", "us-core-patient");
        this.buildFor("USCore", "us-core-allergyintolerance");
        this.buildFor("USCore", "us-core-careplan");
        this.buildFor("USCore", "us-core-careteam");
        this.buildFor("USCore", "us-core-condition");
        this.buildFor("USCore", "us-core-diagnosticreport-lab");
        this.buildFor("USCore", "us-core-diagnosticreport-note");
        this.buildFor("USCore", "us-core-documentreference");
        this.buildFor("USCore", "us-core-encounter");
        this.buildFor("USCore", "us-core-goal");
        this.buildFor("USCore", "us-core-immunization");
        this.buildFor("USCore", "us-core-implantable-device");
        this.buildFor("USCore", "us-core-observation-lab");
        this.buildFor("USCore", "us-core-location");
        this.buildFor("USCore", "us-core-medication");
        this.buildFor("USCore", "us-core-medicationrequest");
        this.buildFor("USCore", "us-core-organization");
        this.buildFor("USCore", "vitalspanel");
        this.buildFor("USCore", "resprate");
        this.buildFor("USCore", "heartrate");
        this.buildFor("USCore", "oxygensat");
        this.buildFor("USCore", "bodytemp");
        this.buildFor("USCore", "bodyheight");
        this.buildFor("USCore", "headcircum");
        this.buildFor("USCore", "bodyweight");
        this.buildFor("USCore", "bmi");
        this.buildFor("USCore", "bp");
        this.buildFor("USCore", "us-core-smokingstatus");
        this.buildFor("USCore", "us-core-pulse-oximetry");
        this.buildFor("USCore", "pediatric-bmi-for-age");
        this.buildFor("USCore", "pediatric-weight-for-height");
        this.buildFor("USCore", "us-core-practitioner");
        this.buildFor("USCore", "us-core-practitionerrole");
        this.buildFor("USCore", "us-core-procedure");
        this.buildFor("USCore", "us-core-provenance");
        */

        /*
        // 6.1.0
        this.buildFor("USCore", "us-core-allergyintolerance");
        this.buildFor("USCore", "us-core-careplan");
        this.buildFor("USCore", "us-core-careteam");
        this.buildFor("USCore", "us-core-condition-encounter-diagnosis");
        this.buildFor("USCore", "us-core-condition-problems-health-concerns");
        this.buildFor("USCore", "us-core-coverage");
        this.buildFor("USCore", "us-core-implantable-device");
        this.buildFor("USCore", "us-core-diagnosticreport-lab");
        this.buildFor("USCore", "us-core-diagnosticreport-note");
        this.buildFor("USCore", "us-core-documentreference");
        this.buildFor("USCore", "us-core-encounter");
        this.buildFor("USCore", "us-core-goal");
        this.buildFor("USCore", "us-core-immunization");
        this.buildFor("USCore", "us-core-location");
        this.buildFor("USCore", "us-core-medication");
        this.buildFor("USCore", "us-core-medicationdispense");
        this.buildFor("USCore", "us-core-medicationrequest");
        this.buildFor("USCore", "us-core-observation-clinical-result");
        this.buildFor("USCore", "us-core-observation-lab");
        this.buildFor("USCore", "us-core-observation-occupation");
        this.buildFor("USCore", "us-core-observation-pregnancyintent");
        this.buildFor("USCore", "us-core-observation-pregnancystatus");
        this.buildFor("USCore", "us-core-observation-screening-assessment");
        this.buildFor("USCore", "us-core-observation-sexual-orientation");
        this.buildFor("USCore", "us-core-simple-observation");
        this.buildFor("USCore", "us-core-smokingstatus");
        this.buildFor("USCore", "us-core-vital-signs");
        this.buildFor("USCore", "head-occipital-frontal-circumference-percentile");
        this.buildFor("USCore", "pediatric-bmi-for-age");
        this.buildFor("USCore", "pediatric-weight-for-height");
        this.buildFor("USCore", "us-core-blood-pressure");
        this.buildFor("USCore", "us-core-bmi");
        this.buildFor("USCore", "us-core-body-height");
        this.buildFor("USCore", "us-core-body-temperature");
        this.buildFor("USCore", "us-core-body-weight");
        this.buildFor("USCore", "us-core-head-circumference");
        this.buildFor("USCore", "us-core-heart-rate");
        //this.buildFor("USCore", "oxygensat"); // TODO: Did this go away? Subsumed by pulse oximetry?
        this.buildFor("USCore", "us-core-pulse-oximetry");
        this.buildFor("USCore", "us-core-respiratory-rate");
        this.buildFor("USCore", "us-core-organization");
        this.buildFor("USCore", "us-core-patient");
        this.buildFor("USCore", "us-core-practitioner");
        this.buildFor("USCore", "us-core-practitionerrole");
        this.buildFor("USCore", "us-core-procedure");
        this.buildFor("USCore", "us-core-provenance");
        this.buildFor("USCore", "us-core-questionnaireresponse");
        this.buildFor("USCore", "us-core-relatedperson");
        this.buildFor("USCore", "us-core-servicerequest");
        this.buildFor("USCore", "us-core-specimen");
         */

        // 8.0.0
        this.buildFor("USCore", "us-core-adi-documentreference");
        this.buildFor("USCore", "us-core-allergyintolerance");
        this.buildFor("USCore", "us-core-average-blood-pressure");
        this.buildFor("USCore", "us-core-bmi");
        this.buildFor("USCore", "us-core-blood-pressure");
        this.buildFor("USCore", "us-core-body-height");
        this.buildFor("USCore", "us-core-body-temperature");
        this.buildFor("USCore", "us-core-body-weight");
        this.buildFor("USCore", "us-core-care-experience-preference");
        this.buildFor("USCore", "us-core-careplan");
        this.buildFor("USCore", "us-core-careteam");
        this.buildFor("USCore", "us-core-condition-encounter-diagnosis");
        this.buildFor("USCore", "us-core-condition-problems-health-concerns");
        this.buildFor("USCore", "us-core-coverage");
        this.buildFor("USCore", "us-core-diagnosticreport-lab");
        this.buildFor("USCore", "us-core-diagnosticreport-note");
        this.buildFor("USCore", "us-core-documentreference");
        this.buildFor("USCore", "us-core-encounter");
        this.buildFor("USCore", "us-core-goal");
        this.buildFor("USCore", "us-core-head-circumference");
        this.buildFor("USCore", "us-core-heart-rate");
        this.buildFor("USCore", "us-core-immunization");
        this.buildFor("USCore", "us-core-implantable-device");
        this.buildFor("USCore", "us-core-observation-lab");
        this.buildFor("USCore", "us-core-location");
        this.buildFor("USCore", "us-core-medication");
        this.buildFor("USCore", "us-core-medicationdispense");
        this.buildFor("USCore", "us-core-medicationrequest");
        this.buildFor("USCore", "us-core-observation-adi-documentation");
        this.buildFor("USCore", "us-core-observation-clinical-result");
        this.buildFor("USCore", "us-core-observation-occupation");
        this.buildFor("USCore", "us-core-observation-pregnancyintent");
        this.buildFor("USCore", "us-core-observation-pregnancystatus");
        this.buildFor("USCore", "us-core-observation-screening-assessment");
        this.buildFor("USCore", "us-core-observation-sexual-orientation");
        this.buildFor("USCore", "us-core-organization");
        this.buildFor("USCore", "us-core-patient");
        this.buildFor("USCore", "pediatric-bmi-for-age");
        this.buildFor("USCore", "head-occipital-frontal-circumference-percentile");
        this.buildFor("USCore", "pediatric-weight-for-height");
        this.buildFor("USCore", "us-core-practitioner");
        this.buildFor("USCore", "us-core-practitionerrole");
        this.buildFor("USCore", "us-core-procedure");
        this.buildFor("USCore", "us-core-provenance");
        this.buildFor("USCore", "us-core-pulse-oximetry");
        this.buildFor("USCore", "us-core-questionnaireresponse");
        this.buildFor("USCore", "us-core-relatedperson");
        this.buildFor("USCore", "us-core-respiratory-rate");
        this.buildFor("USCore", "us-core-servicerequest");
        this.buildFor("USCore", "us-core-simple-observation");
        this.buildFor("USCore", "us-core-smokingstatus");
        this.buildFor("USCore", "us-core-specimen");
        this.buildFor("USCore", "us-core-treatment-intervention-preference");
        this.buildFor("USCore", "us-core-vital-signs");
    }

    @Override
    protected TypeSpecifier resolveContentReference(String modelName, String path) throws Exception {
        // This is necessary because USCore doesn't have a straight Observation type, so this content reference fails
        if (path.equals("#Observation.referenceRange")) {
            return resolveContentReference(modelName,"#LaboratoryResultObservationProfile.referenceRange");
        }

        return super.resolveContentReference(modelName, path);
    }
}