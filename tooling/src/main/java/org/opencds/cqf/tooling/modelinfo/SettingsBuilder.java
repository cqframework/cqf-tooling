package org.opencds.cqf.tooling.modelinfo;

import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.Map;


public class SettingsBuilder {

    private Map<String, String> primaryCodePath;

    private String getDefaultPrimaryCodePath(String resourceType) {
        if (resourceType != null && primaryCodePath != null) {
            return primaryCodePath.getOrDefault(resourceType, "code");
        }
        return "code";
    }

    public SettingsBuilder(Atlas atlas) {
        this.atlas = atlas;

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

    private Atlas atlas;

    public Bundle build() {
        var result = new Bundle();

        result.setId("modelinfo-settings");

        for (ImplementationGuide ig : atlas.getImplementationGuides().values()) {
            Parameters settings = buildSettings(ig);
            result.addEntry().setResource(settings);
        }

        return result;
    }

    private Parameters buildSettings(ImplementationGuide ig) {
        var result = new Parameters();

        // modelName
        result.addParameter("modelName", ig.getName());
        // modelVersion
        result.addParameter("modelVersion", ig.getVersion());
        // modelNamespace
        result.addParameter("modelNamespace", ig.getPackageId());
        // modelUrl
        result.addParameter("modelUrl", ig.getUrl()); // Need to determine how to extract canonical? Just remove /ImplementationGuide...?
        // patientClassName
        result.addParameter("patientClassName", "Patient"); // TODO: Define an extension for this?
        // patientBirthDatePropertyName
        result.addParameter("patientBirthDatePropertyName", "birthDate"); // TODO: Define an extension for this?
        // targetQualifier
        result.addParameter("targetQualifier", ig.getName());
        // targetUrl
        result.addParameter("targetUrl", "http://hl7.org/fhir"); // TODO: Define an extension for this?
        // useCqlPrimitives
        result.addParameter("useCqlPrimitives", false);
        // createSliceElements
        result.addParameter("createSliceElements", false);
        // includeMetadata
        result.addParameter("includeMetadata", false);
        // flatten
        result.addParameter("flatten", false);

        // for each dependsOn
        for (var dependsOn : ig.getDependsOn()) {
            result.addParameter().setName("dependency")
                    .addPart(new Parameters.ParametersParameterComponent().setName("modelName").setValue(new StringType(dependsOn.getPackageId())))
                    .addPart(new Parameters.ParametersParameterComponent().setName("modelVersion").setValue(new StringType(dependsOn.getVersion())))
                    .addPart(new Parameters.ParametersParameterComponent().setName("modelUrl").setValue(new StringType(dependsOn.getUri())));
        }

        // for each profile
        for (var resource : ig.getDefinition().getResource()) {
            // NOTE: This assumes the reference here will always be relative and include the resource type name
            // This should be a safe assumption within the context of the ig.definition.resource elements
            // This also assumes the atlas is indexed by id (which assumes a single namespace for ids
            // This should be reasonably safe given current naming conventions (but will break at some point)
            if (resource.getReference() != null && resource.getReference().getReference() != null && resource.getReference().getReference().contains("StructureDefinition/")) {
                var ids = resource.getReference().getReference().split("/");
                var id = ids[ids.length - 1];

                var sd = atlas.getStructureDefinitions().get(id);
                if (sd != null && sd.getKind() == StructureDefinition.StructureDefinitionKind.RESOURCE) {
                    result.addParameter().setName("profile")
                            // url
                            .addPart(new Parameters.ParametersParameterComponent().setName("url").setValue(sd.getUrlElement()))
                            // isIncluded
                            .addPart(new Parameters.ParametersParameterComponent().setName("isIncluded").setValue(new BooleanType(true)))
                            // isRetrievable
                            .addPart(new Parameters.ParametersParameterComponent().setName("isRetrievable").setValue(new BooleanType(true)))
                            // label
                            .addPart(new Parameters.ParametersParameterComponent().setName("label").setValue(sd.getTitleElement()))
                            // primaryCodePath
                            .addPart(new Parameters.ParametersParameterComponent().setName("primaryCodePath").setValue(new StringType(getDefaultPrimaryCodePath(sd.getType()))));
                }
            }
        }

        return result;
    }
}
