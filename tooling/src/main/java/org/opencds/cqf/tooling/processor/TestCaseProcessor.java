package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.api.IFhirVersion;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TestCaseProcessor {
    public static final String NEWLINE_INDENT = "\r\n\t";
    public static final String NEWLINE = "\r\n";

    public static final String separator = System.getProperty("file.separator");
    private static final Logger logger = LoggerFactory.getLogger(TestCaseProcessor.class);

    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, Boolean includeErrors) {
        refreshTestCases(path, encoding, fhirContext, null, includeErrors);
    }

    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, @Nullable List<String> refreshedResourcesNames,
                                 Boolean includeErrors) {
        System.out.println("\r\n[Refreshing Tests]\r\n");


        final Map<String, String> testCaseRefreshSuccessMap = new ConcurrentHashMap<>();
        final Map<String, String> testCaseRefreshFailMap = new ConcurrentHashMap<>();
        final Map<String, String> groupFileRefreshSuccessMap = new ConcurrentHashMap<>();
        final Map<String, String> groupFileRefreshFailMap = new ConcurrentHashMap<>();

        final List<Callable<Void>> testCaseRefreshTasks = new CopyOnWriteArrayList<>();
        IFhirVersion version = fhirContext.getVersion();
        //build list of tasks via for loop:
        List<Callable<Void>> testGroupTasks = new ArrayList<>();
        ExecutorService testGroupExecutor = Executors.newCachedThreadPool();
        List<String> resourceTypeTestGroups = IOUtils.getDirectoryPaths(path, false);
        for (String group : resourceTypeTestGroups) {
            testGroupTasks.add(() -> {
                List<String> testArtifactPaths = IOUtils.getDirectoryPaths(group, false);

                //build list of tasks via for loop:
                List<Callable<Void>> testArtifactTasks = new CopyOnWriteArrayList<>();
                ExecutorService testArtifactExecutor = Executors.newCachedThreadPool();

                for (String testArtifactPath : testArtifactPaths) {
                    testArtifactTasks.add(() -> {
                        List<String> testCasePaths = IOUtils.getDirectoryPaths(testArtifactPath, false);

                        org.hl7.fhir.r4.model.Group testGroup;
                        if (version.getVersion() == FhirVersionEnum.R4) {
                            testGroup = new org.hl7.fhir.r4.model.Group();
                            testGroup.setActive(true);
                            testGroup.setType(Group.GroupType.PERSON);
                            testGroup.setActual(true);
                        } else {
                            testGroup = null;
                        }

                        // For each test case we need to create a group
                        if (!testCasePaths.isEmpty()) {
                            String measureName = IOUtils.getMeasureTestDirectory(testCasePaths.get(0));

                            if (testGroup != null) {
                                testGroup.setId(measureName);

                                testGroup.addExtension("http://hl7.org/fhir/StructureDefinition/artifact-testArtifact",
                                        new CanonicalType("http://ecqi.healthit.gov/ecqms/Measure/" + measureName));
                            }

                            for (String testCasePath : testCasePaths) {
                                testCaseRefreshTasks.add(() -> {
                                    try {
                                        List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                                        List<IBaseResource> resources = IOUtils.readResources(paths, fhirContext);
                                        ensureIds(testCasePath, resources);

                                        // Loop through resources and any that are patients need to be added to the test Group
                                        // Handle individual resources when they exist
                                        for (IBaseResource resource : resources) {
                                            if ((resource.fhirType().equalsIgnoreCase("Patient")) && (version.getVersion() == FhirVersionEnum.R4)) {
                                                org.hl7.fhir.r4.model.Patient patient = (org.hl7.fhir.r4.model.Patient) resource;
                                                if (testGroup != null) {
                                                    addPatientToGroupR4(testGroup, patient);
                                                }
                                            }

                                            // Handle bundled resources when that is how they are provided
                                            if ((resource.fhirType().equalsIgnoreCase("Bundle")) && (version.getVersion() == FhirVersionEnum.R4)) {
                                                org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) resource;
                                                var bundleResources =
                                                        BundleUtils.getR4ResourcesFromBundle(bundle);
                                                for (IBaseResource bundleResource : bundleResources) {
                                                    if (bundleResource.fhirType().equalsIgnoreCase("Patient")) {
                                                        org.hl7.fhir.r4.model.Patient patient = (org.hl7.fhir.r4.model.Patient) bundleResource;
                                                        if (testGroup != null) {
                                                            addPatientToGroupR4(testGroup, patient);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // If the resource is a transaction bundle then don't bundle it again otherwise do
                                        String fileId = getId(FilenameUtils.getName(testCasePath));
                                        Object bundle;
                                        if ((resources.size() == 1) && (BundleUtils.resourceIsABundle(resources.get(0)))) {
                                            bundle = processTestBundle(fileId, resources.get(0), fhirContext, testArtifactPath, testCasePath);
                                        } else {
                                            bundle = BundleUtils.bundleArtifacts(fileId, resources, fhirContext, false);
                                        }
                                        IOUtils.writeBundle(bundle, testArtifactPath, encoding, fhirContext);

                                    } catch (Exception e) {
                                        testCaseRefreshFailMap.put(testCasePath, e.getMessage());
                                    }


                                    testCaseRefreshSuccessMap.put(testCasePath, "");
                                    reportProgress((testCaseRefreshFailMap.size() + testCaseRefreshSuccessMap.size()), testCaseRefreshTasks.size());
                                    //task requires return statement
                                    return null;
                                });
                            }//end for (String testCasePath : testCasePaths) {

                            // Need to output the Group if it exists
                            if (testGroup != null) {
                                String groupFileName = "Group-" + measureName;
                                String groupFileIdentifier = testArtifactPath + separator + groupFileName;

                                try {
                                    IOUtils.writeResource(testGroup, testArtifactPath, encoding, fhirContext, true,
                                            groupFileName);

                                    groupFileRefreshSuccessMap.put(groupFileIdentifier, "");

                                } catch (Exception e) {

                                    groupFileRefreshFailMap.put(groupFileIdentifier, e.getMessage());
                                }

                            }
                        }
                        //task requires return statement
                        return null;
                    });
                }//
                ThreadUtils.executeTasks(testArtifactTasks, testArtifactExecutor);

                //task requires return statement
                return null;
            });
        }//end for (String group : resourceTypeTestGroups) {
        ThreadUtils.executeTasks(testGroupTasks, testGroupExecutor);
        //Now with all possible tasks collected, progress can be reported instead of flooding the console.
        ThreadUtils.executeTasks(testCaseRefreshTasks);
        //ensure accurate progress at final stage:
        reportProgress((testCaseRefreshFailMap.size() + testCaseRefreshSuccessMap.size()), testCaseRefreshTasks.size());

        StringBuilder testCaseMessage = buildInformationMessage(testCaseRefreshFailMap, testCaseRefreshSuccessMap, "Test Case", "Refreshed", includeErrors);
        if (!groupFileRefreshSuccessMap.isEmpty() || !groupFileRefreshFailMap.isEmpty()) {
            testCaseMessage.append(buildInformationMessage(groupFileRefreshFailMap, groupFileRefreshSuccessMap, "Group File", "Created", includeErrors));
        }
        System.out.println(testCaseMessage);
    }

    /**
     * Gives the user a nice report at the end of the refresh test case process (used to report group file status as well)
     *
     * @param failMap       which items failed
     * @param successMap    which items succeeded
     * @param type          group file or test case
     * @param successType   created or refreshed
     * @param includeErrors give the exception message if includeErrors is on
     * @return built message for console
     */
    private StringBuilder buildInformationMessage(Map<String, String> failMap, Map<String, String> successMap, String type, String successType, boolean includeErrors) {
        StringBuilder message = new StringBuilder();
        if (!successMap.isEmpty() || !failMap.isEmpty()) {
            message.append(NEWLINE).append(successMap.size()).append(" ").append(type).append("(s) successfully ").append(successType.toLowerCase()).append(":");
            for (String refreshedTestCase : successMap.keySet()) {
                message.append(NEWLINE_INDENT).append(refreshedTestCase).append(" ").append(successType.toUpperCase());
            }
            if (!failMap.isEmpty()) {
                message.append(NEWLINE).append(failMap.size()).append(" ").append(type).append("(s) failed to be ").append(successType.toLowerCase()).append(":");
                for (String failed : failMap.keySet()) {
                    message.append(NEWLINE_INDENT).append(failed).append(" FAILED").append(includeErrors ? ": " + failMap.get(failed) : "");
                }
            }
        }
        return message;
    }

    private void reportProgress(int count, int total) {
        double percentage = (double) count / total * 100;
        System.out.print("\rTest Refresh: " + String.format("%.2f%%", percentage) + " processed.");
    }

    public static Object processTestBundle(String id, IBaseResource resource, FhirContext fhirContext, String testArtifactPath, String testCasePath) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                org.hl7.fhir.dstu3.model.Bundle dstu3Bundle = (org.hl7.fhir.dstu3.model.Bundle) resource;
                ResourceUtils.setIgId(id, dstu3Bundle, false);
                dstu3Bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);

                for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : dstu3Bundle.getEntry()) {
                    org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent request = new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent();
                    request.setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT);
                    request.setUrl(entry.getResource().fhirType() + "/" + entry.getResource().getIdElement().getIdPart());
                    entry.setRequest(request);
                }

                return dstu3Bundle;

            case R4:
                org.hl7.fhir.r4.model.Bundle r4Bundle = (org.hl7.fhir.r4.model.Bundle) resource;
                ResourceUtils.setIgId(id, r4Bundle, false);
                r4Bundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);
                for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : r4Bundle.getEntry()) {
                    org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent request = new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent();
                    request.setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.PUT); // Adjust the HTTP method as needed
                    request.setUrl(entry.getResource().fhirType() + "/" + entry.getResource().getIdElement().getIdPart());
                    entry.setRequest(request);
                }

                return r4Bundle;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }


    private void addPatientToGroupR4(Group group, org.hl7.fhir.r4.model.Patient patient) {
        IdType idType = patient.getIdElement();
        org.hl7.fhir.r4.model.Group.GroupMemberComponent member = group.addMember();
        org.hl7.fhir.r4.model.Reference patientRef = new Reference();
        patientRef.setReference("Patient/" + idType.getIdPart());

        // Get name for display value
        org.hl7.fhir.r4.model.HumanName name = patient.getName().get(0);
        patientRef.setDisplay(name.getNameAsSingleString());

        member.setEntity(patientRef);
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
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.TEST_CASE_PATH_ELEMENT), contextResourceType), libraryName);

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
            logger.error(igTestCasePath, e);
        }
        return shouldPersist;
    }


    //TODO: the bundle needs to have -expectedresults added too

    /**
     * Bundles test case files from the specified path into a destination path.
     * The method copies relevant test case files, including expected results for MeasureReports,
     * and returns a summary message with the number of files copied.
     *
     * @param igPath              The path to the Implementation Guide (IG) containing test case files.
     * @param contextResourceType The resource type associated with the test cases.
     * @param libraryName         The name of the library associated with the test cases.
     * @param destPath            The destination path for the bundled test case files.
     * @param fhirContext         The FHIR context used for reading and processing resources.
     */
    public static void bundleTestCaseFiles(String igPath, String contextResourceType, String libraryName, String destPath, FhirContext fhirContext) {
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.TEST_CASE_PATH_ELEMENT), contextResourceType), libraryName);
        List<String> testCasePaths = IOUtils.getFilePaths(igTestCasePath, false);
        for (String testPath : testCasePaths) {
            String bundleTestDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testPath));
            IOUtils.copyFile(testPath, bundleTestDestPath);
        }
    }

}