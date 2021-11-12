package org.opencds.cqf.tooling;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ca.uhn.fhir.context.FhirContext;

import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public abstract class RefreshTest implements CqfmSoftwareSystemTest {
    private FhirContext fhirContext;
    private String testName;

    public RefreshTest(FhirContext fhirContext, String testName) {
        this.fhirContext = fhirContext;
        this.testName = testName;
    }

    public RefreshTest(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("Beginning Test: " + testName);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        System.out.println("Finished Test: " + testName);
    }
    
    protected void validateCqfmSofwareSystemExtension(String domainResourcePath) {
        IBaseResource resource = IOUtils.readResource(domainResourcePath, getFhirContext());
        if (resource == null || !(resource instanceof Library)) {
            // log error
        } else {
            DomainResource domainResource = (DomainResource)resource;
            Extension softwareSystemExtension = domainResource.getExtensionByUrl(cqfmSoftwareSystemExtensionUrl);
            assertNotNull(softwareSystemExtension);
        }
    }

    protected void copyResourcesToTargetDir(String targetDirectory, String resourceDirectoryPath) throws IOException {
        File outputDirectory = new File(targetDirectory);
        outputDirectory.mkdirs();
        URL url = RefreshTest.class.getResource(resourceDirectoryPath);
        String path = url.getPath();
        File resourceDirectory = new File(path);
        FileUtils.copyDirectory(resourceDirectory, outputDirectory);
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }
}
