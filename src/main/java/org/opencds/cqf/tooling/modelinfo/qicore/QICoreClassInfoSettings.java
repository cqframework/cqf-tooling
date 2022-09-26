package org.opencds.cqf.tooling.modelinfo.qicore;

import java.util.HashMap;
import java.util.HashSet;

import org.opencds.cqf.tooling.modelinfo.ClassInfoSettings;


@SuppressWarnings("serial")
class QICoreClassInfoSettings extends ClassInfoSettings {

    public QICoreClassInfoSettings() {
        this.modelName = "QICore";
        this.modelPrefix = "QICore";
        this.helpersLibraryName = "FHIRHelpers";
        this.useCQLPrimitives = true;
        this.createSliceElements = true;

        this.codeableTypes = new HashSet<String>() {
            {
                add("System.String");
                add("System.Code");
                add("System.Concept");
            }
        };

        this.urlToModel.put("http://hl7.org/fhir", "QICore");

        this.primitiveTypeMappings = new HashMap<String, String>() {
            {
                put("QICore.base64Binary", "System.String");
                put("QICore.boolean", "System.Boolean");
                put("QICore.canonical", "System.String");
                put("QICore.code", "System.String");
                put("QICore.date", "System.Date");
                put("QICore.dateTime", "System.DateTime");
                put("QICore.decimal", "System.Decimal");
                put("QICore.id", "System.String");
                put("QICore.instant", "System.DateTime");
                put("QICore.integer", "System.Integer");
                put("QICore.markdown", "System.String");
                put("QICore.oid", "System.String");
                put("QICore.positiveInt", "System.Integer");
                put("QICore.string", "System.String");
                put("QICore.time", "System.Time");
                put("QICore.unsignedInt", "System.Integer");
                put("QICore.uri", "System.String");
                put("QICore.url", "System.String");
                put("QICore.uuid", "System.String");
                put("QICore.xhtml", "System.String");
            }
        };

        this.cqlTypeMappings = new HashMap<String, String>() {
            {
                put("QICore.xsd:base64Binary", "System.String");
                put("QICore.base64Binary", "System.String");
                put("QICore.xsd:boolean", "System.Boolean");
                put("QICore.boolean", "System.Boolean");
                put("QICore.canonical", "System.String");
                put("QICore.xsd:token", "System.String");
                put("QICore.code", "System.String");
                put("QICore.xsd:gYear OR xsd:gYearMonth OR xsd:date", "System.Date");
                put("QICore.xsd:date", "System.Date");
                put("QICore.date", "System.Date");
                put("QICore.xsd:gYear OR xsd:gYearMonth OR xsd:date OR xsd:dateTime", "System.DateTime");
                put("QICore.dateTime", "System.DateTime");
                put("QICore.xsd:decimal OR xsd:double", "System.Decimal");
                put("QICore.decimal", "System.Decimal");
                put("QICore.id", "System.String");
                put("QICore.xsd:dateTime", "System.DateTime");
                put("QICore.instant", "System.DateTime");
                put("QICore.xsd:int", "System.Integer");
                put("QICore.integer", "System.Integer");
                put("QICore.markdown", "System.String");
                put("QICore.oid", "System.String");
                put("QICore.xsd:positiveInteger", "System.Integer");
                put("QICore.positiveInt", "System.Integer");
                put("QICore.xsd:string", "System.String");
                put("QICore.string", "System.String");
                put("QICore.xsd:time", "System.Time");
                put("QICore.time", "System.Time");
                put("QICore.xsd:nonNegativeInteger", "System.Integer");
                put("QICore.unsignedInt", "System.Integer");
                put("QICore.xsd:anyURI", "System.String");
                put("QICore.uri", "System.String");
                put("QICore.url", "System.String");
                put("QICore.uuid", "System.String");
                put("QICore.xhtml:div", "System.String");
                put("QICore.xhtml", "System.String");
                put("QICore.Coding", "System.Code");
                put("QICore.CodeableConcept", "System.Concept");
                put("QICore.Period", "Interval<System.DateTime>");
                put("QICore.Range", "Interval<System.Quantity>");
                put("QICore.Quantity", "System.Quantity");
                put("QICore.Age", "System.Quantity");
                put("QICore.Distance", "System.Quantity");
                put("QICore.SimpleQuantity", "System.Quantity");
                put("QICore.Duration", "System.Quantity");
                put("QICore.Count", "System.Quantity");
                put("QICore.MoneyQuantity", "System.Quantity");
                put("QICore.Money", "System.Decimal");
                put("QICore.Ratio", "System.Ratio");
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
                put("BodyStructure", "location");
                put("CarePlan", "category");
                put("CareTeam", "category");
                put("ChargeItemDefinition", "code");
                put("Claim", "type");
                put("ClinicalImpression", "code");
                put("Communication", "reasonCode");
                put("CommunicationNotDone", "reasonCode");
                put("CommunicationRequest", "category");
                put("Composition", "type");
                put("Condition", "code");
                put("Consent", "category");
                put("Coverage", "type");
                put("DetectedIssue", "category");
                put("Device", "type");
                put("DeviceMetric", "type");
                put("DeviceRequest", "code");
                put("DeviceNotRequested", "code");
                put("DeviceUseStatement", "device.code");
                put("DiagnosticReport", "code");
                put("DiagnosticReportLab", "code");
                put("DiagnosticReportNote", "code");
                put("Encounter", "type");
                put("EpisodeOfCare", "type");
                put("ExplanationOfBenefit", "type");
                put("Flag", "code");
                put("FamilyMemberHistory", "relationship");
                put("Goal", "category");
                put("GuidanceResponse", "module");
                put("HealthcareService", "type");
                put("ImagingStudy", "procedureCode");
                put("Immunization", "vaccineCode");
                put("ImmunizationEvaluation", "targetDisease");
                put("ImmunizationNotDone", "vaccineCode");
                put("ImmunizationRecommendation", "recommendation.vaccineCode");
                put("Location", "type");
                put("Library", "topic");
                put("Measure", "topic");
                put("MeasureReport", "measure.topic");
                put("Medication", "code");
                put("MedicationAdministration", "medication");
                put("MedicationAdministrationNotDone", "medication");
                put("MedicationDispense", "medication");
                put("MedicationDispenseNotDone", "medication");
                put("MedicationRequest", "medication");
                put("MedicationNotRequested", "medication");
                put("MedicationStatement", "medication");
                put("MessageDefinition", "event");
                put("Observation", "code");
                put("ObservationNotDone", "code");
                put("OperationOutcome", "issue.code");
                put("Organization", "type");
                // put("Practitioner", ""); // Not clear what the primary code path should be...
                put("PractitionerRole", "code");
                put("Procedure", "code");
                put("ProcedureNotDone", "code");
                put("ProcedureRequest", "code");
                put("Questionnaire", "name");
                //put("QuestionnaireResponse", ""); // Not clear what the primary code path should be...
                put("ServiceRequest", "type");
                put("ServiceNotRequested", "type");
                put("RelatedPerson", "relationship");
                put("RiskAssessment", "code");
                put("SearchParameter", "target");
                put("Sequence", "type");
                put("Specimen", "type");
                put("Substance", "code");
                put("SupplyDelivery", "type");
                put("SupplyRequest", "category");
                put("Task", "code");
                put("TaskNotDone", "code");
                put("USCoreImplantableDeviceProfile", "type");
                put("USCoreLaboratoryResultObservationProfile", "code");
                put("USCorePediatricBMIforAgeObservationProfile", "code");
                put("USCorePediatricWeightForHeightObservationProfile", "code");
                put("USCorePulseOximetryProfile", "code");
                put("USCoreSmokingStatusProfile", "code");
                put("observation-bmi", "code");
                put("observation-bodyheight", "code");
                put("observation-bodytemp", "code");
                put("observation-bodyweight", "code");
                put("observation-bp", "code");
                put("observation-headcircum", "code");
                put("observation-heartrate", "code");
                put("observation-oxygensat", "code");
                put("observation-resprate", "code");
                put("observation-vitalspanel", "code");
            }
        };
    }
}