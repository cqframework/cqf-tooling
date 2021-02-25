package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gson.Gson;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.builder.ModelElmBuilder;

public class ElmContext {

    public LibraryBuilder libraryBuilder;
    private String currentContext = "Patient"; // default context to patient
    public ModelElmBuilder modelBuilder;

    // libraryName, libraryString, ElmLibrary
    public Map<String, Pair<String, Library>> elmLibraryMap = new HashMap<String, Pair<String, Library>>();
    public Stack<Expression> expressionStack = new Stack<Expression>();
    public Stack<Pair<String, ExpressionRef>> referenceStack = new Stack<Pair<String, ExpressionRef>>();
    public Stack<String> operatorContext = new Stack<String>();
    public final Map<String, Element> contextDefinitions = new HashMap<>();
    public static int elmLibraryIndex = 0;
    //libraryName , libraryBuilder
    public Map<String, LibraryBuilder> libraries = new HashMap<String, LibraryBuilder>();

    public ElmContext(ModelElmBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public void newLibraryBuilder(VersionedIdentifier versionedIdentifier, ContextDef contextDef) {
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(modelBuilder.getLibrarySourceProvider());
        // this.setTranslatorOptions(CqlTranslatorOptions.defaultOptions());
        try {
            UcumService ucumService = new UcumEssenceService(
                    UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            this.libraryBuilder = new LibraryBuilder(modelManager, libraryManager, ucumService);
            this.libraryBuilder.setTranslatorOptions(CqlTranslatorOptions.defaultOptions());
            this.libraryBuilder.setLibraryIdentifier(versionedIdentifier);
            this.libraryBuilder.getModel(new UsingDef().withUri(modelBuilder.getModelUri())
                    .withLocalIdentifier(modelBuilder.getModelIdentifier()).withVersion(modelBuilder.getModelVersion()));
            this.libraryBuilder.addContext(contextDef);
            

            libraryBuilder.addInclude(modelBuilder.getIncludeHelper());
            this.libraryBuilder.beginTranslation();
        } catch (UcumException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void buildLibrary() {
        this.libraryBuilder.endTranslation();
        libraries.put(libraryBuilder.getLibraryIdentifier().getId(), libraryBuilder);
        elmLibraryIndex++;
    }

    public void writeElm(String outpuDirectoryPath) {
        elmLibraryMap.entrySet().stream().forEach(entry -> {
            try {
                // TODO: make dir
                File outputFile = new File(outpuDirectoryPath + "/" + entry.getKey() + ".xml");
                IOUtil.writeToFile(outputFile, entry.getValue().getLeft());
            } catch (Exception e) {
                System.out.println(e.getMessage() + "\n" + outpuDirectoryPath + "/" + entry.getKey() + ".xml");
            }
        });
    }
    
    public void writeElm(File outpuDirectory) {
        if (outpuDirectory.isDirectory()) {
            elmLibraryMap.entrySet().stream().forEach(entry -> {
                try {
                    File outputFile = new File(outpuDirectory.getAbsolutePath() + "/" + entry.getKey() + ".xml");
                    IOUtil.writeToFile(outputFile, entry.getValue().getLeft());
                } catch (Exception e) {
                    System.out.println(e.getMessage() + "\n" + outpuDirectory.getAbsolutePath() + "/" + entry.getKey() + ".xml");
                }
            });
        } else {
            System.out.println("Output directory is not a directory: " + outpuDirectory.getAbsolutePath());
        }
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String currentContext) {
        this.currentContext = currentContext;
    }

        //TODO: remove after resolving missing valuesets
	public void writeValueSets(Set<String> valueSetIds) {
        Gson gson = new Gson();
        String json = gson.toJson(valueSetIds);
        File outputFile = new File("../CQLGenerationDocs/GeneratedDocs/valueset/valuesetids" + ".txt");
        IOUtil.writeToFile(outputFile, json);
	}
}
