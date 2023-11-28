package org.opencds.cqf.tooling.operation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExtractMatBundleOperationTest {

    private ExtractMatBundleOperation operation;

    @BeforeClass
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

        File newFile = File.createTempFile("temp", "json");

        String[] args = {"-ExtractMatBundle", newFile.getAbsolutePath(), "-dir"};

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -dir is not a directory.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithEmptyDirectory() throws IOException {
        File emptyDir = Files.createTempDirectory("emptyDir").toFile();
        emptyDir.deleteOnExit();
        String[] args = {"-ExtractMatBundle", emptyDir.getAbsolutePath(), "-dir"};

        try {
            operation.execute(args);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The path specified with -dir is empty.", e.getMessage());
        }
    }

//    @Test
//    public void testExecuteWithNonJsonFile() throws IOException {
//        File nonJsonFile = tempFolder.newFile("file.non_json");
//        String err = "The path to a bundle file of type json or xml is required." + "\n" + nonJsonFile.getAbsolutePath();
//        String[] args = {"-ExtractMatBundle", nonJsonFile.getAbsolutePath()};
//
//        try {
//            operation.execute(args);
//            fail("Expected IllegalArgumentException was not thrown");
//        } catch (IllegalArgumentException e) {
//            assertEquals(err, e.getMessage());
//        }
//    }
//
//    @Test
//    public void testExecuteWithNonXmlFile() throws IOException {
//        File nonXmlFile = tempFolder.newFile("file.non_xml");
//        String err = "The path to a bundle file of type json or xml is required." + "\n" + nonXmlFile.getAbsolutePath();
//        String[] args = {"-ExtractMatBundle", nonXmlFile.getAbsolutePath()};
//
//        try {
//            operation.execute(args);
//        } catch (IllegalArgumentException e) {
//            assertEquals(err, e.getMessage());
//        }
//    }

    @Test
    public void testExecuteWithFileAndDirArg() throws IOException {
        File nonXmlFile = File.createTempFile("file","xml");
        nonXmlFile.deleteOnExit();

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
        File nonXmlFile = File.createTempFile("file", "xml");
        nonXmlFile.deleteOnExit();

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
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        File emptyDir = Files.createTempDirectory("emptyDir").toFile();
        emptyDir.deleteOnExit();

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
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
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

        File emptyDir = Files.createTempDirectory("emptyDir").toFile();
        emptyDir.deleteOnExit();

        File file = File.createTempFile("missing", "xml");
        file.deleteOnExit();
        ExtractMatBundleOperation o = new ExtractMatBundleOperation();
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
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
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        File emptyDir = Files.createTempDirectory("bundles").toFile();
        emptyDir.deleteOnExit();

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

    @Test
    public void TestExtractMatBundleWithDirectoryAndSubDirectories() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_mixed/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        File emptyDir = Files.createTempDirectory("bundles").toFile();
        emptyDir.deleteOnExit();

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
        assertTrue(files.length >= 40);
    }

    @Test
    public void TestExtractMatBundleWithDuplicateBundleXmlJson() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_duplicate/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        File emptyDir = Files.createTempDirectory("bundles").toFile();
        emptyDir.deleteOnExit();

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
        assertEquals(8, files.length);
    }
}
