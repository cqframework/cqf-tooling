package org.opencds.cqf.tooling;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.constants.CrmiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public abstract class RefreshTest implements SoftwareSystemTest {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTest.class);

    private final FhirContext fhirContext;
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
        if (!Strings.isNullOrEmpty(testName)) {
            logger.info("Beginning Test: " + testName);
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (!Strings.isNullOrEmpty(testName)) {
            logger.info("Finished Test: " + testName);
        }
    }

    protected void validateSoftwareSystemExtension(String domainResourcePath) {
        IBaseResource resource = IOUtils.readResource(domainResourcePath, getFhirContext());
        if (!(resource instanceof Library)) {
            logger.warn("Expected Library resource, found {}", resource.fhirType());
        } else {
            DomainResource domainResource = (DomainResource)resource;
            Extension softwareSystemExtension = domainResource.getExtensionByUrl(CrmiConstants.SOFTWARE_SYSTEM_EXT_URL);
            assertNotNull(softwareSystemExtension);
        }
    }

    protected void validateNoCqfmSoftwareSystemExtension(String domainResourcePath) {
        IBaseResource resource = IOUtils.readResource(domainResourcePath, getFhirContext());
        if (!(resource instanceof Library)) {
            logger.warn("Expected Library resource, found {}", resource.fhirType());
        } else {
            DomainResource domainResource = (DomainResource)resource;
            Extension softwareSystemExtension = domainResource.getExtensionByUrl(CrmiConstants.SOFTWARE_SYSTEM_EXT_URL);
            assertNull(softwareSystemExtension);
        }
    }

    protected void copyResourcesToTargetDir(String targetDirectory, String resourceDirectoryPath) throws IOException {
        File outputDirectory = new File(targetDirectory);
        outputDirectory.mkdirs();
        URL url = RefreshTest.class.getResource(resourceDirectoryPath);
        File resourceDirectory = FileUtils.toFile(url);
        FileUtils.copyDirectory(resourceDirectory, outputDirectory);
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }
}
