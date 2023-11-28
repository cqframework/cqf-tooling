package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.util.HashMap;
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
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.tooling.cql_generation.drool.converter.DefinitionConverter;
import org.opencds.cqf.tooling.cql_generation.drool.converter.DroolPredicateToElmExpressionConverter;
import org.opencds.cqf.tooling.cql_generation.drool.converter.LibraryConverter;
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
    /**
     * Toggle ** of cql output
     */
    public enum CQLTYPES {
        CONDITION, CONDITIONREL
    }

    private Enum<CQLTYPES> type = null;
    private VmrToModelElmBuilder modelBuilder;
    private ElmContext context;
    private DroolPredicateToElmExpressionConverter expressionBodyAdapter;
    private DefinitionConverter definitionAdapter = new DefinitionConverter();
    private LibraryConverter libraryAdapter = new LibraryConverter();
    private static final Logger logger = LoggerFactory.getLogger(DroolToElmVisitor.class);
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    /**
     * Default to CONDITION granularity.
     * @param modelBuilder modelBuilder
     */
    public DroolToElmVisitor(VmrToModelElmBuilder modelBuilder) {
        this.type = CQLTYPES.CONDITION;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionConverter(modelBuilder);
        markers.put("Expression", MarkerFactory.getMarker("Expression"));
        markers.put("Library", MarkerFactory.getMarker("Library"));
        markers.put("ExpressionDef", MarkerFactory.getMarker("ExpressionDef"));
    }

    public DroolToElmVisitor(Enum<CQLTYPES> type, VmrToModelElmBuilder modelBuilder) {
        this.type = type;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionConverter(modelBuilder);
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
            logger.debug("Adapting source predicate part");
            expressionBodyAdapter.adapt(sourcePredicatePartDTO, context.libraryBuilder);
        } else {
            logger.debug("Source predicate part was null");
        }
    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        logger.debug("Adapting Open Cds Concept DTO");
        expressionBodyAdapter.adapt(openCdsConceptDTO, context.libraryBuilder);
    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        logger.debug("Adapting Data Input Node");
        expressionBodyAdapter.adapt(dIN);
    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        if (criteriaResourceParamDTO.getName() != null) {
            logger.debug("Adapting Criteria Resource Param DTO");
            expressionBodyAdapter.adapt(criteriaResourceParamDTO);
        } else {
            logger.debug("Criteria Resource Param DTO was null");
        }
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        logger.debug("Adapting Condition Criteria Predicate Part DTO");
        expressionBodyAdapter.adapt(predicatePart, context.libraryBuilder);
        definitionAdapter.adapt(predicatePart, context.libraryBuilder);
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        logger.debug("Adapting Condition Criteria Predicate DTO");
        if (predicate.getPredicatePartDTOs().size() > 0 && !predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
            Expression predicateExpression = expressionBodyAdapter.adapt(predicate, context.libraryBuilder);
            if (predicateExpression == null) {
                logger.warn(markers.get("Expression"), "Not enough information to generate elm from {}", predicate.getUuid());
            } else {
                logger.debug(markers.get("Expression"), "pushing Predicate Expression to expression Stack: {}", predicateExpression);
                context.expressionStack.push(predicateExpression);
            }
        } else {
            logger.debug("predicate.getPredicatePartDTOs was empty or Predicate Type was PredicateGroup");
        }
        logger.debug("clearing Expression Body Adapter state");
        expressionBodyAdapter.clearState();
        logger.debug("Adapting Expression Reference");
        Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
        definitionAdapter.adapt(predicate, context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
        if (expressionReferenceConjunctionPair != null) {
            logger.debug(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
            context.referenceStack.push(expressionReferenceConjunctionPair);
        } else {
            logger.warn(markers.get("ExpressionDef"), "Not enough information to generate elm from {}", predicate.getUuid());
        }
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            if (context.referenceStack.size() > 0) {
                Pair<String, ExpressionRef> expressionReferenceConjunctionPair = 
                    definitionAdapter.conditionCriteriaMetExpression(context.libraryBuilder, modelBuilder, context.expressionStack, context.referenceStack);
                if (expressionReferenceConjunctionPair != null) {
                    logger.debug(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                } else {
                    logger.debug("No Criteria Met Expression Reference built for conditionRel {}", conditionCriteriaRel.getUuid());
                }
            } else {
                logger.warn(markers.get("ExpressionDef"), "Not enough information to generate elm from remaining Expression Reference to build CriteriaMetExpression for condition {}", conditionCriteriaRel.getUuid());
            }
            logger.debug(markers.get("Library"), "Building Library {}", context.libraryBuilder.getLibraryIdentifier());
            context.buildLibrary();
        } else {
            logger.debug("CQLType was null or not ConditionRel");
        }
    }

    @Override
    public void peek(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            logger.debug("Adapting Library Identifier");
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionCriteriaRel, modelBuilder, context.libraries.size() + 1);
            logger.debug("Initializing new LibraryBuilder");
            context.newLibraryBuilder(libraryInfo);
        } else {
            logger.debug("CQLType was null or not ConditionRel");
        }
    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        logger.debug("Adapting CDS Code DTO");
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
                    logger.debug(markers.get("ExpressionDef"), "pushing expression reference to reference stack: {}", expressionReferenceConjunctionPair);
                    context.referenceStack.push(expressionReferenceConjunctionPair);
                } else {
                    logger.debug("No Criteria Met Expression Reference built for condition {}", conditionDTO.getUuid());
                }
            } else {
                logger.warn(markers.get("Library"), "No remaining Expression Reference to build CriteriaMetExpression from condition {}", conditionDTO.getUuid());
            }
            logger.debug(markers.get("Library"), "Building Library {}", context.libraryBuilder.getLibraryIdentifier());
            context.buildLibrary();
        } else {
            logger.debug("CQLType was null or not Condition");
        }
    }

    @Override
    public void peek(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            logger.debug("Adapting Library Identifier");
            Pair<VersionedIdentifier, ContextDef> libraryInfo = libraryAdapter.adapt(conditionDTO, modelBuilder, context.libraries.size() + 1);
            logger.debug("Initializing new LibraryBuilder");
            context.newLibraryBuilder(libraryInfo);
        } else {
            logger.debug("CQLType was null or not ConditionRel");
        }

    }

    @Override
    public ElmContext visit(List<ConditionDTO> rootNode) {
        return context;
    }
}
