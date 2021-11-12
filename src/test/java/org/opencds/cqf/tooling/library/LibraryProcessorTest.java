package org.opencds.cqf.tooling.library;

import java.util.ArrayList;

import org.opencds.cqf.tooling.RefreshTest;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.testng.annotations.BeforeMethod;

import ca.uhn.fhir.context.FhirContext;

public abstract class LibraryProcessorTest extends RefreshTest {
    private LibraryProcessor libraryProcessor;

    // When running mvn package there is some collisions between tests running while trying to delete this directory
    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.resourceDirectories = new ArrayList<String>();
        IOUtils.clearDevicePaths();
        // File dir  = new File("target/refreshLibraries");
        // if (dir.exists()) {
        //     FileUtils.deleteDirectory(dir);
        // }
    }

    public LibraryProcessorTest(LibraryProcessor libraryProcessor, FhirContext fhirContext, String testName) {
        super(fhirContext, testName);
        this.libraryProcessor = libraryProcessor;
    }

    protected LibraryProcessor getLibraryProcessor() {
        return this.libraryProcessor;
    }

    protected void runRefresh(String targetDirectory, String libraryResourcePath, String cqlResourcePath,
            boolean versioned) {
        runRefresh(targetDirectory, libraryResourcePath, null, cqlResourcePath, versioned);
    }

    protected void runRefresh(String targetDirectory, String libraryResourcePath, String libraryOutputDirectoryPath,
            String cqlResourcePath, boolean versioned) {
        RefreshLibraryParameters params = new RefreshLibraryParameters();
        params.encoding = Encoding.JSON;
        params.fhirContext = getFhirContext();
        params.libraryPath = libraryResourcePath;
        params.libraryOutputDirectory = libraryOutputDirectoryPath;
        params.cqlContentPath = cqlResourcePath;
        params.ini = targetDirectory + separator + "ig.ini";
        params.versioned = versioned;
        getLibraryProcessor().refreshLibraryContent(params);
    }
}
