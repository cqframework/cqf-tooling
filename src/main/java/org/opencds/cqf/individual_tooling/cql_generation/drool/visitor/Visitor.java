package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

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

public interface Visitor {

	public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts, Context context);

	public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts, Context context);

	public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO, Context context);

	public void visit(OpenCdsConceptDTO openCdsConceptDTO, Context context);

	public void visit(DataInputNodeDTO dIN, Context context);

	public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO, Context context);

	public void visit(ConditionCriteriaPredicatePartDTO predicatePart, Context context);

	public void visit(ConditionCriteriaPredicateDTO predicate, Context context);

	public void visit(ConditionCriteriaRelDTO conditionCriteriaRel, Context context);

	public void visit(CdsCodeDTO cdsCodeDTO, Context context);

	public void visit(CriteriaResourceDTO criteriaResourceDTO, Context context);
    
}
