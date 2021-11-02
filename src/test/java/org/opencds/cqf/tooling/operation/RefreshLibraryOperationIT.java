package org.opencds.cqf.tooling.operation;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RefreshLibraryOperationIT {
    private String cqfmSoftwareSystemExtensionUrl = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";
    private FhirContext fhirContext = FhirContext.forCached(FhirVersionEnum.R4);

    @BeforeMethod
    public void setUp() throws Exception {
        File dir  = new File("target/refreshLibraries");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
    
    @Test
    private void testRefreshOverwriteLibraries() throws Exception {
        String targetDirectory = "./target/refreshLibraries/r4";
        copyResourcesToTargetRefreshLibrariesDir(targetDirectory, "r4");
        
        String libraryPath = "/input/resources/library/library-EXM124_FHIR4-8.2.000.json";

		String args[] = {
            "-RefreshLibrary",
            "-ini=" + targetDirectory + "/ig.ini",
            "-lp=" + targetDirectory + libraryPath,
            "-cql=" + targetDirectory + "/input/pagecontent/cql/EXM124_FHIR4-8.2.000.cql",
            "-e=json",
            "-fv=4.0.1" 
        };

        RefreshLibraryOperation refreshLibraryOperation = new RefreshLibraryOperation();
        refreshLibraryOperation.execute(args);

        validateCqfmSofwareSystemExtension(targetDirectory + libraryPath);
    }

    @Test
    private void testRefreshOutputDirectory() throws Exception {
        // create a output directory under target directory
        File targetDirectory = new File("./target/refreshLibraries/" + "r4");
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        String resourceDirPath = RefreshLibraryOperationIT.class.getResource("r4").getPath();
        assertTrue(targetDirectory.listFiles().length == 0);

        String libraryPath = "/input/resources/library/library-EXM124_FHIR4-8.2.000.json";

		String args[] = {
            "-RefreshLibrary",
            "-ini=" + resourceDirPath + "/ig.ini",
            "-lp=" + resourceDirPath + libraryPath,
            "-lop=" + targetDirectory.getAbsolutePath(),
            "-cql=" + resourceDirPath + "/input/pagecontent/cql/EXM124_FHIR4-8.2.000.cql",
            "-e=json",
            "-fv=4.0.1" 
        };

        RefreshLibraryOperation refreshLibraryOperation = new RefreshLibraryOperation();
        refreshLibraryOperation.execute(args);

        assertTrue(targetDirectory.listFiles().length > 0);
    }

    private void copyResourcesToTargetRefreshLibrariesDir(String targetDirectory, String resourceDirectory) throws IOException {
        File outputDirectory = new File(targetDirectory);
        outputDirectory.mkdirs();
        URL url = RefreshLibraryOperationIT.class.getResource(resourceDirectory);
        String path = url.getPath();
        File libraryResourceDirectory = new File(path);
        FileUtils.copyDirectory(libraryResourceDirectory, outputDirectory);
    }

    private void validateCqfmSofwareSystemExtension(String libraryResourcePath) {
        IBaseResource resource = IOUtils.readResource(libraryResourcePath, fhirContext);
        if (resource == null || !(resource instanceof Library)) {
            // log error
        } else {
            Library library = (Library)resource;
            Extension softwareSystemExtension = library.getExtensionByUrl(cqfmSoftwareSystemExtensionUrl);
            assertNotNull(softwareSystemExtension);
        }
    }
}
