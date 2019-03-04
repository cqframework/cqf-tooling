package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.Operation;

import java.io.*;
import java.util.*;

public class LibraryGenerator extends Operation {

    private String pathToLibraryDir;
    private String encoding = "json";

    private Map<String, CqlTranslator> translatorMap = new HashMap<>();
    private Map<String, String> cqlMap = new HashMap<>();
    private Map<String, Library> libraryMap = new HashMap<>();

    private ModelManager modelManager;
    private LibraryManager libraryManager;
    private GenericLibrarySourceProvider sourceProvider;

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/library/output"); // default

        for (String arg : args) {
            if (arg.equals("-CqlToLibrary")) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            String flag = flagAndValue[0];
            String value = flagAndValue.length < 2 ? null : flagAndValue[1];

            if (flag.equals("-pathtolibrarydirectory") || flag.equals("-ptld")) {
                pathToLibraryDir = value;
            }
            else if (flag.equals("-encoding") || flag.equals("-e")) {
                encoding = value == null ? "json" : value.toLowerCase();
            }
            else if (flag.equals("-outputpath") || flag.equals("-op")) {
                setOutputPath(value);
            }
        }

        if (pathToLibraryDir == null) {
            throw new IllegalArgumentException("The path to the CQL Library is required");
        }

        File libraryDir = new File(pathToLibraryDir);
        if (!libraryDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to library files is not a directory");
        }

        File[] cqlFiles = libraryDir.listFiles();
        if (cqlFiles == null) {
            return;
        }
        else if (cqlFiles.length == 0) {
            return;
        }

        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
        sourceProvider = new GenericLibrarySourceProvider(pathToLibraryDir);
        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        CqlTranslator translator;
        for (File cqlFile : cqlFiles) {
            translator = translate(cqlFile);
            translatorMap.put(translator.toELM().getIdentifier().getId(), translator);
            cqlMap.put(translator.toELM().getIdentifier().getId(), getCql(cqlFile));
        }

        for (Map.Entry<String, CqlTranslator> entry : translatorMap.entrySet()) {
            if (!libraryMap.containsKey(entry.getKey())) {
                processLibrary(entry.getKey(), entry.getValue());
            }
        }

        output();
    }

    public String getCql(File file) {
        StringBuilder cql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                cql.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error reading CQL file: " + file.getName());
        }

        return cql.toString();
    }

    public CqlTranslator translate(File cqlFile) {
        try {
            ArrayList<CqlTranslator.Options> options = new ArrayList<>();
            options.add(CqlTranslator.Options.EnableDateRangeOptimization);

            CqlTranslator translator =
                    CqlTranslator.fromFile(
                            cqlFile,
                            modelManager,
                            libraryManager,
                            options.toArray(new CqlTranslator.Options[options.size()])
                    );

            if (translator.getErrors().size() > 0) {
                System.err.println("Translation failed due to errors:");
                ArrayList<String> errors = new ArrayList<>();
                for (CqlTranslatorException error : translator.getErrors()) {
                    TrackBack tb = error.getLocator();
                    String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                            tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
                    System.err.printf("%s %s%n", lines, error.getMessage());
                    errors.add(lines + error.getMessage());
                }
                throw new IllegalArgumentException(errors.toString());
            }

            return translator;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error encountered during CQL translation: " + e.getMessage());
        }
    }

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
        libraryMap.put(id, library);
    }

    // Populate metadata
    public Library populateMeta(String id) {
        Library library = new Library();
        library.setId(id);
        library.setStatus(Enumerations.PublicationStatus.DRAFT);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/codesystem-library-type.html")));
        return library;
    }

    // Resolve DataRequirements
    public void resolveDataRequirements(Library library, CqlTranslator translator) {
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

    public String getValueSetId(String valueSetName) {
        for (CqlTranslator translator : translatorMap.values()) {
            org.hl7.elm.r1.Library.ValueSets valueSets = translator.toELM().getValueSets();
            if (valueSets != null) {
                for (ValueSetDef def : valueSets.getDef()) {
                    if (def.getName().equals(valueSetName)) {
                        return def.getId();
                    }
                }
            }
        }
        return valueSetName;
    }

    // Base64 encode content
    public void attachContent(Library library, CqlTranslator translator, String cql) {
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

    // Output
    public void output() {
        for (Map.Entry<String, Library> entry : libraryMap.entrySet()) {
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + entry.getKey() + "." + encoding)) {
                writer.write(
                        encoding.equals("json")
                                ? FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(entry.getValue()).getBytes()
                                : FhirContext.forDstu3().newXmlParser().setPrettyPrint(true).encodeResourceToString(entry.getValue()).getBytes()
                );
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error outputting library: " + entry.getKey());
            }
        }
    }
}
