package org.opencds.cqf.processor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.opencds.cqf.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.library.GenericLibrarySourceProvider;
import org.opencds.cqf.parameter.RefreshLibraryParameters;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;
import org.opencds.cqf.utilities.R4FHIRUtils;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class R4LibraryProcessor implements LibraryProcessor{
    private String igCanonicalBase;
    private String cqlContentPath;
    private String libraryPath;
    private FhirContext fhirContext;
    private Encoding encoding;
    private Boolean versioned;
    private static CqfmSoftwareSystemHelper cqfmHelper = new CqfmSoftwareSystemHelper();

    public Boolean refreshLibraryContent(RefreshLibraryParameters params) {
        igCanonicalBase = params.igCanonicalBase;
        cqlContentPath = params.cqlContentPath;
        libraryPath = params.libraryPath;
        fhirContext = params.fhirContext;
        encoding = params.encoding;
        versioned = params.versioned;

        CqlTranslator translator = getTranslator(cqlContentPath);

        Boolean libraryExists = false;
        Library resource = null;
        if (libraryPath != null) {
            resource = (Library) IOUtils.readResource(libraryPath, fhirContext, true);
            libraryExists = resource != null;
        }
              
        if (libraryExists) {            
            String libraryResourceDirPath = IOUtils.getParentDirectoryPath(libraryPath);
            if(!IOUtils.resourceDirectories.contains(libraryResourceDirPath)) {
                IOUtils.resourceDirectories.add(libraryResourceDirPath);
            }   

            refreshLibrary(igCanonicalBase, resource, cqlContentPath, IOUtils.getParentDirectoryPath(libraryPath), encoding, versioned, translator, fhirContext);
        } else {
            Optional<String> anyOtherLibraryDirectory = IOUtils.getLibraryPaths(fhirContext).stream().findFirst();
            String parentDirectory = anyOtherLibraryDirectory.isPresent() ? IOUtils.getParentDirectoryPath(anyOtherLibraryDirectory.get()) : IOUtils.getParentDirectoryPath(cqlContentPath);
            generateLibrary(igCanonicalBase, cqlContentPath, parentDirectory, encoding, versioned, translator, fhirContext);
        }
      
        return true;
    }

    private static void refreshLibrary(String igCanonicalBase, Library referenceLibrary, String cqlContentPath, String outputPath, Encoding encoding, Boolean includeVersion, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(igCanonicalBase, cqlContentPath, translator, includeVersion, fhirContext);
        mergeDiff(referenceLibrary, generatedLibrary, cqlContentPath, translator, fhirContext);
        cqfmHelper.ensureToolingExtensionAndDevice(referenceLibrary);
        // Issue 96
        // Passing the includeVersion here to handle not using the version number in the filename
        IOUtils.writeResource(referenceLibrary, outputPath, encoding, fhirContext, includeVersion);
    }

    private static void mergeDiff(Library referenceLibrary, Library generatedLibrary, String cqlContentPath, CqlTranslator translator,
        FhirContext fhirContext) {
        referenceLibrary.getRelatedArtifact().removeIf(a -> a.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
        generatedLibrary.getRelatedArtifact().stream().forEach(relatedArtifact -> referenceLibrary.addRelatedArtifact(relatedArtifact));

        referenceLibrary.getDataRequirement().clear();
        generatedLibrary.getDataRequirement().stream().forEach(dateRequirement -> referenceLibrary.addDataRequirement(dateRequirement));

        referenceLibrary.getContent().clear();
        attachContent(referenceLibrary, translator, IOUtils.getCqlString(cqlContentPath));

        referenceLibrary.setUrl(generatedLibrary.getUrl());

        // BaseNarrativeProvider<Narrative> narrativeProvider = new org.opencds.cqf.library.r4.NarrativeProvider();
        // INarrative narrative = narrativeProvider.getNarrative(fhirContext, generatedLibrary);
        // referenceLibrary.setText((Narrative)narrative);
    }

    private static void generateLibrary(String igCanonicalBase, String cqlContentPath, String outputPath, Encoding encoding, Boolean includeVersion, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(igCanonicalBase, cqlContentPath, translator, includeVersion, fhirContext);
        IOUtils.writeResource(generatedLibrary, outputPath, encoding, fhirContext);
    }

    private static Library processLibrary(String igCanonicalBase, String cqlContentPath, CqlTranslator translator, Boolean includeVersion, FhirContext fhirContext) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        String id = elm.getIdentifier().getId();
        String version = elm.getIdentifier().getVersion();
        Library library = populateMeta(igCanonicalBase, id, version, includeVersion);
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(igCanonicalBase, library, def, includeVersion);
            }
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, IOUtils.getCqlString(cqlContentPath));
        cqfmHelper.ensureToolingExtensionAndDevice(library);
        // BaseNarrativeProvider<Narrative> narrativeProvider = new org.opencds.cqf.library.r4.NarrativeProvider();
        // INarrative narrative = narrativeProvider.getNarrative(fhirContext, library);
        // library.setText((Narrative) narrative);
        return library;
    }

    // Populate metadata
    private static Library populateMeta(String igCanonicalBase, String name, String version, Boolean includeVersion) {
        if (igCanonicalBase != null) {
            igCanonicalBase = igCanonicalBase + "/";
        }
        else {
            igCanonicalBase = "";
        }

        // Special case for FHIRHelpers
        if (name.equals("FHIRHelpers")) {
            igCanonicalBase = "http://hl7.org/fhir/";
        }

        Library library = new Library();
        if(!includeVersion) {
            ResourceUtils.setIgId(name, library, "");
        }
        else ResourceUtils.setIgId(name, library, version);
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://terminology.hl7.org/CodeSystem/library-type")));
        library.setUrl((igCanonicalBase + "Library/"+  (includeVersion ? LibraryProcessor.ResourcePrefix : "")  + name));
        return library;
    }

    // Add Related Artifact
    private static void addRelatedArtifact(String igCanonicalBase, Library library, IncludeDef def, Boolean includeVersion) {
        if (igCanonicalBase != null) {
            igCanonicalBase = igCanonicalBase + "/";


            // Special case for FHIRHelpers
            if (def.getPath().equals("FHIRHelpers")) {
                igCanonicalBase = "http://hl7.org/fhir/";
            }

            //TODO: adding the resource prefix here is a temporary workaround until the rest of the tooling can get rid of it.
            library.addRelatedArtifact(
                new RelatedArtifact()
                    .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                    //TODO: we probably want to replace this "-library" behavior with a switch (or get rid of it altogether)
                    //HACK: this is not a safe implementation.  Someone could run without -v and everything else would still get the "library-".
                    //Doing this for the connectathon.
                    //.setResource(igCanonicalBase + "Library/" + (includeVersion ? LibraryProcessor.ResourcePrefix : "") + getResourceCanonicalReference(def, includeVersion))
                    // Issue 96
                    // When not using the -v option we want it to find the files without the version number but we want it
                    // to generate the dependencies using the version so forcing the includeVersion to true here
                    .setResource(igCanonicalBase + "Library/" + (includeVersion ? LibraryProcessor.ResourcePrefix : "") + getResourceCanonicalReference(def, true))
            );
        }
        else {
            if (includeVersion) {
                throw new IllegalArgumentException("-v argument requires -igrp with a valid url.");
            }
            library.addRelatedArtifact(
                new RelatedArtifact()
                    .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                    .setResource("Library/" + getIncludedLibraryId(def, includeVersion)) //this is the reference name
            );
        }

        
    }

    // Resolve DataRequirements
    private static void resolveDataRequirements(Library library, CqlTranslator translator) {
        for (Retrieve retrieve : translator.toRetrieves()) {
            DataRequirement dataReq = new DataRequirement();
            dataReq.setType(retrieve.getDataType().getLocalPart());

            // Set profile if specified
            if (retrieve.getTemplateId() != null) {
                dataReq.setProfile(Collections.singletonList(new CanonicalType(retrieve.getTemplateId())));
            }

            if (retrieve.getCodeProperty() != null) {
                DataRequirement.DataRequirementCodeFilterComponent codeFilter = new DataRequirement.DataRequirementCodeFilterComponent();
                codeFilter.setPath(retrieve.getCodeProperty());

                // TODO: Support retrieval when the target is a CodeSystemRef

                if (retrieve.getCodes() instanceof ValueSetRef) {
                    ValueSetRef vsr = (ValueSetRef)retrieve.getCodes();
                    Map<String, TranslatedLibrary> translatedLibraries = translator.getTranslatedLibraries();
                    TranslatedLibrary translatedLibrary = translator.getTranslatedLibrary();
                    codeFilter.setValueSet(R4FHIRUtils.toReference(R4FHIRUtils.resolveValueSetRef(vsr, translatedLibrary, translatedLibraries)));
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

    private static String getResourceCanonicalReference(IncludeDef def, Boolean includeVersion) {
        String version = includeVersion ? "|" + def.getVersion() : "";
        String reference = def.getPath() + version;
        return reference;
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
