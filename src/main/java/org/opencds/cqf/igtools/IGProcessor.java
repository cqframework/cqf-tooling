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
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class IGProcessor
{    
    public enum IGVersion 
    { 
        FHIR3("fhir3"), FHIR4("fhir4"); 
  
        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private IGVersion(String string) 
        { 
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

    public static void refreshIG(String igPath, IGVersion igVersion, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases,  Boolean includeVersion)
    {
        FhirContext fhirContext = getIgFhirContext(igVersion);
  
        //TODO: if refresh content is fhir version non-specific, no need for two
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

        if (includeTestCases)
        {
            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, testCasePathElement), IOUtils.Encoding.JSON, fhirContext);
        }
        bundleIg(igPath, includeELM, includeDependencies, includeTerminology, includeTestCases, includeVersion, fhirContext);
    }

    private static void refreshStu3IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext)
    {
        refreshStu3IgLibraryContent(igPath, includeELM, fhirContext);
        //refreshMeasureContent();
    }    

    private static void refreshR4IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext)
    {
        refreshR4LibraryContent(igPath, includeELM, fhirContext);
        //refreshMeasureContent();
    }

    public static final String libraryPathElement = "resources/library/";
    public static void refreshStu3IgLibraryContent(String igPath, Boolean includeELM, FhirContext fhirContext)
    {
        String libraryPath = FilenameUtils.concat(igPath, libraryPathElement);
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<DSTU3>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
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
    public static void bundleIg(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext) {
        //bundle
        /*
                - if include dependencies, add dependencies to bundle
                - if include terminiology, add terminology to bundle
                - if include test cases, add testcases to bundle
        */  
        //zip
        /*
                - if include dependencies, add bundle of libary dependencies to zip
                - if include terminology, add bundle of terminology to zip
                - if include test cases, add test cases to zip
             
        */   
        Encoding encoding = Encoding.JSON;
        
        String igMeasurePath = FilenameUtils.concat(igPath, measurePathElement);
        String igLibraryPath = FilenameUtils.concat(igPath, libraryPathElement);

        List<String> measureSourcePaths = IOUtils.getFilePaths(igMeasurePath, false);
        Boolean shouldPersist = true;
        for (String measureSourcePath : measureSourcePaths) {
            List<IAnyResource> resources = new ArrayList<IAnyResource>();
            Map<String, String> resourceExceptions = new HashMap<String, String>();
            String libraryName = FilenameUtils.getBaseName(measureSourcePath).replace("measure-", "");
            String librarySourcePath = FilenameUtils.concat(igLibraryPath, IOUtils.getFileName(LibraryProcessor.getId(libraryName), encoding));

            shouldPersist = safeAddResource(measureSourcePath, resources, fhirContext, resourceExceptions);    
            shouldPersist = shouldPersist & safeAddResource(librarySourcePath, resources, fhirContext, resourceExceptions);

            if (includeTestCases) {
                shouldPersist = shouldPersist & bundleTestCases(igPath, libraryName, fhirContext, resources, resourceExceptions);
            }

            if (shouldPersist) {
                String bundlePath = FilenameUtils.concat(igPath, bundlePathElement);
                String bundleDestPath = FilenameUtils.concat(bundlePath, libraryName);
                persistBundle(igPath, bundleDestPath, libraryName, encoding, fhirContext, resources);
                bundleFiles(igPath, bundleDestPath, libraryName, measureSourcePath, librarySourcePath);
            }
            else {
                String exceptionMessage = "";
                for (Map.Entry<String, String> resourceException : resourceExceptions.entrySet()) {
                    exceptionMessage += "\r\n" + "          Resource could not be processed: " + resourceException.getKey() + " - " + resourceException.getValue();
                }
                ourLog.warn("Measure could not be processed: " + libraryName + " - " + exceptionMessage);
            }
        } 
    }

    private static Boolean bundleTestCases(String igPath, String libraryName, FhirContext fhirContext, List<IAnyResource> resources, Map<String, String> resourceExceptions ) {
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
            resources.addAll(testCaseResources);
        }
        catch(Exception e) {
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
    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String measureSourcePath, String librarySourcePath) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(measureSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(measureSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.getFileName(libraryName, Encoding.CQL);
        String cqlLibrarySourcePath = FilenameUtils.concat(FilenameUtils.concat(igPath, cqlLibraryPathElement), cqlFileName);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);        
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);
    }

    private static Boolean safeAddResource(String path, List<IAnyResource> resources, FhirContext fhirContext, Map<String, String> resourceExceptions) {
        Boolean added = true;
        try {
             resources.add(IOUtils.readResource(path, fhirContext, true));
        }
        catch(Exception e) {
            added = false;
            resourceExceptions.put(path, e.getMessage());
        }  
        return added;
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
