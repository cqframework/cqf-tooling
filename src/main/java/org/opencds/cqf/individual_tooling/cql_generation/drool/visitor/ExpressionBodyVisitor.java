package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.util.Map;

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
import org.cdsframework.enumeration.DataModelClassType;
import org.cdsframework.enumeration.PredicatePartType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Expression;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class ExpressionBodyVisitor implements Visitor {

    //Create a Model retriever thingy
    private Map<String, Pair<String, String>> cdsdmToFhirMap = Map.ofEntries(
            Map.entry("EncounterEvent.encounterType", Pair.of("Encounter", "type")),
            Map.entry("EncounterEvent.", Pair.of("Encounter", ".reasonReference.resolve() as Observation).value")),
            Map.entry("EncounterEvent.relatedClinicalStatement.problem.problemCode", Pair.of("Encounter", "?")),
            Map.entry("EncounterEvent.relatedClinicalStatement.observationResult.observationValue.concept", Pair.of("Encounter", "?")),
            Map.entry("EvaluatedPerson.demographics.gender", Pair.of("Patient", "gender")),
            Map.entry("ObservationOrder.observationFocus", Pair.of("Observation", "focus")),
            Map.entry("ObservationOrder.observationMethod", Pair.of("Observation", "?")),
            Map.entry("ObservationResult.interpretation", Pair.of("Observation", "interpretation")),
            Map.entry("ObservationResult.observationFocus", Pair.of("Observation", "focus")),
            Map.entry("ObservationResult.observationValue.concept", Pair.of("Observation", "value as CodeableConcept")),
            Map.entry("Problem.problemCode", Pair.of("Condition", "code")),
            Map.entry("Problem.problemStatus", Pair.of("Condition", "status")),
            Map.entry("ProcedureEvent.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("ProcedureOrder.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("ProcedureProposal.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("SubstanceAdministrationEvent.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to be a little more
                                                                                    // complicated
            Map.entry("SubstanceAdministrationOrder.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to be a little more
                                                                                    // complicated
            Map.entry("SubstanceAdministrationProposal.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to be a little more
                                                                                    // complicated
            Map.entry("SubstanceDispensationEvent.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to be a little more
                                                                                    // complicated
            Map.entry("SubstanceSubstanceAdministationEvent.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")),
            Map.entry("SubstanceAdministationOrder.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")),
            Map.entry("SubstanceAdministationProposal.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")),
            Map.entry("SubstanceDispensationEvent.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")));

    @Override
    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts, Context context) {

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts, Context context) {

    }

    @Override
    public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO, Context context) {
        if (sourcePredicatePartDTO.getPartType().equals(PredicatePartType.Text)) {
            if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                Expression expression = new Expression();
                expression.setLeft("AgeInYearsAt(Today())");
                context.expressionStack.push(expression);
            } else if (sourcePredicatePartDTO.getText().equals("Number:")) {
                // System.out.println("skip, Number:");
            } else if (sourcePredicatePartDTO.getText().equals("Units:")) {
                // System.out.println("skip, Units:");
                //TODO: create a UnitCodeSystem and DirectReferenceCode for the Unit
            }
        }

    }

    // right operand
    // "ValueSet"
    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO, Context context) {
        Expression expression = context.expressionStack.pop();
        if (openCdsConceptDTO.getCode() != null && openCdsConceptDTO.getDisplayName() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            DirectReferenceCode directReferenceCode = new DirectReferenceCode(openCdsConceptDTO.getDisplayName(),
                    openCdsConceptDTO.getCode());
            context.printMap.put(directReferenceCode.getAlias(), directReferenceCode);
            expression.setConcept(directReferenceCode.getAlias());
        }
        context.expressionStack.push(expression);
    }

    // left operand
    // Observation.value as CodeableConcept
    @Override
    public void visit(DataInputNodeDTO dIN, Context context) {
        Expression expression = new Expression();
        Pair<String, String> fhirModeling = cdsdmToFhirMap
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
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO, Context context) {
        Expression expression = context.expressionStack.pop();
        expression.setOperator(criteriaResourceParamDTO.getName());
        context.expressionStack.push(expression);
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart, Context context) {
        if (predicatePart.getDataInputClassType() != null
                && predicatePart.getDataInputClassType().equals(DataModelClassType.String)
                && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            Expression expression = context.expressionStack.pop();
            expression.setRight(predicatePart.getText());
            context.expressionStack.push(expression);
        }
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
        Expression expression = context.expressionStack.pop();
        if (cdsCodeDTO.getCode() != null && cdsCodeDTO.getDisplayName() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            DirectReferenceCode directReferenceCode = new DirectReferenceCode(cdsCodeDTO.getDisplayName(),
                    cdsCodeDTO.getCode());
            context.printMap.put(directReferenceCode.getAlias(), directReferenceCode);
            expression.setConcept(directReferenceCode.getAlias());
        }
        context.expressionStack.push(expression);
    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO, Context context) {
        // TODO Auto-generated method stub

    }
    
}
