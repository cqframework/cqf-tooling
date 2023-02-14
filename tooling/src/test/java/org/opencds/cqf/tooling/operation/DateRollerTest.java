package org.opencds.cqf.tooling.operation;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import static org.opencds.cqf.tooling.CqfmSoftwareSystemTest.separator;
import static org.testng.Assert.assertTrue;

public class DateRollerTest {

    private File originalDirectory;
    private File testRootDirectory;
    private String testFilePathRoot = "target" + separator + "dateRoller";

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        originalDirectory = new File(DateRollerTest.class.getResource("/org/opencds/cqf/tooling/operation/dateRoller").getPath());//new File("test" + separator + "resources" + separator + "org.opencds.cqf.tooling" + separator + "operation" + separator + "dateRoller");
        testRootDirectory = new File(testFilePathRoot);
        if (testRootDirectory.exists()) {
            FileUtils.cleanDirectory(testRootDirectory);
        } else {
            testRootDirectory.mkdir();
        }
        FileUtils.copyDirectory(originalDirectory, testRootDirectory);
    }

    @Test
    public void testRollDirectory() {
        // this is expecting just files in this directory, although the DateRoller can handle nested directories
        try {
            setUp();
            String args[] = {"-RollTestsDataDates", "-v=r4", "-ip=" + testFilePathRoot};
            File file = new File(testFilePathRoot);
            if (file.isDirectory()) {
                for (File nextFile : file.listFiles()) {
                    String filePath = nextFile.getAbsolutePath();
                    testRollSingleFile(filePath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void testRollSingleFile(String filePath) {
        try {
            setUp();
            String args[] = {"-RollTestsDataDates", "-v=r4", "-ip=" + filePath};
            new DataDateRollerOperation().execute(args);
            File fileRolled = new File(filePath);
            String fileToCheck = IOUtils.getFileContent(fileRolled);
            if (null != fileToCheck) {
                checkFileResults(fileToCheck);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFileResults(String fileToCheck) {
        assertTrue(fileToCheck.contains("2022-04-28") == false, "At least one date was not updated.");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateToday = LocalDate.now();
        String today = dtf.format(dateToday);
        assertTrue(fileToCheck.contains(today) == true);
    }

    private String getFileAsString(String filePath) {
        String content = null;
        try {
            content = Files.lines(Paths.get(filePath))
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
