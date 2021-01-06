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
        DefinitionBlock definitionBlock = (!this.context.definitionBlockStack.isEmpty())
            ? this.context.definitionBlockStack.pop() : new DefinitionBlock(predicate.getDescription() + "-" + predicate.getUuid());

        boolean firstExpression = true;
        if (this.context.expressionStack.size() > 0) {  
            while (this.context.expressionStack.size() > 0) {
                String conjunction = (firstExpression) ? null : predicate.getPredicateConjunction().name();
                definitionBlock.getExpressions().add(Pair.of(conjunction, this.context.expressionStack.pop()));
                firstExpression = false;
            }
        }
        int index = 0;
        if (this.context.referenceStack.size() > 0 && predicate.getPredicateDTOs() != null 
            && !predicate.getPredicateDTOs().isEmpty() && this.context.referenceStack.size() > predicate.getPredicateDTOs().size()) {
            while (index < predicate.getPredicateDTOs().size()) {
                Pair<CriteriaPredicateType, Pair<String, String>> reference = this.context.referenceStack.pop();
                if (firstExpression) {
                    reference = Pair.of(reference.getLeft(), Pair.of(null, reference.getRight().getRight()));
                    firstExpression = false;
                }
                definitionBlock.getReferences().add(reference);
                index++;
            } 
        }
        this.context.printMap.put(definitionBlock.getAlias(), definitionBlock);
        this.context.referenceStack.push(Pair.of(predicate.getPredicateType(), Pair.of(predicate.getPredicateConjunction().name(), definitionBlock.getAlias())));
    }
}
