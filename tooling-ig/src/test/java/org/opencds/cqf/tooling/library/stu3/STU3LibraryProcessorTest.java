package org.opencds.cqf.tooling.library.stu3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
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

public class STU3LibraryProcessorTest extends LibraryProcessorTest {

    private final String resourceDirectory = "stu3";

    public STU3LibraryProcessorTest() {
        super(new STU3LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.DSTU3), "STU3LibraryProcessorTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        ResourceDiscovery.clearDevicePaths();
        var dir = Paths.get("target", "refreshLibraries", "stu3").toFile();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = Paths.get("target", "refreshLibraries", this.resourceDirectory).toString();
        copyResourcesToTargetDir(targetDirectory, this.resourceDirectory);

        String libraryPath = Paths.get("input", "resources", "library", "library-EXM105-FHIR3-8.0.000.json")
                .toString();
        runRefresh(
                targetDirectory,
                Paths.get(targetDirectory, libraryPath).toString(),
                Paths.get(targetDirectory, "input", "pagecontent", "cql", "EXM105_FHIR3-8.0.000.cql")
                        .toString(),
                false);

        validateSoftwareSystemExtension(Paths.get(targetDirectory, libraryPath).toString());
    }

    @Test
    void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        var targetDirectory = Paths.get("target", "refreshLibraries", resourceDirectory).toFile();
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = Objects.requireNonNull(RefreshTest.class.getResource(resourceDirectory))
                .getPath();
        assertEquals(Objects.requireNonNull(targetDirectory.listFiles()).length, 0);

        String libraryPath = Paths.get("input", "resources", "library", "library-EXM105-FHIR3-8.0.000.json")
                .toString();
        runRefresh(
                resourceDirPath,
                Paths.get(resourceDirPath, libraryPath).toString(),
                targetDirectory.getAbsolutePath(),
                Paths.get(resourceDirPath, "input", "pagecontent", "cql", "EXM105_FHIR3-8.0.000.cql")
                        .toString(),
                false);

        assertTrue(Objects.requireNonNull(targetDirectory.listFiles()).length > 0);
    }
}
