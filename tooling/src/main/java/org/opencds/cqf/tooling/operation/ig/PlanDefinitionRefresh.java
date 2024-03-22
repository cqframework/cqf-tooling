package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.util.BundleUtil;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.cqframework.fhir.npm.NpmPackageManager;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlanDefinitionRefresh extends Refresh {
   private static final Logger logger = LoggerFactory.getLogger(PlanDefinitionRefresh.class);
   private final CqlProcessor cqlProcessor;
   private final List<LibraryPackage> libraryPackages;
   private final List<PlanDefinitionPackage> planDefinitionPackages;

   public PlanDefinitionRefresh(IGInfo igInfo, CqlProcessor cqlProcessor, List<LibraryPackage> libraryPackages) {
      super(igInfo);
      this.cqlProcessor = cqlProcessor;
      this.libraryPackages = libraryPackages;
      this.planDefinitionPackages = new ArrayList<>();
   }

   @Override
   public List<IBaseResource> refresh() {
      List<IBaseResource> refreshedPlanDefinitions = new ArrayList<>();

      if (getIgInfo().isRefreshPlanDefinitions()) {
         logger.info("Refreshing PlanDefinitions...");

         if (cqlProcessor.getFileMap() == null) {
            cqlProcessor.execute();
         }

         DataRequirementsProcessor dataRecProc = new DataRequirementsProcessor();
         Class<? extends IBaseResource> clazz = getFhirContext().getResourceDefinition(
                 "PlanDefinition").newInstance().getClass();
         IBaseBundle bundle = BundleUtils.getBundleOfResourceTypeFromDirectory(
                 getIgInfo().getPlanDefinitionResourcePath(), getFhirContext(), clazz);

         for (var resource : BundleUtil.toListOfResources(getFhirContext(), bundle)) {
            PlanDefinition planDefinition = (PlanDefinition) ResourceAndTypeConverter.convertToR5Resource(
                    getFhirContext(), resource);

            logger.info("Refreshing {}", planDefinition.getId());

            validatePrimaryLibraryReference(planDefinition);
            String libraryUrl = planDefinition.getLibrary().get(0).getValueAsString();
            LibraryPackage libraryPackage = libraryPackages.stream().filter(
                    pkg -> libraryUrl.endsWith(pkg.getCqlFileInfo().getIdentifier().getId()))
                    .findFirst().orElse(null);
            for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
               if (libraryUrl.endsWith(info.getIdentifier().getId())) {
                  // TODO: should likely verify or resolve/refresh the following elements:
                  //  cpg-knowledgeCapability, cpg-knowledgeRepresentationLevel, url, identifier, status,
                  //  experimental, type, publisher, contact, description, useContext, jurisdiction,
                  //  and profile(s) (http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-shareableplandefinition)
                  planDefinition.setDate(new Date());
                  Library moduleDefinitionLibrary = getModuleDefinitionLibrary(
                          planDefinition, dataRecProc, info);
                  cleanModuleDefinitionLibrary(moduleDefinitionLibrary);
                  refreshCqfmExtensions(planDefinition, moduleDefinitionLibrary);
                  attachModuleDefinitionLibrary(planDefinition, moduleDefinitionLibrary);
                  IBaseResource refreshedPlanDefinition = ResourceAndTypeConverter.convertFromR5Resource(getFhirContext(), planDefinition);
                  refreshedPlanDefinitions.add(refreshedPlanDefinition);
                  this.planDefinitionPackages.add(new PlanDefinitionPackage(planDefinition, refreshedPlanDefinition, getFhirContext(), libraryPackage));
               }
            }

            logger.info("Success!");
         }
         resolvePlanDefinitionPackages();
      }
      return refreshedPlanDefinitions;
   }

   private List<IBaseResource> activityDefinitions;
   private List<IBaseResource> questionnaires;
   private void resolvePlanDefinitionPackages() {
      // TODO: only resolving definition resources from source IG - enhance to resolve from NPM package.
      //  Additionally need to resolve nested PlanDefinitions
      activityDefinitions = BundleUtil.toListOfResources(getFhirContext(),
              BundleUtils.getBundleOfResourceTypeFromDirectory(getIgInfo().getActivityDefinitionResourcePath(),
                      getFhirContext(), getFhirContext().getResourceDefinition("ActivityDefinition")
                              .newInstance().getClass()));
      questionnaires = BundleUtil.toListOfResources(getFhirContext(),
              BundleUtils.getBundleOfResourceTypeFromDirectory(getIgInfo().getActivityDefinitionResourcePath(),
                      getFhirContext(), getFhirContext().getResourceDefinition("Questionnaire")
                              .newInstance().getClass()));
      this.planDefinitionPackages.forEach(
              pkg -> pkg.getR5PlanDefinition().getAction().forEach(action -> resolveAction(action, pkg))
      );
   }

   private void resolveAction(PlanDefinition.PlanDefinitionActionComponent action, PlanDefinitionPackage pkg) {
      final IdDt definitionRef;
      if (action.hasDefinitionCanonicalType()) {
         definitionRef = new IdDt(action.getDefinitionCanonicalType().getValueAsString());
      }
      else if (action.hasDefinitionUriType()) {
         definitionRef = new IdDt(action.getDefinitionUriType().getValueAsString());
      }
      else {
         definitionRef = null;
      }
      if (definitionRef != null && definitionRef.hasResourceType()) {
         if (definitionRef.getResourceType().equals("ActivityDefinition")) {
            pkg.addActivityDefinition(
                    activityDefinitions.stream().filter(ad -> ad.getIdElement().getIdPart()
                            .equals(definitionRef.getIdPart())).findFirst().orElse(null)
            );

         }
         else if (definitionRef.getResourceType().equals("Questionnaire")) {
            pkg.addQuestionnaire(
                    questionnaires.stream().filter(q -> q.getIdElement().getIdPart()
                            .equals(definitionRef.getIdPart())).findFirst().orElse(null)
            );
         }
         else {
            logger.warn("Definitions of type {} are not currently supported", definitionRef.getResourceType());
         }
      }
      if (action.hasAction()) {
         action.getAction().forEach(nextAction -> resolveAction(nextAction, pkg));
      }
   }

   public void bundleResources(NpmPackageManager npmPackageManager, List<IBaseResource> resources) {

   }

   private Library getModuleDefinitionLibrary(PlanDefinition planDefinition, DataRequirementsProcessor dataRecProc,
                                              CqlProcessor.CqlSourceFileInformation info) {
      // TODO: do we still need this?
      Set<String> expressions = new HashSet<>();
      if (planDefinition.hasAction()) {
         getExpressions(planDefinition.getAction(), expressions);
      }
      return dataRecProc.gatherDataRequirements(
              cqlProcessor.getLibraryManager(),
              cqlProcessor.getLibraryManager().resolveLibrary(
                      info.getIdentifier(), new ArrayList<>()),
              info.getOptions().getCqlCompilerOptions(), expressions, true);
   }

   private void getExpressions(List<PlanDefinition.PlanDefinitionActionComponent> actions, Set<String> expressions) {
      for (var action : actions) {
         if (action.hasCondition()) {
            for (var condition : action.getCondition()) {
               if (condition.hasKind() && condition.getKind() == Enumerations.ActionConditionKind.APPLICABILITY
                       && condition.hasExpression() && isExpressionIdentifier(condition.getExpression())) {
                  expressions.add(condition.getExpression().getExpression());
               }
            }
         }
         if (action.hasDynamicValue()) {
            for (var dynamicValue : action.getDynamicValue()) {
               if (dynamicValue.hasExpression() && isExpressionIdentifier(dynamicValue.getExpression())) {
                  expressions.add(dynamicValue.getExpression().getExpression());
               }
            }
         }
         if (action.hasAction()) {
            getExpressions(action.getAction(), expressions);
         }
      }
   }

   public List<PlanDefinitionPackage> getPlanDefinitionPackages() {
      return planDefinitionPackages;
   }
}
