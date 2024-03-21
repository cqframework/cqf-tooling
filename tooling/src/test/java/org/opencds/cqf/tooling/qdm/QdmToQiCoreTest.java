package org.opencds.cqf.tooling.qdm;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class QdmToQiCoreTest {

    private final QdmToQiCore qdmToQiCore = new QdmToQiCore();

    @Test(enabled = false, description = "Hangs in CI because it makes external network requests")
    public void testExecute_HtmlFilesCreated() {
        String projectPath = System.getProperty("user.dir");
        String relativePath = "target/test-output/results";

        String[] args = new String[2];
        args[0] = "-QdmToQiCore";
        args[1] = projectPath + File.separator + relativePath;
        qdmToQiCore.execute(args);

        File resultDir = new File(args[1]);

        Assert.assertTrue(resultDir.exists() && resultDir.isDirectory(), "Result directory does not exist.");

        File[] htmlFiles = resultDir.listFiles((dir, name) -> name.endsWith(".html"));

        Assert.assertNotNull(htmlFiles, "HTML files array should not be null.");

        Assert.assertEquals(htmlFiles.length, 21, "Expected 21 HTML files, but found " + htmlFiles.length + ".");
    }

}