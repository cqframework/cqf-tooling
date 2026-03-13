package org.opencds.cqf.tooling.library.r4;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessorTest;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceDiscovery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class R4LibraryProcessorTest extends LibraryProcessorTest {
    private final String resourceDirectory = "r4";

    public R4LibraryProcessorTest() {
        super(new R4LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.R4), "R4LibraryProcessorTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        ResourceDiscovery.clearDevicePaths();
        File dir = new File(Paths.get("target", "refreshLibraries", "r4").toString());
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    void testRefreshOverwriteLibrariesWithCqfmSoftwareSystemExtension() throws Exception {
        String targetDirectory = Paths.get("target", "refreshLibraries", this.resourceDirectory).toString();
        copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);

        String libraryPath = Paths.get("input", "resources", "library", "library-EXM124_FHIR4-8.2.000.json").toString();
        runRefresh(
                targetDirectory,
                Paths.get(targetDirectory, libraryPath).toString(),
                Paths.get(targetDirectory, "input", "pagecontent", "cql", "EXM124_FHIR4-8.2.000.cql").toString(),
                false);

        validateSoftwareSystemExtension(Paths.get(targetDirectory, libraryPath).toString());
    }

    @Test
    void testRefreshOverwriteLibrariesWithoutCqfmSoftwareSystemExtension() throws Exception {
        String targetDirectory = Paths.get("target", "refreshLibraries", this.resourceDirectory).toString();
        copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);

        String libraryPath = Paths.get("input", "resources", "library", "library-EXM124_FHIR4-8.2.000.json").toString();
        runRefresh(
                targetDirectory,
                Paths.get(targetDirectory, libraryPath).toString(),
                null,
                Paths.get(targetDirectory, "input", "pagecontent", "cql", "EXM124_FHIR4-8.2.000.cql").toString(),
                false,
                false);

        validateNoCqfmSoftwareSystemExtension(Paths.get(targetDirectory, libraryPath).toString());
    }

    @Test
    void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File(Paths.get("target", "refreshLibraries", resourceDirectory).toString());
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = Objects.requireNonNull(RefreshTest.class.getResource(resourceDirectory))
                .getPath();
        assertEquals(Objects.requireNonNull(targetDirectory.listFiles()).length, 0);

        String libraryPath = Paths.get("input", "resources", "library", "library-EXM124_FHIR4-8.2.000.json").toString();
        runRefresh(
                resourceDirPath,
                Paths.get(resourceDirPath, libraryPath).toString(),
                targetDirectory.getAbsolutePath(),
                Paths.get(resourceDirPath, "input", "pagecontent", "cql", "EXM124_FHIR4-8.2.000.cql").toString(),
                false);

        assertTrue(Objects.requireNonNull(targetDirectory.listFiles()).length > 0);
    }
}
