package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.dstu3.model.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class STU3LibraryGenerator extends BaseLibraryGenerator<Library, STU3NarrativeProvider> {

    private Map<String, Library> libraryMap = new HashMap<>();

    public STU3LibraryGenerator() {
        this.narrativeProvider = new STU3NarrativeProvider();
        this.fhirContext = FhirContext.forDstu3();
        setOutputPath("src/main/resources/org/opencds/cqf/library/output/stu3");
        operationName = "-CqlToSTU3Library";
    }

    @Override
    public void processLibrary(String id, CqlTranslator translator) {
        Library library = populateMeta(id);
        org.hl7.elm.r1.Library elm = translator.toELM();
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
                                .setResource(new Reference().setReference("Library/" + def.getPath()))
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
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + entry.getKey() + "." + encoding)) {
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
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/elm-" + entry.getKey().replaceAll("_", "-").toLowerCase() + ".xml"))
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
    private Library populateMeta(String id) {
        Library library = new Library();
        library.setId(id.replaceAll("_", "-"));
        library.setName(id);
        library.setStatus(Enumerations.PublicationStatus.ACTIVE);
        library.setExperimental(true);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/codesystem-library-type.html")));
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
}
