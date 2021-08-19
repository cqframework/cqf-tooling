package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.NamespaceInfo;
import org.cqframework.cql.cql2elm.NamespaceManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.r5.model.DataRequirement;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.operation.ExtractMatBundleOperation;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.opencds.cqf.tooling.measure.MeasureRefreshProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ECQMCreatorTest {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    private static Logger logger = LoggerFactory.getLogger(ECQMCreatorTest.class);

    private static FhirContext context = FhirContext.forR5();

    private Measure refreshMeasure(String primaryLibraryPath, String measurePath) throws IOException {
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        // This option performs data analysis, including element reference detection
        cqlTranslatorOptions.setAnalyzeDataRequirements(true);
        // This option collapses duplicate data requirements
        cqlTranslatorOptions.setCollapseDataRequirements(true);
        CqlTranslator translator = createTranslator(primaryLibraryPath, cqlTranslatorOptions);
        translator.toELM();
        cacheLibrary(translator.getTranslatedLibrary());
        IParser parser = measurePath.endsWith(".json") ? context.newJsonParser() : context.newXmlParser();
        InputStream inputStream = this.getClass().getResourceAsStream(measurePath);
        Measure measureToConvert = parser.parseResource(Measure.class, inputStream);
        MeasureRefreshProcessor refreshProcessor = new MeasureRefreshProcessor();
        Measure returnMeasure = refreshProcessor.refreshMeasure(measureToConvert, libraryManager, translator.getTranslatedLibrary(), cqlTranslatorOptions);
        return returnMeasure;
    }

    private String measureToString(Measure measure) {
        IParser parser = context.newJsonParser().setPrettyPrint(true);
        String measureResourceContent = parser.encodeResourceToString(measure);
        return measureResourceContent;
    }

    private void TestMatOutputCase(String matBundleName, String measureLibraryName){

        System.out.println("Testing " + matBundleName + " " + measureLibraryName);

        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/" + matBundleName).getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/" + measureLibraryName +".cql", "ecqm-content-r4-2021/input/resources/measure/"+ measureLibraryName +".json");
            assertTrue(null != measure);
            // Extract data requirements from the measure:
            List<DataRequirement> drs = new ArrayList<DataRequirement>();
            for (Extension e : measure.getExtensionsByUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement")) {
                if (e.hasValue()) {
                    drs.add(e.getValueDataRequirement());
                }
            }
            assertTrue(!drs.isEmpty());
            // TODO: Measure-specific validation of data requirements content
            logger.debug(measureToString(measure));
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    @DataProvider(name="matMeasureLoader")
    public static Object[][] matMeasures(){
        // TODO: should we be getting the measure name from the json?
        return new Object [][] {
            {"CMS125FHIR-v0-0-004-FHIR-4-0-1.json", "BreastCancerScreeningsFHIR"},
            {"CMS104-v2-0-004-FHIR-4-0-1.json", "DischargedonAntithromboticTherapyFHIR"},
            {"CMS122FHIR-v0-0-004-FHIR-4-0-1.json", "DiabetesHemoglobinA1cHbA1cPoorControl9FHIR"},
            {"CMS130FHIR-v0-0-002-FHIR-4-0-1.json", "ColorectalCancerScreeningsFHIR"},
            {"CMS347FHIR-v0-1-013-FHIR-4-0-1.json", "FHIR347"},
            {"HybridHWMFHIR-v0-101-021-FHIR-4-0-1.json", "HybridHWMFHIR"}
        };
    }


    @Test(dataProvider = "matMeasureLoader")
    public void TestMatOutputCasesParamaterized(String matBundleName, String measureLibraryName){
        TestMatOutputCase(matBundleName, measureLibraryName);
    }

    @Test
    public void TestBCSComponent() {
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/BCSComponent.cql", "CompositeMeasures/resources/BCSComponent-v0-0-001-FHIR-4-0-1.xml");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestCCSComponent() {
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/CCSComponent.cql", "CompositeMeasures/resources/CCSComponent-v0-0-001-FHIR-4-0-1.xml");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestHBPComponent() {
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/HBPComponent.cql", "CompositeMeasures/resources/HBPComponent-v0-0-001-FHIR-4-0-1.xml");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestPVSComponent() {
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/PVSComponent.cql", "CompositeMeasures/resources/PVSComponent-v0-0-001-FHIR-4-0-1.xml");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestTSCComponent() {
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/TSCComponent.cql", "CompositeMeasures/resources/TSCComponent-v0-0-001-FHIR-4-0-1.xml");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Test
    public void TestECQMCreator() {
        // TODO - translate measure into ELM measure then call creator with that measure
        try {
            Measure measure = refreshMeasure("CompositeMeasures/cql/EXM124-9.0.000.cql", "/ecqm/resources/measure-EXM124-9.0.000.json");
            assertTrue(null != measure);
            logger.debug(measureToString(measure));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

/*
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
*/
    }

    @Test
    public void TestECQMCreatorDataRequirements() {
        // TODO - translate measure into ELM measure then call creator with that measure
        CqlTranslatorOptions cqlTranslatorOptions = new CqlTranslatorOptions();
        cqlTranslatorOptions.getFormats().add(CqlTranslator.Format.JSON);
        cqlTranslatorOptions.getOptions().add(CqlTranslator.Options.EnableAnnotations);
        cqlTranslatorOptions.setCollapseDataRequirements(true);
        String libraryPath = "CompositeMeasures/cql/BCSComponent.cql"; //EXM124-9.0.000.cql";//library-EXM124-9.0.000.json";
        try {
            CqlTranslator translator = createTranslator(libraryPath, cqlTranslatorOptions);
            translator.toELM();
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
            logger.debug(parser.setPrettyPrint(true).encodeResourceToString(returnMeasure));
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
