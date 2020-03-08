package org.opencds.cqf.processor;

import java.util.Map;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.ResourceUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public interface LibraryProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    public static Boolean bundleLibraryDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources,
            Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putWarning(path, e.getMessage());
        }
        return shouldPersist;
    }

    public Boolean refreshLibraryContent(String cqlContentPath, String libraryPath, FhirContext fhirContext, Encoding encoding, Boolean includeVersion);
}