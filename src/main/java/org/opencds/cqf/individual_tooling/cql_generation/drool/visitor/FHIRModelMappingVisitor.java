package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
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
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class FHIRModelMappingVisitor implements Visitor {

    private Context context = new Context();

    @Override
    // Retrieve and Left Operand (Modeling)
    public void visit(DataInputNodeDTO dIN) {
        context.fhirModelingSet.add(Pair.of(dIN.getTemplateName(), dIN.getNodePath()));
    }

    @Override
    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        context.writeFHIRModelMapping();
    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionDTO conditionDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(List<ConditionDTO> rootNode) {
        // TODO Auto-generated method stub

    }
}
