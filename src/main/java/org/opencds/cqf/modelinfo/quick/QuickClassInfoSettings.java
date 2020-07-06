package org.opencds.cqf.modelinfo.quick;

import java.util.HashMap;
import java.util.HashSet;

import org.opencds.cqf.modelinfo.ClassInfoSettings;


class QuickClassInfoSettings extends ClassInfoSettings {

    public QuickClassInfoSettings() {
        this.modelName = "QUICK";
        this.modelPrefix = "QICore";
        this.useCQLPrimitives = true;

        this.codeableTypes = new HashSet<String>() {
            {
                add("System.String");
                add("System.Code");
                add("System.Concept");
            }
        };

        this.urlToModel.put("http://hl7.org/fhir", "QUICK");

        this.primitiveTypeMappings = new HashMap<String, String>() {
            {
                put("QUICK.base64Binary", "System.String");
                put("QUICK.boolean", "System.Boolean");
                put("QUICK.canonical", "System.String");
                put("QUICK.code", "System.String");
                put("QUICK.date", "System.Date");
                put("QUICK.dateTime", "System.DateTime");
                put("QUICK.decimal", "System.Decimal");
                put("QUICK.id", "System.String");
                put("QUICK.instant", "System.DateTime");
                put("QUICK.integer", "System.Integer");
                put("QUICK.markdown", "System.String");
                put("QUICK.oid", "System.String");
                put("QUICK.positiveInt", "System.Integer");
                put("QUICK.string", "System.String");
                put("QUICK.time", "System.Time");
                put("QUICK.unsignedInt", "System.Integer");
                put("QUICK.uri", "System.String");
                put("QUICK.url", "System.String");
                put("QUICK.uuid", "System.String");
                put("QUICK.xhtml", "System.String");
            }
        };

        this.cqlTypeMappings = new HashMap<String, String>() {
            {
                 put("QUICK.xsd:base64Binary", "System.String");
                 put("QUICK.base64Binary", "System.String");
                 put("QUICK.xsd:boolean", "System.Boolean");
                 put("QUICK.boolean", "System.Boolean");
                 put("QUICK.canonical", "System.String");
                 put("QUICK.xsd:token", "System.String");
                 put("QUICK.code", "System.String");
                 put("QUICK.xsd:gYear OR xsd:gYearMonth OR xsd:date", "System.Date");
                 put("QUICK.xsd:date", "System.Date");
                 put("QUICK.date", "System.Date");
                 put("QUICK.xsd:gYear OR xsd:gYearMonth OR xsd:date OR xsd:dateTime", "System.DateTime");
                 put("QUICK.dateTime", "System.DateTime");
                 put("QUICK.xsd:decimal OR xsd:double", "System.Decimal");
                 put("QUICK.decimal", "System.Decimal");
                 put("QUICK.id", "System.String");
                 put("QUICK.xsd:dateTime", "System.DateTime");
                 put("QUICK.instant", "System.DateTime");
                 put("QUICK.xsd:int", "System.Integer");
                 put("QUICK.integer", "System.Integer");
                 put("QUICK.markdown", "System.String");
                 put("QUICK.oid", "System.String");
                 put("QUICK.xsd:positiveInteger", "System.Integer");
                 put("QUICK.positiveInt", "System.Integer");
                 put("QUICK.xsd:string", "System.String");
                 put("QUICK.string", "System.String");
                 put("QUICK.xsd:time", "System.Time");
                 put("QUICK.time", "System.Time");
                 put("QUICK.xsd:nonNegativeInteger", "System.Integer");
                 put("QUICK.unsignedInt", "System.Integer");
                 put("QUICK.xsd:anyURI", "System.String");
                 put("QUICK.uri", "System.String");
                 put("QUICK.url", "System.String");
                 put("QUICK.uuid", "System.String");
                 put("QUICK.xhtml:div", "System.String");
                 put("QUICK.xhtml", "System.String");
                 put("QUICK.Coding", "System.Code");
                 put("QUICK.CodeableConcept", "System.Concept");
                 put("QUICK.Period", "Interval<System.DateTime>");
                 put("QUICK.Range", "Interval<System.Quantity>");
                 put("QUICK.Quantity", "System.Quantity");
                 put("QUICK.Age", "System.Quantity");
                 put("QUICK.Distance", "System.Quantity");
                 put("QUICK.SimpleQuantity", "System.Quantity");
                 put("QUICK.Duration", "System.Quantity");
                 put("QUICK.Count", "System.Quantity");
                 put("QUICK.MoneyQuantity", "System.Quantity");
                 put("QUICK.Money", "System.Decimal");
                 put("QUICK.Ratio", "System.Ratio");
            }
        };

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
                put("MedicationAdministration", "medicationCodeableConcept");
                put("MedicationDispense", "medicationCodeableConcept");
                put("MedicationRequest", "medicationCodeableConcept");
                put("MedicationStatement", "medicationCodeableConcept");
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