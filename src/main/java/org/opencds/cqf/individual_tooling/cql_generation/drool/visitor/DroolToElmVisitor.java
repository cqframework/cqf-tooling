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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Implements the {@link Visitor Visitor} Interface and uses the
 * DefinitionAdapter DroolPredicateToElmExpressionAdapter and LibraryAdapter to
 * build up the {@link ElmContext ElmContext}. The ganularity of the libraries
 * can be toggled by CONDITION or CONDITIONREL using the {@link CQLTYPES
 * CQLTYPES} enumeration.
 * 
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class DroolToElmVisitor implements Visitor {
    public enum CQLTYPES {
        CONDITION, CONDITIONREL
    }

    private Enum<CQLTYPES> type = null;
    private VmrToModelElmBuilder modelBuilder;
    private ElmContext context;
    private DroolPredicateToElmExpressionAdapter expressionBodyAdapter;
    private DefinitionAdapter definitionAdapter = new DefinitionAdapter();
    private LibraryAdapter libraryAdapter = new LibraryAdapter();
    private Logger logger;
    private Map<String, Marker> markers = Map.of(
        "Expression", MarkerFactory.getMarker("Expression"),
        "Library", MarkerFactory.getMarker("Library"),
        "ExpressionDef", MarkerFactory.getMarker("ExpressionDef")
    );

    /**
     * Default to CONDITION granularity.
     * @param modelBuilder modelBuilder
     */
    public DroolToElmVisitor(VmrToModelElmBuilder modelBuilder) {
        this.type = CQLTYPES.CONDITION;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public DroolToElmVisitor(Enum<CQLTYPES> type, VmrToModelElmBuilder modelBuilder) {
        this.type = type;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
        logger = LoggerFactory.getLogger(this.getClass());
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
            logger.info("Adapting source predicate part");
            expressionBodyAdapter.adapt(sourcePredicatePartDTO, context.libraryBuilder);
        } else {
            logger.info("Source predicate part was null");
        }
    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        logger.info("Adapting Open Cds Concept DTO");
        expressionBodyAdapter.adapt(openCdsConceptDTO, context.libraryBuilder);
    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        logger.info("Adapting Data Input Node");
        expressionBodyAdapter.adapt(dIN);
    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        if (criteriaResourceParamDTO.getName() != null) {
            logger.info("Adapting Criteria Resource Param DTO");
            expressionBodyAdapter.adapt(criteriaResourceParamDTO);
        } else {
            logger.info("Criteria Resource Param DTO was null");
        }
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        logger.info("Adapting Condition Criteria Predicate Part DTO");
        expressionBodyAdapter.adapt(predicatePart, context.libraryBuilder);
        definitionAdapter.adapt(predicatePart, context.libraryBuilder);
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        logger.info("Adapting Condition Criteria Predicate DTO");
        if (predicate.getPredicatePartDTOs().size() > 0 && !predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
            Expression predicateExpression = expressionBodyAdapter.adapt(predicate, context.libraryBuilder);
            if (predicateExpression == null) {
                logger.warn(markers.get("Expression"), "Not enough information to generate elm from " + predicate.getUuid());
            } else {
                logger.info(markers.get("Expression"), "pushing Predicate Expression to expression Stack: {}", predicateExpression);
                context.expressionStack.push(predicateExpression);
            }
        } else {
            logger.info("predicate.getPredicatePartDTOs was empty or Predicate Type was PredicateGroup");
        }
        logger.info("clearing Expression Body Adapter state");
        expressionBodyAdapter.clearState();
        logger.info("Adapting Expression Reference");
        Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
        definitionAdapter.adapt(predicate, context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
        if (expressionReferenceConjunctionPair != null) {
            logger.info(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
            context.referenceStack.push(expressionReferenceConjunctionPair);
        } else {
            logger.warn(markers.get("ExpressionDef"), "Not enough information to generate elm from " + predicate.getUuid());
        }
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            if (context.referenceStack.size() > 0) {
                Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
                    definitionAdapter.conditionCriteriaMetExpression(context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
                if (expressionReferenceConjunctionPair != null) {
                    logger.info(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                } else {
                    logger.info("No Criteria Met Expression Reference built for conditionRel {}", conditionCriteriaRel.getUuid());
                }
            } else {
                logger.warn(markers.get("ExpressionDef"), "Not enough information to generate elm from remaining Expression Reference to build CriteriaMetExpression for condition {}" + conditionCriteriaRel.getUuid());
            }
            logger.info(markers.get("Library"), "Building Library {}", context.libraryBuilder.getLibraryIdentifier());
            context.buildLibrary();
        } else {
            logger.info("CQLType was null or not ConditionRel");
        }
    }

    @Override
    public void peek(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            logger.debug("Adapting Library Identifier");
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionCriteriaRel, modelBuilder, context.libraries.size() + 1);
            logger.info("Initializing new LibraryBuilder");
            context.newLibraryBuilder(libraryInfo);
        } else {
            logger.info("CQLType was null or not ConditionRel");
        }
    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        logger.info("Adapting CDS Code DTO");
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
                    logger.info(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                } else {
                    logger.info("No Criteria Met Expression Reference built for condition {}", conditionDTO.getUuid());
                }
            } else {
                logger.warn(markers.get("Library"), "No remaining Expression Reference to build CriteriaMetExpression from condition {}" + conditionDTO.getUuid());
            }
            logger.info(markers.get("Library"), "Building Library {}", context.libraryBuilder.getLibraryIdentifier());
            context.buildLibrary();
        } else {
            logger.info("CQLType was null or not Condition");
        }
    }

    @Override
    public void peek(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            logger.debug("Adapting Library Identifier");
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionDTO, modelBuilder, context.libraries.size() + 1);
            logger.info("Initializing new LibraryBuilder");
            context.newLibraryBuilder(libraryInfo);
        } else {
            logger.info("CQLType was null or not ConditionRel");
        }

    }

    @Override
    public ElmContext visit(List<ConditionDTO> rootNode) {
        return context;
    }
}
