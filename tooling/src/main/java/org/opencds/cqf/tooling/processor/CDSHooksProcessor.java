package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CDSHooksProcessor {
    public static final String requestsPathElement = "input/pagecontent/requests/";
    public static final String responsesPathElement = "input/pagecontent/responses/";
    public static final String requestFilesPathElement = "requests/";
    public static final String responseFilesPathElement = "responses/";
    public void addRequestAndResponseFilesToBundle(String igPath, String bundleDestPath, String libraryName) {
        var bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + IGBundleProcessor.bundleFilesPathElement);
        var requestFilesPath = FilenameUtils.concat(igPath, requestsPathElement);
        var responseFilesPath = FilenameUtils.concat(igPath, responsesPathElement);
        var requestFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, requestFilesPathElement);
        IOUtils.initializeDirectory(requestFilesDirectory);
        var responseFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, responseFilesPathElement);
        IOUtils.initializeDirectory(responseFilesDirectory);
        var requestDirectories = IOUtils.getDirectoryPaths(requestFilesPath, false);
        for (var dir : requestDirectories) {
            if (dir.endsWith(libraryName)) {
                var requestPaths = IOUtils.getFilePaths(dir, true);
                for (var path : requestPaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(requestFilesDirectory, FilenameUtils.getName(path)));
                }
            }
        }
        var responseDirectories = IOUtils.getDirectoryPaths(responseFilesPath, false);
        for (var dir : responseDirectories) {
            if (dir.endsWith(libraryName)) {
                var responsePaths = IOUtils.getFilePaths(dir, true);
                for (var path : responsePaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(responseFilesDirectory, FilenameUtils.getName(path)));
                }
            }
        }
    }

    public static List<String> bundleActivityDefinitions(String planDefinitionPath, FhirContext fhirContext, Map<String, IBaseResource> resources,
    Encoding encoding, Boolean includeVersion, Boolean shouldPersist) {
        var activityDefinitionPaths = new ArrayList<String>();
        try {
            var activityDefinitions = ResourceUtils.getActivityDefinitionResources(planDefinitionPath, fhirContext, includeVersion);
            for (var entry : activityDefinitions.entrySet()) {
                resources.putIfAbsent(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                activityDefinitionPaths.add(entry.getKey());
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(planDefinitionPath, e.getMessage());
        }
        return activityDefinitionPaths;
    }

    public void addActivityDefinitionFilesToBundle(String igPath, String bundleDestPath, List<String> activityDefinitionPaths,
    FhirContext fhirContext, Encoding encoding) {
        var bundleDestFilesPath =
        FilenameUtils.concat(bundleDestPath, FilenameUtils.getBaseName(bundleDestPath) + "-" + IGBundleProcessor.bundleFilesPathElement);
                for (var path : activityDefinitionPaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(path)));
                }
            }
    }
