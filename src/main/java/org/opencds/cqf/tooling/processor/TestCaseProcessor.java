package org.opencds.cqf.tooling.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class TestCaseProcessor {
    private final static String separator = System.getProperty("file.separator");
    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, String rootDir) {
        refreshTestCases(path, encoding, fhirContext, null, rootDir);
    }

    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, @Nullable List<String> refreshedResourcesNames, String rootDir) {
        System.out.println("Refreshing tests");
        List<String> resourceTypeTestGroups = IOUtils.getDirectoryPaths(path, false);

        for (String group : resourceTypeTestGroups) {
            List<String> testArtifactPaths = IOUtils.getDirectoryPaths(group, false);
            for (String testArtifactPath : testArtifactPaths) {
                List<String> testCasePaths = IOUtils.getDirectoryPaths(testArtifactPath, false);
                for (String testCasePath : testCasePaths) {
                    try {
                        List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                        List<IBaseResource> resources = IOUtils.readResources(paths, fhirContext);
                        ensureIds(testCasePath, resources);
                        Object bundle = BundleUtils.bundleArtifacts(getId(FilenameUtils.getName(testCasePath)), resources, fhirContext);
                        String outputPath = getOutputPath(rootDir, testArtifactPath, group);
                        IOUtils.writeBundle(bundle, outputPath, encoding, fhirContext);
                    } catch (Exception e) {
                        LogUtils.putException(testCasePath, e);
                    } finally {
                        LogUtils.warn(testCasePath);
                    }
                }
            }
        }
    }

    private String getOutputPath(String rootDir, String testArtifactPath, String group) {
        String outputPath = FilenameUtils.normalize(rootDir) +
                separator + "bundles" + separator + "tests" + separator + FilenameUtils.getName(group) + separator +
                FilenameUtils.getName(testArtifactPath);
        return outputPath;
    }

    public static void removeTestBundleFiles(String rootDir){
        String testBundleDir = FilenameUtils.normalize(rootDir) + FilenameUtils.normalize("/bundles/tests");
        try {
            IOUtils.deleteDirectory(testBundleDir);
        } catch (IOException e) {
            System.out.println("Unable to delete the directory" + testBundleDir);
            e.printStackTrace();
        }
    }

    public static List<IBaseResource> getTestCaseResources(String path, FhirContext fhirContext) {
        List<IBaseResource> resources = new ArrayList<IBaseResource>();
        List<String> testCasePaths = IOUtils.getDirectoryPaths(path, false);
        for (String testCasePath : testCasePaths) {
            List<String> paths = IOUtils.getFilePaths(testCasePath, true);
            resources.addAll(ensureIds(testCasePath, IOUtils.readResources(paths, fhirContext)));
        }
        return resources;
    }

    private static List<IBaseResource> ensureIds(String baseId, List<IBaseResource> resources) {
        for (IBaseResource resource : resources) {
            if (resource.getIdElement().getIdPart() == null || resource.getIdElement().getIdPart().equals("")) {
                ResourceUtils.setIgId(FilenameUtils.getName(baseId), resource, false);
                resource.setId(resource.getClass().getSimpleName() + "/" + resource.getIdElement().getIdPart());
            }
        }
        return resources;
    }

    public static String getId(String baseId) {
        return "tests-" + baseId;
    }

    public static Boolean bundleTestCases(String igPath, String contextResourceType, String libraryName, FhirContext fhirContext,
                                          Map<String, IBaseResource> resources) {
        Boolean shouldPersist = true;
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), contextResourceType), libraryName);

        // this is breaking for bundle of a bundle. Replace with individual resources
        // until we can figure it out.
        // List<String> testCaseSourcePaths = IOUtils.getFilePaths(igTestCasePath,
        // false);
        // for (String testCaseSourcePath : testCaseSourcePaths) {
        // shouldPersist = shouldPersist & safeAddResource(testCaseSourcePath,
        // resources, fhirContext);
        // }

        try {
            List<IBaseResource> testCaseResources = TestCaseProcessor.getTestCaseResources(igTestCasePath, fhirContext);
            for (IBaseResource resource : testCaseResources) {
                if ((!(resource instanceof org.hl7.fhir.dstu3.model.Bundle)) && (!(resource instanceof org.hl7.fhir.r4.model.Bundle))) {
                    resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);
                }
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putException(igTestCasePath, e);
        }
        return shouldPersist;
    }

    //TODO: the bundle needs to have -expectedresults added too
    public static void bundleTestCaseFiles(String igPath, String contextResourceType, String libraryName, String destPath, FhirContext fhirContext) {
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), contextResourceType), libraryName);
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
                        IBaseResource measureReport = IOUtils.readResource(testContentPath, fhirContext);
                        if (!measureReport.getIdElement().getIdPart().startsWith("measurereport") || !measureReport.getIdElement().getIdPart().endsWith("-expectedresults")) {
                            Object measureReportStatus = ResourceUtils.resolveProperty(measureReport, "status", fhirContext);
                            String measureReportStatusValue = ResourceUtils.resolveProperty(measureReportStatus, "value", fhirContext).toString();
                            if (measureReportStatusValue.equals("COMPLETE")) {
                                String expectedResultsId = FilenameUtils.getBaseName(testContentPath) + (FilenameUtils.getBaseName(testContentPath).endsWith("-expectedresults") ? "" : "-expectedresults");
                                measureReport.setId(expectedResultsId);
                            }
                        }
                        IOUtils.writeResource(measureReport, destPath, IOUtils.Encoding.JSON, fhirContext);
                    } else {
                        String bundleTestContentDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testContentPath));
                        IOUtils.copyFile(testContentPath, bundleTestContentDestPath);
                    }
                }
            }
        }
        String bundledTestsDir = FilenameUtils.normalize(igPath) + separator +
                "bundles" + separator +
                "tests" + separator +
                contextResourceType + separator +
                libraryName;
        List<String> testContentPaths = IOUtils.getFilePaths(bundledTestsDir, false);
        for (String testContentPath : testContentPaths) {
            String bundleTestContentDestPath = new StringBuilder().append(FilenameUtils.normalize(igPath)).append(separator)
                    .append("bundles").append(separator)
                    .append(contextResourceType).append(separator)
                    .append(libraryName).append(separator)
                    .append(libraryName).append("-").append(IGBundleProcessor.bundleFilesPathElement)
                    .append(FilenameUtils.getName(testContentPath)).toString();
            IOUtils.copyFile(testContentPath, bundleTestContentDestPath);

        }
    }
}
