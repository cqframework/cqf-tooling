package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.util.Map;

import com.google.common.base.Strings;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.And;
import org.hl7.elm.r1.BinaryExpression;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.Or;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;

/**
 * Provides adapter functionality for building an Expression Definition and Reference Graph
 * using the ElmContext to flush out a stack of Expressions
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DefinitionAdapter {
    private String identifier;
    private String expressionContext = "Patient";

    /**
     * If the PredicatePartType of the {@link ConditionCriteriaPredicatePartDTO predicatePart} is ModelElement or Text
     * infers the ExpressionDefinition identifier from either the partAlias, text, or nodeLabel
     * and initializes context in the LibraryBuilder
     * @param predicatePart predicatePart
     * @param context elmContext
     */
    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, ElmContext context) {
        switch (predicatePart.getPartType()) {
            case DataInput:
                break;
            case ModelElement: 
                inferIdentifier(predicatePart); 
                initializeContext(context);
                break;
            case Resource:
                break;
            case Text: 
                inferIdentifier(predicatePart); 
                initializeContext(context);
                break;
            default:
                break;
        }
    }

    /**
     * If there is no identifier, interrogate {@link ConditionCriteriaPredicateDTO predicate} 
     * for description as identifier and initializes context
     * includes all Expressions in the expression stack and includes a number of Expression
     * References in the reference stack equal to the number of Predicate entries in the predicateDTOs
     * finalizes context
     * @param predicate predicate
     * @param context elmContext
     */
    public void adapt(ConditionCriteriaPredicateDTO predicate, ElmContext context) {
        if (Strings.isNullOrEmpty(identifier)) {
            identifier = predicate.getDescription() + "-" + predicate.getUuid();
            initializeContext(context);
        }
        ExpressionDef expressionDef = includeDefinitions(predicate.getPredicateConjunction().name(), context);
        expressionDef = includeReferences(predicate.getPredicateDTOs().size(), context, expressionDef);
        if (expressionDef != null) {
            finalizeContext(predicate.getPredicateConjunction().name(), context, expressionDef.getName());
        }
    }

    public void conditionCriteriaMetExpression(ElmContext context) {
        if (context.referenceStack.size() > 0) {
            identifier = "ConditionCriteriaMet";
            initializeContext(context);
            ExpressionDef expressionDef = includeDefinitions(null, context);
            expressionDef = includeReferences(context.referenceStack.size(), context, expressionDef);
            finalizeContext(null, context, expressionDef.getName());
        } else {
            System.out.println("No Reference in Library");
        }
    }

    private void inferIdentifier(ConditionCriteriaPredicatePartDTO predicatePart) {
        String alias = predicatePart.getPartAlias();
        alias = (Strings.isNullOrEmpty(alias)) ? predicatePart.getText() : alias;
        alias = (Strings.isNullOrEmpty(alias)) ? predicatePart.getNodeLabel() : alias;
        String id = predicatePart.getPartId();
        if (alias != null && id != null) {
            identifier = alias + "-" + id;
        } else {
            throw new RuntimeException("No Inferred Alias found for " + predicatePart.getUuid());
        }
    }

    private void initializeContext(ElmContext context) {
        ExpressionDef def = context.libraryBuilder.resolveExpressionRef(identifier);
        if (def == null || isImplicitContextExpressionDef(context, def)) {
            if (def != null && isImplicitContextExpressionDef(context, def)) {
                context.libraryBuilder.removeExpression(def);
                removeImplicitContextExpressionDef(context, def);
                def = null;
            }
            context.libraryBuilder.pushExpressionContext(expressionContext);
            context.libraryBuilder.currentExpressionContext();
            try {
                context.libraryBuilder.pushExpressionDefinition(identifier);
                context.libraryBuilder.resolveExpressionRef(identifier);
            } finally {
                context.libraryBuilder.popExpressionContext();
            }
        }
    }

    private ExpressionDef includeReferences(Integer actualListSize, ElmContext context,
            ExpressionDef expressionDef) {
        if (!context.referenceStack.isEmpty() && actualListSize != null
                && context.referenceStack.size() >= actualListSize.intValue()) {
            int index = 0;
            while (index < actualListSize.intValue()) {
                Pair<String, ExpressionRef> reference = context.referenceStack.pop();
                if (expressionDef == null) {
                    expressionDef = adaptExpressionDef(context, reference.getRight());
                } else {
                    buildBinaryFromConjunction(reference.getLeft(), context, expressionDef, reference.getRight());
                }
                index++;
            }
        }
        return expressionDef;
    }

    private ExpressionDef includeDefinitions(String conjunction, ElmContext context) {
        ExpressionDef expressionDef = null;
        if (!context.expressionStack.isEmpty()) {
            while (!context.expressionStack.isEmpty()) {
                Expression expression = context.expressionStack.pop();
                if (expressionDef == null) {
                    expressionDef = adaptExpressionDef(context, expression);
                } else {
                    buildBinaryFromConjunction(conjunction, context, expressionDef,
                            expression);
                }
            }
        }
        return expressionDef;
    }
    
    private ExpressionDef adaptExpressionDef(ElmContext context, Expression expression) {
        ExpressionDef def = context.libraryBuilder.resolveExpressionRef(identifier);
        if (def == null) {
            def = context.modelBuilder.of.createExpressionDef()
                    .withAccessLevel(AccessModifier.PUBLIC)
                    .withName(this.identifier)
                    .withContext(this.expressionContext)
                    .withExpression(expression);
            if (def.getExpression() != null) {
                def.setResultType(def.getExpression().getResultType());
            }
            context.libraryBuilder.addExpression(def);
        } else {
            def.setExpression(expression);
            if (def.getExpression() != null) {
                def.setResultType(def.getExpression().getResultType());
            }
        }
        return def;
    }

    private void finalizeContext(String conjunction, ElmContext context, String expressionName) {
        context.libraryBuilder.popExpressionDefinition();
        ExpressionRef expressionRef = context.modelBuilder.of.createExpressionRef().withName(expressionName);
        if (conjunction != null) {
            context.referenceStack.push(Pair.of(conjunction, expressionRef));
        }
        identifier = null;
    }

    private void buildBinaryFromConjunction(String conjunction, ElmContext context,
            ExpressionDef expressionDef, Expression expression) {
        BinaryExpression binary; 
        switch (conjunction.toLowerCase()) {
            case "or": {
                binary = new Or().withOperand(expressionDef.getExpression()).withOperand(expression);
                break;
            }
            case "and": {
                binary = new And().withOperand(expressionDef.getExpression()).withOperand(expression);
                break;
            }
            default: throw new RuntimeException("Unkown conjunction " + conjunction);
        }
        expressionDef.setExpression(binary);
    }

    private boolean isImplicitContextExpressionDef(ElmContext context, ExpressionDef def) {
        for (Element e : context.contextDefinitions.values()) {
            if (def == e) {
                return true;
            }
        }

        return false;
    }

    private void removeImplicitContextExpressionDef(ElmContext context, ExpressionDef def) {
        for (Map.Entry<String, Element> e : context.contextDefinitions.entrySet()) {
            if (def == e.getValue()) {
                context.contextDefinitions.remove(e.getKey());
                break;
            }
        }
    }
}
