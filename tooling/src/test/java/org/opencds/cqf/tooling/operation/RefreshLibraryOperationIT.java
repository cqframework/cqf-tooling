package org.opencds.cqf.tooling.operation;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class RefreshLibraryOperationIT extends RefreshTest {

    public RefreshLibraryOperationIT() {
        super(FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshLibraries");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    //@Test
    //TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "target" + separator + "refreshLibraries" + separator + "r4";
        copyResourcesToTargetDir(targetDirectory, "r4");

        String libraryPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM124_FHIR4-8.2.000.json";

		String args[] = {
            "-RefreshLibrary",
            "-ini=" + targetDirectory + separator + "ig.ini",
            "-lp=" + targetDirectory + libraryPath,
            "-cql=" + targetDirectory + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM124_FHIR4-8.2.000.cql",
            "-e=json",
            "-fv=4.0.1"
        };

        RefreshLibraryOperation refreshLibraryOperation = new RefreshLibraryOperation();
        refreshLibraryOperation.execute(args);

        validateSoftwareSystemExtension(targetDirectory + libraryPath);
    }

    //@Test
    //TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("target" + separator + "refreshLibraries" + separator + "r4");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String libraryPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM124_FHIR4-8.2.000.json";

		String args[] = {
            "-RefreshLibrary",
            "-ini=" + resourceDirPath + separator + "ig.ini",
            "-lp=" + resourceDirPath + libraryPath,
            "-lop=" + targetDirectory.getAbsolutePath(),
            "-cql=" + resourceDirPath + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM124_FHIR4-8.2.000.cql",
            "-e=json",
            "-fv=4.0.1"
        };

        RefreshLibraryOperation refreshLibraryOperation = new RefreshLibraryOperation();
        refreshLibraryOperation.execute(args);

        assertTrue(targetDirectory.listFiles().length > 0);
    }
}
