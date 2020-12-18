package org.opencds.cqf.individual_tooling.cql_generation.drool.traversal;

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
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;

public class DepthFirstDroolTraverser<T> extends DroolTraverser<Visitor> {

    public DepthFirstDroolTraverser(Visitor visitor) {
        super(visitor);
    }

    @Override
    public void traverse(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (conditionCriteriaRel.getUuid().toString().equals("9cc20b85-0e4d-43fd-a840-3c3c8720685b")) {
            System.out.println("found you");
        }
        if (!conditionCriteriaRel.getConditionCriteriaPredicateDTOs().isEmpty()
                && !conditionCriteriaRel.getName().toLowerCase().contains("not yet implemented")) {
            for (ConditionCriteriaPredicateDTO predicate : conditionCriteriaRel.getConditionCriteriaPredicateDTOs()) {
                traverse(predicate);
            }
        } else if (conditionCriteriaRel.getName().contains("Not Yet Implemented")) {
            System.out.println("Not Yet Implemented: " + conditionCriteriaRel.getUuid());
        }
        this.visitor.visit(conditionCriteriaRel, this.context);
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
        }
        this.visitor.visit(predicate, this.context);
    }

    @Override
    public void traverse(ConditionCriteriaPredicatePartDTO predicatePart) {
        // mid

        if (predicatePart.getSourcePredicatePartDTO() != null
                && predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
            traverse(predicatePart.getSourcePredicatePartDTO());
        } else {
            // left
            if (predicatePart.getDataInputNodeDTO() != null) {
                traverse(predicatePart.getDataInputNodeDTO());
            }
            // right
            if (!predicatePart.getPredicatePartConceptDTOs().isEmpty()) {
                for (ConditionCriteriaPredicatePartConceptDTO predicatePartConcepts : predicatePart
                        .getPredicatePartConceptDTOs()) {
                    traverse(predicatePartConcepts);
                }
            }
        }
        if (predicatePart.getCriteriaResourceParamDTO() != null) {
            traverse(predicatePart.getCriteriaResourceParamDTO());
        }
        this.visitor.visit(predicatePart, this.context);
    }

    @Override
    protected void traverse(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        this.visitor.visit(criteriaResourceParamDTO, this.context);
    }

    @Override
    protected void traverse(DataInputNodeDTO dIN) {
        this.visitor.visit(dIN, this.context);
    }

    @Override
    protected void traverse(OpenCdsConceptDTO openCdsConceptDTO) {
        this.visitor.visit(openCdsConceptDTO, this.context);
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
            // for now doing nothing because sourcePredicate criteria resource objects do not contain string representation of operator.
                // if (sourcePredicatePartDTO.getCriteriaResourceParamDTO() != null) {
                //     traverse(sourcePredicatePartDTO.getCriteriaResourceParamDTO());
                // } else if (sourcePredicatePartDTO.getCriteriaResourceDTO() != null) {
                //     traverse(sourcePredicatePartDTO.getCriteriaResourceDTO());
                // }
                break;
            case Text:
                break;
            default:
                break;

        }
        this.visitor.visit(sourcePredicatePartDTO, this.context);
    }

    @Override
    protected void traverse(CriteriaResourceDTO criteriaResourceDTO) {
        this.visitor.visit(criteriaResourceDTO, this.context);
    }

    @Override
    protected void traverse(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts) {
        if (conditionPredicatePartConcepts.getOpenCdsConceptDTO() != null) {
            traverse(conditionPredicatePartConcepts.getOpenCdsConceptDTO());
        } else if (conditionPredicatePartConcepts.getCdsCodeDTO() != null) {
            traverse(conditionPredicatePartConcepts.getCdsCodeDTO());
        }
        this.visitor.visit(conditionPredicatePartConcepts, this.context);
    }

    @Override
    protected void traverse(CdsCodeDTO cdsCodeDTO) {
        this.visitor.visit(cdsCodeDTO, this.context);
    }

    @Override
    protected void traverse(CriteriaPredicatePartConceptDTO predicatePartConcepts) {
        if (predicatePartConcepts.getOpenCdsConceptDTO() != null) {
            traverse(predicatePartConcepts.getOpenCdsConceptDTO());
        }
        this.visitor.visit(predicatePartConcepts, this.context);
    }
    
}
