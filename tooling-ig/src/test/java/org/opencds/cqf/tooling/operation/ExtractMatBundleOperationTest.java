package org.opencds.cqf.tooling.operation;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.mat.ExtractMatBundle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExtractMatBundleOperationTest {

    private Operation operation;

    @BeforeMethod
    public void setUp() {
        operation = new ExecutableOperationAdapter(new ExtractMatBundle());
    }

    @Test
    public void testExecuteWithMissingBundleFile() {
        String[] args = {"-ExtractMatBundle"};

        try {
            operation.execute(args);
            fail("Expected InvalidOperationArgs was not thrown");
        } catch (InvalidOperationArgs e) {
            assertEquals("Missing required parameter: -ptb | -pathtobundle", e.getMessage());
        }
    }

    @Test
    public void testExecuteWithNonExistentBundleFile() {
        String[] args = {"-ExtractMatBundle", "-ptb=nonexistent.json"};

        try {
            operation.execute(args);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
            // The new operation delegates to IOUtils.readResource which throws RuntimeException for missing files
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testExecuteWithDirectoryPath() throws IOException {
        // The new operation does not support -dir; passing a directory path
        // causes IOUtils.readResource to return null (unknown encoding for directory path)
        // and the operation logs an error but does not throw
        File tempDir = Files.createTempDirectory("tempDir").toFile();
        tempDir.deleteOnExit();

        String[] args = {"-ExtractMatBundle", "-ptb=" + tempDir.getAbsolutePath()};

        // Should not throw; the operation handles null bundle gracefully via logging
        operation.execute(args);
    }

    @Test
    public void testExecuteWithNonJsonFile() throws IOException {
        // should return null from IOUtils.readResource for non-json/xml files
        File nonJsonFile = File.createTempFile("file", ".txt");
        nonJsonFile.deleteOnExit();
        String[] args = {"-ExtractMatBundle", "-ptb=" + nonJsonFile.getAbsolutePath()};
        // The new operation logs an error for null bundle but does not throw
        operation.execute(args);
    }

    @Test
    public void testExecuteWithNonXmlFile() throws IOException {
        File nonXmlFile = File.createTempFile("file", ".txt");
        nonXmlFile.deleteOnExit();
        String[] args = {"-ExtractMatBundle", "-ptb=" + nonXmlFile.getAbsolutePath()};

        // The new operation returns null from IOUtils.readResource for unknown encoding
        // and logs an error rather than throwing
        operation.execute(args);
    }

    @Test
    public void TestExtractMatBundleWithSingleBundle() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath =
                "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/CMS68FHIR-v0-0-004-FHIR-4-0-1.json";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Use a name without "bundles" so createDirectoryStructure creates bundles/input/ inside it
        File outputDir = Files.createTempDirectory("matExtract").toFile();
        outputDir.deleteOnExit();

        Operation op = new ExecutableOperationAdapter(new ExtractMatBundle());
        op.execute(
                new String[] {"-ExtractMatBundle", "-ptb=" + resourceUrl.getFile(), "-op=" + outputDir.getAbsolutePath()
                });

        // Output structure is outputDir/bundles/input/{resources/{library,measure},cql}
        File bundlesDir = new File(outputDir, "bundles");
        assertTrue(bundlesDir.exists(), "Expected bundles directory in output");
        File inputDir = new File(bundlesDir, "input");
        assertTrue(inputDir.exists(), "Expected input directory under bundles");
    }

    @Test
    public void TestExtractMatBundleWithSingleBundleDefaultOutput() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath =
                "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_small/CMS68FHIR-v0-0-004-FHIR-4-0-1.json";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Copy the bundle to a temp directory so we don't pollute test resources
        File tempDir = Files.createTempDirectory("bundleTest").toFile();
        tempDir.deleteOnExit();
        File sourceFile = new File(resourceUrl.getFile());
        File destFile = new File(tempDir, sourceFile.getName());
        Files.copy(sourceFile.toPath(), destFile.toPath());

        Operation op = new ExecutableOperationAdapter(new ExtractMatBundle());
        op.execute(new String[] {"-ExtractMatBundle", "-ptb=" + destFile.getAbsolutePath()});

        // When no -op is specified, output defaults to parent directory of the bundle
        File[] files = tempDir.listFiles();
        assertNotNull(files);
        // Should have the original bundle file plus extracted output directory
        assertTrue(files.length > 1, "Expected extracted resources alongside bundle file");
    }

    @Test
    public void TestExtractMatBundleWithDuplicateBundle() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String resourcePath =
                "org/opencds/cqf/tooling/operation/ExtractMatBundle/bundles_duplicate/CMS72FHIR4-v0-2-002-FHIR-4-0-1.json";
        URL resourceUrl = classLoader.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Use a name without "bundles" so createDirectoryStructure creates bundles/input/ inside it
        File outputDir = Files.createTempDirectory("matExtractDup").toFile();
        outputDir.deleteOnExit();

        Operation op = new ExecutableOperationAdapter(new ExtractMatBundle());
        op.execute(
                new String[] {"-ExtractMatBundle", "-ptb=" + resourceUrl.getFile(), "-op=" + outputDir.getAbsolutePath()
                });

        // Output structure is outputDir/bundles/input/{resources/{library,measure},cql}
        File bundlesDir = new File(outputDir, "bundles");
        assertTrue(bundlesDir.exists(), "Expected bundles directory in output");
        File inputDir = new File(bundlesDir, "input");
        assertTrue(inputDir.exists(), "Expected input directory under bundles");
    }
}
