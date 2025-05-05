package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Strings;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.plandefinition.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.utilities.IGUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class IGProcessor extends BaseProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IGProcessor.class);
    public static final String IG_VERSION_REQUIRED = "igVersion required";
    public static final String CQL_LIBRARY_PATH_ELEMENT = "input/pagecontent/cql/";
    public static final String LIBRARY_PATH_ELEMENT = "input/resources/library/";
    public static final String MEASURE_PATH_ELEMENT = "input/resources/measure/";
    public static final String PLAN_DEFINITION_PATH_ELEMENT = "input/resources/plandefinition/";
    public static final String VALUE_SETS_PATH_ELEMENT = "input/vocabulary/valueset/";
    public static final String TEST_CASE_PATH_ELEMENT = "input/tests/";
    public static final String DEVICE_PATH_ELEMENT = "input/resources/device/";
    public static final String BUNDLE_PATH_ELEMENT = "bundles/";

    private final List<String> refreshedResourcesNames = new ArrayList<>();

    //mega ig method
    public void publishIG(RefreshIGParameters params) throws IOException {
        if (params.skipPackages == null) {
            params.skipPackages = false;
        }
        requireNonNull(params.includeDependencies, "includeDependencies can not be null");
        requireNonNull(params.includeELM, "includeELM can not be null");
        requireNonNull(params.includePatientScenarios, "includePatientScenarios can not be null");
        requireNonNull(params.includeTerminology, "includeTerminology can not be null");
        requireNonNull(params.versioned, "versioned can not be null");
        requireNonNull(params.resourceDirs, "resourceDirs can not be null");
        requireNonNull(params.outputEncoding, "outputEncoding can not be null");

        var iniProvided = params.ini != null && !params.ini.isEmpty();
        var rootDirProvided = params.rootDir != null && !params.rootDir.isEmpty();
        var igPathProvided = params.igPath != null && !params.igPath.isEmpty();

        //presence of -x arg means give error details instead of just error count during cql processing
        verboseMessaging = (params.verboseMessaging != null ? params.verboseMessaging : false);

        if (!iniProvided && (!rootDirProvided || !igPathProvided)) {
            throw new IllegalArgumentException("Either the ini argument or both igPath and rootDir must be provided");
        }

        if (params.ini != null) {
            initializeFromIni(params.ini);
        } else {
            initializeFromIg(params.rootDir, params.igPath, null);
        }

        // String measureToRefreshPath = params.measureToRefreshPath;
        var resourceDirs = new ArrayList<String>();
        for (var resourceDir : params.resourceDirs) {
            if (!Utilities.isAbsoluteFileName(resourceDir)) {
                try {
                    resourceDirs.add(Utilities.path(rootDir, resourceDir));
                } catch (IOException e) {
                    LogUtils.putException("ig", e);
                }
            }
        }

        IOUtils.resourceDirectories.addAll(resourceDirs);

        var fhirContext = IGProcessor.getIgFhirContext(this.getFhirVersion());

        //Use case 1
        //Scaffold basic templating for the type of content, Measure, PlanDefinition, or Questionnaire
        //Give it a name and generate all the files, cql, library, measure... should be very basic template
        //ScaffoldProcessor.scaffold(ScaffoldParameters);

        //Use case 2 while developing in Atom refresh content and run tests for either entire IG or targeted Artifact
        //refreshContent
        refreshIG(params);
        //validate
        //ValidateProcessor.validate(ValidateParameters);
        //run all tests
        //IGTestProcessor.testIg(IGTestParameters);

        //Use case 3
        //package everything
        var skipPackages = params.skipPackages;

        if (Boolean.FALSE.equals(skipPackages)) {
            new IGBundleProcessor(params.verboseMessaging, new LibraryProcessor(), new CDSHooksProcessor()).bundleIg(
                    refreshedResourcesNames,
                    rootDir,
                    getBinaryPaths(),
                    params.outputEncoding,
                    params.includeELM,
                    params.includeDependencies,
                    params.includeTerminology,
                    params.includePatientScenarios,
                    params.versioned,
                    params.addBundleTimestamp,
                    fhirContext,
                    params.fhirUri
            );
        }
        //test everything
        //IGTestProcessor.testIg(IGTestParameters);
        //Publish?
    }

    public void refreshIG(RefreshIGParameters params) throws IOException {
        if (params.ini != null) {
            initializeFromIni(params.ini);
        } else {
            try {
                initializeFromIg(params.rootDir, params.igPath, null);
            } catch (Exception e) {
                logMessage(String.format("Error Refreshing for File %s: %s", params.igPath, e.getMessage()));
            }
        }

        var resourceDirs = params.resourceDirs;
        if (resourceDirs.isEmpty()) {
            try {
                resourceDirs = IGUtils.extractResourcePaths(this.rootDir, this.sourceIg);
            } catch (IOException e) {
                logger.info(String.format(
                        "Error Extracting Resource Paths for File %s: %s", params.igPath, e.getMessage()));
            }
        }

        IOUtils.resourceDirectories.addAll(resourceDirs);
        var fhirContext = IGProcessor.getIgFhirContext(fhirVersion);
        IGProcessor.ensure(rootDir, params.includePatientScenarios, params.includeTerminology,
                IOUtils.resourceDirectories);

        var libraryProcessor = new LibraryProcessor();
        refreshedResourcesNames.addAll(libraryProcessor
                .refreshIgLibraryContent(this, params.outputEncoding,
                        params.libraryPath, params.libraryOutputPath, params.versioned,
                        fhirContext, params.shouldApplySoftwareSystemStamp));

        var measureProcessor = new MeasureProcessor();
        if (Strings.isNullOrEmpty(params.measureOutputPath)) {
            refreshedResourcesNames.addAll(measureProcessor.refreshIgMeasureContent(
                    this, params.outputEncoding, params.versioned,
                    fhirContext, params.measureToRefreshPath, params.shouldApplySoftwareSystemStamp,
                    params.includePopulationLevelDataRequirements));
        } else {
            refreshedResourcesNames.addAll(measureProcessor.refreshIgMeasureContent(
                    this, params.outputEncoding, params.measureOutputPath, params.versioned,
                    fhirContext, params.measureToRefreshPath, params.shouldApplySoftwareSystemStamp,
                    params.includePopulationLevelDataRequirements));
        }

        var planDefinitionProcessor = new PlanDefinitionProcessor(libraryProcessor);
        if (Strings.isNullOrEmpty(params.planDefinitionOutputPath)) {
            refreshedResourcesNames.addAll(planDefinitionProcessor.refreshIgPlanDefinitionContent(
                    this, params.outputEncoding, params.versioned, fhirContext,
                    params.planDefinitionToRefreshPath, params.shouldApplySoftwareSystemStamp));
        } else {
            refreshedResourcesNames.addAll(planDefinitionProcessor.refreshIgPlanDefinitionContent(
                    this, params.outputEncoding, params.planDefinitionOutputPath, params.versioned,
                    fhirContext, params.planDefinitionToRefreshPath, params.shouldApplySoftwareSystemStamp));
        }

        if (refreshedResourcesNames.isEmpty()) {
            logger.info("No resources successfully refreshed.");
            return;
        }

        if (Boolean.TRUE.equals(params.includePatientScenarios)) {
            var testCaseProcessor = new TestCaseProcessor();
            testCaseProcessor.refreshTestCases(
                    FilenameUtils.concat(rootDir, IGProcessor.TEST_CASE_PATH_ELEMENT),
                    params.outputEncoding, fhirContext, refreshedResourcesNames, verboseMessaging);
        }
    }

    public static FhirContext getIgFhirContext(String igVersion) {
        if (igVersion == null) {
            throw new IllegalArgumentException(IG_VERSION_REQUIRED);
        }

        switch (igVersion) {
            case "3.0.0":
            case "3.0.1":
            case "3.0.2":
                return FhirContext.forDstu3Cached();

            case "4.0.0":
            case "4.0.1":
                return FhirContext.forR4Cached();

            default:
                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
        }
    }

    public static String getBundlesPath(String igPath) {
        return FilenameUtils.concat(igPath, BUNDLE_PATH_ELEMENT);
    }

    public static void ensure(String igPath, Boolean includePatientScenarios, Boolean includeTerminology, List<String> resourcePaths) {
        var directory = new File(getBundlesPath(igPath));
        if (!directory.exists() && !directory.mkdir()) {
            logger.warn("Bundle path: {} was not created", getBundlesPath(igPath));
        }
        if (resourcePaths.isEmpty()) {
            ensureDirectory(igPath, IGProcessor.CQL_LIBRARY_PATH_ELEMENT);
            ensureDirectory(igPath, IGProcessor.LIBRARY_PATH_ELEMENT);
            ensureDirectory(igPath, IGProcessor.MEASURE_PATH_ELEMENT);
            ensureDirectory(igPath, IGProcessor.PLAN_DEFINITION_PATH_ELEMENT);
            ensureDirectory(igPath, IGProcessor.VALUE_SETS_PATH_ELEMENT);
            ensureDirectory(igPath, IGProcessor.TEST_CASE_PATH_ELEMENT);
        } else {
            checkForDirectory(igPath, IGProcessor.CQL_LIBRARY_PATH_ELEMENT);
            checkForDirectory(igPath, IGProcessor.LIBRARY_PATH_ELEMENT);
            checkForDirectory(igPath, IGProcessor.MEASURE_PATH_ELEMENT);
            checkForDirectory(igPath, IGProcessor.PLAN_DEFINITION_PATH_ELEMENT);
            checkForDirectory(igPath, IGProcessor.VALUE_SETS_PATH_ELEMENT);
            checkForDirectory(igPath, IGProcessor.TEST_CASE_PATH_ELEMENT);
        }
        checkForDirectory(igPath, IGProcessor.DEVICE_PATH_ELEMENT);

        // HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
    }

    private static void ensureDirectory(String igPath, String pathElement) {
        var directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + pathElement);
        }
        // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
        // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
        // leads to surprising results when bundling like picking up resources from the /tests directory.
        IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
    }

    private static void checkForDirectory(String igPath, String pathElement) {
        var directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            logger.info("No directory found by convention for: {}", directory.getName());
        } else {
            // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
            // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
            // leads to surprising results when bundling like picking up resources from the /tests directory.
            IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
        }
    }
}
