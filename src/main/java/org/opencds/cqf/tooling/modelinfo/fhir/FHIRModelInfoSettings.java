package org.opencds.cqf.tooling.modelinfo.fhir;

import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.opencds.cqf.tooling.modelinfo.ModelInfoSettings;

@SuppressWarnings("serial")
public class FHIRModelInfoSettings extends ModelInfoSettings {

    public FHIRModelInfoSettings(String version) {
        super("FHIR", version, "http://hl7.org/fhir", "FHIR.Patient", "birthDate.value", "fhir", null);
        this.conversionInfos = new ArrayList<ConversionInfo>() {
            {
                add(new ConversionInfo().withFromType("FHIR.Coding").withToType("System.Code").withFunctionName("FHIRHelpers.ToCode"));
                add(new ConversionInfo().withFromType("FHIR.CodeableConcept").withToType("System.Concept")
                    .withFunctionName("FHIRHelpers.ToConcept"));
                add(new ConversionInfo().withFromType("FHIR.Quantity").withToType("System.Quantity")
                    .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.SimpleQuantity").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.Age").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.Distance").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.Duration").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.Count").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                //add(new ConversionInfo().withFromType("FHIR.MoneyQuantity").withToType("System.Quantity")
                //        .withFunctionName("FHIRHelpers.ToQuantity"));
                add(new ConversionInfo().withFromType("FHIR.Period").withToType("Interval<System.DateTime>")
                            .withFunctionName("FHIRHelpers.ToInterval"));
                add(new ConversionInfo().withFromType("FHIR.Range").withToType("Interval<System.Quantity>")
                        .withFunctionName("FHIRHelpers.ToInterval"));
                add(new ConversionInfo().withFromType("FHIR.Ratio").withToType("System.Ratio")
                        .withFunctionName("FHIRHelpers.ToRatio"));
            }
        };
/*
        this.primarySearchPath = new HashMap<String, String>() {
            {
                put("Account", "type");
                put("ActivityDefinition", "context");
                put("AdverseEvent", "event");
                put("AllergyIntolerance", "code");
                put("Appointment", "service-type");
                put("Basic", "code");
                put("BodyStructure", "location");
                put("CarePlan", "category");
                put("CareTeam", "category");
                put("ChargeItemDefinition", "context");
                put("Claim", "use");
                put("ClinicalImpression", "status"); // TODO: Should request a search parameter on clinical impression by code
                put("CodeSystem", "context");
                put("Communication", "category");
                put("CommunicationRequest", "category");
                put("CompartmentDefinition", "context");
                put("Composition", "type");
                put("Condition", "code");
                put("Consent", "category");
                put("Coverage", "type");
                put("DetectedIssue", "code");
                put("Device", "type");
                put("DeviceMetric", "type");
                put("DeviceRequest", "code");
                put("DeviceUseStatement", "device");
                put("DiagnosticReport", "code");
                put("Encounter", "type");
                put("EpisodeOfCare", "type");
                put("ExplanationOfBenefit", "status"); // TODO: Should request a search parameter on explanation of benefit by type
                put("Flag", "identifier"); // TODO: Should request a search parameter on Flag by code
                put("Goal", "category");
                put("GuidanceResponse", "identifier"); // TODO: Should request a search parameter on GuidanceResponse by module
                put("HealthcareService", "service-type");
                put("Immunization", "vaccine-code");
                put("Library", "context");
                put("Location", "type");
                put("Measure", "context");
                put("MeasureReport", "measure");
                put("Medication", "code");
                put("MedicationAdministration", "medication");
                put("MedicationDispense", "medication");
                put("MedicationRequest", "medication");
                put("MedicationStatement", "medication");
                put("MessageDefinition", "context");
                put("Observation", "code");
                //put("OperationOutcome", "issue.code");
                put("PlanDefinition", "context");
                put("PractitionerRole", "role");
                put("Procedure", "code");
                put("Questionnaire", "context");
                put("RelatedPerson", "relationship");
                put("RiskAssessment", "method"); // TODO: Request a search parameter by code
                put("SearchParameter", "context");
                put("ServiceRequest", "code");
                put("Specimen", "type");
                put("StructureDefinition", "context");
                put("Substance", "code");
                put("SupplyDelivery", "status"); // TODO: Request a searchparameter by type
                put("SupplyRequest", "category");
                put("Task", "code");
                put("ValueSet", "context");
            }
        };
*/
    }
}