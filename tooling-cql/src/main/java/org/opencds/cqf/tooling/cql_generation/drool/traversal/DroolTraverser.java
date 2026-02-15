package org.opencds.cqf.tooling.cql_generation.drool.traversal;

import java.util.List;

import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.cdsframework.dto.CriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;


/**
 * Traversal Abstraction for RCKMS Drool Object Graph
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public abstract class DroolTraverser<T> {
    protected T visitor;

    public DroolTraverser(T visitor) {
        this.visitor = visitor;
    }

    public abstract ElmContext traverse(List<ConditionDTO> rootNode);

    protected abstract void traverse(ConditionDTO conditionDTO);

	protected abstract void traverse(ConditionCriteriaRelDTO conditionCriteriaRel);

    protected abstract void traverse(ConditionCriteriaPredicateDTO predicate);

    protected abstract void traverse(ConditionCriteriaPredicatePartDTO predicatePart);

    // Operator
    protected abstract void traverse(CriteriaResourceParamDTO criteriaResourceParamDTO);

    // Retrieve and Left Operand (Modeling)
    protected abstract void traverse(DataInputNodeDTO dIN);

    // Right operand (Terminology)
    protected abstract void traverse(OpenCdsConceptDTO openCdsConceptDTO);

    protected abstract void traverse(CriteriaPredicatePartDTO  sourcePredicatePartDTO);

    protected abstract void traverse(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts);

    protected abstract void traverse(CriteriaPredicatePartConceptDTO predicatePartConcepts);

    // Right operand (Terminology)
    protected abstract void traverse(CdsCodeDTO cdsCodeDTO);

    // Operator
    protected abstract void traverse(CriteriaResourceDTO criteriaResourceDTO);
}
