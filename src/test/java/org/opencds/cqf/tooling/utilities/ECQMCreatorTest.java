package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.elm.r1.Library;
import org.hl7.fhir.r5.model.Measure;
import org.junit.Test;
import org.opencds.cqf.tooling.measure.ECQMCreator;
import org.opencds.cqf.tooling.processor.DataRequirementsProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class ECQMCreatorTest {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    @Test
    public void TestECQMCreator() {
        Set<String> expressions = new HashSet<>();
        expressions.add("Conditions Indicating End of Life or With Limited Life Expectancy");
        expressions.add("Active Ambulatory Opioid Rx");
        expressions.add("Active Ambulatory Benzodiazepine Rx");
        expressions.add("Active Ambulatory Naloxone Rx");
        expressions.add("Admitted/Referred/Discharged to Hospice Care");
        org.hl7.fhir.r5.model.Library modDefLibrary =  getModuleDefinitionLibrary(expressions);
        assertTrue(null != modDefLibrary);

        ECQMCreator eCQMCreator = new ECQMCreator();
        Measure newMeasure = eCQMCreator.create_eCQM(modDefLibrary);
        FhirContext context =  FhirContext.forR5();
        IParser parser = context.newJsonParser();
        String measureString = parser.setPrettyPrint(true).encodeResourceToString(newMeasure);
        System.out.println(measureString);
    }

    private org.hl7.fhir.r5.model.Library getModuleDefinitionLibrary(Set<String> expressions){
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        try {
            CqlTranslator translator = createTranslator("OpioidCDS/cql/OpioidCDSCommon.cql", cqlTranslatorOptions);
            Library elmLibrary = translator.toELM();
            assertTrue(translator.getErrors().isEmpty());
            cacheLibrary(translator.getTranslatedLibrary());
            DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
            org.hl7.fhir.r5.model.Library moduleDefinitionLibrary = dqReqTrans.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions, expressions);
            assertTrue(moduleDefinitionLibrary.getType().getCode("http://terminology.hl7.org/CodeSystem/library-type").equalsIgnoreCase("module-definition"));

            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            String moduleDefString = parser.setPrettyPrint(true).encodeResourceToString(moduleDefinitionLibrary);
            System.out.println(moduleDefString);

            return  moduleDefinitionLibrary;

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }

    private void cacheLibrary(TranslatedLibrary library) {
        // Add the translated library to the library manager (NOTE: This should be a "cacheLibrary" call on the LibraryManager, available in 1.5.3+)
        // Without this, the data requirements processor will try to load the current library, resulting in a re-translation
        String libraryPath = NamespaceManager.getPath(library.getIdentifier().getSystem(), library.getIdentifier().getId());
        libraryManager.getTranslatedLibraries().put(libraryPath, library);
    }

    private static void tearDown() {
        ucumService = null;
        libraryManager = null;
        modelManager = null;
    }

    public static void reset() {
        tearDown();
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

    public static CqlTranslator createTranslator(String testFileName, CqlTranslatorOptions options) throws IOException {
        return createTranslator(null, testFileName, options);
    }

    public static CqlTranslator createTranslator(NamespaceInfo namespaceInfo, String testFileName, CqlTranslator.Options... options) throws IOException {
        return createTranslator(namespaceInfo, testFileName, new CqlTranslatorOptions(options));
    }

    public static CqlTranslator createTranslator(NamespaceInfo namespaceInfo, String testFileName, CqlTranslatorOptions options) throws IOException {
        File translationTestFile = new File(DataRequirementsProcessorTest.class.getResource(testFileName).getFile());
        if(null != translationTestFile) {
            reset();
            setup(translationTestFile.getParent());
            CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, getModelManager(), getLibraryManager(), getUcumService(), options);
            return translator;

        }
        return null;
    }

}
