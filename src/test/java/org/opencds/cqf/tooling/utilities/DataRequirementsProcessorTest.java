package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.*;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.Library;
//import org.hl7.fhir.r5.formats.IParser;
import ca.uhn.fhir.parser.IParser;
import org.junit.Test;
import org.opencds.cqf.tooling.processor.DataRequirementsProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class DataRequirementsProcessorTest {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    @Test
    public void TestDataRequirementsProcessor() {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        try {
            CqlTranslator translator = createTranslator("OpioidCDSCommon.cql", cqlTranslatorOptions);
            Library elmLibrary = translator.toELM();
            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(elmLibrary, null);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            System.out.println(moduleDefString);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestDataRequirementsProcessorWithExpressions() {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        try {
            Set<String> expressions = new HashSet<>();
            // TODO - add expressions to expressions
            expressions.add("Active Ambulatory Benzodiazepine Rx");
            CqlTranslator translator = createTranslator("OpioidCDSCommon.cql", cqlTranslatorOptions);
            Library elmLibrary = translator.toELM();
            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(elmLibrary, expressions);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            System.out.println(moduleDefString);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void setup() {
        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(new TestLibrarySourceProvider());
        libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
        try {
            ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
        }
        catch (UcumException e) {
            e.printStackTrace();
        }
    }

    private static void tearDown() {
        ucumService = null;
        libraryManager = null;
        modelManager = null;
    }

    public static void reset() {
        tearDown();
    }

    private static ModelManager getModelManager() {
        if (modelManager == null) {
            setup();
        }

        return modelManager;
    }

    private static LibraryManager getLibraryManager() {
        if (libraryManager == null) {
            setup();
        }

        return libraryManager;
    }

    private static UcumService getUcumService() {
        if (ucumService == null) {
            setup();
        }

        return ucumService;
    }

    public static CqlTranslator createTranslator(String testFileName, CqlTranslator.Options... options) throws IOException {
        return createTranslator(null, testFileName, new CqlTranslatorOptions(options));
    }

    public static CqlTranslator createTranslator(String testFileName, CqlTranslatorOptions options) throws IOException {
        return createTranslator(null, testFileName, options);
    }

    public static CqlTranslator createTranslator(NamespaceInfo namespaceInfo, String testFileName, CqlTranslator.Options... options) throws IOException {
        return createTranslator(namespaceInfo, testFileName, new CqlTranslatorOptions(options));
    }

    public static CqlTranslator createTranslator(NamespaceInfo namespaceInfo, String testFileName, CqlTranslatorOptions options) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File translationTestFile = new File(classLoader.getResource(testFileName).getFile());
        if(null != translationTestFile){
            ModelManager modelManager = new ModelManager();
            LibraryManager libraryManager = new LibraryManager(modelManager);
            libraryManager.getLibrarySourceLoader().registerProvider(new TestLibrarySourceProvider());
            libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
            CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, modelManager, libraryManager, getUcumService(), options);
            return translator;

        }
        return null;
    }
}
