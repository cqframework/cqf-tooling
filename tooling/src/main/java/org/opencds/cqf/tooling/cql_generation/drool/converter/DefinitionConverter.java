package org.opencds.cqf.tooling.cql_generation.drool.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.google.common.base.Strings;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.And;
import org.hl7.elm.r1.BinaryExpression;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.Or;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Provides adapter functionality for building an Expression Definition and
 * Reference Graph
 * 
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class DefinitionConverter {
    private static final Logger logger = LoggerFactory.getLogger(DefinitionConverter.class);
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public DefinitionConverter() {
        markers.put("ExpressionDef", MarkerFactory.getMarker("ExpressionDef"));
    }
    private String identifier;
    private String expressionContext = "Patient";

    /**
     * If the PredicatePartType of the {@link ConditionCriteriaPredicatePartDTO predicatePart} is ModelElement or Text
     * infers the ExpressionDefinition identifier from either the partAlias, text, or nodeLabel
     * and initializes context in the LibraryBuilder
     * @param predicatePart predicatePart
     * @param libraryBuilder libraryBuilder
     */
    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, LibraryBuilder libraryBuilder) {
        switch (predicatePart.getPartType()) {
            case DataInput:
                break;
            case ModelElement:
                inferIdentifier(predicatePart);
                logger.debug(markers.get("ExpressionDef"), "Initializing Context for Expression {}", identifier);
                initializeContext(libraryBuilder);
                break;
            case Resource:
                break;
            case Text: 
                inferIdentifier(predicatePart);
                logger.debug(markers.get("ExpressionDef"), "Initializing Context for Expression {}", identifier);
                initializeContext(libraryBuilder);
                break;
            default:
                break;
        }
    }

    /**
     * If there is no identifier, interrogate {@link ConditionCriteriaPredicateDTO predicate} 
     * for description and initializes context
     * includes all Expressions in the expression stack and includes a number of Expression
     * References in the reference stack equal to the number of Predicate entries in the predicateDTOs
     * finalizes context to flush out a stack of Expressions
     * @param predicate predicate
     * @param libraryBuilder libraryBuilder
     * @param modelBuilder modelBuilder
     * @param expressionStack expressionStack
     * @param referenceStack referenceStack (Conjunction, ExpressionRef)
     * @return Pair of (String, ExpressionRef)
     */
    public Pair<String, ExpressionRef> adapt(ConditionCriteriaPredicateDTO predicate, LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, Stack<Expression> expressionStack, Stack<Pair<String, ExpressionRef>> referenceStack) {
        if (Strings.isNullOrEmpty(identifier)) {
            identifier = predicate.getDescription() + "-" + predicate.getUuid();
            logger.debug(markers.get("ExpressionDef"), "Initializing Context for Expression {}", identifier);
            initializeContext(libraryBuilder);
        }
        logger.debug(markers.get("ExpressionDef"), "Including Expressions in ExpressionDefinition {}", identifier);
        ExpressionDef expressionDef = includeDefinitions(predicate.getPredicateConjunction().name(), libraryBuilder, modelBuilder, expressionStack);
        logger.debug(markers.get("ExpressionDef"), "Including References for Expression {}", identifier);
        expressionDef = includeReferences(predicate.getPredicateDTOs().size(), libraryBuilder, modelBuilder, referenceStack, expressionDef);
        if (expressionDef != null) {
            logger.debug(markers.get("ExpressionDef"), "Finalizing Context for Expression {}", identifier);
            return finalizeContext(predicate.getPredicateConjunction().name(), libraryBuilder, modelBuilder, expressionDef.getName());
        }
        else return null;
    }

    public Pair<String, ExpressionRef> conditionCriteriaMetExpression(LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, Stack<Expression> expressionStack, Stack<Pair<String, ExpressionRef>> referenceStack) {
        identifier = "ConditionCriteriaMet";
        logger.debug(markers.get("ExpressionDef"), "Initializing Context for Expression {}", identifier);
        initializeContext(libraryBuilder);
        logger.debug(markers.get("ExpressionDef"), "Including Expressions in ExpressionDefinition {}", identifier);
        ExpressionDef expressionDef = includeDefinitions(null, libraryBuilder, modelBuilder, expressionStack);
        logger.debug(markers.get("ExpressionDef"), "Including References for Expression {}", identifier);
        expressionDef = includeReferences(referenceStack.size(), libraryBuilder, modelBuilder, referenceStack, expressionDef);
        logger.debug(markers.get("ExpressionDef"), "Finalizing Context for Expression {}", identifier);
        return finalizeContext(null, libraryBuilder, modelBuilder, expressionDef.getName());
    }

    private void inferIdentifier(ConditionCriteriaPredicatePartDTO predicatePart) {
        String alias = predicatePart.getPartAlias();
        alias = (Strings.isNullOrEmpty(alias)) ? predicatePart.getText() : alias;
        alias = (Strings.isNullOrEmpty(alias)) ? predicatePart.getNodeLabel() : alias;
        String id = predicatePart.getPartId();
        if (alias != null && id != null) {
            identifier = alias + "-" + id;
        } else {
            logger.error(markers.get("ExpressionDefinition"), "predicatePart.getPartAlias(), predicatePart.getText(), and predicatePart.getNodeLabel() were null.");
            throw new RuntimeException("No Inferred Alias found for " + predicatePart.getUuid());
        }
    }

    private void initializeContext(LibraryBuilder libraryBuilder) {
        ExpressionDef def = libraryBuilder.resolveExpressionRef(identifier);
        if (def == null) {
            libraryBuilder.pushExpressionContext(expressionContext);
            libraryBuilder.currentExpressionContext();
            try {
                libraryBuilder.pushExpressionDefinition(identifier);
                libraryBuilder.resolveExpressionRef(identifier);
            } finally {
                libraryBuilder.popExpressionContext();
            }
        }
    }

    private ExpressionDef includeReferences(Integer actualListSize, LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, Stack<Pair<String, ExpressionRef>> referenceStack,
            ExpressionDef expressionDef) {
        if (!referenceStack.isEmpty() && actualListSize != null) {
            if (referenceStack.size() >= actualListSize.intValue()) {
                int index = 0;
                while (index < actualListSize.intValue()) {
                    Pair<String, ExpressionRef> reference = referenceStack.pop();
                    if (expressionDef == null) {
                        expressionDef = adaptExpressionDef(libraryBuilder, modelBuilder, reference.getRight());
                    } else {
                        buildBinaryFromConjunction(reference.getLeft(), libraryBuilder, expressionDef, reference.getRight());
                    }
                    index++;
                }
            } else if (referenceStack.size() <= actualListSize.intValue()) {
                logger.warn(markers.get("ExpressionDef"), "missing Expression Reference, expected {} but found {}.", actualListSize, referenceStack.size());
                for (Pair<String, ExpressionRef> reference : referenceStack) {
                    if (expressionDef == null) {
                        expressionDef = adaptExpressionDef(libraryBuilder, modelBuilder, reference.getRight());
                    } else {
                        buildBinaryFromConjunction(reference.getLeft(), libraryBuilder, expressionDef, reference.getRight());
                    }
                }
                logger.debug("Clearing Reference stack.");
                referenceStack.clear();
            }
        }
        return expressionDef;
    }

    private ExpressionDef includeDefinitions(String conjunction, LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, Stack<Expression> expressionStack) {
        ExpressionDef expressionDef = null;
        if (!expressionStack.isEmpty()) {
            while (!expressionStack.isEmpty()) {
                Expression expression = expressionStack.pop();
                if (expressionDef == null) {
                    expressionDef = adaptExpressionDef(libraryBuilder, modelBuilder, expression);
                } else {
                    buildBinaryFromConjunction(conjunction, libraryBuilder, expressionDef,
                            expression);
                }
            }
        }
        return expressionDef;
    }
    
    private ExpressionDef adaptExpressionDef(LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, Expression expression) {
        ExpressionDef def = libraryBuilder.resolveExpressionRef(identifier);
        if (def == null) {
            def = modelBuilder.of.createExpressionDef()
                    .withAccessLevel(AccessModifier.PUBLIC)
                    .withName(this.identifier)
                    .withContext(this.expressionContext)
                    .withExpression(expression);
            if (def.getExpression() != null) {
                def.setResultType(def.getExpression().getResultType());
            }
            libraryBuilder.addExpression(def);
        } else {
            def.setExpression(expression);
            if (def.getExpression() != null) {
                def.setResultType(def.getExpression().getResultType());
            }
        }
        return def;
    }

    private Pair<String, ExpressionRef> finalizeContext(String conjunction, LibraryBuilder libraryBuilder, VmrToModelElmBuilder modelBuilder, String expressionName) {
        libraryBuilder.popExpressionDefinition();
        ExpressionRef expressionRef = modelBuilder.of.createExpressionRef().withName(expressionName);
        identifier = null;
        if (conjunction != null) {
            return Pair.of(conjunction, expressionRef);
        }
        return null;
    }

    private void buildBinaryFromConjunction(String conjunction, LibraryBuilder libraryBuilder,
            ExpressionDef expressionDef, Expression expression) {
        BinaryExpression binary; 
        switch (conjunction.toLowerCase()) {
            case "or": {
                binary = new Or().withOperand(expressionDef.getExpression()).withOperand(expression);
                break;
            }
            case "and": {
                binary = new And().withOperand(expressionDef.getExpression()).withOperand(expression);
                break;
            }
            default: throw new RuntimeException("Unkown conjunction " + conjunction);
        }
        expressionDef.setExpression(binary);
    }
}
