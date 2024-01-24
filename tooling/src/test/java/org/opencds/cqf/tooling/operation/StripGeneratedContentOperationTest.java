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
import java.nio.file.Paths;

public class StripGeneratedContentOperationTest {
    private static final String separator = System.getProperty("file.separator");
    @Test
    public void test_strip_generated_content() throws URISyntaxException, FileNotFoundException {
        String dataInputPath = "strip-resources";
        String operation = "StripGeneratedContent";
        String inputFilePath = StripGeneratedContentOperationTest.class.getResource(dataInputPath).toURI().getPath();
        String outputPath = "target/test-output/strip-generated-content";
        String version = "r4";


        Library libraryBeforeStrip = (Library)FhirContext.forR4Cached().newJsonParser().parseResource(
                new FileReader(inputFilePath+"/LibraryBreastCancerScreeningFHIR.json"));

        assertEquals(libraryBeforeStrip.getContent().size(), 3);
        assertTrue(libraryBeforeStrip.hasText());
        assertTrue(libraryBeforeStrip.hasParameter());
        assertTrue(libraryBeforeStrip.hasDataRequirement());
        assertEquals(libraryBeforeStrip.getRelatedArtifact().size(), 46);

        String[] args = { "-" + operation, "-ptr=" + inputFilePath, "-op=" + outputPath, "-v=" + version };
        Operation stripGeneratedContentOperation = new StripGeneratedContentOperation();
        stripGeneratedContentOperation.execute(args);

        Library libraryAfterStrip = null;
        if (separator.equalsIgnoreCase("/")) {

            Path path = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().getPath() +
                    "/../test-output/strip-generated-content");
            libraryAfterStrip = (Library)FhirContext.forR4Cached().newJsonParser().parseResource(
                    new FileReader(path + "/LibraryBreastCancerScreeningFHIR.json"));

        }else{

            File classLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            File parentDir = classLocation.getParentFile(); // Get the parent directory of the class location
            File outputDir = new File(parentDir, "test-output/");
            File jsonFile = new File(outputDir, "strip-generated-contentLibraryBreastCancerScreeningFHIR.json");

            libraryAfterStrip = (Library) FhirContext.forR4Cached().newJsonParser().parseResource(new FileReader(jsonFile));
        }


        assertEquals(libraryAfterStrip.getContent().size(), 1);
        assertFalse(libraryAfterStrip.hasText());
        assertFalse(libraryAfterStrip.hasParameter());
        assertFalse(libraryAfterStrip.hasDataRequirement());
        assertEquals(libraryAfterStrip.getRelatedArtifact().size(), 1);

    }
}
