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
import org.opencds.cqf.tooling.operation.RefreshIGOperation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestCaseProcessor {
    private final static Logger logger = LoggerFactory.getLogger(TestCaseProcessor.class);
    public static final String NEWLINE_INDENT = "\r\n\t";
    public static final String NEWLINE_INDENT2 = "\r\n\t\t";
    public static final String NEWLINE = "\r\n";

    public static final String separator = System.getProperty("file.separator");
    public static final String TEST_ARTIFACT_URL = "http://hl7.org/fhir/StructureDefinition/artifact-testArtifact";
    public static final String MEASURE_URL = "http://ecqi.healthit.gov/ecqms/Measure/";
    public static final String PATIENT_TYPE = "Patient";
    public static final String BUNDLE_TYPE = "Bundle";
    public static final String GROUP_FILE_SEPARATOR = "Group-";

    private Map<String, String> getIgnoredTestList() {
        Map<String, String> ignoredTestList = new HashMap<>();
        File ignoreTestsFile = new File("ignore_tests.txt");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("ignore_tests.txt")))) {
            for (String input : bufferedReader.lines().collect(Collectors.toList())) {
                try {
                    int startIndex = input.indexOf("tests-") + "tests-".length();
                    int endIndex = input.indexOf("-bundle.json");
                    String uuid = input.substring(startIndex, endIndex);

                    int hapiIndex = input.indexOf("HAPI-");
                    String hapiMessage = input.substring(hapiIndex);

                    ignoredTestList.put(uuid, hapiMessage);
                } catch (Exception e) {
                    //parsing failed, that's ok, continue through the loop
                }
            }
        } catch (Exception e) {
            //no file exists, that's ok. ignoredTestList is simply blank and ignored later.
        }
        return ignoredTestList;
    }

    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, Boolean verboseMessaging) {
        refreshTestCases(path, encoding, fhirContext, null, verboseMessaging);
    }

    public void refreshTestCases(String path, IOUtils.Encoding encoding, FhirContext fhirContext, @Nullable List<String> refreshedResourcesNames,
                                 Boolean verboseMessaging) {
        logger.info("[Refreshing Tests]");

        final Map<String, String> testCaseRefreshSuccessMap = new HashMap<>();
        final Map<String, String> testCaseRefreshFailMap = new HashMap<>();

        final Map<String, String> groupFileRefreshSuccessMap = new HashMap<>();
        final Map<String, String> groupFileRefreshFailMap = new HashMap<>();

        IFhirVersion version = fhirContext.getVersion();

        final int totalTestFileCount = getTestFileCount(path);

        //build list of tasks via for loop:
        List<String> resourceTypeTestGroups = IOUtils.getDirectoryPaths(path, false);

        Map<String, String> ignoredTestsList = getIgnoredTestList();
        final Map<String, Map<String, String>> testCaseRefreshIgnoredMap = new HashMap<>();

        for (String group : resourceTypeTestGroups) {

            List<String> testArtifactPaths = IOUtils.getDirectoryPaths(group, false);

            for (String testArtifactPath : testArtifactPaths) {

                String artifact = FilenameUtils.getName(testArtifactPath);

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

                        testGroup.addExtension(TEST_ARTIFACT_URL,
                                new CanonicalType(MEASURE_URL + measureName));
                    }

                    for (String testCasePath : testCasePaths) {
                        reportProgress((testCaseRefreshSuccessMap.size() + testCaseRefreshFailMap.size()), totalTestFileCount);

                        try {
                            List<String> paths = IOUtils.getFilePaths(testCasePath, true);
                            String testCase = FilenameUtils.getName(testCasePath);
                            String fileId = getId(testCase);

                            //ignore the test if specified in ignore_tests.txt in root dir:
                            if (ignoredTestsList.containsKey(testCase)) {
                                Map<String, String> artifactTestsIgnoredMap;
                                //try to group the tests by artifact:
                                if (testCaseRefreshIgnoredMap.containsKey(artifact)) {
                                    artifactTestsIgnoredMap = new HashMap<>(testCaseRefreshIgnoredMap.get(artifact));
                                } else {
                                    artifactTestsIgnoredMap = new HashMap<>();
                                }
                                //add the test case and reason specified in file:
                                StringBuilder pathsString = new StringBuilder();
//                                //attach actual filename after uuid for easier identification:
//                                for (String pathStr : paths){
//                                    pathsString.append("(")
//                                            .append(FilenameUtils.getName(pathStr))
//                                            .append(")");
//                                }
                                artifactTestsIgnoredMap.put(testCase + pathsString, ignoredTestsList.get(testCase));

                                //add this map to collection of maps:
                                testCaseRefreshIgnoredMap.put(artifact, artifactTestsIgnoredMap);

                                //try to delete any existing tests-* files that may have been created in previous tests:
                                File testCaseDeleteFile = new File(testArtifactPath + separator + fileId + "-bundle.json");
                                if (testCaseDeleteFile.exists()) {
                                    try {
                                        testCaseDeleteFile.delete();
                                    } catch (Exception e) {
                                        //something went wrong in deleting the old test file, it won't interfere with group file creation though
                                    }
                                }
                                continue;
                            }
                            List<IBaseResource> resources = IOUtils.readResources(paths, fhirContext);
                            ensureIds(testCasePath, resources);

                            // Loop through resources and any that are patients need to be added to the test Group
                            // Handle individual resources when they exist
                            for (IBaseResource resource : resources) {
                                if ((resource.fhirType().equalsIgnoreCase(PATIENT_TYPE)) && (version.getVersion() == FhirVersionEnum.R4)) {
                                    org.hl7.fhir.r4.model.Patient patient = (org.hl7.fhir.r4.model.Patient) resource;
                                    if (testGroup != null) {
                                        addPatientToGroupR4(testGroup, patient);
                                    }
                                }

                                // Handle bundled resources when that is how they are provided
                                if ((resource.fhirType().equalsIgnoreCase(BUNDLE_TYPE)) && (version.getVersion() == FhirVersionEnum.R4)) {
                                    org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) resource;
                                    var bundleResources =
                                            BundleUtils.getR4ResourcesFromBundle(bundle);
                                    for (IBaseResource bundleResource : bundleResources) {
                                        if (bundleResource.fhirType().equalsIgnoreCase(PATIENT_TYPE)) {
                                            org.hl7.fhir.r4.model.Patient patient = (org.hl7.fhir.r4.model.Patient) bundleResource;
                                            if (testGroup != null) {
                                                addPatientToGroupR4(testGroup, patient);
                                            }
                                        }
                                    }
                                }
                            }

                            // If the resource is a transaction bundle then don't bundle it again otherwise do
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
                    }

                    // Need to output the Group if it exists
                    if (testGroup != null) {
                        String groupFileName = GROUP_FILE_SEPARATOR + measureName;
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
            }
        }

        reportProgress((testCaseRefreshSuccessMap.size() + testCaseRefreshFailMap.size()), totalTestFileCount);

        StringBuilder testCaseMessage = buildInformationMessage(testCaseRefreshFailMap, testCaseRefreshSuccessMap, "Test Case", "Refreshed", verboseMessaging);

        if (!groupFileRefreshSuccessMap.isEmpty() || !groupFileRefreshFailMap.isEmpty()) {
            testCaseMessage.append(buildInformationMessage(groupFileRefreshFailMap, groupFileRefreshSuccessMap, "Group File", "Created", verboseMessaging));
        }

        //There were specified ignored tests, categorize by artifact:
        if (!testCaseRefreshIgnoredMap.isEmpty()) {
            int totalTestCaseIgnoredCount = 0;
            for (Map<String, String> testCaseMap : testCaseRefreshIgnoredMap.values()) {
                totalTestCaseIgnoredCount += testCaseMap.size();
            }
            testCaseMessage.append(NEWLINE).append(totalTestCaseIgnoredCount).append(" Test Case(s) were designated to be ignored:");
            for (String artifact : testCaseRefreshIgnoredMap.keySet()) {
                testCaseMessage.append(NEWLINE_INDENT)
                        .append(artifact)
                        .append(": ");

                Map<String, String> testCaseIgnored = testCaseRefreshIgnoredMap.get(artifact);

                for (Map.Entry<String, String> entry : testCaseIgnored.entrySet()) {
                    testCaseMessage.append(NEWLINE_INDENT2)
                            .append(entry.getKey())
                            .append(": ")
                            //get the reason specified by the ignore file:
                            .append(entry.getValue());
                }
            }
        }
        System.out.println(testCaseMessage);
    }

    private int getTestFileCount(String path) {
        int counter = 0;
        List<String> resourceTypeTestGroups = IOUtils.getDirectoryPaths(path, false);
        for (String group : resourceTypeTestGroups) {
            List<String> testArtifactPaths = IOUtils.getDirectoryPaths(group, false);
            for (String testArtifactPath : testArtifactPaths) {
                counter += IOUtils.getDirectoryPaths(testArtifactPath, false).size();
            }
        }
        return counter;
    }

    /**
     * Gives the user a nice report at the end of the refresh test case process (used to report group file status as well)
     *
     * @param failMap          which items failed
     * @param successMap       which items succeeded
     * @param type             group file or test case
     * @param successType      created or refreshed
     * @param verboseMessaging give the exception message if verboseMessaging is on
     * @return built message for console
     */
    private StringBuilder buildInformationMessage(Map<String, String> failMap, Map<String, String> successMap, String type, String successType, boolean verboseMessaging) {
        StringBuilder message = new StringBuilder();
        if (!successMap.isEmpty()) {
            message.append(NEWLINE).append(successMap.size()).append(" ").append(type).append("(s) successfully ").append(successType.toLowerCase()).append(":");
            List<String> successKeys = new ArrayList<>(successMap.keySet());
            Collections.sort(successKeys);
            for (String successCase : successKeys) {
                message.append(NEWLINE_INDENT).append(successCase).append(" ").append(successType.toUpperCase());
            }
        }
        if (!failMap.isEmpty()) {
            message.append(NEWLINE).append(failMap.size()).append(" ").append(type).append("(s) failed to be ").append(successType.toLowerCase()).append(":");
            List<String> failKeys = new ArrayList<>(failMap.keySet());
            Collections.sort(failKeys);
            for (String failEntry : failKeys) {
                message.append(NEWLINE_INDENT).append(failEntry).append(" FAILED").append(verboseMessaging ? ": " + failMap.get(failEntry) : "");
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

    public static void bundleTestCases(String igPath, String contextResourceType, String libraryName, FhirContext fhirContext,
                                       Map<String, IBaseResource> resources) throws Exception {
        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(igPath, IGProcessor.TEST_CASE_PATH_ELEMENT), contextResourceType), libraryName);

        // this is breaking for bundle of a bundle. Replace with individual resources
        // until we can figure it out.
        // List<String> testCaseSourcePaths = IOUtils.getFilePaths(igTestCasePath,
        // false);
        // for (String testCaseSourcePath : testCaseSourcePaths) {
        // shouldPersist = shouldPersist & safeAddResource(testCaseSourcePath,
        // resources, fhirContext);
        // }

        List<IBaseResource> testCaseResources = TestCaseProcessor.getTestCaseResources(igTestCasePath, fhirContext);
        for (IBaseResource resource : testCaseResources) {
            if ((!(resource instanceof org.hl7.fhir.dstu3.model.Bundle)) && (!(resource instanceof org.hl7.fhir.r4.model.Bundle))) {
                resources.putIfAbsent(resource.getIdElement().getIdPart(), resource);
            }
        }
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