package org.opencds.cqf.tooling.cql_generation.drool.converter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.cdsframework.enumeration.CriteriaResourceType;
import org.cdsframework.enumeration.DataModelClassType;
import org.cdsframework.enumeration.PredicatePartType;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Literal;
import org.hl7.elm.r1.Quantity;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Strings;

/**
 * Provides adapter functionality from any node in a
 * ConditionCriteriaPredicateDTO object graph to the respective Elm
 * representation. This is meant to build a single expression at a time. The
 * order should not necessarily matter although there are some cases where, for
 * example, a function declaration must start, gather operands, and end.
 * 
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class DroolPredicateToElmExpressionConverter {
    /**
     * the {@link VmrToModelElmBuilder ModelElmBuilder } is needed to determine the respective mapping 
     * from the VMR Model to a Model used in Cql i.e. FHIR
     */
    private VmrToModelElmBuilder modelBuilder;
    /**
     * left represents the left operand of a predicate Literal, Quantity,
     * Expression, or vmrModeling
     */
    private Object left;
    /**
     * right represents the right operand of a predicate
     */
    private Expression right;
    /**
     * operator represents the operator of a predicate
     */
    private String operator;
    /**
     * holds context of whether or not the current state is processing a function
     */
    private boolean startedFunction = false;

    public static Set<String> valueSetIds = new HashSet<String>();
    private static final Logger logger = LoggerFactory.getLogger(DroolPredicateToElmExpressionConverter.class);
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    /**
     * Provides adapter functionality from any node in a ConditionCriteriaPredicateDTO object graph to the respective elm representation.
     * @param modelBuilder modelBuilder
    */
    public DroolPredicateToElmExpressionConverter(VmrToModelElmBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
        markers.put("Left_Operand", MarkerFactory.getMarker("Left_Operand"));
        markers.put("Right_Operand", MarkerFactory.getMarker("Right_Operand"));
        markers.put("Expression", MarkerFactory.getMarker("Expression"));
        markers.put("Modeling", MarkerFactory.getMarker("Modeling"));
        markers.put("Function", MarkerFactory.getMarker("Function"));
        markers.put("Operator", MarkerFactory.getMarker("Operator"));
    }
    
    /**
     * Interrogates the {@link DataInputNodeDTO dataInputNode } for a TemplateName and NodePath in order to determine output Model Mapping
     * @param dIN dataInputNode
    */
    public void adapt(DataInputNodeDTO dIN) {
        logger.debug("Creating left side of operator...");
        left = Pair.of(dIN.getTemplateName(), dIN.getNodePath());
        logger.debug("left result: {}", left);
	}

    /**
     * Interrogates the {@link ConditionCriteriaPredicatePartDTO predicatePart } in order to determine if a Quantity or Function must be resolved in Elm
     * @param predicatePart predicatePart
     * @param libraryBuilder libraryBuilder
    */
    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, LibraryBuilder libraryBuilder) {
        if (predicatePart.getDataInputClassType() != null && predicatePart.getPartType()!= null
            && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            switch (predicatePart.getDataInputClassType()) {
                case Numeric: logger.debug("Resolving Numeric Quantity..."); resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), libraryBuilder);
                    break;
                case Quantity: logger.debug("Resolving Quantity..."); resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), libraryBuilder);
                    break;
                case String: {
                    if (predicatePart.getText() != null && predicatePart.getText().equals("Null")) {
                        logger.debug("Resolving Null String"); right = libraryBuilder.buildNull(libraryBuilder.resolveTypeName("System", "String"));
                    } else {
                        logger.debug("Resolving Number String Quantity..."); resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), libraryBuilder);
                    }
                }
                    break;
                default:
                    break;
            }
        } else {
            if (predicatePart.getResourceType() != null && predicatePart.getCriteriaResourceDTO() != null && predicatePart.getResourceType().equals(CriteriaResourceType.Function)) {
                logger.debug("Resolving Function...");
                resolveFunction(predicatePart.getCriteriaResourceDTO().getName(), libraryBuilder);
            }
        }
    }

    /**
     * Interrogates the {@link CriteriaPredicatePartDTO sourcePredicatePartDTO } in order to determine if a Boolean literal
     * or a Patient Age Function should be resolved in Elm
     * @param sourcePredicatePartDTO sourcePredicatePartDTO
     * @param libraryBuilder
    */
	public void adapt(CriteriaPredicatePartDTO sourcePredicatePartDTO, LibraryBuilder libraryBuilder) {
        switch(sourcePredicatePartDTO.getPartType()) {
            case DataInput: {
                if (sourcePredicatePartDTO.getDataInputClassType().equals(DataModelClassType.Boolean)) {
                    Marker expressionMarker = markers.get("Expression");
                    expressionMarker.add(markers.get("Right_Operand"));
                    logger.debug(expressionMarker, "Setting right operand to Boolean Literal.");
                    right = libraryBuilder.createLiteral(sourcePredicatePartDTO.isDataInputBoolean());
                    logger.debug(expressionMarker, "right result: {}", right);
                }
            } break;
            case Text: {
                if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                    Marker expressionMarker = markers.get("Expression");
                    expressionMarker.add(markers.get("Left_Operand"));
                    logger.debug(expressionMarker, "Setting left operand to Calculated Patient Age.");
                    left = modelBuilder.resolveModeling(libraryBuilder, Pair.of("Patient", "Age"), right, operator);
                    logger.debug(expressionMarker, "left result: {}", left);
                }
            } break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveFunction(String criteriaResourceName, LibraryBuilder libraryBuilder) {
        if (!startedFunction) {
            if (!Strings.isNullOrEmpty(criteriaResourceName) && criteriaResourceName.toLowerCase().equals("countunique")) {
                logger.debug(markers.get("Function"), "Starting function: {}", criteriaResourceName);
                startedFunction = true;
            } else {
                throw new RuntimeException("Unknown Function: " + criteriaResourceName);
            }
        } else {
            Marker expressionModelingMarker = markers.get("Expression");
            expressionModelingMarker.add(markers.get("Modeling"));
            logger.debug(expressionModelingMarker, "Resolving Modeling for left operand.");
            Expression modeling = modelBuilder.resolveModeling(libraryBuilder, (Pair<String, String>)left, right, operator);
            logger.debug(expressionModelingMarker, "modeling result: {}", modeling);
            Marker expressionMarker = markers.get("Expression");
            expressionMarker.add(markers.get("Left_Operand"));
            logger.debug(expressionMarker, "Setting left operand to Count Query.");
            left = modelBuilder.resolveCountQuery(libraryBuilder, modeling, right, operator);
            logger.debug(expressionMarker, "left result: {}", left);
            startedFunction = false;
            logger.debug(markers.get("Function"), "Ending function: {}", criteriaResourceName);
        }
    }

    private void resolveQuantity(String text, BigDecimal dataInputNumericValue, LibraryBuilder libraryBuilder) {
        if (dataInputNumericValue != null) {
            if (!Strings.isNullOrEmpty(text)) {
                List<String> unitParts = Arrays.asList(text.split(" "));
                if (unitParts.contains("mg/24") && unitParts.contains("hours")) {
                    logger.debug("Found units: {}, adapting to mg/(24.h)", unitParts);
                    adaptQuantityOperand("mg/(24.h)", dataInputNumericValue, libraryBuilder);
                    return;
                }
            }
            if (StringUtils.isNumeric(text)) {
                adaptQuantityOperand(null, dataInputNumericValue, libraryBuilder);
            } else {
                adaptQuantityOperand(text, dataInputNumericValue, libraryBuilder);
            }
        } else {
            adaptQuantityOperand(text, null, libraryBuilder);
        }
    }

    /**
     * Interrogates the {@link CriteriaResourceParamDTO criteriaResourceParamDTO } for the operator name
     * @param criteriaResourceParamDTO criteriaResourceParamDTO
     */
    public void adapt(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        logger.debug(markers.get("Operator"), "Creating operator...");
        operator = criteriaResourceParamDTO.getName();
        logger.debug(markers.get("Operator"), "operator result: {}", operator);
    }

    /**
     * Interrogates the {@link OpenCdsConceptDTO openCdsConceptDTO } for the code and displayName in order to determine Terminology
     * @param openCdsConceptDTO openCdsConceptDTO
     * @param libraryBuilder libraryBuilder
     */
    public void adapt(OpenCdsConceptDTO openCdsConceptDTO, LibraryBuilder libraryBuilder) {
        logger.debug("Resolving VMR concept: {}", openCdsConceptDTO.getCode());
        valueSetIds.add(openCdsConceptDTO.getCode());
        if (openCdsConceptDTO.getCode() != null) {
            resoveValueSet(openCdsConceptDTO.getCode(), openCdsConceptDTO.getDisplayName(), libraryBuilder);
        } else {   
            logger.warn("Vmr Concept code was not found: {}", openCdsConceptDTO.getCode());
        }
    }

    /**
     * Interrogates the {@link CdsCodeDTO cdsCodeDTO } for the code and displayName in order to determine Terminology
     * @param cdsCodeDTO
     * @param libraryBuilder libraryBuilder
     */
    public void adapt(CdsCodeDTO cdsCodeDTO, LibraryBuilder libraryBuilder) {
        logger.debug("Resolving VMR concept: {}", cdsCodeDTO.getCode());
        valueSetIds.add(cdsCodeDTO.getCode());
        if (cdsCodeDTO.getCode() != null) {
            resoveValueSet(cdsCodeDTO.getCode(), cdsCodeDTO.getDisplayName(), libraryBuilder);
        } else {   
            logger.warn("Vmr Concept code was not found: {}", cdsCodeDTO.getCode());
        }
    }

    private void resoveValueSet(String code, String display, LibraryBuilder libraryBuilder) {
        Marker expressionMarker = markers.get("Expression");
        expressionMarker.add(markers.get("Right_Operand"));
        if (code.matches("(?i)y|m|d")) {
            logger.debug("Resolving Quantity Unit: {}", code);
            right = inferUnitAndQuantity(libraryBuilder, code);
            logger.debug(expressionMarker, "right result: {}", right);
        } else {
            String url = "https://hln.com/fhir/ValueSet/" + code;
            if (display.equals("Active")) {
                logger.debug("Resolving Active Code...");
                right = resolveActiveConditionCode(libraryBuilder);
                logger.debug(expressionMarker, "right result: {}", right);
            } else {
                logger.debug("Resolving ValueSet: {}", code);
                right = modelBuilder.resolveValueSet(libraryBuilder, url, display);
                logger.debug(expressionMarker, "right result: {}", right);
            }
        }
    }

    private CodeRef resolveActiveConditionCode(LibraryBuilder libraryBuilder) {
        String systemUrl = "http://terminology.hl7.org/CodeSystem/condition-clinical";
        String systemName = "Condition Clinical Status Codes";

        String finalId = "active";
        String finalName = "Active";
        String finalDisplay = "Active";

        CodeSystemRef csRef = modelBuilder.resolveCodeSystem(libraryBuilder, systemUrl, systemName);
        return modelBuilder.resolveCode(libraryBuilder, finalId, finalName, finalDisplay, csRef);
    }

    /**
     * build Predicate from current state (left, right, operator)
     * @param predicate predicate
     * @param libraryBuilder libraryBuilder
     */
	public Expression adapt(ConditionCriteriaPredicateDTO predicate, LibraryBuilder libraryBuilder) {
        return modelBuilder.resolvePredicate(libraryBuilder, left, right, operator);
    }

    public void clearState() {
        left = null;
        right = null;
        operator = null;
        startedFunction = false;
    }

    private void adaptQuantityOperand(String text, BigDecimal bigDecimal, LibraryBuilder libraryBuilder) {
        if (Strings.isNullOrEmpty(text) || text.toLowerCase().equals("null")) {
            logger.warn("No unit found");
            logger.debug("No Unit Provided");
        }
        if (bigDecimal == null && (Strings.isNullOrEmpty(text) || text.toLowerCase().equals("null"))) {
            logger.warn("Data Input was null");
            return;
        }
        if (bigDecimal != null) {
            if (left == null) {
                Marker expressionMarker = markers.get("Expression");
                expressionMarker.add(markers.get("Left_Operand"));
                if (!Strings.isNullOrEmpty(text)) {
                    logger.debug(expressionMarker, "Creating left side of operator...");
                    left = libraryBuilder.createQuantity(bigDecimal, text);
                    logger.debug(expressionMarker, "left result: {}", left);
                } else {
                    logger.debug(expressionMarker, "Creating left side of operator...");
                    left = libraryBuilder.createLiteral(bigDecimal.doubleValue());
                    logger.debug(expressionMarker, "left result: {}", left);
                }
            }
            else {
                Marker expressionMarker = markers.get("Expression");
                expressionMarker.add(markers.get("Right_Operand"));
                if (!Strings.isNullOrEmpty(text)) {
                    logger.debug(expressionMarker, "Creating right side of operator...");
                    right = libraryBuilder.createQuantity(bigDecimal, text);
                    logger.debug(expressionMarker, "right result: {}", right);
                } else {
                    logger.debug(expressionMarker, "Creating right side of operator...");
                    right = libraryBuilder.createLiteral(bigDecimal.doubleValue());
                    logger.debug(expressionMarker, "right result: {}", right);
                }
            }
        } else {
            if (left == null) {
                Marker expressionMarker = markers.get("Expression");
                expressionMarker.add(markers.get("Left_Operand"));
                logger.debug(expressionMarker, "Creating left side of operator...");
                left = libraryBuilder.createNumberLiteral(text);
                logger.debug(expressionMarker, "left result: {}", left);
            } else {
                Marker expressionMarker = markers.get("Expression");
                expressionMarker.add(markers.get("Right_Operand"));
                logger.debug(expressionMarker, "Creating right side of operator...");
                right = libraryBuilder.createNumberLiteral(text);
                logger.debug(expressionMarker, "right result: {}", right);
            }
        }
    }

    private Quantity inferUnitAndQuantity(LibraryBuilder libraryBuilder, String code) {
        // TODO: what if unit is for left Quantity?
        String unit;
        switch (code) {
            case "y": unit = "years"; break;
            case "m": unit = "months"; break;
            case "d": unit = "days"; break;
            default: throw new RuntimeException("Unkown unit type: " + code);
        }
        if (right != null && right instanceof Quantity) {
            return libraryBuilder.createQuantity(((Quantity) right).getValue(), unit);
        } else if (right != null && right instanceof Literal) {
            Literal literal = (Literal) right;
            return libraryBuilder.createQuantity(modelBuilder.parseDecimal(literal.getValue()), unit);
        } else {
            throw new RuntimeException("Unable to infer Quantity from " + right.getClass().getName());
        }
    }
}
