package org.opencds.cqf.tooling.library.r4;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.library.LibraryProcessorTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.assertTrue;

public class R4LibraryProcessorTest extends LibraryProcessorTest {
    private String resourceDirectory = "r4";
    public R4LibraryProcessorTest() {
        super(new R4LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        File dir  = new File("target/refreshLibraries/r4");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "./target/refreshLibraries/" + this.resourceDirectory;
        copyResourcesToTargetRefreshLibrariesDir(targetDirectory, this.resourceDirectory);
        
        String libraryPath = "/input/resources/library/library-EXM124_FHIR4-8.2.000.json";
        runRefresh(
            targetDirectory,
            targetDirectory + libraryPath,
            targetDirectory + "/input/pagecontent/cql/EXM124_FHIR4-8.2.000.cql",
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

        String libraryPath = "/input/resources/library/library-EXM124_FHIR4-8.2.000.json";
        runRefresh(
            resourceDirPath,
            resourceDirPath + libraryPath,
            targetDirectory.getAbsolutePath(),
            resourceDirPath + "/input/pagecontent/cql/EXM124_FHIR4-8.2.000.cql",
            false
        );

        assertTrue(targetDirectory.listFiles().length > 0);
    }
}
