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

import java.io.*;
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
        // TODO - translate measure into ELM measure then call creator with that measure
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        String libraryPath = "CompositeMeasures/cql/EXM124-9.0.000.cql";//library-EXM124-9.0.000.json";
        try {
            CqlTranslator translator = createTranslator(libraryPath, cqlTranslatorOptions);
            org.hl7.elm.r1.Library elmLibrary = translator.toELM();
            cacheLibrary(translator.getTranslatedLibrary());
            FhirContext context =  FhirContext.forR5();
            IParser parser = context.newJsonParser();
            InputStream inputStream = this.getClass().getResourceAsStream("/ecqm/resources/measure-EXM124-9.0.000.json");
            Measure measureToConvert = parser.parseResource(Measure.class, inputStream);
            ECQMCreator eCQMCreator = new ECQMCreator();
            Measure returnMeasure = eCQMCreator.create_eCQMFromMeasure(measureToConvert, libraryManager, translator.getTranslatedLibrary());
            assertTrue(null != returnMeasure);
            System.out.println(parser.setPrettyPrint(true).encodeResourceToString(returnMeasure));
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }




/*
        System.out.println(parser.setPrettyPrint(true).encodeResourceToString(measureToConvert));
        System.out.println();


        Set<String> expressions = new HashSet<>();
        expressions.add("Conditions Indicating End of Life or With Limited Life Expectancy");
        expressions.add("Active Ambulatory Opioid Rx");
        expressions.add("Active Ambulatory Benzodiazepine Rx");
        expressions.add("Active Ambulatory Naloxone Rx");
        expressions.add("Admitted/Referred/Discharged to Hospice Care");
        org.hl7.fhir.r5.model.Library modDefLibrary =  getModuleDefinitionLibrary(expressions);
        assertTrue(null != modDefLibrary);

        Measure newMeasure = eCQMCreator.create_eCQMFromLibrary(modDefLibrary);
        String measureString = parser.setPrettyPrint(true).encodeResourceToString(newMeasure);
        System.out.println(measureString);
*/
    }

    @Test
    public void TestECQMCreatorDataRequirements() {
        // TODO - translate measure into ELM measure then call creator with that measure
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        String libraryPath = "CompositeMeasures/cql/BCSComponent.cql"; //EXM124-9.0.000.cql";//library-EXM124-9.0.000.json";
        try {
            CqlTranslator translator = createTranslator(libraryPath, cqlTranslatorOptions);
            org.hl7.elm.r1.Library elmLibrary = translator.toELM();
            cacheLibrary(translator.getTranslatedLibrary());
            FhirContext context = FhirContext.forR5();
            IParser parser = context.newJsonParser();
            IParser xmlParser = context.newXmlParser();
            InputStream inputStream = this.getClass().getResourceAsStream("CompositeMeasures/resources/BCSComponent-v0-0-001-FHIR-4-0-1.xml");  //"/ecqm/resources/measure-EXM124-9.0.000.json");
            Measure measureToConvert = xmlParser.parseResource(Measure.class, inputStream);

//            Measure measureToConvert = parser.parseResource(Measure.class, inputStream);
            ECQMCreator eCQMCreator = new ECQMCreator();
            Measure returnMeasure = eCQMCreator.create_eCQMFromMeasure(measureToConvert, libraryManager, translator.getTranslatedLibrary());
            assertTrue(null != returnMeasure);
            System.out.println(parser.setPrettyPrint(true).encodeResourceToString(returnMeasure));
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void cacheLibrary(TranslatedLibrary library) {
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

    public static CqlTranslator createTranslator(NamespaceInfo namespaceInfo, String libraryName, CqlTranslatorOptions options) throws IOException {
        File translationTestFile = new File(ECQMUtils.class.getResource(libraryName).getFile());
        if(null != translationTestFile) {
            reset();
            setup(translationTestFile.getParent());
            CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, getModelManager(), getLibraryManager(), getUcumService(), options);
            return translator;

        }
        return null;
    }

}
