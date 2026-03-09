package org.opencds.cqf.tooling.operation;

import static org.opencds.cqf.tooling.SoftwareSystemTest.separator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.dateroller.RollTestDates;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceDiscovery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DateRollerTest {

    private final String testFilePathRoot = "target" + separator + "test-output" + separator + "dateRoller";

    @BeforeMethod
    public void setup() throws Exception {
        ResourceDiscovery.clearDevicePaths();
        File originalDirectory = new File(Objects.requireNonNull(DateRollerTest.class.getResource("dateRoller"))
                .getPath());
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
        File dir = new File(testFilePathRoot);
        // Capture original file contents for comparison
        java.util.Map<String, String> originalContents = new java.util.HashMap<>();
        if (dir.isDirectory()) {
            for (File nextFile : dir.listFiles()) {
                originalContents.put(nextFile.getAbsolutePath(), IOUtils.getFileContent(nextFile));
            }
        }

        // Roll all dates using the new operation on the directory
        String[] args = {"-RollTestsDataDates", "-ptreq=" + testFilePathRoot, "-v=r4"};
        Operation op = new ExecutableOperationAdapter(new RollTestDates());
        op.execute(args);

        // Verify each file was updated
        if (dir.isDirectory()) {
            for (File nextFile : dir.listFiles()) {
                String fileToCheck = IOUtils.getFileContent(nextFile);
                if (null != fileToCheck) {
                    checkFileResults(fileToCheck);
                    compareItem1Size(originalContents.get(nextFile.getAbsolutePath()), fileToCheck);
                }
            }
        }
    }

    // if the original file contains a prefetch/item1 element, the size of this object is compared to the rolled file
    // item1
    private void compareItem1Size(String original, String rolled) {
        JsonObject originalJson = JsonParser.parseString(original).getAsJsonObject();
        JsonObject rolledJson = JsonParser.parseString(rolled).getAsJsonObject();

        String prefetch = "prefetch";
        String item1 = "item1";

        Assert.assertEquals(
                originalJson.entrySet().size(), rolledJson.entrySet().size());
        if (originalJson.has(prefetch) && originalJson.get(prefetch).isJsonObject()) {
            assertTrue(rolledJson.get(prefetch).isJsonObject());

            if (originalJson.get(prefetch).getAsJsonObject().get(item1).isJsonObject()) {
                assertEquals(
                        originalJson
                                .get(prefetch)
                                .getAsJsonObject()
                                .get(item1)
                                .getAsJsonObject()
                                .entrySet()
                                .size(),
                        rolledJson
                                .get(prefetch)
                                .getAsJsonObject()
                                .get(item1)
                                .getAsJsonObject()
                                .entrySet()
                                .size());
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
            content = Files.lines(Paths.get(filePath)).collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
