package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.cdsframework.enumeration.PredicatePartType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefinitionBlock;

public class DefinitionBlockVisitor extends ExpressionBodyVisitor {
    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        super.visit(predicatePart);
        DefinitionBlock definitionBlock = new DefinitionBlock();
        if (predicatePart.getPartType() != null && ( predicatePart.getPartType().equals(PredicatePartType.Text) || predicatePart.getPartType().equals(PredicatePartType.ModelElement))) {
            if (predicatePart.getPartAlias() != null) {
                definitionBlock.setAlias(predicatePart.getPartAlias() + "-" + predicatePart.getPartId());
            } else if (predicatePart.getText() != null) {
                definitionBlock.setAlias(predicatePart.getText() + "-" + predicatePart.getPartId());
            }
            this.context.definitionBlockStack.push(definitionBlock);
        }
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        super.visit(predicate);
        DefinitionBlock definitionBlock = null;
        if (!this.context.definitionBlockStack.isEmpty()) {
            definitionBlock = this.context.definitionBlockStack.pop();
        } else {
            definitionBlock = new DefinitionBlock();
            definitionBlock.setAlias(predicate.getDescription() + "-" + predicate.getUuid());
        }
        boolean firstExpression = true;
        if (this.context.expressionStack.size() > 0) {  
            do {
                if (firstExpression) {
                    definitionBlock.getExpressions().add(Pair.of(null, this.context.expressionStack.pop()));
                    firstExpression = false;
                } else {
                    definitionBlock.getExpressions().add(Pair.of(predicate.getPredicateConjunction().name(), this.context.expressionStack.pop()));
                }
            } while (this.context.expressionStack.size() > 0);
        }
        int index = 0;
        if (this.context.referenceStack.size() > 0 && predicate.getPredicateDTOs() != null 
            && !predicate.getPredicateDTOs().isEmpty() && this.context.referenceStack.size() > predicate.getPredicateDTOs().size()) {
            do {
                if (firstExpression) {
                    Pair<CriteriaPredicateType, Pair<String, String>> reference = this.context.referenceStack.pop();
                    Pair<String, String> aliasContext = reference.getRight();
                    definitionBlock.getReferences().add(Pair.of(reference.getLeft(), Pair.of(null, aliasContext.getRight())));
                    firstExpression = false;
                } else {
                    definitionBlock.getReferences().add(this.context.referenceStack.pop());
                }
                index++;
            } while (index < predicate.getPredicateDTOs().size());
        }
        this.context.printMap.put(definitionBlock.getAlias(), definitionBlock);
        this.context.referenceStack.push(Pair.of(predicate.getPredicateType(), Pair.of(predicate.getPredicateConjunction().name(), definitionBlock.getAlias())));
    } 
}
