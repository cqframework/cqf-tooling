package org.opencds.cqf.tooling.processor;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public interface LibraryProcessor {
    public static final String ResourcePrefix = "library-";   
    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    public static Boolean bundleLibraryDependencies(String path, FhirContext fhirContext, Map<String, IBaseResource> resources,
            Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IBaseResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
            String currentResourceID = FilenameUtils.getBaseName(path);
            for (IBaseResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);

                // NOTE: Assuming dependency library will be in directory of dependent.
                String dependencyPath = path.replace(currentResourceID, resource.getIdElement().getIdPart().replace("Library/", ""));
                bundleLibraryDependencies(dependencyPath, fhirContext, resources, encoding);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(path, e);
        }
        return shouldPersist;
    }

    public Boolean refreshLibraryContent(RefreshLibraryParameters params);
}