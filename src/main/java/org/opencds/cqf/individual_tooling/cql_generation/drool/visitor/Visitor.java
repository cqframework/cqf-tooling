package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

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

public interface Visitor {

	public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts);

	public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts);

	public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO);

	public void visit(OpenCdsConceptDTO openCdsConceptDTO);

	public void visit(DataInputNodeDTO dIN);

	public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO);

	public void visit(ConditionCriteriaPredicatePartDTO predicatePart);

	public void visit(ConditionCriteriaPredicateDTO predicate);

	public void visit(ConditionCriteriaRelDTO conditionCriteriaRel);

	public void visit(CdsCodeDTO cdsCodeDTO);

	public void visit(CriteriaResourceDTO criteriaResourceDTO);

	public void visit(ConditionDTO conditionDTO);

	public void visit(List<ConditionDTO> rootNode);
    
}
