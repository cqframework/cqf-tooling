package org.opencds.cqf.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseLibraryGenerator<L extends IBaseResource, T extends BaseNarrativeProvider> extends Operation {

    private T narrativeProvider;
    private FhirContext fhirContext;

    private String operationName;
    private String encoding = "json";
    private File cqlContentDir;
    private File[] cqlFiles;

    private String pathToCQLContent;
    private String pathToCqlContentDir;
    private ModelManager modelManager;
    private LibraryManager libraryManager;
    private LibrarySourceProvider sourceProvider;

    private String pathToLibrary;
    private Map<String, CqlTranslator> translatorMap = new HashMap<>();
    private Map<String, String> cqlMap = new HashMap<>();
    private Map<String, String> elmMap = new HashMap<>();
    private Map<String, L> libraryMap = new HashMap<>();

    //instead of processLibrary this would be refreshLibrary or refreshMeasure
    public abstract void processLibrary(String id, CqlTranslator translator);
    public abstract void output();

    @Override
    public void execute(String[] args) {
        buildArgs(args);
        setRelevantCqlFiles();
        
        modelManager = new ModelManager();
        sourceProvider = new DefaultLibrarySourceProvider(new File(pathToCQLContent).getParentFile().toPath());
        //sourceProvider = new GenericLibrarySourceProvider(pathToCqlContentDir);
        libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        translateCqlFiles();

        for (Map.Entry<String, CqlTranslator> entry : translatorMap.entrySet()) {
            if (!libraryMap.containsKey(entry.getKey())) {
                processLibrary(entry.getKey(), entry.getValue());
            }
        }

        output();
    }

    protected String getValueSetId(String valueSetName) {
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

    private void buildArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals(operationName)) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            String flag = flagAndValue[0];
            String value = flagAndValue.length < 2 ? null : flagAndValue[1];

            switch (flag) {
                case "-pathtocqlcontent":
                case "-ptcql":
                    pathToCQLContent = value;
                    break;
                case "-pathtolibrary":
                case "-ptl":
                    pathToLibrary = value;
                    break;
                case "-encoding":
                case "-e":
                    encoding = value == null ? "json" : value.toLowerCase();
                    break;
                case "-outputpath":
                case "-op":
                    setOutputPath(value);
                    break;        
            }
        }

        if(pathToCQLContent == null)
        {
            throw new IllegalArgumentException("The path to the CQL Content is required");
        }  
    }

    private void setRelevantCqlFiles() {
        // TODO: I don't understand why we even need to do this, we've been given a CQL input file
        // The expectation is that that file is in a directory with any required dependencies
        // And we've been given an output file
        // Shouldn't we _only_ be processing that file?
        File cqlContent = new File(pathToCQLContent);
        cqlFiles = new File[] { cqlContent };
/*
        cqlContentDir = cqlContent.getParentFile();
        pathToCqlContentDir = cqlContentDir.getPath();
        if (!cqlContentDir.isDirectory()) {
            throw new IllegalArgumentException("The specified path to library files is not a directory");
        }
        String cql = getCql(cqlContent);
        ArrayList<String> dependencyLibraries = getIncludedLibraries(cql);
        File[] allCqlContentFiles = cqlContentDir.listFiles();
        if (allCqlContentFiles == null) {
            return;
        }
        else if (allCqlContentFiles.length == 0) {
            return;
        }
        ArrayList<File> dependencyLibrarieFiles = new ArrayList<>();
        dependencyLibrarieFiles.add(cqlContent);
        for (File cqlFile : allCqlContentFiles) {
            if (dependencyLibraries.contains(cqlFile.getName().replace(".cql", ""))) {
                dependencyLibrarieFiles.add(cqlFile);
            }

        }
        cqlFiles = dependencyLibrarieFiles.toArray(new File[0]);
*/
    }

    private void translateCqlFiles() {
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
    }

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
                    options.toArray(new CqlTranslator.Options[0])
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

    // TODO: This should be pulled from the ELM, not the CQL directly, why are we doing this this way?
    private ArrayList<String> getIncludedLibraries(String cql) {
        int includeDefinitionIndex = cql.indexOf("include");
        int index = 0;
        ArrayList<String> relatedArtifacts = new ArrayList<>();

        if (includeDefinitionIndex >= 0) {
            String[] includedDefsAndBelow = cql.substring(includeDefinitionIndex).split("\\n");

            while (includedDefsAndBelow[index].startsWith("include")) {
                String includedLibraryName = includedDefsAndBelow[index].replace("include ", "").split(" version ")[0];
                String includedLibraryVersion = includedDefsAndBelow[index].replace("include ", "").split(" version ")[1].replaceAll("\'", "").split(" called")[0];
                String includedLibraryId = includedLibraryName + "-" + includedLibraryVersion;
                relatedArtifacts.add(includedLibraryId);
                index++;
            }
        }

        return relatedArtifacts;
    }
    private String getIdFromSource(String cql) {
        if (cql.startsWith("library")) {
            return getNameFromSource(cql);
        }

        throw new RuntimeException("This tool requires cql libraries to include a named/versioned identifier");
    }

    private String getNameFromSource(String cql) {
        return cql.replaceFirst("library ", "").split(" version")[0].replaceAll("\"", "");
    }

    private String getVersionFromSource(String cql) {
        return cql.split("version")[1].split("'")[1];
    }

    protected T getNarrativeProvider() {
        return narrativeProvider;
    }

    protected void setNarrativeProvider(T narrativeProvider) {
        this.narrativeProvider = narrativeProvider;
    }

    protected FhirContext getFhirContext() {
        return fhirContext;
    }

    protected void setFhirContext(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    protected String getOperationName() {
        return operationName;
    }

    protected void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    protected String getEncoding() { return encoding; }

    protected Map<String, L> getLibraryMap() {
        return libraryMap;
    }

    protected Map<String, CqlTranslator> getTranslatorMap() {
        return translatorMap;
    }

    protected Map<String, String> getCqlMap() {
        return cqlMap;
    }

    protected Map<String, String> getElmMap() {
        return elmMap;
    }

    protected String getPathToLibrary() {
        return pathToLibrary;
    }
}
