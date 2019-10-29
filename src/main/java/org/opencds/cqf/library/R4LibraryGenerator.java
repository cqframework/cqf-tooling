package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.r4.model.*;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public class R4LibraryGenerator extends BaseLibraryGenerator<Library, R4NarrativeProvider> {

    public R4LibraryGenerator() {
        this.narrativeProvider = new R4NarrativeProvider();
        this.fhirContext = FhirContext.forR4();
        setOutputPath("src/main/resources/org/opencds/cqf/library/output/r4");
        operationName = "-CqlToR4Library";
    }

    @Override
    public void processLibrary(String id, CqlTranslator translator) {
        org.hl7.elm.r1.Library elm = translator.toELM();
        Library library = populateMeta(id, elm.getIdentifier().getVersion());
        if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
            for (IncludeDef def : elm.getIncludes().getDef()) {
                if (!libraryMap.containsKey(def.getPath())) {
                    if (!translatorMap.containsKey(def.getPath())) {
                        throw new IllegalArgumentException("Referenced library: " + def.getPath() + " not found");
                    }
                    processLibrary(def.getPath(), translatorMap.get(def.getPath()));
                }

                library.addRelatedArtifact(
                        new RelatedArtifact()
                                .setType(RelatedArtifact.RelatedArtifactType.DEPENDSON)
                                .setResource("Library/" + nameToId(def.getPath()))
                );

                for (DataRequirement req : libraryMap.get(def.getPath()).getDataRequirement()) {
                    library.addDataRequirement(req);
                }
            }
        }

        resolveDataRequirements(library, translator);
        attachContent(library, translator, cqlMap.get(id));
        library.setText(narrativeProvider.getNarrative(fhirContext, library));
        libraryMap.put(id, library);
    }

    @Override
    public void output() {
        Bundle bundle = new Bundle();

        for (Map.Entry<String, Library> entry : libraryMap.entrySet()) {
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/library-" + entry.getKey().replaceAll("_", "-").toLowerCase() + "." + encoding))
            {
                bundle.addEntry().setResource(entry.getValue()).setRequest(new Bundle.BundleEntryRequestComponent().setMethod(Bundle.HTTPVerb.PUT).setUrl("Library/" + entry.getValue().getId()));
                writer.write(
                        encoding.equals("json")
                                ? fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(entry.getValue()).getBytes()
                                : fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(entry.getValue()).getBytes()
                );
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error outputting library: " + entry.getKey());
            }
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/elm-" + entry.getKey().replaceAll("_", "-").toLowerCase() + "." + encoding))
            {
                writer.write(elmMap.get(entry.getKey()).getBytes());
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error outputting elm for library: " + entry.getKey());
            }
        }

        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/all-libraries-bundle." +  encoding)) {
            writer.write(
                    encoding.equals("json")
                            ? fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes()
                            : fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error outputting library bundle");
        }
    }

    // Populate metadata
    private Library populateMeta(String name, String version) {
        Library library = new Library();
        library.setId(nameToId(name));
        library.setName(name);
        library.setVersion(version);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://terminology.hl7.org/CodeSystem/library-type")));
        return library;
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
                    codeFilter.setValueSet(getValueSetId(((ValueSetRef) retrieve.getCodes()).getName()));
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

    private String nameToId(String name) {
        return name.replaceAll("_", "-").toLowerCase();
    }
}
