package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
//import org.hl7.fhir.r5.formats.IParser;
import ca.uhn.fhir.parser.IParser;
import org.testng.annotations.Test;
import org.opencds.cqf.tooling.processor.DataRequirementsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertTrue;

public class DataRequirementsProcessorTest {
    private static Logger logger = LoggerFactory.getLogger(DataRequirementsProcessorTest.class);

    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    @Test
    public void TestDataRequirementsProcessor() {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        try {
            /*
                OpioidCDSCommon.cql
                cernerTestCases.cql
                AdultOutpatientEncountersFHIR4.xml
                AdvancedIllnessandFrailtyExclusionECQMFHIR4.xml
                BCSComponent.xml
                CCSComponent.xml
                FHIRHelpers.xml
                HBPComponent.xml
                HospiceFHIR4.xml
                MATGlobalCommonFunctionsFHIR4.xml
                PVSComponent.xml
                SupplementalDataElementsFHIR4.xml
                TSCComponent.xml
                BCSComponent-v0-0-001-FHIR-4-0-1.xml
                CCSComponent-v0-0-001-FHIR-4-0-1.xml
                HBPComponent-v0-0-001-FHIR-4-0-1.xml
                PVSComponent-v0-0-001-FHIR-4-0-1.xml
                TSCComponent-v0-0-001-FHIR-4-0-1.xml
                PreventiveCareandWellness-v0-0-001-FHIR-4-0-1.xml
             */
            CqlTranslator translator = createTranslator("CompositeMeasures/cql/EXM124-9.0.000.cql", cqlTranslatorOptions);//"OpioidCDS/cql/OpioidCDSCommon.cql", cqlTranslatorOptions);
            assertTrue(translator.getErrors().isEmpty());
            cacheLibrary(translator.getTranslatedLibrary());

            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions, null, false);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            logger.debug(moduleDefString);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void cacheLibrary(TranslatedLibrary library) {
        // Add the translated library to the library manager (NOTE: This should be a "cacheLibrary" call on the LibraryManager, available in 1.5.3+)
        // Without this, the data requirements processor will try to load the current library, resulting in a re-translation
        String libraryPath = NamespaceManager.getPath(library.getIdentifier().getSystem(), library.getIdentifier().getId());
        libraryManager.getTranslatedLibraries().put(libraryPath, library);
    }

    @Test
    public void TestDataRequirementsProcessorWithExpressions() {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        try {
            Set<String> expressions = new HashSet<>();
            // TODO - add expressions to expressions
            expressions.add("Conditions Indicating End of Life or With Limited Life Expectancy");//Active Ambulatory Opioid Rx");
            CqlTranslator translator = createTranslator("OpioidCDS/cql/OpioidCDSCommon.cql", cqlTranslatorOptions);
            assertTrue(translator.getErrors().isEmpty());
            cacheLibrary(translator.getTranslatedLibrary());
            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions, expressions, false);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            logger.debug(moduleDefString);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestLibraryDataRequirements() {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        try {
//            CqlTranslator translator = createTranslator("/ecqm/resources/library-EXM506-2.2.000.json", cqlTranslatorOptions);
            CqlTranslator translator = createTranslator("CompositeMeasures/cql/BCSComponent.cql", cqlTranslatorOptions);
            assertTrue(translator.getErrors().isEmpty());
            cacheLibrary(translator.getTranslatedLibrary());
            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions, null, false);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            logger.debug(moduleDefString);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void setup(String relativePath) {
        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(Paths.get(relativePath)));
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
            setup(null);
        }

        return modelManager;
    }

    private static LibraryManager getLibraryManager() {
        if (libraryManager == null) {
            setup(null);
        }

        return libraryManager;
    }

    private static UcumService getUcumService() {
        if (ucumService == null) {
            setup(null);
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
        File translationTestFile = new File(DataRequirementsProcessorTest.class.getResource(testFileName).getFile());
        reset();
        setup(translationTestFile.getParent());
        CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, getModelManager(), getLibraryManager(), getUcumService(), options);
        return translator;
    }
}
