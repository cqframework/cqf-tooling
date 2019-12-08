package org.opencds.cqf.igtools;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.IOUtils;

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
    }

    private static void refreshStu3IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases, Boolean includeVersion, FhirContext fhirContext)
    {
        refreshStu3IgLibraryContent(igPath, includeELM, fhirContext);
        //refreshMeasureContent();
        if (includeTestCases)
        {
            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, testCasePathElement), IOUtils.Encoding.JSON, fhirContext);
        }
        //bundle
        /*
            - iterate cql files
                - add measure to bundle
                - add libary to bundle
                - if include dependencies, add dependencies to bundle
                - if include terminiology, add terminology to bundle
                - if include test cases, include testcases to bundle
                - save bundle to bundle directory, by library name

        */   
        //zip
        /*
            - iterate cql files
                - add cql to zip
                - add measure to zip
                - add library to zip
                - if include dependencies, add bundle of libary dependencies to zip
                - if include terminology, add bundle of terminology to zip
                - if include test cases, add test cases to zip
                - save zip to bundle directory, by library name

        */   

    }    

    private static void refreshR4IG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCasts, Boolean includeVersion, FhirContext fhirContext)
    {
        refreshR4LibraryContent(igPath, includeELM, fhirContext);
        //refreshMeasureContent();
        //refreshTestCases();
    }

    public static final String libraryPathElement = "library/";
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
