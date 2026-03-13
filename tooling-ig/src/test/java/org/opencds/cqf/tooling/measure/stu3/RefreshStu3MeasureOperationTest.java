package org.opencds.cqf.tooling.measure.stu3;

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

public class RefreshStu3MeasureOperationTest extends RefreshTest {
    private String targetDirectoryPath = Paths.get("target", "refreshMeasures", "stu3").toString();

    public RefreshStu3MeasureOperationTest() {
        super(FhirContext.forCached(FhirVersionEnum.DSTU3), "RefreshStu3MeasureOperationTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        ResourceDiscovery.clearDevicePaths();
        File dir = Paths.get("target", "refreshMeasures", "stu3").toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        File bundleDir = Paths.get(targetDirectoryPath, "output", "refreshedMeasureBundles").toFile();
        if (bundleDir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    // @Test
    // TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOverwriteLibraries() throws Exception {
        copyResourcesToTargetDir(targetDirectoryPath, "stu3");

        String measureDirectoryPath = Paths.get("input", "resources", "measure").toString();
        String libraryDirectoryPath = Paths.get("input", "resources", "library").toString();

        String args[] = {
            "-RefreshStu3Measure",
            "-op=" + Paths.get(targetDirectoryPath, "output", "refreshedMeasureBundles"),
            "-ptm=" + Paths.get(targetDirectoryPath, measureDirectoryPath),
            "-ptl=" + Paths.get(targetDirectoryPath, libraryDirectoryPath),
        };

        RefreshStu3MeasureOperation refreshMeasureOperation = new RefreshStu3MeasureOperation(targetDirectoryPath);
        refreshMeasureOperation.execute(args);

        String measureValidationPath = Paths.get("input", "resources", "measure",
                "measure-EXM105-FHIR3-8.0.000.json").toString();
        String libraryValidationPath = Paths.get("input", "resources", "library",
                "library-EXM105-FHIR3-8.0.000.json").toString();

        validateSoftwareSystemExtension(Paths.get(targetDirectoryPath, measureValidationPath).toString());
        validateSoftwareSystemExtension(Paths.get(targetDirectoryPath, libraryValidationPath).toString());
    }

    // @Test
    // TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("stu3").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String measureDirectoryPath = Paths.get("input", "resources", "measure").toString();
        String libraryDirectoryPath = Paths.get("input", "resources", "library").toString();

        String args[] = {
            "-RefreshStu3Measure",
            "-op=" + Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles"),
            "-ptm=" + Paths.get(resourceDirPath, measureDirectoryPath),
            "-ptl=" + Paths.get(resourceDirPath, libraryDirectoryPath),
        };

        RefreshStu3MeasureOperation refreshMeasureOperation =
                new RefreshStu3MeasureOperation(
                        Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles").toString());
        refreshMeasureOperation.execute(args);

        File validationFile = Paths.get(targetDirectory.getAbsolutePath(), "output", "refreshedMeasureBundles").toFile();

        assertTrue(validationFile.exists());
        assertTrue(validationFile.listFiles().length > 0);
    }
}
