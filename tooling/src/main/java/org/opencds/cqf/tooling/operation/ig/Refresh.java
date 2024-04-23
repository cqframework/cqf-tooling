package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.TerserUtil;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
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
      TerserUtil.setField(getIgInfo().getFhirContext(), "date", resource,
              ResourceAndTypeConverter.convertType(getFhirContext(), new DateTimeType(new Date())));
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
      String effectiveDataReq = "effective-data-requirements";
      resource.getContained().removeIf(
              res -> res.getId().equalsIgnoreCase("#" + effectiveDataReq));
      moduleDefinitionLibrary.setExtension(Collections.emptyList());
      resource.addContained(moduleDefinitionLibrary.setId(effectiveDataReq));
      resource.addExtension()
              .setUrl(CqfmConstants.EFFECTIVE_DATA_REQS_EXT_URL)
              .setValue(new Reference("#" + effectiveDataReq)).setId(effectiveDataReq);
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
