package org.opencds.cqf.tooling.casereporting.validation;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.validation.IValidationContext;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UsPublicHealthValidatorModule implements IValidatorModule {
    private static final Logger logger = LoggerFactory.getLogger(UsPublicHealthValidatorModule.class);

    @Override
    public void validateResource(IValidationContext<IBaseResource> theCtx) {
        if (!validateCtx(theCtx)) {
            return;
        }
        validateProfiles(theCtx);
    }

    private void validateProfiles(IValidationContext<IBaseResource> theCtx) {
        List<String> profiles = theCtx.getResource().getMeta().getProfile().stream().map(x -> x.getValueAsString())
                .collect(Collectors.toList());
        for (String profile : profiles) {
            switch (profile) {
                case "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-plandefinition":
                    validateUSPublicHealthPlanDefinition(theCtx);
                    break;
                case "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset-library":
                    validateUsPublicHealthTriggeringValueSetLibrary(theCtx);
                    break;
                case "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset":
                    validateUsPublicHealthTriggeringValueSet(theCtx);
                    break;
                default:
                    logger.info("Profile is not US Public Health skipping validation for profile " + profile);
                    break;
            }
        }
    }

    private void validateUsPublicHealthTriggeringValueSet(IValidationContext<IBaseResource> theCtx) {
    }

    private void validateUsPublicHealthTriggeringValueSetLibrary(IValidationContext<IBaseResource> theCtx) {
    }

    private void validateUSPublicHealthPlanDefinition(IValidationContext<IBaseResource> theCtx) {
        PlanDefinition planDefinition = (PlanDefinition) theCtx.getResource();
        validateExtensions(theCtx, planDefinition);
    }

    private boolean validateExtensions(IValidationContext<IBaseResource> theCtx, PlanDefinition planDefinition) {
        // if (planDefinition.hasExtension("http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-receiver-address-extension")) {
        //     String message = "Found expected extension: http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-receiver-address-extension.";
        //     theCtx.addValidationMessage(createInformationMessage(message));
        //     return true;
        // } else {
        //     String theMessage = "Expected extension: http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-receiver-address-extension.";
        //     theCtx.addValidationMessage(createErrorMessage(theMessage));
        //     return false;
        // }
        return true;
    }

    private boolean validateCtx(IValidationContext<IBaseResource> theCtx) {
        if (theCtx.getFhirContext().getVersion().getVersion().isOlderThan(FhirVersionEnum.R4)) {
            String theMessage = "Public Health Profiles only valid for fhir versions greater than R4";
            theCtx.addValidationMessage(createErrorMessage(theMessage));
            return false;
        }
        return true;
    }

    private SingleValidationMessage createInformationMessage(String messageString) {
        SingleValidationMessage message = new SingleValidationMessage();
        message.setMessage(messageString);
        message.setSeverity(ResultSeverityEnum.INFORMATION);
        return message;
    }

    private SingleValidationMessage createErrorMessage(String theMessage) {
        SingleValidationMessage errorMessage = new SingleValidationMessage();
        errorMessage.setSeverity(ResultSeverityEnum.ERROR);
        errorMessage.setMessage(theMessage);
        return errorMessage;
    }

}
