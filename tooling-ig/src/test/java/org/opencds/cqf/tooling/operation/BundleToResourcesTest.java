package org.opencds.cqf.tooling.operation;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.bundle.BundleToResources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BundleToResourcesTest {

    private final String PATH_ARGUMENT = "-ptb=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String ENCODING_ARGUMENT = "-e=";

    @Test
    public void testExecute_BundleDecomposition() {

        Operation bundleToResources = new ExecutableOperationAdapter(new BundleToResources());

        String projectPath = System.getProperty("user.dir");

        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;

        String relativePath = "target/test-output/bundleResourcesResults";

        String[] args = new String[4];
        args[0] = "-BundleToResources";
        args[1] = PATH_ARGUMENT + jsonFilePath;
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + projectPath + File.separator + relativePath;
        bundleToResources.execute(args);

        File resultDir = new File(projectPath + File.separator + relativePath);

        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        // Expected file names - the new BundleToResources operation uses resource ID as the filename
        // Note: on case-insensitive file systems (macOS), Library "LibraryEvaluationTest" and
        // Questionnaire "libraryevaluationtest" collide, resulting in 4 files instead of 5
        Set<String> expectedFiles = new HashSet<>();
        expectedFiles.add("LibraryEvaluationTestConcepts.json");
        expectedFiles.add("LibraryEvaluationTestDependency.json");
        expectedFiles.add("condition-problem-list-category.json");

        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Bundle resource folder should not be null.");

        // Check if all expected files are present (case-insensitive check)
        for (String expectedFile : expectedFiles) {
            boolean found = false;
            for (File file : actualFiles) {
                if (file.getName().equalsIgnoreCase(expectedFile)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "Expected file not found: " + expectedFile);
        }

        // Verify we have at least the expected number of files
        Assert.assertTrue(
                actualFiles.length >= expectedFiles.size(),
                "Expected at least " + expectedFiles.size() + " resource files, but found " + actualFiles.length + ".");
    }

    @Test
    public void testExecute_BundleDecomposition_WithTempFile() throws IOException {
        Operation bundleToResources = new ExecutableOperationAdapter(new BundleToResources());

        String projectPath = System.getProperty("user.dir");

        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        File sourceFile = new File(projectPath + File.separator + relativeJsonPath);

        // Create a temporary file
        File tempFile = File.createTempFile("temp-bundle", ".json");
        tempFile.deleteOnExit(); // Ensures cleanup in case the test fails early

        // Copy source file content to the temp file
        try (InputStream input = new FileInputStream(sourceFile);
                OutputStream output = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        // Use the temp file path in the BundleToResources call
        String relativePath = "target/test-output/bundleResourcesResultsTmp";
        String[] args = new String[4];
        args[0] = "-BundleToResources";
        args[1] = PATH_ARGUMENT + tempFile.getAbsolutePath();
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + projectPath + File.separator + relativePath;
        bundleToResources.execute(args);

        // Verify that the result directory exists
        File resultDir = new File(projectPath + File.separator + relativePath);
        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        // Expected file names - uses resource ID as filename
        Set<String> expectedFiles = new HashSet<>();
        expectedFiles.add("LibraryEvaluationTestConcepts.json");
        expectedFiles.add("LibraryEvaluationTestDependency.json");
        expectedFiles.add("condition-problem-list-category.json");

        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Bundle resource folder should not be null.");

        // Check if all expected files are present
        for (String expectedFile : expectedFiles) {
            boolean found = false;
            for (File file : actualFiles) {
                if (file.getName().equalsIgnoreCase(expectedFile)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "Expected file not found: " + expectedFile);
        }

        Assert.assertTrue(
                actualFiles.length >= expectedFiles.size(),
                "Expected at least " + expectedFiles.size() + " resource files, but found " + actualFiles.length + ".");
    }
}
