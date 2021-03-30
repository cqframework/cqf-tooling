package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.utilities.IGUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;

import ca.uhn.fhir.context.FhirContext;

public class IGProcessor extends BaseProcessor {
    //mega ig method
    public void publishIG(RefreshIGParameters params) {
        if (params.ini != null) {
            initializeFromIni(params.ini);
        }
        else {
            initializeFromIg(params.rootDir, params.igPath, null);
        }

        Encoding encoding = params.outputEncoding;
        Boolean includeELM = params.includeELM;
        Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        Boolean cdsHooksIg = params.cdsHooksIg;
        String fhirUri = params.fhirUri;
        // String measureToRefreshPath = params.measureToRefreshPath;
        ArrayList<String> resourceDirs = new ArrayList<String>();
        for (String resourceDir : params.resourceDirs) {
            if (!Utilities.isAbsoluteFileName(resourceDir)) {
                try {
                    resourceDirs.add(Utilities.path(rootDir, resourceDir));
                } catch (IOException e) {
                    LogUtils.putException("ig", e);
                }
            }
        }

        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = IGProcessor.getIgFhirContext(this.getFhirVersion());

        //Use case 1
        //Scaffold basic templating for the type of content, Measure, PlanDefinition, or Questionnaire
        //Give it a name and generate all the files, cql, library, measure... should be very basic template
        //ScaffoldProcessor.scaffold(ScaffoldParameters);

        //Use case 2 while developing in Atom refresh content and run tests for either entire IG or targeted Artifact
        //refreshcontent
        LogUtils.info("IGProcessor.publishIG - refreshIG");
        refreshIG(params);
        //validate
        //ValidateProcessor.validate(ValidateParameters);
        //run all tests
        //IGTestProcessor.testIg(IGTestParameters);

        //Use case 3
        //package everything
        LogUtils.info("IGProcessor.publishIG - bundleIg");
        IGBundleProcessor.bundleIg(refreshedResourcesNames, rootDir, encoding, includeELM, includeDependencies, includeTerminology, includePatientScenarios,
        versioned, cdsHooksIg, fhirContext, fhirUri);
        //test everything
        //IGTestProcessor.testIg(IGTestParameters);
        //Publish?
    }

    public ArrayList<String> refreshedResourcesNames = new ArrayList<String>();
    public void refreshIG(RefreshIGParameters params) {
        if (params.ini != null) {
            initializeFromIni(params.ini);
        }
        else {
            try {
                initializeFromIg(params.rootDir, params.igPath, null);
            }
            catch (Exception e) {
                logMessage(String.format("Error Refreshing for File "+ params.igPath+": "+e.getMessage(), e));
            }
        }

        Encoding encoding = params.outputEncoding;
        // Boolean includeELM = params.includeELM;
        // Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        // String fhirUri = params.fhirUri;
        String measureToRefreshPath = params.measureToRefreshPath;
        ArrayList<String> resourceDirs = params.resourceDirs;
        if (resourceDirs.size() == 0) {
            try {
                resourceDirs = IGUtils.extractResourcePaths(this.rootDir, this.sourceIg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = IGProcessor.getIgFhirContext(fhirVersion);

        IGProcessor.ensure(rootDir, includePatientScenarios, includeTerminology, IOUtils.resourceDirectories);

        List<String> refreshedLibraryNames;
        refreshedLibraryNames = LibraryProcessor.refreshIgLibraryContent(this, encoding, versioned, fhirContext);
        // Only add libraries if this is a cds IG, else only measures.
        if (params.cdsHooksIg) {
            refreshedResourcesNames.addAll(refreshedLibraryNames);
        }

        List<String> refreshedMeasureNames;
        refreshedMeasureNames = MeasureProcessor.refreshIgMeasureContent(rootDir, encoding, versioned, fhirContext, measureToRefreshPath);
        refreshedResourcesNames.addAll(refreshedMeasureNames);

        if (refreshedResourcesNames.isEmpty()) {
            LogUtils.info("No resources successfully refreshed.");
            return;
        }

        if (includePatientScenarios) {
            TestCaseProcessor testCaseProcessor = new TestCaseProcessor();
            testCaseProcessor.refreshTestCases(FilenameUtils.concat(rootDir, IGProcessor.testCasePathElement), encoding, fhirContext, refreshedResourcesNames);
        }
    }

    public static FhirContext getIgFhirContext(String igVersion)
    {
        if (igVersion == null) {
            throw new IllegalArgumentException("igVersion required");
        }

        switch (igVersion) {
            case "3.0.0":
            case "3.0.1":
            case "3.0.2":
                return FhirContext.forDstu3();

            case "4.0.0":
            case "4.0.1":
                return FhirContext.forR4();

            default:
                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
        }     
    }
    
    public static final String bundlePathElement = "bundles/";
    public static String getBundlesPath(String igPath) {
        return FilenameUtils.concat(igPath, bundlePathElement);
    }
    public static final String cqlLibraryPathElement = "input/pagecontent/cql/";
    public static final String libraryPathElement = "input/resources/library/";
    public static final String measurePathElement = "input/resources/measure/";
    public static final String valuesetsPathElement = "input/vocabulary/valueset/";
    public static final String testCasePathElement = "input/tests/";
    public static final String devicePathElement = "input/resources/device/";
    
    public static void ensure(String igPath, Boolean includePatientScenarios, Boolean includeTerminology, ArrayList<String> resourcePaths) {                
        File directory = new File(getBundlesPath(igPath));
        if (!directory.exists()) {
            directory.mkdir();
        }        
        if (resourcePaths.isEmpty()) {
            ensureDirectory(igPath, IGProcessor.cqlLibraryPathElement);
            ensureDirectory(igPath, IGProcessor.libraryPathElement);
            ensureDirectory(igPath, IGProcessor.measurePathElement);
            ensureDirectory(igPath, IGProcessor.valuesetsPathElement);
            ensureDirectory(igPath, IGProcessor.testCasePathElement);
        }
        else {
            checkForDirectory(igPath, IGProcessor.cqlLibraryPathElement);
            checkForDirectory(igPath, IGProcessor.libraryPathElement);
            checkForDirectory(igPath, IGProcessor.measurePathElement);
            checkForDirectory(igPath, IGProcessor.valuesetsPathElement);
            checkForDirectory(igPath, IGProcessor.testCasePathElement);
        }
        checkForDirectory(igPath, IGProcessor.devicePathElement);

        // HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
    }

    private static void ensureDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + pathElement);
        }
        // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
        // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
        // leads to surprising results when bundling like picking up resources from the /tests directory.
        IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
    }

    private static void checkForDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            System.out.println("No directory found by convention for: " + directory.getName());
        }
        else {
            // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
            // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
            // leads to surprising results when bundling like picking up resources from the /tests directory.
            IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
        }
    }
}
