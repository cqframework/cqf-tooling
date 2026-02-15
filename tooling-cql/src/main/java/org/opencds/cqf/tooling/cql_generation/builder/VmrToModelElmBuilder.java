package org.opencds.cqf.tooling.cql_generation.builder;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.QueryContext;
import org.cqframework.cql.cql2elm.model.invocation.InValueSetInvocation;
import org.hl7.cql.model.*;
import org.hl7.elm.r1.*;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.*;

// Some of these methods should probably live in LibraryBuilder... possible all
/**
 * @author Joshua Reynolds
 */
public abstract class VmrToModelElmBuilder {
    public final ObjectFactory of = new ObjectFactory();
    public final org.hl7.cql_annotations.r1.ObjectFactory af = new org.hl7.cql_annotations.r1.ObjectFactory();
    protected String modelIdentifier;
    protected String modelVersion;
    protected String modelUri;
    protected LibrarySourceProvider lsp;
    protected IncludeDef includeHelper;
    protected DecimalFormat decimalFormat;
    protected Logger logger;
    protected Map<String, Marker> markers = new HashMap<String, Marker>();


    public VmrToModelElmBuilder(String modelIdentifier, String modelVersion, String modelUri, LibrarySourceProvider lsp, DecimalFormat decimalFormat) {
        this.modelIdentifier = modelIdentifier;
        this.modelVersion = modelVersion;
        this.modelUri = modelUri;
        this.lsp = lsp;
        this.decimalFormat = decimalFormat;
        this.decimalFormat.setParseBigDecimal(true);
        logger = LoggerFactory.getLogger(this.getClass());   
        markers.put("Modeling", MarkerFactory.getMarker("Modeling"));                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    }

    // These two methods are only appropriate for building predicates...  need to abstract this a bit in the future.

    // Not really sure if this is the best signature....
    public abstract Expression resolveModeling(LibraryBuilder libraryBuilder, Pair<String, String> left, Expression right, String operator);

    // This may be more appropriate to live in DroolPredicateToElmExpressionAdapter
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Expression resolvePredicate(LibraryBuilder libraryBuilder, Object left, Expression right, String operator) {
        if (left instanceof Pair) {
            Pair pair = (Pair) left;
            if (pair.getLeft() instanceof String && pair.getRight() instanceof String) {
                logger.debug(markers.get("Modeling"), "Resolving Modeling for left");
                left = resolveModeling(libraryBuilder, (Pair<String, String>)left, right, operator);
                return resolveOperator(libraryBuilder, operator, (Expression)left, right);
            } else {
                logger.debug(markers.get("Modeling"), "Unknown Vmr Modeling for left, {}", pair);
                throw new RuntimeException("Unknown modeling pair: " + pair.getLeft() + " , " + pair.getRight());
            }
        } else if (left instanceof Expression) {
            return resolveOperator(libraryBuilder, operator, (Expression)left, right);
        } else {
            logger.debug("Unkown type left, {}", left);
            throw new RuntimeException("Unknown left operand type: " + left);
        }
	}

    private Expression resolveOperator(LibraryBuilder libraryBuilder, String operator, Expression left, Expression right) {
        logger.debug("resolving operator");
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
                        Expression where = resolveInequality(libraryBuilder, operator, aliasRef, right);
                        elements.add(where);
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveInequality(libraryBuilder, operator, left, right);
                    }
                    break;
                }
                case "==": {    
                    if (right instanceof ValueSetRef || right instanceof CodeRef) {       
                        if (left.getResultType() instanceof ListType) {
                            List<Element> elements = new ArrayList<Element>();
                            List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                            AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                            sources.add(source);
                            AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                            aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                            elements.add(resolveWhere(libraryBuilder, left, right, elements, aliasRef));
                             Query query = resolveQuery(libraryBuilder, sources, elements);
                            Exists exists = of.createExists().withOperand(query);
                            exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                            operatorExpression = exists;
                        }
                        else {
                            operatorExpression = resolveMembership(libraryBuilder, "in", left, right);
                        }
                    } else {
                        if (left.getResultType() instanceof ListType) {
                            List<Element> elements = new ArrayList<Element>();
                            List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                            AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                            sources.add(source);
                            AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                            aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                            Expression where = resolveEquality(libraryBuilder, operator, aliasRef, right);
                            elements.add(where);
                            Query query = resolveQuery(libraryBuilder, sources, elements);
                            Exists exists = of.createExists().withOperand(query);
                            exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                            operatorExpression = exists;
                        }
                        else {
                            operatorExpression = resolveEquality(libraryBuilder, operator, left, right);
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
                        Expression where = resolveInequality(libraryBuilder, operator, aliasRef, right);
                        elements.add(where);
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveInequality(libraryBuilder, operator, left, right);
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
                        Expression where = resolveInequality(libraryBuilder, operator, aliasRef, right);
                        elements.add(where);
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveInequality(libraryBuilder, operator, left, right);
                    }
                    break;
                }
                case "<=":{
                    if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = resolveInequality(libraryBuilder, operator, aliasRef, right);
                        elements.add(where);
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveInequality(libraryBuilder, operator, left, right);
                    }
                    break;
                }
                case "!=": {
                    if (right instanceof ValueSetRef || right instanceof CodeRef) {
                        operatorExpression = resolveNot(libraryBuilder, resolveMembership(libraryBuilder, "in", left, right));
                    }
                    else if (left.getResultType() instanceof ListType) {
                        List<Element> elements = new ArrayList<Element>();
                        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
                        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(left);
                        sources.add(source);
                        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
                        aliasRef.setResultType(((ListType) left.getResultType()).getElementType());
                        Expression where = resolveEquality(libraryBuilder, operator, aliasRef, right);
                        elements.add(where);
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveEquality(libraryBuilder, operator, left, right);
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
                        elements.add(resolveWhere(libraryBuilder, left, right, elements, aliasRef));
                        Query query = resolveQuery(libraryBuilder, sources, elements);
                        Exists exists = of.createExists().withOperand(query);
                        exists.setResultType(libraryBuilder.resolveTypeName("System", "Boolean"));
                        operatorExpression = exists;
                    }
                    else {
                        operatorExpression = resolveMembership(libraryBuilder, "in", left, right);
                    }
                    break;
                }
                default:
                logger.debug("Unkown Operator: " + operator);
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
                return operatorExpression;
    }
    
    public Expression resolveCountQuery(LibraryBuilder libraryBuilder, Expression expression, Expression right, String operator) {
        logger.debug(markers.get("Modeling"), "Resolving Count Query");
        List<Element> elements = new ArrayList<Element>();
        List<AliasedQuerySource> sources = new ArrayList<AliasedQuerySource>();
        AliasedQuerySource source = of.createAliasedQuerySource().withAlias("Alias").withExpression(expression);
        source.setResultType(expression.getResultType());
        sources.add(source);
        AliasRef aliasRef = of.createAliasRef().withName(source.getAlias());
        if (expression.getResultType() instanceof ListType) {
            aliasRef.setResultType(((ListType) expression.getResultType()).getElementType());
        } else {
            aliasRef.setResultType((expression.getResultType()));
        }
        //TODO: need to determine operator
        if (operator != null && operator.equals("==")) {
            Expression where = resolveMembership(libraryBuilder, "in", aliasRef, right);
            elements.add(where);
        }
        Query query = resolveQuery(libraryBuilder, sources, elements);
        Expression distinct = libraryBuilder.resolveFunction("System", "Distinct", Arrays.asList(query));
        Expression count = libraryBuilder.resolveFunction("System", "Count", Arrays.asList(distinct));
        return count;
    }

    private Expression resolveWhere(LibraryBuilder libraryBuilder, Expression left, Expression right, List<Element> elements,
            AliasRef aliasRef) {
        logger.debug(markers.get("Modeling"), "Resolving Where");
        if (right instanceof CodeRef) {
            return resolveEquality(libraryBuilder, "~", aliasRef, right);
        } else if (right instanceof ValueSetRef) {
            return resolveMembership(libraryBuilder, "in", aliasRef, right);
        } else {
            throw new RuntimeException("Unable to build predicate expression for: " + left.getResultType() + " and " + right.getResultType());
        }
    }
    
    public Not resolveNot(LibraryBuilder libraryBuilder, Expression operand) {
        logger.debug(markers.get("Modeling"), "Resolving Not");
        Not result = of.createNot().withOperand(operand);
        libraryBuilder.resolveUnaryCall("System", "Not", result);
        return result;
    }

    public Exists resolveExistence(LibraryBuilder libraryBuilder, Expression operand) {
        logger.debug(markers.get("Modeling"), "Resolving Exists");
        Exists result = of.createExists().withOperand(operand);
        libraryBuilder.resolveUnaryCall("System", "Exists", result);
        return result;
    }

    public Expression resolveMembership(LibraryBuilder libraryBuilder, String operator, Expression left, Expression right) {
        switch (operator) {
            case "in":
                logger.debug(markers.get("Modeling"), "Resolving In");
                return libraryBuilder.resolveIn(left, right);
            case "contains":
                if (left instanceof ValueSetRef) {
                    logger.debug(markers.get("Modeling"), "Resolving InValueSet");
                    InValueSet in = of.createInValueSet()
                            .withCode(right)
                            .withValueset((ValueSetRef) left);
                    libraryBuilder.resolveCall("System", "InValueSet", new InValueSetInvocation(in));
                    return in;
                }

                Contains contains = of.createContains().withOperand(left, right);
                logger.debug(markers.get("Modeling"), "Resolving Contains");
                libraryBuilder.resolveBinaryCall("System", "Contains", contains);
                return contains;
        }

        throw new IllegalArgumentException(String.format("Unknown operator: %s", operator));
    }

    public And resolveAnd(LibraryBuilder libraryBuilder, Expression left, Expression right) {
        logger.debug(markers.get("Modeling"), "Resolving And");
        And and = of.createAnd().withOperand(left,right);
        libraryBuilder.resolveBinaryCall("System", "And", and);
        return and;
    }

    public Expression adaptOrExpression(LibraryBuilder libraryBuilder, String text, Expression left, Expression right) {
        if (text.equals("xor")) {
            logger.debug(markers.get("Modeling"), "Resolving Xor");
            Xor xor = of.createXor().withOperand(left, right);
            libraryBuilder.resolveBinaryCall("System", "Xor", xor);
            return xor;
        } else {
            logger.debug(markers.get("Modeling"), "Resolving Or");
            Or or = of.createOr().withOperand(left, right);
            libraryBuilder.resolveBinaryCall("System", "Or", or);
            return or;
        }
    }

    public Expression resolveEquality(LibraryBuilder libraryBuilder, String operator, Expression left, Expression right) {
        if (operator.equals("~") || operator.equals("!~")) {
            BinaryExpression equivalent = of.createEquivalent().withOperand(left, right);
            logger.debug(markers.get("Modeling"), "Resolving Equivalent");
            libraryBuilder.resolveBinaryCall("System", "Equivalent", equivalent);
            if (!"~".equals(operator)) {
                logger.debug(markers.get("Modeling"), "Resolving Not");
                Not not = of.createNot().withOperand(equivalent);
                libraryBuilder.resolveUnaryCall("System", "Not", not);
                return not;
            }
            return equivalent;
        }
        else {
            logger.debug(markers.get("Modeling"), "Resolving Equal");
            BinaryExpression equal = of.createEqual().withOperand(left, right);
            libraryBuilder.resolveBinaryCall("System", "Equal", equal);
            
            if (!"=".equals(operator)) {
                logger.debug(markers.get("Modeling"), "Resolving Not");
                Not not = of.createNot().withOperand(equal);
                libraryBuilder.resolveUnaryCall("System", "Not", not);
                return not;
            }
            return equal;
        }
    }

    public BinaryExpression resolveInequality(LibraryBuilder libraryBuilder, String operator, Expression left, Expression right) {
        BinaryExpression exp;
        String operatorName;
        switch (operator) {
            case "<=":
                logger.debug(markers.get("Modeling"), "Resolving LessOrEqual");
                operatorName = "LessOrEqual";
                exp = of.createLessOrEqual();
                break;
            case "<":
                logger.debug(markers.get("Modeling"), "Resolving Less");
                operatorName = "Less";
                exp = of.createLess();
                break;
            case ">":
                logger.debug(markers.get("Modeling"), "Resolving Greater");
                operatorName = "Greater";
                exp = of.createGreater();
                break;
            case ">=":
                logger.debug(markers.get("Modeling"), "Resolving GreaterOrEqual");
                operatorName = "GreaterOrEqual";
                exp = of.createGreaterOrEqual();
                break;
            default:
                logger.debug(markers.get("Modeling"), "Unknown operator: ", operator);
                throw new IllegalArgumentException(String.format("Unknown operator: %s", operator));
        }
        exp.withOperand(left, right);
        libraryBuilder.resolveBinaryCall("System", operatorName, exp);
        return exp;
    }

    public Retrieve resolveRetrieve(LibraryBuilder libraryBuilder, String resource, Expression terminology, String codeComparator, String codeProperty) {
        // libraryBuilder.checkLiteralContext();
        //As of now there is only the Fhir Model
        logger.debug(markers.get("Modeling"), "Resolving Retrieve");
        String label = modelIdentifier + "." + resource;
        DataType dataType = libraryBuilder.resolveTypeName(modelIdentifier, label);
        libraryBuilder.resolveLabel(modelIdentifier, resource);
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

        ModelInfo modelInfo = libraryBuilder.getModel(namedType.getNamespace()).getModelInfo();
        boolean useStrictRetrieveTyping = modelInfo.isStrictRetrieveTyping() != null && modelInfo.isStrictRetrieveTyping();

        Retrieve retrieve = of.createRetrieve()
                .withDataType(libraryBuilder.dataTypeToQName((DataType)namedType))
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
            CqlCompilerException propertyException = null;
            if (retrieve.getCodeProperty() == null) {
                // ERROR:
                propertyException = new CqlSemanticException("Retrieve has a terminology target but does not specify a code path and the type of the retrieve does not have a primary code path defined.",
                    CqlCompilerException.ErrorSeverity.Error);
                    libraryBuilder.recordParsingException(propertyException);
            }
            else {
                try {
                    DataType codeType = libraryBuilder.resolvePath((DataType) namedType, retrieve.getCodeProperty());
                    property = of.createProperty().withPath(retrieve.getCodeProperty());
                    property.setResultType(codeType);
                }
                catch (Exception e) {
                    // ERROR:
                    // WARNING:
                    propertyException = new CqlSemanticException(String.format("Could not resolve code path %s for the type of the retrieve %s.",
                            retrieve.getCodeProperty(), namedType.getName()), useStrictRetrieveTyping ? CqlCompilerException.ErrorSeverity.Error : CqlCompilerException.ErrorSeverity.Warning, e);
                            libraryBuilder.recordParsingException(propertyException);
                }
            }

            // Resolve the terminology target using an in or ~ operator
            try {
                if (codeComparator == null) {
                    codeComparator = terminology.getResultType().isSubTypeOf(libraryBuilder.resolveTypeName("System", "Vocabulary")) ? "in" : "~";
                }

                if (property == null) {
                    throw propertyException;
                }

                switch (codeComparator) {
                    case "in": {
                        Expression in = libraryBuilder.resolveIn(property, terminology);
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
                            libraryBuilder.recordParsingException(new CqlSemanticException(String.format("Unexpected membership operator %s in retrieve", in.getClass().getSimpleName()),
                                    useStrictRetrieveTyping ? CqlCompilerException.ErrorSeverity.Error : CqlCompilerException.ErrorSeverity.Warning));
                        }
                    }
                    break;

                    case "~": {
                        // Resolve with equivalent to verify the type of the target
                        BinaryExpression equivalent = of.createEquivalent().withOperand(property, terminology);
                        libraryBuilder.resolveBinaryCall("System", "Equivalent", equivalent);

                        // Automatically promote to a list for use in the retrieve target
                        if (!(equivalent.getOperand().get(1).getResultType() instanceof ListType)) {
                            retrieve.setCodes(libraryBuilder.resolveToList(equivalent.getOperand().get(1)));
                        }
                        else {
                            retrieve.setCodes(equivalent.getOperand().get(1));
                        }
                    }
                    break;

                    case "=": {
                        // Resolve with equality to verify the type of the source and target
                        BinaryExpression equal = of.createEqual().withOperand(property, terminology);
                        libraryBuilder.resolveBinaryCall("System", "Equal", equal);

                        // Automatically promote to a list for use in the retrieve target
                        if (!(equal.getOperand().get(1).getResultType() instanceof ListType)) {
                            retrieve.setCodes(libraryBuilder.resolveToList(equal.getOperand().get(1)));
                        }
                        else {
                            retrieve.setCodes(equal.getOperand().get(1));
                        }
                    }
                    break;

                    default:
                        // ERROR:
                        // WARNING:
                        libraryBuilder.recordParsingException(new CqlSemanticException(String.format("Unknown code comparator %s in retrieve", codeComparator),
                                useStrictRetrieveTyping ? CqlCompilerException.ErrorSeverity.Error : CqlCompilerException.ErrorSeverity.Warning));
                }

                retrieve.setCodeComparator(codeComparator);

                // Verify that the type of the terminology target is a List<Code>
                // Due to implicit conversion defined by specific models, the resolution path above may result in a List<Concept>
                // In that case, convert to a list of code (Union the Code elements of the Concepts in the list)
                if (retrieve.getCodes() != null && retrieve.getCodes().getResultType() != null && retrieve.getCodes().getResultType() instanceof ListType
                    && ((ListType)retrieve.getCodes().getResultType()).getElementType().equals(libraryBuilder.resolveTypeName("System", "Concept"))) {
                    if (retrieve.getCodes() instanceof ToList) {
                        // ToList will always have a single argument
                        ToList toList = (ToList)retrieve.getCodes();
                        // If that argument is a ToConcept, replace the ToList argument with the code (skip the implicit conversion, the data access layer is responsible for it)
                        if (toList.getOperand() instanceof ToConcept) {
                            toList.setOperand(((ToConcept)toList.getOperand()).getOperand());
                        }
                        else {
                            // Otherwise, access the codes property of the resulting Concept
                            Expression codesAccessor = libraryBuilder.buildProperty(toList.getOperand(), "codes", false, toList.getOperand().getResultType());
                            retrieve.setCodes(codesAccessor);
                        }
                    }
                    else {
                        // WARNING:
                        libraryBuilder.recordParsingException(new CqlSemanticException("Terminology target is a list of concepts, but expects a list of codes",
                                CqlCompilerException.ErrorSeverity.Warning));
                    }
                }
            }
            catch (Exception e) {
                // If something goes wrong attempting to resolve, just set to the expression and report it as a warning,
                // it shouldn't prevent translation unless the modelinfo indicates strict retrieve typing
                if (!(terminology.getResultType().isSubTypeOf(libraryBuilder.resolveTypeName("System", "Vocabulary")))) {
                    retrieve.setCodes(libraryBuilder.resolveToList(terminology));
                }
                else {
                    retrieve.setCodes(terminology);
                }
                retrieve.setCodeComparator(codeComparator);
                // ERROR:
                // WARNING:
                libraryBuilder.recordParsingException(new CqlSemanticException("Could not resolve membership operator for terminology target of the retrieve.",
                        useStrictRetrieveTyping ? CqlCompilerException.ErrorSeverity.Error : CqlCompilerException.ErrorSeverity.Warning, e));
            }
        }

        retrieve.setResultType(new ListType((DataType) namedType));

        return retrieve;
    }

    public Query resolveQuery(LibraryBuilder libraryBuilder, List<AliasedQuerySource> sources, List<Element> elements) {
        logger.debug(markers.get("Modeling"), "Resolving Query");
        QueryContext queryContext = new QueryContext();
        libraryBuilder.pushQueryContext(queryContext);
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
                                    libraryBuilder.verifyComparable(queryContext.getResultElementType());
                                } else {
                                    libraryBuilder.verifyComparable(sortByItem.getResultType());
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
                    libraryBuilder.popExpressionContext();
                }
            }

        } finally {
            libraryBuilder.popQueryContext();
        }
    }

    public Object resolveAliasedQuerySource(Expression querySource, String alias) {
        logger.debug(markers.get("Modeling"), "Resolving Aliased Query Source");
        AliasedQuerySource source = of.createAliasedQuerySource().withExpression(querySource).withAlias(alias);
        source.setResultType(source.getExpression().getResultType());
        return source;
    }

    public Object resolveWhereClause(Expression expression, LibraryBuilder libraryBuilder) {
        logger.debug(markers.get("Modeling"), "Resolving Where Clause");
        DataTypes.verifyType(expression.getResultType(), libraryBuilder.resolveTypeName("System", "Boolean"));
        return expression;
    }

    public CodeSystemRef resolveCodeSystem(LibraryBuilder libraryBuilder, String systemUrl, String systemName) {
        logger.debug(markers.get("Modeling"), "Resolving CodeSystem");
        CodeSystemDef cs = libraryBuilder.resolveCodeSystemRef(systemName);
        if (cs == null) {
            logger.debug(markers.get("Modeling"), "Creating new CodeSystem Definition");
            cs = of.createCodeSystemDef().withAccessLevel(AccessModifier.PUBLIC)
                .withId(systemUrl).withName(systemName);

            cs.setResultType(libraryBuilder.resolveTypeName("System", "CodeSystem"));
            libraryBuilder.addCodeSystem(cs);
        }

        CodeSystemRef csRef = of.createCodeSystemRef().withName(systemName);

        csRef.setResultType(libraryBuilder.resolveTypeName("System", "CodeSystem"));
        return csRef;
    }
    
    public CodeRef resolveCode(LibraryBuilder libraryBuilder, String codeId, String codeName, String codeDisplay,
            CodeSystemRef csRef) {
        logger.debug(markers.get("Modeling"), "Resolving Code");
        CodeDef code = libraryBuilder.resolveCodeRef(codeName);
        if (code == null) {
            logger.debug(markers.get("Modeling"), "Creating new Code Definition");
            code = of.createCodeDef().withAccessLevel(AccessModifier.PUBLIC)
                .withCodeSystem(csRef).withDisplay(codeDisplay).withId(codeId)
                .withName(codeName);
            code.setResultType(libraryBuilder.resolveTypeName("System", "Code"));
            libraryBuilder.addCode(code);
        }
        CodeRef codeRef = of.createCodeRef().withName(codeName);
        
        codeRef.setResultType(libraryBuilder.resolveTypeName("System", "Code"));
        return codeRef;
    }

    public Expression resolveValueSet(LibraryBuilder libraryBuilder, String url, String name) {
        logger.debug(markers.get("Modeling"), "Resolving ValueSet");
        ValueSetDef vs = libraryBuilder.resolveValueSetRef(name);
        if (vs == null) {
            logger.debug(markers.get("Modeling"), "Creating new ValueSet Definition");
            vs = of.createValueSetDef()
                .withAccessLevel(AccessModifier.PUBLIC)
                .withName(name)
                .withId(url);
            vs.setResultType(libraryBuilder.resolveTypeName("System", "ValueSet"));
            libraryBuilder.addValueSet(vs);
        }
        ValueSetRef valueSetRef = of.createValueSetRef().withName(name);
        valueSetRef.setResultType(libraryBuilder.resolveTypeName("System", "ValueSet"));
        return valueSetRef;
    }

    public BigDecimal parseDecimal(String value) {
        try {
            BigDecimal result = (BigDecimal)decimalFormat.parse(value);
            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse number literal: %s", value), e);
        }
    }

	public LibrarySourceProvider getLibrarySourceProvider() {
		return lsp;
	}

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public void setLibrarySourceProvider(LibrarySourceProvider lsp) {
        this.lsp = lsp;
    }

    public IncludeDef getIncludeHelper() {
        return includeHelper;
    }

    public void setIncludeHelper(IncludeDef includeHelper) {
        this.includeHelper = includeHelper;
    }

    public String getModelUri() {
        return modelUri;
    }

    public void setModelUri(String modelUri) {
        this.modelUri = modelUri;
    }
}
