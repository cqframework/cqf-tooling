package org.opencds.cqf.testcase;

import java.util.*;

import org.hl7.fhir.instance.model.api.IAnyResource;
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
    public static void refreshTestCases(FhirContext fhirContext, String path)
    {
        //TODO: this is a stub

        List<String> paths = IOUtils.getFilePaths(path, true);
        List<IAnyResource> resources = IOUtils.readResources(paths, fhirContext);
    }
}
