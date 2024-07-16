package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.elm.serializing.ElmLibraryReader;
import org.cqframework.cql.elm.serializing.ElmLibraryReaderFactory;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.Environment;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.tooling.utilities.ElmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

public class DroolToElmVisitorIT {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    @Ignore("Disabled in 3.0 update, needs clinical-reasoning support")
    public void EvaluatePatientHasADiagnosisOf() throws IOException {
        setup("[]_Thrombocytopenia_UPDATED_TEMPLATE_5477e.xml");
        List<String> expressions = readFileInList("[]_Thrombocytopenia__Expression.txt");
        List<Object> results = new ArrayList<>();
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
                        EvaluationResult evaluationResult = engine.evaluate(new VersionedIdentifier().withId("[]_Thrombocytopenia_UPDATED_TEMPLATE_5477e").withVersion("4.0.1"), Collections.singleton(expression));
                        Object result = evaluationResult.forExpression(expression).value();
                        if (result instanceof Boolean) {
                            if (!((Boolean) result)) {
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
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Disabled in 3.0 update, needs clinical-reasoning support")
    public void test_ModelingK() throws IOException {
        String xmlFileName = "GeneratedCql14_922c0.xml";
        String expressionListFilePath =
        "GeneratedCql14_922c0_Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("GeneratedCql14_922c0").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Fails with latest engine because of signature requirements")
    public void test_ModelingLAndD() throws IOException {
        String xmlFileName = "_Medications_Administered_2764c.xml";
        String expressionListFilePath = "_Medications_Adminis_Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("_Medications_Administered_2764c").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Fails with latest engine because of signature requirements")
    public void test_ModelingC() throws IOException {
        String xmlFileName = "GeneratedCql136_89fdc.xml";
        String expressionListFilePath = "GeneratedCql136_89fd_Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("GeneratedCql136_89fdc").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Disabled in 3.0 update, needs clinical-reasoning support")
    public void test_ModelingN() throws IOException {
        String xmlFileName = "Patient_is_deceased_84755.xml";
        String expressionListFilePath = "Patient_is_deceased__Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("Patient_is_deceased_84755").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Disabled in 3.0 update, needs clinical-reasoning support")
    public void other() throws IOException {
        String xmlFileName = "Abdominal_Cramps_314c4.xml";
        String expressionListFilePath = "Abdominal_Cramps_314_Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("Abdominal_Cramps_314c4").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    @Test
    @Ignore("Disabled in 3.0 update, needs clinical-reasoning support")
    public void otherOther() throws IOException {
        String xmlFileName = "Anorexia_baace.xml";
        String expressionListFilePath = "Anorexia_baace_Expression.txt";
        List<Object> results = runAllExpressionsFromFile(xmlFileName, new VersionedIdentifier().withId("Anorexia_baace").withVersion("4.0.1"), expressionListFilePath);
        results.forEach(object -> Assert.assertTrue((Boolean) object));
    }

    private List<Object> runAllExpressionsFromFile(String xmlFileName, VersionedIdentifier versionedIdentifier, String expressionListFilePath) throws IOException {
        setup(xmlFileName);
        List<String> expressions = readFileInList(expressionListFilePath);
        List<Object> results = new ArrayList<>();
        for (String expression : expressions) {
            EvaluationResult evaluationResult = engine.evaluate(versionedIdentifier, Collections.singleton(expression));
            Object result = evaluationResult.forExpression(expression).value();
            if (result instanceof Boolean) {
                if (!((Boolean) result)) {
                    logger.debug("False");
                }
            }
            results.add(result);
        }
        return results;
    }

    private CqlEngine engine;
    public void setup(String libraryPath) throws IOException {
        FhirContext fhirContext = FhirContext.forR4Cached();
        ModelManager modelManager = new ModelManager();
        ElmLibraryReader reader = ElmLibraryReaderFactory.getReader("application/xml");
        Library library = reader.read(this.getClass().getResource(libraryPath));
        CompiledLibrary compiledLibrary = ElmUtils.generateCompiledLibrary(library);
        Map<VersionedIdentifier, CompiledLibrary> cache = new HashMap<>();
        cache.put(library.getIdentifier(), compiledLibrary);
        LibraryManager libraryManager = new LibraryManager(modelManager, CqlCompilerOptions.defaultOptions(), cache);
        IBaseBundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class,
                DroolToElmVisitorIT.class.getResourceAsStream("concepts_full.json"));
        Environment environment = null; // TODO: Use a BundleRepositoryProvider?
        // new Environment(libraryManager, getDataProviders(fhirContext), new BundleTerminologyProvider(fhirContext, bundle));
        engine = new CqlEngine(environment);
    }

    private Map<String, DataProvider> getDataProviders(FhirContext fhirContext) {
        Map<String, DataProvider> dataProviders = new HashMap<>();
        IBaseBundle dataBundle = fhirContext.newJsonParser().parseResource(Bundle.class,
                DroolToElmVisitorIT.class.getResourceAsStream("AllResources.json"));

        // TODO: Use a BundleRepositoryProvider?
        // DataProvider dataProvider = new CompositeDataProvider(new CachingModelResolverDecorator(new R4FhirModelResolver()),
        //         new BundleRetrieveProvider(fhirContext, dataBundle));
        // dataProviders.put("http://hl7.org/fhir", dataProvider);
        return dataProviders;
    }

    public static List<String> readFileInList(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(DroolToElmVisitorIT.class.getResourceAsStream(fileName))));
        while(reader.ready()) {
            list.add(reader.readLine());
        }
        return list;
    }
}
