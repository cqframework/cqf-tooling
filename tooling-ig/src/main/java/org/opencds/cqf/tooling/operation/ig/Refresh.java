package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.TerserUtil;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.opencds.cqf.tooling.utilities.constants.CrmiConstants;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Refresh {
   private static final Logger logger = LoggerFactory.getLogger(Refresh.class);
   private final IGInfo igInfo;
   private final FhirContext fhirContext;

   public Refresh(IGInfo igInfo) {
      this.igInfo = igInfo;
      this.fhirContext = igInfo.getFhirContext();
   }

   public abstract List<IBaseResource> refresh();

   public void refreshDate(IBaseResource resource) {
      TerserUtil.clearField(getIgInfo().getFhirContext(), "date", resource);
      TerserUtil.setField(getIgInfo().getFhirContext(), "date", resource,
              ResourceAndTypeConverter.convertType(getFhirContext(), new DateTimeType(new Date())));
   }

    public void refreshVersion(IBaseResource resource, RefreshIGParameters params) {
        if (params != null && params.updatedVersion != null && !params.updatedVersion.isEmpty()) {
            TerserUtil.clearField(getIgInfo().getFhirContext(), "version", resource);
            TerserUtil.setField(getIgInfo().getFhirContext(), "version", resource,
                    ResourceAndTypeConverter.convertType(getFhirContext(), new StringType(params.updatedVersion)));
        }
    }

   public void validatePrimaryLibraryReference(IBaseResource resource) {
      if ((resource instanceof PlanDefinition && !((PlanDefinition) resource).hasLibrary())
              || (resource instanceof Measure && !((Measure) resource).hasLibrary())) {
         String message = resource.fhirType() + " resources must have a Library reference";
         logger.error(message);
         throw new FHIRException(message);
      }
   }

   public boolean isExpressionIdentifier(Expression expression) {
      return expression.hasLanguage() && expression.hasExpression()
              && (expression.getLanguage().equalsIgnoreCase("text/cql.identifier")
              || expression.getLanguage().equalsIgnoreCase("text/cql"));
   }

   public void refreshCqfmExtensions(MetadataResource resource, Library moduleDefinitionLibrary) {
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

   public void attachModuleDefinitionLibrary(MetadataResource resource, Library moduleDefinitionLibrary) {
      resource.getContained().removeIf(res -> res.getId()
              .equalsIgnoreCase("#" + CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
      moduleDefinitionLibrary.setExtension(Collections.emptyList());
      resource.addContained(moduleDefinitionLibrary.setId(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
      ensureSingleEffectiveDataRequirementsReference(resource);
   }

   private void ensureSingleEffectiveDataRequirementsReference(MetadataResource resource) {
      // Remove STU4 extensions
      resource.getExtension().removeIf(ext -> CqfmConstants.EFFECTIVE_DATA_REQS_EXT_URL.equals(ext.getUrl()));

      List<Extension> matchingExtensions = resource.getExtension().stream()
              .filter(ext -> CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL.equals(ext.getUrl()))
              .collect(Collectors.toList());

      if (matchingExtensions.isEmpty()) {
         // Add a new extension
         Extension newExtension = new Extension()
                 .setUrl(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL)
                 .setValue(new CanonicalType("#" + CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
         newExtension.setId(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER);
         resource.addExtension(newExtension);
      } else if (matchingExtensions.size() > 1) {
         // Keep the first, remove the rest
         Extension oneToKeep = matchingExtensions.get(0);
         resource.getExtension()
                 .removeIf(ext ->
                         CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL.equals(ext.getUrl()) && ext != oneToKeep);
      }
   }

   public void addProfiles(MetadataResource resource, String... profiles) {
      if (!resource.hasMeta()) {
         resource.setMeta(new Meta());
      }
      Arrays.stream(profiles).filter(profile -> !resource.getMeta().hasProfile(profile))
              .forEach(profile -> resource.getMeta().addProfile(profile));
   }

   public void cleanModuleDefinitionLibrary(Library moduleDefinitionLibrary) {
      Set<String> pathSet = new HashSet<>();
      moduleDefinitionLibrary.setRelatedArtifact(moduleDefinitionLibrary.getRelatedArtifact().stream()
              .filter(e -> pathSet.add(e.getResource())).collect(Collectors.toList()));
   }

   public List<IBaseResource> getResourcesOfTypeFromDirectory(String resourceType, String directoryPath) {
      Class<? extends IBaseResource> clazz =
              getFhirContext().getResourceDefinition(resourceType).newInstance().getClass();
      IBaseBundle bundle = BundleUtils.getBundleOfResourceTypeFromDirectory(directoryPath, getFhirContext(), clazz);
      return BundleUtil.toListOfResources(getFhirContext(), bundle);
   }

   public IGInfo getIgInfo() {
      return igInfo;
   }

   public FhirContext getFhirContext() {
      return fhirContext;
   }
}
