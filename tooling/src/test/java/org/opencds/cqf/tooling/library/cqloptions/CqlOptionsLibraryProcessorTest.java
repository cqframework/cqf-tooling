package org.opencds.cqf.tooling.library.cqloptions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.library.LibraryProcessorTest;
import org.opencds.cqf.tooling.library.r4.R4LibraryProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class CqlOptionsLibraryProcessorTest extends LibraryProcessorTest {
    private final String resourceDirectory = "cqloptions";
    public CqlOptionsLibraryProcessorTest() {
        super(new R4LibraryProcessor(), FhirContext.forCached(FhirVersionEnum.R4), "CqlOptionsLibraryProcessorTest");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        File dir  = new File("target" + separator + "refreshLibraries" + separator + "cqloptions");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test
    // This test validates that given a present cql-options.json file that includes no ELM formats
    // the generated file includes CQL only
    void testCqlOptionsFormats() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("target" + separator + "refreshLibraries" + separator + resourceDirectory);
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = Objects.requireNonNull(RefreshTest.class.getResource(resourceDirectory)).getPath();
        assertEquals(Objects.requireNonNull(targetDirectory.listFiles()).length, 0);

        String libraryPath = separator + "input" + separator + "resources" + separator + "library" + separator + "library-EXM124_FHIR4-8.2.000.json";
        runRefresh(
            resourceDirPath,
            resourceDirPath + libraryPath,
            targetDirectory.getAbsolutePath(),
            resourceDirPath + separator + "input" + separator + "pagecontent" + separator + "cql" + separator + "EXM124_FHIR4-8.2.000.cql",
            false
        );

        assertTrue(Objects.requireNonNull(targetDirectory.listFiles()).length > 0);

        var libPath = Path.of(targetDirectory.getAbsolutePath(), "Test.json").toFile();

        var lib = (Library) this.getFhirContext().newJsonParser().parseResource(new FileInputStream(libPath));

        assertEquals(lib.getContent().size(), 1);

        var att = lib.getContent().get(0);

        assertEquals(att.getContentType(), "text/cql");
    }
}
