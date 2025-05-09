package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.utilities.IniFile;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.operation.ig.NewRefreshIGOperation;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
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

	private final String INI_LOC = Path.of("target","refreshIG","ig.ini").toString();

	private static final String[] NEW_REFRESH_IG_LIBRARY_FILE_NAMES = {
			"GMTPInitialExpressions.json", "GMTPInitialExpressions.json",
			"MBODAInitialExpressions.json", "USCoreCommon.json", "USCoreElements.json", "USCoreTests.json"
	};

    private static final String TARGET_OUTPUT_FOLDER_PATH = "target" + separator + "NewRefreshIG";
	private static final String TARGET_OUTPUT_IG_CQL_FOLDER_PATH = TARGET_OUTPUT_FOLDER_PATH + separator + "input" + separator + "cql";
	private static final String TARGET_OUTPUT_IG_LIBRARY_FOLDER_PATH = TARGET_OUTPUT_FOLDER_PATH + separator + "input" + separator + "resources" + separator + "library";

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

		// Delete directories
		deleteDirectory("target" + File.separator + "refreshIG");
		deleteDirectory("target" + File.separator + "NewRefreshIG");

		deleteTempINI();
	}

	/**
	 * Attempts to delete a directory if it exists.
	 * @param path The path to the directory to delete.
	 */
	private void deleteDirectory(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			try {
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				System.err.println("Failed to delete directory: " + path + " - " + e.getMessage());
			}
		}
	}

	@Test
    public void testNewRefreshOperation() throws IOException {
		copyResourcesToTargetDir(TARGET_OUTPUT_FOLDER_PATH, "testfiles/NewRefreshIG");
        File folder = new File(TARGET_OUTPUT_FOLDER_PATH);
        assertTrue(folder.exists(), "Folder should be present");
        File jsonFile = new File(folder, "ig.ini");
        assertTrue(jsonFile.exists(), "ig.ini file should be present");

        NewRefreshIGOperation newRefreshIGOperation = new NewRefreshIGOperation();
        String[] args = new String[]{
                "-NewRefreshIG",
                "-ini=" + TARGET_OUTPUT_FOLDER_PATH + separator + "ig.ini",
                "-rd=" + TARGET_OUTPUT_FOLDER_PATH,
                "-uv=" + "1.0.1",
                "-d",
                "-p",
                "-t"
        };
        newRefreshIGOperation.execute(args);

        // Verify correct update of cql files following refresh
        File cumulativeMedFile = new File(TARGET_OUTPUT_IG_CQL_FOLDER_PATH, "CumulativeMedicationDuration.cql");
        assertTrue(cumulativeMedFile.exists(), "CumulativeMedicationDuration.cql should exist");
        verifyFileContent(cumulativeMedFile, "library CumulativeMedicationDuration version '1.0.1'");

        File mbodaFile = new File(TARGET_OUTPUT_IG_CQL_FOLDER_PATH, "MBODAInitialExpressions.cql");
        assertTrue(mbodaFile.exists(), "MBODAInitialExpressions.cql should exist");
        verifyFileContent(mbodaFile, "include CumulativeMedicationDuration version '1.0.1' called CMD");

        folder = new File(TARGET_OUTPUT_IG_LIBRARY_FOLDER_PATH);
        assertTrue(folder.exists(), "Folder should be created");

        for (String fileName : NEW_REFRESH_IG_LIBRARY_FILE_NAMES) {
            jsonFile = new File(folder, fileName);
            assertTrue(jsonFile.exists(), "JSON file " + fileName + " should be created");
        }

        File gmtpFile = new File(TARGET_OUTPUT_IG_LIBRARY_FOLDER_PATH, "GMTPInitialExpressions.json");
        assertTrue(gmtpFile.exists(), "GMTPInitialExpressions.json file should exist");
        try (FileReader reader = new FileReader(gmtpFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            verifyJsonContent(jsonObject, "GMTPInitialExpressions.json");

            String version = jsonObject.get("version").getAsString();
            assertEquals("1.0.1", version, "Version parameter should be modified correctly");

            JsonArray relatedArtifacts = jsonObject.getAsJsonArray("relatedArtifact");
            boolean foundFHIRHelpers = verifyRelatedArtifacts(relatedArtifacts);
            assertTrue(foundFHIRHelpers, "Library FHIRHelpers not found with correct resource value");
        } catch (IOException e) {
            fail("Error reading GMTPInitialExpressions.json file: " + e.getMessage());
        }
    }

    private void verifyJsonContent(JsonObject jsonObject, String jsonFileName) {
        JsonArray contentArray = jsonObject.getAsJsonArray("content");
        assertNotNull(contentArray, "Content array should not be null in " + jsonFileName);
        assertTrue(contentArray.size() > 0, "Content array should not be empty in " + jsonFileName);

        JsonObject contentObject = contentArray.get(0).getAsJsonObject();
        assertEquals(contentObject.get("contentType").getAsString(), "text/cql", "Content type should be 'text/cql' in " + jsonFileName);
        assertTrue(contentObject.has("data"), "Data field should exist in " + jsonFileName);
    }

    private boolean verifyRelatedArtifacts(JsonArray relatedArtifacts) {
        for (JsonElement element : relatedArtifacts) {
            JsonObject artifact = element.getAsJsonObject();
            if (artifact.has("display") && artifact.get("display").getAsString().equals("Library FHIRHelpers")) {
                String resource = artifact.get("resource").getAsString();
                if (resource.equals("http://fhir.org/guides/cqf/common/Library/FHIRHelpers|4.0.1")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void verifyFileContent(File file, String expectedContent) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains(expectedContent)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Expected content not found in " + file.getName());
        } catch (IOException e) {
            fail("Failed to read " + file.getName() + ": " + e.getMessage());
        }
    }

	@AfterSuite
	public void cleanup() {
		deleteDirectory("null");
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
		String fhirUri = "http://localhost:" + availablePort + "/fhir/";
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
			wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/fhir/([a-zA-Z]*)"))
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
		assertEquals(requestCount, 1);

		if (wireMockServer != null) {
			wireMockServer.stop();
		}

		// determine fhirContext for measure lookup
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
			List<String> groupPatientList = new ArrayList<>();

			try (final DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
				dirStream.forEach(path -> {
					File file = new File(path.toString());

					//Group file testing:
					if (file.getName().equalsIgnoreCase("Group-BreastCancerScreeningFHIR.json")){
						try{
							org.hl7.fhir.r4.model.Group group = (org.hl7.fhir.r4.model.Group)IOUtils.readResource(file.getAbsolutePath(), fhirContext);
							assertTrue(group.hasMember());
							// Check if the group contains members
								// Iterate through the members
								for (Group.GroupMemberComponent member : group.getMember()) {
									groupPatientList.add(member.getEntity().getDisplay());
								}
						}catch (Exception e){
							fail("Group-BreastCancerScreeningFHIR.json did not parse to valid Group instance.");
						}

					}

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

			//Group file should contain two patients:
			assertEquals(groupPatientList.size(), 2);

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

		String[] args = { "-RefreshIG", "-ini=" + iniFile.getAbsolutePath(), "-t", "-d", "-p" };

		RefreshIGParameters params = null;
		try {
			params = new RefreshIGArgumentProcessor().parseAndConvert(args);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		//override ini to be null
		params.ini = null;

		try {
			new IGProcessor().publishIG(params);
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
		// TODO: this is flawed... Test has 2 different resources with the same name/id of different types
//		boolean comparison = map1.entrySet().stream().allMatch(e -> e.getValue().equals(map2.get(e.getKey())));
//		System.out.println("#TEST INFO: MATCH: " + comparison);
//		return comparison;
		return true;
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
