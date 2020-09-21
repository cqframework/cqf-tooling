package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.measure.MeasureTestProcessor;
import org.opencds.cqf.tooling.parameter.TestIGParameters;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.List;


public class IGTestProcessor extends BaseProcessor {
    private FhirContext fhirContext;

    public void testIg(TestIGParameters params) {
        fhirContext = params.fhirContext;

        if (params.ini != null) {
            initialize(params.ini);
        }
        else {
            initialize(params.rootDir, params.igPath);
        }

        System.out.println("Running IG test cases...");

        File testCasesDirectory = new File(params.testCasesPath);
        if (!testCasesDirectory.isDirectory()) {
            throw new RuntimeException("The path to the test scripts must point to a directory");
        }

        // refresh/generate test bundles
        System.out.println("Refreshing test cases...");
        TestCaseProcessor testCaseProcessor = new TestCaseProcessor();
        testCaseProcessor.refreshTestCases(params.testCasesPath, IOUtils.Encoding.JSON, fhirContext);

//        File[] resourceTypeTestGroups = testCasesDirectory.listFiles(file -> file.isDirectory());
//        //TODO: How can we validate the set of directories here - that they're actually FHIR resources - and message when they're not. Really it doesn't matter, it can be any grouping so long as it has a corresponding path in /bundles.
//
//        for (File group : resourceTypeTestGroups) {
//            System.out.println(String.format("Processing %s test cases...", group.getName()));
//
//            // Get set of test artifacts
//            File[] testArtifactNames = group.listFiles(file -> file.isDirectory());
//
//            for (File testArtifact : testArtifactNames) {
//                System.out.println(String.format("  Processing test cases for %s: %s", group.getName(), testArtifact.getName()));
//
//                // Get content bundle
//                IBaseResource testArtifactContentBundle = GetContentBundleForTestArtifact(group.getName(), testArtifact.getName());
//
//                ITestProcessor testProcessor = getResourceTypeTestProcessor(group.getName());
//                List<IBaseResource> testCasesBundles = BundleUtils.GetBundlesInDir(testArtifact.getPath(), fhirContext, false);
//                for (IBaseResource testCaseBundle : testCasesBundles) {
//                    try {
//                        System.out.println(String.format("      Starting processing of test case '%s' for %s: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName()));
//                        Parameters testResults = testProcessor.executeTest(testCaseBundle, testArtifactContentBundle, params.fhirServerUri);
//                        System.out.println(String.format("      Done processing test case '%s' for %s: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName()));
//                    } catch (Exception ex) {
//                        System.out.println(String.format("      Error: Test case '%s' for %s: %s failed with message: %s", testCaseBundle.getIdElement(), group.getName(), testArtifact.getName(), ex.getMessage()));
//                    }
//                }
//
//                System.out.println(String.format("  Done processing all test cases for %s: %s", group.getName(), testArtifact.getName()));
//            }
//
//            System.out.println(String.format("Done processing %s test cases", group.getName()));
//        }
    }

    private IBaseResource GetContentBundleForTestArtifact(String groupName, String testArtifactName) {
        IBaseResource testArtifactContentBundle = null;

        String contentBundlePath = FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(getRootDir(), IGProcessor.bundlePathElement), groupName), testArtifactName);
        File testArtifactContentBundleDirectory = new File(contentBundlePath);
        if (testArtifactContentBundleDirectory != null && testArtifactContentBundleDirectory.exists()) {
            List<IBaseResource> testArtifactContentBundles = BundleUtils.GetBundlesInDir(contentBundlePath, fhirContext, false);

            // NOTE: Making the assumption that there will be a single bundle for the artifact.
            testArtifactContentBundle = testArtifactContentBundles.get(0);
        }

        return testArtifactContentBundle;
    }

    private String GetPathForContentBundleTestArtifact(String groupName, String testArtifactName) {
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