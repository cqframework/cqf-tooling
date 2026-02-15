package org.opencds.cqf.tooling.cql_generation.drool.traversal;

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
import org.cdsframework.enumeration.CriteriaResourceType;
import org.cdsframework.enumeration.DataModelClassType;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traverses Depth First through a Drool Object Graph.
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class DepthFirstDroolTraverser<T> extends DroolTraverser<Visitor> {

    private static final Logger logger = LoggerFactory.getLogger(DepthFirstDroolTraverser.class);
    private Stack<CriteriaResourceParamDTO> criteriaResourceParamDTOExtensionStack = new Stack<CriteriaResourceParamDTO>();
    private Boolean unableToDetermineModeling = false;
    public DepthFirstDroolTraverser(Visitor visitor) {
        super(visitor);
    }

    @Override
    public ElmContext traverse(List<ConditionDTO> rootNode) {
        rootNode.forEach(node -> traverse(node));
        return this.visitor.visit(rootNode);
    }

    @Override
    protected void traverse(ConditionDTO conditionDTO) {
        this.visitor.peek(conditionDTO);
        List<ConditionCriteriaRelDTO> conditionCriteriaRels = conditionDTO.getConditionCriteriaRelDTOs();
        if (!conditionCriteriaRels.isEmpty() || conditionCriteriaRels != null) {

            conditionCriteriaRels.stream()
            .filter(rel ->
            rel.getConditionCriteriaPredicateDTOs().isEmpty()
             || rel.getName().toLowerCase().contains("not yet implemented"))
            .forEach(rel -> logger.info("Not Yet Implemented: {}", rel.getUuid()));

            conditionCriteriaRels.stream()
            .filter(rel ->
            !rel.getConditionCriteriaPredicateDTOs().isEmpty()
             && !rel.getName().toLowerCase().contains("not yet implemented"))
            .forEach(rel -> { this.visitor.peek(rel); traverse(rel); });
        }
        this.visitor.visit(conditionDTO);
    }

    @Override
    protected void traverse(ConditionCriteriaRelDTO conditionCriteriaRel) {
        for (ConditionCriteriaPredicateDTO predicate : conditionCriteriaRel.getConditionCriteriaPredicateDTOs()) {
            unableToDetermineModeling = false;
            traverse(predicate);
        }
        this.visitor.visit(conditionCriteriaRel);
    }

    @Override
    protected void traverse(ConditionCriteriaPredicateDTO predicate) {
        if (!predicate.getPredicateDTOs().isEmpty()) {
            for (ConditionCriteriaPredicateDTO nestedPredicate : predicate.getPredicateDTOs()) {
                unableToDetermineModeling = false;
                traverse(nestedPredicate);
            }
        }
        if (!predicate.getPredicatePartDTOs().isEmpty()) {
            for (ConditionCriteriaPredicatePartDTO predicatePart : predicate.getPredicatePartDTOs()) {
                if (!unableToDetermineModeling) {
                    traverse(predicatePart);
                } else return;
            }
            if (!unableToDetermineModeling) {
                if (criteriaResourceParamDTOExtensionStack.size() > 0 || !criteriaResourceParamDTOExtensionStack.empty()) {
                    traverse(criteriaResourceParamDTOExtensionStack.pop());
                }
            } else return;
        }
        if (!unableToDetermineModeling) {
            this.visitor.visit(predicate);
        } else return;
    }

    @Override
    protected void traverse(ConditionCriteriaPredicatePartDTO predicatePart) {
        if (predicatePart.getSourcePredicatePartDTO() != null
                && predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
            traverse(predicatePart.getSourcePredicatePartDTO());
            if (unableToDetermineModeling) {
                return;
            }
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
        if (unknownOperatorModeling(predicatePart)) {
            logger.info("Unable to determine operator from {}", predicatePart.getCriteriaResourceDTO().getUuid());
            unableToDetermineModeling = true;
            return;
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
                    if (sourcePredicatePartDTO.getPartAlias() != null && sourcePredicatePartDTO.getPartAlias().equals("an order and only an order")) {
                        logger.info("Unable to determine modeling from {}", sourcePredicatePartDTO.getUuid());
                        unableToDetermineModeling = true;
                    }
                    break;
                } else if (unknownConceptModeling(sourcePredicatePartDTO)){
                    logger.info("Unable to determine modeling from {}", sourcePredicatePartDTO.getUuid());
                    unableToDetermineModeling = true;
                    return;
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

    private boolean unknownConceptModeling(CriteriaPredicatePartDTO sourcePredicatePartDTO) {
        return sourcePredicatePartDTO.getPredicatePartRelDTOs() != null && !sourcePredicatePartDTO.getPredicatePartRelDTOs().isEmpty()
                    && sourcePredicatePartDTO.getPredicatePartRelDTOs().size() == 1 && sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getOpenCdsConceptDTO() == null
                    && sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsCodeDTO() == null && sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getValueSetDTO() == null
                    && sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsListDTO() != null
                    && (sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsListDTO().getName().equals("Reportable Condition Lab Test Concepts")
                    || sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsListDTO().getName().equals("Reportable Condition Lab Result Concepts")
                    || sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsListDTO().getName().equals("Reportable Condition Lab Results Interpretation Concepts")
                    || sourcePredicatePartDTO.getPredicatePartRelDTOs().get(0).getCdsListDTO().getName().equals("Reportable Condition Substance Administration"));
    }

    private boolean unknownOperatorModeling(ConditionCriteriaPredicatePartDTO predicatePart) {
        return predicatePart.getCriteriaResourceParamDTO() == null && predicatePart.getCriteriaResourceDTO() != null
            && ((predicatePart.getCriteriaResourceDTO().getResourceType() != null
                && !predicatePart.getCriteriaResourceDTO().getResourceType().equals(CriteriaResourceType.Function))
                || predicatePart.getCriteriaResourceDTO().getResourceType() == null);
    }
}
