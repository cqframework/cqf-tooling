package org.opencds.cqf.igtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.library.LibraryProcessor;
import org.opencds.cqf.library.R4LibraryProcessor;
import org.opencds.cqf.library.STU3LibraryProcessor;
import org.opencds.cqf.measure.MeasureProcessor;
import org.opencds.cqf.terminology.ValueSetsProcessor;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.HttpClientUtils;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.ResourceUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.HttpClientUtil;

public class IGProcessor {
    public enum IGVersion {
        FHIR3("fhir3"), FHIR4("fhir4");

        private String string;

        public String toString() {
            return this.string;
        }

        private IGVersion(String string) {
            this.string = string;
        }

        public static IGVersion parse(String value) {
            switch (value) {
            case "fhir3":
                return FHIR3;
            case "fhir4":
                return FHIR4;
            default:
                throw new RuntimeException("Unable to parse IG version value:" + value);
            }
        }
    }

    public static void refreshIG(String igPath, IGVersion igVersion, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includeTestCases, String fhirServerUrl) {
        refreshIG(igPath, igVersion, includeELM, includeDependencies, includeTerminology, includeTestCases, false, fhirServerUrl);
    }

    public static void refreshIG(String igPath, IGVersion igVersion, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, String fhirServerUrl) {
        igPath = Paths.get(igPath).toAbsolutePath().toString();
        ensure(igPath);

        FhirContext fhirContext = getIgFhirContext(igVersion);

        ArrayList<String> refreshedLibraryNames = null;
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            refreshedLibraryNames = refreshStu3IG(igPath, includeELM, includeDependencies, includeTerminology,
                    includeTestCases, includeVersion, fhirContext);
            break;
        case R4:
            refreshedLibraryNames = refreshR4IG(igPath, includeELM, includeDependencies, includeTerminology,
                    includeTestCases, includeVersion, fhirContext);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (refreshedLibraryNames.isEmpty()) {
            LogUtils.info("No libraries successfully refreshed.");
            return;
        }

        if (includeTestCases) {
            TestCaseProcessor.refreshTestCases(getTestsPath(igPath), IOUtils.Encoding.JSON, fhirContext);
        }

        bundleIg(refreshedLibraryNames, igPath, includeELM, includeDependencies, includeTerminology, includeTestCases,
                includeVersion, fhirContext, fhirServerUrl);
    }

    private static ArrayList<String> refreshStu3IG(String igPath, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = refreshStu3IgLibraryContent(igPath, includeELM, fhirContext);
        // union with below when this is implemented.
        // refreshMeasureContent();
        return refreshedLibraryNames;
    }

    private static ArrayList<String> refreshR4IG(String igPath, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = refreshR4LibraryContent(igPath, includeELM, fhirContext);
        // union with below when this is implemented.
        // refreshMeasureContent();
        return refreshedLibraryNames;
    }

    public static ArrayList<String> refreshStu3IgLibraryContent(String igPath, Boolean includeELM,
            FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
        List<String> cqlContentPaths = IOUtils.getFilePaths(getCqlLibraryPath(igPath), false);

        for (String path : cqlContentPaths) {
            try {
                STU3LibraryProcessor.refreshLibraryContent(path, getLibraryPath(igPath), fhirContext, Encoding.JSON);
                refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
            } catch (Exception e) {
                LogUtils.putWarning(path, e.getMessage());
            }
            LogUtils.warn(path);
        }

        return refreshedLibraryNames;
    }

    public static ArrayList<String> refreshR4LibraryContent(String igPath, Boolean includeELM,
            FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
        List<String> cqlContentPaths = IOUtils.getFilePaths(getCqlLibraryPath(igPath), false);

        for (String path : cqlContentPaths) {
            try {
                R4LibraryProcessor.refreshLibraryContent(path, getLibraryPath(igPath), fhirContext, Encoding.JSON);
                refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
            } catch (Exception e) {
                LogUtils.putWarning(path, e.getMessage());
            }
            LogUtils.warn(path);
        }

        return refreshedLibraryNames;
    }

    // TODO: most of the work of the sub methods of this should probably be moved to
    // their respective resource Processors.
    // No time for a refactor atm though. So stinky it is!
    public static void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeELM,
            Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion,
            FhirContext fhirContext, String fhirServerUrl) {
        Encoding encoding = Encoding.JSON;

        // The set to bundle should be the union of the successfully refreshed Measures
        // and Libraries
        // Until we have the ability to refresh Measures, the set is the union of
        // existing Measures and successfully refreshed Libraries
        List<String> measureSourcePaths = IOUtils.getFilePaths(getMeasurePath(igPath), false);
        List<String> measurePathLibraryNames = new ArrayList<String>();
        for (String measureSourcePath : measureSourcePaths) {
            measurePathLibraryNames
                    .add(FilenameUtils.getBaseName(measureSourcePath).replace(MeasureProcessor.ResourcePrefix, ""));
        }

        List<String> bundledMeasures = new ArrayList<String>();
        for (String refreshedLibraryName : refreshedLibraryNames) {
            try {
                if (!measurePathLibraryNames.contains(refreshedLibraryName)) {
                    continue;
                }

                Map<String, IAnyResource> resources = new HashMap<String, IAnyResource>();

                String librarySourcePath = FilenameUtils.concat(getLibraryPath(igPath),
                        IOUtils.formatFileName(LibraryProcessor.getId(refreshedLibraryName), encoding));
                String measureSourcePath = FilenameUtils.concat(getMeasurePath(igPath),
                        IOUtils.formatFileName(MeasureProcessor.getId(refreshedLibraryName), encoding));

                Boolean shouldPersist = ResourceUtils.safeAddResource(measureSourcePath, resources, fhirContext);
                shouldPersist = shouldPersist
                        & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext);

                String cqlFileName = IOUtils.formatFileName(refreshedLibraryName, Encoding.CQL);
                String cqlLibrarySourcePath = FilenameUtils.concat(getCqlLibraryPath(igPath), cqlFileName);
                if (includeTerminology) {
                    shouldPersist = shouldPersist
                            & bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding);
                }

                if (includeDependencies) {
                    shouldPersist = shouldPersist
                            & bundleDependencies(librarySourcePath, fhirContext, resources, encoding);
                }

                if (includeTestCases) {
                    shouldPersist = shouldPersist
                            & bundleTestCases(igPath, refreshedLibraryName, fhirContext, resources);
                }

                if (shouldPersist) {
                    String bundleDestPath = FilenameUtils.concat(getBundlesPath(igPath), refreshedLibraryName);
                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()), fhirServerUrl);
                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, measureSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includeTestCases);
                    bundledMeasures.add(refreshedLibraryName);
                }
            } catch (Exception e) {
                LogUtils.putWarning(refreshedLibraryName, e.getMessage());
            } finally {
                LogUtils.warn(refreshedLibraryName);
            }
        }
        String message = "\r\n" + bundledMeasures.size() + " Measures successfully bundled:";
        for (String bundledMeasure : bundledMeasures) {
            message += "\r\n     " + bundledMeasure + " BUNDLED";
        }

        ArrayList<String> failedMeasures = new ArrayList<>(measurePathLibraryNames);
        measurePathLibraryNames.removeAll(bundledMeasures);
        measurePathLibraryNames.retainAll(refreshedLibraryNames);
        message += "\r\n" + measurePathLibraryNames.size() + " Measures refreshed, but not bundled (due to issues):";
        for (String notBundled : measurePathLibraryNames) {
            message += "\r\n     " + notBundled + " REFRESHED";
        }

        failedMeasures.removeAll(bundledMeasures);
        failedMeasures.removeAll(measurePathLibraryNames);
        message += "\r\n" + failedMeasures.size() + " Measures failed refresh:";
        for (String failed : failedMeasures) {
            message += "\r\n     " + failed + " FAILED";
        }

        LogUtils.info(message);
    }

    public static Boolean bundleValueSets(String cqlContentPath, String igPath, FhirContext fhirContext,
            Map<String, IAnyResource> resources, Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath,
                    fhirContext, true);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putWarning(cqlContentPath, e.getMessage());
        }
        return shouldPersist;
    }

    public static Boolean bundleDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources,
            Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            LogUtils.putWarning(path, e.getMessage());
        }
        return shouldPersist;
    }

    private static Boolean bundleTestCases(String igPath, String libraryName, FhirContext fhirContext,
            Map<String, IAnyResource> resources) {
        Boolean shouldPersist = true;
        String igTestCasePath = FilenameUtils.concat(getTestsPath(igPath), libraryName);

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
            LogUtils.putWarning(igTestCasePath, e.getMessage());
        }
        return shouldPersist;
    }

    private static void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IAnyResource> resources, String fhirServerUrl) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirServerUrl != null && !fhirServerUrl.equals("")) {
            try {
                HttpClientUtils.post(fhirServerUrl, (IAnyResource) bundle, encoding, fhirContext);
            } catch (IOException e) {

            }
        }
    }

    public static final String bundleFilesPathElement = "files/";    
    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String measureSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includeTestCases) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(measureSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(measureSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL);
        String cqlLibrarySourcePath = FilenameUtils.concat(getCqlLibraryPath(igPath), cqlFileName);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {  
            try {     
                Map<String, IAnyResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, true);      
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IAnyResource>(valuesets.values()), fhirContext);
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
                }  
            }  catch (Exception e) {
                LogUtils.putWarning(libraryName, e.getMessage());
            }       
        }
        
        if (includeDependencies) {
            Map<String, IAnyResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IAnyResource>(depLibraries.values()), fhirContext);            
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);  
            }        
        }

        if (includeTestCases) {
            bundleTestCaseFiles(igPath, libraryName, bundleDestFilesPath);
        }        
    }

    public static void bundleTestCaseFiles(String igPath, String libraryName, String destPath) {    
        String igTestCasePath = FilenameUtils.concat(getTestsPath(igPath), libraryName);
        List<String> testCasePaths = IOUtils.getFilePaths(igTestCasePath, false);
        for (String testPath : testCasePaths) {
            String bundleTestDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testPath));
            IOUtils.copyFile(testPath, bundleTestDestPath);

            List<String> testCaseDirectories = IOUtils.getDirectoryPaths(igTestCasePath, false);
            for (String testCaseDirectory : testCaseDirectories) {
                List<String> testContentPaths = IOUtils.getFilePaths(testCaseDirectory, false);
                for (String testContentPath : testContentPaths) {
                    String bundleTestContentDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testContentPath));
                    IOUtils.copyFile(testContentPath, bundleTestContentDestPath);
                }
            }            
        }
    }

    public static FhirContext getIgFhirContext(IGVersion igVersion)
    {
        switch (igVersion) {
            case FHIR3:
                return FhirContext.forDstu3();
            case FHIR4:
                return FhirContext.forR4();
            default:
                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
        }     
    }

    public static IGVersion getIgVersion(String igPath)
    {
        if (IOUtils.pathIncludesElement(igPath, IGVersion.FHIR3.toString()))
        {
            return IGVersion.FHIR3;
        }
        else if (IOUtils.pathIncludesElement(igPath, IGVersion.FHIR4.toString()))
        {
            return IGVersion.FHIR4;
        }
        throw new IllegalArgumentException("IG version not found in IG Path.");
    }

    public static final String bundlePathElement = "bundles/";
    public static String getBundlesPath(String igPath) {
        return FilenameUtils.concat(igPath, bundlePathElement);
    }

    public static final String cqlLibraryPathElement = "cql/";
    public static String getCqlLibraryPath(String igPath) {
        return FilenameUtils.concat(igPath, cqlLibraryPathElement);
    }

    public static final String libraryPathElement = "resources/library/";
    public static String getLibraryPath(String igPath) {
        return FilenameUtils.concat(igPath, libraryPathElement);
    }

    public static final String measurePathElement = "resources/measure/";
    public static String getMeasurePath(String igPath) {
        return FilenameUtils.concat(igPath, measurePathElement);
    }

    public static final String valuesetsPathElement = "resources/valuesets/";
    public static String getValueSetsPath(String igPath) {
        return FilenameUtils.concat(igPath, valuesetsPathElement);
    }

    public static final String testCasePathElement = "tests/";
    public static String getTestsPath(String igPath) {
        return FilenameUtils.concat(igPath, testCasePathElement);
    }
    
    private static void ensure(String igPath) {
        File directory = null;
                
        directory = new File(getBundlesPath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + bundlePathElement);
        }        
        directory = new File(getCqlLibraryPath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + cqlLibraryPathElement);
        }
        directory = new File(getLibraryPath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + libraryPathElement);
        }
        directory = new File(getMeasurePath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + measurePathElement);
        }
        directory = new File(getValueSetsPath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + valuesetsPathElement);
        }
        directory = new File(getTestsPath(igPath));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + testCasePathElement);
        }

        List<String> cqlContentPaths = IOUtils.getFilePaths(getCqlLibraryPath(igPath), false);
        for (String cqlContentPath : cqlContentPaths) {
            String cqlLibraryContent = IOUtils.getCqlString(cqlContentPath);
            if (!cqlLibraryContent.startsWith("library ")) {
                throw new RuntimeException("Unable to refresh IG.  All Libraries must begin with \"library \": " + cqlContentPath);
            }

            String strippedLibraryName = FilenameUtils.getBaseName(cqlContentPath);
            for (IGProcessor.IGVersion igVersion : IGVersion.values()) {                
                String igVersionToken = "_" + igVersion.toString().toUpperCase();
               
                if (strippedLibraryName.contains(igVersionToken)) {
                    strippedLibraryName = strippedLibraryName.replace(igVersionToken, "");
                    if (strippedLibraryName.contains("_")) {
                        throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
                    }
                }    
            }
            if (strippedLibraryName.contains("_")) {
                throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
            }
        }
    }
}
