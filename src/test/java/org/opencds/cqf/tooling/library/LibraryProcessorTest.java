package org.opencds.cqf.tooling.library;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.testng.annotations.BeforeMethod;

import ca.uhn.fhir.context.FhirContext;

import static org.testng.Assert.assertNotNull;

public abstract class LibraryProcessorTest {
    private String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";
    private LibraryProcessor libraryProcessor;
    private FhirContext fhirContext;

    // When running mvn package there is some collisions between tests running while trying to delete this directory
    // @BeforeMethod
    // public void setUp() throws Exception {
    //     File dir  = new File("target/refreshLibraries");
    //     if (dir.exists()) {
    //         FileUtils.deleteDirectory(dir);
    //     }
    // }

    public LibraryProcessorTest(LibraryProcessor libraryProcessor, FhirContext fhirContext) {
        this.libraryProcessor = libraryProcessor;
        this.fhirContext = fhirContext;
    }

    protected LibraryProcessor getLibraryProcessor() {
        return libraryProcessor;
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    protected void copyResourcesToTargetRefreshLibrariesDir(String targetDirectory, String resourceDirectory) throws IOException {
        File outputDirectory = new File(targetDirectory);
        outputDirectory.mkdirs();
        URL url = LibraryProcessorTest.class.getResource(resourceDirectory);
        String path = url.getPath();
        File libraryResourceDirectory = new File(path);
        FileUtils.copyDirectory(libraryResourceDirectory, outputDirectory);
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
        params.ini = targetDirectory + "/ig.ini";
        params.versioned = versioned;
        getLibraryProcessor().refreshLibraryContent(params);
    }

    protected void validateCqfmSofwareSystemExtension(String libraryResourcePath) {
        IBaseResource resource = IOUtils.readResource(libraryResourcePath, getFhirContext());
        if (resource == null || !(resource instanceof Library)) {
            // log error
        } else {
            Library library = (Library)resource;
            Extension softwareSystemExtension = library.getExtensionByUrl(cqfmSoftwareSystemExtensionUrl);
            assertNotNull(softwareSystemExtension);
        }
    }
}
