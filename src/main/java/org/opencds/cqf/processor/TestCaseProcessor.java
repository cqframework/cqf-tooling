package org.opencds.cqf.processor;

import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.ResourceUtils;

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
                ensureIds(testCasePath, resources);
                Object bundle = BundleUtils.bundleArtifacts(getId(FilenameUtils.getName(testCasePath)), resources, fhirContext);
                IOUtils.writeBundle(bundle, libraryTestCasePath, encoding, fhirContext);
            }
        }        
    }

    public static List<IAnyResource> getTestCaseResources(String path, FhirContext fhirContext)
    {
        List<IAnyResource> resources = new ArrayList<IAnyResource>();
        List<String> testCasePaths = IOUtils.getDirectoryPaths(path, false); 
        for (String testCasePath : testCasePaths) {
            List<String> paths = IOUtils.getFilePaths(testCasePath, true);
            resources.addAll(ensureIds(testCasePath, IOUtils.readResources(paths, fhirContext)));
        }         
        return resources; 
    }

    private static List<IAnyResource> ensureIds(String baseId, List<IAnyResource> resources) {
        for (IAnyResource resource : resources) {
            if (resource.getId() == null || resource.getId().equals("")) {
                ResourceUtils.setIgId(FilenameUtils.getName(baseId), resource, false);
                resource.setId(resource.getClass().getSimpleName() + "/" + resource.getId());
            }
        }
        return resources;
    }

    public static String getId(String baseId) {
        return "tests-" + baseId;
    }
}
