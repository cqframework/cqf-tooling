package org.opencds.cqf.tooling.operation;

import java.io.File;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.bundle.BundleToTransaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BundleToTransactionTest {

    @Test
    public void testExecute_BundleDecomposition() {
        String projectPath = System.getProperty("user.dir");
        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;
        String relativePath = "target/test-output/bundleToTransactionResults";

        Operation operation = new ExecutableOperationAdapter(new BundleToTransaction());
        String[] args = new String[] {
            "-MakeTransaction",
            "-p=" + jsonFilePath,
            "-e=json",
            "-op=" + projectPath + File.separator + relativePath
        };
        operation.execute(args);

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

    @Test
    public void testExecute_CollectionToTransaction() {
        String projectPath = System.getProperty("user.dir");
        String relativeJsonPath = "src/main/resources/eRSDv2_specification_bundle.json";
        String jsonFilePath = projectPath + File.separator + relativeJsonPath;
        String relativePath = "target/test-output/bundleTransactionResults";

        Operation operation = new ExecutableOperationAdapter(new BundleToTransaction());
        String[] args = new String[] {
            "-MakeTransaction",
            "-p=" + jsonFilePath,
            "-e=json",
            "-op=" + projectPath + File.separator + relativePath
        };
        operation.execute(args);

        File resultDir = new File(projectPath + File.separator + relativePath);
        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        String expectedFile = "Bundle-rctc-release-1.2.4.0-Bundle-rctc.json";
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
