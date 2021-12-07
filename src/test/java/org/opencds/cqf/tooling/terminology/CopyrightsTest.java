package org.opencds.cqf.tooling.terminology;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.utilities.IniFile;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.*;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.*;

public class CopyrightsTest extends RefreshTest {

    private IGProcessor processor;
    private ByteArrayOutputStream console = new ByteArrayOutputStream();

    private final String operation = "-RefreshIG";
    private final String rootDir = "-root-dir=target" + separator + "refreshIG";
    private final String ip = "-ip=target" + separator + "refreshIG" + separator + "input" + separator + "ecqm-content-r4.xml";
    private final String ini = "-ini=target" + separator + "refreshIG" + separator + "ig.ini";
    private final String rp = "-rp=input" + separator + "resources";
    private final String dp = "-dp=input" + separator + "data";
    private final String t = "-t";
    private final String d = "-d";
    private final String p = "-p";

    //copyright text
    private final String cpt = "American Medical Association CPT®";
    private final String snomed = "SNOMED CT®";

    private String[] args = {operation, rootDir, ip, ini, rp, dp, t, d, p};

    public CopyrightsTest() {
        super(FhirContext.forCached(FhirVersionEnum.R4), "CopyrightsRefreshTest");
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        MeasureProcessor measureProcessor = new MeasureProcessor();
        ValueSetsProcessor valueSetsProcessor = new ValueSetsProcessor();
        CDSHooksProcessor cdsHooksProcessor = new CDSHooksProcessor();
        PlanDefinitionProcessor planDefinitionProcessor = new PlanDefinitionProcessor(libraryProcessor, cdsHooksProcessor);
        IGBundleProcessor igBundleProcessor = new IGBundleProcessor(measureProcessor, planDefinitionProcessor);
        processor = new IGProcessor(igBundleProcessor, libraryProcessor, measureProcessor, valueSetsProcessor);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<>();
        IOUtils.dataDirectories = new ArrayList<>();
        IOUtils.clearDevicePaths();
        System.setOut(new PrintStream(this.console));
        File dir  = new File("target" + separator + "refreshIG");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    public void testCopyrights() throws Exception {
        String targetDirectory = "target" + separator + "refreshIG";
        copyResourcesToTargetDir(targetDirectory, "testfiles/refreshIG");

        if (args == null) {
            throw new IllegalArgumentException();
        }

        RefreshIGParameters params = null;
        try {
            params = new RefreshIGArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        File iniFile = new File(params.ini);
        String iniFileLocation = iniFile.getAbsolutePath();
        IniFile ini = new IniFile(iniFileLocation);

        processor.publishIG(params);

        assertFalse(IOUtils.getCopyrightsPath() == null || IOUtils.getCopyrightsPath().equals(""));

        Copyrights copyrights = new Copyrights();
        assertNotNull(copyrights.getName(), "No copyrights file");

        FhirContext fhirContext = IGProcessor.getIgFhirContext(getFhirVersion(ini));

        HashSet<String> valuesetPaths = IOUtils.getTerminologyPaths(fhirContext);
        HashSet<String> libraryPaths = IOUtils.getLibraryPaths(fhirContext);
        HashSet<String> measurePaths = IOUtils.getMeasurePaths(fhirContext);

        ArrayList<ValueSet> valueSets = new ArrayList<>();
        for (String path : valuesetPaths){
            valueSets.add((ValueSet) IOUtils.readResource(path, fhirContext, true));
        }

        assertFalse(valueSets.isEmpty(), "No valuesets collected");

        assertTrue(ValuesetsCopyrightsRefreshed(valueSets));

        ArrayList<org.hl7.fhir.dstu3.model.Library> DSTU3Libraries = new ArrayList<>();
        ArrayList<org.hl7.fhir.r4.model.Library> R4Libraries = new ArrayList<>();
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                for (String path : libraryPaths) {
                    DSTU3Libraries.add((org.hl7.fhir.dstu3.model.Library) IOUtils.readResource(path, fhirContext, true));
                }

                assertFalse(DSTU3Libraries.isEmpty(), "No DSTU3 libraries collected");

                assertTrue(DSTU3LibrariesCopyrightsRefreshed(DSTU3Libraries));

                break;
            case R4:
                for (String path : libraryPaths) {
                    R4Libraries.add((org.hl7.fhir.r4.model.Library) IOUtils.readResource(path, fhirContext, true));
                }

                assertFalse(R4Libraries.isEmpty(), "No R4 libraries collected");

                assertTrue(R4LibrariesCopyrightsRefreshed(R4Libraries));

                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        ArrayList<Measure> measures = new ArrayList<>();
        for (String path : measurePaths){
            measures.add((Measure) IOUtils.readResource(path, fhirContext));
        }

        assertFalse(measures.isEmpty(), "No measures collected");

        assertTrue(MeasuresCopyrightsRefreshed(measures));
    }



    private boolean ValuesetsCopyrightsRefreshed(ArrayList<ValueSet> valuesets) throws AssertionError {
        for (ValueSet valueset : valuesets) {
            if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1023")) {
                if (!valueset.getCopyright().equals(cpt)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1025")) {
                if (!valueset.getCopyright().equals(cpt)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1086")) {
                if (!valueset.getCopyright().equals(cpt)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113762.1.4.1108.15")) {
                if (!valueset.getCopyright().equals(snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.110.12.1082")) {
                if (!valueset.getCopyright().equals(snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.198.12.1068")) {
                if (!valueset.getCopyright().equals(snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1014")) {
                if (!valueset.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1085")) {
                if (!valueset.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.101.12.1088")) {
                if (!valueset.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113762.1.4.1")) {
                if (valueset.hasCopyright()) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: null or '' but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.108.12.1018")) {
                if (valueset.hasCopyright()) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: null or '' but found: " + valueset.getCopyright());
                }
            } else if (valueset.getUrl().endsWith("2.16.840.1.113883.3.464.1003.196.12.1510")) {
                if (valueset.hasCopyright()) {
                    throw new AssertionError("Wrong copyright text for valueset: " + valueset.getUrl() + "Expected: null or '' but found: " + valueset.getCopyright());
                }
            }
        }
        return true;
    }

    private boolean DSTU3LibrariesCopyrightsRefreshed(ArrayList<Library> libraries) throws AssertionError {
        for (Library library : libraries) {
            if (library.getUrl().endsWith("AdultOutpatientEncountersFHIR4")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("DSTU3: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            } else if (library.getUrl().endsWith("AdvancedIllnessandFrailtyExclusionECQMFHIR4")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("DSTU3: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            } else if (library.getUrl().endsWith("BreastCancerScreeningFHIR")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("DSTU3: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            }
        }
        return true;
    }

    private boolean R4LibrariesCopyrightsRefreshed(ArrayList<org.hl7.fhir.r4.model.Library> libraries) throws AssertionError {
        for (org.hl7.fhir.r4.model.Library library : libraries) {
            if (library.getUrl().endsWith("AdultOutpatientEncountersFHIR4")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("R4: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            } else if (library.getUrl().endsWith("AdvancedIllnessandFrailtyExclusionECQMFHIR4")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("R4: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            } else if (library.getUrl().endsWith("BreastCancerScreeningFHIR")) {
                if (!library.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("R4: Wrong copyright text for library: " + library.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + library.getCopyright());
                }
            }
        }
        return true;
    }

    private boolean MeasuresCopyrightsRefreshed(ArrayList<Measure> measures) throws AssertionError {
        for (Measure measure : measures) {
            if (measure.getUrl().endsWith("BreastCancerScreeningFHIR")) {
                if (!measure.getCopyright().equals(cpt + ", " + snomed)) {
                    throw new AssertionError("Wrong copyright text for measure: " + measure.getUrl() + "Expected: " + cpt + ", " + snomed + " but found: " + measure.getCopyright());
                }
            }
        }
        return true;
    }

    private String getFhirVersion(IniFile ini) {
        String specifiedFhirVersion = ini.getStringProperty("IG", "fhir-version");
        if (specifiedFhirVersion == null || specifiedFhirVersion.equals("")) {
            specifiedFhirVersion = "4.0.1";
        }
        return specifiedFhirVersion;
    }
}
