package org.opencds.cqf.tooling.operation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ExtractMatBundleOperationTest {

    private ExtractMatBundleOperation operation;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        operation = new ExtractMatBundleOperation();
    }

    @Test
    public void testExecuteWithMissingBundleFile() {
        String[] args = { "-ExtractMatBundle" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path to a bundle file is required", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonExistentBundleFile() {
        String[] args = { "-ExtractMatBundle", "nonexistent.json" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified for the bundle doesn't exist on your system.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonExistentDirectory() throws IOException {
        File nonexistentDir = tempFolder.newFile("nonexistent_directory");
        String[] args = { "-ExtractMatBundle", nonexistentDir.getAbsolutePath(), "-dir" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -dir is not a directory.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithEmptyDirectory() throws IOException {
        File emptyDir = tempFolder.newFolder("emptyDir");
        String[] args = { "-ExtractMatBundle", emptyDir.getAbsolutePath(), "-dir" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -dir is empty.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonJsonFile() throws IOException {
        File nonJsonFile = tempFolder.newFile("file.non_json");
        String[] args = { "-ExtractMatBundle", nonJsonFile.getAbsolutePath() };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path to a bundle file of type json or xml is required.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonXmlFile() throws IOException {
        File nonXmlFile = tempFolder.newFile("file.non_xml");
        String[] args = { "-ExtractMatBundle", nonXmlFile.getAbsolutePath() };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path to a bundle file of type json or xml is required.", e.getMessage());
        }
    }
    @Test
    public void testExecuteWithFileAndDirArg() throws IOException {
        File nonXmlFile = tempFolder.newFile("file.xml");
        String[] args = { "-ExtractMatBundle", nonXmlFile.getAbsolutePath(), "-dir" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -dir is not a directory.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonsense() throws IOException {
        File nonXmlFile = tempFolder.newFile("file.xml");
        String[] args = { "-ExtractMatBundle", nonXmlFile.getAbsolutePath(), "-nonsense" };

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid argument: -nonsense", e.getMessage());
        }
    }


    @Test
    public void TestExtractMatBundleWithDirectory() throws IOException {
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        // Use ClassLoader to load the resource
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/utilities/ecqm-content-r4-2021/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }


        File emptyDir = tempFolder.newFolder("bundles");
        // Create a new thread to execute the operation
        Thread executionThread = new Thread(new Runnable() {
            public void run() {
                o.execute(new String[] { "-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath() });
            }
        });

        // Start the thread
        executionThread.start();

        // Wait for the thread to finish
        try {
            executionThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        File[] files = emptyDir.listFiles();

        assertEquals(16, emptyDir.listFiles().length);
    }
}
