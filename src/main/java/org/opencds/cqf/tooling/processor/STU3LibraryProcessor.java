package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.convertors.VersionConvertor_30_50;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.library.GenericLibrarySourceProvider;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.opencds.cqf.tooling.utilities.STU3FHIRUtils;

import java.io.File;
import java.util.*;

public class STU3LibraryProcessor extends LibraryProcessor {
    private String igCanonicalBase;
    private String cqlContentPath;
    private String libraryPath;
    private FhirContext fhirContext;
    private Encoding encoding;
    private static CqfmSoftwareSystemHelper cqfmHelper;

    /*
    Refresh all library resources in the given libraryPath
     */
    protected List<String> refreshLibraries(String libraryPath) {
        File file = new File(libraryPath);
        Map<String, String> fileMap = new HashMap<String, String>();
        List<org.hl7.fhir.r5.model.Library> libraries = new ArrayList<>();
        if (file.isDirectory()) {
            for (File libraryFile : file.listFiles()) {
                loadLibrary(fileMap, libraries, libraryFile);
            }
        }
        else {
            loadLibrary(fileMap, libraries, file);
        }

        List<String> refreshedLibraryNames = new ArrayList<String>();
        List<org.hl7.fhir.r5.model.Library> refreshedLibraries = super.refreshGeneratedContent(libraries);
        for (org.hl7.fhir.r5.model.Library refreshedLibrary : refreshedLibraries) {
            String filePath = fileMap.get(refreshedLibrary.getId());
            org.hl7.fhir.dstu3.model.Library library = (org.hl7.fhir.dstu3.model.Library) VersionConvertor_30_50.convertResource(refreshedLibrary, false);
            cqfmHelper.ensureCQFToolingExtensionAndDevice(library, fhirContext);
            IOUtils.writeResource(library, filePath, IOUtils.getEncoding(filePath), fhirContext);
            String refreshedLibraryName;
            if (this.versioned && refreshedLibrary.getVersion() != null) {
                refreshedLibraryName = refreshedLibrary.getName() + "-" + refreshedLibrary.getVersion();
            } else {
                refreshedLibraryName = refreshedLibrary.getName();
            }
            refreshedLibraryNames.add(refreshedLibraryName);
        }

        return refreshedLibraryNames;
    }

    private void loadLibrary(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.Library> libraries, File libraryFile) {
        try {
            Resource resource = (Resource) IOUtils.readResource(libraryFile.getAbsolutePath(), fhirContext);
            org.hl7.fhir.r5.model.Library library = (org.hl7.fhir.r5.model.Library) VersionConvertor_30_50.convertResource(resource, false);
            fileMap.put(library.getId(), libraryFile.getAbsolutePath());
            libraries.add(library);
        } catch (Exception ex) {
            logMessage(String.format("Error reading library: %s. Error: %s", libraryFile.getAbsolutePath(), ex.getMessage()));
        }
    }

    @Override
    public List<String> refreshLibraryContent(RefreshLibraryParameters params) {
        if (params.parentContext != null) {
            initialize(params.parentContext);
        }
        else {
            initialize(params.ini);
            igCanonicalBase = params.igCanonicalBase;
            cqlContentPath = params.cqlContentPath;
        }

        libraryPath = params.libraryPath;
        fhirContext = params.fhirContext;
        encoding = params.encoding;
        versioned = params.versioned;

        this.cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

        return refreshLibraries(libraryPath);

/*
        CqlTranslator translator = getTranslator(cqlContentPath);

        Boolean libraryExists = false;
        Library resource = null;
        if (libraryPath != null) {
            resource = (Library)IOUtils.readResource(libraryPath, fhirContext, true);
            libraryExists = resource != null;
        }

        if (libraryExists) {            
            String libraryResourceDirPath = IOUtils.getParentDirectoryPath(libraryPath);
            if(!IOUtils.resourceDirectories.contains(libraryResourceDirPath) )
            {
                IOUtils.resourceDirectories.add(libraryResourceDirPath);
            }     

            refreshLibrary(igCanonicalBase, resource, cqlContentPath, IOUtils.getParentDirectoryPath(libraryPath), encoding, versioned, translator, fhirContext);
        } else {
            Optional<String> anyOtherLibrary = IOUtils.getLibraryPaths(fhirContext).stream().findFirst();
            String parentDirectory = anyOtherLibrary.isPresent() ? IOUtils.getParentDirectoryPath(anyOtherLibrary.get()) : IOUtils.getParentDirectoryPath(cqlContentPath);
            generateLibrary(igCanonicalBase, cqlContentPath, parentDirectory, encoding, versioned, translator, fhirContext);
        }

 */
    }

    private static void refreshLibrary(String igCanonicalBase, Library referenceLibrary, String cqlContentPath, String outputPath, Encoding encoding, Boolean versioned, CqlTranslator translator, FhirContext fhirContext) {
        Library generatedLibrary = processLibrary(igCanonicalBase, cqlContentPath, translator, versioned, fhirContext);
        mergeDiff(referenceLibrary, generatedLibrary, cqlContentPath, translator, fhirContext);
        cqfmHelper.ensureCQFToolingExtensionAndDevice(referenceLibrary, fhirContext);
        IOUtils.writeResource(referenceLibrary, outputPath, encoding, fhirContext);
    }

    private static void mergeDiff(Library referenceLibrary, Library generatedLibrary, String cqlContentPath, CqlTranslator translator, FhirContext fhirContext) {
        referenceLibrary.getRelatedArtifact().removeIf(a -> a.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
        generatedLibrary.getRelatedArtifact().stream().forEach(relatedArtifact -> referenceLibrary.addRelatedArtifact(relatedArtifact));

        referenceLibrary.getDataRequirement().clear();
        generatedLibrary.getDataRequirement().stream().forEach(dateRequirement -> referenceLibrary.addDataRequirement(dateRequirement));

        referenceLibrary.getContent().clear();
        attachContent(referenceLibrary, translator, IOUtils.getCqlString(cqlContentPath));

        referenceLibrary.setUrl(generatedLibrary.getUrl());

        // BaseNarrativeProvider<Narrative> narrativeProvider = new NarrativeProvider();
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
        cqfmHelper.ensureCQFToolingExtensionAndDevice(library, fhirContext);
        // BaseNarrativeProvider<Narrative> narrativeProvider = new NarrativeProvider();
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
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/library-type")));
        library.setUrl((igCanonicalBase + "Library/"+  (includeVersion ? LibraryProcessor.ResourcePrefix : "")  + name));
        return library;
    }

    // Add Related Artifact
    private static void addRelatedArtifact(String igCanonicalBase, Library library, IncludeDef def, Boolean includeVersion) {
        if (igCanonicalBase != null) {
            igCanonicalBase = igCanonicalBase + "/";
        }
        else {
            igCanonicalBase = "";
        }

        // Special case for FHIRHelpers
        if (def.getPath().equals("FHIRHelpers")) {
            igCanonicalBase = "http://hl7.org/fhir/";
        }

        //TODO: adding the resource prefix here is a temporary workaround until the rest of the tooling can get rid of it.
        library.addRelatedArtifact(
            new RelatedArtifact()
                .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                .setResource(new Reference().setReference(igCanonicalBase + "Library/" + getResourceCanonicalReference(def, includeVersion))) //this is the reference name
        );
    }

    // Resolve DataRequirements
    private static void resolveDataRequirements(Library library, CqlTranslator translator) {
        for (Retrieve retrieve : translator.toRetrieves()) {
            DataRequirement dataReq = new DataRequirement();
            dataReq.setType(retrieve.getDataType().getLocalPart());

            // Set profile if specified
            if (retrieve.getTemplateId() != null) {
                dataReq.setProfile(Collections.singletonList(new org.hl7.fhir.dstu3.model.UriType(retrieve.getTemplateId())));
            }

            if (retrieve.getCodeProperty() != null) {
                DataRequirement.DataRequirementCodeFilterComponent codeFilter = new DataRequirement.DataRequirementCodeFilterComponent();
                codeFilter.setPath(retrieve.getCodeProperty());

                // TODO: Support retrieval when the target is a CodeSystemRef

                if (retrieve.getCodes() instanceof ValueSetRef) {
                    ValueSetRef vsr = (ValueSetRef)retrieve.getCodes();
                    Map<String, TranslatedLibrary> translatedLibraries = translator.getTranslatedLibraries();
                    TranslatedLibrary translatedLibrary = translator.getTranslatedLibrary();
                    codeFilter.setValueSet(new org.hl7.fhir.dstu3.model.Reference(STU3FHIRUtils.toReference(STU3FHIRUtils.resolveValueSetRef(vsr, translatedLibrary, translatedLibraries))));
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
    private static String getResourceCanonicalReference(IncludeDef def, Boolean includeVersion) {
        String version = includeVersion ? "|" + def.getVersion() : "";
        String reference = def.getPath() + version;
        return reference;
    }

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
