package org.opencds.cqf.tooling.operation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BundleToResourcesTest {
    private final BundleToResources bundleToResources = new BundleToResources();
    private final String PATH_ARGUMENT = "-p=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String ENCODING_ARGUMENT = "-e=";

    @Test
    public void testExecute_BundleDecomposition() {
        String projectPath = System.getProperty("user.dir");

        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;

        String relativePath = "tooling-cli/bundleResourcesResults";

        String[] args = new String[4];
        args[0] = "-BundleToResources";
        args[1] = PATH_ARGUMENT + jsonFilePath;
        args[2] = ENCODING_ARGUMENT+ "json";
        args[3] = OUTPUT_PATH_ARGUMENT + projectPath + File.separator + relativePath;
        bundleToResources.execute(args);

        File resultDir = new File(projectPath + File.separator + relativePath);

        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        // Expected file names
        Set<String> expectedFiles = new HashSet<>();
        expectedFiles.add("Library-LibraryEvaluationTest.json");
        expectedFiles.add("Library-LibraryEvaluationTestConcepts.json");
        expectedFiles.add("Library-LibraryEvaluationTestDependency.json");
        expectedFiles.add("Questionnaire-libraryevaluationtest.json");
        expectedFiles.add("ValueSet-condition-problem-list-category.json");

        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Bundle resource folder should not be null.");

        // Check if all expected files are present
        for (String expectedFile : expectedFiles) {
            boolean found = false;
            for (File file : actualFiles) {
                if (file.getName().equals(expectedFile)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "Expected file not found: " + expectedFile);
        }

        Assert.assertEquals(actualFiles.length, expectedFiles.size(), "Expected " + expectedFiles.size() + " resources files, but found " + actualFiles.length + ".");
    }
}
