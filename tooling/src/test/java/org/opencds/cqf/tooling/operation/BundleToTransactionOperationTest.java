package org.opencds.cqf.tooling.operation;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class BundleToTransactionOperationTest {
    private final BundleToTransactionOperation bundleToTransactionOperation = new BundleToTransactionOperation();
    private final String PATH_ARGUMENT = "-p=";
    private final String OUTPUT_PATH_ARGUMENT = "-op=";
    private final String ENCODING_ARGUMENT = "-e=";

    @Test
    public void testExecute_BundleDecomposition() {
        String projectPath = System.getProperty("user.dir");

        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;

        String relativePath = "target/test-output/bundleTransactionResults";

        String[] args = new String[4];
        args[0] = "-MakeTransaction";
        args[1] = PATH_ARGUMENT + jsonFilePath;
        args[2] = ENCODING_ARGUMENT + "json";
        args[3] = OUTPUT_PATH_ARGUMENT + projectPath + File.separator + relativePath;
        bundleToTransactionOperation.execute(args);

        File resultDir = new File(projectPath + File.separator + relativePath);

        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        String expectedFile = "Bundle-libraryevaluationtest-bundle.json";

        File[] actualFiles = resultDir.listFiles((dir, name) -> name.endsWith(".json"));
        Assert.assertNotNull(actualFiles, "Bundle resource folder should not be null.");

        boolean found = false;
        for (File file : actualFiles) {
            if (file.getName().equals(expectedFile)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found, "Expected file not found: " + expectedFile);
    }
}