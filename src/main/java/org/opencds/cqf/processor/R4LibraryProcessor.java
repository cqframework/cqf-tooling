package org.opencds.cqf.processor;

import java.util.Collections;
import java.util.Optional;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.instance.model.api.INarrative;
import org.opencds.cqf.library.BaseNarrativeProvider;
import org.opencds.cqf.library.GenericLibrarySourceProvider;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.ResourceUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.model.*;

public class R4LibraryProcessor {
        public static Boolean refreshLibraryContent(String cqlContentPath, String libraryPath, FhirContext fhirContext, Encoding encoding, Boolean includeVersion) {         
        Library resource = (Library)IOUtils.readResource(libraryPath, fhirContext, true);
        Boolean libraryExists = resource != null;       

        CqlTranslator translator = getTranslator(cqlContentPath);
              
        if (libraryExists) {            
            refreshLibrary(resource, cqlContentPath, IOUtils.getParentDirectoryPath(libraryPath), encoding, includeVersion, translator, fhirContext);
        } else {
            Optional<String> anyOtherLibraryDirectory = IOUtils.getLibraryPaths(fhirContext).stream().findFirst();
            String parentDirectory = anyOtherLibraryDirectory.isPresent() ? IOUtils.getParentDirectoryPath(anyOtherLibraryDirectory.get()) : IOUtils.getParentDirectoryPath(cqlContentPath);
            generateLibrary(cqlContentPath, parentDirectory, encoding, includeVersion, translator, fhirContext);
        }
      
        return true;
    }

    private static void refreshLibrary(Library referenceLibrary, String cqlContentPath, String outputPath, Encoding encoding, Boolean includeVersion, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(cqlContentPath, translator, includeVersion, fhirContext);
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

        BaseNarrativeProvider<Narrative> narrativeProvider = new org.opencds.cqf.library.r4.NarrativeProvider();
        INarrative narrative = narrativeProvider.getNarrative(fhirContext, generatedLibrary);
        referenceLibrary.setText((Narrative)narrative);
    }

    private static void generateLibrary(String cqlContentPath, String outputPath, Encoding encoding, Boolean includeVersion, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(cqlContentPath, translator, includeVersion, fhirContext);
        IOUtils.writeResource(generatedLibrary, outputPath, encoding, fhirContext);
    }

    private static Library processLibrary(String cqlContentPath, CqlTranslator translator, Boolean includeVersion, FhirContext fhirContext) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        String id = elm.getIdentifier().getId();
        String version = elm.getIdentifier().getVersion();
        Library library = populateMeta(id, version, includeVersion);
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(library, def, includeVersion);
            }
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, IOUtils.getCqlString(cqlContentPath));
        BaseNarrativeProvider<Narrative> narrativeProvider = new org.opencds.cqf.library.r4.NarrativeProvider();
        INarrative narrative = narrativeProvider.getNarrative(fhirContext, library);
        library.setText((Narrative) narrative);
        return library;
    }


    // Populate metadata
    private static Library populateMeta(String name, String version, Boolean includeVersion) {
        Library library = new Library();
        if(!includeVersion) {
            ResourceUtils.setIgId(name, library, "");
        }
        else ResourceUtils.setIgId(name, library, version);
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/codesystem-library-type.html")));
        return library;
    }

    // Add Related Artifact
    private static void addRelatedArtifact(Library library, IncludeDef def, Boolean includeVersion) {
        library.addRelatedArtifact(
                new RelatedArtifact()
                        .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                        .setResource("Library/" + getIncludedLibraryId(def, includeVersion)) //this is the reference name
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
                    String valueSetName = getValueSetId(((ValueSetRef) retrieve.getCodes()).getName(), translator);
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

    //TODO: need to move to Library Processor
    private static String getIncludedLibraryId(IncludeDef def, Boolean includeVersion) {
        Library tempLibrary = new Library();
        String name = getIncludedLibraryName(def);
        String version = includeVersion ? def.getVersion() : "";
        ResourceUtils.setIgId(name, tempLibrary, version);
        return tempLibrary.getId();
    }

    private static String getIncludedLibraryName(IncludeDef def) {
        return def.getPath();
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
