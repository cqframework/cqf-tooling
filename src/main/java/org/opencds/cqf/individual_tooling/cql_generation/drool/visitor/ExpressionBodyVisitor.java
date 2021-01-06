package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

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
import org.cdsframework.enumeration.DataModelClassType;
import org.cdsframework.enumeration.PredicatePartType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.ValueSet;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Expression;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;
import org.opencds.cqf.individual_tooling.cql_generation.context.FHIRContext;

public class ExpressionBodyVisitor implements Visitor {
    protected Context context = new Context();

    

    @Override
    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts) {

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts) {

    }

    @Override
    public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO) {
        if (sourcePredicatePartDTO.getPartType().equals(PredicatePartType.Text)) {
            if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                Expression expression = new Expression();
                expression.setLeft("AgeInYearsAt(Today())");
                context.expressionStack.push(expression);
            } else if (sourcePredicatePartDTO.getText().equals("Number:")) {
                // System.out.println("skip, Number:");
            } else if (sourcePredicatePartDTO.getText().equals("Units:")) {
                // System.out.println("skip, Units:");
                // TODO: create a UnitCodeSystem and DirectReferenceCode for the Unit
            }
        }

    }

    // right operand
    // "ValueSet"
    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        Expression expression = context.expressionStack.pop();
        if (openCdsConceptDTO.getCode() != null && openCdsConceptDTO.getDisplayName() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            Map<String,Pair<String, String>> valueSetMap = FHIRContext.readValueSetMapping();
            String display = "";
            String url = "";
            if (valueSetMap != null && valueSetMap.size() > 0) {
                Pair<String, String> displayUrlPair = valueSetMap.get(openCdsConceptDTO.getCode());
                url = displayUrlPair.getRight();
                display = displayUrlPair.getLeft();
            }
            ValueSet valueset = new ValueSet(display, url);
            context.printMap.put(valueset.getAlias(), valueset);
            expression.setConcept(valueset.getAlias());
        }
        context.expressionStack.push(expression);
    }

    // left operand
    // Observation.value as CodeableConcept
    @Override
    public void visit(DataInputNodeDTO dIN) {
        Expression expression = new Expression();
        Pair<String, String> fhirModeling = FHIRContext.cdsdmToFhirMap
                .get(dIN.getTemplateName() + "." + dIN.getNodePath().replaceAll("/", "."));
        if (fhirModeling != null) {
            if (fhirModeling.getLeft() != null) {
                expression.setResourceType(fhirModeling.getLeft());
            }
            if (fhirModeling.getRight() != null) {
                expression.setPath(fhirModeling.getRight());
            }
        }
        context.expressionStack.push(expression);
    }

    // operator
    // in
    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        Expression expression = context.expressionStack.pop();
        expression.setOperator(criteriaResourceParamDTO.getName());
        context.expressionStack.push(expression);
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        if (predicatePart.getDataInputClassType() != null
                && predicatePart.getDataInputClassType().equals(DataModelClassType.String)
                && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            Expression expression = context.expressionStack.pop();
            expression.setRight(predicatePart.getText());
            context.expressionStack.push(expression);
        }
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
        Expression expression = context.expressionStack.pop();
        if (cdsCodeDTO.getCode() != null && cdsCodeDTO.getDisplayName() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            Map<String,Pair<String, String>> valueSetMap = FHIRContext.readValueSetMapping();
            String display = "";
            String url = "";
            if (valueSetMap != null && valueSetMap.size() > 0) {
                Pair<String, String> displayUrlPair = valueSetMap.get(cdsCodeDTO.getCode());
                url = displayUrlPair.getRight();
                display = displayUrlPair.getLeft();
            }
            ValueSet valueset = new ValueSet(display, url);
            context.printMap.put(valueset.getAlias(), valueset);
            expression.setConcept(valueset.getAlias());
        }
        context.expressionStack.push(expression);
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
