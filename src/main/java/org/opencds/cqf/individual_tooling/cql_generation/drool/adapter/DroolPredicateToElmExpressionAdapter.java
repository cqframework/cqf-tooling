package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.cdsframework.enumeration.CriteriaResourceType;
import org.cdsframework.enumeration.DataModelClassType;
import org.cdsframework.enumeration.PredicatePartType;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Literal;
import org.hl7.elm.r1.Quantity;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.builder.ModelElmBuilder;

/**
 * Provides adapter functionality from any node in a ConditionCriteriaPredicateDTO object graph to the respective elm Expression representation.
 * This is meant to build a single expression at a time. The order should not necessarily matter although there are some cases
 * where, for example, a function declaration must start, gather operands, and end.
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DroolPredicateToElmExpressionAdapter {
    /**
     * the {@link ModelElmBuilder ModelElmBuilder } is needed to determine the respective mapping 
     * from the VMR Model to a Model used in Cql i.e. FHIR
     */
    private ModelElmBuilder modelBuilder;
    /**
     * left represents the left operand of a predicate
     * Literal, Quantity, Expression, or vmrModeling
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

    //TODO: only used for debugging... maybe only engage when debugging and add to log?
    private boolean hitLeft = false;
    private boolean hitRight = false;
    public static Set<String> valueSetIds = new HashSet<String>();

    /**
     * Provides adapter functionality from any node in a ConditionCriteriaPredicateDTO object graph to the respective elm representation.
     * @param modelBuilder modelBuilder
     */
    public DroolPredicateToElmExpressionAdapter(ModelElmBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }
    
    /**
     * Interrogates the {@link DataInputNodeDTO dataInputNode } for a TemplateName and NodePath in order to determine output Model Mapping
     * @param dIN dataInputNode
     * @param context elmContext
     */
    public void adapt(DataInputNodeDTO dIN, ElmContext context) {
        left = Pair.of(dIN.getTemplateName(), dIN.getNodePath());
        if (left != null) {
            hitLeft = true;
        }
	}

    /**
     * Interrogates the {@link ConditionCriteriaPredicatePartDTO predicatePart } in order to determine if a Quantity or Function must be resolved in Elm
     * @param predicatePart predicatePart
     * @param context elmContext
     */
    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, ElmContext context) {
        if (predicatePart.getDataInputClassType() != null && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            switch (predicatePart.getDataInputClassType()) {
                case Numeric: resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), context.libraryBuilder);
                    break;
                case Quantity: resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), context.libraryBuilder);
                    break;
                case String: {
                    if (predicatePart.getText() != null && predicatePart.getText().equals("Null")) {
                        right = context.libraryBuilder.buildNull(context.libraryBuilder.resolveTypeName("System", "String"));
                        hitRight = true;
                    } else {
                        resolveQuantity(predicatePart.getText(), predicatePart.getDataInputNumeric(), context.libraryBuilder);
                    }
                }
                    break;
                default:
                    break;
            }
        } else {
            if (predicatePart.getResourceType() != null && predicatePart.getCriteriaResourceDTO() != null && predicatePart.getResourceType().equals(CriteriaResourceType.Function)) {
                resolveFunction(predicatePart.getCriteriaResourceDTO().getName(), context.libraryBuilder);
            }
        }
    }

    /**
     * Interrogates the {@link CriteriaPredicatePartDTO sourcePredicatePartDTO } in order to determine if a Boolean literal
     * or a Patient Age Function should be resolved in Elm
     * @param sourcePredicatePartDTO sourcePredicatePartDTO
     * @param context elmContext
     */
	public void adapt(CriteriaPredicatePartDTO sourcePredicatePartDTO, ElmContext context) {
        switch(sourcePredicatePartDTO.getPartType()) {
            case DataInput: {
                if (sourcePredicatePartDTO.getDataInputClassType().equals(DataModelClassType.Boolean)) {
                    right = context.libraryBuilder.createLiteral(sourcePredicatePartDTO.isDataInputBoolean());
                    if (right != null) {
                        hitRight = true;
                    }
                }
            } break;
            case Text: {
                if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                    left = modelBuilder.resolveModeling(context.libraryBuilder, Pair.of("Patient", "Age"), right, operator);
                if (left != null) {
                    hitLeft = true;
                }
                }
            } break;
            default:
                break;
        }
    }

    private void resolveFunction(String criteriaResourceName, LibraryBuilder libraryBuilder) {
        if (!startedFunction) {
            if (!Strings.isNullOrEmpty(criteriaResourceName) && criteriaResourceName.toLowerCase().equals("countunique")) {
                startedFunction = true;
            } else {
                throw new RuntimeException("Unknown Function: " + criteriaResourceName);
            }
        } else {
            Expression modeling = modelBuilder.resolveModeling(libraryBuilder, (Pair<String, String>)left, right, operator);
            left = modelBuilder.buildCountQuery(libraryBuilder, modeling, right, operator);
            if (left != null) {
                hitLeft = true;
            }
            startedFunction = false;
        }
    }

    private void resolveQuantity(String text, BigDecimal dataInputNumericValue, LibraryBuilder libraryBuilder) {
        if (dataInputNumericValue != null) {
            if (!Strings.isNullOrEmpty(text)) {
                List<String> unitParts = List.of(text.split(" "));
                if (unitParts.contains("mg/24") && unitParts.contains("hours")) {
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
     * @param context elmContext
     */
    public void adapt(CriteriaResourceParamDTO criteriaResourceParamDTO, ElmContext context) {
        if (criteriaResourceParamDTO.getName() != null) {
            operator = criteriaResourceParamDTO.getName();
        }
    }

    /**
     * Interrogates the {@link OpenCdsConceptDTO openCdsConceptDTO } for the code and displayName in order to determine Terminology
     * @param openCdsConceptDTO openCdsConceptDTO
     * @param context elmContext
     */
    public void adapt(OpenCdsConceptDTO openCdsConceptDTO, ElmContext context) {
        valueSetIds.add(openCdsConceptDTO.getCode());
        resoveValueSet(openCdsConceptDTO.getCode(), openCdsConceptDTO.getDisplayName(), context.libraryBuilder);
    }

    /**
     * Interrogates the {@link CdsCodeDTO cdsCodeDTO } for the code and displayName in order to determine Terminology
     * @param cdsCodeDTO
     * @param context
     */
    public void adapt(CdsCodeDTO cdsCodeDTO, ElmContext context) {
        valueSetIds.add(cdsCodeDTO.getCode());
        resoveValueSet(cdsCodeDTO.getCode(), cdsCodeDTO.getDisplayName(), context.libraryBuilder);
    }

    private void resoveValueSet(String code, String display, LibraryBuilder libraryBuilder) {
        if (code != null) {
            if (code.matches("(?i)y|m|d")) {
                right = inferUnitAndQuantity(libraryBuilder, code);
                if (right != null) {
                    hitLeft = true;
                }
            } else {
                String url = "https://hln.com/fhir/ValueSet/" + code;
                if (display.equals("Active")) {
                    right = resolveActiveConditionCode(libraryBuilder);
                    if (right != null) {
                        hitRight = true;
                    }
                } else {
                    right = modelBuilder.adaptValueSet(libraryBuilder, url, display);
                    if (right != null) {
                        hitRight = true;
                    }
                }
            }
        }
    }

    private CodeRef resolveActiveConditionCode(LibraryBuilder libraryBuilder) {
        String systemUrl = "http://terminology.hl7.org/CodeSystem/condition-clinical";
        String systemName = "Condition Clinical Status Codes";

        String finalId = "active";
        String finalName = "Active";
        String finalDisplay = "Active";

        CodeSystemRef csRef = modelBuilder.buildCodeSystem(libraryBuilder, systemUrl, systemName);
        return modelBuilder.buildCode(libraryBuilder, finalId, finalName, finalDisplay, csRef);
    }

    /**
     * If there are any {@link ConditionCriteriaPredicatePartDTO predicateParts}, push a predicate expression to the expression stack.
     * @param predicate
     * @param context
     */
	public void adapt(ConditionCriteriaPredicateDTO predicate, ElmContext context) {
        if (predicate.getPredicatePartDTOs().size() > 0 && !predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
            if (!hitRight || !hitLeft) {
                System.out.println("Not enough information to generate drool from " + predicate.getUuid());
            }
            context.expressionStack.push(modelBuilder.buildPredicate(context.libraryBuilder, left, right, operator));
            if (left instanceof Quantity) {
                left = null;
            }
            clearState();
        }
    }

    private void clearState() {
        hitLeft = false;
        hitRight = false;
        left = null;
        right = null;
        operator = null;
        startedFunction = false;
    }

    private void adaptQuantityOperand(String text, BigDecimal bigDecimal, LibraryBuilder libraryBuilder) {
        if (Strings.isNullOrEmpty(text) || text.toLowerCase().equals("null")) {
            System.out.println("No Unit Provided");
        }
        if (bigDecimal == null && (Strings.isNullOrEmpty(text) || text.toLowerCase().equals("null"))) {
            System.out.println("Data Input was Null");
            return;
        }
        if (bigDecimal != null) {
            if (left == null) {
                if (!Strings.isNullOrEmpty(text)) {
                    left = libraryBuilder.createQuantity(bigDecimal, text);
                } else {
                    left = libraryBuilder.createLiteral(bigDecimal.doubleValue());
                }
                if (left != null) {
                    hitLeft = true;
                }
            }
            else {
                if (!Strings.isNullOrEmpty(text)) {
                    right = libraryBuilder.createQuantity(bigDecimal, text);
                } else {
                    right = libraryBuilder.createLiteral(bigDecimal.doubleValue());
                }
                if (right != null) {
                    hitRight = true;
                }
            }
        } else {
            if (left == null) {
                left = libraryBuilder.createNumberLiteral(text);
            } else {
                right = libraryBuilder.createNumberLiteral(text);
            }
            if (left != null) {
                hitLeft = true;
            }
            if (right != null) {
                hitRight = true;
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
