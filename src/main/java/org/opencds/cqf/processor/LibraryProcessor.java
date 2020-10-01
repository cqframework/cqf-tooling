package org.opencds.cqf.processor;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.parameter.RefreshLibraryParameters;
import org.opencds.cqf.utilities.IOUtils.Encoding;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public interface LibraryProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    public static Boolean bundleLibraryDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources,
            Encoding encoding, Boolean includeVersion) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding, includeVersion);
            String currentResourceID = FilenameUtils.getBaseName(path);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);

                // NOTE: Assuming dependency library will be in directory of dependent.
                String dependencyPath;
                // Issue 96 - Do not include version number in filename
                if (includeVersion) {
                	dependencyPath = path.replace(currentResourceID, resource.getId().replace("Library/", ""));
                } else {
                	String resourceId = resource.getId().replace("Library/", "");
                	dependencyPath = path.replace(currentResourceID, resourceId.split("-")[0]);
                }
                bundleLibraryDependencies(dependencyPath, fhirContext, resources, encoding, includeVersion);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(path, e);
        }
        return shouldPersist;
    }

    public Boolean refreshLibraryContent(RefreshLibraryParameters params);
}