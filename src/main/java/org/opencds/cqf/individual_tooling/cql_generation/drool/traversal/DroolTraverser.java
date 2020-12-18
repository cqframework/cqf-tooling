package org.opencds.cqf.individual_tooling.cql_generation.drool.traversal;

import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.CriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public abstract class DroolTraverser<T> {
    protected Context context;
    protected T visitor;

    public DroolTraverser(T visitor) {
        this.context = new Context();
        this.visitor = visitor;
    }

	public abstract void traverse(ConditionCriteriaRelDTO conditionCriteriaRel);

    protected abstract void traverse(ConditionCriteriaPredicateDTO predicate);

    public abstract void traverse(ConditionCriteriaPredicatePartDTO predicatePart);

    // Operator
    protected abstract void traverse(CriteriaResourceParamDTO criteriaResourceParamDTO);

    // Retrieve and Left Operand (Modeling)
    protected abstract void traverse(DataInputNodeDTO dIN);

    // Right operand (Terminology)
    protected abstract void traverse(OpenCdsConceptDTO openCdsConceptDTO);

    protected abstract void traverse(CriteriaPredicatePartDTO  sourcePredicatePartDTO);

    protected abstract void traverse(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts);

    protected abstract void traverse(CriteriaPredicatePartConceptDTO predicatePartConcepts);

    protected abstract void traverse(CdsCodeDTO cdsCodeDTO);

    protected abstract void traverse(CriteriaResourceDTO criteriaResourceDTO);

    public Context getContext() {
        return context;
    }

    public DroolTraverser<T> withContext(Context context) {
        this.context = context;
        return this;
    }

    public void setDefaultContext() {
        this.context = new Context();
    }
    
}
