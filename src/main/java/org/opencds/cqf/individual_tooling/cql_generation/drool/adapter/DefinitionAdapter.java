package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.cdsframework.enumeration.PredicatePartType;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.And;
import org.hl7.elm.r1.BinaryExpression;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.ObjectFactory;
import org.hl7.elm.r1.Or;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;

public class DefinitionAdapter {
    private String identifier;
    private String expressionContext;
    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, ElmContext context) {
        if (predicatePart.getPartType() != null && (predicatePart.getPartType().equals(PredicatePartType.Text)
                || predicatePart.getPartType().equals(PredicatePartType.ModelElement))) {
            if (predicatePart.getPartAlias() != null) {
                identifier = predicatePart.getPartAlias() + "-" + predicatePart.getPartId();
                expressionContext = "Patient";
                initializeContext(context, expressionContext);
            } else if (predicatePart.getText() != null) {
                identifier = predicatePart.getText() + "-" + predicatePart.getPartId();
                expressionContext = "Patient";
                initializeContext(context, expressionContext);
            } else if (predicatePart.getNodeLabel() != null) {
                identifier = predicatePart.getNodeLabel() + "-" + predicatePart.getPartId();
                expressionContext = "Patient";
                initializeContext(context, expressionContext);
            } else {
                System.out.println("No Inferred Alias found for " + predicatePart.getUuid());
            }
        }
    }

    private void initializeContext(ElmContext context, String expressionContext) {
        ExpressionDef def = context.libraryBuilder.resolveExpressionRef(identifier);
        if (def == null || isImplicitContextExpressionDef(context, def)) {
            if (def != null && isImplicitContextExpressionDef(context, def)) {
                context.libraryBuilder.removeExpression(def);
                removeImplicitContextExpressionDef(context, def);
                def = null;
            }
            context.libraryBuilder.pushExpressionContext(expressionContext);
            try {
                context.libraryBuilder.pushExpressionDefinition(identifier);
            } finally {
                    context.libraryBuilder.popExpressionContext();
                }
            }
        }
    
    private ExpressionDef adaptExpressionDef(ConditionCriteriaPredicateDTO predicate, ElmContext context,
            String identifier, String expressionContext, Expression expression) {
        ExpressionDef def = context.libraryBuilder.resolveExpressionRef(identifier);
        if (def == null) {
            def = context.of.createExpressionDef()
                    .withAccessLevel(AccessModifier.PUBLIC)
                    .withName(identifier)
                    .withContext(expressionContext)
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

    public void adapt(ConditionCriteriaPredicateDTO predicate, ElmContext context) {
        if (identifier == null) {
            identifier = predicate.getDescription() + "-" + predicate.getUuid();
            initializeContext(context, "Patient");
        }
        ExpressionDef expressionDef = null;
        if (context.expressionStack.size() > 0) {
            while (context.expressionStack.size() > 0) {
                Expression expression = context.expressionStack.pop();
                if (expressionDef == null) {
                    expressionDef = adaptExpressionDef(predicate, context, identifier, expressionContext, expression);
                } else {
                    buildBinaryFromConjunction(predicate.getPredicateConjunction().name(), context, expressionDef,
                            expression);
                }
            }
        }
        int index = 0;
        List<ConditionCriteriaPredicateDTO> dtos = predicate.getPredicateDTOs();
        if (context.referenceStack.size() > 0 && dtos != null
                && !dtos.isEmpty() && context.referenceStack.size() >= dtos.size()) {
            while (index < dtos.size()) {
                Pair<String, ExpressionRef> reference = context.referenceStack.pop();
                if (expressionDef == null) {
                    expressionDef = adaptExpressionDef(predicate, context, identifier, expressionContext, reference.getRight());
                } else {
                    buildBinaryFromConjunction(reference.getLeft(), context, expressionDef, reference.getRight());
                }
                index++;
            }
        }
        context.libraryBuilder.popExpressionDefinition();
        ExpressionRef expressionRef = context.of.createExpressionRef().withName(expressionDef.getName());
        context.referenceStack.push(Pair.of(predicate.getPredicateConjunction().name(), expressionRef));
        identifier = null;
    }

    public void adapt(ConditionCriteriaRelDTO conditionCriteriaRel, ElmContext context) {
        ExpressionDef expressionDef = new ExpressionDef().withContext("Patient").withAccessLevel(AccessModifier.PUBLIC)
            .withName("ConditionCriteriaMet");
        List<ConditionCriteriaRelDTO> dtos = new ArrayList<ConditionCriteriaRelDTO>();
        dtos.add(conditionCriteriaRel);
        resolveReferences(context, expressionDef, dtos);
    }

    public void adapt(ConditionDTO conditionDTO, ElmContext context) {
        ExpressionDef expressionDef = new ExpressionDef().withContext("Patient").withAccessLevel(AccessModifier.PUBLIC)
            .withName("ConditionCriteriaMet");
        List<ConditionCriteriaRelDTO> dtos = conditionDTO.getConditionCriteriaRelDTOs();
        resolveReferences(context, expressionDef, dtos);
    }

    private void resolveReferences(ElmContext context, ExpressionDef expressionDef, List<ConditionCriteriaRelDTO> dtos) {
        int index = 0;
        boolean firstExpression = true;
        if (context.referenceStack.size() > 0 &&  dtos != null 
            && !dtos.isEmpty() && context.referenceStack.size() >= dtos.size()) {
            while (index <= dtos.size() && index <= context.referenceStack.size()) {
                Pair<String, ExpressionRef> reference = context.referenceStack.pop();
                if (firstExpression) {
                    expressionDef.setExpression(reference.getRight());
                    firstExpression = false;
                } else {
                    buildBinaryFromConjunction(reference.getLeft(), context, expressionDef, reference.getRight());
                }
                index++;
            } 
        }
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
}
