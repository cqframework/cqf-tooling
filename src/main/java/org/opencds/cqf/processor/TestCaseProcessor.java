package org.opencds.cqf.processor;

import java.util.*;

import com.sun.istack.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseProcessor
{
    public static void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext) {
        refreshTestCases(path, encoding, fhirContext, null);
    }

    public static void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, @Nullable ArrayList<String> refreshedResourcesNames)
    {
        System.out.println("Refreshing tests");     
        List<String> libraryTestCasePaths = IOUtils.getDirectoryPaths(path, false);

        List<String> libraryTestCasePathsToBundle = new ArrayList<String>();
        if (refreshedResourcesNames != null && !refreshedResourcesNames.isEmpty()) {
            libraryTestCasePaths.removeIf(tcp -> !refreshedResourcesNames.contains(FilenameUtils.getName(tcp)));
        }

        for (String libraryTestCasePath : libraryTestCasePaths) {
            List<String> testCasePaths = IOUtils.getDirectoryPaths(libraryTestCasePath, false); 
            for (String testCasePath : testCasePaths) {
                try {
                List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                List<IAnyResource> resources = IOUtils.readResources(paths, fhirContext);
                ensureIds(testCasePath, resources);
                Object bundle = BundleUtils.bundleArtifacts(getId(FilenameUtils.getName(testCasePath)), resources, fhirContext);
                IOUtils.writeBundle(bundle, libraryTestCasePath, encoding, fhirContext);
                } catch (Exception e) {
                    LogUtils.putException(testCasePath, e);
                }
                finally {
                    LogUtils.warn(testCasePath);
                }
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

    public static Boolean bundleTestCases(String igPath, String libraryName, FhirContext fhirContext,
            Map<String, IAnyResource> resources) {
        Boolean shouldPersist = true;
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), libraryName);

        // this is breaking for bundle of a bundle. Replace with individual resources
        // until we can figure it out.
        // List<String> testCaseSourcePaths = IOUtils.getFilePaths(igTestCasePath,
        // false);
        // for (String testCaseSourcePath : testCaseSourcePaths) {
        // shouldPersist = shouldPersist & safeAddResource(testCaseSourcePath,
        // resources, fhirContext);
        // }

        try {
            List<IAnyResource> testCaseResources = TestCaseProcessor.getTestCaseResources(igTestCasePath, fhirContext);
            for (IAnyResource resource : testCaseResources) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(igTestCasePath, e);
        }
        return shouldPersist;
    }

    //TODO: the bundle needs to have -expectedresults added too
    public static void bundleTestCaseFiles(String igPath, String libraryName, String destPath, FhirContext fhirContext) {    
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), libraryName);
        List<String> testCasePaths = IOUtils.getFilePaths(igTestCasePath, false);
        for (String testPath : testCasePaths) {
            String bundleTestDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testPath));
            IOUtils.copyFile(testPath, bundleTestDestPath);

            List<String> testCaseDirectories = IOUtils.getDirectoryPaths(igTestCasePath, false);
            for (String testCaseDirectory : testCaseDirectories) {
                List<String> testContentPaths = IOUtils.getFilePaths(testCaseDirectory, false);
                for (String testContentPath : testContentPaths) {
                    Optional<String> matchingMeasureReportPath = IOUtils.getMeasureReportPaths(fhirContext).stream()
                        .filter(path -> path.equals(testContentPath))
                        .findFirst();
                    if (matchingMeasureReportPath.isPresent()) {
                        IAnyResource measureReport = IOUtils.readResource(testContentPath, fhirContext);
                        if (!measureReport.getId().startsWith("measurereport") || !measureReport.getId().endsWith("-expectedresults")) {
                            Object measureReportStatus = ResourceUtils.resolveProperty(measureReport, "status", fhirContext);
                            String measureReportStatusValue = ResourceUtils.resolveProperty(measureReportStatus, "value", fhirContext).toString();
                            if (measureReportStatusValue.equals("COMPLETE")) {
                                String expectedResultsId = FilenameUtils.getBaseName(testContentPath) + (FilenameUtils.getBaseName(testContentPath).endsWith("-expectedresults") ? "" : "-expectedresults");
                                measureReport.setId(expectedResultsId);
                            }
                        }
                        IOUtils.writeResource(measureReport, destPath, IOUtils.Encoding.JSON, fhirContext);
                    }
                    else {
                        String bundleTestContentDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testContentPath));
                        IOUtils.copyFile(testContentPath, bundleTestContentDestPath);
                    }
                }
            }            
        }
    }
}
