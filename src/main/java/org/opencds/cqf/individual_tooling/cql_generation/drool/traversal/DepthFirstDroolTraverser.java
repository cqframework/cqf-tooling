package org.opencds.cqf.individual_tooling.cql_generation.drool.traversal;

import java.util.List;
import java.util.Stack;

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
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;

public class DepthFirstDroolTraverser<T> extends DroolTraverser<Visitor> {

    private Stack<CriteriaResourceParamDTO> criteriaResourceParamDTOExtensionStack = new Stack<CriteriaResourceParamDTO>();

    public DepthFirstDroolTraverser(Visitor visitor) {
        super(visitor);
    }

    @Override
    public void traverse(List<ConditionDTO> rootNode) {
        rootNode.forEach(node -> traverse(node));
        this.visitor.visit(rootNode);
    }

    @Override
    protected void traverse(ConditionDTO conditionDTO) {
        this.visitor.peek(conditionDTO);
        List<ConditionCriteriaRelDTO> conditionCriteriaRels = conditionDTO.getConditionCriteriaRelDTOs();
        if (!conditionCriteriaRels.isEmpty() || conditionCriteriaRels != null) {
            conditionCriteriaRels.forEach(rel -> { this.visitor.peek(rel); traverse(rel); });
        }
        this.visitor.visit(conditionDTO);
    }

    @Override
    protected void traverse(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (!conditionCriteriaRel.getConditionCriteriaPredicateDTOs().isEmpty()
                && !conditionCriteriaRel.getName().toLowerCase().contains("not yet implemented")) {
            for (ConditionCriteriaPredicateDTO predicate : conditionCriteriaRel.getConditionCriteriaPredicateDTOs()) {
                traverse(predicate);
            }
        } else if (conditionCriteriaRel.getName().contains("Not Yet Implemented")) {
            System.out.println("Not Yet Implemented: " + conditionCriteriaRel.getUuid());
        }
        this.visitor.visit(conditionCriteriaRel);
    }

    @Override
    protected void traverse(ConditionCriteriaPredicateDTO predicate) {
        if (!predicate.getPredicateDTOs().isEmpty()) {
            for (ConditionCriteriaPredicateDTO nestedPredicate : predicate.getPredicateDTOs()) {
                traverse(nestedPredicate);
            }
        }
        if (!predicate.getPredicatePartDTOs().isEmpty()) {
            for (ConditionCriteriaPredicatePartDTO predicatePart : predicate.getPredicatePartDTOs()) {
                traverse(predicatePart);
            }
            if (criteriaResourceParamDTOExtensionStack.size() > 0 || !criteriaResourceParamDTOExtensionStack.empty()) {
                traverse(criteriaResourceParamDTOExtensionStack.pop());
            }
        }
        this.visitor.visit(predicate);
    }

    @Override
    protected void traverse(ConditionCriteriaPredicatePartDTO predicatePart) {
        if (predicatePart.getSourcePredicatePartDTO() != null
                && predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
            traverse(predicatePart.getSourcePredicatePartDTO());
        } else {
            if (predicatePart.getDataInputNodeDTO() != null) {
                traverse(predicatePart.getDataInputNodeDTO());
            }
            if (!predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
                for (ConditionCriteriaPredicatePartConceptDTO predicatePartConcepts : predicatePart
                        .getPredicatePartConceptDTOs()) {
                    traverse(predicatePartConcepts);
                }
            }
        }
        if (predicatePart.getCriteriaResourceParamDTO() != null) {
            // In order to get left, right, operator instead of left, operator, right
            criteriaResourceParamDTOExtensionStack.push(predicatePart.getCriteriaResourceParamDTO());
        }
        this.visitor.visit(predicatePart);
    }

    @Override
    protected void traverse(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        this.visitor.visit(criteriaResourceParamDTO);
    }

    @Override
    protected void traverse(DataInputNodeDTO dIN) {
        this.visitor.visit(dIN);
    }

    @Override
    protected void traverse(OpenCdsConceptDTO openCdsConceptDTO) {
        this.visitor.visit(openCdsConceptDTO);
    }

    @Override
    protected void traverse(CriteriaPredicatePartDTO sourcePredicatePartDTO) {
        switch (sourcePredicatePartDTO.getPartType()) {
            case DataInput:
                if (!sourcePredicatePartDTO.getPredicatePartConceptDTOs().isEmpty()) {
                    for (CriteriaPredicatePartConceptDTO predicatePartConcepts : sourcePredicatePartDTO
                            .getPredicatePartConceptDTOs()) {
                        traverse(predicatePartConcepts);
                    }
                } else if (sourcePredicatePartDTO.getDataInputClassType().equals(DataModelClassType.String)) {
                    break;
                }
                break;
            case ModelElement:
                if (sourcePredicatePartDTO.getDataInputNodeDTO() != null) {
                    traverse(sourcePredicatePartDTO.getDataInputNodeDTO());
                }
                break;
            case Resource:
                break;
            case Text:
                break;
            default:
                break;

        }
        this.visitor.visit(sourcePredicatePartDTO);
    }

    @Override
    protected void traverse(CriteriaResourceDTO criteriaResourceDTO) {
        this.visitor.visit(criteriaResourceDTO);
    }

    @Override
    protected void traverse(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts) {
        if (conditionPredicatePartConcepts.getOpenCdsConceptDTO() != null) {
            traverse(conditionPredicatePartConcepts.getOpenCdsConceptDTO());
        } else if (conditionPredicatePartConcepts.getCdsCodeDTO() != null) {
            traverse(conditionPredicatePartConcepts.getCdsCodeDTO());
        }
        this.visitor.visit(conditionPredicatePartConcepts);
    }

    @Override
    protected void traverse(CdsCodeDTO cdsCodeDTO) {
        this.visitor.visit(cdsCodeDTO);
    }

    @Override
    protected void traverse(CriteriaPredicatePartConceptDTO predicatePartConcepts) {
        if (predicatePartConcepts.getOpenCdsConceptDTO() != null) {
            traverse(predicatePartConcepts.getOpenCdsConceptDTO());
        }
        this.visitor.visit(predicatePartConcepts);
    }
}
