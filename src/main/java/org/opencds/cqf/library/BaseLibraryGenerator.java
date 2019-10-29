package org.opencds.cqf.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.Operation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseLibraryGenerator<L extends IBaseResource, T extends BaseNarrativeProvider> extends Operation {

    T narrativeProvider;
    FhirContext fhirContext;

    String operationName;
    String encoding = "json";

    private String pathToLibraryDir;
    private ModelManager modelManager;
    private LibraryManager libraryManager;
    private LibrarySourceProvider sourceProvider;

    Map<String, CqlTranslator> translatorMap = new HashMap<>();
    Map<String, String> cqlMap = new HashMap<>();
    Map<String, String> elmMap = new HashMap<>();
    Map<String, L> libraryMap = new HashMap<>();

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals(operationName)) {
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
            if (!cqlFile.getName().endsWith(".cql")) continue;
            translator = translate(cqlFile);
            translatorMap.put(translator.toELM().getIdentifier().getId(), translator);
            cqlMap.put(translator.toELM().getIdentifier().getId(), getCql(cqlFile));
            if (encoding.equals("json")) {
                elmMap.put(translator.toELM().getIdentifier().getId(), translator.toJson());
            }
            else {
                elmMap.put(translator.toELM().getIdentifier().getId(), translator.toXml());
            }
        }

        for (Map.Entry<String, CqlTranslator> entry : translatorMap.entrySet()) {
            if (!libraryMap.containsKey(entry.getKey())) {
                processLibrary(entry.getKey(), entry.getValue());
            }
        }

        output();
    }

    public abstract void processLibrary(String id, CqlTranslator translator);
    public abstract void output();

    private String getCql(File file) {
        StringBuilder cql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                cql.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error reading CQL file: " + file.getName());
        }

        return cql.toString();
    }

    private CqlTranslator translate(File cqlFile) {
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

    String getValueSetId(String valueSetName) {
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
}
