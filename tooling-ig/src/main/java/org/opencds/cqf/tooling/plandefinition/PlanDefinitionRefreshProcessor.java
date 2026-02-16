package org.opencds.cqf.tooling.plandefinition;

import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.utilities.constants.CqfConstants;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.opencds.cqf.tooling.utilities.constants.CrmiConstants;

import java.util.*;

public class PlanDefinitionRefreshProcessor {

    public PlanDefinition refreshPlanDefinition(PlanDefinition planToUse, LibraryManager libraryManager,
                                                CompiledLibrary compiledLibrary, CqlCompilerOptions options) {
        planToUse.setDate(new Date());
        var expressions = new HashSet<String>();
        if (planToUse.hasAction()) {
            getExpressions(planToUse.getAction(), expressions);
        }
        var dqReqTrans = new DataRequirementsProcessor();
        var moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, compiledLibrary,
                options, expressions, true);

        // Clear all existing CQFM extensions
        // These extensions are now deprecated, but may be in use for older artifacts
        // May want a configuration point to determine whether to persist these
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfmConstants.PARAMETERS_EXT_URL));
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfmConstants.DATA_REQUIREMENT_EXT_URL));
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfmConstants.DIRECT_REF_CODE_EXT_URL));
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfmConstants.LOGIC_DEFINITION_EXT_URL));
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfmConstants.EFFECTIVE_DATA_REQS_EXT_URL));

        // Clear all existing CQF extensions - include inputParameters (http://hl7.org/fhir/StructureDefinition/cqf-inputParameters)?
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfConstants.DIRECT_REF_CODE_EXT_URL));
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CqfConstants.LOGIC_DEFINITION_EXT_URL));

        // Clear all existing CRMI extensions
        planToUse.getExtension().removeAll(planToUse.getExtensionsByUrl(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL));

        for (var extension : moduleDefinitionLibrary.getExtension()) {
            if (extension.hasUrl() && extension.getUrl().equals(CqfmConstants.DIRECT_REF_CODE_EXT_URL)) {
                // update to use extension pack extension
                extension.setUrl(CqfConstants.DIRECT_REF_CODE_EXT_URL);
            } else if (extension.hasUrl() && extension.getUrl().equals(CqfmConstants.LOGIC_DEFINITION_EXT_URL)) {
                // update to use extension pack extension
                extension.setUrl(CqfConstants.LOGIC_DEFINITION_EXT_URL);
            }
            planToUse.addExtension(extension);
        }

        planToUse.getContained().removeIf(
                resource -> resource.getId().equalsIgnoreCase(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
        moduleDefinitionLibrary.setExtension(Collections.emptyList());
        planToUse.addContained(moduleDefinitionLibrary.setId(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
        planToUse.addExtension().setUrl(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL).setValue(
                new CanonicalType("#" + CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER))
                .setId(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER);
        return planToUse;
    }

    private void getExpressions(List<PlanDefinition.PlanDefinitionActionComponent> actions, Set<String> expressions) {
        actions.forEach(action -> {
            // Process conditions if present
            if (action.hasCondition()) {
                action.getCondition().stream()
                        .filter(condition -> condition.hasKind()
                                && condition.getKind() == Enumerations.ActionConditionKind.APPLICABILITY
                                && condition.hasExpression()
                                && isExpressionIdentifier(condition.getExpression()))
                        .map(condition -> condition.getExpression().getExpression())
                        .forEach(expressions::add);
            }

            // Process dynamic values if present
            if (action.hasDynamicValue()) {
                action.getDynamicValue().stream()
                        .filter(dynamicValue -> dynamicValue.hasExpression()
                                && isExpressionIdentifier(dynamicValue.getExpression()))
                        .map(dynamicValue -> dynamicValue.getExpression().getExpression())
                        .forEach(expressions::add);
            }

            // Recursively process nested actions if present
            if (action.hasAction()) {
                getExpressions(action.getAction(), expressions);
            }
        });
    }

    private boolean isExpressionIdentifier(Expression expression) {
        return expression.hasLanguage() && expression.hasExpression()
                && (expression.getLanguage().equalsIgnoreCase("text/cql.identifier")
                || expression.getLanguage().equalsIgnoreCase("text/cql-identifier")
                || expression.getLanguage().equalsIgnoreCase("text/cql"));
    }

}
