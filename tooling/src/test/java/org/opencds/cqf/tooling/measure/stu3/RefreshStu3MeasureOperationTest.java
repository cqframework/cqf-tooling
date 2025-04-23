package org.opencds.cqf.tooling.measure.stu3;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class RefreshStu3MeasureOperationTest extends RefreshTest {
    private String targetDirectoryPath = "target" + separator + "refreshMeasures" + separator + "stu3";

    public RefreshStu3MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.DSTU3), "RefreshStu3MeasureOperationTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshMeasures" + separator + "stu3");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        File bundleDir = new File(targetDirectoryPath + separator + "output" + separator + "refreshedMeasureBundles" + separator);
        if (bundleDir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    //@Test
    //TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOverwriteLibraries() throws Exception {
        copyResourcesToTargetDir(targetDirectoryPath, "stu3");

        String measureDirectoryPath = separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshStu3Measure",
            "-op=" + targetDirectoryPath + separator + "output" + separator + "refreshedMeasureBundles" + separator,
            "-ptm=" + targetDirectoryPath + measureDirectoryPath,
            "-ptl=" + targetDirectoryPath + libraryDirectoryPath,
        };

        RefreshStu3MeasureOperation refreshMeasureOperation = new RefreshStu3MeasureOperation(targetDirectoryPath);
        refreshMeasureOperation.execute(args);

        String measureValidationPath = separator + "input" + separator + "resources" + separator + "measure" + separator + "measure-EXM105-FHIR3-8.0.000.json";
        String libraryValidationPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM105-FHIR3-8.0.000.json";

        validateSoftwareSystemExtension(targetDirectoryPath + measureValidationPath);
        validateSoftwareSystemExtension(targetDirectoryPath + libraryValidationPath);
    }

    //@Test
    //TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("stu3").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String measureDirectoryPath = separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshStu3Measure",
            "-op=" + targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator,
            "-ptm=" + resourceDirPath + measureDirectoryPath,
            "-ptl=" + resourceDirPath + libraryDirectoryPath,
        };

        RefreshStu3MeasureOperation refreshMeasureOperation = new RefreshStu3MeasureOperation(targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator);
        refreshMeasureOperation.execute(args);

        File validationFile = new File(targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator);

        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}
