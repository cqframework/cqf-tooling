package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.Operation;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.testng.Assert.*;

public class StripGeneratedContentOperationTest {
    static final String separator = System.getProperty("file.separator");
    @Test
    public void test_strip_generated_content() throws URISyntaxException, FileNotFoundException {
        String dataInputPath = "strip-resources";
        String operation = "StripGeneratedContent";
        String inputFilePath = StripGeneratedContentOperationTest.class.getResource(dataInputPath).toURI().getPath();
        String outputPath = "target/test-output/strip-generated-content";
        String version = "r4";


        Library libraryBeforeStrip = (Library)FhirContext.forR4Cached().newJsonParser().parseResource(
                new FileReader(inputFilePath+"/LibraryBreastCancerScreeningFHIR.json"));

        assertEquals(libraryBeforeStrip.getContent().size(), 4);
        assertTrue(libraryBeforeStrip.hasText());
        assertTrue(libraryBeforeStrip.hasParameter());
        assertTrue(libraryBeforeStrip.hasDataRequirement());
        assertEquals(libraryBeforeStrip.getRelatedArtifact().size(), 46);

        String[] args = { "-" + operation, "-ptr=" + inputFilePath, "-op=" + outputPath, "-v=" + version };
        Operation stripGeneratedContentOperation = new StripGeneratedContentOperation();
        stripGeneratedContentOperation.execute(args);

        Path path = getPath();
        Library libraryAfterStrip = (Library)FhirContext.forR4Cached().newJsonParser().parseResource(
                new FileReader(path.resolve( "LibraryBreastCancerScreeningFHIR.json").toFile()));

        assertEquals(libraryAfterStrip.getContent().size(), 1);
        assertFalse(libraryAfterStrip.hasText());
        assertFalse(libraryAfterStrip.hasParameter());
        assertFalse(libraryAfterStrip.hasDataRequirement());
        assertEquals(libraryAfterStrip.getRelatedArtifact().size(), 1);

    }

    private Path getPath() {
        String location = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String os = System.getProperty("os.name");
        String[] st = location.split("/");
        String[] str = Arrays.copyOf(st, st.length-1);
        StringBuilder sb = new StringBuilder();
        if(!os.contains("Windows")){
            sb.append(separator);
        }
        for(String item: str){
            if(!StringUtils.isBlank(item)) {
                sb.append(item);
                sb.append(separator);
            }
        }
        return Paths.get(sb.toString()+"test-output/strip-generated-content");
    }
}
