package org.opencds.cqf.modelinfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.hl7.elm_modelinfo.r1.ConversionInfo;

public class Configuration {

    public static final Configuration DefaultConfiguration = new Configuration();

    public ClassInfoSettings classInfoSettings = new ClassInfoSettings();

    class ClassInfoSettings {
        public boolean useCQLPrimitives = false;
        public boolean createExtensionElements = false;
        public boolean createReferenceElements = false;
    }

    class ModelInfoSettings {
        public ModelInfoSettings(String name, String version, String url, String patientClassName,
                String patientBirthDatePropertyName, String targetQualifier) {
            this.name = name;
            this.version = version;
            this.url = url;
            this.patientClassName = patientClassName;
            this.targetQualifier = targetQualifier;
        }

        public String name;
        public String version;
        public String url;
        public String patientClassName;
        public String patientBirthDatePropertyName;
        public String targetQualifier;
    }

    public Map<String, ModelInfoSettings> modelInfoSettings = new HashMap<String, ModelInfoSettings>() {
        {
            put("System", new ModelInfoSettings("System", null, "urn:hl7-org:elm-types:r1", null, null, null));
            put("FHIR", new ModelInfoSettings("FHIR", "3.0.1", "http://hl7.org/fhir", "FHIR.Patient", "birthDate.value",
                    "fhir"));
            // put("USCore", "http://hl7.org/fhir/us/core");
            // put("QICore", "http://hl7.org/fhir/us/qicore");
        }
    };

    public Set<String> codeableTypes = new HashSet<String>() {
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

    public Map<String, String> primitiveTypeMappings = new HashMap<String, String>() {
        {
            put("FHIR.base64Binary", "System.String");
            put("FHIR.boolean", "System.Boolean");
            put("FHIR.code", "System.String");
            put("FHIR.date", "System.DateTime");
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
            put("FHIR.uuid", "System.String");
            put("FHIR.xhtml", "System.String");
        }
    };

    public Map<String, String> cqlTypeMappings = new HashMap<String, String>() {
        {
            put("FHIR.base64Binary", "System.String");
            put("FHIR.boolean", "System.Boolean");
            put("FHIR.code", "System.String");
            put("FHIR.date", "System.DateTime");
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
            put("FHIR.uuid", "System.String");
            put("FHIR.xhtml", "System.String");
            put("FHIR.Coding", "System.Code");
            put("FHIR.CodeableConcept", "System.Concept");
            put("FHIR.Period", "System.DateTime");
            put("FHIR.Range", "System.Quantity");
            put("FHIR.Quantity", "System.Quantity");
            put("FHIR.Age", "System.Quantity");
            put("FHIR.Distance", "System.Quantity");
            put("FHIR.SimpleQuantity", "System.Quantity");
            put("FHIR.Duration", "System.Quantity");
            put("FHIR.Count", "System.Quantity");
            put("FHIR.Money", "System.Quantity");
        }
    };

    public Map<String, String> primaryCodePath = new HashMap<String, String>() {
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

    Collection<ConversionInfo> modelConversionInfos = new ArrayList<ConversionInfo>() {
        {
            add(new ConversionInfo().withFromType("Coding").withToType("System.Code").withFunctionName("ToCode"));
            add(new ConversionInfo().withFromType("CodeableConcept").withToType("System.Concept")
                    .withFunctionName("ToCode"));
            add(new ConversionInfo().withFromType("Quantity").withToType("System.Quantity")
                    .withFunctionName("ToQuantity"));
            add(new ConversionInfo().withFromType("Period").withToType("Interval<System.DateTime>")
                    .withFunctionName("ToInterval"));
            add(new ConversionInfo().withFromType("Range").withToType("Interval<System.Quantity>")
                    .withFunctionName("ToInterval"));
        }
    };
}
