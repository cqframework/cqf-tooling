package org.opencds.cqf.tooling.questionnaire;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

public class QuestionnaireProcessorTest extends RefreshTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireProcessorTest.class);

    public static final String separator = System.getProperty("file.separator");
    private final String TARGET_PATH = "target" + separator + "bundleQuestionnaires";
    private final String INI_PATH = TARGET_PATH + separator + "ig.ini";
    private ByteArrayOutputStream console = new ByteArrayOutputStream();
    private QuestionnaireBundler questionnaireProcessor;

    public QuestionnaireProcessorTest() {
        super(FhirContext.forCached(FhirVersionEnum.R4), "QuestionnaireProcessorTest");
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        questionnaireProcessor = new QuestionnaireBundler(libraryProcessor);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        System.setOut(new PrintStream(this.console));
        File dir  = new File(TARGET_PATH);
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    public QuestionnaireProcessorTest(QuestionnaireBundler questionnaireProcessor, FhirContext fhirContext) {
        super(fhirContext);
        this.questionnaireProcessor = questionnaireProcessor;
    }

    // A very crude test just to ensure that a bundle file is created. Should be improved and made to be more
    // sophisticated with new $package implementation
    //NOTE: Currently disabled because the testing infrastructure doesn't quite work - IOUtils is has static methods accessing
    // instance state and so the state (an IG load specifically) carries accross tests. Specifically, the paths get set in IOUtils
    // on the first pass and then don't update so in this test, the relevant paths from the IG test data don't ever get set -
    // the paths are still set to those from the first test.
    //@Test
    private void testBundleQuestionnairesR4() throws Exception {
        copyResourcesToTargetDir(TARGET_PATH, "testfiles" + separator + "bundleQuestionnaires");

        File iniFile = new File(INI_PATH);
        String iniFileLocation = iniFile.getAbsolutePath();

        FhirContext fhirContext = FhirContext.forCached(FhirVersionEnum.R4);
        ArrayList<String> refreshedLibraryNames = new ArrayList<>();
        refreshedLibraryNames.add("LibraryEvaluationTest");
        refreshedLibraryNames.add("LibraryEvaluationTestElements");
        refreshedLibraryNames.add("LibraryEvaluationTestConcepts");
        ArrayList<String> binaryPaths = new ArrayList<String>();
        binaryPaths.add(TARGET_PATH + separator + "input" + separator + "cql");
        Boolean includeDependencies = true;
        Boolean includeTerminology = true;
        Boolean includePatientScenarios = true;
        Boolean versioned = false;
        Boolean addBundleTimestamp = false;

        IOUtils.resourceDirectories.add(TARGET_PATH + separator + "input" + separator + "resources");
        IOUtils.resourceDirectories.add(TARGET_PATH + separator + "input" + separator + "vocabulary");
        String outputBundleFilePath =
                TARGET_PATH + separator + "bundles" + separator + "questionnaire" + separator +
                        "libraryevaluationtest" + separator + "libraryevaluationtest-bundle.json";

        questionnaireProcessor.bundleResources(refreshedLibraryNames, TARGET_PATH, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext, null, IOUtils.Encoding.JSON, true);

        File outputBundleFile = new File(outputBundleFilePath);

        logger.info(String.format("OutputBundleFilePath: %s", outputBundleFilePath));
        //TODO: more intelligently inspect the contents. For now just a naive check to see if the bundle file was successfully created.
        assert(outputBundleFile.exists());
    }
}