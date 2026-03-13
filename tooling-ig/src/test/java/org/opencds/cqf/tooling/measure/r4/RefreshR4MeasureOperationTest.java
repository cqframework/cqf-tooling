package org.opencds.cqf.tooling.measure.r4;

import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceDiscovery;
import org.testng.annotations.BeforeMethod;

public class RefreshR4MeasureOperationTest extends RefreshTest {
    private String targetDirectoryPath = Paths.get("target", "refreshMeasures", "r4").toString();

    public RefreshR4MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.R4), "RefreshR4MeasureOperationTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        ResourceDiscovery.clearDevicePaths();
        File dir = Paths.get("target", "refreshMeasures", "r4").toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        File bundleDir = Paths.get(targetDirectoryPath, "output", "refreshedMeasureBundles").toFile();
        if (bundleDir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    // @Test
    private void testRefreshOverwriteMeasures() throws Exception {
        setUp();
        copyResourcesToTargetDir(targetDirectoryPath, "r4");

        String measureDirectoryPath = Paths.get("input", "resources", "measure").toString();
        String libraryDirectoryPath = Paths.get("input", "resources", "library").toString();

        String args[] = {
            "-RefreshR4Measure",
            "-op=" + Paths.get(targetDirectoryPath, "output", "refreshedMeasureBundles").toString(),
            "-ss=true",
            "-ptm=" + Paths.get(targetDirectoryPath, measureDirectoryPath).toString(),
            "-ptl=" + Paths.get(targetDirectoryPath, libraryDirectoryPath).toString(),
        };

        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(targetDirectoryPath);
        refreshMeasureOperation.execute(args);

        // Currently tooling writes output file with a "-" rather than an "_" for "measure-EXM124_FHIR4-8.2.000.json" vs
        // "measure-EXM124-FHIR4-8.2.000.json"
        String measureValidationPath = Paths.get("output", "refreshedMeasureBundles", "measure-EXM124-FHIR4-8.2.000.json").toString();

        validateSoftwareSystemExtension(Paths.get(targetDirectoryPath, measureValidationPath).toString());
    }

    // @Test
    // TODO: There is a file handle leak somewhere in the refresh process that results in a failure when this test is
    // run after the prior one (or vice versa)
    private void testRefreshOutputDirectory() throws Exception {
        setUp();
        // create a output directory under target directory
        File targetDirectory = Paths.get(targetDirectoryPath).toFile();
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String measureDirectoryPath = Paths.get("input", "resources", "measure").toString();
        String libraryDirectoryPath = Paths.get("input", "resources", "library").toString();

        String args[] = {
            "-RefreshR4Measure",
            "-op=" + Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles").toString(),
            "-ptm=" + Paths.get(resourceDirPath, measureDirectoryPath).toString(),
            "-ptl=" + Paths.get(resourceDirPath, libraryDirectoryPath).toString(),
        };

        String operationOutputPath = Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles").toString();
        RefreshR4MeasureOperation refreshMeasureOperation = new RefreshR4MeasureOperation(operationOutputPath);
        refreshMeasureOperation.execute(args);

        File validationFile = Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles").toFile();

        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}
