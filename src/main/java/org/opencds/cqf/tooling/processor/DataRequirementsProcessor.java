package org.opencds.cqf.tooling.processor;

import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.NamespaceManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.cql.model.IntervalType;
import org.hl7.cql.model.ListType;
import org.hl7.cql.model.NamedType;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.opencds.cqf.tooling.visitor.ElmRequirement;
import org.opencds.cqf.tooling.visitor.ElmRequirementsContext;
import org.opencds.cqf.tooling.visitor.ElmRequirementsVisitor;

import javax.xml.bind.JAXBElement;
import java.io.Serializable;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * @author Adam Stevenson
 */
public class DataRequirementsProcessor {

    private java.util.List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
    public java.util.List<ValidationMessage> getValidationMessages() {
        return this.validationMessages;
    }

    public Library gatherDataRequirements(LibraryManager libraryManager, TranslatedLibrary translatedLibrary, CqlTranslatorOptions options, Set<String> expressions, boolean includeLogicDefinitions) {
        if (libraryManager == null) {
            throw new IllegalArgumentException("libraryManager required");
        }

        if (translatedLibrary == null) {
            throw new IllegalArgumentException("translatedLibrary required");
        }

        ElmRequirementsVisitor visitor = new ElmRequirementsVisitor();
        ElmRequirementsContext context = new ElmRequirementsContext(libraryManager, options, visitor);

        if (expressions == null) {
            visitor.visitLibrary(translatedLibrary.getLibrary(), context);
        }
        else {
            context.enterLibrary(translatedLibrary.getIdentifier());
            try {
                for (String expression : expressions) {
                    visitor.visitElement(translatedLibrary.resolve(expression), context);
                }
            }
            finally {
                context.exitLibrary();
            }
        }

        return createLibrary(context, includeLogicDefinitions);
    }

    private Library createLibrary(ElmRequirementsContext context, boolean includeLogicDefinitions) {
        Library returnLibrary = new Library();
        returnLibrary.setStatus(Enumerations.PublicationStatus.ACTIVE);
        CodeableConcept libraryType = new CodeableConcept();
        Coding typeCoding = new Coding().setCode("module-definition");
        typeCoding.setSystem("http://terminology.hl7.org/CodeSystem/library-type");
        libraryType.addCoding(typeCoding);
        returnLibrary.setType(libraryType);
        returnLibrary.setDate(new Date());
        returnLibrary.setSubject(extractSubject(context));
        returnLibrary.getExtension().addAll(extractDirectReferenceCodes(context));
        returnLibrary.getRelatedArtifact().addAll(extractRelatedArtifacts(context));
        returnLibrary.getDataRequirement().addAll(extractDataRequirements(context));
        returnLibrary.getParameter().addAll(extractParameters(context));
        if (includeLogicDefinitions) {
            returnLibrary.getExtension().addAll(extractLogicDefinitions(context));
        }
        return returnLibrary;

    }

    private CodeableConcept extractSubject(ElmRequirementsContext context) {
        // TODO: Determine context (defaults to Patient if not set, so not critical until we have a non-patient-context use case)
        return null;
    }

    private List<Extension> extractDirectReferenceCodes(ElmRequirementsContext context) {
        List<Extension> result = new ArrayList<>();

        for (ElmRequirement def : context.getRequirements().getCodeDefs()) {
            result.add(toDirectReferenceCode(context, def.getLibraryIdentifier(), (CodeDef)def.getElement()));
        }

        return result;
    }

    private Extension toDirectReferenceCode(ElmRequirementsContext context, VersionedIdentifier libraryIdentifier, CodeDef def) {
        Extension e = new Extension();
        // TODO: Promote this extension to the base specification
        e.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode");
        e.setValue(toCoding(context, libraryIdentifier, context.toCode(def)));
        return e;
    }

    private List<RelatedArtifact> extractRelatedArtifacts(ElmRequirementsContext context) {
        List<RelatedArtifact> result = new ArrayList<>();

        // Report model dependencies
        // URL for a model info is: [baseCanonical]/Library/[model-name]-ModelInfo
        for (ElmRequirement def : context.getRequirements().getUsingDefs()) {
            // System model info is an implicit dependency, do not report
            if (!((UsingDef)def.getElement()).getLocalIdentifier().equals("System")) {
                result.add(toRelatedArtifact(def.getLibraryIdentifier(), (UsingDef)def.getElement()));
            }
        }

        // Report library dependencies
        for (ElmRequirement def : context.getRequirements().getIncludeDefs()) {
            result.add(toRelatedArtifact(def.getLibraryIdentifier(), (IncludeDef)def.getElement()));
        }

        // Report CodeSystem dependencies
        for (ElmRequirement def : context.getRequirements().getCodeSystemDefs()) {
            result.add(toRelatedArtifact(def.getLibraryIdentifier(), (CodeSystemDef)def.getElement()));
        }

        // Report ValueSet dependencies
        for (ElmRequirement def : context.getRequirements().getValueSetDefs()) {
            result.add(toRelatedArtifact(def.getLibraryIdentifier(), (ValueSetDef)def.getElement()));
        }

        return result;
    }

    private List<ParameterDefinition> extractParameters(ElmRequirementsContext context) {
        List<ParameterDefinition> result = new ArrayList<>();

        for (ElmRequirement def : context.getRequirements().getParameterDefs()) {
            result.add(toParameterDefinition(def.getLibraryIdentifier(), (ParameterDef)def.getElement()));
        }

        for (ElmRequirement def : context.getRequirements().getExpressionDefs()) {
            if (!(def.getElement() instanceof FunctionDef) && (((ExpressionDef)def.getElement()).getAccessLevel() == null
                    || ((ExpressionDef)def.getElement()).getAccessLevel() == AccessModifier.PUBLIC)) {
                result.add(toOutputParameterDefinition(def.getLibraryIdentifier(), (ExpressionDef)def.getElement()));
            }
        }

        return result;
    }

    private org.hl7.cql_annotations.r1.Annotation getAnnotation(Element e) {
        for (Object o : e.getAnnotation()) {
            if (o instanceof org.hl7.cql_annotations.r1.Annotation) {
                return (org.hl7.cql_annotations.r1.Annotation)o;
            }
        }

        return null;
    }

    private String toNarrativeText(org.hl7.cql_annotations.r1.Annotation a) {
        StringBuilder sb = new StringBuilder();
        if (a.getS() != null) {
            addNarrativeText(sb, a.getS());
        }
        return sb.toString();
    }

    private void addNarrativeText(StringBuilder sb, org.hl7.cql_annotations.r1.Narrative n) {
        for (Serializable s : n.getContent()) {
            if (s instanceof org.hl7.cql_annotations.r1.Narrative) {
                addNarrativeText(sb, (org.hl7.cql_annotations.r1.Narrative)s);
            }
            else if (s instanceof String) {
                sb.append((String)s);
            }
            // TODO: THIS IS WRONG... SHOULDN'T NEED TO KNOW ABOUT JAXB TO ACCOMPLISH THIS
            else if (s instanceof JAXBElement<?>) {
                JAXBElement<?> j = (JAXBElement<?>)s;
                if (j.getValue() instanceof org.hl7.cql_annotations.r1.Narrative) {
                    addNarrativeText(sb, (org.hl7.cql_annotations.r1.Narrative)j.getValue());
                }
            }
        }
    }

    private List<Extension> extractLogicDefinitions(ElmRequirementsContext context) {
        List<Extension> result = new ArrayList<Extension>();

        int sequence = 0;
        for (ElmRequirement req : context.getRequirements().getExpressionDefs()) {
            ExpressionDef def = (ExpressionDef)req.getElement();
            org.hl7.cql_annotations.r1.Annotation a = getAnnotation(def);
            if (a != null) {
                result.add(toLogicDefinition(req, def, toNarrativeText(a), sequence++));
            }
        }

        return result;
    }

    private StringType toString(String value) {
        StringType result = new StringType();
        result.setValue(value);
        return result;
    }

    private IntegerType toInteger(int value) {
        IntegerType result = new IntegerType();
        result.setValue(value);
        return result;
    }

    private Extension toLogicDefinition(ElmRequirement req, ExpressionDef def, String text, int sequence) {
        Extension e = new Extension();
        e.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition");
        // TODO: Include the libraryUrl
        e.addExtension(new Extension().setUrl("libraryName").setValue(toString(req.getLibraryIdentifier().getId())));
        e.addExtension(new Extension().setUrl("name").setValue(toString(def.getName())));
        e.addExtension(new Extension().setUrl("statement").setValue(toString(text)));
        e.addExtension(new Extension().setUrl("displaySequence").setValue(toInteger(sequence)));
        return e;
    }

    private List<DataRequirement> extractDataRequirements(ElmRequirementsContext context) {
        List<DataRequirement> result = new ArrayList<>();

        for (ElmRequirement retrieve : context.getRequirements().getRetrieves()) {
            result.add(toDataRequirement(context, retrieve.getLibraryIdentifier(), (Retrieve)retrieve.getElement()));
        }

        return result;
    }

    private org.hl7.fhir.r5.model.RelatedArtifact toRelatedArtifact(VersionedIdentifier libraryIdentifier, UsingDef usingDef) {
        return new org.hl7.fhir.r5.model.RelatedArtifact()
                .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setDisplay(usingDef.getLocalIdentifier() != null ? String.format("%s model information", usingDef.getLocalIdentifier()) : null) // Could potentially look for a well-known comment tag too, @description?
                .setResource(getModelInfoReferenceUrl(usingDef.getUri(),
                        usingDef.getLocalIdentifier(), usingDef.getVersion()));
    }

    /*
    Override the referencing URL for the FHIR-ModelInfo library
    This is required because models do not have a "namespace" in the same way that libraries do,
    so there is no way for the UsingDefinition to have a Uri that is different than the expected URI that the
    providers understand. I.e. model names and model URIs are one-to-one.
     */
    private String mapModelInfoUri(String uri, String name) {
        if (name.equals("FHIR") && uri.equals("http://hl7.org/fhir")) {
            return "http://fhir.org/guides/cqf/common";
        }
        return uri;
    }

    private String getModelInfoReferenceUrl(String uri, String name, String version) {
        if (uri != null) {
            return String.format("%s/Library/%s-ModelInfo%s", mapModelInfoUri(uri, name), name, version != null ? ("|" + version) : "");
        }

        return String.format("Library/%-ModelInfo%s", name, version != null ? ("|" + version) : "");
    }

    private org.hl7.fhir.r5.model.RelatedArtifact toRelatedArtifact(VersionedIdentifier libraryIdentifier, IncludeDef includeDef) {
        return new org.hl7.fhir.r5.model.RelatedArtifact()
                .setType(org.hl7.fhir.r5.model.RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setDisplay(includeDef.getLocalIdentifier() != null ? String.format("Library %s", includeDef.getLocalIdentifier()) : null) // Could potentially look for a well-known comment tag too, @description?
                .setResource(getReferenceUrl(includeDef.getPath(), includeDef.getVersion()));
    }

    private String getReferenceUrl(String path, String version) {
        String uri = NamespaceManager.getUriPart(path);
        String name = NamespaceManager.getNamePart(path);

        if (uri != null) {
            // The translator has no way to correctly infer the namespace of the FHIRHelpers library, since it will happily provide that source to any namespace that wants it
            // So override the declaration here so that it points back to the FHIRHelpers library in the base specification
            if (name.equals("FHIRHelpers") && !(uri.equals("http://hl7.org/fhir") || uri.equals("http://fhir.org/guides/cqf/common"))) {
                uri = "http://fhir.org/guides/cqf/common";
            }
            return String.format("%s/Library/%s%s", uri, name, version != null ? ("|" + version) : "");
        }

        return String.format("Library/%s%s", path, version != null ? ("|" + version) : "");
    }

    private org.hl7.fhir.r5.model.RelatedArtifact toRelatedArtifact(VersionedIdentifier libraryIdentifier, CodeSystemDef codeSystemDef) {
        return new org.hl7.fhir.r5.model.RelatedArtifact()
                .setType(org.hl7.fhir.r5.model.RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setDisplay(String.format("Code system %s", codeSystemDef.getName()))
                .setResource(toReference(codeSystemDef));
    }

    private org.hl7.fhir.r5.model.RelatedArtifact toRelatedArtifact(VersionedIdentifier libraryIdentifier, ValueSetDef valueSetDef) {
        return new org.hl7.fhir.r5.model.RelatedArtifact()
                .setType(org.hl7.fhir.r5.model.RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setDisplay(String.format("Value set %s", valueSetDef.getName()))
                .setResource(toReference(valueSetDef));
    }

    private ParameterDefinition toParameterDefinition(VersionedIdentifier libraryIdentifier, ParameterDef def) {
        org.hl7.cql.model.DataType parameterType = def.getResultType() instanceof ListType
                ? ((ListType)def.getResultType()).getElementType() : def.getResultType();

        AtomicBoolean isList = new AtomicBoolean(false);
        Enumerations.FHIRAllTypes typeCode = Enumerations.FHIRAllTypes.fromCode(toFHIRParameterTypeCode(parameterType, def.getName(), isList));

        return new ParameterDefinition()
                .setName(def.getName())
                .setUse(Enumerations.OperationParameterUse.IN)
                .setMin(0)
                .setMax(isList.get() ? "*" : "1")
                .setType(typeCode);
    }

    private ParameterDefinition toOutputParameterDefinition(VersionedIdentifier libraryIdentifier, ExpressionDef def) {
        AtomicBoolean isList = new AtomicBoolean(false);
        Enumerations.FHIRAllTypes typeCode = Enumerations.FHIRAllTypes.fromCode(
                toFHIRResultTypeCode(def.getResultType(), def.getName(), isList));

        return new ParameterDefinition()
                .setName(def.getName())
                .setUse(Enumerations.OperationParameterUse.OUT)
                .setMin(0)
                .setMax(isList.get() ? "*" : "1")
                .setType(typeCode);
    }

    private String toFHIRResultTypeCode(org.hl7.cql.model.DataType dataType, String defName, AtomicBoolean isList) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        String resultCode = toFHIRTypeCode(dataType, isValid, isList);
        if (!isValid.get()) {
            // Issue a warning that the result type is not supported
            validationMessages.add(new ValidationMessage(ValidationMessage.Source.Publisher, ValidationMessage.IssueType.NOTSUPPORTED, "CQL Library Packaging",
                    String.format("Result type %s of definition %s is not supported; implementations may not be able to use the result of this expression",
                            dataType.toLabel(), defName), ValidationMessage.IssueSeverity.WARNING));
        }

        return resultCode;
    }

    private String toFHIRParameterTypeCode(org.hl7.cql.model.DataType dataType, String parameterName, AtomicBoolean isList) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        String resultCode = toFHIRTypeCode(dataType, isValid, isList);
        if (!isValid.get()) {
            // Issue a warning that the parameter type is not supported
            validationMessages.add(new ValidationMessage(ValidationMessage.Source.Publisher, ValidationMessage.IssueType.NOTSUPPORTED, "CQL Library Packaging",
                    String.format("Parameter type %s of parameter %s is not supported; reported as FHIR.Any", dataType.toLabel(), parameterName), ValidationMessage.IssueSeverity.WARNING));
        }

        return resultCode;
    }

    private String toFHIRTypeCode(org.hl7.cql.model.DataType dataType, AtomicBoolean isValid, AtomicBoolean isList) {
        isList.set(false);
        if (dataType instanceof ListType) {
            isList.set(true);
            return toFHIRTypeCode(((ListType)dataType).getElementType(), isValid);
        }

        return toFHIRTypeCode(dataType, isValid);
    }

    private String toFHIRTypeCode(org.hl7.cql.model.DataType dataType, AtomicBoolean isValid) {
        isValid.set(true);
        if (dataType instanceof NamedType) {
            switch (((NamedType)dataType).getName()) {
                case "System.Boolean": return "boolean";
                case "System.Integer": return "integer";
                case "System.Decimal": return "decimal";
                case "System.Date": return "date";
                case "System.DateTime": return "dateTime";
                case "System.Time": return "time";
                case "System.String": return "string";
                case "System.Quantity": return "Quantity";
                case "System.Ratio": return "Ratio";
                case "System.Any": return "Any";
                case "System.Code": return "Coding";
                case "System.Concept": return "CodeableConcept";
            }

            if ("FHIR".equals(((NamedType)dataType).getNamespace())) {
                return ((NamedType)dataType).getSimpleName();
            }
        }

        if (dataType instanceof IntervalType) {
            if (((IntervalType)dataType).getPointType() instanceof NamedType) {
                switch (((NamedType)((IntervalType)dataType).getPointType()).getName()) {
                    case "System.Date":
                    case "System.DateTime": return "Period";
                    case "System.Quantity": return "Range";
                }
            }
        }

        isValid.set(false);
        return "Any";
    }

    private org.hl7.fhir.r5.model.DataRequirement toDataRequirement(ElmRequirementsContext context, VersionedIdentifier libraryIdentifier, Retrieve retrieve) {
        org.hl7.fhir.r5.model.DataRequirement dr = new org.hl7.fhir.r5.model.DataRequirement();

        dr.setType(org.hl7.fhir.r5.model.Enumerations.FHIRAllTypes.fromCode(retrieve.getDataType().getLocalPart()));

        // Set profile if specified
        if (retrieve.getTemplateId() != null) {
            dr.setProfile(Collections.singletonList(new org.hl7.fhir.r5.model.CanonicalType(retrieve.getTemplateId())));
        }

        // Set code path if specified
        if (retrieve.getCodeProperty() != null) {
            org.hl7.fhir.r5.model.DataRequirement.DataRequirementCodeFilterComponent cfc =
                    new org.hl7.fhir.r5.model.DataRequirement.DataRequirementCodeFilterComponent();

            cfc.setPath(retrieve.getCodeProperty());

            // TODO: Support retrieval when the target is a CodeSystemRef

            if (retrieve.getCodes() instanceof ValueSetRef) {
                ValueSetRef vsr = (ValueSetRef)retrieve.getCodes();
                cfc.setValueSet(toReference(context.resolveValueSetRef(libraryIdentifier, vsr)));
            }

            if (retrieve.getCodes() instanceof org.hl7.elm.r1.ToList) {
                org.hl7.elm.r1.ToList toList = (org.hl7.elm.r1.ToList)retrieve.getCodes();
                resolveCodeFilterCodes(context, libraryIdentifier, cfc, toList.getOperand());
            }

            if (retrieve.getCodes() instanceof org.hl7.elm.r1.List) {
                org.hl7.elm.r1.List codeList = (org.hl7.elm.r1.List)retrieve.getCodes();
                for (Expression e : codeList.getElement()) {
                    resolveCodeFilterCodes(context, libraryIdentifier, cfc, e);
                }
            }

            dr.getCodeFilter().add(cfc);
        }

        // TODO: Set date range filters if literal

        return dr;
    }

    private void resolveCodeFilterCodes(ElmRequirementsContext context, VersionedIdentifier libraryIdentifier,
                                        org.hl7.fhir.r5.model.DataRequirement.DataRequirementCodeFilterComponent cfc,
                                        Expression e) {
        if (e instanceof org.hl7.elm.r1.CodeRef) {
            CodeRef cr = (CodeRef)e;
            cfc.addCode(toCoding(context, libraryIdentifier, context.toCode(context.resolveCodeRef(libraryIdentifier, cr))));
        }

        if (e instanceof org.hl7.elm.r1.Code) {
            cfc.addCode(toCoding(context, libraryIdentifier, (org.hl7.elm.r1.Code)e));
        }

        if (e instanceof org.hl7.elm.r1.ConceptRef) {
            ConceptRef cr = (ConceptRef)e;
            org.hl7.fhir.r5.model.CodeableConcept c = toCodeableConcept(context, libraryIdentifier,
                    context.toConcept(libraryIdentifier, context.resolveConceptRef(libraryIdentifier, cr)));
            for (org.hl7.fhir.r5.model.Coding code : c.getCoding()) {
                cfc.addCode(code);
            }
        }

        if (e instanceof org.hl7.elm.r1.Concept) {
            org.hl7.fhir.r5.model.CodeableConcept c = toCodeableConcept(context, libraryIdentifier, (org.hl7.elm.r1.Concept)e);
            for (org.hl7.fhir.r5.model.Coding code : c.getCoding()) {
                cfc.addCode(code);
            }
        }
    }

    private org.hl7.fhir.r5.model.Coding toCoding(ElmRequirementsContext context, VersionedIdentifier libraryIdentifier, Code code) {
        CodeSystemDef codeSystemDef = context.resolveCodeSystemRef(libraryIdentifier, code.getSystem());
        org.hl7.fhir.r5.model.Coding coding = new org.hl7.fhir.r5.model.Coding();
        coding.setCode(code.getCode());
        coding.setDisplay(code.getDisplay());
        coding.setSystem(codeSystemDef.getId());
        coding.setVersion(codeSystemDef.getVersion());
        return coding;
    }

    private org.hl7.fhir.r5.model.CodeableConcept toCodeableConcept(ElmRequirementsContext context,
                                                                    VersionedIdentifier libraryIdentifier,
                                                                    Concept concept) {
        org.hl7.fhir.r5.model.CodeableConcept codeableConcept = new org.hl7.fhir.r5.model.CodeableConcept();
        codeableConcept.setText(concept.getDisplay());
        for (Code code : concept.getCode()) {
            codeableConcept.addCoding(toCoding(context, libraryIdentifier, code));
        }
        return codeableConcept;
    }

    private String toReference(CodeSystemDef codeSystemDef) {
        return codeSystemDef.getId() + (codeSystemDef.getVersion() != null ? ("|" + codeSystemDef.getVersion()) : "");
    }

    private String toReference(ValueSetDef valueSetDef) {
        return valueSetDef.getId() + (valueSetDef.getVersion() != null ? ("|" + valueSetDef.getVersion()) : "");
    }
}
