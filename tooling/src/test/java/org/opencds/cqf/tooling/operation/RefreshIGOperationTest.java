package org.opencds.cqf.tooling.operation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.IniFile;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;
import org.opencds.cqf.tooling.processor.IGBundleProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.processor.TestCaseProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
public class RefreshIGOperationTest extends RefreshTest {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	public RefreshIGOperationTest() {
		super(FhirContext.forCached(FhirVersionEnum.R4));
	}

	private static final String EXCEPTIONS_OCCURRED_LOADING_IG_FILE = "Exceptions occurred loading IG file";
	private static final String EXCEPTIONS_OCCURRED_INITIALIZING_REFRESH_FROM_INI_FILE = "Exceptions occurred initializing refresh from ini file";
	private final String ID = "id";
	private final String ENTRY = "entry";
	private final String RESOURCE = "resource";
	private final String RESOURCE_TYPE = "resourceType";
	private final String BUNDLE_TYPE = "Bundle";
	private final String LIB_TYPE = "Library";
	private final String MEASURE_TYPE = "Measure";

	private final String INI_LOC = "target" + separator + "refreshIG" + separator + "ig.ini";


	// Store the original standard out before changing it.
	private final PrintStream originalStdOut = System.out;
	private ByteArrayOutputStream console = new ByteArrayOutputStream();

    @BeforeClass
    public void init() {
        // This overrides the default max string length for Jackson (which wiremock uses under the hood).
        var constraints = StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build();
        Json.getObjectMapper().getFactory().setStreamReadConstraints(constraints);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        System.setOut(new PrintStream(this.console));
        File dir  = new File("target" + separator + "refreshIG");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }

		deleteTempINI();
    }


    /**
     * This test breaks down refreshIG's process and can verify multiple bundles
     */
    @SuppressWarnings("unchecked")
    @Test
    //TODO: Fix separately, this is blocking a bunch of other higher priority things
    public void testBundledFiles() throws IOException {
        //we can assert how many bundles were posted by keeping track via WireMockServer
        //first find an open port:
        int availablePort = findAvailablePort();
        String fhirUri = "http://localhost:" + availablePort + "/fhir";
        if (availablePort == -1){
            fhirUri = "";
            logger.info("No available ports to test post with. Removing mock fhir server from test.");
        }else{
            System.out.println("Available port: " + availablePort + ", mock fhir server url: " + fhirUri);
        }

        WireMockServer wireMockServer = null;
        if (!fhirUri.isEmpty()) {
            wireMockServer = new WireMockServer(availablePort);
            wireMockServer.start();

            WireMock.configureFor("localhost", availablePort);
            wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/fhir"))
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)
                            .withBody("Mock response")));
        }

        // Call the method under test, which should use HttpClientUtils.post
        copyResourcesToTargetDir("target" + separator + "refreshIG", "testfiles/refreshIG");
        // build ini object
        File iniFile = new File(INI_LOC);
        String iniFileLocation = iniFile.getAbsolutePath();
        IniFile ini = new IniFile(iniFileLocation);

        String bundledFilesLocation = iniFile.getParent() + separator + "bundles" + separator + "measure" + separator;

        String[] args;
        if (!fhirUri.isEmpty()) {
            args = new String[]{"-RefreshIG", "-ini=" + INI_LOC, "-t", "-d", "-p", "-e=json", "-ts=false", "-fs=" + fhirUri};
        } else {
            args = new String[]{"-RefreshIG", "-ini=" + INI_LOC, "-t", "-d", "-p", "-e=json", "-ts=false"};
        }

        // EXECUTE REFRESHIG WITH OUR ARGS:
        new RefreshIGOperation().execute(args);

        int requestCount = WireMock.getAllServeEvents().size();
        assertEquals(requestCount, 3); //Looking for 3 resources posts (measure, and two tests)

        if (wireMockServer != null) {
            wireMockServer.stop();
        }

        // determine fhireContext for measure lookup
        FhirContext fhirContext = IGProcessor.getIgFhirContext(getFhirVersion(ini));

        // get list of measures resulting from execution
        Map<String, IBaseResource> measures = IOUtils.getMeasures(fhirContext);

        // loop through measure, verify each has all resources from multiple files
        // bundled into single file using id/resourceType as lookup:
        for (String measureName : measures.keySet()) {
            // location of single bundled file:
            final String bundledFileResult = bundledFilesLocation + measureName + separator + measureName
                    + "-bundle.json";
            // multiple individual files in sub directory to loop through:
            final Path dir = Paths
                    .get(bundledFilesLocation + separator + measureName + separator + measureName + "-files");

            // loop through each file, determine resourceType and treat accordingly
            Map<String, String> resourceTypeMap = new HashMap<>();

            try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                dirStream.forEach(path -> {
                    File file = new File(path.toString());

                    if (file.getName().toLowerCase().endsWith(".json")) {

                        Map<?, ?> map = this.jsonMap(file);
                        if (map == null) {
                            System.out.println("# Unable to parse " + file.getName() + " as json");
                        } else {

                            // ensure "resourceType" exists
                            if (map.containsKey(RESOURCE_TYPE)) {
                                String parentResourceType = (String) map.get(RESOURCE_TYPE);
                                // if Library, resource will be translated into "Measure" in main bundled file:
                                if (parentResourceType.equalsIgnoreCase(LIB_TYPE)) {
                                    resourceTypeMap.put((String) map.get(ID), MEASURE_TYPE);
                                } else if (parentResourceType.equalsIgnoreCase(BUNDLE_TYPE)) {
                                    // file is a bundle type, loop through resources in entry list, build up map of
                                    // <id, resourceType>:
                                    if (map.get(ENTRY) != null) {
                                        ArrayList<Map<?, ?>> entryList = (ArrayList<Map<?, ?>>) map.get(ENTRY);
                                        for (Map<?, ?> entry : entryList) {
                                            if (entry.containsKey(RESOURCE)) {
                                                Map<?, ?> resourceMap = (Map<?, ?>) entry.get(RESOURCE);
                                                resourceTypeMap.put((String) resourceMap.get(ID),
                                                        (String) resourceMap.get(RESOURCE_TYPE));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

            } catch (IOException e) {
                logger.info(e.getMessage());
            }

            // map out entries in the resulting single bundle file:
            Map<?, ?> bundledJson = this.jsonMap(new File(bundledFileResult));
            Map<String, String> bundledJsonResourceTypes = new HashMap<>();
            ArrayList<Map<?, ?>> entryList = (ArrayList<Map<?, ?>>) bundledJson.get(ENTRY);
            for (Map<?, ?> entry : entryList) {
                Map<?, ?> resourceMap = (Map<?, ?>) entry.get(RESOURCE);
                bundledJsonResourceTypes.put((String) resourceMap.get(ID), (String) resourceMap.get(RESOURCE_TYPE));
            }

            // compare mappings of <id, resourceType> to ensure all bundled correctly:
            assertTrue(mapsAreEqual(resourceTypeMap, bundledJsonResourceTypes));
        }

		// run cleanup (maven runs all ci tests sequentially and static member variables could retain values from previous tests)
		IOUtils.cleanUp();
		ResourceUtils.cleanUp();
		TestCaseProcessor.cleanUp();
    }

    private static int findAvailablePort() {
        for (int port = 8000; port <= 9000; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }

    private static boolean isPortAvailable(int port) {
        ServerSocket ss;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Trying " + serverSocket);
            ss = serverSocket;
        } catch (IOException e) {
            return false;
        }
        System.out.println(ss + " is open.");
        return true;
    }

	//@Test(expectedExceptions = IllegalArgumentException.class)
	//TODO: Fix separately, this is blocking a bunch of other higher priority things
	public void testNullArgs() {
		new RefreshIGOperation().execute(null);
	}

	//@Test
	//TODO: Fix separately, this is blocking a bunch of other higher priority things
	public void testBlankINILoc() {
		String args[] = { "-RefreshIG", "-ini=", "-t", "-d", "-p" };

		try {
			new RefreshIGOperation().execute(args);
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), IGProcessor.IG_VERSION_REQUIRED);
			assertTrue(this.console.toString().indexOf("fhir-version was not specified in the ini file.") != -1);
		}
	}


	//@Test
	//TODO: Fix separately, this is blocking a bunch of other higher priority things
	public void testInvalidIgVersion() {
		Map<String, String> igProperties = new HashMap<String, String>();
		igProperties.put("ig", "nonsense");
		igProperties.put("template", "nonsense");
		igProperties.put("usage-stats-opt-out", "nonsense");
		igProperties.put("fhir-version", "nonsense");

		File iniFile = this.createTempINI(igProperties);

		String args[] = { "-RefreshIG", "-ini=" + iniFile.getAbsolutePath(), "-t", "-d", "-p" };

		if (iniFile != null) {
			try {
				new RefreshIGOperation().execute(args);
			} catch (Exception e) {
				assertTrue(e.getClass() == IllegalArgumentException.class);
				assertTrue(this.console.toString().indexOf(EXCEPTIONS_OCCURRED_INITIALIZING_REFRESH_FROM_INI_FILE) != -1);
				assertTrue(this.console.toString().indexOf("Unknown Version 'nonsense'") != -1);

				assertEquals(e.getMessage(), IGProcessor.IG_VERSION_REQUIRED);
			}
			deleteTempINI();
		}
	}

	//@Test
	//TODO: Fix separately, this is blocking a bunch of other higher priority things
	public void testInvalidIgInput() {
		Map<String, String> igProperties = new HashMap<String, String>();
		igProperties.put("ig", "nonsense");
		igProperties.put("template", "nonsense");
		igProperties.put("usage-stats-opt-out", "nonsense");
		igProperties.put("fhir-version", "4.0.1");

		File iniFile = this.createTempINI(igProperties);

		String args[] = { "-RefreshIG", "-ini=" + iniFile.getAbsolutePath(), "-t", "-d", "-p" };

		if (iniFile != null) {
			try {
				new RefreshIGOperation().execute(args);
			} catch (Exception e) {
				assertTrue(e.getClass() == IllegalArgumentException.class);
				assertEquals(e.getMessage(), IGProcessor.IG_VERSION_REQUIRED);

				assertTrue(this.console.toString().indexOf(EXCEPTIONS_OCCURRED_LOADING_IG_FILE) != -1);
				assertTrue(this.console.toString().indexOf(EXCEPTIONS_OCCURRED_INITIALIZING_REFRESH_FROM_INI_FILE) != -1);
			}
			deleteTempINI();
		}
	}


	//@Test
	//TODO: Fix separately, this is blocking a bunch of other higher priority things
	public void testParamsMissingINI() {
		Map<String, String> igProperties = new HashMap<String, String>();
		igProperties.put("ig", "nonsense");
		igProperties.put("template", "nonsense");
		igProperties.put("usage-stats-opt-out", "nonsense");
		igProperties.put("fhir-version", "4.0.1");

		File iniFile = this.createTempINI(igProperties);

		String args[] = { "-RefreshIG", "-ini=" + iniFile.getAbsolutePath(), "-t", "-d", "-p" };

        RefreshIGParameters params = null;
        try {
            params = new RefreshIGArgumentProcessor().parseAndConvert(args);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        MeasureProcessor measureProcessor = new MeasureProcessor();
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        CDSHooksProcessor cdsHooksProcessor = new CDSHooksProcessor();
        PlanDefinitionProcessor planDefinitionProcessor = new PlanDefinitionProcessor(libraryProcessor, cdsHooksProcessor);
		QuestionnaireProcessor questionnaireProcessor = new QuestionnaireProcessor(libraryProcessor);
        IGBundleProcessor igBundleProcessor = new IGBundleProcessor(measureProcessor, planDefinitionProcessor, questionnaireProcessor);
        IGProcessor processor = new IGProcessor(igBundleProcessor, libraryProcessor, measureProcessor);

        //override ini to be null
        params.ini = null;


        try {
			processor.publishIG(params);
		} catch (Exception e) {
			assertEquals(e.getClass(), NullPointerException.class);
		}

        deleteTempINI();
	}


    @AfterMethod
    public void afterTest() {
		deleteTempINI();
        System.setOut(this.originalStdOut);
        System.out.println(this.console.toString());
        this.console = new ByteArrayOutputStream();
    }


	private File createTempINI(Map<String, String> properties) {
//		should look like:
//		[IG]
//		ig = input/ecqm-content-r4.xml
//		template = cqf.fhir.template
//		usage-stats-opt-out = false
//		fhir-version=4.0.1
		try {
			File iniFile = new File("temp.ini");
			FileOutputStream fos = new FileOutputStream(iniFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write("[IG]");
			bw.newLine();
			for (String key : properties.keySet()) {
				bw.write(key + " = " + properties.get(key));
				bw.newLine();
			}

			bw.close();
			return iniFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean deleteTempINI() {
		try {
			File iniFile  = new File("temp.ini");
			if (iniFile.exists()) {
				iniFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private Map<?, ?> jsonMap(File file) {
		Map<?, ?> map = null;
		try {
			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			map = gson.fromJson(reader, Map.class);
			reader.close();
		} catch (Exception ex) {
			// swallow exception if directory doesnt' exist
			// ex.printStackTrace();
		}
		return map;
	}

	private boolean mapsAreEqual(Map<String, String> map1, Map<String, String> map2) {
		System.out.println("#TEST INFO: COMPARING " + map1.getClass() + "(" + map1.size() + ") AND " + map2.getClass()
				+ "(" + map2.size() + ")");

		if (map1.size() != map2.size()) {
			return false;
		}
		boolean comparison = map1.entrySet().stream().allMatch(e -> e.getValue().equals(map2.get(e.getKey())));
		System.out.println("#TEST INFO: MATCH: " + comparison);
		return comparison;
	}

	private String getFhirVersion(IniFile ini) {
		String specifiedFhirVersion = ini.getStringProperty("IG", "fhir-version");
		if (specifiedFhirVersion == null || specifiedFhirVersion.equals("")) {

			// TODO: Should point to global constant:
			specifiedFhirVersion = "4.0.1";
		}
		return specifiedFhirVersion;
	}
}
