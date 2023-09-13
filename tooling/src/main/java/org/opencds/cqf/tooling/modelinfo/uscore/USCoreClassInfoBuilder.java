package org.opencds.cqf.tooling.modelinfo.uscore;


import java.util.Map;

import org.hl7.elm_modelinfo.r1.TypeSpecifier;
import org.hl7.fhir.ImplementationGuide;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.tooling.modelinfo.ClassInfoBuilder;

public class USCoreClassInfoBuilder extends ClassInfoBuilder {

    public USCoreClassInfoBuilder(Map<String, StructureDefinition> structureDefinitions) {
        super(new USCoreClassInfoSettings(), structureDefinitions);
    }

    @Override
    protected void innerBuild() {
        System.out.println("Building Resources");
        if (!this.settings.flattenHierarchy) {
            this.buildFor("USCore", (x -> x.getKind() == StructureDefinition.StructureDefinitionKind.RESOURCE && x.getUrl().startsWith("http://hl7.org/fhir/us/core")));
        }
        else {
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
        }
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