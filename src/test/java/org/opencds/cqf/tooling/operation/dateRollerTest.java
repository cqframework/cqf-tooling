package org.opencds.cqf.tooling.operation;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

import java.io.File;
import java.util.ArrayList;

import static org.opencds.cqf.tooling.CqfmSoftwareSystemTest.separator;

public class dateRollerTest {

    private File originalDirectory;
    private File testDirectory;
    private String testFilePathRoot = "target" + separator + "dateRoller" + separator + "filesToTest";
    private String dateRollOperationCall = "-RollTestsDataDates -v=r4 -ip=";

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        originalDirectory  = new File("test" + separator + "resources" + separator + "org.opencds.cqf.tooling"+ separator + "operation" + separator + "dateRoller" + separator + "originalFiles");
        testDirectory  = new File("target" + separator + "dateRoller" + separator + "filesToTest");
        if (testDirectory.exists()) {
            FileUtils.cleanDirectory(testDirectory);
        }
        FileUtils.copyDirectory(originalDirectory, testDirectory);
    }

    @Test
    public void testBundleInPrefetchRollDate(){
        try {
            setUp();
            String requestBundleFilePath = ""
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String callString = dateRollOperationCall + testFilePathRoot + separator + "preResourceBundle";
    }

}
