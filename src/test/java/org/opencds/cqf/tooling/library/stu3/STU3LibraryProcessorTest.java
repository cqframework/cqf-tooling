package org.opencds.cqf.tooling.library.stu3;

import java.io.File;

import org.opencds.cqf.tooling.library.LibraryProcessorTest;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.assertTrue;

public class STU3LibraryProcessorTest extends LibraryProcessorTest {

    private String resourceDirectory = "stu3";
    public STU3LibraryProcessorTest() {
        super(new STU3LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.R4));
    }
    
    @Test
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "./target/refreshLibraries/" + this.resourceDirectory;
        copyResourcesToTargetRefreshLibrariesDir(targetDirectory, this.resourceDirectory);
        
        String libraryPath = "/input/resources/library/library-EXM105-FHIR3-8.0.000.json";
        runRefresh(
            targetDirectory,
            targetDirectory + libraryPath,
            targetDirectory + "/input/pagecontent/cql/EXM105_FHIR3-8.0.000.cql",
            false
        );

        validateCqfmSofwareSystemExtension(targetDirectory + libraryPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("./target/refreshLibraries/" + resourceDirectory);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = LibraryProcessorTest.class.getResource(resourceDirectory).getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String libraryPath = "/input/resources/library/library-EXM105-FHIR3-8.0.000.json";
        runRefresh(
            resourceDirPath,
            resourceDirPath + libraryPath,
            targetDirectory.getAbsolutePath(),
            resourceDirPath + "/input/pagecontent/cql/EXM105_FHIR3-8.0.000.cql",
            false
        );

        assertTrue(targetDirectory.listFiles().length > 0);
    }
}
