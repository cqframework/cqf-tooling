package org.opencds.cqf.tooling.operation;

import static org.opencds.cqf.tooling.SoftwareSystemTest.separator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DateRollerTest {

    private final String testFilePathRoot = "target" + separator + "test-output" + separator + "dateRoller";

    @BeforeMethod
    public void setup() throws Exception {
        IOUtils.clearDevicePaths();
        File originalDirectory = new File(Objects.requireNonNull(DateRollerTest.class.getResource("dateRoller")).getPath());
        File testRootDirectory = new File(testFilePathRoot);
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
            String[] args = {"-RollTestsDataDates", "-v=r4", "-ip=" + filePath};
            String fileContentOriginal = IOUtils.getFileContent(new File(filePath));
            new DataDateRollerOperation().execute(args);
            File fileRolled = new File(filePath);
            String fileToCheck = IOUtils.getFileContent(fileRolled);
            if (null != fileToCheck) {
                checkFileResults(fileToCheck);
                compareItem1Size(fileContentOriginal, fileToCheck);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //if the original file contains a prefetch/item1 element, the size of this object is compared to the rolled file item1
    private void compareItem1Size(String original, String rolled) {
        JsonObject originalJson = JsonParser.parseString(original).getAsJsonObject();
        JsonObject rolledJson = JsonParser.parseString(rolled).getAsJsonObject();

        String prefetch = "prefetch";
        String item1 = "item1";

        Assert.assertEquals(originalJson.entrySet().size(), rolledJson.entrySet().size());
        if (originalJson.has(prefetch) && originalJson.get(prefetch).isJsonObject()) {
            assertTrue (rolledJson.get(prefetch).isJsonObject());

            if (originalJson.get(prefetch).getAsJsonObject().get(item1).isJsonObject()) {
                assertEquals (
                        originalJson.get(prefetch).getAsJsonObject().get(item1).getAsJsonObject().entrySet().size(),
                        rolledJson.get(prefetch).getAsJsonObject().get(item1).getAsJsonObject().entrySet().size()
                );
            }
        }
    }

    private void checkFileResults(String fileToCheck) {
        assertFalse(fileToCheck.contains("2022-04-28"), "At least one date was not updated.");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateToday = LocalDate.now();
        String today = dtf.format(dateToday);
        assertTrue(fileToCheck.contains(today));
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
