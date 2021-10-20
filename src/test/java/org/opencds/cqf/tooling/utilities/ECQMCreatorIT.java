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
import org.opencds.cqf.tooling.measure.MeasureRefreshProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ECQMCreatorIT {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static UcumService ucumService;

    private static Logger logger = LoggerFactory.getLogger(ECQMCreatorIT.class);

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

    @Test
    public void TestCMS125FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test to use as the template to add the rest of the content for testing
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS125FHIR-v0-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/BreastCancerScreeningsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/BreastCancerScreeningsFHIR.json");
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

    @Test
    public void TestCMS104FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS104-v2-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/DischargedonAntithromboticTherapyFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/DischargedonAntithromboticTherapyFHIR.json");
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
    
    @Test
    public void TestCMS130FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS130FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ColorectalCancerScreeningsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ColorectalCancerScreeningsFHIR.json");
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
    
    @Test
    public void TestCMS347FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS347FHIR-v0-1-013-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FHIR347.cql", "ecqm-content-r4-2021/input/resources/measure/FHIR347.json");
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
    
    @Test
    public void TestHybridHWRFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/HybridHWRFHIR-v1-3-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/HybridHWRFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/HybridHWRFHIR.json");
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
    
    @Test
    public void TestHybridHWMFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/HybridHWMFHIR-v0-101-021-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/HybridHWMFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/HybridHWMFHIR.json");
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
    
    @Test
    public void TestSafeUseofOpioidsFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/Safe Use of OpioidsFHIR-v0-0-009-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/SafeUseofOpioidsConcurrentPrescribingFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/SafeUseofOpioidsConcurrentPrescribingFHIR.json");
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
    
    @Test
    public void TestIPSSwithBPHdxFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/IPSS with BPH dxFHIR-v0-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/UrinarySymptomScoreChange6to12MonthsAfterDiagnosisofBenignProstaticHyperplasiaFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/UrinarySymptomScoreChange6to12MonthsAfterDiagnosisofBenignProstaticHyperplasiaFHIR.json");
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
    
    @Test
    public void TestHIVScreeningFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/HIV ScreeningFHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/HIVScreeningFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/HIVScreeningFHIR.json");
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
    
    @Test
    public void TestDEXAScreenProstateFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/DEXA screen prostateFHIR-v0-0-005-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/BoneDensityProstateCancerAndrogenDeprivationTherapyFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/BoneDensityProstateCancerAndrogenDeprivationTherapyFHIR.json");
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
    
    @Test
    public void TestCMS249FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS249FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AppropriateDXAScansForWomenUnder65FHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AppropriateDXAScansForWomenUnder65FHIR.json");
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
    
    @Test
    public void TestCMS177FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS177FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ChildandAdolescentMajorDepressiveDisorderMDDSuicideRiskAssessmentFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ChildandAdolescentMajorDepressiveDisorderMDDSuicideRiskAssessmentFHIR.json");
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
    
    @Test
    public void TestCMS165FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS165FHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ControllingHighBloodPressureFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ControllingHighBloodPressureFHIR.json");
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
    
    @Test
    public void TestCMS161FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS161FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AdultMajorDepressiveDisorderMDDSuicideRiskAssessmentFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AdultMajorDepressiveDisorderMDDSuicideRiskAssessmentFHIR.json");
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
    
    @Test
    public void TestCMS157FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS157FHIR-v0-0-006-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/OncologyPainIntensityQuantifiedFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/OncologyPainIntensityQuantifiedFHIR.json");
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
    
    @Test
    public void TestCMS156FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS156FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/UseofHighRiskMedicationsintheElderlyFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/UseofHighRiskMedicationsintheElderlyFHIR.json");
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
    
    @Test
    public void TestCMS155FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS155FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/WeightAssessmentandCounselingforNutritionandPhysicalActivityforChildrenandAdolescentsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/WeightAssessmentandCounselingforNutritionandPhysicalActivityforChildrenandAdolescentsFHIR.json");
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
    
    @Test
    public void TestCMS154FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS154FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AppropriateTreatmentforUpperRespiratoryInfectionURIFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AppropriateTreatmentforUpperRespiratoryInfectionURIFHIR.json");
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
    
    @Test
    public void TestCMS153FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS153FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ChlamydiaScreeningforWomenFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ChlamydiaScreeningforWomenFHIR.json");
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
    
    @Test
    public void TestCMS149FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS149FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/DementiaCognitiveAssessmentFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/DementiaCognitiveAssessmentFHIR.json");
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
    
    @Test
    public void TestCMS146FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS146FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AppropriateTestingforPharyngitisFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AppropriateTestingforPharyngitisFHIR.json");
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
    
    @Test
    public void TestCMS145FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS145FHIR-v0-0-007-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/CADBetaBlockerTherapyPriorMIorLVSDFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/CADBetaBlockerTherapyPriorMIorLVSDFHIR.json");
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
    
    @Test
    public void TestCMS144FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS144FHIR-v0-0-005-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/HFBetaBlockerTherapyforLVSDFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/HFBetaBlockerTherapyforLVSDFHIR.json");
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
    
    @Test
    public void TestCMS143FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS143FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/POAGOpticNerveEvaluationFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/POAGOpticNerveEvaluationFHIR.json");
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
    
    @Test
    public void TestCMS139FHIR() { 
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS139FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FallsScreeningforFutureFallRiskFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/FallsScreeningforFutureFallRiskFHIR.json");
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
    
    @Test
    public void TestCMS138FHIR() { 
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS138FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PreventiveCareandScreeningTobaccoUseScreeningandCessationInterventionFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PreventiveCareandScreeningTobaccoUseScreeningandCessationInterventionFHIR.json");
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
    
    @Test
    public void TestCMS137FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS137FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/InitiationandEngagementofAlcoholandOtherDrugDependenceTreatmentFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/InitiationandEngagementofAlcoholandOtherDrugDependenceTreatmentFHIR.json");
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
    
    @Test
    public void TestCMS136FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS136FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FollowUpCareforChildrenPrescribedADHDMedicationADDFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/FollowUpCareforChildrenPrescribedADHDMedicationADDFHIR.json");
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
    
    @Test
    public void TestCMS135FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS135FHIR-v0-0-013-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/HFACEIorARBorARNIforLVSDFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/HFACEIorARBorARNIforLVSDFHIR.json");
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
    
    @Test
    public void TestCMS134FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS134FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/DiabetesMedicalAttentionforNephropathyFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/DiabetesMedicalAttentionforNephropathyFHIR.json");
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
    
    @Test
    public void TestCMS133FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS133FHIR-v0-0-009-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/Cataracts2040BCVAwithin90DaysFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/Cataracts2040BCVAwithin90DaysFHIR.json");
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
    
    @Test
    public void TestCMS131FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS131FHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/DiabetesEyeExamFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/DiabetesEyeExamFHIR.json");
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
    
    @Test
    public void TestCMS129FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS129FHIR-v0-0-005-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ProstateCaAvoidanceBoneScanOveruseFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ProstateCaAvoidanceBoneScanOveruseFHIR.json");
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
    
    @Test
    public void TestCMS128FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS128FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AntidepressantMedicationManagementFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AntidepressantMedicationManagementFHIR.json");
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
    
    @Test
    public void TestCMS127FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS127FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PneumococcalVaccinationStatusforOlderAdultsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PneumococcalVaccinationStatusforOlderAdultsFHIR.json");
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
    
    @Test
    public void TestCMS124FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS124FHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/CervicalCancerScreeningFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/CervicalCancerScreeningFHIR.json");
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
    
    @Test
    public void TestCMS117FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS117FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ChildhoodImmunizationStatusFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ChildhoodImmunizationStatusFHIR.json");
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
    
    @Test
    public void TestCMS90FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS90FHIR-v0-0-005-FHIR-4-0-1.json").getFile() });
        
        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FunctionalStatusAssessmentsforHeartFailureFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/FunctionalStatusAssessmentsforHeartFailureFHIR.json");
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
    
    @Test
    public void TestCMS75FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS75FHIR-v0-0-001-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/ChildrenWhoHaveDentalDecayorCavitiesFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/ChildrenWhoHaveDentalDecayorCavitiesFHIR.json");
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
    
    @Test
    public void TestCMS74FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS74FHIR-v0-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PrimaryCariesPreventionasOfferedbyPCPsincludingDentistsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PrimaryCariesPreventionasOfferedbyPCPsincludingDentistsFHIR.json");
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
    
    @Test
    public void TestCMS72FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS72FHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/AntithromboticTherapyByEndofHospitalDay2FHIR.cql", "ecqm-content-r4-2021/input/resources/measure/AntithromboticTherapyByEndofHospitalDay2FHIR.json");
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
    
    @Test
    public void TestCMS69FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS69FHIR-v0-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PCSBMIScreenAndFollowUpFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PCSBMIScreenAndFollowUpFHIR.json");
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
    
    @Test
    public void TestCMS68FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS68FHIR-v0-0-004-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/DocumentationofCurrentMedicationsFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/DocumentationofCurrentMedicationsFHIR.json");
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
    
    @Test
    public void TestCMS66FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS66FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FunctionalStatusAssessmentforTotalKneeReplacementFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/FunctionalStatusAssessmentforTotalKneeReplacementFHIR.json");
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
    
    @Test
    public void TestCMS56FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS56FHIR-v0-0-003-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/FunctionalStatusAssessmentforTotalHipReplacementFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/FunctionalStatusAssessmentforTotalHipReplacementFHIR.json");
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
    
    @Test
    public void TestCMS50FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS50FHIR-v0-0-013-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/CRLReceiptofSpecialistReportFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/CRLReceiptofSpecialistReportFHIR.json");
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
    
    @Test
    public void TestCMS22FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS22FHIR-v0-0-007-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PreventiveBPScreeningFollowUpFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PreventiveBPScreeningFollowUpFHIR.json");
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
    
    @Test
    public void TestCMS2FHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/CMS2FHIR-v0-0-002-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/PCSDepressionScreenAndFollowUpFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/PCSDepressionScreenAndFollowUpFHIR.json");
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
    
    @Test
    public void TestBCG4NonmuscleInvasiveBCaFHIR() {
        // Extract the bundle
        // NOTE: This is a 2021-AUFHIR measure, this is the test created using TestCMS125FHIR as template
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        o.execute(new String[] { "-ExtractMATBundle", this.getClass().getResource("ecqm-content-r4-2021/bundles/BCG 4 nonmuscle invasive BCaFHIR-v0-0-006-FHIR-4-0-1.json").getFile() });

        try {
            Measure measure = refreshMeasure("ecqm-content-r4-2021/input/cql/IntravesicalBacillusCalmetteGuerinForNonmuscleInvasiveBladderCancerFHIR.cql", "ecqm-content-r4-2021/input/resources/measure/IntravesicalBacillusCalmetteGuerinForNonmuscleInvasiveBladderCancerFHIR.json");
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
        File translationTestFile = new File(ECQMCreatorIT.class.getResource(libraryName).getFile());
        reset();
        setup(translationTestFile.getParent());
        CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, translationTestFile, getModelManager(), getLibraryManager(), getUcumService(), options);
        return translator;
    }

}
