package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.google.gson.Gson;

import org.cdsframework.dto.ConditionDTO;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.junit.Test;

import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider;
import org.opencds.cqf.cql.evaluator.engine.terminology.BundleTerminologyProvider;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.builder.VmrToFhirElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

import ca.uhn.fhir.context.FhirContext;

import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;

public class DroolToElmVisitorTest {
    @Test
    public void EvaluatePatienthasadiagnosisof() {
        setup("[]_Thrombocytopenia_UPDATED_TEMPLATE_5477e.xml");
        List<String> expressions = readFileInList(
                "../CQLGenerationDocs/GeneratedDocs/elm/expressions/[]_Thrombocytopenia__Expression.txt");
        List<Object> results = new ArrayList<Object>();
        for (String expression : expressions) {
            String observationInterpretationRelatedExpressions = "(?i)Group 1.1-d5acd129f47353d9de23d4f5c54c4821|Group 2.2-bd2434cd92e9dc1f22b181256d29884b|Lab Result Interpretation-bcfb9161a92a6b6575cf9e6658816c12|Group 1-b2ec8fd8ef385f444209414cdd4f0bad|Lab Result Interpretation-459dc3e6db577f49f4221697c63ffec9|Group 2-5acef8b7bbc1bdea7569c54d5f407772|ConditionCriteriaMet";
            if (expression.matches(observationInterpretationRelatedExpressions)) {
                System.out.println("Awaiting engine fix Observation Interpretation logic");
            } else {
                if (expression.equals(
                        "Patient has lab result with test name (specific or an organism or substance)-3f12c73cbe886ab3f908544bb1ca8266")) {
                    System.out.println("Missing ValueSet: https://hln.com/fhir/ValueSet/VHF008");
                } else {
                    System.out.println(" Expression: " + expression);
                    try {
                        Object result = context.resolveExpressionRef(expression).getExpression().evaluate(context);
                        if (result instanceof Boolean) {
                            if (((Boolean) result).booleanValue() == false) {
                                System.out.println("False");
                            }
                        }
                        results.add(result);
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }
        }
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingK() {
        // String xmlFileName = "GeneratedCql14_922c0.xml";
        // String expressionListFilePath =
        // "../CQLGenerationDocs/GeneratedDocs/elm/expressions/GeneratedCql14_922c0_Expression.txt";
        // runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingLAndD() {
        String xmlFileName = "_Medications_Administered_2764c.xml";
        String expressionListFilePath = "../CQLGenerationDocs/GeneratedDocs/elm/expressions/_Medications_Adminis_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingN() {
        String xmlFileName = "Patient_is_deceased_84755.xml";
        String expressionListFilePath = "../CQLGenerationDocs/GeneratedDocs/elm/expressions/Patient_is_deceased__Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingC() {
        String xmlFileName = "GeneratedCql136_89fdc.xml";
        String expressionListFilePath = "../CQLGenerationDocs/GeneratedDocs/elm/expressions/GeneratedCql136_89fd_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void other() {
        // String xmlFileName = "Abdominal_Cramps_314c4.xml";
        // String expressionListFilePath =
        // "../CQLGenerationDocs/GeneratedDocs/elm/expressions/Abdominal_Cramps_314_Expression.txt";
        // runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // setup("Patient_is_deceased_84755.xml");
        generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void otherOther() {
        String xmlFileName = "Anorexia_baace.xml";
        String expressionListFilePath = "../CQLGenerationDocs/GeneratedDocs/elm/expressions/Anorexia_baace_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    private void runAllExpressionsFromFile(String xmlFileName, String expressionListFilePath) {
        setup(xmlFileName);
        List<String> expressions = readFileInList(expressionListFilePath);
        List<Object> results = new ArrayList<Object>();
        for (String expression : expressions) {
            Object result = context.resolveExpressionRef(expression).getExpression().evaluate(context);
            if (result instanceof Boolean) {
                if (((Boolean) result).booleanValue() == false) {
                    System.out.println("False");
                }
            }
            results.add(result);
        }
    }

    private void generateElmForDebug() {
        String encodingPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        File file = new File(encodingPath);
        readAndGenerateCQL(file);
    }

    private void readAndGenerateCQL(File file) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit(conditions);
    }

    private void doVisit(List<ConditionDTO> rootNode) {
        DroolToElmVisitor visitor = new DroolToElmVisitor(CQLTYPES.CONDITION,
                new VmrToFhirElmBuilder("4.0.0", new DecimalFormat("#.#")));
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        ElmContext context = traverser.traverse(rootNode);
        System.out.println("");
    }

    private Library library;
    private Context context;

    public void setup(String libraryPath) {
        try {
            this.library = CqlLibraryReader.read(DroolToElmVisitorTest.class.getResourceAsStream(libraryPath));
        } catch (IOException | JAXBException e) {
            e.getCause().getCause().getMessage();
            throw new IllegalArgumentException("Error reading ELM: " + e.getMessage());
        }
        FhirContext fhirContext = FhirContext.forR4();
        this.context = new Context(library);
        File valuesetIdFile = new File("../CQLGenerationDocs/GeneratedDocs/valueset/valuesetids" + ".txt");
        String json = IOUtil.readFile(valuesetIdFile);
        Gson gson = new Gson();
        Set<String> ids = gson.fromJson(json, Set.class);
        IBaseBundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class,
                DroolToElmVisitorTest.class.getResourceAsStream("concepts_full.json"));
        writeMissingValueSets(gson, ids, bundle);
        registerProviders(fhirContext, bundle);
    }

        //TODO: remove after resolving missing valuesets
    private void writeMissingValueSets(Gson gson, Set<String> ids, IBaseBundle bundle) {
    Set<String> values = new HashSet<String>();
      for (String id : ids) {
         boolean matching = false;
         for (BundleEntryComponent entry : ((Bundle) bundle).getEntry()) {
        if (entry.getResource() != null && entry.getResource() instanceof ValueSet) {
            if (entry.getResource().getIdElement().getIdPart().equals("VHF008")) {
                System.out.println("");
            }
            if (id.equals("VHF008")) {
                System.out.println("");
            }
            if (id.equals(entry.getResource().getIdElement().getIdPart())) {
                matching = true;
            }
        }
         }
         if (!matching && !id.toLowerCase().equals("active")) {
        values.add(id);
         }
         
      }
      String missingValueSets = gson.toJson(values);
      File outputFile = new File("../CQLGenerationDocs/GeneratedDocs/valueset/missingValueSets" + ".txt");
      IOUtil.writeToFile(outputFile, missingValueSets);
    }

    private InputStream getLibraryHelpersElm(FhirContext fhirContext) {
        org.hl7.fhir.r4.model.Library fhirHelpersLibrary = fhirContext.newJsonParser().parseResource(
                org.hl7.fhir.r4.model.Library.class,
                DroolToElmVisitorTest.class.getResourceAsStream("FHIRHelpers.json"));
        for (Attachment attachment : fhirHelpersLibrary.getContent()) {
            if (attachment.getContentType().equals("application/elm+xml")) {
                InputStream xmlInput = new DataInputStream(new ByteArrayInputStream(attachment.getData()));
                return xmlInput;
            }
        }
        throw new RuntimeException("No elm content found on Library Resource: " + fhirHelpersLibrary.getId());
    }

    private void registerProviders(FhirContext fhirContext, IBaseBundle bundle) {
        IBaseBundle dataBundle = fhirContext.newJsonParser().parseResource(Bundle.class,
                DroolToElmVisitorTest.class.getResourceAsStream("AllResources.json"));
        DataProvider dataProvider = new CompositeDataProvider(new R4FhirModelResolver(),
                new BundleRetrieveProvider(fhirContext, dataBundle));
        TerminologyProvider terminologyProvider = new BundleTerminologyProvider(fhirContext, bundle);
        Library fhirHelpers;
        try {
            fhirHelpers = CqlLibraryReader.read(getLibraryHelpersElm(fhirContext));
        } catch (IOException | JAXBException e) {
            e.getCause().getCause().getMessage();
            throw new IllegalArgumentException("Error reading ELM: " + e.getMessage());
        }
        LibraryLoader libraryLoader = new InMemoryLibraryLoader(List.of(library, fhirHelpers));
        context.registerDataProvider(Constants.FHIR_MODEL_URI, dataProvider);
        context.registerLibraryLoader(libraryLoader);
        context.registerTerminologyProvider(terminologyProvider);
    }

    public static List<String> readFileInList(String fileName) {
        List<String> lines = Collections.emptyList();
        try {
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        }

        catch (IOException e) {
            // do something
            e.printStackTrace();
        }
        return lines;
    }
}
