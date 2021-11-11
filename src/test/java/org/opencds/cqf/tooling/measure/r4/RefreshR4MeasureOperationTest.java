package org.opencds.cqf.tooling.measure.r4;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.assertTrue;

public class RefreshR4MeasureOperationTest extends RefreshTest {

    public RefreshR4MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        File dir  = new File("target" + separator + "refreshMeasures");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteMeasures() throws Exception {
        String targetDirectory = "." + separator + "target" + separator + "refreshMeasures" + separator + "r4";
        copyResourcesToTargetDir(targetDirectory, "r4");
        
        String measureDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshR4Measure",
            "-op=" + targetDirectory + "" + separator + "output" + separator + "refreshedMeasureBundles",
            "-ptm=" + targetDirectory + measureDirectoryPath,
            "-ptl=" + targetDirectory + libraryDirectoryPath,
        };

        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(targetDirectory);
        refreshMeasureOperation.execute(args);

        //Currently tooling writes output file with a "-" rather than an "_" for "measure-EXM124_FHIR4-8.2.000.json" vs "measure-EXM124-FHIR4-8.2.000.json"
        String measureValidationPath = "" + separator + "output" + separator + "refreshedMeasureBundles" + separator + "measure-EXM124-FHIR4-8.2.000.json";

        validateCqfmSofwareSystemExtension(targetDirectory + measureValidationPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("." + separator + "target" + separator + "refreshMeasures" + separator + "r4");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);
        
        String measureDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = "" + separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshR4Measure",
            "-op=" + targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles",
            "-ptm=" + resourceDirPath + measureDirectoryPath,
            "-ptl=" + resourceDirPath + libraryDirectoryPath,
        };

        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles");
        refreshMeasureOperation.execute(args);

        File validationFile = new File(targetDirectory.getAbsolutePath() + "" + separator + "output" + separator + "refreshedMeasureBundles");

        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}