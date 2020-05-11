package org.opencds.cqf.modelinfo.uscore;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import org.opencds.cqf.modelinfo.ClassInfoSettings;


class USCoreClassInfoSettings extends ClassInfoSettings {

    public USCoreClassInfoSettings() {
        this.modelName = "USCore";
        this.modelPrefix = "USCore";
        this.helpersLibraryName = "FHIRHelpers";
        this.useCQLPrimitives = true;

        this.codeableTypes = new HashSet<String>() {
            {
                add("System.String");
                add("System.Code");
                add("System.Concept");
            }
        };

        this.urlToModel.put("http://hl7.org/fhir", "USCore");

        this.primitiveTypeMappings = new HashMap<String, String>() {
            {
                put("USCore.base64Binary", "System.String");
                put("USCore.boolean", "System.Boolean");
                put("USCore.canonical", "System.String");
                put("USCore.code", "System.String");
                put("USCore.date", "System.Date");
                put("USCore.dateTime", "System.DateTime");
                put("USCore.decimal", "System.Decimal");
                put("USCore.id", "System.String");
                put("USCore.instant", "System.DateTime");
                put("USCore.integer", "System.Integer");
                put("USCore.markdown", "System.String");
                put("USCore.oid", "System.String");
                put("USCore.positiveInt", "System.Integer");
                put("USCore.string", "System.String");
                put("USCore.time", "System.Time");
                put("USCore.unsignedInt", "System.Integer");
                put("USCore.uri", "System.String");
                put("USCore.url", "System.String");
                put("USCore.uuid", "System.String");
                put("USCore.xhtml", "System.String");
            }
        };

        this.cqlTypeMappings = new HashMap<String, String>() {
            {
                put("USCore.xsd:base64Binary", "System.String");
                put("USCore.base64Binary", "System.String");
                put("USCore.xsd:boolean", "System.Boolean");
                put("USCore.boolean", "System.Boolean");
                put("USCore.canonical", "System.String");
                put("USCore.xsd:token", "System.String");
                put("USCore.code", "System.String");
                put("USCore.xsd:gYear OR xsd:gYearMonth OR xsd:date", "System.Date");
                put("USCore.xsd:date", "System.Date");
                put("USCore.date", "System.Date");
                put("USCore.xsd:gYear OR xsd:gYearMonth OR xsd:date OR xsd:dateTime", "System.DateTime");
                put("USCore.dateTime", "System.DateTime");
                put("USCore.xsd:decimal OR xsd:double", "System.Decimal");
                put("USCore.decimal", "System.Decimal");
                put("USCore.id", "System.String");
                put("USCore.xsd:dateTime", "System.DateTime");
                put("USCore.instant", "System.DateTime");
                put("USCore.xsd:int", "System.Integer");
                put("USCore.integer", "System.Integer");
                put("USCore.markdown", "System.String");
                put("USCore.oid", "System.String");
                put("USCore.xsd:positiveInteger", "System.Integer");
                put("USCore.positiveInt", "System.Integer");
                put("USCore.xsd:string", "System.String");
                put("USCore.string", "System.String");
                put("USCore.xsd:time", "System.Time");
                put("USCore.time", "System.Time");
                put("USCore.xsd:nonNegativeInteger", "System.Integer");
                put("USCore.unsignedInt", "System.Integer");
                put("USCore.xsd:anyURI", "System.String");
                put("USCore.uri", "System.String");
                put("USCore.url", "System.String");
                put("USCore.uuid", "System.String");
                put("USCore.xhtml:div", "System.String");
                put("USCore.xhtml", "System.String");
                put("USCore.Coding", "System.Code");
                put("USCore.CodeableConcept", "System.Concept");
                put("USCore.Period", "Interval<System.DateTime>");
                put("USCore.Range", "Interval<System.Quantity>");
                put("USCore.Quantity", "System.Quantity");
                put("USCore.Age", "System.Quantity");
                put("USCore.Distance", "System.Quantity");
                put("USCore.SimpleQuantity", "System.Quantity");
                put("USCore.Duration", "System.Quantity");
                put("USCore.Count", "System.Quantity");
                put("USCore.MoneyQuantity", "System.Quantity");
                put("USCore.Money", "System.Decimal");
                put("USCore.Ratio", "System.Ratio");
            }
        };

/*
        this.typeNameMappings = new HashMap<String, String>() {
            {
                put("CarePlanProfile", "CarePlan");
                put("DiagnosticReportProfileLaboratoryReporting", "DiagnosticReport"); // TODO: Support synonyms?
                put("DiagnosticReportProfileNoteExchange", "DiagnosticReportNote");
                put("DocumentReferenceProfile", "DocumentReference");
                put("EncounterProfile", "Encounter");
                put("GoalProfile", "Goal");
                put("ImmunizationProfile", "Immunizataion");
                put("ImplantableDeviceProfile", "Device"); // TODO: Support synonyms?
                put("LaboratoryResultObservationProfile", "Observation"); // TODO: Support synonyms?
                put("MedicationProfile", "Medication");
                put("MedicationRequestProfile", "MedicationRequest");
                put("OrganizationProfile", "Organization");
                put("PatientProfile", "Patient");
                put("PediatricBMIforAgeObservationProfile", "PediatricBMIforAgeObservation");
                put("PediatricWeightForHeightObservationProfile", "PediatricWeightForHeightObservation");
                put("PractitionerProfile", "Practitioner");
                put("PractitionerRoleProfile", "PractitionerRole");
                put("ProcedureProfile", "Procedure");
                put("PulseOximetryProfile", "PulseOximetry");
                put("SmokingStatusProfile", "SmokingStatus");
            }
        };
 */

        this.primaryCodePath = new HashMap<String, String>() {
            {
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