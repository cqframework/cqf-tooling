package org.opencds.cqf.tooling.operation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

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
        String[] args = {"-ExtractMatBundle"};

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path to a bundle file is required", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonExistentBundleFile() {
        String[] args = {"-ExtractMatBundle", "nonexistent.json"};

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
        String[] args = {"-ExtractMatBundle", nonexistentDir.getAbsolutePath(), "-dir"};

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
        String[] args = {"-ExtractMatBundle", emptyDir.getAbsolutePath(), "-dir"};

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
        String[] args = {"-ExtractMatBundle", nonJsonFile.getAbsolutePath()};

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
        String[] args = {"-ExtractMatBundle", nonXmlFile.getAbsolutePath()};

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
        String[] args = {"-ExtractMatBundle", nonXmlFile.getAbsolutePath(), "-dir"};

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
        String[] args = {"-ExtractMatBundle", nonXmlFile.getAbsolutePath(), "-nonsense"};

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid argument: -nonsense", e.getMessage());
        }
    }

    @Test
    public void TestExtractMatBundleWithInvalidOutputDirectory() throws IOException {
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/utilities/ecqm-content-r4-2021/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        File emptyDir = tempFolder.newFolder("emptyDir");
        try {
            operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("When specifying the output folder using -op for ExtractMatBundle, the output directory name must contain the word 'bundle' (all lowercase.)", e.getMessage());
        }
    }

    @Test
    public void TestExtractMatBundleWithMissingOutputDirectory() throws IOException {
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/utilities/ecqm-content-r4-2021/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        try {
            operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + "this/bundle/directory/is/missing"});
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified for the output folder doesn't exist on your system.", e.getMessage());
        }
    }

    @Test
    public void TestExtractMatBundleWithMissingOutputDirectoryAsAFile() throws IOException {
        tempFolder.newFolder("emptyDir/bundle/");
        File file = tempFolder.newFile("emptyDir/bundle/missing.xml");
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/utilities/ecqm-content-r4-2021/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        try {
            operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + file.getAbsolutePath()});
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -op is not a directory.", e.getMessage());
        }
    }

    @Test
    public void TestExtractMatBundleWithDirectory() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/utilities/ecqm-content-r4-2021/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        File emptyDir = tempFolder.newFolder("bundles");
        Thread executionThread = new Thread(new Runnable() {
            public void run() {
                operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});
            }
        });

        executionThread.start();
        try {
            executionThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        File[] files = emptyDir.listFiles();
        assertNotNull(files);
        assertEquals(16, files.length);
    }
}
