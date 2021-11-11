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
    private String targetDirectoryPath = "target" + separator + "refreshMeasures" + separator + "r4";

    public RefreshR4MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        File dir  = new File("target" + separator + "refreshMeasures" + separator + "r4");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        File bundleDir = new File(targetDirectoryPath + separator + "output" + separator + "refreshedMeasureBundles" + separator);
        if (bundleDir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteMeasures() throws Exception {
        copyResourcesToTargetDir(targetDirectoryPath, "r4");
        
        String measureDirectoryPath = separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshR4Measure",
            "-op=" + targetDirectoryPath + separator + "output" + separator + "refreshedMeasureBundles" + separator,
            "-ptm=" + targetDirectoryPath + measureDirectoryPath,
            "-ptl=" + targetDirectoryPath + libraryDirectoryPath,
        };

        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(targetDirectoryPath);
        refreshMeasureOperation.execute(args);

        //Currently tooling writes output file with a "-" rather than an "_" for "measure-EXM124_FHIR4-8.2.000.json" vs "measure-EXM124-FHIR4-8.2.000.json"
        String measureValidationPath = separator + "output" + separator + "refreshedMeasureBundles" + separator + "measure-EXM124-FHIR4-8.2.000.json";

        validateCqfmSofwareSystemExtension(targetDirectoryPath + measureValidationPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);
        
        String measureDirectoryPath = separator + "input" + separator + "resources" + separator + "measure";
        String libraryDirectoryPath = separator + "input" + separator + "resources" + separator + "library";

		String args[] = {
            "-RefreshR4Measure",
            "-op=" + targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator,
            "-ptm=" + resourceDirPath + measureDirectoryPath,
            "-ptl=" + resourceDirPath + libraryDirectoryPath,
        };

        String operationOutputPath = targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator;
        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(operationOutputPath);
        refreshMeasureOperation.execute(args);

        File validationFile = new File(targetDirectory.getAbsolutePath() + separator + "output" + separator + "refreshedMeasureBundles" + separator);

        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}