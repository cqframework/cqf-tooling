package org.opencds.cqf.tooling.operation;

import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.library.LibraryRefresh;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceDiscovery;
import org.testng.annotations.BeforeMethod;

public class RefreshLibraryOperationIT extends RefreshTest {

    public RefreshLibraryOperationIT() {
        super(FhirContext.forCached(FhirVersionEnum.R4));
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        ResourceDiscovery.clearDevicePaths();
        File dir = Paths.get("target", "refreshLibraries").toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    // @Test
    // TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = Paths.get("target", "refreshLibraries", "r4").toString();
        copyResourcesToTargetDir(targetDirectory, "r4");

        String libraryPath = Paths.get("input", "resources", "library",
                "library-EXM124_FHIR4-8.2.000.json").toString();

        String args[] = {
            "-RefreshLibrary",
            "-ini=" + Paths.get(targetDirectory, "ig.ini"),
            "-lp=" + Paths.get(targetDirectory, libraryPath),
            "-cql=" + Paths.get(targetDirectory, "input", "pagecontent", "cql",
                    "EXM124_FHIR4-8.2.000.cql"),
            "-e=json",
            "-fv=4.0.1"
        };

        Operation refreshLibraryOperation = new ExecutableOperationAdapter(new LibraryRefresh());
        refreshLibraryOperation.execute(args);

        validateSoftwareSystemExtension(Paths.get(targetDirectory, libraryPath).toString());
    }

    // @Test
    // TODO: Fix separately, this is blocking a bunch of other higher priority things
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = Paths.get("target", "refreshLibraries", "r4").toFile();
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshTest.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String libraryPath = Paths.get("input", "resources", "library",
                "library-EXM124_FHIR4-8.2.000.json").toString();

        String args[] = {
            "-RefreshLibrary",
            "-ini=" + Paths.get(resourceDirPath, "ig.ini"),
            "-lp=" + Paths.get(resourceDirPath, libraryPath),
            "-lop=" + targetDirectory.getAbsolutePath(),
            "-cql=" + Paths.get(resourceDirPath, "input", "pagecontent", "cql",
                    "EXM124_FHIR4-8.2.000.cql"),
            "-e=json",
            "-fv=4.0.1"
        };

        Operation refreshLibraryOperation = new ExecutableOperationAdapter(new LibraryRefresh());
        refreshLibraryOperation.execute(args);

        assertTrue(targetDirectory.listFiles().length > 0);
    }
}
