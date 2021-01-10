package org.opencds.cqf.individual_tooling.cql_generation.drool.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.cdsframework.enumeration.DataModelClassType;
import org.cdsframework.enumeration.PredicatePartType;
import org.hl7.cql_annotations.r1.Annotation;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.As;
import org.hl7.elm.r1.CalculateAgeAt;
import org.hl7.elm.r1.DateTimePrecision;
import org.hl7.elm.r1.Equal;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.FunctionRef;
import org.hl7.elm.r1.Greater;
import org.hl7.elm.r1.GreaterOrEqual;
import org.hl7.elm.r1.InValueSet;
import org.hl7.elm.r1.Less;
import org.hl7.elm.r1.LessOrEqual;
import org.hl7.elm.r1.NamedTypeSpecifier;
import org.hl7.elm.r1.Not;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Quantity;
import org.hl7.elm.r1.Query;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ToQuantity;
import org.hl7.elm.r1.Today;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.context.FHIRContext;

public class ExpressionBodyAdapter {
    private FHIRContext fhirContext = new FHIRContext();

    public void adapt(OpenCdsConceptDTO openCdsConceptDTO, ElmContext context) {
        if (openCdsConceptDTO.getCode() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            adaptValueSetDef(openCdsConceptDTO.getCode(), context);
            adaptExpressionInValueSet(openCdsConceptDTO.getCode(), context);
        }
    }

    public void adapt(ConditionCriteriaPredicatePartDTO predicatePart, ElmContext context) {
        if (predicatePart.getDataInputClassType() != null
                && (predicatePart.getDataInputClassType().equals(DataModelClassType.String)
                        || predicatePart.getDataInputClassType().equals(DataModelClassType.Quantity))
                && predicatePart.getPartType().equals(PredicatePartType.DataInput)) {
            if (predicatePart.getDataInputNumeric() != null) {
                adaptQuantityOperand(predicatePart.getText(), predicatePart.getDataInputNumeric(), context);
            } else {
                adaptQuantityOperand(predicatePart.getText(), null, context);
                if (predicatePart.getText() != null && predicatePart.getText().equals("ug/mL")) {
                    System.out.println("what");
                }
            }
        }
    }

    public void adapt(CdsCodeDTO cdsCodeDTO, ElmContext context) {
        if (cdsCodeDTO.getCode() != null) {
            // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
            // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
            adaptValueSetDef(cdsCodeDTO.getCode(), context);
            adaptExpressionInValueSet(cdsCodeDTO.getCode(), context);
        }
    }

    public void adapt(CriteriaResourceParamDTO criteriaResourceParamDTO, ElmContext context) {
        adaptOperator(criteriaResourceParamDTO.getName(), context);
    }

    public void adapt(DataInputNodeDTO dIN, ElmContext context) {
        Expression query = null;
        Pair<String, String> fhirModeling = fhirContext.cdsdmToFhirMap
                .get(dIN.getTemplateName() + "." + dIN.getNodePath().replaceAll("/", "."));
        AliasedQuerySource aliasedQuerySource = null;
        Expression where = null;
        if (fhirModeling != null) {
            if (fhirModeling.getLeft() != null) {
                Expression retrieve = new Retrieve()
                    .withDataType(
                        new QName("", fhirModeling.getLeft(), "fhir")
                    )
                    .withTemplateId(fhirContext.resourceTemplateMap.get(fhirModeling.getLeft()));

                aliasedQuerySource = new AliasedQuerySource()
                    .withAlias(fhirModeling.getLeft()).withExpression(retrieve);
            }
            if (fhirModeling.getRight() != null) {
                Expression asOperand = new As().withStrict(false)
                .withOperand(
                    new Property().withScope(fhirModeling.getLeft()).withPath(fhirModeling.getRight())
                ).withAsTypeSpecifier(
                    new NamedTypeSpecifier().withName(new QName("", "CodeableConcept", "fhir"))
                );
                Expression codeOperand = new FunctionRef().withLibraryName("FHIRHelpers").withName("ToConcept").withOperand(asOperand);
                where = new InValueSet().withCode(codeOperand);
            }
            if (aliasedQuerySource != null && where != null) {
                query = new Query().withSource(aliasedQuerySource).withWhere(where);
            }
        } else {
            System.out.println("No fhirModeling found for: " + dIN.getTemplateName() + "." + dIN.getNodePath());
            return;
        }
        context.expressionStack.push(query);
	}

	public void adapt(CriteriaPredicatePartDTO sourcePredicatePartDTO, ElmContext context) {
        if (sourcePredicatePartDTO.getPartType().equals(PredicatePartType.Text)) {
            if (sourcePredicatePartDTO.getText().equals("Patient age is")) {
                Expression operand = new ToQuantity()
                    .withOperand(
                        new CalculateAgeAt().withPrecision(DateTimePrecision.YEAR)
                            .withOperand(
                                new Property().withPath("birthDate.value")
                                .withSource( new ExpressionRef().withName("Patient")),
                                new Today()
                            )
                );
                context.expressionStack.push(operand);
            } else if (sourcePredicatePartDTO.getText().equals("Number:")) {
                // System.out.println("skip, Number:");
            } else if (sourcePredicatePartDTO.getText().equals("Units:")) {
                // System.out.println("skip, Units:");
                // TODO: create a UnitCodeSystem and DirectReferenceCode for the Unit
            }
        }
	}

    // Start helper methods
    private void adaptQuantityOperand(String text, BigDecimal bigDecimal, ElmContext context) {
        if (text == null || text.equals("") || text.toLowerCase().equals("null")) {
            System.out.println("Data Input was null.");
            return;
        }
        Expression operand;
        if (bigDecimal != null) {
            operand = new Quantity().withValue(bigDecimal).withUnit(text);
        } else {
            operand = new Quantity().withValue(new BigDecimal(text));
        }
        context.expressionStack.push(operand);
    }

    private void adaptOperator(String operator, ElmContext context) {
        Expression operatorExpression;
        if (operator.equals("==")) {

        }
        switch (operator) {
            case ">=":
                operatorExpression = new GreaterOrEqual();
                break;
            case "==":
                operatorExpression = new InValueSet();
                break;
            case "<":
                operatorExpression = new Less();
                break;
            case ">":
                operatorExpression = new Greater();
                break;
            case "<=":
                operatorExpression = new LessOrEqual();
                break;
            case "!=":
                operatorExpression = new Not().withOperand(new Equal());
                break;
            case "in":
                operatorExpression = new InValueSet();
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
        context.expressionStack.push(operatorExpression);
    }

    private void adaptExpressionInValueSet(String valueSetIdentifier, ElmContext context) {
        String name = "";
        if (fhirContext.valueSetMap != null && fhirContext.valueSetMap.size() > 0) {
            name = fhirContext.valueSetMap.get(valueSetIdentifier).getLeft();
        }
        Expression where = new InValueSet(); // context.expressionStack.pop();
        ValueSetRef valuesetRef = new ValueSetRef().withName(name);
        if (where.getClass().equals(InValueSet.class)) {
            InValueSet inValueSet = (InValueSet) where;
            inValueSet.setValueset(valuesetRef);
        }
        context.expressionStack.push(where);
    }

    private void adaptValueSetDef(String valueSetIdentifier, ElmContext context) {
        // Create valueSet Identifier: displayName GoballyUniqueIdentifier: displayName
        // Only if it's not 2.16.840.1.113883.3.795.5.4.12.5.1
        String url = "";
        String name = "";
        if (fhirContext.valueSetMap != null && fhirContext.valueSetMap.size() > 0) {
            Pair<String, String> displayUrlPair = fhirContext.valueSetMap.get(valueSetIdentifier);
            url = displayUrlPair.getRight();
            name = displayUrlPair.getLeft();
        }
        ValueSetDef valueset = new ValueSetDef().withAccessLevel(AccessModifier.PUBLIC);
        List<Object> annotations = new ArrayList<Object>();
        Annotation annotation = new Annotation();
        annotations.add(annotation);
        valueset.withId(url).withName(name).withAnnotation(annotations);
        context.addContext(valueset);
    }
}
