package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cqframework.cql.elm.execution.Library;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.serializing.CqlLibraryReaderFactory;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.builder.Constants;
import org.opencds.cqf.cql.evaluator.engine.model.CachingModelResolverDecorator;
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider;
import org.opencds.cqf.cql.evaluator.engine.terminology.BundleTerminologyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

public class DroolToElmVisitorIT {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void EvaluatePatienthasadiagnosisof() throws IOException {
        setup("[]_Thrombocytopenia_UPDATED_TEMPLATE_5477e.xml");
        List<String> expressions = readFileInList("[]_Thrombocytopenia__Expression.txt");
        List<Object> results = new ArrayList<Object>();
        for (String expression : expressions) {
            String observationInterpretationRelatedExpressions = "(?i)Group 1.1-d5acd129f47353d9de23d4f5c54c4821|Group 2.2-bd2434cd92e9dc1f22b181256d29884b|Lab Result Interpretation-bcfb9161a92a6b6575cf9e6658816c12|Group 1-b2ec8fd8ef385f444209414cdd4f0bad|Lab Result Interpretation-459dc3e6db577f49f4221697c63ffec9|Group 2-5acef8b7bbc1bdea7569c54d5f407772|ConditionCriteriaMet";
            if (expression.matches(observationInterpretationRelatedExpressions)) {
                logger.debug("Awaiting engine fix Observation Interpretation logic");
            } else {
                if (expression.equals(
                        "Patient has lab result with test name (specific or an organism or substance)-3f12c73cbe886ab3f908544bb1ca8266")) {
                            logger.debug("Missing ValueSet: https://hln.com/fhir/ValueSet/VHF008");
                } else {
                    logger.debug(" Expression: " + expression);
                    try {
                        Object result = context.resolveExpressionRef(expression).getExpression().evaluate(context);
                        if (result instanceof Boolean) {
                            if (((Boolean) result).booleanValue() == false) {
                                logger.debug("False");
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
    public void test_ModelingK() throws IOException {
        String xmlFileName = "GeneratedCql14_922c0.xml";
        String expressionListFilePath =
        "GeneratedCql14_922c0_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        //generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingLAndD() throws IOException {
        String xmlFileName = "_Medications_Administered_2764c.xml";
        String expressionListFilePath = "_Medications_Adminis_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void test_ModelingN() throws IOException {
        String xmlFileName = "Patient_is_deceased_84755.xml";
        String expressionListFilePath = "Patient_is_deceased__Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();
        // System.out.println("test");

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    // @Test
    // public void test_ModelingC() {
    //     String xmlFileName = "GeneratedCql136_89fdc.xml";
    //     String expressionListFilePath = "GeneratedCql136_89fd_Expression.txt";
    //     runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
    //     //generateElmForDebug();

    //     // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    // }

    @Test
    public void other() throws IOException {
        String xmlFileName = "Abdominal_Cramps_314c4.xml";
        String expressionListFilePath = "Abdominal_Cramps_314_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // setup("Patient_is_deceased_84755.xml");
        //generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    @Test
    public void otherOther() throws IOException {
        String xmlFileName = "Anorexia_baace.xml";
        String expressionListFilePath = "Anorexia_baace_Expression.txt";
        runAllExpressionsFromFile(xmlFileName, expressionListFilePath);
        // generateElmForDebug();

        // results.forEach(object -> Assert.assertTrue(((List<?>) object).size() == 2));
    }

    private List<Object> runAllExpressionsFromFile(String xmlFileName, String expressionListFilePath) throws IOException {
        setup(xmlFileName);
        List<String> expressions = readFileInList(expressionListFilePath);
        List<Object> results = new ArrayList<Object>();
        for (String expression : expressions) {
            Object result = context.resolveExpressionRef(expression).getExpression().evaluate(context);
            if (result instanceof Boolean) {
                if (((Boolean) result).booleanValue() == false) {
                    logger.debug("False");
                }
            }
            results.add(result);
        }
        return results;
    }

    private Library library;
    private Context context;

    public void setup(String libraryPath) {
        try {
            this.library = CqlLibraryReaderFactory.getReader("application/elm+xml").read(DroolToElmVisitorIT.class.getResourceAsStream(libraryPath));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading ELM: " + e.getMessage());
        }
        FhirContext fhirContext = FhirContext.forR4Cached();
        this.context = new Context(library);
        IBaseBundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class,
                DroolToElmVisitorIT.class.getResourceAsStream("concepts_full.json"));
        registerProviders(fhirContext, bundle);
    }

    private InputStream getLibraryHelpersElm(FhirContext fhirContext) {
        org.hl7.fhir.r4.model.Library fhirHelpersLibrary = fhirContext.newJsonParser().parseResource(
                org.hl7.fhir.r4.model.Library.class,
                DroolToElmVisitorIT.class.getResourceAsStream("FHIRHelpers.json"));
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
                DroolToElmVisitorIT.class.getResourceAsStream("AllResources.json"));
        DataProvider dataProvider = new CompositeDataProvider(new CachingModelResolverDecorator(new R4FhirModelResolver()),
                new BundleRetrieveProvider(fhirContext, dataBundle));
        TerminologyProvider terminologyProvider = new BundleTerminologyProvider(fhirContext, bundle);
        Library fhirHelpers;
        try {
            fhirHelpers = CqlLibraryReaderFactory.getReader("application/elm+xml").read(getLibraryHelpersElm(fhirContext));
        } catch (Exception e) {
            e.getCause().getCause().getMessage();
            throw new IllegalArgumentException("Error reading ELM: " + e.getMessage());
        }
        LibraryLoader libraryLoader = new InMemoryLibraryLoader(Arrays.asList(library, fhirHelpers));
        context.registerDataProvider(Constants.FHIR_MODEL_URI, dataProvider);
        context.registerLibraryLoader(libraryLoader);
        context.registerTerminologyProvider(terminologyProvider);
    }

    public static List<String> readFileInList(String fileName) throws IOException {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(DroolToElmVisitorIT.class.getResourceAsStream(fileName)));
        while(reader.ready()) {
            list.add(reader.readLine());
        }
        return list;
    }
}
