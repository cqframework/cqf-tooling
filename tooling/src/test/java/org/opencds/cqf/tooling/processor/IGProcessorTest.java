package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.Gson;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.IniFile;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.*;

public class IGProcessorTest extends RefreshTest {

	private final ByteArrayOutputStream console = new ByteArrayOutputStream();

	private final String ID = "id";
	private final String ENTRY = "entry";
	private final String RESOURCE = "resource";
	private final String RESOURCE_TYPE = "resourceType";
	private final String BUNDLE_TYPE = "Bundle";
	private final String LIB_TYPE = "Library";
	private final String MEASURE_TYPE = "Measure";
	private final String GROUP_TYPE = "Group";

	private final String INI_LOC = "target" + separator + "refreshIG" + separator + "ig.ini";

	public IGProcessorTest() {
		super(FhirContext.forCached(FhirVersionEnum.R4), "IGProcessorTest");
	}

	@BeforeMethod
	public void setUp() throws Exception {
		IOUtils.resourceDirectories = new ArrayList<>();
		IOUtils.clearDevicePaths();
		System.setOut(new PrintStream(this.console));
		File dir  = new File("target" + separator + "refreshIG");
		if (dir.exists()) {
			FileUtils.deleteDirectory(dir);
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void testRefreshIG() throws Exception {
		String targetDirectory = "target" + separator + "refreshIG";
		copyResourcesToTargetDir(targetDirectory, "testfiles/refreshIG");

		File iniFile = new File(INI_LOC);
		String iniFileLocation = iniFile.getAbsolutePath();
		IniFile ini = new IniFile(iniFileLocation);

		String bundledFilesLocation = iniFile.getParent() + separator + "bundles" + separator + "measure" + separator;
		RefreshIGParameters params = new RefreshIGParameters();
		params.ini = INI_LOC;
		params.outputEncoding = IOUtils.Encoding.JSON;
		params.resourceDirs = new ArrayList<>();
		params.includeELM = false;
		params.includeTerminology = true;
		params.includeDependencies = true;
		params.includePatientScenarios = true;
		params.versioned = false;
		params.shouldApplySoftwareSystemStamp = true;
		params.addBundleTimestamp = true;  //setting this true to test timestamp added in generated bundle
		new IGProcessor().publishIG(params);

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
				for (Path path : dirStream) {
					File file = new File(path.toString());

					if (file.getName().toLowerCase().endsWith(".json")) {

						Map<?, ?> map = this.jsonMap(file);
						if (map == null) {
							System.out.println("# Unable to parse " + file.getName() + " as json");
						} else {

							// ensure "resourceType" exists
							if (map.containsKey(RESOURCE_TYPE)) {
								String parentResourceType = (String) map.get(RESOURCE_TYPE);
								// if Library, resource will produce a "Measure" and a "Group" in main bundled file:
								if (parentResourceType.equalsIgnoreCase(LIB_TYPE)) {
									resourceTypeMap.put(MEASURE_TYPE + "_" + map.get(ID), MEASURE_TYPE);
									resourceTypeMap.put(LIB_TYPE + "_" + map.get(ID), LIB_TYPE);
									resourceTypeMap.put(GROUP_TYPE + "_" + map.get(ID), GROUP_TYPE);
								} else if (parentResourceType.equalsIgnoreCase(BUNDLE_TYPE)) {
									// file is a bundle type, loop through resources in entry list, build up map of
									// <id, resourceType>:
									if (map.get(ENTRY) != null) {
										ArrayList<Map<?, ?>> entryList = (ArrayList<Map<?, ?>>) map.get(ENTRY);
										for (Map<?, ?> entry : entryList) {
											if (entry.containsKey(RESOURCE)) {
												Map<?, ?> resourceMap = (Map<?, ?>) entry.get(RESOURCE);
												resourceTypeMap.put(resourceMap.get(RESOURCE_TYPE) + "_" + resourceMap.get(ID),
														(String) resourceMap.get(RESOURCE_TYPE));
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// map out entries in the resulting single bundle file:
			Map<?, ?> bundledJson = this.jsonMap(new File(bundledFileResult));
			testTimestamp(bundledJson);
			Map<String, String> bundledJsonResourceTypes = new HashMap<>();
			ArrayList<Map<?, ?>> entryList = (ArrayList<Map<?, ?>>) bundledJson.get(ENTRY);
			for (Map<?, ?> entry : entryList) {
				Map<?, ?> resourceMap = (Map<?, ?>) entry.get(RESOURCE);
				bundledJsonResourceTypes.put(resourceMap.get(RESOURCE_TYPE) + "_" + resourceMap.get(ID), (String) resourceMap.get(RESOURCE_TYPE));
			}

			// compare mappings of <id, resourceType> to ensure all bundled correctly:
			assertTrue(mapsAreEqual(resourceTypeMap, bundledJsonResourceTypes));

		}
	}

	private void testTimestamp(Map<?, ?> bundledJson) throws ParseException {
		String timeStamp = (String)bundledJson.get("timestamp");
		assertNotNull(timeStamp);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals(dateFormat.format(dateFormat.parse(timeStamp)), dateFormat.format(new Date()));
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
