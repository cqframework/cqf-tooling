package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.r5.model.Measure;
import org.junit.Test;
import org.opencds.cqf.tooling.measure.MeasureRefreshProcessor;

import java.io.*;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class ECQMCreatorTest {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    private String refreshMeasure(String primaryLibraryPath, String measurePath) throws IOException {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        CqlTranslator translator = createTranslator(primaryLibraryPath, cqlTranslatorOptions);
        cacheLibrary(translator.getTranslatedLibrary());
        FhirContext context =  FhirContext.forR5();
        IParser parser = measurePath.endsWith(".json") ? context.newJsonParser() : context.newXmlParser();
        InputStream inputStream = this.getClass().getResourceAsStream(measurePath);
        Measure measureToConvert = parser.parseResource(Measure.class, inputStream);
        MeasureRefreshProcessor refreshProcessor = new MeasureRefreshProcessor();
        Measure returnMeasure = refreshProcessor.refreshMeasure(measureToConvert, libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions);
        assertTrue(null != returnMeasure);
        String measureResourceContent = parser.setPrettyPrint(true).encodeResourceToString(returnMeasure);
        return measureResourceContent;
    }

    @Test
    public void TestBCSComponent() {
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/BCSComponent.cql", "CompositeMeasures/resources/BCSComponent-v0-0-001-FHIR-4-0-1.xml");
            System.out.println(measureResourceContent);
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestCCSComponent() {
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/CCSComponent.cql", "CompositeMeasures/resources/CCSComponent-v0-0-001-FHIR-4-0-1.xml");
            System.out.println(measureResourceContent);
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestHBPComponent() {
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/HBPComponent.cql", "CompositeMeasures/resources/HBPComponent-v0-0-001-FHIR-4-0-1.xml");
            System.out.println(measureResourceContent);
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestPVSComponent() {
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/PVSComponent.cql", "CompositeMeasures/resources/PVSComponent-v0-0-001-FHIR-4-0-1.xml");
            System.out.println(measureResourceContent);
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestTSCComponent() {
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/TSCComponent.cql", "CompositeMeasures/resources/TSCComponent-v0-0-001-FHIR-4-0-1.xml");
            System.out.println(measureResourceContent);
            System.out.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestECQMCreator() {

        // TODO - translate measure into ELM measure then call creator with that measure
        try {
            String measureResourceContent = refreshMeasure("CompositeMeasures/cql/EXM124-9.0.000.cql", "/ecqm/resources/measure-EXM124-9.0.000.json");
            System.out.println(measureResourceContent);
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
            cacheLibrary(translator.getTranslatedLibrary());
            FhirContext context = FhirContext.forR5();
            IParser parser = context.newJsonParser();
            IParser xmlParser = context.newXmlParser();
            InputStream inputStream = this.getClass().getResourceAsStream("CompositeMeasures/resources/BCSComponent-v0-0-001-FHIR-4-0-1.xml");  //"/ecqm/resources/measure-EXM124-9.0.000.json");
            Measure measureToConvert = xmlParser.parseResource(Measure.class, inputStream);

//            Measure measureToConvert = parser.parseResource(Measure.class, inputStream);
            MeasureRefreshProcessor refreshProcessor = new MeasureRefreshProcessor();
            Measure returnMeasure = refreshProcessor.refreshMeasure(measureToConvert, libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions);
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
        File translationTestFile = new File(ECQMCreatorTest.class.getResource(libraryName).getFile());
        reset();
        setup(translationTestFile.getParent());
        CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, getModelManager(), getLibraryManager(), getUcumService(), options);
        return translator;
    }

}
