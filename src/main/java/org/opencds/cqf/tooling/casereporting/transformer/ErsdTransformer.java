package org.opencds.cqf.tooling.casereporting.transformer;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.casereporting.validation.UsPublicHealthValidatorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class ErsdTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformer.class);
    private FhirContext ctx;
    private FhirValidator validator;
    private IValidatorModule module = new UsPublicHealthValidatorModule();
    private final String usPhPlanDefinitionProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-plandefinition";
    private final String usPhSpecificationLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-specification-library";
    private final String usPhTriggeringValueSetLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset-library";
    private final String usPhTriggeringValueSetProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset";

    public ErsdTransformer() {
        ctx = FhirContext.forR4();
        validator = ctx.newValidator();
        // validator.setValidateAgainstStandardSchema(true);
        // validator.setValidateAgainstStandardSchematron(true);
        validator.registerValidatorModule(module);
    }

    public Bundle transform(Bundle bundle) {
        Library specificationLibrary = new Library();
        specificationLibrary.setId(new IdType("Library", "SpecificationLibrary", "1.0.0"));
        specificationLibrary.getMeta().addProfile(usPhSpecificationLibraryProfileUrl);
        specificationLibrary.setName("SpecificationLibrary");
        specificationLibrary.setTitle("Specification Library");
        specificationLibrary.setVersion("1.0.0");
        specificationLibrary.setDescription(
                "Defines the asset-collection library containing the US Public Health specification assets.");
        specificationLibrary.setStatus(PublicationStatus.ACTIVE);
        specificationLibrary.setExperimental(true);
        specificationLibrary.setPublisher("eCR");
        specificationLibrary.setUrl("http://hl7.org/fhir/us/ecr/Library/SpecificationLibrary");
        resolveRelatedArtifacts(bundle, specificationLibrary);
        specificationLibrary.setType(new CodeableConcept(
                new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection", null)));
        boolean foundSpecificationType = false;
        String useContextCode = "specification-type";
        if (specificationLibrary.hasUseContext() && !specificationLibrary.getUseContext().isEmpty()) {
            for (UsageContext useContext : specificationLibrary.getUseContext()) {
                if (useContext.getCode().getCode().equals(useContextCode)) {
                    foundSpecificationType = true;
                }
            }
        }
        CodeableConcept type = new CodeableConcept(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "program", null));
        UsageContext usageContext = new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", useContextCode, null),
                type);
        if (!foundSpecificationType) {
            specificationLibrary.addUseContext(usageContext);
        }

        return resolveSpecificationBundle(bundle, specificationLibrary);
    }

    private Bundle resolveRelatedArtifacts(Bundle bundle, Library specificationLibrary) {
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .forEach(x -> resolvePlanDefinition((PlanDefinition) x.getResource(), specificationLibrary));
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("Library"))
                .forEach(
                        x -> resolveTriggeringValueSetLibrary((Library) x.getResource(), specificationLibrary, bundle));
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("ValueSet"))
                .forEach(x -> resolveTriggeringValueSet((ValueSet) x.getResource(), specificationLibrary));
        return bundle;
    }

    private IBaseOperationOutcome resolvePlanDefinition(PlanDefinition res, Library specificationLibrary) {
        if (!resolveProfile(res, usPhPlanDefinitionProfileUrl)) {
            res.getMeta().addProfile(usPhPlanDefinitionProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        }

        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        relatedArtifact.setResource(res.getUrlElement().asStringValue());
        specificationLibrary.addRelatedArtifact(relatedArtifact);
        // res.setType(new CodeableConcept(new
        // Coding("http://terminology.hl7.org/CodeSystem/plan-definition-type",
        // "workflow-definition", "Workflow Definition")));
        // res.getAction().forEach(action -> {
        // action.getTrigger().forEach(trigger -> {
        // trigger.setType(TriggerType.NAMEDEVENT);
        // });
        // });
        // res.setVersion("1.0.0");
        // res.setPublisher("eCR");
        // res.setExperimental(true);
        // res.setDescription("Example Description");
        return null;
    }

    private boolean resolveProfile(Resource res, String profile) {
        boolean resolvedProfile = false;
        if (res.hasMeta() && res.getMeta().hasProfile()) {
            for (CanonicalType canonical : res.getMeta().getProfile()) {
                if (canonical.asStringValue().equals(profile)) {
                    resolvedProfile = true;
                }
            }
        }
        return resolvedProfile;
    }

    private IBaseOperationOutcome resolveTriggeringValueSetLibrary(Library res, Library specificationLibrary,
            Bundle bundle) {
        if (!resolveProfile(res, usPhTriggeringValueSetLibraryProfileUrl)) {
            res.getMeta().addProfile(usPhTriggeringValueSetLibraryProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        }
        // bundle.getEntry().stream()
        // .filter(x -> (x.hasResource() &&
        // x.getResource().fhirType().equals("ValueSet")))
        // .map(x -> (ValueSet) x.getResource())
        // .forEach(vs -> {
        // RelatedArtifact relatedArtifact = new RelatedArtifact();
        // relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        // relatedArtifact.setResource(vs.getUrlElement().asStringValue());
        // res.addRelatedArtifact(relatedArtifact);
        // });
        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        relatedArtifact.setResource(res.getUrlElement().asStringValue());
        specificationLibrary.addRelatedArtifact(relatedArtifact);
        res.setPublisher("eCR");
        res.setExperimental(false);
        return null;
    }

    private IBaseOperationOutcome resolveTriggeringValueSet(ValueSet res, Library specificationLibrary) {
        if (!resolveProfile(res, usPhTriggeringValueSetProfileUrl)) {
            res.getMeta().addProfile(usPhTriggeringValueSetProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        } else {
            RelatedArtifact relatedArtifact = new RelatedArtifact();
            relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
            relatedArtifact.setResource(res.getUrlElement().asStringValue());
            // specificationLibrary.addRelatedArtifact(relatedArtifact);
        }
        res.addUseContext(
                new UsageContext(
                        new Coding(
                                "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "reporting", null),
                        new CodeableConcept(
                                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "triggering",
                                        null))));
        res.setVersion("1.0.0");
        res.setPublisher("eCR");
        res.setExperimental(true);
        return null;
    }

    private Bundle resolveSpecificationBundle(Bundle bundle, Library specificationLibrary) {
        System.out.println(FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(specificationLibrary));
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();
        entries.add(new BundleEntryComponent().setResource(specificationLibrary)
                .setFullUrl("http://hl7.org/fhir/us/ecr/Library/SpecificationLibrary"));
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof ValueSet) {
                ValueSet v = (ValueSet) entry.getResource();
                // v.setCompose(new ValueSetComposeComponent());
                v.setText(new Narrative());
                v.getExpansion().getContains().forEach(x -> x.setDisplay(""));
            }
        }
        bundle.getEntry().forEach(entry -> entries.add(entry));
        bundle.setEntry(entries);
        return bundle;
    }
}
