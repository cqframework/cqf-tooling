package org.opencds.cqf.modelinfo.fhir;

import java.util.HashMap;
import java.util.HashSet;

import org.opencds.cqf.modelinfo.ClassInfoSettings;


class FHIRClassInfoSettings extends ClassInfoSettings {

    public FHIRClassInfoSettings() {
        this.modelName = "FHIR";
        this.modelPrefix = "";
        this.helpersLibraryName = "FHIRHelpers";
        this.codeableTypes = new HashSet<String>() {
            {
                add("System.String");
                add("System.Code");
                add("System.Concept");
                add("FHIR.string");
                add("FHIR.code");
                add("FHIR.Coding");
                add("FHIR.CodeableConcept");
            }
        };

        this.primitiveTypeMappings = new HashMap<String, String>() {
            {
                put("FHIR.base64Binary", "System.String");
                put("FHIR.boolean", "System.Boolean");
                put("FHIR.canonical", "System.String");
                put("FHIR.code", "System.String");
                put("FHIR.date", "System.Date");
                put("FHIR.dateTime", "System.DateTime");
                put("FHIR.decimal", "System.Decimal");
                put("FHIR.id", "System.String");
                put("FHIR.instant", "System.DateTime");
                put("FHIR.integer", "System.Integer");
                put("FHIR.markdown", "System.String");
                put("FHIR.oid", "System.String");
                put("FHIR.positiveInt", "System.Integer");
                put("FHIR.string", "System.String");
                put("FHIR.time", "System.Time");
                put("FHIR.unsignedInt", "System.Integer");
                put("FHIR.uri", "System.String");
                put("FHIR.url", "System.String");
                put("FHIR.uuid", "System.String");
                put("FHIR.xhtml", "System.String");
            }
        };

        this.cqlTypeMappings = new HashMap<String, String>() {
            {
                put("FHIR.base64Binary", "System.String");
                put("FHIR.boolean", "System.Boolean");
                put("FHIR.code", "System.String");
                put("FHIR.date", "System.Date");
                put("FHIR.dateTime", "System.DateTime");
                put("FHIR.decimal", "System.Decimal");
                put("FHIR.id", "System.String");
                put("FHIR.instant", "System.DateTime");
                put("FHIR.integer", "System.Integer");
                put("FHIR.markdown", "System.String");
                put("FHIR.oid", "System.String");
                put("FHIR.positiveInt", "System.Integer");
                put("FHIR.string", "System.String");
                put("FHIR.time", "System.Time");
                put("FHIR.unsignedInt", "System.Integer");
                put("FHIR.uri", "System.String");
                put("FHIR.url", "System.String");
                put("FHIR.uuid", "System.String");
                put("FHIR.xhtml", "System.String");
                put("FHIR.Coding", "System.Code");
                put("FHIR.CodeableConcept", "System.Concept");
                put("FHIR.Period", "Interval<System.DateTime>");
                put("FHIR.Range", "Interval<System.Quantity>");
                put("FHIR.Ratio", "System.Ratio");
                put("FHIR.Quantity", "System.Quantity");
                put("FHIR.Age", "System.Quantity");
                put("FHIR.Distance", "System.Quantity");
                put("FHIR.SimpleQuantity", "System.Quantity");
                put("FHIR.Duration", "System.Quantity");
                put("FHIR.Count", "System.Quantity");
                put("FHIR.MoneyQuantity", "System.Quantity");
                put("FHIR.Money", "System.Decimal");
            }
        };

        this.primaryCodePath = new HashMap<String, String>() {
            {
                put("Account", "type");
                put("ActivityDefinition", "topic");
                put("AdverseEvent", "type");
                put("AllergyIntolerance", "code");
                put("Appointment", "serviceType");
                put("Basic", "code");
                put("CarePlan", "category");
                put("CareTeam", "category");
                put("ChargeItemDefinition", "code");
                put("Claim", "type");
                put("ClinicalImpression", "code");
                put("Communication", "category");
                put("CommunicationRequest", "category");
                put("Composition", "type");
                put("Condition", "code");
                put("Consent", "category");
                put("Coverage", "type");
                put("DetectedIssue", "category");
                put("Device", "type");
                put("DeviceMetric", "type");
                put("DeviceRequest", "codeCodeableConcept");
                put("DeviceUseStatement", "device.code");
                put("DiagnosticReport", "code");
                put("Encounter", "type");
                put("EpisodeOfCare", "type");
                put("ExplanationOfBenefit", "type");
                put("Flag", "code");
                put("Goal", "category");
                put("GuidanceResponse", "module");
                put("HealthcareService", "type");
                put("Immunization", "vaccineCode");
                put("Library", "topic");
                put("Measure", "topic");
                put("MeasureReport", "measure.topic");
                put("Medication", "code");
                put("MedicationAdministration", "medication");
                put("MedicationDispense", "medication");
                put("MedicationRequest", "medication");
                put("MedicationStatement", "medication");
                put("MessageDefinition", "event");
                put("Observation", "code");
                put("OperationOutcome", "issue.code");
                put("Procedure", "code");
                put("ProcedureRequest", "code");
                put("Questionnaire", "name");
                put("ReferralRequest", "type");
                put("RiskAssessment", "code");
                put("SearchParameter", "target");
                put("Sequence", "type");
                put("Specimen", "type");
                put("Substance", "code");
                put("SupplyDelivery", "type");
                put("SupplyRequest", "category");
                put("Task", "code");
            }
        };
    }
}