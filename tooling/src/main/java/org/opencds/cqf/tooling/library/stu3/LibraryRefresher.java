package org.opencds.cqf.tooling.library.stu3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DataRequirement;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.BaseLibraryGenerator;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class LibraryRefresher extends BaseLibraryGenerator<Library, NarrativeProvider> {

    private Map<String, IBaseResource> libraryMap = new HashMap<>();

    public LibraryRefresher() {
        setNarrativeProvider(new NarrativeProvider());
        setFhirContext(FhirContext.forDstu3Cached());
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/library/output/stu3");
        setOperationName("-refreshLibrary");
    }

    @Override
    public void processLibrary(String id, CqlTranslator translator) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        Library generatedLibrary = populateMeta(id, elm.getIdentifier().getVersion());
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(generatedLibrary, def);
            }        
        }

        resolveDataRequirements(generatedLibrary, translator);
        attachContent(generatedLibrary, translator, getCqlMap().get(id));
        // generatedLibrary.setText(getNarrativeProvider().getNarrative(getFhirContext(), generatedLibrary));
        Library refreshedLibrary = refreshLibrary(generatedLibrary, id, translator);
        libraryMap.put(id, refreshedLibrary);
    }

    private Library refreshLibrary(Library generatedLibrary, String id, CqlTranslator generatedLibraryTranslator) {
        if (getPathToLibrary() == null) {
            throw new IllegalArgumentException("The path to the CQL Library is required to refresh Content");
        }

        Library referenceLibrary;
        try {
            referenceLibrary = (Library) IOUtils.readResource(getPathToLibrary(), getFhirContext());
        } catch (Exception e) {
            throw new IllegalArgumentException("The path to the CQL Library is not a Library Resource");
        }

        referenceLibrary.getRelatedArtifact().removeIf(a -> a.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
        generatedLibrary.getRelatedArtifact().stream().forEach(relatedArtifact -> referenceLibrary.addRelatedArtifact(relatedArtifact));

        referenceLibrary.getDataRequirement().clear();
        generatedLibrary.getDataRequirement().stream().forEach(dateRequirement -> referenceLibrary.addDataRequirement(dateRequirement));

        referenceLibrary.getContent().clear();
        attachContent(referenceLibrary, generatedLibraryTranslator, getCqlMap().get(id));

        // referenceLibrary.setText(getNarrativeProvider().getNarrative(getFhirContext(), generatedLibrary));

        return referenceLibrary;
    }

    @Override
    public void output() {
        //replace with writeResources
        IOUtils.writeResources(libraryMap, getOutputPath(), IOUtils.Encoding.parse(getEncoding()), getFhirContext());
    }

    // Populate metadata
    private Library populateMeta(String name, String version) {
        Library library = new Library();
        library.setId(nameToId(name, version));
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://terminology.hl7.org/CodeSystem/library-type")));
        return library;
    }

    // Add Related Artifact
    private void addRelatedArtifact(Library library, IncludeDef def) {
        library.addRelatedArtifact(
                new RelatedArtifact()
                        .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                        .setResource(new Reference().setReference("Library/" + getIncludedLibraryId(def))) //this is the reference name
        );
    }

    // Resolve DataRequirements
    private void resolveDataRequirements(Library library, CqlTranslator translator) {
        for (Retrieve retrieve : translator.toRetrieves()) {
            DataRequirement dataReq = new DataRequirement();
            dataReq.setType(retrieve.getDataType().getLocalPart());
            if (retrieve.getCodeProperty() != null) {
                DataRequirement.DataRequirementCodeFilterComponent codeFilter = new DataRequirement.DataRequirementCodeFilterComponent();
                codeFilter.setPath(retrieve.getCodeProperty());
                if (retrieve.getCodes() instanceof ValueSetRef) {
                    Type valueSetName = new StringType(getValueSetId(((ValueSetRef) retrieve.getCodes()).getName()));
                    codeFilter.setValueSet(valueSetName);
                }
                dataReq.setCodeFilter(Collections.singletonList(codeFilter));
            }
            // TODO - Date filters - we want to populate this with a $data-requirements request as there isn't a good way through elm analysis
            library.addDataRequirement(dataReq);
        }
    }

    // Base64 encode content
    private void attachContent(Library library, CqlTranslator translator, String cql) {
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
    private String getIncludedLibraryId(IncludeDef def) {
        String name = getIncludedLibraryName(def);
        String version = def.getVersion();
        return nameToId(name, version);
    }

    private String getIncludedLibraryName(IncludeDef def) {
        return def.getPath();
    }

    private String nameToId(String name, String version) {
        String nameAndVersion = LibraryProcessor.ResourcePrefix + name + "-" + version;
        return nameAndVersion.replaceAll("_", "-");
    }

    @SuppressWarnings("unused")
    private String createFileName(String id, String encoding) {
        return id + "." + encoding;
    }
}
