package org.opencds.cqf.individual_tooling.cql_generation.builder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.hl7.cql.model.DataType;
import org.hl7.cql.model.ListType;
import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.As;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.IsNull;
import org.hl7.elm.r1.LetClause;
import org.hl7.elm.r1.Not;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Query;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ReturnClause;
import org.hl7.elm.r1.TypeSpecifier;

public class FhirModelElmBuilder extends ModelElmBuilder {

    public FhirModelElmBuilder(String modelVersion, DecimalFormat decimalFormat) {
        super("FHIR", modelVersion, "http://hl7.org/fhir", new FhirLibrarySourceProvider(), decimalFormat);
        IncludeDef includeHelper = of.createIncludeDef().withLocalIdentifier("FHIRHelpers").withPath("FHIRHelpers")
            .withVersion(modelVersion);
        this.setIncludeHelper(includeHelper);
    }

    @Override
    public Expression resolveModeling(LibraryBuilder libraryBuilder, Pair<String, String> left, Expression right, String operator) {
        switch (left.getLeft()) {
            case ("EncounterEvent"): {
                switch (left.getRight()) {
                    case ("relatedClinicalStatement/problem/problemCode"):
                        return buildModelingA(libraryBuilder);
                    case ("relatedClinicalStatement/observationResult/observationValue/concept"):
                        return buildModelingB(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("SubstanceAdministrationEvent"): {
                switch (left.getRight()) {
                    case ("relatedClinicalStatement/problem/problemCode"):
                        return buildModelingC(libraryBuilder);
                    case ("substance/substanceCode"):
                        return buildModelingD(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("Problem"): {
                switch (left.getRight()) {
                    case ("problemCode"):
                        return buildModelingE(libraryBuilder);
                    case ("problemStatus"):
                        return buildModelingF(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("ObservationResult"): {
                switch (left.getRight()) {
                    case ("interpretation"):
                        return buildModelingG(libraryBuilder);
                    case ("observationFocus"):
                        return buildModelingH(libraryBuilder);
                    case ("observationValue/concept"):
                        return buildModelingI(libraryBuilder);
                    case ("observationValue/physicalQuantity"):
                        return buildModelingJ(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("ObservationOrder"): {
                switch (left.getRight()) {
                    case ("observationFocus"):
                        return buildModelingK(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("SubstanceAdministrationOrder"): {
                switch (left.getRight()) {
                    case ("substance/substanceCode"):
                        return buildModelingL(libraryBuilder);
                    case ("id"):
                        return buildModelingM(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("EvaluatedPerson"): {
                switch (left.getRight()) {
                    case ("demographics/isDeceased"):
                        return buildModelingN(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("Patient"): {
                switch (left.getRight()) {
                    case ("Age"):
                        return resolvePatientAge(libraryBuilder);
                    default:
                        throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            default:
                throw new RuntimeException("Unknown vmr template: " + left.getLeft());
        }
    }

    // [Condition: category ~ "Encounter diagnosis"] C return C.code
    // checked
    private Expression buildModelingA(LibraryBuilder libraryBuilder) {
        String resource = "Condition";
        String retrieveDataPath = "category";
        String retrieveAlias = "C";
        String dataPath = "code";
        String codeComparator = "~";

        String systemUrl = "http://terminology.hl7.org/CodeSystem/condition-category";
        String systemName = "Condition Category Codes";

        String codeId = "encounter-diagnosis";
        String codeName = "Encounter Diagnosis";
        String codeDisplay = "Encounter Diagnosis";

        List<Element> elements = new ArrayList<Element>();

        return simpleReturnQuery(libraryBuilder, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.CodeableConcept // TODO: Contextualize to the encounter?
    //checked 
    private Expression buildModelingB(LibraryBuilder libraryBuilder) {
        String resource = "Observation";
        String dataPath = "value";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();

        String systemUrl = "http://hl7.org/fhir/observation-status";
        String systemName = "ObservationStatus";

        String finalId = "final";
        String finalName = "Final";
        String finalDisplay = "Final";

        String amendedId = "amended";
        String amendedName = "Amended";
        String amendedDisplay = "Amended";

        CodeSystemRef csRef = buildCodeSystem(libraryBuilder, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(libraryBuilder, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(libraryBuilder, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";
        
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Expression where = buildWhereReturnPathQuery(source, libraryBuilder, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, typeSpecifier, source);
    }

    // [MedicationAdministration] M return M.reasonCode // TODO: Work out the "include" capability
    //   union
    //   (
    //     [MedicationAdministration] M 
    //       let reasonConditions: [Condition: id in (M.reasonReference R return GetTail(R.reference))]
    //       return reasonConditions.code
    //   )
    private Expression buildModelingC(LibraryBuilder libraryBuilder) {
        String resource = "MedicationAdministration";
        String dataPath = "reasonCode";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Query simpleReasonCodeSource = simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null, source);
        Expression flattenSimpleCodeQuery = of.createFlatten().withOperand(simpleReasonCodeSource);
        flattenSimpleCodeQuery.setResultType(((ListType)simpleReasonCodeSource.getResultType()).getElementType());
        Query reasonConditionCodesSource = letConditionReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null);
        Expression union = libraryBuilder.resolveUnion(flattenSimpleCodeQuery, reasonConditionCodesSource);

        return union;
    }

    // [MedicationAdministration] M return M.medication.code as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail(M.medication.reference)])
    //       return medicationResource.code
    //   )
    // checked
    private Expression buildModelingD(LibraryBuilder libraryBuilder) {
        String resource = "MedicationAdministration";
        String dataPath = "medication";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();
        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Query simpleReasonCodeSource = simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, typeSpecifier, source);
        Query medicationResourceCodes = letMedicationReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null);
        Expression union = libraryBuilder.resolveUnion(simpleReasonCodeSource, medicationResourceCodes);

        return union;
    }

    // [Condition: category ~ "Problem"] C return C.code
    // checked
    private Expression buildModelingE(LibraryBuilder libraryBuilder) {
        String resource = "Condition";
        String retrieveDataPath = "category";
        String retrieveAlias = "C";
        String dataPath = "code";
        String codeComparator = "~";

        String systemUrl = "http://terminology.hl7.org/CodeSystem/condition-category";
        String systemName = "Condition Category Codes";

        String codeId = "problem-list-item";
        String codeName = "Problem";
        String codeDisplay = "Problem List Item";

        List<Element> elements = new ArrayList<Element>();

        return simpleReturnQuery(libraryBuilder, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // [Condition: category ~ "Problem"] C return C.clinicalStatus
    // checked
    private Expression buildModelingF(LibraryBuilder libraryBuilder) {
        String resource = "Condition";
        String retrieveDataPath = "category";
        String retrieveAlias = "C";
        String dataPath = "clinicalStatus";
        String codeComparator = "~";

        String systemUrl = "http://terminology.hl7.org/CodeSystem/condition-category";
        String systemName = "Condition Category Codes";

        String codeId = "problem-list-item";
        String codeName = "Problem";
        String codeDisplay = "Problem List Item";

        List<Element> elements = new ArrayList<Element>();

        return simpleReturnQuery(libraryBuilder, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // flatten ([Observation] O return O.interpretation)
    // checked
    private Expression buildModelingG(LibraryBuilder libraryBuilder) {
        String resource = "Observation";
        String dataPath = "interpretation";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Query query = simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null, source);
        Expression flatten = of.createFlatten().withOperand(query);
        flatten.setResultType(query.getReturn().getExpression().getResultType());
        return flatten;
    }

    // [Observation] O return O.code
    // checked
    private Expression buildModelingH(LibraryBuilder libraryBuilder) {
        String resource = "Observation";
        String dataPath = "code";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null, source);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.CodeableConcept
    // checked
    private Expression buildModelingI(LibraryBuilder libraryBuilder) {
        String resource = "Observation";
        String dataPath = "value";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();

        String systemUrl = "http://hl7.org/fhir/observation-status";
        String systemName = "ObservationStatus";

        String finalId = "final";
        String finalName = "Final";
        String finalDisplay = "Final";

        String amendedId = "amended";
        String amendedName = "Amended";
        String amendedDisplay = "Amended";

        CodeSystemRef csRef = buildCodeSystem(libraryBuilder, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(libraryBuilder, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(libraryBuilder, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Expression where = buildWhereReturnPathQuery(source, libraryBuilder, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, typeSpecifier, source);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.Quantity
    // checked
    private Expression buildModelingJ(LibraryBuilder libraryBuilder) {
        String resource = "Observation";
        String dataPath = "value";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();

        String systemUrl = "http://hl7.org/fhir/observation-status";
        String systemName = "ObservationStatus";

        String finalId = "final";
        String finalName = "Final";
        String finalDisplay = "Final";

        String amendedId = "amended";
        String amendedName = "Amended";
        String amendedDisplay = "Amended";

        CodeSystemRef csRef = buildCodeSystem(libraryBuilder, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(libraryBuilder, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(libraryBuilder, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Expression where = buildWhereReturnPathQuery(source, libraryBuilder, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "Quantity"));

        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, typeSpecifier, source);
    }

    // [ServiceRequest] SR return SR.code
    // checked
    private Expression buildModelingK(LibraryBuilder libraryBuilder) {
        String resource = "ServiceRequest";
        String dataPath = "code";
        String retrieveAlias = "SR";

        List<Element> elements = new ArrayList<Element>();
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, null, source);
    }

    // [MedicationRequest] M return M.medication as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail(M.medication.reference)])
    //       return medicationResource.code
    //   )
    // checked
    private Expression buildModelingL(LibraryBuilder libraryBuilder) {
        String medicationRequest = "MedicationRequest";
        String medicationAdministration = "MedicationAdministration";
        String dataPath = "medication";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();
        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));
        Retrieve retrieve = buildRetrieve(libraryBuilder, medicationRequest, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        Query simpleReasonCodeSource = simpleReturnQuery(libraryBuilder, medicationRequest, dataPath, retrieveAlias, elements, typeSpecifier, source);
        Query medicationResourceCodes = letMedicationReturnQuery(libraryBuilder, medicationAdministration, dataPath, retrieveAlias, elements, null);
        Expression union = libraryBuilder.resolveUnion(simpleReasonCodeSource, medicationResourceCodes);

        return union;
    }

    // ????
    private Expression buildModelingM(LibraryBuilder libraryBuilder) {
        Expression unknownModeling = of.createList().withElement(libraryBuilder.createLiteral("TODO: Determine Modeling"));
        unknownModeling.setResultType(new ListType(libraryBuilder.resolveTypeName("System" + "." + "String")));
        return unknownModeling;
    }

    // [Patient] P return P.deceased as FHIR.boolean
    //checked
    private Expression buildModelingN(LibraryBuilder libraryBuilder) {
        String resource = "Patient";
        String dataPath = "deceased";
        String retrieveAlias = "P";

        List<Element> elements = new ArrayList<Element>();

        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "boolean"));
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());
        return simpleReturnQuery(libraryBuilder, resource, dataPath, retrieveAlias, elements, typeSpecifier, source);
    }

    // right side
    // <operand localId="66" locator="60:23-60:38" xsi:type="Query">
    //               <source alias="$this">
    //                  <expression localId="65" locator="60:23-60:33" name="listofcodes" xsi:type="ExpressionRef"/>
    //               </source>
    //               <where xsi:type="Not">
    //                  <operand xsi:type="IsNull">
    //                     <operand path="code" xsi:type="Property">
    //                        <source name="$this" xsi:type="AliasRef"/>
    //                     </operand>
    //                  </operand>
    //               </where>
    //               <return distinct="false">
    //                  <expression path="code" xsi:type="Property">
    //                     <source name="$this" xsi:type="AliasRef"/>
    //                  </expression>
    //               </return>
    //            </operand>
    private Expression buildWhereReturnPathQuery(AliasedQuerySource source, LibraryBuilder libraryBuilder, String resource,
            org.hl7.elm.r1.List codeRefs, String filterPath) {
        DataType dataType = libraryBuilder.resolveTypeName(modelIdentifier + "." + resource);
        DataType codeType = libraryBuilder.resolvePath(dataType, filterPath);
        Expression property = libraryBuilder.buildProperty(source.getAlias(), resource + "." + filterPath, codeType);

        Expression left = property;

        AliasedQuerySource thisSource = of.createAliasedQuerySource().withAlias("$this").withExpression(codeRefs);
        thisSource.setResultType(thisSource.getExpression().getResultType());
        List<Element> elements = new ArrayList<Element>();
        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(thisSource);
        DataType codeStringType = libraryBuilder.resolvePath(libraryBuilder.resolveTypeName("System", "Code"), "code");
        Expression codeStringProperty = libraryBuilder.buildProperty(thisSource.getAlias(), "code", codeStringType);
        IsNull isNull = of.createIsNull().withOperand(codeStringProperty);
        isNull.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
        Not not = of.createNot().withOperand(isNull);
        not.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
        elements.add(not);
        libraryBuilder.resolveUnaryCall("System", "Not", not);
        ReturnClause returnClause = of.createReturnClause().withExpression(codeStringProperty);
        returnClause.setResultType(new ListType(codeStringProperty.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(libraryBuilder, sources, elements);

        Expression right = query;
        
        Expression where = libraryBuilder.resolveIn(left, right);
        return where;
    }

    // <expression localId="57" locator="56:3-56:35" dataType="fhir:Condition" codeProperty="category" codeComparator="~" xsi:type="Retrieve">
    //               <codes xsi:type="ToList">
    //                  <operand locator="56:26-56:34" name="Problem" xsi:type="CodeRef"/>
    //               </codes>
    //            </expression>
    private Expression simpleReturnQuery(LibraryBuilder libraryBuilder, String resource, String retrieveDataPath, String retrieveAlias, String dataPath,
            String codeComparator, String systemUrl, String systemName, String codeId, String codeName,
            String codeDisplay, List<Element> elements) {
        CodeSystemRef csRef = buildCodeSystem(libraryBuilder, systemUrl, systemName);
        CodeRef codeRef = buildCode(libraryBuilder, codeId, codeName, codeDisplay, csRef);
        Expression codeList = libraryBuilder.resolveToList(codeRef);
        // Retrieve retrieve = buildRetrieve(libraryBuilder, resource + "." + retrieveDataPath, codeRef, codeComparator);
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, codeList, codeComparator, retrieveDataPath);

        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);
        DataType dataType = libraryBuilder.resolveTypeName(modelIdentifier + "." + resource);
        DataType codeType = libraryBuilder.resolvePath(dataType, dataPath);
        // build ReturnClause
        // set the queryResultType to ReturnClause resultType
        // add the Return to Query
        Expression property = libraryBuilder.buildProperty(source.getAlias(), dataPath, codeType);
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(libraryBuilder, sources, elements);
        return query;
    }

    private Query simpleReturnQuery(LibraryBuilder libraryBuilder, String resource, String dataPath, String retrieveAlias, List<Element> elements,
            TypeSpecifier typeSpecifier, AliasedQuerySource source) {

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);
        DataType dataType = libraryBuilder.resolveTypeName(modelIdentifier + "." + resource);
        DataType codeType = libraryBuilder.resolvePath(dataType, dataPath);
        // build ReturnClause
        // set the queryResultType to ReturnClause resultType
        // add the Return to Query
        Expression property = libraryBuilder.buildProperty(source.getAlias(), dataPath, codeType);

        As as = null;
        if (typeSpecifier != null) {
            as = new As().withStrict(false)
                .withOperand(
                    property
                ).withAsTypeSpecifier(
                    typeSpecifier
            );
            as.setResultType(typeSpecifier.getResultType());
        }
        ReturnClause returnClause = (as != null) ? of.createReturnClause().withExpression(as) : of.createReturnClause().withExpression(property);
        returnClause.setResultType((as != null) ? new ListType(as.getResultType()) : new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(libraryBuilder, sources, elements);
        return query;
    }

    // [MedicationAdministration] M return M.medication as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail((M.medication as Reference).reference)])
    //       return medicationResource.code
    //   )
    // tested
    private Query letMedicationReturnQuery(LibraryBuilder libraryBuilder, String resource, String dataPath, String retrieveAlias,
    List<Element> elements, Object object) {
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(((ListType)source.getExpression().getResultType()).getElementType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);

        DataType medicationType = libraryBuilder.resolvePath(source.getResultType(), "medication");
        Expression medicationProperty = libraryBuilder.buildProperty(source.getAlias(), "medication", medicationType);
        DataType referenceType = libraryBuilder.resolvePath(medicationProperty.getResultType(), "reference");
        TypeSpecifier typeSpecifier = libraryBuilder.dataTypeToTypeSpecifier(libraryBuilder.resolveTypeName("FHIR", "Reference"));
        As as = null;
        if (typeSpecifier != null) {
            as = new As().withStrict(false)
                .withOperand(
                    medicationProperty
                ).withAsTypeSpecifier(
                    typeSpecifier
            );
            as.setResultType(typeSpecifier.getResultType());
        }
        Expression referenceProperty = libraryBuilder.buildProperty(as, "reference", referenceType);
        Expression separator = libraryBuilder.createLiteral("/");
        Expression split = libraryBuilder.resolveFunction("System", "Split", List.of(referenceProperty, separator));
        Expression last = libraryBuilder.resolveFunction("System", "Last", List.of(split));

        Expression idList = of.createList().withElement(last);
        idList.setResultType(new ListType(last.getResultType()));
        Retrieve medicationRetrieve = buildRetrieve(libraryBuilder, "Medication", idList, "in", "id");
        Expression function = libraryBuilder.resolveFunction("System", "First", List.of(medicationRetrieve));
        LetClause let = of.createLetClause().withIdentifier("medicationResource").withExpression(function);
        let.setResultType(function.getResultType());
        elements.add(let);

        DataType codeType = libraryBuilder.resolvePath(let.getResultType(), "code");
        Expression property = libraryBuilder.buildProperty(let.getIdentifier(), "code", codeType);
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(libraryBuilder, sources, elements);
        return query;
    }

    // [MedicationAdministration] M return M.reasonCode // TODO: Work out the "include" capability
    //   union
    //   (
    //     [MedicationAdministration] M 
    //       let reasonConditions: [Condition: id in (M.reasonReference R return GetTail(R.reference))]
    //       return reasonConditions.code
    //   )
    private Query letConditionReturnQuery(LibraryBuilder libraryBuilder, String resource, String dataPath, String retrieveAlias,
    List<Element> elements, Object object) {
        Retrieve retrieve = buildRetrieve(libraryBuilder, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(((ListType)source.getExpression().getResultType()).getElementType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);

        List<Element> letQueryElements = new ArrayList<Element>();
        DataType reasonReferenceType = libraryBuilder.resolvePath(source.getResultType(), "reasonReference");
        Expression reasonReferenceProperty = libraryBuilder.buildProperty(source.getAlias(), "reasonReference", reasonReferenceType);
        AliasedQuerySource reasonReferenceSource = of.createAliasedQuerySource().withAlias("R").withExpression(reasonReferenceProperty);
        reasonReferenceSource.setResultType(((ListType)reasonReferenceProperty.getResultType()).getElementType());
        DataType referenceType = libraryBuilder.resolvePath(((ListType)reasonReferenceProperty.getResultType()).getElementType(), "reference");
        Expression referenceProperty = libraryBuilder.buildProperty(reasonReferenceSource.getAlias(), "reference", referenceType);
        Expression separator = libraryBuilder.createLiteral("/");
        Expression split = libraryBuilder.resolveFunction("System", "Split", List.of(referenceProperty, separator));
        Expression last = libraryBuilder.resolveFunction("System", "Last", List.of(split));
        ReturnClause letQueryReturn = of.createReturnClause().withExpression(last);
        letQueryReturn.setResultType(new ListType(last.getResultType()));
        letQueryElements.add(letQueryReturn);
        List<AliasedQuerySource> letQuerySources = new ArrayList<AliasedQuerySource>();
        letQuerySources.add(reasonReferenceSource);
        Query letQuery = visitQuery(libraryBuilder, letQuerySources, letQueryElements);
        Expression idList = of.createList().withElement(letQuery);
        idList.setResultType(new ListType(letQuery.getResultType()));
        Retrieve conditionRetrieve = buildRetrieve(libraryBuilder, "Condition", idList, "in", "id");
        
        LetClause let = of.createLetClause().withIdentifier("reasonConditions").withExpression(conditionRetrieve);
        let.setResultType(conditionRetrieve.getResultType());
        elements.add(let);

        DataType codeType = libraryBuilder.resolvePath(((ListType)let.getResultType()).getElementType(), "code");
        Expression property = libraryBuilder.buildProperty(let.getIdentifier(), "code", codeType);
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(libraryBuilder, sources, elements);
        return query;
    }

    public Expression resolvePatientAge(LibraryBuilder libraryBuilder) {
        DataType dataType = libraryBuilder.resolveTypeName(modelIdentifier + "." + "Patient");
        DataType birthDateType = libraryBuilder.resolvePath(dataType, "birthDate");
        Property birthDateProperty = of.createProperty().withPath("birthDate").withSource(of.createExpressionRef().withName("Patient"));
        birthDateProperty.setResultType(birthDateType);
        Expression today = of.createToday();
        today.setResultType(libraryBuilder.resolveTypeName("System", "Date"));
        Expression operand = libraryBuilder.resolveFunction("System", "CalculateAgeAt", List.of(birthDateProperty, today));
        return operand;
    }
}
