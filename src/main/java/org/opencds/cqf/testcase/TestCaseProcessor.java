package org.opencds.cqf.testcase;

import java.util.*;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseProcessor
{    
    /*
        - iterate through the test case directories
        - collect all the resources
        - bundle them
        - write them out to the root of the test directory
            - with the name of the test case directory 
    */
    public static void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext)
    {
        List<String> libaryTestCasePaths = IOUtils.getDirectoryPaths(path, false); 
        for (String libraryTestCasePath : libaryTestCasePaths) {
            List<String> testCasePaths = IOUtils.getDirectoryPaths(libraryTestCasePath, false); 
            for (String testCasePath : testCasePaths) {
                List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                List<IAnyResource> resources = IOUtils.readResources(paths, fhirContext);
                Object bundle = BundleUtils.bundleArtifacts(testCasePath, resources, fhirContext);
                IOUtils.writeBundle(bundle, path, testCasePath, encoding, fhirContext);
            }
        }        
    }
}
