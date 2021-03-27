package org.opencds.cqf.tooling.visitor;

import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.NamespaceManager;
import org.cqframework.cql.cql2elm.model.LibraryRef;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.elm.r1.*;

import java.util.*;

public class ElmRequirementsContext {

    private CqlTranslatorOptions options;
    public CqlTranslatorOptions getOptions() {
        return options;
    }
    public void setOptions(CqlTranslatorOptions options) {
        this.options = options;
    }

    private LibraryManager libraryManager;
    public LibraryManager getLibraryManager() {
        return libraryManager;
    }

    private Stack<VersionedIdentifier> libraryStack = new Stack<VersionedIdentifier>();
    public void enterLibrary(VersionedIdentifier libraryIdentifier) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("Library Identifier must be provided");
        }
        libraryStack.push(libraryIdentifier);
    }
    public void exitLibrary() {
        libraryStack.pop();
    }
    public VersionedIdentifier getCurrentLibraryIdentifier() {
        if (libraryStack.empty()) {
            throw new IllegalArgumentException("Not in a library context");
        }

        return libraryStack.peek();
    }

    private Set<Element> visited = new HashSet<Element>();

    private ElmRequirements requirements = new ElmRequirements();
    public ElmRequirements getRequirements() {
        return requirements;
    }

    private ElmRequirementsVisitor visitor;
    public ElmRequirementsVisitor getVisitor() {
        return visitor;
    }

    public ElmRequirementsContext(LibraryManager libraryManager, CqlTranslatorOptions options, ElmRequirementsVisitor visitor) {
        if (libraryManager == null) {
            throw new IllegalArgumentException("Library Manager required");
        }
        this.libraryManager = libraryManager;
        this.options = options;

        if (visitor == null) {
            throw new IllegalArgumentException("visitor required");
        }
        this.visitor = visitor;
    }

    private void reportRequirement(Element element) {
        visited.add(element);
        requirements.reportRequirement(getCurrentLibraryIdentifier(), element);
    }

    public void reportUsingDef(UsingDef usingDef) {
        reportRequirement(usingDef);
    }

    public void reportIncludeDef(IncludeDef includeDef) {
        reportRequirement(includeDef);
    }

    public void reportContextDef(ContextDef contextDef) {
        reportRequirement(contextDef);
    }

    public void reportCodeDef(CodeDef codeDef) {
        reportRequirement(codeDef);
    }

    public void reportCodeSystemDef(CodeSystemDef codeSystemDef) {
        reportRequirement(codeSystemDef);
    }

    public void reportConceptDef(ConceptDef conceptDef) {
        reportRequirement(conceptDef);
    }

    public void reportParameterDef(ParameterDef parameterDef) {
        reportRequirement(parameterDef);
    }

    public void reportValueSetDef(ValueSetDef valueSetDef) {
        reportRequirement(valueSetDef);
    }

    public void reportExpressionDef(ExpressionDef expressionDef) {
        if (!(expressionDef instanceof FunctionDef)) {
            reportRequirement(expressionDef);
        }
    }

    public void reportFunctionDef(FunctionDef functionDef) {
        reportRequirement(functionDef);
    }

    /*
    Prepares a library visit if necessary (i.e. localLibraryName is not null) and returns the associated translated
    library. If there is no localLibraryName, returns the current library.
     */
    private TranslatedLibrary prepareLibraryVisit(VersionedIdentifier libraryIdentifier, String localLibraryName) {
        TranslatedLibrary targetLibrary = resolveLibrary(libraryIdentifier);
        if (localLibraryName != null) {
            IncludeDef includeDef = targetLibrary.resolveIncludeRef(localLibraryName);
            if (!visited.contains(includeDef)) {
                visitor.visitElement(includeDef, this);
            }
            targetLibrary = resolveLibraryFromIncludeDef(includeDef);
            enterLibrary(targetLibrary.getIdentifier());
        }
        return targetLibrary;
    }

    private void unprepareLibraryVisit(String localLibraryName) {
        if (localLibraryName != null) {
            exitLibrary();
        }
    }

    public void reportCodeRef(CodeRef codeRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), codeRef.getLibraryName());
        try {
            CodeDef cd = targetLibrary.resolveCodeRef(codeRef.getName());
            if (!visited.contains(cd)) {
                visitor.visitElement(cd, this);
            }
        }
        finally {
            unprepareLibraryVisit(codeRef.getLibraryName());
        }
    }

    public void reportCodeSystemRef(CodeSystemRef codeSystemRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), codeSystemRef.getLibraryName());
        try {
            CodeSystemDef csd = targetLibrary.resolveCodeSystemRef(codeSystemRef.getName());
            if (!visited.contains(csd)) {
                visitor.visitElement(csd, this);
            }
        }
        finally {
            unprepareLibraryVisit(codeSystemRef.getLibraryName());
        }
    }

    public void reportConceptRef(ConceptRef conceptRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), conceptRef.getLibraryName());
        try {
            ConceptDef cd = targetLibrary.resolveConceptRef(conceptRef.getName());
            if (!visited.contains(cd)) {
                visitor.visitElement(cd, this);
            }
        }
        finally {
            unprepareLibraryVisit(conceptRef.getLibraryName());
        }
    }

    public void reportParameterRef(ParameterRef parameterRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), parameterRef.getLibraryName());
        try {
            ParameterDef pd = targetLibrary.resolveParameterRef(parameterRef.getName());
            if (!visited.contains(pd)) {
                visitor.visitElement(pd, this);
            }
        }
        finally {
            unprepareLibraryVisit(parameterRef.getLibraryName());
        }
    }

    public void reportValueSetRef(ValueSetRef valueSetRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), valueSetRef.getLibraryName());
        try {
            ValueSetDef vsd = targetLibrary.resolveValueSetRef(valueSetRef.getName());
            if (!visited.contains(vsd)) {
                visitor.visitElement(vsd, this);
            }
        }
        finally {
            unprepareLibraryVisit(valueSetRef.getLibraryName());
        }
    }

    public void reportExpressionRef(ExpressionRef expressionRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), expressionRef.getLibraryName());
        try {
            ExpressionDef ed = targetLibrary.resolveExpressionRef(expressionRef.getName());
            if (!visited.contains(ed)) {
                visitor.visitElement(ed, this);

                // TODO: Report context?
            }
        }
        finally {
            unprepareLibraryVisit(expressionRef.getLibraryName());
        }
    }

    public void reportFunctionRef(FunctionRef functionRef) {
        TranslatedLibrary targetLibrary = prepareLibraryVisit(getCurrentLibraryIdentifier(), functionRef.getLibraryName());
        try {
            // TODO: Needs full operator resolution to be able to distinguish overloads.
            // For now, reports all overloads
            for (ExpressionDef def : targetLibrary.getLibrary().getStatements().getDef()) {
                if (def instanceof FunctionDef && def.getName().equals(functionRef.getName())) {
                    if (!visited.contains(def)) {
                        visitor.visitElement(def, this);
                    }
                }
            }
        }
        finally {
            unprepareLibraryVisit(functionRef.getLibraryName());
        }
    }

    public void reportRetrieve(Retrieve retrieve) {
        reportRequirement(retrieve);
    }

    public Concept toConcept(ElmRequirement conceptDef) {
        return toConcept(conceptDef.getLibraryIdentifier(), (ConceptDef)conceptDef.getElement());
    }

    public org.hl7.elm.r1.Concept toConcept(VersionedIdentifier libraryIdentifier, ConceptDef conceptDef) {
        org.hl7.elm.r1.Concept concept = new org.hl7.elm.r1.Concept();
        concept.setDisplay(conceptDef.getDisplay());
        for (org.hl7.elm.r1.CodeRef codeRef : conceptDef.getCode()) {
            concept.getCode().add(toCode(resolveCodeRef(libraryIdentifier, codeRef)));
        }
        return concept;
    }

    public org.hl7.elm.r1.Code toCode(CodeDef codeDef) {
        return new org.hl7.elm.r1.Code().withCode(codeDef.getId()).withSystem(codeDef.getCodeSystem()).withDisplay(codeDef.getDisplay());
    }

    public CodeDef resolveCodeRef(ElmRequirement codeRef) {
        return resolveCodeRef(codeRef.getLibraryIdentifier(), (CodeRef)codeRef.getElement());
    }

    public org.hl7.elm.r1.CodeDef resolveCodeRef(VersionedIdentifier libraryIdentifier, CodeRef codeRef) {
        // If the reference is to another library, resolve to that library
        if (codeRef.getLibraryName() != null) {
            return resolveLibrary(libraryIdentifier, codeRef.getLibraryName()).resolveCodeRef(codeRef.getName());
        }

        return resolveLibrary(libraryIdentifier).resolveCodeRef(codeRef.getName());
    }

    public org.hl7.elm.r1.ConceptDef resolveConceptRef(ElmRequirement conceptRef) {
        return resolveConceptRef(conceptRef.getLibraryIdentifier(), (ConceptRef)conceptRef.getElement());
    }

    public org.hl7.elm.r1.ConceptDef resolveConceptRef(VersionedIdentifier libraryIdentifier, ConceptRef conceptRef) {
        if (conceptRef.getLibraryName() != null) {
            return resolveLibrary(libraryIdentifier, conceptRef.getLibraryName()).resolveConceptRef(conceptRef.getName());
        }

        return resolveLibrary(libraryIdentifier).resolveConceptRef(conceptRef.getName());
    }

    public CodeSystemDef resolveCodeSystemRef(ElmRequirement codeSystemRef) {
        return resolveCodeSystemRef(codeSystemRef.getLibraryIdentifier(), (CodeSystemRef)codeSystemRef.getElement());
    }

    public CodeSystemDef resolveCodeSystemRef(VersionedIdentifier libraryIdentifier, CodeSystemRef codeSystemRef) {
        if (codeSystemRef.getLibraryName() != null) {
            return resolveLibrary(libraryIdentifier, codeSystemRef.getLibraryName()).resolveCodeSystemRef(codeSystemRef.getName());
        }

        return resolveLibrary(libraryIdentifier).resolveCodeSystemRef(codeSystemRef.getName());
    }

    public ValueSetDef resolveValueSetRef(ElmRequirement valueSetRef) {
        return resolveValueSetRef(valueSetRef.getLibraryIdentifier(), (ValueSetRef)valueSetRef.getElement());
    }

    public ValueSetDef resolveValueSetRef(VersionedIdentifier libraryIdentifier, ValueSetRef valueSetRef) {
        if (valueSetRef.getLibraryName() != null) {
            return resolveLibrary(libraryIdentifier, valueSetRef.getLibraryName()).resolveValueSetRef(valueSetRef.getName());
        }

        return resolveLibrary(libraryIdentifier).resolveValueSetRef(valueSetRef.getName());
    }

    public TranslatedLibrary resolveLibrary(ElmRequirement libraryRef) {
        return resolveLibrary(libraryRef.getLibraryIdentifier(), ((LibraryRef)libraryRef.getElement()).getLibraryName());
    }

    public IncludeDef resolveIncludeRef(VersionedIdentifier libraryIdentifier, String localLibraryName) {
        TranslatedLibrary targetLibrary = resolveLibrary(libraryIdentifier);
        return targetLibrary.resolveIncludeRef(localLibraryName);
    }

    public TranslatedLibrary resolveLibrary(VersionedIdentifier libraryIdentifier, String localLibraryName) {
        IncludeDef includeDef = resolveIncludeRef(libraryIdentifier, localLibraryName);
        return resolveLibraryFromIncludeDef(includeDef);
    }

    public TranslatedLibrary resolveLibraryFromIncludeDef(IncludeDef includeDef) {
        VersionedIdentifier targetLibraryIdentifier = new VersionedIdentifier()
                .withSystem(NamespaceManager.getUriPart(includeDef.getPath()))
                .withId(NamespaceManager.getNamePart(includeDef.getPath()))
                .withVersion(includeDef.getVersion());

        return resolveLibrary(targetLibraryIdentifier);
    }

    public TranslatedLibrary resolveLibrary(VersionedIdentifier libraryIdentifier) {
        // TODO: Need to support loading from ELM so we don't need options.
        ArrayList<CqlTranslatorException> errors = new ArrayList<CqlTranslatorException>();
        TranslatedLibrary referencedLibrary = libraryManager.resolveLibrary(libraryIdentifier, options, errors);
        // TODO: Report translation errors here...
        //for (CqlTranslatorException error : errors) {
        //    this.recordParsingException(error);
        //}

        return referencedLibrary;
    }
}
