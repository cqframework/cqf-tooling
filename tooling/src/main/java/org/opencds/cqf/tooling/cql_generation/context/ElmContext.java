package org.opencds.cqf.tooling.cql_generation.context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.IdObjectFactory;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.*;
import org.opencds.cqf.tooling.cql_generation.IOUtil;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;

import com.google.gson.Gson;

/**
 * Carries state needed to build Elm Libraries for any given Model.
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class ElmContext {
    public StringBuilder sb = new StringBuilder();
    public Stack<String> cqlStrings = new Stack<String>();
    public LibraryBuilder libraryBuilder;
    private String currentContext = "Patient"; // default context to patient
    public VmrToModelElmBuilder modelBuilder;
    public Stack<Expression> expressionStack = new Stack<Expression>();
    public Stack<Pair<String, ExpressionRef>> referenceStack = new Stack<Pair<String, ExpressionRef>>();
    public Stack<String> operatorContext = new Stack<String>();
    //libraryName , elmLibrary
    public Map<String, Library> libraries = new HashMap<String, Library>();

    public ElmContext(VmrToModelElmBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    /**
     * initialize a new LibraryBuilder using infromation gathered from {@link VmrToModelElmBuilder modelBuilder}
     * @param libraryInfo libraryInfo
     */
    public void newLibraryBuilder(Pair<VersionedIdentifier, ContextDef> libraryInfo) {
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(modelBuilder.getLibrarySourceProvider());
        // this.setTranslatorOptions(CqlTranslatorOptions.defaultOptions());
        try {
            UcumService ucumService = new UcumEssenceService(
                    UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            libraryManager.setUcumService(ucumService);
            this.libraryBuilder = new LibraryBuilder(libraryManager, new IdObjectFactory());
            this.libraryBuilder.setLibraryIdentifier(libraryInfo.getLeft());
            this.libraryBuilder.getModel(new UsingDef().withUri(modelBuilder.getModelUri())
                    .withLocalIdentifier(modelBuilder.getModelIdentifier()).withVersion(modelBuilder.getModelVersion()));
            this.libraryBuilder.addContext(libraryInfo.getRight());
            libraryBuilder.addInclude(modelBuilder.getIncludeHelper());
            this.libraryBuilder.beginTranslation();
        } catch (UcumException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * end Translation
     * add library to libraries
     */
    public void buildLibrary() {
        this.libraryBuilder.endTranslation();
        libraries.put(libraryBuilder.getLibraryIdentifier().getId(), libraryBuilder.getLibrary());
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
