package org.opencds.cqf.testcase;

import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseProcessor
{         
    public static void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext)
    {
        List<String> libaryTestCasePaths = IOUtils.getDirectoryPaths(path, false); 
        for (String libraryTestCasePath : libaryTestCasePaths) {
            List<String> testCasePaths = IOUtils.getDirectoryPaths(libraryTestCasePath, false); 
            for (String testCasePath : testCasePaths) {
                List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                List<IAnyResource> resources = IOUtils.readResources(paths, fhirContext);
                Object bundle = BundleUtils.bundleArtifacts(getId(FilenameUtils.getName(testCasePath)), resources, fhirContext);
                IOUtils.writeBundle(bundle, libraryTestCasePath, encoding, fhirContext);
            }
        }        
    }

    public static String getId(String baseId) {
        return "tests-" + baseId;
    }
}
