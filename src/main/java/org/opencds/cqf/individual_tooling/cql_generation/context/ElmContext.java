package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.ContextDef;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.ExpressionRef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ObjectFactory;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;

public class ElmContext {

    public LibraryBuilder libraryBuilder;
    private String modelIdentifier;

    // Put them here for now, but eventually somewhere else?
    public final ObjectFactory of = new ObjectFactory();
    public final org.hl7.cql_annotations.r1.ObjectFactory af = new org.hl7.cql_annotations.r1.ObjectFactory();
    private String currentContext = "Patient"; // default context to patient
    private final List<Retrieve> retrieves = new ArrayList<>();
    private final List<Expression> expressions = new ArrayList<>();
    public final Map<String, Element> contextDefinitions = new HashMap<>();
    private DecimalFormat decimalFormat = new DecimalFormat("#.#");

    // libraryName, libraryString, ElmLibrary
    public Map<String, Pair<String, Library>> elmLibraryMap = new HashMap<String, Pair<String, Library>>();
    public Stack<Expression> expressionStack = new Stack<Expression>();
    public Stack<Pair<String, ExpressionRef>> referenceStack = new Stack<Pair<String, ExpressionRef>>();
    public Stack<String> operatorContext = new Stack<String>();

    public void newLibraryBuilder(VersionedIdentifier versionedIdentifier, ContextDef contextDef) {
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
        // this.setTranslatorOptions(CqlTranslatorOptions.defaultOptions());
        try {
            UcumService ucumService = new UcumEssenceService(
            UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            this.libraryBuilder = new LibraryBuilder(modelManager, libraryManager, ucumService);
            this.libraryBuilder.setTranslatorOptions(CqlTranslatorOptions.defaultOptions());
            this.libraryBuilder.setLibraryIdentifier(versionedIdentifier);
            this.modelIdentifier = "FHIR";
            this.libraryBuilder.getModel(
                    new UsingDef().withUri("http://hl7.org/fhir").withLocalIdentifier(modelIdentifier).withVersion("4.0.0"));
            this.libraryBuilder.addContext(contextDef);
            IncludeDef fhirHelpers = of.createIncludeDef()
                .withLocalIdentifier("FHIRHelpers")
                .withPath("FHIRHelpers")
                .withVersion("4.0.0");

            libraryBuilder.addInclude(fhirHelpers);
            this.libraryBuilder.beginTranslation();
            this.decimalFormat.setParseBigDecimal(true);
        } catch (UcumException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void buildLibrary() {
        this.libraryBuilder.endTranslation();
    }

	public void writeElm(String outpuDirectoryPath) {
        elmLibraryMap.entrySet().stream().forEach(entry -> {
            try {
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
    
    public List<Retrieve> getRetrieves() {
        return retrieves;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String currentContext) {
        this.currentContext = currentContext;
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }
}
