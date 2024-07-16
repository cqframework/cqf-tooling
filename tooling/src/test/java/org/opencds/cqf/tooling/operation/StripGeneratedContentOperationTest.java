package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.Operation;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class StripGeneratedContentOperationTest {
    @Test
    public void test_strip_generated_content() throws URISyntaxException, FileNotFoundException {
        String dataInputPath = "strip-resources";
        String operation = "StripGeneratedContent";
        var inputFilePath = Path.of(StripGeneratedContentOperationTest.class.getResource(dataInputPath).toURI());
        var outputPath = Path.of("target", "test-output", "strip-generated-content");
        String version = "r4";

        Library libraryBeforeStrip = (Library)FhirContext.forR4Cached().newJsonParser().parseResource(
                new FileReader(inputFilePath + "/LibraryBreastCancerScreeningFHIR.json"));

        assertEquals(libraryBeforeStrip.getContent().size(), 3);
        assertTrue(libraryBeforeStrip.hasText());
        assertTrue(libraryBeforeStrip.hasParameter());
        assertTrue(libraryBeforeStrip.hasDataRequirement());
        assertEquals(libraryBeforeStrip.getRelatedArtifact().size(), 46);

        String[] args = { "-" + operation, "-ptr=" + inputFilePath, "-op=" + outputPath, "-v=" + version };
        Operation stripGeneratedContentOperation = new StripGeneratedContentOperation();
        stripGeneratedContentOperation.execute(args);

        File jsonFile = outputPath.resolve("LibraryBreastCancerScreeningFHIR.json").toFile();

        var libraryAfterStrip = (Library) FhirContext.forR4Cached().newJsonParser().parseResource(new FileReader(jsonFile));

        assertEquals(libraryAfterStrip.getContent().size(), 1);
        // Cql should not be stripped or exported
        assertTrue(libraryAfterStrip.getContent().get(0).hasData());
        assertFalse(libraryAfterStrip.hasText());
        assertFalse(libraryAfterStrip.hasParameter());
        assertFalse(libraryAfterStrip.hasDataRequirement());
        assertEquals(libraryAfterStrip.getRelatedArtifact().size(), 1);

    }

    @Test
    void exportsCql() throws URISyntaxException, FileNotFoundException {
        String dataInputPath = "strip-resources";
        String operation = "StripGeneratedContent";
        var inputFilePath = Path.of(StripGeneratedContentOperationTest.class.getResource(dataInputPath).toURI());
        var outputPath = Path.of("target", "test-output", "strip-generated-content-cql");
       
        String[] args = { "-" + operation, "-ptr=" + inputFilePath, "-op=" + outputPath, "-cql=" + outputPath + File.separator + "cql"};
        Operation stripGeneratedContentOperation = new StripGeneratedContentOperation();
        stripGeneratedContentOperation.execute(args);

        File jsonFile = outputPath.resolve("LibraryBreastCancerScreeningFHIR.json").toFile();
        var libraryAfterStrip = (Library) FhirContext.forR4Cached().newJsonParser().parseResource(new FileReader(jsonFile));

        assertEquals(libraryAfterStrip.getContent().size(), 1);
        // Cql should be exported
        assertFalse(libraryAfterStrip.getContent().get(0).hasData());
        assertTrue(libraryAfterStrip.getContent().get(0).hasUrl());
        assertFalse(libraryAfterStrip.hasText());
        assertFalse(libraryAfterStrip.hasParameter());
        assertFalse(libraryAfterStrip.hasDataRequirement());
        assertEquals(libraryAfterStrip.getRelatedArtifact().size(), 1);

        File cqlFile = outputPath.resolve("cql").resolve("BreastCancerScreeningFHIR.cql").toFile();
        assertTrue(cqlFile.exists());
    }
}
