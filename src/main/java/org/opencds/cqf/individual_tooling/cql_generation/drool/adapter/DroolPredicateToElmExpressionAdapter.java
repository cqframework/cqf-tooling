package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

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
import org.hl7.cql.model.DataType;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.Count;
import org.hl7.elm.r1.Distinct;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Literal;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Quantity;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.context.FHIRContext;

public class DroolPredicateToElmExpressionAdapter {
    private FHIRContext fhirContext = new FHIRContext();
    // Literal, Quantity, Expression, or vmrModeling
    private Object left;
    private Expression right;
    private String operator;
    private boolean startedFunction = false;

    public void adapt(OpenCdsConceptDTO openCdsConceptDTO, ElmContext context) {
        // TODO: Support List of ValueSets for counting each
        if (openCdsConceptDTO.getCode() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            if (openCdsConceptDTO.getCode().equals("y") || openCdsConceptDTO.getCode().equals("m") || openCdsConceptDTO.getCode().equals("d")) {
                right = inferUnitAndQuantity(context, openCdsConceptDTO.getCode());
            } else {
                String url = "";
                String name = "";
                if (fhirContext.valueSetMap != null && fhirContext.valueSetMap.size() > 0) {
                    Pair<String, String> displayUrlPair = fhirContext.valueSetMap.get(openCdsConceptDTO.getCode());
                    // url = displayUrlPair.getRight();
                    url = openCdsConceptDTO.getCode();
                    name = displayUrlPair.getLeft();
                }
                ValueSetRef valueSetRef = adaptValueSet(context, url, name);
                right = valueSetRef;
            }
        }
    }

    public void adapt(CdsCodeDTO cdsCodeDTO, ElmContext context) {
        // TODO: Support List of ValueSets for counting each
        if (cdsCodeDTO.getCode() != null) {
            if (cdsCodeDTO.getCode().equals("y") || cdsCodeDTO.getCode().equals("m") || cdsCodeDTO.getCode().equals("d")) {
                right = inferUnitAndQuantity(context, cdsCodeDTO.getCode());
            } else {
                String url = "";
                String name = "";
                if (fhirContext.valueSetMap != null && fhirContext.valueSetMap.size() > 0) {
                    Pair<String, String> displayUrlPair = fhirContext.valueSetMap.get(cdsCodeDTO.getCode());
                    // url = displayUrlPair.getRight();
                    url = cdsCodeDTO.getCode();
                    name = displayUrlPair.getLeft();
                }
                ValueSetRef valueSetRef = adaptValueSet(context, url, name);
                right = valueSetRef;
            }
        }
    }

    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, ElmContext context) {
        if (predicatePart.getDataInputClassType() != null
                && (predicatePart.getDataInputClassType().equals(DataModelClassType.String)
                        || predicatePart.getDataInputClassType().equals(DataModelClassType.Quantity)
                        || predicatePart.getDataInputClassType().equals(DataModelClassType.Numeric))
                && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            if (predicatePart.getDataInputNumeric() != null) {
                if (predicatePart.getText() != null && predicatePart.getText().equals("mg/24 hours ")) {
                    adaptQuantityOperand("mg/(24.h)", predicatePart.getDataInputNumeric(), context);
                } else{
                    adaptQuantityOperand(predicatePart.getText(), predicatePart.getDataInputNumeric(), context);
                }
            } else {
                adaptQuantityOperand(predicatePart.getText(), null, context);
            }
        }
        if (predicatePart.getResourceType() != null && predicatePart.getResourceType().equals(CriteriaResourceType.Function)) {
            if (!startedFunction) {
                if (predicatePart.getCriteriaResourceDTO() != null && predicatePart.getCriteriaResourceDTO().getName().equals("CountUnique")) {
                    startedFunction = true;
                } else {
                    throw new RuntimeException("Unknown Function: " + predicatePart.getCriteriaResourceDTO().getName());
                }
            } else {
                left = fhirContext.buildCountQuery(context, left, right, operator);
                startedFunction = false;
            }
        }
    }

    public void adapt(CriteriaResourceParamDTO criteriaResourceParamDTO, ElmContext context) {
        if (criteriaResourceParamDTO.getName() != null) {
            operator = criteriaResourceParamDTO.getName();
        }
    }

    public void adapt(DataInputNodeDTO dIN, ElmContext context) {
        // could look at predicateCount to figure out if this truly represents left
        left = Pair.of(dIN.getTemplateName(), dIN.getNodePath());
	}

	public void adapt(CriteriaPredicatePartDTO sourcePredicatePartDTO, ElmContext context) {
        if (sourcePredicatePartDTO.getPartType().equals(PredicatePartType.Text)) {
            if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                DataType dataType = context.libraryBuilder.resolveTypeName(context.getModelIdentifier() + "." + "Patient");
                DataType birthDateType = context.libraryBuilder.resolvePath(dataType, "birthDate");
                Property birthDateProperty = context.of.createProperty().withPath("birthDate").withSource(context.of.createExpressionRef().withName("Patient"));
                birthDateProperty.setResultType(birthDateType);
                Expression today = context.of.createToday();
                today.setResultType(context.libraryBuilder.resolveTypeName("System", "Date"));
                Expression operand = context.libraryBuilder.resolveFunction("System", "CalculateAgeAt", List.of(birthDateProperty, today));
                left = operand;
            }
        } else if (sourcePredicatePartDTO.getPartType().equals(PredicatePartType.DataInput)) {
            if (sourcePredicatePartDTO.getDataInputClassType().equals(DataModelClassType.Boolean)) {
                // TODO: need a way to retrieve dataInputBoolean value
                right = context.libraryBuilder.createLiteral(true);
            }
        }
    }
    
	public void adapt(ConditionCriteriaPredicateDTO predicate, ElmContext context) {
        if (predicate.getPredicatePartDTOs().size() > 0 && ! predicate.getPredicateType().equals(CriteriaPredicateType.PredicateGroup)) {
            context.expressionStack.push(fhirContext.buildPredicate(context, left, right, operator));
            if (left instanceof Quantity) {
                left = null;
            }
            // TODO: need to clearState() each time somehow;
        }
    }

    private ValueSetRef adaptValueSet(ElmContext context, String url, String name) {
        ValueSetDef vs = context.libraryBuilder.resolveValueSetRef(name);
        if (vs == null) {
            vs = context.of.createValueSetDef()
                .withAccessLevel(AccessModifier.PUBLIC)
                .withName(name)
                .withId(url);
            vs.setResultType(context.libraryBuilder.resolveTypeName("System", "ValueSet"));
            context.libraryBuilder.addValueSet(vs);
        }
        ValueSetRef valueSetRef = context.of.createValueSetRef().withName(name);
        valueSetRef.setResultType(context.libraryBuilder.resolveTypeName("System", "ValueSet"));
        return valueSetRef;
    }

    private void adaptQuantityOperand(String text, BigDecimal bigDecimal, ElmContext context) {
        if (text == null || text.equals("") || text.toLowerCase().equals("null")) {
            System.out.println("No Unit Provided");
        }
        if (bigDecimal == null && (text == null || text.equals("") || text.toLowerCase().equals("null"))) {
            System.out.println("Data Input was Null");
            return;
        }
        if (bigDecimal != null) {
            if (left == null) {
                if (text != null) {
                    left = context.libraryBuilder.createQuantity(bigDecimal, text);
                } else {
                    left = context.libraryBuilder.createLiteral(bigDecimal.doubleValue());
                }
            }
            else {
                if (text != null) {
                    right = context.libraryBuilder.createQuantity(bigDecimal, text);
                } else {
                    right = context.libraryBuilder.createLiteral(bigDecimal.doubleValue());
                }
            }
        } else {
            if (left == null) {
                left = context.libraryBuilder.createNumberLiteral(text);
            } else {
                right = context.libraryBuilder.createNumberLiteral(text);
            }
        }
    }

    private Quantity inferUnitAndQuantity(ElmContext context, String code) {
        // TODO: what if unit is for left Quantity?
        switch (code) {
            case "y": {
                if (right != null && right instanceof Quantity) {
                    return context.libraryBuilder.createQuantity(((Quantity) right).getValue(), "years");
                } else if (right != null && right instanceof Literal) {
                    Literal literal = (Literal) right;
                    return context.libraryBuilder.createQuantity(parseDecimal(context, literal.getValue()), "years");
                }
            }
            case "m": {
                if (right != null && right instanceof Quantity) {
                    return context.libraryBuilder.createQuantity(((Quantity) right).getValue(), "months");
                } else if (right != null && right instanceof Literal) {
                    Literal literal = (Literal) right;
                    return context.libraryBuilder.createQuantity(parseDecimal(context, literal.getValue()), "months");
                }
            }
            case "d": {
                if (right != null && right instanceof Quantity) {
                    return context.libraryBuilder.createQuantity(((Quantity) right).getValue(), "days");
                } else if (right != null && right instanceof Literal) {
                    Literal literal = (Literal) right;
                    return context.libraryBuilder.createQuantity(parseDecimal(context, literal.getValue()), "days");
                }
            }
            default: throw new RuntimeException("Unkown unit type: " + code);
        }
    }

    private BigDecimal parseDecimal(ElmContext context, String value) {
        try {
            BigDecimal result = (BigDecimal)context.getDecimalFormat().parse(value);
            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse number literal: %s", value, e));
        }
    }
}
