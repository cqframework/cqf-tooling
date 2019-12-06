package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;



public class STU3LibraryGenerator extends BaseLibraryGenerator<Library, STU3NarrativeProvider> {

    private Map<String, IAnyResource> libraryMap = new HashMap<>();

    public STU3LibraryGenerator() {
        this.narrativeProvider = new STU3NarrativeProvider();
        this.fhirContext = FhirContext.forDstu3();
        setOutputPath("src/main/resources/org/opencds/cqf/library/output/stu3");
        operationName = "-CqlToSTU3Library";
    }

    @Override
    public void processLibrary(String id, CqlTranslator translator) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        Library library = populateMeta(id, elm.getIdentifier().getVersion());
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                addRelatedArtifact(library, def);
            }        
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, cqlMap.get(id));
        library.setText(narrativeProvider.getNarrative(fhirContext, library));
        libraryMap.put(id, library);
    }

    @Override
    public void output() {
        IOUtils.writeResources(libraryMap, getOutputPath(), encoding, fhirContext);
    }

    // Populate metadata
    private Library populateMeta(String name, String version) {
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

    private String nameToId(String name) {
        return name.replaceAll("_", "-").toLowerCase();
    }

    private String nameToId(String name, String version) {
        String nameAndVersion = "library-" + name + "-" + version;
        return nameAndVersion.replaceAll("_", "-");
    }

    private String createFileName(String id, String encoding) {
        return id + "." + encoding;
    }
}
