package org.opencds.cqf.library.stu3;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.library.BaseLibraryGenerator;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class LibraryGenerator extends BaseLibraryGenerator<Library, NarrativeProvider> {

    private Map<String, IAnyResource> libraryMap = new HashMap<>();

    public LibraryGenerator() {
        setNarrativeProvider(new NarrativeProvider());
        setFhirContext(FhirContext.forDstu3());
        setOutputPath("src/main/resources/org/opencds/cqf/library/output/stu3");
        setOperationName("-CqlToSTU3Library");
    }

    @Override
    public void processLibrary(String id, CqlTranslator translator) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        Library library = loadIfExists();
        if (library == null) {
            library = createLibrary(nameToId(elm.getIdentifier().getId(), elm.getIdentifier().getVersion()),
                    elm.getIdentifier().getId(), elm.getIdentifier().getVersion());
        }
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(library, def);
            }        
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, getCqlMap().get(id));
        // library.setText(getNarrativeProvider().getNarrative(getFhirContext(), library));
        libraryMap.put(id, library);
    }

    @Override
    public void output() {
        //replace with writeResources
        IOUtils.writeResources(libraryMap, getOutputPath(), IOUtils.Encoding.parse(getEncoding()), getFhirContext());
    }

    private Library loadIfExists() {
        return (Library)IOUtils.readResource(getPathToLibrary(), getFhirContext(), true);
    }

    private Library createLibrary(String id, String name, String version) {
        Library library = new Library();
        library.setId(id);
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/library-type").setDisplay("Logic Library")));
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

    public Library refreshGeneratedContent(List<Library> libraries) {
        return null;
    }

    public Library refreshGeneratedContent(Path pathToLibraryDirectory) {
        return null;
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
        String nameAndVersion = "library-" + name + "-" + version;
        return nameAndVersion.replaceAll("_", "-");
    }

    private String createFileName(String id, String encoding) {
        return id + "." + encoding;
    }
}
