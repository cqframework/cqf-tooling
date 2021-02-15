package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.CqlSemanticException;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.DataTypes;
import org.cqframework.cql.cql2elm.model.QueryContext;
import org.cqframework.cql.cql2elm.model.invocation.InValueSetInvocation;
import org.hl7.cql.model.ChoiceType;
import org.hl7.cql.model.ClassType;
import org.hl7.cql.model.DataType;
import org.hl7.cql.model.ListType;
import org.hl7.cql.model.NamedType;
import org.hl7.cql.model.TupleType;
import org.hl7.cql.model.TupleTypeElement;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.AggregateClause;
import org.hl7.elm.r1.AliasRef;
import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.And;
import org.hl7.elm.r1.AnyInCodeSystem;
import org.hl7.elm.r1.AnyInValueSet;
import org.hl7.elm.r1.As;
import org.hl7.elm.r1.BinaryExpression;
import org.hl7.elm.r1.ByDirection;
import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Contains;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Exists;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.FunctionRef;
import org.hl7.elm.r1.In;
import org.hl7.elm.r1.InCodeSystem;
import org.hl7.elm.r1.InValueSet;
import org.hl7.elm.r1.IsNull;
import org.hl7.elm.r1.LetClause;
import org.hl7.elm.r1.NamedTypeSpecifier;
import org.hl7.elm.r1.Not;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Query;
import org.hl7.elm.r1.RelationshipClause;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ReturnClause;
import org.hl7.elm.r1.SortByItem;
import org.hl7.elm.r1.SortClause;
import org.hl7.elm.r1.ToConcept;
import org.hl7.elm.r1.ToList;
import org.hl7.elm.r1.Tuple;
import org.hl7.elm.r1.TupleElement;
import org.hl7.elm.r1.TypeSpecifier;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.elm.r1.Xor;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm.r1.ObjectFactory;
import org.hl7.elm.r1.Or;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.PairJacksonProvider;

public class FHIRContext {

    private final ObjectFactory of = new ObjectFactory();
    private final org.hl7.cql_annotations.r1.ObjectFactory af = new org.hl7.cql_annotations.r1.ObjectFactory();
    private final String modelIdentifier = "FHIR";
    public Set<Pair<String, String>> fhirModelingSet = new HashSet<Pair<String, String>>();
    public Map<String, Pair<String, String>> valueSetMap;
    private File fhirModelingMapFile;
    private File valueSetMappingFile;

    public FHIRContext() {
        this.fhirModelingMapFile = new File(
                ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\fhirmodelingmap.txt");
        this.valueSetMappingFile = new File(
                ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\valuesetMapping.json");
        this.valueSetMap = initializeValueSetMap();
    }

    // Create a Model retriever thingy
    public final Map<String, Pair<String, String>> cdsdmToFhirMap = Map.ofEntries(
            Map.entry("EncounterEvent.encounterType", Pair.of("Encounter", "type")),
            Map.entry("EncounterEvent.", Pair.of("Encounter", "reasonReference")),
            Map.entry("EncounterEvent.relatedClinicalStatement.problem.problemCode", Pair.of("Encounter", "diagnosis")),
            Map.entry("EncounterEvent.relatedClinicalStatement.observationResult.observationValue.concept",
                    Pair.of("Encounter", "diagnosis")),
            Map.entry("EvaluatedPerson.demographics.gender", Pair.of("Patient", "gender")),
            Map.entry("EvaluatedPerson.demographics.isDeceased", Pair.of("Patient", "deceased as boolean")),
            Map.entry("ObservationOrder.observationFocus", Pair.of("ServiceRequest", "code")),
            Map.entry("ObservationOrder.observationMethod", Pair.of("Observation", "?")),
            Map.entry("ObservationResult.interpretation", Pair.of("Observation", "interpretation")),
            Map.entry("ObservationResult.observationFocus", Pair.of("Observation", "focus")),
            Map.entry("ObservationResult.observationValue.concept", Pair.of("Observation", "value as CodeableConcept")),
            Map.entry("ObservationResult.observationValue.physicalQuantity",
                    Pair.of("Observation", "value as Quantity")),
            Map.entry("Problem.problemCode", Pair.of("Condition", "code")),
            Map.entry("Problem.problemStatus", Pair.of("Condition", "clinicalStatus")),
            Map.entry("ProcedureEvent.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("ProcedureOrder.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("ProcedureProposal.procedureCode", Pair.of("Procedure", "code")),
            Map.entry("SubstanceAdministrationEvent.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                    // be a little
                                                                                    // more
                                                                                    // complicated
            Map.entry("SubstanceAdministrationEvent.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")), // This needs to
                                                        // be a little
                                                        // more
                                                        // complicated
            Map.entry("SubstanceAdministrationOrder.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                    // be a little
                                                                                    // more
                                                                                    // complicated
            Map.entry("SubstanceAdministrationProposal.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                    // be a little
                                                                                    // more
                                                                                    // complicated
            Map.entry("SubstanceDispensationEvent.substance.substanceCode",
                    Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                    // be a little
                                                                                    // more
                                                                                    // complicated
            Map.entry("SubstanceSubstanceAdministationEvent.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationAdministration", "reasonReference -> Condition.code  |  .reasonCode")),
            Map.entry("SubstanceAdministationOrder.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")),
            Map.entry("SubstanceAdministationProposal.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")),
            Map.entry("SubstanceAdministrationOrder.id", Pair.of("MedicationRequest", "id")),
            Map.entry("SubstanceDispensationEvent.relatedClinicalStatement.problem.problemCode",
                    Pair.of("MedicationRequest", "?")));

    public final Map<String, String> resourceTemplateMap = Map.ofEntries(
            Map.entry("Encounter", "http://hl7.org/fhir/StructureDefinition/Encounter"),
            Map.entry("Patient", "http://hl7.org/fhir/StructureDefinition/Patient"),
            Map.entry("Observation", "http://hl7.org/fhir/StructureDefinition/Observation"),
            Map.entry("Condition", "http://hl7.org/fhir/StructureDefinition/Condition"),
            Map.entry("Procedure", "http://hl7.org/fhir/StructureDefinition/Procedure"),
            Map.entry("MedicationRequest", "http://hl7.org/fhir/StructureDefinition/MedicationRequest"),
            Map.entry("MedicationAdministration", "http://hl7.org/fhir/StructureDefinition/MedicationAdministration"));

    private ObjectMapper mapper = new ObjectMapper();

    public void writeFHIRModelMapping() {
        if (fhirModelingMapFile.exists()) {
            fhirModelingMapFile.delete();
        }
        try {
            fhirModelingMapFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fhirModelingSet.stream().forEach(element -> IOUtil.writeToFile(fhirModelingMapFile,
                element.getLeft() + ":     " + element.getRight() + "\n"));
    }

    private void writeValueSetMapping() {
        if (fhirModelingMapFile.exists()) {
            fhirModelingMapFile.delete();
        }
        try {
            fhirModelingMapFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonResult;
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueSetMappingFile);
            IOUtil.writeToFile(fhirModelingMapFile, jsonResult);
        } catch (JsonProcessingException e) {
            System.out.println("Unable to serialize valuesetMapping");
            e.printStackTrace();
        }
    }

    public Map<String, Pair<String, String>> initializeValueSetMap() {
        if (!valueSetMappingFile.exists()) {
            writeValueSetMapping();
        }
        String jsonInput = IOUtil.readFile(valueSetMappingFile);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Pair.class, new PairJacksonProvider());
        mapper.registerModule(module);
        TypeReference<HashMap<String, Pair<String, String>>> typeRef = new TypeReference<HashMap<String, Pair<String, String>>>() {
        };
        try {
            return mapper.readValue(jsonInput, typeRef);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Expression buildCountQuery(ElmContext context, Object left, Expression right, String operator) {
        if (left instanceof Pair) {
            Expression modeling = determineModeling(context, (Pair) left);
            List<Element> elements = new ArrayList<Element>();
            List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
            AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(modeling);
            source.setResultType(modeling.getResultType());
            sources.add(source);
            AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
            if (modeling.getResultType() instanceof ListType) {
                aliasRef.setResultType(((ListType) modeling.getResultType()).getElementType());
            } else {
                aliasRef.setResultType((modeling.getResultType()));
            }
            //TODO: need to determine operator
            if (operator != null && operator.equals("==")) {
                Expression where = adaptMembershipExpression(context, "in", aliasRef, right);
                elements.add(where);
            }
            Query query = visitQuery(context, sources, elements);
            Expression distinct = context.libraryBuilder.resolveFunction("System", "Distinct", List.of(query));
            Expression count = context.libraryBuilder.resolveFunction("System", "Count", List.of(distinct));
            left = count;
            return count;
        } else {
            throw new RuntimeException("Unable to build Count query for: " + left);
        }
    }

	public Expression buildPredicate(ElmContext context, Object left, Expression right, String operator) {
        if (left instanceof Pair) {
            // determine fhir modeling
            Pair pair = (Pair) left;
            if (pair.getLeft() instanceof String && pair.getRight() instanceof String) {
                left = determineModeling(context, (Pair<String, String>)left);
                return inferOperator(context, operator, (Expression)left, right);
            } else {
                throw new RuntimeException("Unknown modeling pair: " + left + " , " + right);
            }
        } else if (left instanceof Expression) {
            return inferOperator(context, operator, (Expression)left, right);
        } else {
            throw new RuntimeException("Unknown left operand type: " + left);
        }
	}

    private Expression determineModeling(ElmContext context, Pair<String, String> left) {
        switch (left.getLeft()) {
            case ("EncounterEvent"): {
                switch (left.getRight()) {
                    case("relatedClinicalStatement/problem/problemCode"): return buildModelingA(context);
                    case("relatedClinicalStatement/observationResult/observationValue/concept"): return buildModelingB(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("SubstanceAdministrationEvent"): {
                switch (left.getRight()) {
                    case("relatedClinicalStatement/problem/problemCode"): return buildModelingC(context);
                    case("substance/substanceCode"): return buildModelingD(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("Problem"): {
                switch (left.getRight()) {
                    case("problemCode"): return buildModelingE(context);
                    case("problemStatus"): return buildModelingF(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("ObservationResult"): {
                switch (left.getRight()) {
                    case("interpretation"): return buildModelingG(context);
                    case("observationFocus"): return buildModelingH(context);
                    case("observationValue/concept"): return buildModelingI(context);
                    case("observationValue/physicalQuantity"): return buildModelingJ(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("ObservationOrder"): {
                switch (left.getRight()) {
                    case("observationFocus"): return buildModelingK(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("SubstanceAdministrationOrder"): {
                switch (left.getRight()) {
                    case("substance/substanceCode"): return buildModelingL(context);
                    case("id"): return buildModelingM(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            case ("EvaluatedPerson"): {
                switch (left.getRight()) {
                    case("demographics/isDeceased"): return buildModelingN(context);
                    default: throw new RuntimeException("Unknown vmr node: " + left.getRight());
                }
            }
            default: throw new RuntimeException("Unknown vmr template: " + left.getLeft());
        }
    }

    // [Condition: category ~ "Encounter diagnosis"] C return C.code
    private Expression buildModelingA(ElmContext context) {
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

        return simpleReturnQuery(context, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.CodeableConcept // TODO: Contextualize to the encounter?
    private Expression buildModelingB(ElmContext context) {
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

        CodeSystemRef csRef = buildCodeSystem(context, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(context, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(context, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";

        Expression where = buildWhere(context, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
    }

    // [MedicationAdministration] M return M.reasonCode // TODO: Work out the "include" capability
    //   union
    //   (
    //     [MedicationAdministration] M 
    //       let reasonConditions: [Condition: id in (M.reasonReference R return GetTail(R.reference))]
    //       return reasonConditions.code
    //   )
    private Expression buildModelingC(ElmContext context) {
        String resource = "MedicationAdministration";
        String dataPath = "reasonCode";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();

        Query simpleReasonCodeSource = simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
        Expression flattenSimpleCodeQuery = of.createFlatten().withOperand(simpleReasonCodeSource);
        flattenSimpleCodeQuery.setResultType(((ListType)simpleReasonCodeSource.getResultType()).getElementType());
        Query reasonConditionCodesSource = letMedicationReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
        Expression flattenConditionCodesQuery = of.createFlatten().withOperand(reasonConditionCodesSource);
        flattenConditionCodesQuery.setResultType(((ListType)reasonConditionCodesSource.getResultType()).getElementType());

        Expression union = of.createUnion().withOperand(flattenSimpleCodeQuery, flattenConditionCodesQuery);
        // TODO: union result type
        union.setResultType(new ListType(flattenConditionCodesQuery.getResultType()));
        Expression flattenUnion = of.createFlatten().withOperand(simpleReasonCodeSource);
        flattenUnion.setResultType(((ListType)union.getResultType()).getElementType());

        return flattenUnion;
    }

    // [MedicationAdministration] M return M.medication.code as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail(M.medication.reference)])
    //       return medicationResource.code
    //   )
    private Expression buildModelingD(ElmContext context) {
        String resource = "MedicationAdministration";
        String dataPath = "medication";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();
        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        Query simpleReasonCodeSource = simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
        Query medicationResourceCodes = letMedicationReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
        Expression flattenMedicationQuery = of.createFlatten().withOperand(medicationResourceCodes);
        flattenMedicationQuery.setResultType(((ListType)medicationResourceCodes.getResultType()).getElementType());

        Expression union = of.createUnion().withOperand(simpleReasonCodeSource, medicationResourceCodes);
        // TODO: union result type
        union.setResultType(new ListType(flattenMedicationQuery.getResultType()));
        Expression flattenUnion = of.createFlatten().withOperand(simpleReasonCodeSource);
        flattenUnion.setResultType(((ListType)union.getResultType()).getElementType());

        return flattenUnion;
    }

    // [Condition: category ~ "Problem"] C return C.code
    private Expression buildModelingE(ElmContext context) {
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

        return simpleReturnQuery(context, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // [Condition: category ~ "Problem"] C return C.clinicalStatus
    private Expression buildModelingF(ElmContext context) {
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

        return simpleReturnQuery(context, resource, retrieveDataPath, retrieveAlias, dataPath, codeComparator, systemUrl, systemName, codeId,
                codeName, codeDisplay, elements);
    }

    // flatten ([Observation] O return O.interpretation)
    private Expression buildModelingG(ElmContext context) {
        String resource = "Observation";
        String dataPath = "interpretation";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();
        Query query = simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
        Expression flatten = of.createFlatten().withOperand(query);
        flatten.setResultType(query.getReturn().getExpression().getResultType());
        return flatten;
    }

    // [Observation] O return O.code
    private Expression buildModelingH(ElmContext context) {
        String resource = "Observation";
        String dataPath = "code";
        String retrieveAlias = "O";

        List<Element> elements = new ArrayList<Element>();

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.CodeableConcept
    private Expression buildModelingI(ElmContext context) {
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

        CodeSystemRef csRef = buildCodeSystem(context, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(context, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(context, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";

        Expression where = buildWhere(context, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
    }

    // [Observation] O where O.status in { 'final', 'amended' } return O.value as FHIR.Quantity
    private Expression buildModelingJ(ElmContext context) {
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

        CodeSystemRef csRef = buildCodeSystem(context, systemUrl, systemName);
        CodeRef finalCodeRef = buildCode(context, finalId, finalName, finalDisplay, csRef);
        CodeRef ammendedCodeRef = buildCode(context, amendedId, amendedName, amendedDisplay, csRef);

        org.hl7.elm.r1.List codeRefs = of.createList().withElement(finalCodeRef, ammendedCodeRef);
        codeRefs.setResultType(new ListType(finalCodeRef.getResultType()));

        String filterPath = "status";

        Expression where = buildWhere(context, resource, codeRefs, filterPath);
        elements.add(where);

        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "Quantity"));

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
    }

    // [ServiceRequest] SR return SR.code
    private Expression buildModelingK(ElmContext context) {
        String resource = "ServiceRequest";
        String dataPath = "code";
        String retrieveAlias = "SR";

        List<Element> elements = new ArrayList<Element>();

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
    }

    // [MedicationRequest] M return M.medication as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail(M.medication.reference)])
    //       return medicationResource.code
    //   )
    private Expression buildModelingL(ElmContext context) {
        String resource = "MedicationAdministration";
        String dataPath = "medication";
        String retrieveAlias = "M";

        List<Element> elements = new ArrayList<Element>();
        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "CodeableConcept"));

        Query simpleReasonCodeSource = simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
        Query medicationResourceCodes = letMedicationReturnQuery(context, resource, dataPath, retrieveAlias, elements, null);
        Expression flattenMedicationQuery = of.createFlatten().withOperand(medicationResourceCodes);
        flattenMedicationQuery.setResultType(((ListType)medicationResourceCodes.getResultType()).getElementType());

        Expression union = of.createUnion().withOperand(simpleReasonCodeSource, medicationResourceCodes);
        // TODO: union result type
        union.setResultType(new ListType(flattenMedicationQuery.getResultType()));
        Expression flattenUnion = of.createFlatten().withOperand(simpleReasonCodeSource);
        flattenUnion.setResultType(((ListType)union.getResultType()).getElementType());

        return flattenUnion;
    }

    // ????
    private Expression buildModelingM(ElmContext context) {
        Expression unknownModeling = context.of.createList().withElement(context.libraryBuilder.createLiteral("TODO: Determine Modeling"));
        unknownModeling.setResultType(new ListType(context.libraryBuilder.resolveTypeName("System" + "." + "String")));
        return unknownModeling;
    }

    // [Patient] P return P.deceased as FHIR.boolean
    private Expression buildModelingN(ElmContext context) {
        String resource = "Patient";
        String dataPath = "deceased";
        String retrieveAlias = "P";

        List<Element> elements = new ArrayList<Element>();

        TypeSpecifier typeSpecifier = context.libraryBuilder.dataTypeToTypeSpecifier(context.libraryBuilder.resolveTypeName("FHIR", "boolean"));

        return simpleReturnQuery(context, resource, dataPath, retrieveAlias, elements, typeSpecifier);
    }
    
    private CodeRef buildCode(ElmContext context, String codeId, String codeName, String codeDisplay,
            CodeSystemRef csRef) {
        CodeDef code = context.libraryBuilder.resolveCodeRef(codeName);
        if (code == null) {
            code = of.createCodeDef().withAccessLevel(AccessModifier.PUBLIC)
                .withCodeSystem(csRef).withDisplay(codeDisplay).withId(codeId)
                .withName(codeName);
            code.setResultType(context.libraryBuilder.resolveTypeName("System", "Code"));
            context.libraryBuilder.addCode(code);
        }
        CodeRef codeRef = of.createCodeRef().withName(codeName)
            .withLibraryName(context.libraryBuilder.getLibraryIdentifier().getId());
        
        codeRef.setResultType(context.libraryBuilder.resolveTypeName("System", "Code"));
        return codeRef;
    }

    private CodeSystemRef buildCodeSystem(ElmContext context, String systemUrl, String systemName) {
        CodeSystemDef cs = context.libraryBuilder.resolveCodeSystemRef(systemName);
        if (cs == null) {
            cs = of.createCodeSystemDef().withAccessLevel(AccessModifier.PUBLIC)
                .withId(systemUrl).withName(systemName);

            cs.setResultType(context.libraryBuilder.resolveTypeName("System", "CodeSystem"));
            context.libraryBuilder.addCodeSystem(cs);
        }

        CodeSystemRef csRef = of.createCodeSystemRef().withName(systemName)
            .withLibraryName(context.libraryBuilder.getLibraryIdentifier().getId());

        csRef.setResultType(context.libraryBuilder.resolveTypeName("System", "CodeSystem"));
        return csRef;
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
    private Expression buildWhere(ElmContext context, String resource, org.hl7.elm.r1.List codeRefs, String filterPath) {
        DataType dataType = context.libraryBuilder.resolveTypeName(context.getModelIdentifier() + "." + resource);
        DataType codeType = context.libraryBuilder.resolvePath(dataType, filterPath);
        Property property = of.createProperty().withPath(resource + "." + filterPath);
        property.setResultType(codeType);

        Expression left = property;

        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("$this").withExpression(codeRefs);
        source.setResultType(source.getExpression().getResultType());
        List<Element> elements = new ArrayList<Element>();
        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);
        DataType codeStringType = context.libraryBuilder.resolvePath(context.libraryBuilder.resolveTypeName("System", "Code"), "code");
        Property codeStringProperty = of.createProperty().withPath("code");
        codeStringProperty.setResultType(codeStringType);
        IsNull isNull = of.createIsNull().withOperand(codeStringProperty);
        isNull.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
        Not not = of.createNot().withOperand(isNull);
        not.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
        elements.add(not);
        context.libraryBuilder.resolveUnaryCall("System", "Not", not);
        ReturnClause returnClause = of.createReturnClause().withExpression(codeStringProperty);
        returnClause.setResultType(new ListType(codeStringProperty.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(context, sources, elements);

        Expression right = query;
        
        Expression where = context.libraryBuilder.resolveIn(left, right);
        return where;
    }

    // <expression localId="57" locator="56:3-56:35" dataType="fhir:Condition" codeProperty="category" codeComparator="~" xsi:type="Retrieve">
    //               <codes xsi:type="ToList">
    //                  <operand locator="56:26-56:34" name="Problem" xsi:type="CodeRef"/>
    //               </codes>
    //            </expression>
    private Expression simpleReturnQuery(ElmContext context, String resource, String retrieveDataPath, String retrieveAlias, String dataPath,
            String codeComparator, String systemUrl, String systemName, String codeId, String codeName,
            String codeDisplay, List<Element> elements) {
        CodeSystemRef csRef = buildCodeSystem(context, systemUrl, systemName);
        CodeRef codeRef = buildCode(context, codeId, codeName, codeDisplay, csRef);
        Expression codeList = context.libraryBuilder.resolveToList(codeRef);
        // Retrieve retrieve = buildRetrieve(context, resource + "." + retrieveDataPath, codeRef, codeComparator);
        Retrieve retrieve = buildRetrieve(context, resource, codeList, codeComparator, retrieveDataPath);

        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);
        DataType dataType = context.libraryBuilder.resolveTypeName(context.getModelIdentifier() + "." + resource);
        DataType codeType = context.libraryBuilder.resolvePath(dataType, dataPath);
        // build ReturnClause
        // set the queryResultType to ReturnClause resultType
        // add the Return to Query
        Property property = of.createProperty().withPath(resource + "." + dataPath);
        property.setResultType(codeType);
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(context, sources, elements);
        return query;
    }

    private Query simpleReturnQuery(ElmContext context, String resource, String dataPath, String retrieveAlias, List<Element> elements,
            TypeSpecifier typeSpecifier) {
        Retrieve retrieve = buildRetrieve(context, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);
        DataType dataType = context.libraryBuilder.resolveTypeName(context.getModelIdentifier() + "." + resource);
        DataType codeType = context.libraryBuilder.resolvePath(dataType, dataPath);
        // build ReturnClause
        // set the queryResultType to ReturnClause resultType
        // add the Return to Query
        Property property = of.createProperty().withPath(resource + "." + dataPath);
        property.setResultType(codeType);

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
        Query query = visitQuery(context, sources, elements);
        return query;
    }

    // [MedicationAdministration] M return M.medication.code as FHIR.CodeableConcept
    //   union
    //   (
    //     [MedicationAdministration] M
    //       let medicationResource: First([Medication: id in GetTail(M.medication.reference)])
    //       return medicationResource.code
    //   )
    private Query letMedicationReturnQuery(ElmContext context, String resource, String dataPath, String retrieveAlias,
    List<Element> elements, Object object) {
        Retrieve retrieve = buildRetrieve(context, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);

        Expression medicationReferences = context.of.createList().withElement(context.libraryBuilder.createLiteral("TODO"));
        medicationReferences.setResultType(new ListType(context.libraryBuilder.resolveTypeName("System", "String")));
        Retrieve medicationRetrieve = buildRetrieve(context, "Medication", medicationReferences, "in", "id");

        Expression function = context.libraryBuilder.resolveFunction("System", "First", List.of(medicationRetrieve));
        LetClause let = of.createLetClause().withIdentifier("medicationAdministration").withExpression(function);
        let.setResultType(function.getResultType());
        elements.add(let);

        Property property = of.createProperty().withPath(let.getIdentifier() + "." + "code");
        DataType codeType = context.libraryBuilder.resolvePath(let.getResultType(), "code");
        property.setResultType(new ListType((DataType) codeType));
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(context, sources, elements);
        return query;
    }

    // [MedicationAdministration] M return M.reasonCode // TODO: Work out the "include" capability
    //   union
    //   (
    //     [MedicationAdministration] M 
    //       let reasonConditions: [Condition: id in (M.reasonReference R return GetTail(R.reference))]
    //       return reasonConditions.code
    //   )
    private Expression letConditionReturnQuery(ElmContext context, String resource, String dataPath, String retrieveAlias,
    List<Element> elements, Object object) {
        Retrieve retrieve = buildRetrieve(context, resource, null, null, null);
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(retrieveAlias).withExpression(retrieve);
        source.setResultType(source.getExpression().getResultType());

        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        sources.add(source);

        Expression reasonReferences = context.of.createList().withElement(context.libraryBuilder.createLiteral("TODO"));
        reasonReferences.setResultType(new ListType(context.libraryBuilder.resolveTypeName("System", "String")));
        Retrieve conditionRetrieve = buildRetrieve(context, "Condition", null, "in", "id");
        
        LetClause let = of.createLetClause().withIdentifier("reasonConditions").withExpression(conditionRetrieve);
        let.setResultType(conditionRetrieve.getResultType());
        elements.add(let);

        Property property = of.createProperty().withPath(let.getIdentifier() + "." + "code");
        DataType codeType = context.libraryBuilder.resolvePath(((ListType)let.getResultType()).getElementType(), "code");
        property.setResultType(new ListType((DataType) codeType));
        ReturnClause returnClause = of.createReturnClause().withExpression(property);
        returnClause.setResultType(new ListType(property.getResultType()));
        elements.add(returnClause);
        Query query = visitQuery(context, sources, elements);
        return query;
    }

    private Expression inferOperator(ElmContext context, String operator, Expression left, Expression right) {
        Expression operatorExpression = null;
            switch (operator) {
                case ">=":  {
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptInequalityExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptInequalityExpression(context, operator, left, right);
                    }
                    break;
                }
                case "==": {     
                    if (right instanceof ValueSetRef) {       
                        if (left.getResultType() instanceof ListType) {
                            List<Element> elements = new ArrayList<Element>();
                            List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                            AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                            sources.add(source);
                            AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                            aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                            Expression where = adaptMembershipExpression(context, "in", aliasRef, right);
                            elements.add(where);
                            Query query = visitQuery(context, sources, elements);
                            Exists exists = of.createExists().withOperand(query);
                            exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                            operatorExpression = exists;
                        }
                        else {
                            operatorExpression = adaptMembershipExpression(context, "in", left, right);
                        }
                    } else {
                        if (left.getResultType() instanceof ListType) {
                            List<Element> elements = new ArrayList<Element>();
                            List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                            AliasedQuerySource source = of.createAliasedQuerySource().withAlias(left.getClass().getName().substring(left.getClass().getName().length() - 6, left.getClass().getName().length())).withExpression(left);
                            sources.add(source);
                            AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                            aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                            Expression where = adaptEqualityExpression(context, operator, aliasRef, right);
                            elements.add(where);
                            Query query = visitQuery(context, sources, elements);
                            Exists exists = of.createExists().withOperand(query);
                            exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                            operatorExpression = exists;
                        }
                        else {
                            operatorExpression = adaptEqualityExpression(context, operator, left, right);
                        }
                    }
                    break;
                }
                case "<": {            
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptInequalityExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptInequalityExpression(context, operator, left, right);
                    }
                    break;
                }
                case ">": {
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptInequalityExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptInequalityExpression(context, operator, left, right);
                    }
                    break;
                }
                case "<=":{
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(left.getClass().getName().substring(0, 6)).withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptInequalityExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptInequalityExpression(context, operator, left, right);
                    }
                    break;
                }
                case "!=": {
                    if (right instanceof ValueSetRef) {
                        operatorExpression = adaptNotExpression(context, adaptMembershipExpression(context, "in", left, right));
                    }
                    else if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias(left.getClass().getName().substring(left.getClass().getName().length() - 6, left.getClass().getName().length())).withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptEqualityExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptEqualityExpression(context, operator, left, right);
                    }
                    break;
                }
                case "in": {            
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = adaptMembershipExpression(context, operator, aliasRef, right);
                        elements.add(where);
                        Query query = visitQuery(context, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(context.libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = adaptMembershipExpression(context, "in", left, right);
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
                return operatorExpression;
    }

    public Not adaptNotExpression(ElmContext context, Expression operand) {
        Not result = of.createNot().withOperand(operand);
        context.libraryBuilder.resolveUnaryCall("System", "Not", result);
        return result;
    }

    public Exists adaptExistenceExpression(ElmContext context, Expression operand) {
        Exists result = of.createExists().withOperand(operand);
        context.libraryBuilder.resolveUnaryCall("System", "Exists", result);
        return result;
    }

    public Expression adaptMembershipExpression(ElmContext context, String operator, Expression left, Expression right) {
        switch (operator) {
            case "in":
                return context.libraryBuilder.resolveIn(left, right);
            case "contains":
                if (left instanceof ValueSetRef) {
                    InValueSet in = of.createInValueSet()
                            .withCode(right)
                            .withValueset((ValueSetRef) left);
                    context.libraryBuilder.resolveCall("System", "InValueSet", new InValueSetInvocation(in));
                    return in;
                }

                Contains contains = of.createContains().withOperand(left, right);
                context.libraryBuilder.resolveBinaryCall("System", "Contains", contains);
                return contains;
        }

        throw new IllegalArgumentException(String.format("Unknown operator: %s", operator));
    }

    public And adaptAndExpression(ElmContext context, Expression left, Expression right) {
        And and = of.createAnd().withOperand(left,right);
        context.libraryBuilder.resolveBinaryCall("System", "And", and);
        return and;
    }

    public Expression adaptOrExpression(ElmContext context, String text, Expression left, Expression right) {
        if (text.equals("xor")) {
            Xor xor = of.createXor().withOperand(left, right);
            context.libraryBuilder.resolveBinaryCall("System", "Xor", xor);
            return xor;
        } else {
            Or or = of.createOr().withOperand(left, right);
            context.libraryBuilder.resolveBinaryCall("System", "Or", or);
            return or;
        }
    }

    public Expression adaptEqualityExpression(ElmContext context, String operator, Expression left, Expression right) {
        if (operator.equals("~") || operator.equals("!~")) {
            BinaryExpression equivalent = of.createEquivalent().withOperand(left, right);
            context.libraryBuilder.resolveBinaryCall("System", "Equivalent", equivalent);
            if (!"~".equals(operator)) {
                Not not = of.createNot().withOperand(equivalent);
                context.libraryBuilder.resolveUnaryCall("System", "Not", not);
                return not;
            }
            return equivalent;
        }
        else {
            BinaryExpression equal = of.createEqual().withOperand(left, right);
            context.libraryBuilder.resolveBinaryCall("System", "Equal", equal);
            
            if (!"=".equals(operator)) {
                Not not = of.createNot().withOperand(equal);
                context.libraryBuilder.resolveUnaryCall("System", "Not", not);
                return not;
            }
            return equal;
        }
    }

    public BinaryExpression adaptInequalityExpression(ElmContext context, String operator, Expression left, Expression right) {
        BinaryExpression exp;
        String operatorName;
        switch (operator) {
            case "<=":
                operatorName = "LessOrEqual";
                exp = of.createLessOrEqual();
                break;
            case "<":
                operatorName = "Less";
                exp = of.createLess();
                break;
            case ">":
                operatorName = "Greater";
                exp = of.createGreater();
                break;
            case ">=":
                operatorName = "GreaterOrEqual";
                exp = of.createGreaterOrEqual();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown operator: %s", operator));
        }
        exp.withOperand(left, right);
        context.libraryBuilder.resolveBinaryCall("System", operatorName, exp);
        return exp;
    }

    public Retrieve buildRetrieve(ElmContext context, String resource, Expression terminology, String codeComparator, String codeProperty) {
        // context.libraryBuilder.checkLiteralContext();
        //As of now there is only the Fhir Model
        String label = modelIdentifier + "." + resource;
        DataType dataType = context.libraryBuilder.resolveTypeName(modelIdentifier, label);
        context.libraryBuilder.resolveLabel(modelIdentifier, resource);
        if (dataType == null) {
            // ERROR:
            throw new IllegalArgumentException(String.format("Could not resolve type name %s.", label));
        }

        if (!(dataType instanceof ClassType) || !((ClassType)dataType).isRetrievable()) {
            // ERROR:
            throw new IllegalArgumentException(String.format("Specified data type %s does not support retrieval.", label));
        }

        ClassType classType = (ClassType)dataType;
        // BTR -> The original intent of this code was to have the retrieve return the base type, and use the "templateId"
        // element of the retrieve to communicate the "positive" or "negative" profile to the data access layer.
        // However, because this notion of carrying the "profile" through a type is not general, it causes inconsistencies
        // when using retrieve results with functions defined in terms of the same type (see GitHub Issue #131).
        // Based on the discussion there, the retrieve will now return the declared type, whether it is a profile or not.
        //ProfileType profileType = dataType instanceof ProfileType ? (ProfileType)dataType : null;
        //NamedType namedType = profileType == null ? classType : (NamedType)classType.getBaseType();
        NamedType namedType = classType;

        ModelInfo modelInfo = context.libraryBuilder.getModel(namedType.getNamespace()).getModelInfo();
        boolean useStrictRetrieveTyping = modelInfo.isStrictRetrieveTyping() != null && modelInfo.isStrictRetrieveTyping();

        Retrieve retrieve = of.createRetrieve()
                .withDataType(context.libraryBuilder.dataTypeToQName((DataType)namedType))
                .withTemplateId(classType.getIdentifier());

        if (terminology != null) {

            if (codeProperty != null) {
                try {
                    retrieve.setCodeProperty(codeProperty);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unkown code property: " + codeProperty);
                }
            } else if (classType.getPrimaryCodePath() != null) {
                retrieve.setCodeProperty(classType.getPrimaryCodePath());
            }

            Property property = null;
            CqlTranslatorException propertyException = null;
            if (retrieve.getCodeProperty() == null) {
                // ERROR:
                propertyException = new CqlSemanticException("Retrieve has a terminology target but does not specify a code path and the type of the retrieve does not have a primary code path defined.",
                    CqlTranslatorException.ErrorSeverity.Error);
                    context.libraryBuilder.recordParsingException(propertyException);
            }
            else {
                try {
                    DataType codeType = context.libraryBuilder.resolvePath((DataType) namedType, retrieve.getCodeProperty());
                    property = of.createProperty().withPath(retrieve.getCodeProperty());
                    property.setResultType(codeType);
                }
                catch (Exception e) {
                    // ERROR:
                    // WARNING:
                    propertyException = new CqlSemanticException(String.format("Could not resolve code path %s for the type of the retrieve %s.",
                            retrieve.getCodeProperty(), namedType.getName()), useStrictRetrieveTyping ? CqlTranslatorException.ErrorSeverity.Error : CqlTranslatorException.ErrorSeverity.Warning, e);
                            context.libraryBuilder.recordParsingException(propertyException);
                }
            }

            // Resolve the terminology target using an in or ~ operator
            try {
                if (codeComparator == null) {
                    codeComparator = terminology.getResultType().isSubTypeOf(context.libraryBuilder.resolveTypeName("System", "Vocabulary")) ? "in" : "~";
                }

                if (property == null) {
                    throw propertyException;
                }

                switch (codeComparator) {
                    case "in": {
                        Expression in = context.libraryBuilder.resolveIn(property, terminology);
                        if (in instanceof In) {
                            retrieve.setCodes(((In) in).getOperand().get(1));
                        } else if (in instanceof InValueSet) {
                            retrieve.setCodes(((InValueSet) in).getValueset());
                        } else if (in instanceof InCodeSystem) {
                            retrieve.setCodes(((InCodeSystem) in).getCodesystem());
                        } else if (in instanceof AnyInValueSet) {
                            retrieve.setCodes(((AnyInValueSet) in).getValueset());
                        } else if (in instanceof AnyInCodeSystem) {
                            retrieve.setCodes(((AnyInCodeSystem) in).getCodesystem());
                        } else {
                            // ERROR:
                            // WARNING:
                            context.libraryBuilder.recordParsingException(new CqlSemanticException(String.format("Unexpected membership operator %s in retrieve", in.getClass().getSimpleName()),
                                    useStrictRetrieveTyping ? CqlTranslatorException.ErrorSeverity.Error : CqlTranslatorException.ErrorSeverity.Warning));
                        }
                    }
                    break;

                    case "~": {
                        // Resolve with equivalent to verify the type of the target
                        BinaryExpression equivalent = of.createEquivalent().withOperand(property, terminology);
                        context.libraryBuilder.resolveBinaryCall("System", "Equivalent", equivalent);

                        // Automatically promote to a list for use in the retrieve target
                        if (!(equivalent.getOperand().get(1).getResultType() instanceof ListType)) {
                            retrieve.setCodes(context.libraryBuilder.resolveToList(equivalent.getOperand().get(1)));
                        }
                        else {
                            retrieve.setCodes(equivalent.getOperand().get(1));
                        }
                    }
                    break;

                    case "=": {
                        // Resolve with equality to verify the type of the source and target
                        BinaryExpression equal = of.createEqual().withOperand(property, terminology);
                        context.libraryBuilder.resolveBinaryCall("System", "Equal", equal);

                        // Automatically promote to a list for use in the retrieve target
                        if (!(equal.getOperand().get(1).getResultType() instanceof ListType)) {
                            retrieve.setCodes(context.libraryBuilder.resolveToList(equal.getOperand().get(1)));
                        }
                        else {
                            retrieve.setCodes(equal.getOperand().get(1));
                        }
                    }
                    break;

                    default:
                        // ERROR:
                        // WARNING:
                        context.libraryBuilder.recordParsingException(new CqlSemanticException(String.format("Unknown code comparator % in retrieve", codeComparator),
                                useStrictRetrieveTyping ? CqlTranslatorException.ErrorSeverity.Error : CqlTranslatorException.ErrorSeverity.Warning));
                }

                retrieve.setCodeComparator(codeComparator);

                // Verify that the type of the terminology target is a List<Code>
                // Due to implicit conversion defined by specific models, the resolution path above may result in a List<Concept>
                // In that case, convert to a list of code (Union the Code elements of the Concepts in the list)
                if (retrieve.getCodes() != null && retrieve.getCodes().getResultType() != null && retrieve.getCodes().getResultType() instanceof ListType
                    && ((ListType)retrieve.getCodes().getResultType()).getElementType().equals(context.libraryBuilder.resolveTypeName("System", "Concept"))) {
                    if (retrieve.getCodes() instanceof ToList) {
                        // ToList will always have a single argument
                        ToList toList = (ToList)retrieve.getCodes();
                        // If that argument is a ToConcept, replace the ToList argument with the code (skip the implicit conversion, the data access layer is responsible for it)
                        if (toList.getOperand() instanceof ToConcept) {
                            toList.setOperand(((ToConcept)toList.getOperand()).getOperand());
                        }
                        else {
                            // Otherwise, access the codes property of the resulting Concept
                            Expression codesAccessor = context.libraryBuilder.buildProperty(toList.getOperand(), "codes", toList.getOperand().getResultType());
                            retrieve.setCodes(codesAccessor);
                        }
                    }
                    else {
                        // WARNING:
                        context.libraryBuilder.recordParsingException(new CqlSemanticException("Terminology target is a list of concepts, but expects a list of codes",
                                CqlTranslatorException.ErrorSeverity.Warning));
                    }
                }
            }
            catch (Exception e) {
                // If something goes wrong attempting to resolve, just set to the expression and report it as a warning,
                // it shouldn't prevent translation unless the modelinfo indicates strict retrieve typing
                if (!(terminology.getResultType().isSubTypeOf(context.libraryBuilder.resolveTypeName("System", "Vocabulary")))) {
                    retrieve.setCodes(context.libraryBuilder.resolveToList(terminology));
                }
                else {
                    retrieve.setCodes(terminology);
                }
                retrieve.setCodeComparator(codeComparator);
                // ERROR:
                // WARNING:
                context.libraryBuilder.recordParsingException(new CqlSemanticException("Could not resolve membership operator for terminology target of the retrieve.",
                        useStrictRetrieveTyping ? CqlTranslatorException.ErrorSeverity.Error : CqlTranslatorException.ErrorSeverity.Warning, e));
            }
        }

        retrieve.setResultType(new ListType((DataType) namedType));

        return retrieve;
    }

    public Query visitQuery(ElmContext context, List<AliasedQuerySource> sources, List<Element> elements) {
        QueryContext queryContext = new QueryContext();
        context.libraryBuilder.pushQueryContext(queryContext);
        try {
            queryContext.addPrimaryQuerySources(sources);

            // If we are evaluating a population-level query whose source ranges over any patient-context expressions,
            // then references to patient context expressions within the iteration clauses of the query can be accessed
            // at the patient, rather than the population, context.
            boolean expressionContextPushed = false;
            /* TODO: Address the issue of referencing multiple context expressions within a query (or even expression in general)
            if (libraryBuilder.inUnfilteredContext() && queryContext.referencesSpecificContext()) {
                libraryBuilder.pushExpressionContext("Patient");
                expressionContextPushed = true;
            }
            */
            try {

                List<LetClause> dfcx = new ArrayList<>();
                List<RelationshipClause> qicx = new ArrayList<>();
                Expression where = null;
                AggregateClause agg = null;
                ReturnClause ret = null;

                for (Element element : elements) {
                    if (element instanceof LetClause) {
                        dfcx.add((LetClause) element);
                    } else if (element instanceof RelationshipClause) {
                        qicx.add((RelationshipClause) element);
                    } else if (element instanceof Expression) {
                        where = (Expression) element;
                    } else if (element instanceof AggregateClause) {
                        agg = (AggregateClause) element;
                    } else if (element instanceof ReturnClause) {
                        ret = (ReturnClause) element;
                    }
                }

                if ((agg == null) && (ret == null) && (sources.size() > 1)) {
                    ret = of.createReturnClause()
                            .withDistinct(true);

                    Tuple returnExpression = of.createTuple();
                    TupleType returnType = new TupleType();
                    for (AliasedQuerySource aqs : sources) {
                        TupleElement element =
                                of.createTupleElement()
                                        .withName(aqs.getAlias())
                                        .withValue(of.createAliasRef().withName(aqs.getAlias()));
                        DataType sourceType = aqs.getResultType() instanceof ListType ? ((ListType)aqs.getResultType()).getElementType() : aqs.getResultType();
                        element.getValue().setResultType(sourceType); // Doesn't use the fluent API to avoid casting
                        element.setResultType(element.getValue().getResultType());
                        returnType.addElement(new TupleTypeElement(element.getName(), element.getResultType()));
                        returnExpression.getElement().add(element);
                    }

                    returnExpression.setResultType(queryContext.isSingular() ? returnType : new ListType(returnType));
                    ret.setExpression(returnExpression);
                    ret.setResultType(returnExpression.getResultType());
                }

                queryContext.removeQuerySources(sources);
                if (dfcx != null) {
                    queryContext.removeLetClauses(dfcx);
                }

                DataType queryResultType = null;
                if (agg != null) {
                    queryResultType = agg.getResultType();
                }
                else if (ret != null) {
                    queryResultType = ret.getResultType();
                }
                else {
                    queryResultType = sources.get(0).getResultType();
                }

                SortClause sort = null;
                for (Element element : elements) {
                    if (element instanceof SortClause) {
                        sort = (SortClause) element;
                    }
                }

                if (agg == null) {
                    queryContext.setResultElementType(queryContext.isSingular() ? null : ((ListType) queryResultType).getElementType());
                    if (sort != null) {
                        if (queryContext.isSingular()) {
                            // ERROR:
                            throw new IllegalArgumentException("Sort clause cannot be used in a singular query.");
                        }
                        queryContext.enterSortClause();
                        try {
                            // Validate that the sort can be performed based on the existence of comparison operators for all types involved
                            for (SortByItem sortByItem : sort.getBy()) {
                                if (sortByItem instanceof ByDirection) {
                                    // validate that there is a comparison operator defined for the result element type of the query context
                                    context.libraryBuilder.verifyComparable(queryContext.getResultElementType());
                                } else {
                                    context.libraryBuilder.verifyComparable(sortByItem.getResultType());
                                }
                            }
                        } finally {
                            queryContext.exitSortClause();
                        }
                    }
                }
                else {
                    if (sort != null) {
                        // ERROR:
                        throw new IllegalArgumentException("Sort clause cannot be used in an aggregate query.");
                    }
                }

                Query query = of.createQuery()
                        .withSource(sources)
                        .withLet(dfcx)
                        .withRelationship(qicx)
                        .withWhere(where)
                        .withReturn(ret)
                        .withAggregate(agg)
                        .withSort(sort);

                query.setResultType(queryResultType);
                return query;
            }
            finally {
                if (expressionContextPushed) {
                    context.libraryBuilder.popExpressionContext();
                }
            }

        } finally {
            context.libraryBuilder.popQueryContext();
        }
    }

    public Object visitAliasedQuerySource(Expression querySource, String alias) {
        AliasedQuerySource source = of.createAliasedQuerySource().withExpression(querySource).withAlias(alias);
        source.setResultType(source.getExpression().getResultType());
        return source;
    }

    public Object visitWhereClause(Expression expression, ElmContext context) {
        DataTypes.verifyType(expression.getResultType(), context.libraryBuilder.resolveTypeName("System", "Boolean"));
        return expression;
    }
}
