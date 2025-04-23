package org.opencds.cqf.tooling.operation;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.testng.Assert.*;

public class ExtractMatBundleOperationTest {

    private ExtractMatBundleOperation operation;

    @BeforeMethod
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

        operation.execute(args);
        File[] files = emptyDir.listFiles();
        assertEquals(0, files.length);
    }

    @Test
    public void testExecuteWithNonJsonFile() throws IOException {
        //should output 0 files as it hits error
        File nonJsonFile = File.createTempFile("file","non_json");
        String[] args = {"-ExtractMatBundle", nonJsonFile.getAbsolutePath()};
        operation.execute(args);
        File[] files = nonJsonFile.listFiles();
        assertNull(files);
    }

    @Test
    public void testExecuteWithNonXmlFile() throws IOException {
        File nonXmlFile = File.createTempFile("file", "non_xml");
        String err = "The path to a bundle file of type json or xml is required." + "\n" + nonXmlFile.getAbsolutePath();
        String[] args = {"-ExtractMatBundle", nonXmlFile.getAbsolutePath()};

        try {
            operation.execute(args);
        } catch (IllegalArgumentException e) {
            assertEquals(err, e.getMessage());
        }
    }

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
        assertEquals(17, files.length);
    }

    /**
     * In response to issue https://github.com/cqframework/cqf-tooling/issues/537
     * The ExtractMATBundle process defaults to the location of the bundle file
     * when no IG folder structure exists (no bundle folder found.)
     *
     * @throws IOException
     */
    @Test
    public void TestExtractMatBundleWithNonIGStructuredDirectory() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Create a temporary directory named "noIG" and does not include the name bundles
        File tempDir = Files.createTempDirectory("noIG").toFile();
        tempDir.deleteOnExit();

        // Copy files from resourcePath to the temporary directory
        File sourceDir = new File(resourceUrl.getFile());
        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Resource path is not a directory: " + resourcePath);
        }

        //Copy our test files to the temp folder so the source location of the bundle is the temp folder
        for (File file : sourceDir.listFiles()) {
            File destFile = new File(tempDir, file.getName());
            Files.copy(file.toPath(), destFile.toPath());
        }

        Thread executionThread = new Thread(() ->
                operation.execute(new String[]{"-ExtractMATBundle", tempDir.getAbsolutePath(), "-dir"})
        );

        executionThread.start();
        try {
            executionThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Validate results in the temporary directory
        File[] files = tempDir.listFiles();
        assertNotNull(files);

        //Directory should now include 1 input folder, original json bundles (6), and extracted files (16)
        assertEquals(23, files.length);
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
        assertEquals(42, files.length);
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

        operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});

        File[] files = emptyDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 17);
    }
}
