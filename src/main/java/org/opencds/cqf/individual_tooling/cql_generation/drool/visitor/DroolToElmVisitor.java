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
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.DefinitionAdapter;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.DroolPredicateToElmExpressionAdapter;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.LibraryAdapter;

/**
 * Implements the {@link Visitor Visitor} Interface and uses the DefinitionAdapter 
 * DroolPredicateToElmExpressionAdapter and LibraryAdapter to build up the {@link ElmContext ElmContext}.  
 * The ganularity of the libraries can be toggled by CONDITION or CONDITIONREL
 * using the {@link CQLTYPES CQLTYPES} enumeration.
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DroolToElmVisitor implements Visitor {
    public enum CQLTYPES {
        CONDITION,
        CONDITIONREL
    }
    private Enum<CQLTYPES> type = null;
    private VmrToModelElmBuilder modelBuilder;
    private ElmContext context;
    private DroolPredicateToElmExpressionAdapter expressionBodyAdapter;
    private DefinitionAdapter definitionAdapter = new DefinitionAdapter();
    private LibraryAdapter libraryAdapter = new LibraryAdapter();

    /**
     * Default to CONDITION granularity.
     * @param modelBuilder modelBuilder
     */
    public DroolToElmVisitor(VmrToModelElmBuilder modelBuilder) {
        this.type = CQLTYPES.CONDITION;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
    }

    public DroolToElmVisitor(Enum<CQLTYPES> type, VmrToModelElmBuilder modelBuilder) {
        this.type = type;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
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
        if (sourcePredicatePartDTO.getPartType() != null) {
            expressionBodyAdapter.adapt(sourcePredicatePartDTO, context.libraryBuilder);
        }
    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        expressionBodyAdapter.adapt(openCdsConceptDTO, context.libraryBuilder);
    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        expressionBodyAdapter.adapt(dIN);
    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        if (criteriaResourceParamDTO.getName() != null) {
            expressionBodyAdapter.adapt(criteriaResourceParamDTO);
        }
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        expressionBodyAdapter.adapt(predicatePart, context.libraryBuilder);
        definitionAdapter.adapt(predicatePart, context.libraryBuilder);
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        if (predicate.getPredicatePartDTOs().size() > 0 && !predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
            Expression predicateExpression = expressionBodyAdapter.adapt(predicate, context.libraryBuilder);
            if (predicateExpression == null) {
                System.out.println("Not enough information to generate elm from " + predicate.getUuid());
            } else {
                context.expressionStack.push(predicateExpression);
            }
        }
        expressionBodyAdapter.clearState();
        Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
        definitionAdapter.adapt(predicate, context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
        if (expressionReferenceConjunctionPair != null){
            context.referenceStack.push(expressionReferenceConjunctionPair);
        } else {
            System.out.println("Not enough information to generate elm from " + predicate.getUuid());
        }
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            if (context.referenceStack.size() > 0) {
                Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
                    definitionAdapter.conditionCriteriaMetExpression(context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
                if (expressionReferenceConjunctionPair != null) {
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                }
            } else {
                System.out.println("No remaining Expression Reference to build CriteriaMetExpression" + conditionCriteriaRel.getUuid());
            }
            context.buildLibrary();
        }
    }

    @Override
    public void peek(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionCriteriaRel, modelBuilder, context.libraries.size() + 1);
            context.newLibraryBuilder(libraryInfo);
        }
    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        expressionBodyAdapter.adapt(cdsCodeDTO, context.libraryBuilder);
    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            if (context.referenceStack.size() > 0) {
                Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
                    definitionAdapter.conditionCriteriaMetExpression(context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
                if (expressionReferenceConjunctionPair != null) {
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                }
            } else {
                System.out.println("No remaining Expression Reference to build CriteriaMetExpression" + conditionDTO.getUuid());
            }
            context.buildLibrary();
        }
    }

    @Override
    public void peek(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionDTO, modelBuilder, context.libraries.size() + 1);
            context.newLibraryBuilder(libraryInfo);
        }

    }

    @Override
    public ElmContext visit(List<ConditionDTO> rootNode) {
        return context;
    }
}
