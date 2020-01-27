package org.opencds.cqf.igtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
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

    public static void refreshIG(RefreshIGParameters params) {

        String igPath = params.igPath;
        IGVersion igVersion = params.igVersion;
        Boolean includeELM = params.includeELM;
        Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        String fhirUri = params.fhirUri;
        ArrayList<String> resourceDirs = params.resourceDirs;

        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = getIgFhirContext(igVersion);

        igPath = Paths.get(igPath).toAbsolutePath().toString();

        ensure(igPath, includePatientScenarios, includeTerminology, IOUtils.resourceDirectories);

        ArrayList<String> refreshedLibraryNames = null;
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            refreshedLibraryNames = refreshStu3IG(igPath, includeELM, includeDependencies, includeTerminology,
                    includePatientScenarios, versioned, fhirContext);
            break;
        case R4:
            refreshedLibraryNames = refreshR4IG(igPath, includeELM, includeDependencies, includeTerminology,
                    includePatientScenarios, versioned, fhirContext);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (refreshedLibraryNames.isEmpty()) {
            LogUtils.info("No libraries successfully refreshed.");
            return;
        }

        if (includePatientScenarios) {
            TestCaseProcessor.refreshTestCases(IOUtils.getTestsPath(igPath), IOUtils.Encoding.JSON, fhirContext);
        }

        bundleIg(refreshedLibraryNames, igPath, includeELM, includeDependencies, includeTerminology, includePatientScenarios,
        versioned, fhirContext, fhirUri);
    }

    private static ArrayList<String> refreshStu3IG(String igPath, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = refreshStu3IgLibraryContent(igPath, includeELM, versioned, fhirContext);
        // union with below when this is implemented.
        // refreshMeasureContent();
        return refreshedLibraryNames;
    }

    private static ArrayList<String> refreshR4IG(String igPath, Boolean includeELM, Boolean includeDependencies,
            Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = refreshR4LibraryContent(igPath, includeELM, versioned, fhirContext);
        // union with below when this is implemented.
        // refreshMeasureContent();
        return refreshedLibraryNames;
    }

    public static ArrayList<String> refreshStu3IgLibraryContent(String igPath, Boolean includeELM, Boolean versioned,
            FhirContext fhirContext) {
                ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
                HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
        
                for (String path : cqlContentPaths) {
                    try {
                        //ask about how to do this better
                        String libraryPath;
                        try {
                            libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(path, fhirContext);
                        } catch (Exception e) {
                            libraryPath = "";
                        }
                        
                        STU3LibraryProcessor.refreshLibraryContent(path, libraryPath, fhirContext, Encoding.JSON, versioned);
                        refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
                    } catch (Exception e) {
                        LogUtils.putWarning(path, e.getMessage());
                    }
                    LogUtils.warn(path);
                }
        
                return refreshedLibraryNames;
    }

    public static ArrayList<String> refreshR4LibraryContent(String igPath, Boolean includeELM, Boolean versioned,
            FhirContext fhirContext) {
        ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();

        for (String path : cqlContentPaths) {
            try {
                //ask about how to do this better
                String libraryPath;
                try {
                    libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(path, fhirContext);
                } catch (Exception e) {
                    libraryPath = "";
                }
                
                R4LibraryProcessor.refreshLibraryContent(path, libraryPath, fhirContext, Encoding.JSON, versioned);
                refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
            } catch (Exception e) {
                LogUtils.putWarning(path, e.getMessage() == null ? e.toString() : e.getMessage());
            }
            LogUtils.warn(path);
        }

        return refreshedLibraryNames;
    }

    // TODO: most of the work of the sub methods of this should probably be moved to
    // their respective resource Processors.
    // No time for a refactor atm though. So stinky it is!
    public static void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeELM,
            Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned,
            FhirContext fhirContext, String fhirUri) {
        Encoding encoding = Encoding.JSON;

        // The set to bundle should be the union of the successfully refreshed Measures
        // and Libraries
        // Until we have the ability to refresh Measures, the set is the union of
        // existing Measures and successfully refreshed Libraries
        HashSet<String> measureSourcePaths = IOUtils.getMeasurePaths(fhirContext);
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

                String refreshedLibraryFileName = IOUtils.formatFileName(refreshedLibraryName, encoding);
                String librarySourcePath;
                try {
                    librarySourcePath = IOUtils.getLibraryPathAssociatedWithCqlFileName(refreshedLibraryFileName, fhirContext);
                } catch (Exception e) {
                    LogUtils.putWarning(refreshedLibraryName, e.getMessage());
                    continue;
                } finally {
                    LogUtils.warn(refreshedLibraryName);
                }
                
                String measureSourcePath = "";
                for (String path : measureSourcePaths) {
                    if (path.endsWith(refreshedLibraryFileName))
                    {
                        measureSourcePath = path;
                    }
                }

                Boolean shouldPersist = ResourceUtils.safeAddResource(measureSourcePath, resources, fhirContext);
                shouldPersist = shouldPersist
                        & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext);

                String cqlFileName = IOUtils.formatFileName(refreshedLibraryName, Encoding.CQL);
                List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
                    .filter(path -> path.endsWith(cqlFileName))
                    .collect(Collectors.toList());
                String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
                if (includeTerminology) {
                    shouldPersist = shouldPersist
                            & bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding);
                }

                if (includeDependencies) {
                    shouldPersist = shouldPersist
                            & bundleDependencies(librarySourcePath, fhirContext, resources, encoding);
                }

                if (includePatientScenarios) {
                    shouldPersist = shouldPersist
                            & bundleTestCases(igPath, refreshedLibraryName, fhirContext, resources);
                }

                if (shouldPersist) {
                    String bundleDestPath = FilenameUtils.concat(getBundlesPath(igPath), refreshedLibraryName);
                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()), fhirUri);
                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, measureSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios);
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
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, true);
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
        String igTestCasePath = FilenameUtils.concat(IOUtils.getTestsPath(igPath), libraryName);

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

    private static void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IAnyResource> resources, String fhirUri) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, (IAnyResource) bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putWarning(((IAnyResource)bundle).getId(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
            }
        }
    }

    public static final String bundleFilesPathElement = "files/";    
    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String measureSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(measureSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(measureSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL);
        List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
            .filter(path -> path.endsWith(cqlFileName))
            .collect(Collectors.toList());
        String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
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

        if (includePatientScenarios) {
            bundleTestCaseFiles(igPath, libraryName, bundleDestFilesPath);
        }        
    }

    public static void bundleTestCaseFiles(String igPath, String libraryName, String destPath) {    
        String igTestCasePath = FilenameUtils.concat(IOUtils.getTestsPath(igPath), libraryName);
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

    public static IGVersion getIgVersion(String igPath){
        IGVersion igVersion = null;
        List<File> igPathFiles = IOUtils.getFilePaths(igPath, false).stream()
            .map(path -> new File(path))
            .collect(Collectors.toList());
        for (File file : igPathFiles) {
            if (FilenameUtils.getExtension(file.getName()).equals("ini")) {
                igVersion = tryToReadIni(file);
            }
        }
        if (igVersion == null) {
            throw new IllegalArgumentException("IG version not found in ig.ini");
        }
        else return igVersion;
    }

    private static IGVersion tryToReadIni(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            String igIniContent = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
            String[] contentLines = igIniContent.split("\n");
            inputStream.close();
            return parseVersion(contentLines);
        } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
    }

    private static IGVersion parseVersion(String[] contentLines) {
        for (String line : contentLines) {
            if (line.toLowerCase().startsWith("fhirspec"))
            {
                if (line.contains("R4") || line.contains("r4")){
                    return IGVersion.FHIR4;
                }
                else if (line.contains("stu3") || line.contains("STU3") || line.contains("dstu3") || line.contains("DSTU3")) {
                    return IGVersion.FHIR3;
                }
            }
        }
        return null;
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
    
    private static void ensure(String igPath, Boolean includePatientScenarios, Boolean includeTerminology, ArrayList<String> resourcePaths) {                
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
        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
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

    private static void ensureDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            throw new RuntimeException("Convention requires the following directory:" + pathElement);
        }
        IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
    }

    private static void checkForDirectory(String igPath, String pathElement) {
        File directory = new File(FilenameUtils.concat(igPath, pathElement));
        if (!directory.exists()) {
            System.out.println("No directory found by convention for: " + directory.getName());
        }
        else {
            IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
        }
    }
}
