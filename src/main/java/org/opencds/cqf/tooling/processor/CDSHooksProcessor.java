package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;
/**
 * @author Adam Stevenson
 */
public class CDSHooksProcessor {
    public static final String requestsPathElement = "input/pagecontent/requests/";  
    public static final String responsesPathElement = "input/pagecontent/responses/";   
    public static final String requestFilesPathElement = "requests/";  
    public static final String responseFilesPathElement = "responses/"; 
    public void addRequestAndResponseFilesToBundle(String igPath, String bundleDestPath, String libraryName) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + IGBundleProcessor.bundleFilesPathElement);
        String requestFilesPath = FilenameUtils.concat(igPath, requestsPathElement);
        String responseFilesPath = FilenameUtils.concat(igPath, responsesPathElement);
        String requestFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, requestFilesPathElement);
        IOUtils.initializeDirectory(requestFilesDirectory);
        String responseFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, responseFilesPathElement);
        IOUtils.initializeDirectory(responseFilesDirectory);
        List<String> requestDirectories = IOUtils.getDirectoryPaths(requestFilesPath, false);
        for (String dir : requestDirectories) {
            if (dir.endsWith(libraryName)) {
                List<String> requestPaths = IOUtils.getFilePaths(dir, true);
                for (String path : requestPaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(requestFilesDirectory, FilenameUtils.getName(path)));
                }
            }
        }
        List<String> responseDirectories = IOUtils.getDirectoryPaths(responseFilesPath, false);
        for (String dir : responseDirectories) {
            if (dir.endsWith(libraryName)) {
                List<String> responsePaths = IOUtils.getFilePaths(dir, true);
                for (String path : responsePaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(responseFilesDirectory, FilenameUtils.getName(path)));
                }
            } 
        }
    }

    public static List<String> bundleActivityDefinitions(String planDefinitionPath, FhirContext fhirContext, Map<String, IBaseResource> resources,
    Encoding encoding, Boolean includeVersion, Boolean shouldPersist) {
        List<String> activityDefinitionPaths = new ArrayList<String>();
        try {
            Map<String, IBaseResource> activityDefinitions = ResourceUtils.getActivityDefinitionResources(planDefinitionPath, fhirContext, includeVersion);
            for (Entry<String, IBaseResource> entry : activityDefinitions.entrySet()) {
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
        String bundleDestFilesPath =
        FilenameUtils.concat(bundleDestPath, FilenameUtils.getBaseName(bundleDestPath) + "-" + IGBundleProcessor.bundleFilesPathElement);
                for (String path : activityDefinitionPaths) {
                    IOUtils.copyFile(path, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(path)));
                }
            }
    }