package org.opencds.cqf.tooling.measure.stu3;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.assertTrue;

public class RefreshStu3MeasureOperationTest extends RefreshTest {

    public RefreshStu3MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.DSTU3));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        File dir  = new File("target" + separator + "refreshMeasures");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "." + separator + "target" + separator + "refreshMeasures" + separator + "stu3";
        copyResourcesToTargetDir(targetDirectory, "stu3");
        
        String measureDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshStu3Measure",
            "-op=" + targetDirectory + "" + separator + "output" + separator + "refreshedMeasureBundles",
            "-ptm=" + targetDirectory + measureDirectoryPath,
            "-ptl=" + targetDirectory + libraryDirectoryPath,
        };

        RefreshStu3MeasureOperation refreshMeasureOperation = new RefreshStu3MeasureOperation(targetDirectory);
        refreshMeasureOperation.execute(args);

        String measureValidationPath = "" + separator + "input" + separator + "resources" + separator + "measure" + separator + "measure-EXM105-FHIR3-8.0.000.json";
        String libraryValidationPath = "" + separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM105-FHIR3-8.0.000.json";

        validateCqfmSofwareSystemExtension(targetDirectory + measureValidationPath);
        validateCqfmSofwareSystemExtension(targetDirectory + libraryValidationPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("." + separator + "target" + separator + "refreshMeasures" + separator + "" + "stu3");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("stu3").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);
        
        String measureDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshStu3Measure",
            "-op=" + targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles",
            "-ptm=" + resourceDirPath + measureDirectoryPath,
            "-ptl=" + resourceDirPath + libraryDirectoryPath,
        };

        RefreshStu3MeasureOperation refreshMeasureOperation = new RefreshStu3MeasureOperation(targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles");
        refreshMeasureOperation.execute(args);

        File validationFile = new File(targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles");
        
        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}
