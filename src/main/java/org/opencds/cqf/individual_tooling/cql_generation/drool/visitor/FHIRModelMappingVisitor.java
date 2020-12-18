package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.apache.commons.lang3.tuple.Pair;
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

public class FHIRModelMappingVisitor implements Visitor {

    @Override
    // Retrieve and Left Operand (Modeling)
    public void visit(DataInputNodeDTO dIN, Context context) {
        context.fhirModelingSet.add(Pair.of(dIN.getTemplateName(), dIN.getNodePath()));
    }

    @Override
    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO, Context context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO, Context context) {
        // TODO Auto-generated method stub

    }
}
