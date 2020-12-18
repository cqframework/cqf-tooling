package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.enumeration.PredicatePartType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefinitionBlock;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class DefinitionBlockVisitor extends ExpressionBodyVisitor {

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart, Context context) {
        super.visit(predicatePart, context);
        DefinitionBlock definitionBlock = new DefinitionBlock();
        if (predicatePart.getPartType() != null && ( predicatePart.getPartType().equals(PredicatePartType.Text) || predicatePart.getPartType().equals(PredicatePartType.ModelElement))) {
            if (predicatePart.getPartAlias() != null) {
                definitionBlock.setAlias(predicatePart.getPartAlias() + "-" + predicatePart.getPartId());
            } else if (predicatePart.getText() != null) {
                definitionBlock.setAlias(predicatePart.getText() + "-" + predicatePart.getPartId());
            }
            context.definitionBlockStack.push(definitionBlock);
        }
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate, Context context) {
        super.visit(predicate, context);
        DefinitionBlock definitionBlock = null;
        if (!context.definitionBlockStack.isEmpty()) {
            definitionBlock = context.definitionBlockStack.pop();
        } else {
            definitionBlock = new DefinitionBlock();
            definitionBlock.setAlias(predicate.getDescription() + "-" + predicate.getUuid());
        }
        boolean firstExpression = true;
        if (context.expressionStack.size() > 0) {  
            do {
                if (firstExpression) {
                    definitionBlock.getExpressions().add(Pair.of(null, context.expressionStack.pop()));
                    firstExpression = false;
                } else {
                    definitionBlock.getExpressions().add(Pair.of(predicate.getPredicateConjunction().name(), context.expressionStack.pop()));
                }
            } while (context.expressionStack.size() > 0);
        }
        int index = 0;
        if (context.referenceStack.size() > 0 && predicate.getPredicateDTOs() != null 
            && !predicate.getPredicateDTOs().isEmpty() && context.referenceStack.size() > predicate.getPredicateDTOs().size()) {
            do {
                if (firstExpression) {
                    definitionBlock.getReferences().add(Pair.of(null, context.referenceStack.pop().getRight()));
                    firstExpression = false;
                } else {
                    definitionBlock.getReferences().add(context.referenceStack.pop());
                }
                index++;
            } while (index < predicate.getPredicateDTOs().size());
        }
        context.printMap.put(definitionBlock.getAlias(), definitionBlock);
        context.referenceStack.push(Pair.of(predicate.getPredicateConjunction().name(), definitionBlock.getAlias()));
    } 
}
