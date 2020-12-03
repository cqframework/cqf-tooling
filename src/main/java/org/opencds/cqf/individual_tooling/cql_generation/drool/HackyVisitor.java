package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.CriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.cdsframework.enumeration.PredicatePartType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineStatement;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineStatementBody;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Retrieve;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.WhereClause;

public class HackyVisitor {
    private Map<String, Object> printMap;
    private Map<String, Pair<String, String>> cdsdmToFhirMap = Map.of(
        "EncounterEvent.encounterType" , Pair.of("Encounter", "type"),
        // "EncounterEvent.relatedClinicalStatement.problem.problemCode" , Pair.of("Encounter", "?"),
        "EvaluatedPerson.demographics.gender" , Pair.of("Patient", "gender"),
        // "ObservationOrder.observationFocus" , Pair.of("Observation", "focus"),
        // // "ObservationOrder.observationMethod" , Pair.of("Observation", "?"),
        "ObservationResult.interpretation" , Pair.of("Observation", "interpretation"),
        "ObservationResult.observationFocus" , Pair.of("Observation", "focus"),
        "ObservationResult.observationValue.concept" , Pair.of("Observation", "value as CodeableConcept"),
        "Problem.problemCode" , Pair.of("Condition", "code"),
        // "Problem.problemStatus" , Pair.of("Condition", "status"),
        "ProcedureEvent.procedureCode" , Pair.of("Procedure", "code"),
        "ProcedureOrder.procedureCode" , Pair.of("Procedure", "code"),
        "ProcedureProposal.procedureCode" , Pair.of("Procedure", "code"),
        // "SubstanceAdministrationEvent.substance.substanceCode" , Pair.of("MedicationRequest", "medication as CodeableConcept"), // This needs to be a little more complicated
        // "SubstanceAdministrationOrder.substance.substanceCode" , Pair.of("MedicationRequest", "medication as CodeableConcept"), // This needs to be a little more complicated
        // "SubstanceAdministrationProposal.substance.substanceCode" , Pair.of("MedicationRequest", "medication as CodeableConcept"), // This needs to be a little more complicated
        "SubstanceDispensationEvent.substance.substanceCode" , Pair.of("MedicationRequest", "medication as CodeableConcept") // This needs to be a little more complicated
        // "SubstanceAdministationEvent.relatedClinicalStatement.problem.problemCode" , Pair.of("MedicationRequest", "?"),
        // "SubstanceAdministationOrder.relatedClinicalStatement.problem.problemCode" , Pair.of("MedicationRequest", "?"),
        // "SubstanceAdministationProposal.relatedClinicalStatement.problem.problemCode" , Pair.of("MedicationRequest", "?"),
        // "SubstanceDispensationEvent.relatedClinicalStatement.problem.problemCode" , Pair.of("MedicationRequest", "?")
    );

    public HackyVisitor(Map<String, Object> printMap) {
        this.printMap = printMap;
	}

	public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (!conditionCriteriaRel.getConditionCriteriaPredicateDTOs().isEmpty()) {
			for (ConditionCriteriaPredicateDTO predicate : conditionCriteriaRel.getConditionCriteriaPredicateDTOs()) {
                visit(predicate);
            }
        }
    }

    public DefineBlock visit(ConditionCriteriaPredicateDTO predicate) {
        DefineBlock defineBlock = new DefineBlock();
        DefineStatement defineStatement = new DefineStatement();
        List<DefineStatementBody> defineStatementBodies = new ArrayList<DefineStatementBody>();
        Retrieve retrieve = new Retrieve();
        WhereClause whereClause = new WhereClause();
        if (!predicate.getPredicateDTOs().isEmpty()) {
            for (ConditionCriteriaPredicateDTO nestedPredicate : predicate.getPredicateDTOs()) {
                DefineBlock nestedBlock = visit(nestedPredicate);
                if (defineStatementBodies.isEmpty()) {
                    defineStatementBodies.add(
                        new DefineStatementBody(null, null, null, nestedBlock.getDefineStatement().getAlias()));   
                } else {
                    defineStatementBodies.add(
                        new DefineStatementBody(null, null, predicate.getPredicateConjunction().name(), nestedBlock.getDefineStatement().getAlias()));   
                }
            }
        }
        if (!predicate.getPredicatePartDTOs().isEmpty()) {
            DefineStatementBody defineStatementBody = new DefineStatementBody();
            for (ConditionCriteriaPredicatePartDTO predicatePart : predicate.getPredicatePartDTOs()) {
                visit(predicatePart, retrieve, whereClause, defineStatementBody, defineStatement);
            }
            if (!predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
                if (defineStatementBodies.isEmpty()) {
                    defineStatementBodies.add(defineStatementBody);
                } else {
                    defineStatementBody.setConjunction(predicate.getPredicateConjunction().name());
                    defineStatementBodies.add(defineStatementBody);
                }
            }
        }
        defineBlock.setDefineStatement(defineStatement);
        defineBlock.setDefineStatementBody(defineStatementBodies);
        if (defineBlock.getDefineStatement().getAlias() != null) {
            printMap.put(defineBlock.getDefineStatement().getAlias(), defineBlock);
        }
        return defineBlock;
    }

    public void visit(ConditionCriteriaPredicatePartDTO predicatePart, Retrieve retrieve, WhereClause whereClause, DefineStatementBody defineStatementBody, DefineStatement defineStatement) {
        if (predicatePart.getCriteriaResourceParamDTO() != null) {
            visit(predicatePart.getCriteriaResourceParamDTO(), whereClause);
        }
        if (predicatePart.getDataInputNodeDTO() != null) {
            visit(predicatePart.getDataInputNodeDTO(), retrieve, whereClause);
        }
        if (predicatePart.getSourcePredicatePartDTO() != null && predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
            visit(predicatePart.getSourcePredicatePartDTO(), whereClause);
        }
        if (!predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
            for (ConditionCriteriaPredicatePartConceptDTO predicatePartConcepts : predicatePart.getPredicatePartConceptDTOs()) {
                visit(predicatePartConcepts, whereClause);
            }
        }
        defineStatementBody.setRetrieve(retrieve);
        defineStatementBody.setWhereClause(whereClause);

        if (predicatePart.getPartType() != null && ( predicatePart.getPartType().equals(PredicatePartType.Text) || predicatePart.getPartType().equals(PredicatePartType.ModelElement))) {
            if (predicatePart.getPartAlias() != null) {
                defineStatement.setAlias(predicatePart.getPartAlias());
            } else if (predicatePart.getText() != null) {
                defineStatement.setAlias(predicatePart.getText());
            }
        }
    }

    private void visit(CriteriaResourceParamDTO criteriaResourceParamDTO, WhereClause whereClause) {
        whereClause.setOperator(criteriaResourceParamDTO.getName());
    }

    public void visit(DataInputNodeDTO dIN, Retrieve retrieve, WhereClause whereClause) {
        Pair<String, String> fhirModeling = cdsdmToFhirMap.get(dIN.getTemplateName() + "." + dIN.getNodePath().replaceAll("/", "."));
        if (fhirModeling.getLeft() != null) { 
            retrieve.setResourceType(fhirModeling.getLeft());
            whereClause.setResourceType(fhirModeling.getLeft());
        }
        if (fhirModeling.getRight() != null) {
            whereClause.setPath(fhirModeling.getRight());
        }
    }

    public void visit(CriteriaPredicatePartDTO  sourcePredicatePartDTO, WhereClause whereClause) {
        if (!sourcePredicatePartDTO.getPredicatePartConceptDTOs().isEmpty()) {
            for (CriteriaPredicatePartConceptDTO predicatePartConcepts : sourcePredicatePartDTO.getPredicatePartConceptDTOs()) {
                visit(predicatePartConcepts, whereClause);
            }
        }
    }

    public void visit(ConditionCriteriaPredicatePartConceptDTO predicatePartConcepts, WhereClause whereClause) {
        if (predicatePartConcepts.getOpenCdsConceptDTO() != null) {
            visit(predicatePartConcepts.getOpenCdsConceptDTO(), whereClause);
        }
    }

    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts, WhereClause whereClause) {
        if (predicatePartConcepts.getOpenCdsConceptDTO() != null) {
            visit(predicatePartConcepts.getOpenCdsConceptDTO(), whereClause);
        }
    }

    public void visit(OpenCdsConceptDTO openCdsConceptDTO, WhereClause whereClause) {
        if (openCdsConceptDTO.getCode() != null && openCdsConceptDTO.getDisplayName() != null) {
            DirectReferenceCode directReferenceCode = new DirectReferenceCode(openCdsConceptDTO.getDisplayName(), openCdsConceptDTO.getCode());
            printMap.put(directReferenceCode.getAlias(), directReferenceCode);
            whereClause.setConcept(directReferenceCode.getAlias());
        }
    }

    public Map<String, Object> getCqlObjects() {
        return printMap;
    }
}
