package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.hl7.cql_annotations.r1.Annotation;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.ValueSetDef;
import org.opencds.cqf.individual_tooling.cql_generation.context.CqlContext;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.context.FHIRContext;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.ExpressionBodyAdapter;

public class DroolToElmVisitor implements Visitor {
    private ElmContext context = new ElmContext();
    private ExpressionBodyAdapter expressionBodyAdapter = new ExpressionBodyAdapter();

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
        expressionBodyAdapter.adapt(sourcePredicatePartDTO, context);
    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        expressionBodyAdapter.adapt(openCdsConceptDTO, context);
    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        expressionBodyAdapter.adapt(dIN, context);
    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        expressionBodyAdapter.adapt(criteriaResourceParamDTO, context);
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        expressionBodyAdapter.adapt(predicatePart, context);
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        expressionBodyAdapter.adapt(cdsCodeDTO, context);
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
