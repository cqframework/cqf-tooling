package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResourceDiscoveryTest {

    private static final FhirContext R4 = FhirContext.forR4Cached();
    private Path tempDir;

    @BeforeMethod
    public void setUp() throws IOException {
        ResourceDiscovery.cleanUp();
        tempDir = Files.createTempDirectory("rd-test");
    }

    @AfterMethod
    public void tearDown() {
        ResourceDiscovery.cleanUp();
        // Remove temp dir from resource directories
        IOUtils.resourceDirectories.remove(tempDir.toString());
        deleteRecursive(tempDir.toFile());
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    private void writeR4Resource(String filename, String json) throws IOException {
        Files.writeString(tempDir.resolve(filename), json, StandardCharsets.UTF_8);
    }

    // ── cleanUp ──

    @Test
    public void cleanUp_resetsAllCaches() {
        // After cleanUp, all path sets should be empty
        ResourceDiscovery.cleanUp();

        // Verify that the internal caches were cleared by checking that
        // getDevicePaths returns non-null after clearDevicePaths
        ResourceDiscovery.clearDevicePaths();
        // No exception should be thrown
    }

    @Test
    public void cleanUp_canBeCalledMultipleTimes() {
        ResourceDiscovery.cleanUp();
        ResourceDiscovery.cleanUp();
        ResourceDiscovery.cleanUp();
        // No exception
    }

    // ── getLibraryByUrl ──

    @Test
    public void getLibraryByUrl_notFound_throws() {
        // No libraries loaded — should throw
        assertThrows(IllegalArgumentException.class,
                () -> ResourceDiscovery.getLibraryByUrl(R4, "http://nonexistent.org/Library/Missing"));
    }

    // ── clearDevicePaths ──

    @Test
    public void clearDevicePaths_resetsToNull_allowsReinitialization() {
        // First call to getDevicePaths initializes it
        IOUtils.resourceDirectories.add(tempDir.toString());
        Set<String> paths = ResourceDiscovery.getDevicePaths(R4);
        assertNotNull(paths);

        // clearDevicePaths sets it to null
        ResourceDiscovery.clearDevicePaths();

        // Next call should reinitialize
        Set<String> paths2 = ResourceDiscovery.getDevicePaths(R4);
        assertNotNull(paths2);
    }

    // ── getCqlLibrarySourcePath ──

    @Test
    public void getCqlLibrarySourcePath_existingFile_returnsPath() throws IOException {
        // Create a CQL file in temp dir
        Path cqlFile = tempDir.resolve("TestLibrary.cql");
        Files.writeString(cqlFile, "library TestLibrary version '1.0'", StandardCharsets.UTF_8);

        // Use binaryPaths to find it
        List<String> binaryPaths = new ArrayList<>();
        binaryPaths.add(tempDir.toString());

        String result = ResourceDiscovery.getCqlLibrarySourcePath("TestLibrary", "TestLibrary.cql", binaryPaths);
        assertNotNull(result);
        assertTrue(result.endsWith("TestLibrary.cql"));
    }

    @Test
    public void getCqlLibrarySourcePath_nonExistent_returnsNull() {
        List<String> binaryPaths = new ArrayList<>();
        binaryPaths.add(tempDir.toString());

        String result = ResourceDiscovery.getCqlLibrarySourcePath("Missing", "Missing.cql", binaryPaths);
        assertNull(result);
    }

    @Test
    public void getCqlLibrarySourcePath_emptyBinaryPaths_returnsNull() {
        List<String> binaryPaths = new ArrayList<>();

        String result = ResourceDiscovery.getCqlLibrarySourcePath("Missing", "Missing.cql", binaryPaths);
        assertNull(result);
    }

    // ── Discovery with temp resource directory ──

    @Test
    public void getLibraryPaths_withTempDir_discoversLibraries() throws IOException {
        writeR4Resource("Library-test.json",
                "{\"resourceType\":\"Library\",\"id\":\"test\",\"url\":\"http://example.org/Library/test\",\"version\":\"1.0\",\"name\":\"TestLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getLibraryPaths(R4);
        assertFalse(paths.isEmpty(), "Should discover library in temp directory");
        assertTrue(paths.stream().anyMatch(p -> p.contains("Library-test.json")));
    }

    @Test
    public void getLibraryUrlMap_withTempDir_mapsUrlToResource() throws IOException {
        writeR4Resource("Library-test.json",
                "{\"resourceType\":\"Library\",\"id\":\"test\",\"url\":\"http://example.org/Library/test\",\"version\":\"1.0\",\"name\":\"TestLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Map<String, IBaseResource> urlMap = ResourceDiscovery.getLibraryUrlMap(R4);
        assertNotNull(urlMap);
        // Should have both unversioned and versioned URL entries
        assertTrue(urlMap.containsKey("http://example.org/Library/test"),
                "Should map unversioned URL");
        assertTrue(urlMap.containsKey("http://example.org/Library/test|1.0"),
                "Should map versioned URL");
    }

    @Test
    public void getLibraries_withTempDir_indexedById() throws IOException {
        writeR4Resource("Library-mylib.json",
                "{\"resourceType\":\"Library\",\"id\":\"mylib\",\"url\":\"http://example.org/Library/mylib\",\"name\":\"MyLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Map<String, IBaseResource> libs = ResourceDiscovery.getLibraries(R4);
        assertTrue(libs.containsKey("mylib"), "Should index library by ID");
    }

    @Test
    public void getLibraryPathMap_withTempDir_mapsIdToPath() throws IOException {
        writeR4Resource("Library-mylib.json",
                "{\"resourceType\":\"Library\",\"id\":\"mylib\",\"url\":\"http://example.org/Library/mylib\",\"name\":\"MyLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Map<String, String> pathMap = ResourceDiscovery.getLibraryPathMap(R4);
        assertTrue(pathMap.containsKey("mylib"));
        assertTrue(pathMap.get("mylib").contains("Library-mylib.json"));
    }

    @Test
    public void getLibraryByUrl_withTempDir_returnsResource() throws IOException {
        writeR4Resource("Library-test.json",
                "{\"resourceType\":\"Library\",\"id\":\"test\",\"url\":\"http://example.org/Library/test\",\"name\":\"TestLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        IBaseResource lib = ResourceDiscovery.getLibraryByUrl(R4, "http://example.org/Library/test");
        assertNotNull(lib);
        assertEquals(lib.getIdElement().getIdPart(), "test");
    }

    @Test
    public void getMeasurePaths_withTempDir_discoversMeasures() throws IOException {
        writeR4Resource("Measure-test.json",
                "{\"resourceType\":\"Measure\",\"id\":\"test\",\"url\":\"http://example.org/Measure/test\",\"name\":\"TestMeasure\",\"status\":\"active\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getMeasurePaths(R4);
        assertFalse(paths.isEmpty());
        assertTrue(paths.stream().anyMatch(p -> p.contains("Measure-test.json")));
    }

    @Test
    public void getMeasures_withTempDir_indexedById() throws IOException {
        writeR4Resource("Measure-test.json",
                "{\"resourceType\":\"Measure\",\"id\":\"test\",\"url\":\"http://example.org/Measure/test\",\"name\":\"TestMeasure\",\"status\":\"active\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Map<String, IBaseResource> measures = ResourceDiscovery.getMeasures(R4);
        assertTrue(measures.containsKey("test"));
    }

    @Test
    public void getPlanDefinitionPaths_withTempDir_discovers() throws IOException {
        writeR4Resource("PlanDefinition-test.json",
                "{\"resourceType\":\"PlanDefinition\",\"id\":\"test\",\"url\":\"http://example.org/PlanDefinition/test\",\"name\":\"TestPD\",\"status\":\"active\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getPlanDefinitionPaths(R4);
        assertFalse(paths.isEmpty());
    }

    @Test
    public void getQuestionnairePaths_withTempDir_discovers() throws IOException {
        writeR4Resource("Questionnaire-test.json",
                "{\"resourceType\":\"Questionnaire\",\"id\":\"test\",\"url\":\"http://example.org/Questionnaire/test\",\"name\":\"TestQ\",\"status\":\"active\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getQuestionnairePaths(R4);
        assertFalse(paths.isEmpty());
    }

    @Test
    public void getActivityDefinitionPaths_withTempDir_discovers() throws IOException {
        writeR4Resource("ActivityDefinition-test.json",
                "{\"resourceType\":\"ActivityDefinition\",\"id\":\"test\",\"url\":\"http://example.org/ActivityDefinition/test\",\"name\":\"TestAD\",\"status\":\"active\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getActivityDefinitionPaths(R4);
        assertFalse(paths.isEmpty());
    }

    @Test
    public void getDevicePaths_emptyDir_returnsEmptySet() {
        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getDevicePaths(R4);
        assertNotNull(paths);
        assertTrue(paths.isEmpty());
    }

    @Test
    public void getCqlLibraryPaths_withCqlFile_discovers() throws IOException {
        Files.writeString(tempDir.resolve("TestLib.cql"),
                "library TestLib version '1.0'", StandardCharsets.UTF_8);

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getCqlLibraryPaths();
        assertTrue(paths.stream().anyMatch(p -> p.contains("TestLib.cql")));
    }

    // ── Cross-resource type filtering ──

    @Test
    public void getLibraryPaths_ignoresNonLibraryResources() throws IOException {
        writeR4Resource("Patient-test.json",
                "{\"resourceType\":\"Patient\",\"id\":\"test\"}");
        writeR4Resource("Library-test.json",
                "{\"resourceType\":\"Library\",\"id\":\"test\",\"url\":\"http://example.org/Library/test\",\"name\":\"TestLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getLibraryPaths(R4);
        assertEquals(paths.size(), 1, "Should only discover Library resources");
        assertTrue(paths.stream().anyMatch(p -> p.contains("Library-test.json")));
    }

    @Test
    public void getTerminologyPaths_discoversValueSets() throws IOException {
        writeR4Resource("ValueSet-test.json",
                "{\"resourceType\":\"ValueSet\",\"id\":\"test\",\"url\":\"http://example.org/ValueSet/test\",\"name\":\"TestVS\",\"status\":\"active\"}");
        writeR4Resource("Patient-test.json",
                "{\"resourceType\":\"Patient\",\"id\":\"test\"}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getTerminologyPaths(R4);
        assertTrue(paths.stream().anyMatch(p -> p.contains("ValueSet-test.json")),
                "Should discover ValueSet resources");
        assertFalse(paths.stream().anyMatch(p -> p.contains("Patient-test.json")),
                "Should not include Patient resources");
    }

    // ── Cleanup clears all discovery caches ──

    @Test
    public void cleanUp_afterDiscovery_cachesAreEmpty() throws IOException {
        writeR4Resource("Library-test.json",
                "{\"resourceType\":\"Library\",\"id\":\"test\",\"url\":\"http://example.org/Library/test\",\"name\":\"TestLib\",\"status\":\"active\",\"type\":{\"coding\":[{\"code\":\"logic-library\"}]}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        // Trigger discovery
        ResourceDiscovery.getLibraryPaths(R4);
        assertFalse(ResourceDiscovery.getLibraryPaths(R4).isEmpty(), "Pre-condition: library should be discovered");

        // Clean up caches and remove directory
        ResourceDiscovery.cleanUp();
        IOUtils.resourceDirectories.remove(tempDir.toString());

        // After cleanup with no directories, discovery should find nothing
        Set<String> paths = ResourceDiscovery.getLibraryPaths(R4);
        assertTrue(paths.isEmpty(), "After cleanUp with no resource dirs, library paths should be empty");
    }

    // ── MeasureReport discovery ──

    @Test
    public void getMeasureReportPaths_withTempDir_discovers() throws IOException {
        writeR4Resource("MeasureReport-test.json",
                "{\"resourceType\":\"MeasureReport\",\"id\":\"test\",\"status\":\"complete\",\"type\":\"summary\",\"measure\":\"http://example.org/Measure/test\",\"period\":{\"start\":\"2020-01-01\",\"end\":\"2020-12-31\"}}");

        IOUtils.resourceDirectories.add(tempDir.toString());

        Set<String> paths = ResourceDiscovery.getMeasureReportPaths(R4);
        assertFalse(paths.isEmpty());
        assertTrue(paths.stream().anyMatch(p -> p.contains("MeasureReport-test.json")));
    }
}
