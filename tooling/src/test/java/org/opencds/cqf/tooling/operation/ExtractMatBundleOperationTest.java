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

        operation.execute(args);
        File[] files = emptyDir.listFiles();
        assertEquals(0, files.length);
    }

    @Test
    public void testExecuteWithNonJsonFile() throws IOException {
        //should output 0 files as it hits error
        File nonJsonFile = tempFolder.newFile("file.non_json");
        String[] args = {"-ExtractMatBundle", nonJsonFile.getAbsolutePath()};
        operation.execute(args);
        File[] files = nonJsonFile.listFiles();
        assertNull(files);
    }

    @Test
    public void testExecuteWithNonXmlFile() throws IOException {
        File nonXmlFile = tempFolder.newFile("file.non_xml");
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
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/";
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
        tempFolder.newFolder("emptyDir/bundle/");
        File file = tempFolder.newFile("emptyDir/bundle/missing.xml");
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

        File emptyDir = tempFolder.newFolder("bundles");
        operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});
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

        File emptyDir = tempFolder.newFolder("bundles");
        operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});
        File[] files = emptyDir.listFiles();
        assertNotNull(files);
        assertEquals(41, files.length);
    }

    @Test
    public void TestExtractMatBundleWithDuplicateBundleXmlJson() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath = "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_duplicate/";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        File emptyDir = tempFolder.newFolder("bundles");

        operation.execute(new String[]{"-ExtractMATBundle", resourceUrl.getFile(), "-dir", "-op=" + emptyDir.getAbsolutePath()});

        File[] files = emptyDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 16);
    }
}
