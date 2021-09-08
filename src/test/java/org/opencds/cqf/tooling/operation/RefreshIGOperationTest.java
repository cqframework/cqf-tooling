package org.opencds.cqf.tooling.operation;



import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.IniFile;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;

public class RefreshIGOperationTest {

	private final String ID = "id";
	private final String ENTRY = "entry";
	private final String RESOURCE = "resource";
	private final String RESOURCE_TYPE = "resourceType";
	private final String BUNDLE_TYPE = "Bundle";
	private final String LIB_TYPE = "Library";
	private final String MEASURE_TYPE = "Measure";

	private final String INI_LOC = "testfiles/refreshIG/ig.ini";

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
		if (specifiedFhirVersion == null || specifiedFhirVersion == "") {

			// TODO: Should point to global constant:
			specifiedFhirVersion = "4.0.1";
		}
		return specifiedFhirVersion;
	}

	/**
	 * This test breaks down refreshIG's process and can verify multiple bundles
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testBundledFiles() {
		RefreshIGOperation refreshIGOp = new RefreshIGOperation();

		// build ini object
		File iniFile = new File(INI_LOC);
		String iniFileLocation = iniFile.getPath();
		IniFile ini = new IniFile(iniFileLocation);

		String bundledFilesLocation = iniFile.getParent() + "\\bundles\\measure\\";

		String args[] = { "-RefreshIG", "-ini=" + INI_LOC, "-t", "-d", "-p" };

		// execute refresh using ARGS
		refreshIGOp.execute(args);

		// determine fhireContext for measure lookup
		FhirContext fhirContext = IGProcessor.getIgFhirContext(getFhirVersion(ini));

		// get list of measures resulting from execution
		Map<String, IBaseResource> measures = IOUtils.getMeasures(fhirContext);

		// loop through measure, verify each has all resources from multiple files
		// bundled into single file using id/resourceType as lookup:
		for (String measureName : measures.keySet()) {
			// location of single bundled file:
			final String bundledFileResult = bundledFilesLocation + measureName + "\\" + measureName + "-bundle.json";
			System.out.println(bundledFileResult);
			// multiple individual files in sub directory to loop through:
			File bundledFiles = new File(bundledFilesLocation + "\\" + measureName + "\\" + measureName + "-files");
			System.out.println(bundledFiles);
			// verify files even exist:
			assertTrue(bundledFiles.listFiles().length > 0);

			// loop through each file, determine resourceType and treat accordingly
			Map<String, String> resourceTypeMap = new HashMap<>();
			for (File file : bundledFiles.listFiles()) {
				if (!file.getName().toLowerCase().endsWith(".json")) {
					continue;
				}

				Map<?, ?> map = this.jsonMap(file);
				if (map == null) {
					System.out.println("# Unable to parse " + file.getName() + " as json");
					continue;
				}

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

	}
}
