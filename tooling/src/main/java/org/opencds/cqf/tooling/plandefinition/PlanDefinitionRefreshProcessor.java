package org.opencds.cqf.tooling.plandefinition;

import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.r5.model.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlanDefinitionRefreshProcessor {

   public PlanDefinition refreshPlanDefinition(PlanDefinition planToUse, LibraryManager libraryManager,
                                               CompiledLibrary compiledLibrary, CqlTranslatorOptions options) {
      planToUse.setDate(new Date());
      Set<String> expressions = new HashSet<>();
      if (planToUse.hasAction()) {
         getExpressions(planToUse.getAction(), expressions);
      }
      DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
      Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, compiledLibrary,
              options, expressions, true);

      planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(
              "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter"));
      planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(
              "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement"));
      planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(
              "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode"));
      planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(
              "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition"));
      planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(
              "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-effectiveDataRequirements"));

      for (Extension extension : moduleDefinitionLibrary.getExtension()) {
         if (extension.hasUrl()
                 && extension.getUrl().equals(
                         "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")) {
            continue;
         }
         planToUse.addExtension(extension);
      }

      planToUse.getContained().removeIf(resource -> resource.getId().equalsIgnoreCase("effective-data-requirements"));
//        planToUse.addContained(moduleDefinitionLibrary.setId("effective-data-requirements"));
//        planToUse.addExtension().setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-effectiveDataRequirements").setValue(new Reference("#effective-data-requirements")).setId("effective-data-requirements");
      return planToUse;
   }

   private void getExpressions(List<PlanDefinition.PlanDefinitionActionComponent> actions, Set<String> expressions) {
      for (PlanDefinition.PlanDefinitionActionComponent action : actions) {
         if (action.hasCondition()) {
            for (PlanDefinition.PlanDefinitionActionConditionComponent condition : action.getCondition()) {
               if (condition.hasKind() && condition.getKind() == Enumerations.ActionConditionKind.APPLICABILITY
                       && condition.hasExpression() && isExpressionIdentifier(condition.getExpression())) {
                  expressions.add(condition.getExpression().getExpression());
               }
            }
         }
         if (action.hasDynamicValue()) {
            for (PlanDefinition.PlanDefinitionActionDynamicValueComponent dynamicValue : action.getDynamicValue()) {
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

   private boolean isExpressionIdentifier(Expression expression) {
      return expression.hasLanguage() && expression.hasExpression()
              && (expression.getLanguage().equalsIgnoreCase("text/cql.identifier")
              || expression.getLanguage().equalsIgnoreCase("text/cql"));
   }

}
