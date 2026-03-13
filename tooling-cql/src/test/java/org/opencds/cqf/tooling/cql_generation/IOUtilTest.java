package org.opencds.cqf.tooling.cql_generation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IOUtilTest {

    private Path tempDir;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("ioutil-test");
    }

    @AfterMethod
    public void tearDown() {
        deleteRecursive(tempDir.toFile());
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    // ── readFile ──

    @Test
    public void readFile_byPath_returnsContent() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world", StandardCharsets.UTF_8);

        String result = IOUtil.readFile(file.toString());
        assertEquals(result.trim(), "hello world");
    }

    @Test
    public void readFile_byFile_returnsContent() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "content here", StandardCharsets.UTF_8);

        String result = IOUtil.readFile(file.toFile());
        assertEquals(result.trim(), "content here");
    }

    @Test
    public void readFile_utf8Content_preservedCorrectly() throws IOException {
        // Intent: readFile should handle UTF-8 characters (not platform default charset)
        Path file = tempDir.resolve("utf8.txt");
        Files.writeString(file, "café résumé naïve", StandardCharsets.UTF_8);

        String result = IOUtil.readFile(file.toString());
        assertTrue(result.contains("café"), "UTF-8 characters should be preserved");
        assertTrue(result.contains("résumé"));
        assertTrue(result.contains("naïve"));
    }

    @Test
    public void readFile_nonExistent_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> IOUtil.readFile("/nonexistent/path/file.txt"));
    }

    @Test
    public void readFile_emptyFile_returnsEmpty() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        String result = IOUtil.readFile(file.toString());
        assertTrue(result.isEmpty() || result.equals("\n") || result.isBlank(),
                "Empty file should return empty or near-empty string");
    }

    // ── writeToFile ──

    @Test
    public void writeToFile_createsFileWithContent() throws IOException {
        File file = tempDir.resolve("output.txt").toFile();
        IOUtil.writeToFile(file, "written content");

        assertTrue(file.exists());
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(content, "written content");
    }

    @Test
    public void writeToFile_overwritesExistingFile() throws IOException {
        File file = tempDir.resolve("overwrite.txt").toFile();
        Files.writeString(file.toPath(), "old content", StandardCharsets.UTF_8);

        IOUtil.writeToFile(file, "new content");

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(content, "new content");
    }

    @Test
    public void writeToFile_utf8Content_preserved() throws IOException {
        File file = tempDir.resolve("utf8out.txt").toFile();
        IOUtil.writeToFile(file, "café résumé naïve");

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(content, "café résumé naïve");
    }

    @Test
    public void writeToFile_byPath_createsParentDirs() throws IOException {
        String filePath = tempDir.resolve("sub/dir/output.txt").toString();

        IOUtil.writeToFile(filePath, "nested content");

        File file = new File(filePath);
        assertTrue(file.exists(), "File should be created including parent directories");
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertEquals(content, "nested content");
    }

    // ── roundtrip ──

    @Test
    public void writeAndRead_roundtrip_preservesContent() throws IOException {
        File file = tempDir.resolve("roundtrip.txt").toFile();
        String original = "line 1\nline 2\nline 3";

        IOUtil.writeToFile(file, original);
        String read = IOUtil.readFile(file);

        assertEquals(read.trim(), original.trim());
    }
}
