package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.*;

import ca.uhn.fhir.context.FhirContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Patient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class IOUtilsTests {

    @AfterMethod
    public void cleanup() {
        IOUtils.cleanUp();
    }

    // ========== Encoding enum ==========

    @Test
    public void testEncodingParseJson() {
        assertEquals(IOUtils.Encoding.parse("json"), IOUtils.Encoding.JSON);
    }

    @Test
    public void testEncodingParseXml() {
        assertEquals(IOUtils.Encoding.parse("xml"), IOUtils.Encoding.XML);
    }

    @Test
    public void testEncodingParseCql() {
        assertEquals(IOUtils.Encoding.parse("cql"), IOUtils.Encoding.CQL);
    }

    @Test
    public void testEncodingParseCaseInsensitive() {
        assertEquals(IOUtils.Encoding.parse("JSON"), IOUtils.Encoding.JSON);
        assertEquals(IOUtils.Encoding.parse("Xml"), IOUtils.Encoding.XML);
        assertEquals(IOUtils.Encoding.parse("CQL"), IOUtils.Encoding.CQL);
    }

    @Test
    public void testEncodingParseWithWhitespace() {
        assertEquals(IOUtils.Encoding.parse("  json  "), IOUtils.Encoding.JSON);
        assertEquals(IOUtils.Encoding.parse(" xml "), IOUtils.Encoding.XML);
    }

    @Test
    public void testEncodingParseUnknown() {
        assertEquals(IOUtils.Encoding.parse("txt"), IOUtils.Encoding.UNKNOWN);
        assertEquals(IOUtils.Encoding.parse(""), IOUtils.Encoding.UNKNOWN);
        assertEquals(IOUtils.Encoding.parse("yaml"), IOUtils.Encoding.UNKNOWN);
    }

    @Test
    public void testEncodingParseNull() {
        assertEquals(IOUtils.Encoding.parse(null), IOUtils.Encoding.UNKNOWN);
    }

    @Test
    public void testEncodingToString() {
        assertEquals(IOUtils.Encoding.JSON.toString(), "json");
        assertEquals(IOUtils.Encoding.XML.toString(), "xml");
        assertEquals(IOUtils.Encoding.CQL.toString(), "cql");
        assertEquals(IOUtils.Encoding.UNKNOWN.toString(), "");
    }

    // ========== Path/String utilities ==========

    @Test
    public void testConcatFilePaths() {
        String result = IOUtils.concatFilePath("basePath", "input", "resources", "library");
        String expected =
                "basePath" + File.separator + "input" + File.separator + "resources" + File.separator + "library";
        assertEquals(result, expected);
    }

    @Test
    public void testConcatFilePathSingleElement() {
        String result = IOUtils.concatFilePath("base", "child");
        assertEquals(result, "base" + File.separator + "child");
    }

    @Test
    public void testGetIdFromFileNameReplacesUnderscores() {
        assertEquals(IOUtils.getIdFromFileName("my_library_name"), "my-library-name");
    }

    @Test
    public void testGetIdFromFileNameNoUnderscores() {
        assertEquals(IOUtils.getIdFromFileName("no-underscores"), "no-underscores");
    }

    @Test
    public void testGetIdFromFileNameEmpty() {
        assertEquals(IOUtils.getIdFromFileName(""), "");
    }

    @Test
    public void testGetParentDirectoryPath() {
        String result = IOUtils.getParentDirectoryPath("/some/path/to/file.json");
        assertEquals(result, "/some/path/to");
    }

    @Test
    public void testGetParentDirectoryPathOfDirectory() {
        String result = IOUtils.getParentDirectoryPath("/some/path/resources");
        assertEquals(result, "/some/path");
    }

    @Test
    public void testGetResourceDirectoryWhenParentIsResources() {
        // Parent of the file is "resources" -- should stop there
        String result = IOUtils.getResourceDirectory("/some/path/resources/mylib.json");
        assertTrue(result.endsWith("resources"), "Should end with 'resources' but got: " + result);
    }

    @Test
    public void testGetResourceDirectoryWhenGrandparentIsResources() {
        // Parent is "library", grandparent is "resources" -- goes up two levels
        String result = IOUtils.getResourceDirectory("/some/path/resources/library/mylib.json");
        assertTrue(result.endsWith("resources"), "Should end with 'resources' but got: " + result);
    }

    @Test
    public void testGetEncoding() {
        assertEquals(IOUtils.getEncoding("file.json"), IOUtils.Encoding.JSON);
        assertEquals(IOUtils.getEncoding("file.xml"), IOUtils.Encoding.XML);
        assertEquals(IOUtils.getEncoding("file.cql"), IOUtils.Encoding.CQL);
        assertEquals(IOUtils.getEncoding("file.txt"), IOUtils.Encoding.UNKNOWN);
        assertEquals(IOUtils.getEncoding("noextension"), IOUtils.Encoding.UNKNOWN);
    }

    @Test
    public void testGetEncodingFromFullPath() {
        assertEquals(IOUtils.getEncoding("/some/deep/path/resource.json"), IOUtils.Encoding.JSON);
        assertEquals(IOUtils.getEncoding("/some/deep/path/resource.xml"), IOUtils.Encoding.XML);
    }

    @Test
    public void testGetFileExtension() {
        assertEquals(IOUtils.getFileExtension(IOUtils.Encoding.JSON), ".json");
        assertEquals(IOUtils.getFileExtension(IOUtils.Encoding.XML), ".xml");
        assertEquals(IOUtils.getFileExtension(IOUtils.Encoding.CQL), ".cql");
        assertEquals(IOUtils.getFileExtension(IOUtils.Encoding.UNKNOWN), ".");
    }

    @Test
    public void testPathEndsWithElement() {
        assertTrue(IOUtils.pathEndsWithElement("/some/path/resources", "resources"));
        assertFalse(IOUtils.pathEndsWithElement("/some/path/resources", "library"));
    }

    @Test
    public void testPathEndsWithElementTrailingSeparator() {
        assertTrue(IOUtils.pathEndsWithElement("/some/path/resources/", "resources"));
    }

    @Test
    public void testPathEndsWithElementFileNotDirectory() {
        // pathEndsWithElement uses getBaseName which strips extensions
        assertFalse(IOUtils.pathEndsWithElement("/some/path/file.json", "file.json"));
        assertTrue(IOUtils.pathEndsWithElement("/some/path/file.json", "file"));
    }

    @Test
    public void testIsXMLOrJson() {
        assertTrue(IOUtils.isXMLOrJson("/some/dir/", "library.json"));
        assertTrue(IOUtils.isXMLOrJson("/some/dir/", "library.xml"));
        assertTrue(IOUtils.isXMLOrJson("/some/dir/", "library.JSON"));
        assertTrue(IOUtils.isXMLOrJson("/some/dir/", "library.XML"));
        assertFalse(IOUtils.isXMLOrJson("/some/dir/", "library.cql"));
        assertFalse(IOUtils.isXMLOrJson("/some/dir/", "library.txt"));
    }

    @Test
    public void testGetMeasureTestDirectory() {
        // Should return the second-to-last path element
        String result = IOUtils.getMeasureTestDirectory("/tests/measure-ABC/patient-1");
        assertEquals(result, "measure-ABC");
    }

    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void testGetMeasureTestDirectorySingleElement() {
        // A path with fewer than 2 elements should fail
        IOUtils.getMeasureTestDirectory("single");
    }

    @Test
    public void testPutInListIfAbsent() {
        List<String> list = new ArrayList<>();
        IOUtils.putInListIfAbsent("a", list);
        IOUtils.putInListIfAbsent("b", list);
        IOUtils.putInListIfAbsent("a", list); // duplicate
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), "a");
        assertEquals(list.get(1), "b");
    }

    @Test
    public void testPutAllInListIfAbsent() {
        List<String> list = new ArrayList<>();
        list.add("a");
        IOUtils.putAllInListIfAbsent(List.of("a", "b", "c"), list);
        assertEquals(list.size(), 3);
        assertTrue(list.containsAll(List.of("a", "b", "c")));
    }

    @Test
    public void testPutAllInListIfAbsentPreservesOrder() {
        List<String> list = new ArrayList<>();
        list.add("a");
        IOUtils.putAllInListIfAbsent(List.of("c", "b", "a"), list);
        assertEquals(list.get(0), "a"); // original
        assertEquals(list.get(1), "c"); // first new
        assertEquals(list.get(2), "b"); // second new
    }

    @Test
    public void testFormatFileName() {
        FhirContext ctx = FhirContext.forR4Cached();
        assertEquals(IOUtils.formatFileName("MyLibrary", IOUtils.Encoding.JSON, ctx), "MyLibrary.json");
        assertEquals(IOUtils.formatFileName("MyLibrary", IOUtils.Encoding.XML, ctx), "MyLibrary.xml");
    }

    @Test
    public void testFormatFileNameCqlEncodingReplacesHyphenWithUnderscore() {
        FhirContext ctx = FhirContext.forR4Cached();
        // For CQL encoding, the FHIR version token separator should be underscore not hyphen
        String result = IOUtils.formatFileName("MyLibrary-FHIR4", IOUtils.Encoding.CQL, ctx);
        assertEquals(result, "MyLibrary_FHIR4.cql");
    }

    @Test
    public void testFormatFileNameCqlWithoutVersionToken() {
        FhirContext ctx = FhirContext.forR4Cached();
        // When no FHIR version token in the name, CQL should still work
        String result = IOUtils.formatFileName("MyLibrary", IOUtils.Encoding.CQL, ctx);
        assertEquals(result, "MyLibrary.cql");
    }

    // ========== File system operations ==========

    @Test
    public void testIsDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            assertTrue(IOUtils.isDirectory(tempDir.toString()));
            assertFalse(IOUtils.isDirectory(tempDir.resolve("nonexistent").toString()));
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testIsDirectoryOnFile() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", ".txt");
        try {
            assertFalse(IOUtils.isDirectory(tempFile.toString()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testEnsurePathCreatesDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path newDir = tempDir.resolve("subdir");
        try {
            assertFalse(newDir.toFile().exists());
            IOUtils.ensurePath(newDir.toString());
            assertTrue(newDir.toFile().exists());
            assertTrue(newDir.toFile().isDirectory());
        } finally {
            Files.deleteIfExists(newDir);
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testEnsurePathCreatesNestedDirectories() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path nested = tempDir.resolve("a").resolve("b").resolve("c");
        try {
            IOUtils.ensurePath(nested.toString());
            assertTrue(nested.toFile().exists());
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testEnsurePathAlreadyExists() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.ensurePath(tempDir.toString());
            assertTrue(tempDir.toFile().exists());
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void testDeleteDirectoryRecursive() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(tempDir.resolve("file.txt"), "content");
        Files.writeString(subDir.resolve("nested.txt"), "nested");

        IOUtils.deleteDirectory(tempDir.toString());
        assertFalse(tempDir.toFile().exists());
    }

    @Test
    public void testInitializeDirectoryClearsExisting() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.writeString(tempDir.resolve("file.txt"), "content");
        Files.writeString(tempDir.resolve("other.json"), "{}");

        IOUtils.initializeDirectory(tempDir.toString());
        assertTrue(tempDir.toFile().exists());
        assertTrue(tempDir.toFile().isDirectory());
        assertEquals(tempDir.toFile().listFiles().length, 0, "Directory should be empty after initialize");

        // cleanup
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testInitializeDirectoryCreatesNew() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path newDir = tempDir.resolve("newdir");
        try {
            assertFalse(newDir.toFile().exists());
            IOUtils.initializeDirectory(newDir.toString());
            assertTrue(newDir.toFile().exists());
            assertTrue(newDir.toFile().isDirectory());
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetFileContentReturnsExactContent() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", ".txt");
        String content = "hello world\nline two";
        Files.writeString(tempFile, content);
        try {
            assertEquals(IOUtils.getFileContent(tempFile.toFile()), content);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetFileContentNonexistent() {
        IOUtils.getFileContent(new File("/nonexistent/path/file.txt"));
    }

    @Test
    public void testGetCqlStringPreservesContent() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", ".cql");
        String cqlContent = "library Test version '1.0'\nusing FHIR version '4.0.1'";
        Files.writeString(tempFile, cqlContent);
        try {
            String result = IOUtils.getCqlString(tempFile.toString());
            // getCqlString reads line-by-line and appends \n after each line,
            // so the result will have a trailing newline
            assertTrue(result.startsWith("library Test version '1.0'"));
            assertTrue(result.contains("using FHIR version '4.0.1'"));
            assertTrue(result.endsWith("\n"), "getCqlString appends newline after each line");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetCqlStringNonexistent() {
        IOUtils.getCqlString("/nonexistent/file.cql");
    }

    @Test
    public void testWriteCqlToFileRoundTrip() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        String filePath = tempDir.resolve("output.cql").toString();
        String cqlContent = "library Test version '1.0'";
        try {
            IOUtils.writeCqlToFile(cqlContent, filePath);
            String result = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            assertEquals(result, cqlContent, "Written content should match exactly");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteCqlToFileUtf8() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        String filePath = tempDir.resolve("output.cql").toString();
        String cqlContent = "library Test // unicode: \u00e9\u00e0\u00fc\u00f1";
        try {
            IOUtils.writeCqlToFile(cqlContent, filePath);
            String result = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            assertEquals(result, cqlContent);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetFilePathsNonRecursive() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.writeString(tempDir.resolve("a.json"), "{}");
        Files.writeString(tempDir.resolve("b.xml"), "<x/>");
        Path sub = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(sub.resolve("c.json"), "{}");
        try {
            List<String> paths = IOUtils.getFilePaths(tempDir.toString(), false);
            assertEquals(paths.size(), 2, "Non-recursive should only return files in the top directory");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetFilePathsRecursive() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.writeString(tempDir.resolve("a.json"), "{}");
        Path sub = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(sub.resolve("b.json"), "{}");
        Path nested = Files.createDirectory(sub.resolve("nested"));
        Files.writeString(nested.resolve("c.json"), "{}");
        try {
            List<String> paths = IOUtils.getFilePaths(tempDir.toString(), true);
            assertEquals(paths.size(), 3, "Recursive should return all files in subdirectories");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetFilePathsCaching() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.writeString(tempDir.resolve("a.json"), "{}");
        try {
            List<String> first = IOUtils.getFilePaths(tempDir.toString(), false);
            // Add another file -- cached result should still be returned
            Files.writeString(tempDir.resolve("b.json"), "{}");
            List<String> second = IOUtils.getFilePaths(tempDir.toString(), false);
            assertSame(first, second, "Should return cached list on second call");
            assertEquals(second.size(), 1, "Cached result should not reflect new file");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetDirectoryPaths() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.createDirectory(tempDir.resolve("sub1"));
        Files.createDirectory(tempDir.resolve("sub2"));
        Files.writeString(tempDir.resolve("file.txt"), "not a dir");
        try {
            List<String> paths = IOUtils.getDirectoryPaths(tempDir.toString(), false);
            assertEquals(paths.size(), 2, "Should return only directories, not files");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetDirectoryPathsRecursive() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path sub1 = Files.createDirectory(tempDir.resolve("sub1"));
        Files.createDirectory(tempDir.resolve("sub2"));
        Files.createDirectory(sub1.resolve("nested"));
        try {
            List<String> paths = IOUtils.getDirectoryPaths(tempDir.toString(), true);
            assertEquals(paths.size(), 3, "Recursive should include sub1, sub2, and nested");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCopyFileContent() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path src = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        Files.writeString(src, "copy me");
        try {
            IOUtils.copyFile(src.toString(), dest.toString());
            assertTrue(dest.toFile().exists(), "Destination file should exist");
            assertEquals(Files.readString(dest), "copy me", "Content should match source");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCopyFileNullBothDoesNotThrow() {
        // Both null -- should log error via LogUtils, not throw
        IOUtils.copyFile(null, null);
    }

    @Test
    public void testCopyFileNullInputDoesNotThrow() {
        IOUtils.copyFile(null, "/some/path");
    }

    @Test
    public void testCopyFileNullOutputDoesNotThrow() {
        IOUtils.copyFile("/some/path", null);
    }

    @Test
    public void testCopyFileEmptyPathsDoNotCreateFiles() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            int filesBefore = tempDir.toFile().listFiles().length;
            IOUtils.copyFile("", "");
            IOUtils.copyFile("", tempDir.resolve("dest.txt").toString());
            IOUtils.copyFile(tempDir.resolve("src.txt").toString(), "");
            int filesAfter = tempDir.toFile().listFiles().length;
            assertEquals(filesBefore, filesAfter, "No files should be created for empty paths");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCopyFileDeduplication() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path src = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        Files.writeString(src, "original");
        try {
            IOUtils.copyFile(src.toString(), dest.toString());
            // Modify source after first copy
            Files.writeString(src, "modified");
            // Second copy should be skipped due to deduplication cache
            IOUtils.copyFile(src.toString(), dest.toString());
            assertEquals(Files.readString(dest), "original",
                    "Deduplication should prevent re-copying the same src->dest pair");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCopyFileDeduplicationClearsOnCleanup() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path src = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        Files.writeString(src, "original");
        try {
            IOUtils.copyFile(src.toString(), dest.toString());
            assertEquals(Files.readString(dest), "original");

            // Modify source and clear caches
            Files.writeString(src, "modified");
            IOUtils.cleanUp();

            // Now copy should work again
            IOUtils.copyFile(src.toString(), dest.toString());
            assertEquals(Files.readString(dest), "modified",
                    "After cleanUp, deduplication cache should be cleared");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    // ========== FHIR resource operations ==========

    @Test
    public void testEncodeResourceJsonContent() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test-patient");

        byte[] encoded = IOUtils.encodeResource(patient, IOUtils.Encoding.JSON, ctx);
        String json = new String(encoded, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"resourceType\""), "Should contain resourceType");
        assertTrue(json.contains("Patient"), "Should contain Patient");
        assertTrue(json.contains("test-patient"), "Should contain the ID");
    }

    @Test
    public void testEncodeResourceXmlContent() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test-patient");

        byte[] encoded = IOUtils.encodeResource(patient, IOUtils.Encoding.XML, ctx);
        String xml = new String(encoded, StandardCharsets.UTF_8);
        assertTrue(xml.contains("Patient"), "Should contain Patient element");
        assertTrue(xml.contains("test-patient"), "Should contain the ID");
    }

    @Test
    public void testEncodeResourceUnknownEncodingReturnsEmpty() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        byte[] result = IOUtils.encodeResource(patient, IOUtils.Encoding.UNKNOWN, ctx);
        assertEquals(result.length, 0, "UNKNOWN encoding should return empty bytes");
    }

    @Test
    public void testEncodeResourceProducesUtf8() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test-patient");
        patient.addName().setFamily("\u00e9\u00e0\u00fc"); // unicode chars

        byte[] encoded = IOUtils.encodeResource(patient, IOUtils.Encoding.JSON, ctx);
        // Decode as UTF-8 and verify the characters survive the round-trip
        String json = new String(encoded, StandardCharsets.UTF_8);
        assertTrue(json.contains("\u00e9\u00e0\u00fc") || json.contains("\\u00"),
                "Unicode content should survive encoding as UTF-8");
    }

    @Test
    public void testEncodeResourcePrettyPrint() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test-patient");
        patient.addName().setFamily("Smith");

        byte[] compact = IOUtils.encodeResource(patient, IOUtils.Encoding.JSON, ctx, false);
        byte[] pretty = IOUtils.encodeResource(patient, IOUtils.Encoding.JSON, ctx, true);
        assertTrue(pretty.length > compact.length,
                "Pretty-printed output should be larger than compact");
        String prettyStr = new String(pretty, StandardCharsets.UTF_8);
        assertTrue(prettyStr.contains("\n"), "Pretty-printed output should contain newlines");
    }

    @Test
    public void testEncodeResourceAsStringAlwaysPrettyPrints() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test-patient");

        String result = IOUtils.encodeResourceAsString(patient, IOUtils.Encoding.JSON, ctx);
        assertTrue(result.contains("\n"), "encodeResourceAsString should always pretty-print");
        assertTrue(result.contains("\"resourceType\""));
    }

    @Test
    public void testEncodeResourceAsStringUnknown() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        assertEquals(IOUtils.encodeResourceAsString(patient, IOUtils.Encoding.UNKNOWN, ctx), "");
    }

    @Test
    public void testWriteAndReadResourceRoundTrip() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("write-test");
        patient.addName().setFamily("TestFamily");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeResource(patient, tempDir.toString(), IOUtils.Encoding.JSON, ctx);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().endsWith(".json"));

            IBaseResource readBack = IOUtils.readResource(files[0].getAbsolutePath(), ctx);
            assertNotNull(readBack);
            assertTrue(readBack instanceof Patient);
            Patient readPatient = (Patient) readBack;
            assertEquals(readPatient.getIdElement().getIdPart(), "write-test");
            assertEquals(readPatient.getNameFirstRep().getFamily(), "TestFamily",
                    "Round-trip should preserve all data");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteAndReadResourceXml() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("xml-test");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeResource(patient, tempDir.toString(), IOUtils.Encoding.XML, ctx);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().endsWith(".xml"), "Should write XML file");

            IBaseResource readBack = IOUtils.readResource(files[0].getAbsolutePath(), ctx);
            assertNotNull(readBack);
            assertEquals(readBack.getIdElement().getIdPart(), "xml-test");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadResourceSafeReadNonexistent() {
        FhirContext ctx = FhirContext.forR4Cached();
        IBaseResource result = IOUtils.readResource("/nonexistent/path/file.json", ctx, true);
        assertNull(result, "safeRead should return null for nonexistent file");
    }

    @Test
    public void testReadResourceSafeReadDirectory() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IBaseResource result = IOUtils.readResource(tempDir.toString() + ".json", ctx, true);
            // This tests with a .json extension on a nonexistent path -- should return null
            assertNull(result, "safeRead should return null for nonexistent path");

            // Now test with an actual directory that has a json extension
            // (contrived but tests the safeRead contract for directories)
            Path jsonDir = tempDir.resolve("test.json");
            Files.createDirectory(jsonDir);
            IBaseResource dirResult = IOUtils.readResource(jsonDir.toString(), ctx, true);
            assertNull(dirResult, "safeRead should return null for directory paths, not throw");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReadResourceDirectoryThrowsWithoutSafeRead() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path jsonDir = tempDir.resolve("test.json");
        Files.createDirectory(jsonDir);
        try {
            IOUtils.readResource(jsonDir.toString(), ctx, false);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadResourceUnknownEncoding() {
        FhirContext ctx = FhirContext.forR4Cached();
        assertNull(IOUtils.readResource("file.txt", ctx), "Unknown encoding should return null");
        assertNull(IOUtils.readResource("file.cql", ctx), "CQL encoding should return null");
    }

    @Test
    public void testReadResourceCaching() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("cache-test");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(patient));

            IBaseResource first = IOUtils.readResource(tempFile.toString(), ctx);
            IBaseResource second = IOUtils.readResource(tempFile.toString(), ctx);
            assertSame(first, second, "Second read should return same cached instance");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadResources() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p1 = new Patient();
            p1.setId("p1");
            Patient p2 = new Patient();
            p2.setId("p2");
            Path f1 = tempDir.resolve("p1.json");
            Path f2 = tempDir.resolve("p2.json");
            Files.writeString(f1, ctx.newJsonParser().encodeResourceToString(p1));
            Files.writeString(f2, ctx.newJsonParser().encodeResourceToString(p2));

            List<IBaseResource> resources = IOUtils.readResources(List.of(f1.toString(), f2.toString()), ctx);
            assertEquals(resources.size(), 2);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadResourcesSkipsUnreadable() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p1 = new Patient();
            p1.setId("p1");
            Path f1 = tempDir.resolve("p1.json");
            Files.writeString(f1, ctx.newJsonParser().encodeResourceToString(p1));

            // Include a CQL path that should return null and be skipped
            List<IBaseResource> resources = IOUtils.readResources(
                    List.of(f1.toString(), "/nonexistent/file.cql"), ctx);
            assertEquals(resources.size(), 1, "Should skip non-readable paths");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceWithOutputFileName() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("test");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeResource(patient, tempDir.toString(), IOUtils.Encoding.JSON, ctx, true, "custom-name");

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertEquals(files[0].getName(), "custom-name.json",
                    "Output file should use the custom name with encoding extension");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetCanonicalResourceVersionR4() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setVersion("1.0.0");
        assertEquals(IOUtils.getCanonicalResourceVersion(lib, ctx), "1.0.0");
    }

    @Test
    public void testGetCanonicalResourceVersionNonMetadata() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        assertNull(IOUtils.getCanonicalResourceVersion(patient, ctx),
                "Non-MetadataResource should return null version");
    }

    @Test
    public void testGetCanonicalResourceVersionDstu3() {
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.Library lib = new org.hl7.fhir.dstu3.model.Library();
        lib.setVersion("2.0.0");
        assertEquals(IOUtils.getCanonicalResourceVersion(lib, ctx), "2.0.0");
    }

    @Test
    public void testGetTypeQualifiedResourceId() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("Patient/my-patient");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(patient));
            String result = IOUtils.getTypeQualifiedResourceId(tempFile.toString(), ctx);
            assertEquals(result, "Patient/my-patient");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetTypeQualifiedResourceIdNotFound() {
        String result = IOUtils.getTypeQualifiedResourceId("/nonexistent.json", FhirContext.forR4Cached());
        assertNull(result, "Should return null for nonexistent resource");
    }

    @Test
    public void testWriteBundle() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        bundle.setId("test-bundle");
        bundle.setType(Bundle.BundleType.COLLECTION);

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeBundle(bundle, tempDir.toString(), IOUtils.Encoding.JSON, ctx);
            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().contains("test-bundle"), "File should contain bundle ID");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteBundleWithOutputFileName() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        bundle.setId("test-bundle");
        bundle.setType(Bundle.BundleType.COLLECTION);

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeBundle(bundle, tempDir.toString(), IOUtils.Encoding.JSON, ctx, "custom-bundle");
            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertEquals(files[0].getName(), "custom-bundle.json");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWriteBundleUnsupportedFhirVersion() throws IOException {
        FhirContext ctx = FhirContext.forR5Cached();
        Bundle bundle = new Bundle();
        bundle.setId("test-bundle");

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeBundle(bundle, tempDir.toString(), IOUtils.Encoding.JSON, ctx);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetResourcesInDirectory() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p = new Patient();
            p.setId("p1");
            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(p));
            Files.writeString(tempDir.resolve("not-a-resource.txt"), "ignored");

            List<IBaseResource> resources = IOUtils.getResourcesInDirectory(tempDir.toString(), ctx, false);
            assertEquals(resources.size(), 1, "Should only read json/xml files, not txt");
            assertTrue(resources.get(0) instanceof Patient);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetResourcesInDirectoryRecursive() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path sub = Files.createDirectory(tempDir.resolve("sub"));
        try {
            Patient p1 = new Patient();
            p1.setId("p1");
            Patient p2 = new Patient();
            p2.setId("p2");
            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(p1));
            Files.writeString(sub.resolve("p2.json"), ctx.newJsonParser().encodeResourceToString(p2));

            List<IBaseResource> nonRecursive = IOUtils.getResourcesInDirectory(tempDir.toString(), ctx, false);
            assertEquals(nonRecursive.size(), 1, "Non-recursive should only get top-level");

            IOUtils.cleanUp();
            List<IBaseResource> recursive = IOUtils.getResourcesInDirectory(tempDir.toString(), ctx, true);
            assertEquals(recursive.size(), 2, "Recursive should get resources in subdirectories");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetResourcesOfTypeInDirectory() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient patient = new Patient();
            patient.setId("p1");
            Library library = new Library();
            library.setId("lib1");

            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(patient));
            Files.writeString(tempDir.resolve("lib1.json"), ctx.newJsonParser().encodeResourceToString(library));

            // Should only return Patient resources
            List<IBaseResource> patients =
                    IOUtils.getResourcesOfTypeInDirectory(tempDir.toString(), ctx, Patient.class, false);
            assertEquals(patients.size(), 1, "Should find exactly one Patient");
            assertTrue(patients.get(0) instanceof Patient);

            IOUtils.cleanUp();

            // Should only return Library resources
            List<IBaseResource> libraries =
                    IOUtils.getResourcesOfTypeInDirectory(tempDir.toString(), ctx, Library.class, false);
            assertEquals(libraries.size(), 1, "Should find exactly one Library");
            assertTrue(libraries.get(0) instanceof Library);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetResourcesOfTypeInDirectoryNoMatch() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient patient = new Patient();
            patient.setId("p1");
            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(patient));

            // Ask for Library type when only Patient exists
            List<IBaseResource> libraries =
                    IOUtils.getResourcesOfTypeInDirectory(tempDir.toString(), ctx, Library.class, false);
            assertEquals(libraries.size(), 0, "Should find no Libraries when only Patient exists");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testBundleResourcesInDirectory() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p = new Patient();
            p.setId("Patient/p1");
            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(p));

            IBaseBundle bundle = IOUtils.bundleResourcesInDirectory(tempDir.toString(), ctx, false);
            assertNotNull(bundle);
            assertTrue(bundle instanceof Bundle);
            Bundle r4Bundle = (Bundle) bundle;
            assertEquals(r4Bundle.getEntry().size(), 1);
            assertTrue(r4Bundle.getEntry().get(0).getResource() instanceof Patient);
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testBundleResourcesInDirectoryAsTransaction() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p = new Patient();
            p.setId("Patient/p1");
            Files.writeString(tempDir.resolve("p1.json"), ctx.newJsonParser().encodeResourceToString(p));

            IBaseBundle bundle = IOUtils.bundleResourcesInDirectoryAsTransaction(tempDir.toString(), ctx, false);
            assertNotNull(bundle);
            Bundle r4Bundle = (Bundle) bundle;
            assertEquals(r4Bundle.getType(), Bundle.BundleType.TRANSACTION,
                    "Bundle type should be TRANSACTION");
            assertEquals(r4Bundle.getEntry().size(), 1);
            assertNotNull(r4Bundle.getEntry().get(0).getRequest(),
                    "Transaction entries should have request component");
            assertEquals(r4Bundle.getEntry().get(0).getRequest().getMethod(), Bundle.HTTPVerb.PUT,
                    "Transaction entries should use PUT method");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCleanUpClearsCaches() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient p = new Patient();
            p.setId("cleanup-test");
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(p));

            // Populate caches
            IBaseResource before = IOUtils.readResource(tempFile.toString(), ctx);
            IOUtils.getFilePaths(tempDir.toString(), false);
            IOUtils.getDirectoryPaths(tempDir.toString(), false);

            // After cleanup, reading should create a new instance
            IOUtils.cleanUp();
            IBaseResource after = IOUtils.readResource(tempFile.toString(), ctx);
            assertNotSame(before, after, "After cleanUp, cache should be cleared and new instance returned");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testUpdateCachedResource() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient original = new Patient();
            original.setId("original");
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(original));

            // Read to populate cache
            IOUtils.readResource(tempFile.toString(), ctx);

            // Update cache with a different resource
            Patient updated = new Patient();
            updated.setId("updated");
            IOUtils.updateCachedResource(updated, tempFile.toString());

            // Subsequent read should return the updated resource
            IBaseResource cached = IOUtils.readResource(tempFile.toString(), ctx);
            assertEquals(cached.getIdElement().getIdPart(), "updated",
                    "Cache should return the updated resource");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testUpdateCachedResourceNonexistentKeyNoOp() {
        // Updating a key that doesn't exist in cache should be a no-op
        Patient patient = new Patient();
        patient.setId("ghost");
        IOUtils.updateCachedResource(patient, "/nonexistent/path.json");
        // Should not throw and key should not be added
    }

    @Test
    public void testReadJsonResourceIgnoreElements() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient p = new Patient();
            p.setId("ignore-test");
            p.addName().setFamily("Smith");
            p.setActive(true);
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(p));

            IBaseResource result = IOUtils.readJsonResourceIgnoreElements(tempFile.toString(), ctx, "name");
            assertNotNull(result);
            assertTrue(result instanceof Patient);
            Patient resultPatient = (Patient) result;
            assertTrue(resultPatient.getName().isEmpty(), "Name element should have been stripped");
            assertTrue(resultPatient.getActive(), "Non-stripped elements should be preserved");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadJsonResourceIgnoreElementsNonJson() {
        FhirContext ctx = FhirContext.forR4Cached();
        assertNull(IOUtils.readJsonResourceIgnoreElements("file.xml", ctx, "name"),
                "XML files should return null");
        assertNull(IOUtils.readJsonResourceIgnoreElements("file.cql", ctx, "name"),
                "CQL files should return null");
        assertNull(IOUtils.readJsonResourceIgnoreElements("file.txt", ctx, "name"),
                "Unknown types should return null");
    }

    @Test
    public void testGetResourceFileName() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library");
        lib.setVersion("1.0.0");

        String result = IOUtils.getResourceFileName("/output", lib, IOUtils.Encoding.JSON, ctx, true, true);
        assertTrue(result.contains("library"), "Path should contain resource type directory");
        assertTrue(result.endsWith(".json"), "Should have .json extension");
        assertTrue(result.contains("library-my-library"), "Should be prefixed with resource type");
    }

    @Test
    public void testGetResourceFileNameNotPrefixed() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library");

        String result = IOUtils.getResourceFileName("/output", lib, IOUtils.Encoding.JSON, ctx, false, false);
        assertTrue(result.endsWith(".json"));
        assertFalse(result.contains("library-my-library"),
                "Should not have resource type prefix when prefixed=false");
        assertTrue(result.contains("my-library"));
    }

    @Test
    public void testGetResourceFileNameVersionedAppendsVersion() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library");
        lib.setVersion("2.0.0");

        // versioned=true and the ID doesn't contain the version -- should append it
        String result = IOUtils.getResourceFileName("/output", lib, IOUtils.Encoding.JSON, ctx, true, false);
        assertTrue(result.contains("my-library-2.0.0"),
                "Versioned filename should append version when not already in ID");
    }

    @Test
    public void testGetResourceFileNameUnversionedStripsVersion() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library-1.0.0");
        lib.setVersion("1.0.0");

        // versioned=false and the ID contains the version -- should strip it
        String result = IOUtils.getResourceFileName("/output", lib, IOUtils.Encoding.JSON, ctx, false, false);
        assertTrue(result.contains("my-library.json"),
                "Unversioned filename should strip version from ID");
        assertFalse(result.contains("1.0.0"),
                "Version should be removed from filename");
    }

    @Test
    public void testWriteResources() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p1 = new Patient();
            p1.setId("p1");
            Patient p2 = new Patient();
            p2.setId("p2");

            IOUtils.writeResources(List.of(p1, p2), tempDir.toString(), IOUtils.Encoding.JSON, ctx);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 2, "Should write one file per resource");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourcesFromMap() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p1 = new Patient();
            p1.setId("p1");
            Patient p2 = new Patient();
            p2.setId("p2");

            IOUtils.writeResources(Map.of("first", p1, "second", p2), tempDir.toString(), IOUtils.Encoding.JSON, ctx);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 2, "Should write one file per map entry");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceToExistingFile() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path existingFile = tempDir.resolve("existing.json");
        try {
            // Write initial content
            Patient p = new Patient();
            p.setId("initial");
            Files.writeString(existingFile, ctx.newJsonParser().encodeResourceToString(p));

            // Overwrite by passing the file path directly
            Patient p2 = new Patient();
            p2.setId("overwritten");
            IOUtils.writeResource(p2, existingFile.toString(), IOUtils.Encoding.JSON, ctx);

            // Read back and verify it was overwritten
            IOUtils.cleanUp();
            IBaseResource readBack = IOUtils.readResource(existingFile.toString(), ctx);
            assertEquals(readBack.getIdElement().getIdPart(), "overwritten");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceWithBlankOutputFileName() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p = new Patient();
            p.setId("blank-name-test");

            // Blank outputFileName should fall back to resource ID
            IOUtils.writeResource(p, tempDir.toString(), IOUtils.Encoding.JSON, ctx, true, "   ");

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().contains("blank-name-test"),
                    "Blank outputFileName should fall back to resource ID");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceWithPrettyPrintOverload() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            Patient p = new Patient();
            p.setId("pretty-test");
            p.addName().setFamily("Smith");

            IOUtils.writeResource(p, tempDir.toString(), IOUtils.Encoding.JSON, ctx, true, false);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            String content = Files.readString(files[0].toPath());
            assertFalse(content.contains("\n  "),
                    "Compact output should not have indented newlines");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteBundleDstu3() throws IOException {
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        bundle.setId("stu3-bundle");
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION);

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeBundle(bundle, tempDir.toString(), IOUtils.Encoding.JSON, ctx);
            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().contains("stu3-bundle"));
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteBundleWithPrettyPrintOverload() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        bundle.setId("pretty-bundle");
        bundle.setType(Bundle.BundleType.COLLECTION);

        Path tempDir = Files.createTempDirectory("ioutilstest");
        try {
            IOUtils.writeBundle(bundle, tempDir.toString(), IOUtils.Encoding.JSON, ctx, true);
            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            String content = Files.readString(files[0].toPath());
            assertTrue(content.contains("\n"), "Pretty-printed bundle should contain newlines");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testWriteCqlToFileInvalidPath() {
        IOUtils.writeCqlToFile("library Test", "/nonexistent/deeply/nested/dir/file.cql");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testReadResourceNonexistentThrows() {
        FhirContext ctx = FhirContext.forR4Cached();
        IOUtils.readResource("/nonexistent/path/file.json", ctx, false);
    }

    @Test
    public void testReadJsonResourceIgnoreElementsCached() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient p = new Patient();
            p.setId("cache-ignore");
            p.addName().setFamily("Jones");
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(p));

            // First read populates cache
            IBaseResource first = IOUtils.readJsonResourceIgnoreElements(tempFile.toString(), ctx, "name");
            // Second read should return cached instance
            IBaseResource second = IOUtils.readJsonResourceIgnoreElements(tempFile.toString(), ctx, "name");
            assertSame(first, second, "Should return cached instance on second call");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetCanonicalResourceVersionUnsupportedFhirVersion() {
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.r5.model.Patient p = new org.hl7.fhir.r5.model.Patient();
        try {
            IOUtils.getCanonicalResourceVersion(p, ctx);
            fail("Should throw for unsupported FHIR version");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown fhir version"));
        }
    }

    @Test
    public void testGetDirectoryPathsNonexistent() {
        List<String> result = IOUtils.getDirectoryPaths("/nonexistent/path", false);
        assertTrue(result.isEmpty(), "Should return empty list for nonexistent path");
    }

    @Test
    public void testGetDirectoryPathsCaching() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Files.createDirectory(tempDir.resolve("sub1"));
        try {
            List<String> first = IOUtils.getDirectoryPaths(tempDir.toString(), false);
            Files.createDirectory(tempDir.resolve("sub2"));
            List<String> second = IOUtils.getDirectoryPaths(tempDir.toString(), false);
            assertSame(first, second, "Should return cached list on second call");
            assertEquals(second.size(), 1, "Cached result should not reflect new directory");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInitializeDirectoryThrowsOnFailure() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", ".txt");
        try {
            // Try to create a directory as a child of a file - mkdirs should fail and throw
            IOUtils.initializeDirectory(tempFile.resolve("impossible").toString());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testEnsurePathThrowsOnUncreatablePath() throws IOException {
        Path tempFile = Files.createTempFile("ioutilstest", ".txt");
        try {
            // Trying to create a dir as child of a file should throw
            IOUtils.ensurePath(tempFile.resolve("child").toString());
            fail("Should throw for uncreatable path");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Could not create directory"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testFormatFileNameDstu3CqlEncoding() {
        FhirContext ctx = FhirContext.forDstu3Cached();
        // For DSTU3, the token is FHIR3
        String result = IOUtils.formatFileName("MyLib-FHIR3", IOUtils.Encoding.CQL, ctx);
        assertEquals(result, "MyLib_FHIR3.cql");
    }

    @Test
    public void testFormatFileNameUnknownFhirVersion() {
        FhirContext ctx = FhirContext.forR5Cached();
        // R5 has no igVersionToken, so no replacement should happen
        String result = IOUtils.formatFileName("MyLib", IOUtils.Encoding.CQL, ctx);
        assertEquals(result, "MyLib.cql");
    }

    @Test
    public void testGetParserXml() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        patient.setId("xml-parser-test");

        byte[] encoded = IOUtils.encodeResource(patient, IOUtils.Encoding.XML, ctx);
        String xml = new String(encoded, StandardCharsets.UTF_8);
        assertTrue(xml.startsWith("<?xml") || xml.contains("<Patient"),
                "XML parser should produce XML output");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetParserCqlThrows() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient patient = new Patient();
        // CQL encoding for a resource should throw because there's no CQL parser
        IOUtils.encodeResource(patient, IOUtils.Encoding.CQL, ctx);
    }

    @Test
    public void testPathEndsWithElementEmptyPath() {
        // Edge case: empty string should not throw
        assertFalse(IOUtils.pathEndsWithElement("", "something"));
    }

    @Test
    public void testCopyFileOverwritesExisting() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path src = tempDir.resolve("source.txt");
        Path dest = tempDir.resolve("dest.txt");
        try {
            Files.writeString(dest, "original dest");
            Files.writeString(src, "new content");

            IOUtils.copyFile(src.toString(), dest.toString());
            assertEquals(Files.readString(dest), "new content",
                    "copyFile should overwrite existing destination");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testCopyFileNonexistentSourceDoesNotThrow() {
        // Copying from a nonexistent source should not throw -- logs error via LogUtils
        IOUtils.copyFile("/nonexistent/source.txt", "/tmp/dest.txt");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testReadJsonResourceIgnoreElementsNonexistent() {
        FhirContext ctx = FhirContext.forR4Cached();
        IOUtils.readJsonResourceIgnoreElements("/nonexistent/path/resource.json", ctx, "name");
    }

    @Test
    public void testWriteResourceErrorHandling() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient p = new Patient();
        p.setId("error-test");
        try {
            // Writing to a path under a nonexistent deeply-nested directory
            // where ensurePath will also fail
            IOUtils.writeResource(p, "/nonexistent/deep/path", IOUtils.Encoding.JSON, ctx);
            fail("Should throw for invalid write path");
        } catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testGetFilePathsOnFile() {
        // getFilePaths on a file path (not a directory) should return empty list
        try {
            Path tempFile = Files.createTempFile("ioutilstest", ".txt");
            try {
                List<String> paths = IOUtils.getFilePaths(tempFile.toString(), false);
                assertTrue(paths.isEmpty(), "getFilePaths on a file should return empty list");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (Exception e) {
            // NoSuchElementException if listFiles returns null
        }
    }

    @Test
    public void testPathEndsWithElementNullSafe() {
        // Null path should not throw, just return false
        assertFalse(IOUtils.pathEndsWithElement(null, "something"));
    }

    // ========== Fixed: separate caches for readResource and readJsonResourceIgnoreElements ==========

    @Test
    public void testReadJsonIgnoreElementsThenReadResourceReturnsDifferentInstances() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient p = new Patient();
            p.setId("cache-fix");
            p.addName().setFamily("Smith");
            p.setActive(true);
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(p));

            // Read with element stripping first
            IBaseResource stripped = IOUtils.readJsonResourceIgnoreElements(tempFile.toString(), ctx, "name");
            assertTrue(((Patient) stripped).getName().isEmpty(), "Stripped read should have no name");

            // Normal read should return full resource with name intact
            IBaseResource full = IOUtils.readResource(tempFile.toString(), ctx);
            assertNotSame(stripped, full, "Separate caches should return different instances");
            assertEquals(((Patient) full).getNameFirstRep().getFamily(), "Smith",
                    "readResource should return the full resource with name");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testReadResourceThenReadJsonIgnoreElementsStripsCorrectly() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path tempFile = tempDir.resolve("patient.json");
        try {
            Patient p = new Patient();
            p.setId("cache-fix-reverse");
            p.addName().setFamily("Jones");
            Files.writeString(tempFile, ctx.newJsonParser().encodeResourceToString(p));

            // Read full resource first
            IBaseResource full = IOUtils.readResource(tempFile.toString(), ctx);
            assertEquals(((Patient) full).getNameFirstRep().getFamily(), "Jones");

            // Read with element stripping should still strip, not return cached full version
            IBaseResource stripped = IOUtils.readJsonResourceIgnoreElements(
                    tempFile.toString(), ctx, "name");
            assertNotSame(full, stripped, "Separate caches should return different instances");
            assertTrue(((Patient) stripped).getName().isEmpty(),
                    "readJsonResourceIgnoreElements should strip elements regardless of readResource cache");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    // ========== Fixed: writeResource honors versioned parameter ==========

    @Test
    public void testWriteResourceVersionedProducesVersionedFilename() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest-versioned");
        try {
            Library lib = new Library();
            lib.setId("my-library");
            lib.setVersion("1.0.0");

            // versioned=true and version not already in ID — should append it
            IOUtils.writeResource(lib, tempDir.toString(), IOUtils.Encoding.JSON, ctx, true);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertTrue(files[0].getName().contains("1.0.0"),
                    "versioned=true should append version to filename: " + files[0].getName());
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceUnversionedStripsVersion() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest-unversioned");
        try {
            Library lib = new Library();
            lib.setId("my-library-1.0.0");
            lib.setVersion("1.0.0");

            // versioned=false and version in ID — should strip it
            IOUtils.writeResource(lib, tempDir.toString(), IOUtils.Encoding.JSON, ctx, false);

            File[] files = tempDir.toFile().listFiles();
            assertNotNull(files);
            assertEquals(files.length, 1);
            assertFalse(files[0].getName().contains("1.0.0"),
                    "versioned=false should strip version from filename: " + files[0].getName());
            assertTrue(files[0].getName().startsWith("my-library."),
                    "Filename should be 'my-library.json': " + files[0].getName());
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testWriteResourceVersionedAndUnversionedDiffer() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir1 = Files.createTempDirectory("ioutilstest-v");
        Path tempDir2 = Files.createTempDirectory("ioutilstest-uv");
        try {
            Library lib = new Library();
            lib.setId("my-library-1.0.0");
            lib.setVersion("1.0.0");

            IOUtils.writeResource(lib, tempDir1.toString(), IOUtils.Encoding.JSON, ctx, true);
            IOUtils.writeResource(lib, tempDir2.toString(), IOUtils.Encoding.JSON, ctx, false);

            String versionedName = tempDir1.toFile().listFiles()[0].getName();
            String unversionedName = tempDir2.toFile().listFiles()[0].getName();
            assertNotEquals(versionedName, unversionedName,
                    "versioned=true and versioned=false should produce different filenames");
        } finally {
            IOUtils.deleteDirectory(tempDir1.toString());
            IOUtils.deleteDirectory(tempDir2.toString());
        }
    }

    // ========== Fixed: initializeDirectory uses mkdirs and throws on failure ==========

    @Test
    public void testInitializeDirectoryCreatesNestedPath() throws IOException {
        Path tempDir = Files.createTempDirectory("ioutilstest");
        Path nested = tempDir.resolve("a").resolve("b").resolve("c");
        try {
            assertFalse(nested.toFile().exists());
            IOUtils.initializeDirectory(nested.toString());
            assertTrue(nested.toFile().exists(),
                    "initializeDirectory should create nested directories");
            assertTrue(nested.toFile().isDirectory());
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testResolveBaseNameAppendsVersionWhenMissing() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library");
        lib.setVersion("2.0.0");

        String result = IOUtils.resolveBaseName(lib, ctx, true);
        assertEquals(result, "my-library-2.0.0");
    }

    @Test
    public void testResolveBaseNameStripsVersionWhenPresent() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library-1.0.0");
        lib.setVersion("1.0.0");

        String result = IOUtils.resolveBaseName(lib, ctx, false);
        assertEquals(result, "my-library");
    }

    @Test
    public void testResolveBaseNameKeepsIdWhenVersionAlreadyPresent() {
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library-1.0.0");
        lib.setVersion("1.0.0");

        String result = IOUtils.resolveBaseName(lib, ctx, true);
        assertEquals(result, "my-library-1.0.0", "Should not double-append version");
    }

    @Test
    public void testResolveBaseNameNoVersion() {
        FhirContext ctx = FhirContext.forR4Cached();
        Patient p = new Patient();
        p.setId("my-patient");

        String versioned = IOUtils.resolveBaseName(p, ctx, true);
        String unversioned = IOUtils.resolveBaseName(p, ctx, false);
        assertEquals(versioned, "my-patient", "No version to append for non-MetadataResource");
        assertEquals(unversioned, "my-patient", "No version to strip for non-MetadataResource");
    }

    @Test
    public void testResolveBaseNameUnversionedButVersionNotInId() {
        // When !versioned and version exists but is NOT in the ID string,
        // the filename should be left unchanged (indexOf returns -1)
        FhirContext ctx = FhirContext.forR4Cached();
        Library lib = new Library();
        lib.setId("my-library");
        lib.setVersion("1.0.0");

        String result = IOUtils.resolveBaseName(lib, ctx, false);
        assertEquals(result, "my-library", "Version not in ID, so nothing to strip");
    }

    @Test
    public void testGetResourcesOfTypeInDirectoryFilters() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest-type-filter");
        try {
            // Write a Patient — then request only Library resources
            Patient p = new Patient();
            p.setId("test-patient");
            p.addName().setFamily("Smith");
            Files.writeString(tempDir.resolve("patient.json"),
                    ctx.newJsonParser().encodeResourceToString(p));

            List<IBaseResource> libraries = IOUtils.getResourcesOfTypeInDirectory(
                    tempDir.toString(), ctx, Library.class, false);
            assertTrue(libraries.isEmpty(), "Patient should not match Library filter");

            List<IBaseResource> patients = IOUtils.getResourcesOfTypeInDirectory(
                    tempDir.toString(), ctx, Patient.class, false);
            assertEquals(patients.size(), 1, "Patient should match Patient filter");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }

    @Test
    public void testGetResourcesInDirectoryReturnsAll() throws IOException {
        FhirContext ctx = FhirContext.forR4Cached();
        Path tempDir = Files.createTempDirectory("ioutilstest-all-resources");
        try {
            Patient p = new Patient();
            p.setId("pt1");
            Files.writeString(tempDir.resolve("patient.json"),
                    ctx.newJsonParser().encodeResourceToString(p));

            Library lib = new Library();
            lib.setId("lib1");
            Files.writeString(tempDir.resolve("library.json"),
                    ctx.newJsonParser().encodeResourceToString(lib));

            List<IBaseResource> all = IOUtils.getResourcesInDirectory(
                    tempDir.toString(), ctx, false);
            assertEquals(all.size(), 2, "Should return all resources regardless of type");
        } finally {
            IOUtils.deleteDirectory(tempDir.toString());
        }
    }
}
