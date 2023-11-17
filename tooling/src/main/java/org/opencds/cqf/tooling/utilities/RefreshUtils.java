package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.TerserUtil;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RefreshUtils {
    private static final Logger logger = LoggerFactory.getLogger(RefreshUtils.class);

    public abstract List<IBaseResource> refresh();

    public static void refreshDate(FhirContext fhirContext, IBaseResource resource) {
        IBaseDatatype datatype = fhirContext.getVersion().getVersion() == FhirVersionEnum.R5 ? new DateTimeType(new Date()) : new org.hl7.fhir.r4.model.DateTimeType(new Date());
        TerserUtil.setFieldByFhirPath(fhirContext, "date", resource, datatype);
    }

    public static void validatePrimaryLibraryReference(IBaseResource resource) {
        if ((resource instanceof PlanDefinition && !((PlanDefinition) resource).hasLibrary())
                || (resource instanceof Measure && !((Measure) resource).hasLibrary())) {
            String message = resource.fhirType() + " resources must have a Library reference";
            logger.error(message);
            throw new FHIRException(message);
        }
    }

    public static boolean isExpressionIdentifier(Expression expression) {
        return expression.hasLanguage() && expression.hasExpression()
                && (expression.getLanguage().equalsIgnoreCase("text/cql.identifier")
                || expression.getLanguage().equalsIgnoreCase("text/cql"));
    }

    public static void refreshCqfmExtensions(MetadataResource resource, Library moduleDefinitionLibrary) {
        resource.getExtension().removeAll(resource.getExtensionsByUrl(CqfmConstants.PARAMETERS_EXT_URL));
        resource.getExtension().removeAll(resource.getExtensionsByUrl(CqfmConstants.DATA_REQUIREMENT_EXT_URL));
        resource.getExtension().removeAll(resource.getExtensionsByUrl(CqfmConstants.DIRECT_REF_CODE_EXT_URL));
        resource.getExtension().removeAll(resource.getExtensionsByUrl(CqfmConstants.LOGIC_DEFINITION_EXT_URL));
        resource.getExtension().removeAll(resource.getExtensionsByUrl(CqfmConstants.EFFECTIVE_DATA_REQS_EXT_URL));

        for (Extension extension : moduleDefinitionLibrary.getExtension()) {
            if (extension.hasUrl() && extension.getUrl().equals(CqfmConstants.DIRECT_REF_CODE_EXT_URL)) {
                continue;
            }
            resource.addExtension(extension);
        }
    }

    public static void attachModuleDefinitionLibrary(MetadataResource resource, Library moduleDefinitionLibrary) {
        String effectiveDataReq = "effective-data-requirements";
        resource.getContained().removeIf(
                res -> res.getId().equalsIgnoreCase("#" + effectiveDataReq));
        moduleDefinitionLibrary.setExtension(Collections.emptyList());
        resource.addContained(moduleDefinitionLibrary.setId(effectiveDataReq));
        resource.addExtension()
                .setUrl(CqfmConstants.EFFECTIVE_DATA_REQS_EXT_URL)
                .setValue(new Reference("#" + effectiveDataReq)).setId(effectiveDataReq);
    }

    public static void addProfiles(MetadataResource resource, String... profiles) {
        if (!resource.hasMeta()) {
            resource.setMeta(new Meta());
        }
        Arrays.stream(profiles).filter(profile -> !resource.getMeta().hasProfile(profile))
                .forEach(profile -> resource.getMeta().addProfile(profile));
    }

    public static void cleanModuleDefinitionLibrary(Library moduleDefinitionLibrary) {
        Set<String> pathSet = new HashSet<>();
        moduleDefinitionLibrary.setRelatedArtifact(moduleDefinitionLibrary.getRelatedArtifact().stream()
                .filter(e -> pathSet.add(e.getResource())).collect(Collectors.toList()));
    }

    public static List<IBaseResource> getResourcesOfTypeFromDirectory(FhirContext fhirContext, String resourceType, String directoryPath) {
        Class<? extends IBaseResource> clazz =
                fhirContext.getResourceDefinition(resourceType).newInstance().getClass();
        IBaseBundle bundle = BundleUtils.getBundleOfResourceTypeFromDirectory(directoryPath, fhirContext, clazz);
        return BundleUtil.toListOfResources(fhirContext, bundle);
    }

}
