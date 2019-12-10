package org.opencds.cqf.igtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.library.LibraryProcessor;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.BundleUtils;
import org.opencds.cqf.utilities.IOUtils;
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

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(IGProcessor.class);

    public static final String testCasePathElement = "tests/";

    public static void refreshIG(String igPath, IGVersion igVersion, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases) {
        refreshIG(igPath, igVersion, includeELM, includeDependencies, includeTerminology, includeTestCases, false);
    }

    public static void refreshIG(String igPath, IGVersion igVersion, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion) {
        FhirContext fhirContext = getIgFhirContext(igVersion);

        // TODO: if refresh content is fhir version non-specific, no need for two
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                refreshStu3IG(igPath, includeELM, includeDependencies, includeTerminology, includeTestCases, includeVersion, fhirContext);
                break;
            case R4:
                refreshR4IG(igPath, includeELM, includeDependencies, includeTerminology, includeTestCases, includeVersion, fhirContext);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (includeTestCases) {
            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, testCasePathElement), IOUtils.Encoding.JSON, fhirContext);
        }
        bundleIg(igPath, includeELM, includeDependencies, includeTerminology, includeTestCases, includeVersion, fhirContext);
    }

    private static void refreshStu3IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        refreshStu3IgLibraryContent(igPath, includeELM, fhirContext);
        // refreshMeasureContent();
    }

    private static void refreshR4IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        refreshR4LibraryContent(igPath, includeELM, fhirContext);
        // refreshMeasureContent();
    }

    public static final String libraryPathElement = "resources/library/";
    public static void refreshStu3IgLibraryContent(String igPath, Boolean includeELM, FhirContext fhirContext)
    {
        Map<String, String> resourceExceptions = new HashMap<String, String>();
        String cqlContentDirPath = FilenameUtils.concat(igPath, cqlLibraryPathElement);
        String libraryPath = FilenameUtils.concat(igPath, libraryPathElement);
        List<String> cqlContentPaths = IOUtils.getFilePaths(cqlContentDirPath, false);
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<DSTU3>(libraryPath);
        for (String path : cqlContentPaths) {
            LibraryProcessor.refreshLibraryContent(path, libraryPath, fhirContext, Encoding.JSON, resourceExceptions);
            if (!resourceExceptions.isEmpty()) {
                {
                    String exceptionMessage = "";
                    for (Map.Entry<String, String> resourceException : resourceExceptions.entrySet()) {
                        exceptionMessage += "\r\n" + "          Resource could not be processed: " + resourceException.getKey() + " - " + resourceException.getValue();
                    }
                    ourLog.warn("Library could not be refreshed for: " + FilenameUtils.getName(path) + " - " + exceptionMessage);
                }
            }
        }
    }

    public static void refreshR4LibraryContent(String igPath, Boolean includeELM, FhirContext fhirContext)
    {
        String libraryPath = FilenameUtils.concat(igPath, libraryPathElement);
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<R4>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
    }

    public static final String cqlLibraryPathElement = "cql/";
    public static final String bundlePathElement = "bundles/";

    public static final String measurePathElement = "resources/measure/";

    // TODO: most of the work of the sub methods of this should probably be moved to their respective resource Processors.
    // No time for a refactor atm though. So stinky it is!
    public static void bundleIg(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        // bundle
        /*
                - if include dependencies, add dependencies to bundle
                - if include terminiology, add terminology to bundle         
        */  
        //zip
        /*
                - if include dependencies, add bundle of libary dependencies to zip
                - if include terminology, add bundle of terminology to zip
             
        */   
        Encoding encoding = Encoding.JSON;

        String igMeasurePath = FilenameUtils.concat(igPath, measurePathElement);
        String igLibraryPath = FilenameUtils.concat(igPath, libraryPathElement);

        List<String> measureSourcePaths = IOUtils.getFilePaths(igMeasurePath, false);
        Boolean shouldPersist = true;
        for (String measureSourcePath : measureSourcePaths) {
            Map<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
            Map<String, String> resourceExceptions = new HashMap<String, String>();
            String libraryName = FilenameUtils.getBaseName(measureSourcePath).replace("measure-", "");
            String librarySourcePath = FilenameUtils.concat(igLibraryPath, IOUtils.getFileName(LibraryProcessor.getId(libraryName), encoding));

            shouldPersist = ResourceUtils.safeAddResource(measureSourcePath, resources, fhirContext, resourceExceptions);
            shouldPersist = shouldPersist & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext, resourceExceptions);

            if (includeTerminology) {
                shouldPersist = shouldPersist & bundleValueSets(librarySourcePath, fhirContext, resources, resourceExceptions, encoding);
            }

            if (includeDependencies) {
                shouldPersist = shouldPersist & bundleDependencies(librarySourcePath, fhirContext, resources, resourceExceptions, encoding);
            }

            if (includeTestCases) {
                shouldPersist = shouldPersist & bundleTestCases(igPath, libraryName, fhirContext, resources, resourceExceptions);
            }

            if (shouldPersist) {
                String bundlePath = FilenameUtils.concat(igPath, bundlePathElement);
                String bundleDestPath = FilenameUtils.concat(bundlePath, libraryName);
                persistBundle(igPath, bundleDestPath, libraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()));
                bundleFiles(igPath, bundleDestPath, libraryName, measureSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includeTestCases);
            } else {
                String exceptionMessage = "";
                for (Map.Entry<String, String> resourceException : resourceExceptions.entrySet()) {
                    exceptionMessage += "\r\n" + "          Resource could not be processed: " + resourceException.getKey() + " - " + resourceException.getValue();
                }
                ourLog.warn("Measure could not be processed: " + libraryName + " - " + exceptionMessage);
            }
        }
    }

    public static Boolean bundleValueSets(String path, FhirContext fhirContext, Map<String, IAnyResource> resources, Map<String, String> resourceExceptions, Encoding encoding) {
        Boolean shouldPersist = true;
        // try {
        //     Map<String, IAnyResource> dependencies =
        //     ResourceUtils.getDepValueSetResources(path, fhirContext, encoding);
        //     for (IAnyResource resource : dependencies.values()) {
        //         resources.putIfAbsent(resource.getId(), resource);
        //     }
        // }
        // catch(Exception e) {
        //     shouldPersist = false;
        //     resourceExceptions.put(path, e.getMessage());
        // }
        return shouldPersist;
    }

    public static Boolean bundleDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources, Map<String, String> resourceExceptions, Encoding encoding) {
        Boolean shouldPersist = true;
        try {
            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
            for (IAnyResource resource : dependencies.values()) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            resourceExceptions.put(path, e.getMessage());
        }
        return shouldPersist;
    }

    private static Boolean bundleTestCases(String igPath, String libraryName, FhirContext fhirContext, Map<String, IAnyResource> resources, Map<String, String> resourceExceptions) {
        Boolean shouldPersist = true;
        String igTestsPath = FilenameUtils.concat(igPath, testCasePathElement);
        String igTestCasePath = FilenameUtils.concat(igTestsPath, libraryName);
        
        //this is breaking for bundle of a bundle. Replace with individual resources until we can figure it out.
        // List<String> testCaseSourcePaths = IOUtils.getFilePaths(igTestCasePath, false);
        // for (String testCaseSourcePath : testCaseSourcePaths) {
        //     shouldPersist = shouldPersist & safeAddResource(testCaseSourcePath, resources, fhirContext, resourceExceptions);
        // }

        try {
            List<IAnyResource> testCaseResources = TestCaseProcessor.getTestCaseResources(igTestCasePath, fhirContext);
            for (IAnyResource resource : testCaseResources) {
                resources.putIfAbsent(resource.getId(), resource);
            }
        } catch (Exception e) {
            shouldPersist = false;
            resourceExceptions.put(igTestCasePath, e.getMessage());
        }
        return shouldPersist;
    }

    private static void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IAnyResource> resources) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);
    }

    public static final String bundleFilesPathElement = "files/";
    public static final String valuesetsPathElement = "resources/valuesets/";

    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String measureSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includeTestCases) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(measureSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(measureSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.getFileName(libraryName, Encoding.CQL);
        String cqlLibrarySourcePath = FilenameUtils.concat(FilenameUtils.concat(igPath, cqlLibraryPathElement), cqlFileName);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {
            // String igValueSetsPath = FilenameUtils.concat(igPath, valuesetsPathElement);
            // Map<String, IAnyResource> valuesets = ResourceUtils.getDepValueSetResources(igValueSetsPath, fhirContext, encoding);
            // String valuesetsID = "valuesets-" + libraryName;
            // Object bundle = BundleUtils.bundleArtifacts(valuesetsID, new ArrayList<IAnyResource>(valuesets.values()), fhirContext);
        
            // IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);          
        }
        
        if (includeDependencies) {
            Map<String, IAnyResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding);
            String depLibrariesID = "library-deps-" + libraryName;
            Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IAnyResource>(depLibraries.values()), fhirContext);
        
            IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);          
        }

        if (includeTestCases) {
            bundleTestCaseFiles(igPath, libraryName, bundleDestFilesPath);
        }        
    }

    public static void bundleTestCaseFiles(String igPath, String libraryName, String destPath) {
        String igTestsPath = FilenameUtils.concat(igPath, testCasePathElement);
        String igTestCasePath = FilenameUtils.concat(igTestsPath, libraryName);
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
}
