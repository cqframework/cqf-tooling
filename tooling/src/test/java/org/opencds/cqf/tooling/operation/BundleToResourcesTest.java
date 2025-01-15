package org.opencds.cqf.tooling.operation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class BundleToResourcesTest {

    private final String PATH_ARGUMENT = "-p=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String ENCODING_ARGUMENT = "-e=";

    @Test
    public void testExecute_BundleDecomposition() {

        BundleToResources bundleToResources = new BundleToResources();

        String projectPath = System.getProperty("user.dir");

        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;

        String relativePath = "target/test-output/bundleResourcesResults";

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

    @Test
    public void testExecute_BundleDecomposition_DeleteBundles() throws IOException {
        BundleToResources bundleToResources = new BundleToResources();

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
        String relativePath = "target/test-output/bundleResourcesResults";
        String[] args = new String[5];
        args[0] = "-BundleToResources";
        args[1] = PATH_ARGUMENT + tempFile.getAbsolutePath();
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + projectPath + File.separator + relativePath;
        args[4] = "-db=true";
        bundleToResources.execute(args);

        // Verify that the result directory exists
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

        Assert.assertEquals(actualFiles.length, expectedFiles.size(), "Expected " + expectedFiles.size() + " resource files, but found " + actualFiles.length + ".");

        // Verify the temp file is deleted
        Assert.assertFalse(tempFile.exists(), "Temporary bundle file was not deleted.");
    }

}
