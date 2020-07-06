package org.opencds.cqf.utilities;

import java.util.Map;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.elm.r1.Code;
import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Concept;
import org.hl7.elm.r1.ConceptDef;
import org.hl7.elm.r1.ConceptRef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class R4FHIRUtils {

    public static Coding toCoding(Code code, TranslatedLibrary library, LibraryManager libraryManager) {
        CodeSystemDef codeSystemDef = resolveCodeSystemRef(code.getSystem(), library, libraryManager);
        Coding coding = new Coding();
        coding.setCode(code.getCode());
        coding.setDisplay(code.getDisplay());
        coding.setSystem(codeSystemDef.getId());
        coding.setVersion(codeSystemDef.getVersion());
        return coding;
    }

    public static CodeableConcept toCodeableConcept(Concept concept, TranslatedLibrary library, LibraryManager libraryManager) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(concept.getDisplay());
        for (Code code : concept.getCode()) {
            codeableConcept.addCoding(toCoding(code, library, libraryManager));
        }
        return codeableConcept;
    }

    public static String toReference(CodeSystemDef codeSystemDef) {
        return codeSystemDef.getId() + (codeSystemDef.getVersion() != null ? ("|" + codeSystemDef.getVersion()) : "");
    }

    public static String toReference(ValueSetDef valueSetDef) {
        return valueSetDef.getId() + (valueSetDef.getVersion() != null ? ("|" + valueSetDef.getVersion()) : "");
    }

    // TODO: Move to the CQL-to-ELM translator

    public static Concept toConcept(ConceptDef conceptDef, TranslatedLibrary library, LibraryManager libraryManager) {
        Concept concept = new Concept();
        concept.setDisplay(conceptDef.getDisplay());
        for (CodeRef codeRef : conceptDef.getCode()) {
            concept.getCode().add(toCode(resolveCodeRef(codeRef, library, libraryManager)));
        }
        return concept;
    }

    public static Code toCode(CodeDef codeDef) {
        return new Code().withCode(codeDef.getId()).withSystem(codeDef.getCodeSystem()).withDisplay(codeDef.getDisplay());
    }

    public static CodeDef resolveCodeRef(CodeRef codeRef, TranslatedLibrary library, LibraryManager libraryManager) {
        // If the reference is to another library, resolve to that library
        if (codeRef.getLibraryName() != null) {
            library = resolveLibrary(codeRef.getLibraryName(), library, libraryManager);
        }

        return library.resolveCodeRef(codeRef.getName());
    }

    public static ConceptDef resolveConceptRef(ConceptRef conceptRef, TranslatedLibrary library, LibraryManager libraryManager) {
        // If the reference is to another library, resolve to that library
        if (conceptRef.getLibraryName() != null) {
            library = resolveLibrary(conceptRef.getLibraryName(), library, libraryManager);
        }

        return library.resolveConceptRef(conceptRef.getName());
    }

    public static CodeSystemDef resolveCodeSystemRef(CodeSystemRef codeSystemRef, TranslatedLibrary library, LibraryManager libraryManager) {
        if (codeSystemRef.getLibraryName() != null) {
            library = resolveLibrary(codeSystemRef.getLibraryName(), library, libraryManager);
        }

        return library.resolveCodeSystemRef(codeSystemRef.getName());
    }

    public static ValueSetDef resolveValueSetRef(ValueSetRef valueSetRef, TranslatedLibrary library, LibraryManager libraryManager) {
        // If the reference is to another library, resolve to that library
        if (valueSetRef.getLibraryName() != null) {
            library = resolveLibrary(valueSetRef.getLibraryName(), library, libraryManager);
        }

        return library.resolveValueSetRef(valueSetRef.getName());
    }

    public static ValueSetDef resolveValueSetRef(ValueSetRef valueSetRef, TranslatedLibrary library, Map<String, TranslatedLibrary> translatedLibraries) {
        // If the reference is to another library, resolve to that library
        if (valueSetRef.getLibraryName() != null) {
            library = resolveLibrary(valueSetRef.getLibraryName(), library, translatedLibraries);
        }

        return library.resolveValueSetRef(valueSetRef.getName());
    }

    public static TranslatedLibrary resolveLibrary(String localLibraryName, TranslatedLibrary library, LibraryManager libraryManager) {
        IncludeDef includeDef = library.resolveIncludeRef(localLibraryName);
        return resolveLibrary(libraryManager, new VersionedIdentifier().withId(includeDef.getPath()).withVersion(includeDef.getVersion()));
    }

    public static TranslatedLibrary resolveLibrary(LibraryManager libraryManager, VersionedIdentifier libraryIdentifier) {
        if (libraryManager.getTranslatedLibraries().containsKey(libraryIdentifier.getId())) {
            return libraryManager.getTranslatedLibraries().get(libraryIdentifier.getId());
        }

        throw new IllegalArgumentException(String.format("Could not resolve reference to translated library %s", libraryIdentifier.getId()));
    }

    public static TranslatedLibrary resolveLibrary(String localLibraryName, TranslatedLibrary library, Map<String, TranslatedLibrary> translatedLibraries) {
        IncludeDef includeDef = library.resolveIncludeRef(localLibraryName);
        return resolveLibrary(translatedLibraries, new VersionedIdentifier().withId(includeDef.getPath()).withVersion(includeDef.getVersion()));
    }

    public static TranslatedLibrary resolveLibrary(Map<String, TranslatedLibrary> translatedLibraries, VersionedIdentifier libraryIdentifier) {
        if (translatedLibraries.containsKey(libraryIdentifier.getId())) {
            return translatedLibraries.get(libraryIdentifier.getId());
        }

        throw new IllegalArgumentException(String.format("Could not resolve reference to translated library %s", libraryIdentifier.getId()));
    }
}
