package org.opencds.cqf.tooling.library.stu3;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessorTest;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class STU3LibraryProcessorTest extends LibraryProcessorTest {

    private String resourceDirectory = "stu3";
    public STU3LibraryProcessorTest() {
        super(new STU3LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.DSTU3), "STU3LibraryProcessorTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshLibraries" + separator + "stu3");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "target" + separator + "refreshLibraries" + separator + this.resourceDirectory;
        copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);
        
        String libraryPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM105-FHIR3-8.0.000.json";
        runRefresh(
            targetDirectory,
            targetDirectory + libraryPath,
            targetDirectory + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM105_FHIR3-8.0.000.cql",
            false
        );

        validateCqfmSoftwareSystemExtension(targetDirectory + libraryPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("target" + separator + "refreshLibraries" + separator + resourceDirectory);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource(resourceDirectory).getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String libraryPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM105-FHIR3-8.0.000.json";
        runRefresh(
            resourceDirPath,
            resourceDirPath + libraryPath,
            targetDirectory.getAbsolutePath(),
            resourceDirPath + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM105_FHIR3-8.0.000.cql",
            false
        );

        assertTrue(targetDirectory.listFiles().length > 0);
    }
}
