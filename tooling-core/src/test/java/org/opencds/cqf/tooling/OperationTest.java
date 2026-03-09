package org.opencds.cqf.tooling;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class OperationTest {

    // Concrete subclass for testing the abstract Operation
    static class ConcreteOperation extends Operation {
        @Override
        public void execute(String[] args) {
            // no-op
        }

        // Expose protected methods for testing
        public String testGetOutputPath() {
            return getOutputPath();
        }

        public void testSetOutputPath(String path) {
            setOutputPath(path);
        }
    }

    private Path tempDir;

    @AfterMethod
    public void cleanup() throws IOException {
        if (tempDir != null && tempDir.toFile().exists()) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void getOutputPath_initiallyNull() {
        ConcreteOperation op = new ConcreteOperation();
        assertNull(op.testGetOutputPath());
    }

    @Test
    public void setOutputPath_createsDirectoryAndSetsAbsolutePath() throws IOException {
        tempDir = Files.createTempDirectory("op-test");
        Path nested = tempDir.resolve("a").resolve("b");

        ConcreteOperation op = new ConcreteOperation();
        op.testSetOutputPath(nested.toString());

        assertNotNull(op.testGetOutputPath());
        assertTrue(new File(op.testGetOutputPath()).isDirectory());
        assertTrue(new File(op.testGetOutputPath()).isAbsolute());
    }

    @Test
    public void setOutputPath_existingDirectory_succeeds() throws IOException {
        tempDir = Files.createTempDirectory("op-test");

        ConcreteOperation op = new ConcreteOperation();
        op.testSetOutputPath(tempDir.toString());

        assertEquals(op.testGetOutputPath(), tempDir.toAbsolutePath().toString());
    }

    @Test
    public void setOutputPath_existingFile_throwsIllegalArgumentException() throws IOException {
        tempDir = Files.createTempDirectory("op-test");
        File file = tempDir.resolve("not-a-dir").toFile();
        file.createNewFile();

        ConcreteOperation op = new ConcreteOperation();
        assertThrows(IllegalArgumentException.class, () -> op.testSetOutputPath(file.getAbsolutePath()));
    }
}
