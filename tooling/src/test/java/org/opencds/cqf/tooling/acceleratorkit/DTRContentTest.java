package org.opencds.cqf.tooling.acceleratorkit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

public class DTRContentTest extends BaseContentTest {

    public DTRContentTest() {
        super(new Spreadsheet() {
            {
                path = "acceleratorkit/DTR.xlsx";
                scope = "ASLP";
                dataElementPages = "ASLP.A1 Adult Sleep Studies";
            }
        });
    }

    @Test
    public void validateContentCount() {
        assertEquals(cqlPath().toFile().listFiles().length, 4);
        assertEquals(examplesPath().toFile().listFiles().length, 8);
        assertFalse(extensionsPath().toFile().exists());
        assertEquals(profilesPath().toFile().listFiles().length, 8);
        assertEquals(resourcesPath().toFile().listFiles().length, 1);
        assertFalse(testsPath().toFile().exists());
        assertEquals(vocabularyPath().resolve("codesystem").toFile().listFiles().length, 2);
        assertEquals(vocabularyPath().resolve("conceptmap").toFile().listFiles().length, 2);
        assertEquals(vocabularyPath().resolve("valueset").toFile().listFiles().length, 4);
    }
}