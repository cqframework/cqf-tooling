package org.opencds.cqf.library;

import java.util.Collections;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.instance.model.api.INarrative;
import org.opencds.cqf.library.stu3.NarrativeProvider;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.dstu3.model.*;

public class LibraryProcessor {
    public static String getId(String baseId) {
        return "library-" + baseId;
    }

    public static void refreshLibraryContent(String cqlContentPath, String libraryPath, FhirContext fhirContext, Encoding encoding) {
        Boolean shouldPersist = true;
        Library resource;
        try {
            resource = (Library)IOUtils.readResource(libraryPath, fhirContext, shouldPersist);
        } catch (Exception e) {
            LogUtils.putWarning(libraryPath, e.getMessage());
            resource = null;
        }

        CqlTranslator translator;
        try {
            translator = getTranslator(cqlContentPath);
        } catch (Exception e) {
            LogUtils.putWarning(libraryPath, e.getMessage());
            return;
        }
         
        try {
            if (resource != null) {            
                refreshLibrary(resource, cqlContentPath, libraryPath, encoding, translator, fhirContext);
            } else {
                generateLibrary(cqlContentPath, libraryPath, encoding, translator, fhirContext);
            }
        } catch (Exception e) {
            LogUtils.putWarning(libraryPath, e.getMessage());
        }
    }

    private static void refreshLibrary(Library referenceLibrary, String cqlContentPath, String outputPath, Encoding encoding, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(cqlContentPath, translator, fhirContext);
        mergeDiff(referenceLibrary, generatedLibrary, cqlContentPath, translator, fhirContext);
        IOUtils.writeResource(generatedLibrary, outputPath, encoding, fhirContext);
    }

    private static void mergeDiff(Library referenceLibrary, Library generatedLibrary, String cqlContentPath, CqlTranslator translator,
        FhirContext fhirContext) {
        referenceLibrary.getRelatedArtifact().clear();
        generatedLibrary.getRelatedArtifact().stream()
                .forEach(relatedArtifact -> referenceLibrary.addRelatedArtifact(relatedArtifact));

        referenceLibrary.getDataRequirement().clear();
        generatedLibrary.getDataRequirement().stream()
                .forEach(dateRequirement -> referenceLibrary.addDataRequirement(dateRequirement));

        referenceLibrary.getContent().clear();
        generatedLibrary.getContent().stream()
                .forEach(getContent -> attachContent(referenceLibrary, translator, IOUtils.getCqlString(cqlContentPath)));

        BaseNarrativeProvider narrativeProvider = new NarrativeProvider();
        INarrative narrative = narrativeProvider.getNarrative(fhirContext, generatedLibrary);
        referenceLibrary.setText((Narrative)narrative);
    }

    private static void generateLibrary(String cqlContentPath, String outputPath, Encoding encoding, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(cqlContentPath, translator, fhirContext);
        IOUtils.writeResource(generatedLibrary, outputPath, encoding, fhirContext);
    }

    private static Library processLibrary(String cqlContentPath, CqlTranslator translator, FhirContext fhirContext) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        String id = elm.getIdentifier().getId();
        String version = elm.getIdentifier().getVersion();
        Library library = populateMeta(id, version);
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(library, def);
            }
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, IOUtils.getCqlString(cqlContentPath));
        BaseNarrativeProvider narrativeProvider = new NarrativeProvider();
        INarrative narrative = narrativeProvider.getNarrative(fhirContext, library);
        library.setText((Narrative) narrative);
        return library;
    }


    // Populate metadata
    private static Library populateMeta(String name, String version) {
        Library library = new Library();
        library.setId(nameToId(name, version));
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/codesystem-library-type.html")));
        return library;
    }

    // Add Related Artifact
    private static void addRelatedArtifact(Library library, IncludeDef def) {
        library.addRelatedArtifact(
                new RelatedArtifact()
                        .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                        .setResource(new Reference().setReference("Library/" + getIncludedLibraryId(def))) //this is the reference name
        );
    }

    // Resolve DataRequirements
    private static void resolveDataRequirements(Library library, CqlTranslator translator) {
        for (Retrieve retrieve : translator.toRetrieves()) {
            DataRequirement dataReq = new DataRequirement();
            dataReq.setType(retrieve.getDataType().getLocalPart());
            if (retrieve.getCodeProperty() != null) {
                DataRequirement.DataRequirementCodeFilterComponent codeFilter = new DataRequirement.DataRequirementCodeFilterComponent();
                codeFilter.setPath(retrieve.getCodeProperty());
                if (retrieve.getCodes() instanceof ValueSetRef) {
                    Type valueSetName = new StringType(getValueSetId(((ValueSetRef) retrieve.getCodes()).getName(), translator));
                    codeFilter.setValueSet(valueSetName);
                }
                dataReq.setCodeFilter(Collections.singletonList(codeFilter));
            }
            // TODO - Date filters - we want to populate this with a $data-requirements request as there isn't a good way through elm analysis
            library.addDataRequirement(dataReq);
        }
    }

    private static String getValueSetId(String valueSetName, CqlTranslator translator) {
        org.hl7.elm.r1.Library.ValueSets valueSets = translator.toELM().getValueSets();
        if (valueSets != null) {
            for (ValueSetDef def : valueSets.getDef()) {
                if (def.getName().equals(valueSetName)) {
                    return def.getId();
                }
            }
        }
        return valueSetName;
    }

    // Base64 encode content
    private static void attachContent(Library library, CqlTranslator translator, String cql) {
        library.addContent(
                new Attachment()
                        .setContentType("application/elm+xml")
                        .setData(translator.toXml().getBytes())
        ).addContent(
                new Attachment()
                        .setContentType("text/cql")
                        .setData(cql.getBytes())
        );
    }

    //helpers
    private static String getIncludedLibraryId(IncludeDef def) {
        String name = getIncludedLibraryName(def);
        String version = def.getVersion();
        return nameToId(name, version);
    }

    private static String getIncludedLibraryName(IncludeDef def) {
        return def.getPath();
    }

    private static String nameToId(String name, String version) {
        String nameAndVersion = "library-" + name + "-" + version;
        return nameAndVersion.replaceAll("_", "-");
    }

    private static CqlTranslator getTranslator(String cqlContentPath) {
        String cqlDirPath = IOUtils.getParentDirectoryPath(cqlContentPath);
        ModelManager modelManager = new ModelManager();
        GenericLibrarySourceProvider sourceProvider = new GenericLibrarySourceProvider(cqlDirPath);
        LibraryManager libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);
        return IOUtils.translate(cqlContentPath, modelManager, libraryManager);
    }
}
