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
    private String pathToLibrary;
    private String pathToLibraryResource;
    private String encoding = "json";

    private Map<String, CqlTranslator> translatorMap = new HashMap<>();
    private Map<String, String> cqlMap = new HashMap<>();
    private Map<String, String> libraryNameIdMap = new HashMap<>();
    private Map<String, Library> libraryMap = new HashMap<>();
    private Map<String, String> resourceFileNames = new HashMap<>();

    private ModelManager modelManager;
    private LibraryManager libraryManager;
    private GenericLibrarySourceProvider sourceProvider;
    private boolean isCreate = true;
    private Library libraryResource;

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/library/output"); // default

        for (String arg : args) {
            if (arg.equals("-CqlToLibrary")) {
                isCreate = true;
                continue;
            }

            if (arg.equals("-UpdateCql")) {
                isCreate = false;
                continue;
            }

            String[] flagAndValue = arg.split("=");
            String flag = flagAndValue[0];
            String value = flagAndValue.length < 2 ? null : flagAndValue[1];

            if (flag.equals("-pathToLibrary") || flag.equals("-ptl")) {
                pathToLibrary = value;
                pathToLibraryDir = new File(pathToLibrary).getParent();
            }
            else if (flag.equals("-pathtolibrarydirectory") || flag.equals("-ptld")) {
                pathToLibraryDir = value;
            }
            else if (flag.equals("-pathToLibraryResource") || flag.equals("-ptlr")) {
                pathToLibraryResource = value;
                File file = new File(pathToLibraryResource);
                setOutputPath(file.getParent());
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

        File[] cqlFiles = null;
        if (isCreate) {
            cqlFiles = libraryDir.listFiles(pathname -> !pathname.isDirectory() && !pathname.isHidden());
            if (cqlFiles == null) {
                return;
            }
            else if (cqlFiles.length == 0) {
                return;
            }
        }
        else {
            if (pathToLibrary == null) {
                throw new IllegalArgumentException("The path to the CQL library to be updated is required");
            }
            File pathToLibraryFile = new File(pathToLibrary);
            if (!pathToLibraryFile.isFile()) {
                throw new IllegalArgumentException("The specified path to the library file is not a file");
            }

            if (pathToLibraryResource == null) {
                throw new IllegalArgumentException("The path to the library resource to be updated is required");
            }
            File pathToLibraryResourceFile = new File(pathToLibraryResource);
            if (!pathToLibraryResourceFile.isFile()) {
                throw new IllegalArgumentException("The specified path to the library resource file is not a file");
            }
            setOutputPath(pathToLibraryResourceFile.getParent());

            cqlFiles = new File[] { pathToLibraryFile };
        }

        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
        sourceProvider = new GenericLibrarySourceProvider(pathToLibraryDir);
        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        CqlTranslator translator;
        for (File cqlFile : cqlFiles) {
            translator = translate(cqlFile);
            org.hl7.elm.r1.Library elm = translator.toELM();
            String libraryName = elm.getIdentifier().getId();
            if (pathToLibraryResource != null) {
                libraryResource = loadLibrary(new File(pathToLibraryResource));
                resourceFileNames.put(libraryResource.getId(), getFileNameWithoutExtension(pathToLibraryResource));
            }
            String libraryId = determineLibraryId(libraryName);
            libraryNameIdMap.put(libraryName, libraryId);
            translatorMap.put(libraryName, translator);
            cqlMap.put(libraryName, getCql(cqlFile));
        }

        for (Map.Entry<String, CqlTranslator> entry : translatorMap.entrySet()) {
            if (!libraryMap.containsKey(entry.getKey())) {
                processLibrary(entry.getKey(), entry.getValue());
            }
        }

        output();
    }

    private String getFileNameWithoutExtension(String path) {
        File file = new File(path);
        String fileName = file.getName();
        if (fileName.indexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    private String determineLibraryId(String libraryName) {
        if (libraryResource != null) {
            return libraryResource.getId();
        }
        return libraryName.toLowerCase().replace('_', '-');
    }

    private Library loadLibrary(File path) {
        Library library = null;
        try {
            if (path.toString().toLowerCase().endsWith("json")) {
                library = (Library)FhirContext.forDstu3().newJsonParser().parseResource(new FileInputStream(path));
            }
            else {
                library = (Library)FhirContext.forDstu3().newXmlParser().parseResource(new FileInputStream(path));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return library;
    }

    public String getCql(File file) {
        StringBuilder cql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                cql.append(line);
                cql.append("\r\n");
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

    public void processLibrary(String libraryName, CqlTranslator translator) {
        Library library = libraryResource != null ? libraryResource : createLibrary(libraryName);
        org.hl7.elm.r1.Library elm = translator.toELM();
        // TODO: Fix this - skipping the includes as a hack to avoid having to pass in the
        //  dependency context for a single library content refresh
        if (isCreate) {
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

                /* Data requirements should not be recursive, this shouldn't be here
                for (DataRequirement req : libraryMap.get(def.getPath()).getDataRequirement()) {
                    library.addDataRequirement(req);
                }
                */
                }
            }
        }

        refreshDataRequirements(library, translator);
        refreshContent(library, translator, cqlMap.get(libraryName));
        libraryMap.put(libraryName, library);
    }

    public Library createLibrary(String name) {
        Library library = new Library();
        library.setId(libraryNameIdMap.get(name));
        library.setName(name);
        library.setStatus(Enumerations.PublicationStatus.DRAFT);
        library.setType(new CodeableConcept().addCoding(new Coding().setCode("logic-library").setSystem("http://hl7.org/fhir/codesystem-library-type.html")));
        return library;
    }

    public void refreshDataRequirements(Library library, CqlTranslator translator) {
        library.getDataRequirement().clear();
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

    public void refreshContent(Library library, CqlTranslator translator, String cql) {
        library.setText(null);
        library.getContent().clear();
        library.addContent(
                new Attachment()
                        .setContentType("application/elm+xml")
                        .setData(translator.toXml().getBytes())
        );
        library.addContent(
                new Attachment()
                        .setContentType("text/cql")
                        .setData(cql.getBytes())
        );
    }

    private String getResourceFileName(String libraryId) {
        if (resourceFileNames.containsKey(libraryId)) {
            return resourceFileNames.get(libraryId);
        }

        return libraryId;
    }

    // Output
    public void output() {
        for (Map.Entry<String, Library> entry : libraryMap.entrySet()) {
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + getResourceFileName(libraryNameIdMap.get(entry.getKey())) + "." + encoding)) {
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
