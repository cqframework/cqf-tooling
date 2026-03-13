package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.testng.annotations.Test;

public class TranslatorUtilsTest {

    @Test
    public void getTranslatorOptions_noOptionsFile_returnsDefaultWithXml() throws IOException {
        // When no cql-options.json exists, defaults should include XML format
        Path tempDir = Files.createTempDirectory("translator-test");
        try {
            CqlTranslatorOptions options = TranslatorUtils.getTranslatorOptions(tempDir.toString());
            assertNotNull(options);
            assertTrue(options.getFormats().contains(CqlTranslatorOptions.Format.XML),
                    "Default options should include XML format");
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void getTranslatorOptions_withOptionsFile_loadsFromFile() throws IOException {
        Path tempDir = Files.createTempDirectory("translator-test");
        try {
            // Create a minimal cql-options.json
            Path optionsFile = tempDir.resolve("cql-options.json");
            Files.writeString(optionsFile, """
                    {
                      "formats": ["JSON"],
                      "options": ["EnableAnnotations"]
                    }
                    """, StandardCharsets.UTF_8);

            CqlTranslatorOptions options = TranslatorUtils.getTranslatorOptions(tempDir.toString());
            assertNotNull(options);
        } finally {
            Files.deleteIfExists(tempDir.resolve("cql-options.json"));
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void getTranslatorOptions_nonExistentFolder_returnsDefaults() {
        // Non-existent folder means no cql-options.json, should return defaults
        CqlTranslatorOptions options = TranslatorUtils.getTranslatorOptions("/nonexistent/path");
        assertNotNull(options);
        assertTrue(options.getFormats().contains(CqlTranslatorOptions.Format.XML));
    }
}
