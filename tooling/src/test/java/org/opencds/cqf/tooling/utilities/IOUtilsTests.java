package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.testng.annotations.Test;

public class IOUtilsTests {

    @Test
    public void TestConcatFilePaths() {
        String basePath = "basePath";
        String result = IOUtils.concatFilePath(basePath, "input", "resources", "library");
        String expected = basePath + File.separator + "input" + File.separator + "resources" + File.separator + "library";
        assertEquals(result, expected);
    }
}
