package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.common.BaseCqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.common.CqfmSoftwareSystem;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.parameter.TestIGParameters;
import org.opencds.cqf.tooling.utilities.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class IGTestProcessor extends BaseProcessor {

    public class TestCaseResultSummaryComparator implements Comparator<TestCaseResultSummary> {
        public int compare(TestCaseResultSummary o1, TestCaseResultSummary o2) {
            int value1 = o1.resourceTypeGroup.compareTo(o2.resourceTypeGroup);
            if (value1 == 0) {
                int value2 = o1.testArtifactName.compareTo(o2.testArtifactName);
                if (value2 == 0) {
                    return o1.testCaseID.compareTo(o2.testCaseID);
                } else {
                    return value2;
                }
            }
            return value1;
        }
    }

    public class TestCaseResultSummary {
        public TestCaseResultSummary(String resourceTypeGroup, String testArtifactName, String testCaseID) {
            this.resourceTypeGroup = resourceTypeGroup;
            this.testArtifactName = testArtifactName;
            this.testCaseID = testCaseID;
        }

        private String resourceTypeGroup;
        public String getResourceTypeGroup() { return this.resourceTypeGroup; }
        public void setResourceTypeGroup(String value) { this.resourceTypeGroup = value; }

        private String testArtifactName;
        public String getTestArtifactName() { return this.testArtifactName; }
        public void setTestArtifactName(String value) { this.testArtifactName = value; }

        private String testCaseID;
        public String getTestCaseID() { return this.testCaseID; }
        public void setTestCaseID(String value) { this.testCaseID = value; }

        private Boolean testPassed;
        public Boolean getTestPassed() { return this.testPassed; }
        public void setTestPassed(Boolean value) { this.testPassed = value; }

        private String message;
        public String getMessage() { return this.message; }
        public void setMessage(String value) { this.message = value; }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append((testPassed ? "PASSED - " : "FAILED - ") + resourceTypeGroup + " " + testArtifactName + " " + testCaseID +
                    (!testPassed && message != null && !message.isEmpty() ? " with message: " + message : ""));

            return builder.toString();
        }

    }

    private FhirContext fhirContext;

    private IBaseResource getServerMetadata(String testServerUri) {
        IBaseResource result = null;

        String path = testServerUri + "/metadata";
        try {
            String response = HttpClientUtils.get(path);

            if (response != null && !response.isEmpty()) {
                IParser parser = fhirContext.newJsonParser();
                result = parser.parseResource(response);
            }

            return result;
        }
        catch (Exception ex) {
            LogUtils.putException(String.format("Error retrieving metadata from: 's%'.", path), ex);
            //TODO: Error/Message handling
            return null;
        }
    }

    private CqfmSoftwareSystem getCqfRulerSoftwareSystem(String testServerUri) {
        CqfmSoftwareSystem softwareSystem = null;

        try {
            IBaseResource resource = getServerMetadata(testServerUri);

            RuntimeResourceDefinition capabilityStatementDefinition = (RuntimeResourceDefinition)ResourceUtils.getResourceDefinition(fhirContext, "CapabilityStatement");
            String compatibilityStatementClassName = capabilityStatementDefinition.getImplementingClass().getName();
            if (compatibilityStatementClassName.equals(resource.getClass().getName())) {

                String softwareName = null;
                String softwareVersion = null;

                if (fhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
                    org.hl7.fhir.dstu3.model.CapabilityStatement capabilityStatement = (org.hl7.fhir.dstu3.model.CapabilityStatement)resource;

                    org.hl7.fhir.dstu3.model.Extension softwareModuleExtension =
                        capabilityStatement.getSoftware()
                            .getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/capabilitystatement-softwareModule")
                            .stream()
                            .filter(extension -> !extension.getExtensionString("name").equals(BaseCqfmSoftwareSystemHelper.cqfRulerDeviceName))
                            .collect(Collectors.toList())
                            .get(0);

                    if (softwareModuleExtension != null) {
                        softwareVersion = softwareModuleExtension.getExtensionString("version");//.getValue().toString();
                    }
                } else if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
                    org.hl7.fhir.r4.model.CapabilityStatement capabilityStatement = (org.hl7.fhir.r4.model.CapabilityStatement)resource;

                    org.hl7.fhir.r4.model.Extension softwareModuleExtension =
                        capabilityStatement.getSoftware()
                            .getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/capabilitystatement-softwareModule")
                            .stream()
                            .filter(extension -> !extension.getExtensionString("name").equals(BaseCqfmSoftwareSystemHelper.cqfRulerDeviceName))
                            .collect(Collectors.toList())
                            .get(0);

                    if (softwareModuleExtension != null) {
                        softwareVersion = softwareModuleExtension.getExtensionString("version");//.getValue().toString();
                    }
                }

                if (softwareVersion != null && !softwareVersion.isEmpty()) {
                    softwareSystem = new CqfmSoftwareSystem(BaseCqfmSoftwareSystemHelper.cqfRulerDeviceName, softwareVersion);
                }
            }
        }
        catch (Exception ex) {
            LogUtils.putException(String.format("Error retrieving CapabilityStatement from: 's%'.", testServerUri), ex);
            //TODO: Error/Message handling
            return null;
        }

        return softwareSystem;
    }

    public void testIg(TestIGParameters params) {
        fhirContext = params.fhirContext;

        if (params.ini != null) {
            initialize(params.ini);
        }
        else {
            initialize(params.rootDir, params.igPath, fhirContext.getVersion().toString());
        }

        CqfmSoftwareSystem testTargetSoftwareSystem =  getCqfRulerSoftwareSystem(params.fhirServerUri);

        System.out.println("Running IG test cases...");

        File testCasesDirectory = new File(params.testCasesPath);
        if (!testCasesDirectory.isDirectory()) {
            throw new RuntimeException("The path to the test scripts must point to a directory");
        }

        // refresh/generate test bundles
        System.out.println("Refreshing test cases...");
        TestCaseProcessor testCaseProcessor = new TestCaseProcessor();
        testCaseProcessor.refreshTestCases(params.testCasesPath, IOUtils.Encoding.JSON, fhirContext);

        List<TestCaseResultSummary> TestResults = new ArrayList<TestCaseResultSummary>();

        File[] resourceTypeTestGroups = testCasesDirectory.listFiles(file -> file.isDirectory());
        //TODO: How can we validate the set of directories here - that they're actually FHIR resources - and message when they're not. Really it doesn't matter, it can be any grouping so long as it has a corresponding path in /bundles.

        for (File group : resourceTypeTestGroups) {
            System.out.println(String.format("Processing %s test cases...", group.getName()));

            // Get set of test artifacts
            File[] testArtifactNames = group.listFiles(file -> file.isDirectory());

            for (File testArtifact : testArtifactNames) {
                System.out.println(String.format("  Processing test cases for %s: %s", group.getName(), testArtifact.getName()));
                Boolean allTestArtifactTestsPassed = true;

                // Get content bundle
                Map.Entry<String, IBaseResource> testArtifactContentBundleMap = getContentBundleForTestArtifact(group.getName(), testArtifact.getName());

                if ((testArtifactContentBundleMap == null) || testArtifactContentBundleMap.getValue() == null) {
                    System.out.println(String.format("      No content bundle found for %s: %s", group.getName(), testArtifact.getName()));
                    System.out.println(String.format("  Done processing all test cases for %s: %s", group.getName(), testArtifact.getName()));
                    continue;
                }

                ITestProcessor testProcessor = getResourceTypeTestProcessor(group.getName());
                List<Map.Entry<String, IBaseResource>> testCasesBundles =
                    BundleUtils.GetBundlesInDir(testArtifact.getPath(), fhirContext, false);

                for (Map.Entry<String, IBaseResource> testCaseBundleMapEntry : testCasesBundles) {
                    IBaseResource testCaseBundle = testCaseBundleMapEntry.getValue();
                    TestCaseResultSummary testCaseResult  = new TestCaseResultSummary(group.getName(), testArtifact.getName(),
                        testCaseBundle.getIdElement().toString());
                    try {
                        System.out.println(String.format("      Starting processing of test case '%s' for %s: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName()));
                        Parameters testResults = testProcessor.executeTest(testCaseBundle, testArtifactContentBundleMap.getValue(), params.fhirServerUri);

                        Boolean testPassed = false;
                        for (ParametersParameter param : testResults.getParameter()) {
                            if (param.getName().getValue().indexOf(MeasureTestProcessor.TestPassedKey) >= 0) {
                                testPassed = param.getValueBoolean().isValue();
                                break;
                            }
                        }
                        testCaseResult.setTestPassed(testPassed);
                        System.out.println(String.format("      Done processing test case '%s' for %s: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName()));
                    } catch (Exception ex) {
                        testCaseResult.setTestPassed(false);
                        testCaseResult.setMessage(ex.getMessage());
                        System.out.println(String.format("      Error: Test case '%s' for %s: %s failed with message: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName(), ex.getMessage()));
                    }
                    TestResults.add(testCaseResult);
                }

                System.out.println(String.format("  Done processing all test cases for %s: %s", group.getName(), testArtifact.getName()));

                if (allTestArtifactTestsPassed) {
                    List<CqfmSoftwareSystem> softwareSystems = new ArrayList<CqfmSoftwareSystem>() {
                        {
                            add(testTargetSoftwareSystem);
                        }
                    };

                    if ((fhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU3) || (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4)) {
                        if (fhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
                            // Stamp the testContentBundle artifacts
                            BundleUtils.stampDstu3BundleEntriesWithSoftwareSystems((org.hl7.fhir.dstu3.model.Bundle)testArtifactContentBundleMap.getValue(), softwareSystems, fhirContext, getRootDir());
                        } else if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
                            BundleUtils.stampR4BundleEntriesWithSoftwareSystems((org.hl7.fhir.r4.model.Bundle)testArtifactContentBundleMap.getValue(), softwareSystems, fhirContext, getRootDir());
                        }

                        String bundleFilePath = testArtifactContentBundleMap.getKey();
                        IBaseResource bundle = testArtifactContentBundleMap.getValue();
                        IOUtils.writeResource(bundle, bundleFilePath, IOUtils.getEncoding(bundleFilePath), fhirContext);
                    }
                }
            }

            System.out.println(String.format("Done processing %s test cases", group.getName()));
        }

        TestCaseResultSummaryComparator comparator = new TestCaseResultSummaryComparator();
        Collections.sort(TestResults, comparator);

        List<TestCaseResultSummary> passedTests = new ArrayList<TestCaseResultSummary>();
        List<TestCaseResultSummary> failedTests = new ArrayList<TestCaseResultSummary>();
        for (TestCaseResultSummary result : TestResults) {
            System.out.println(result.toString());
            if (result.testPassed) {
                passedTests.add(result);
            } else {
                failedTests.add(result);
            }
        }

        System.out.println(String.format("%d tests failed", failedTests.size()));
        System.out.println(String.format("%d tests passed", passedTests.size()));
    }

    private Map.Entry<String, IBaseResource> getContentBundleForTestArtifact(String groupName, String testArtifactName) {
        Map.Entry<String, IBaseResource> testArtifactContentBundle = null;

        String contentBundlePath = getPathForContentBundleTestArtifact(groupName, testArtifactName);
        File testArtifactContentBundleDirectory = new File(contentBundlePath);
        if (testArtifactContentBundleDirectory != null && testArtifactContentBundleDirectory.exists()) {
            List<Map.Entry<String, IBaseResource>> testArtifactContentBundles = BundleUtils.GetBundlesInDir(contentBundlePath, fhirContext, false);

            // NOTE: Making the assumption that there will be a single bundle for the artifact.
            testArtifactContentBundle = testArtifactContentBundles.get(0);
        }

        return testArtifactContentBundle;
    }

    private String getPathForContentBundleTestArtifact(String groupName, String testArtifactName) {
        String contentBundlePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(getRootDir(), IGProcessor.bundlePathElement), groupName), testArtifactName);
        return contentBundlePath;
    }

    private ITestProcessor getResourceTypeTestProcessor(String resourceTypeName) {
        ITestProcessor testProcessor = null;
        String loweredResourceTypeName = resourceTypeName.toLowerCase();

        switch(loweredResourceTypeName) {
            case "measure":
                testProcessor = new MeasureTestProcessor(fhirContext);
                break;
            default:
                // Currently unsupported/undocumented
                System.out.println(String.format("No test processor implemented for resource type: $s", resourceTypeName));
                break;
        }

        return testProcessor;
    }
}