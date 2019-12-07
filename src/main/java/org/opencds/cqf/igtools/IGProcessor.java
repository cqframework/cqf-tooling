package org.opencds.cqf.igtools;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.testcase.TestCaseProcessor;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class IGProcessor
{    
    public static final String stu3IgVersion = "fhir3";
    public static final String r4IgVersion = "fhir4";

    public static final String testCasePathElement = "tests/";

    public static void refreshIG(String igPath, String igVersion, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases) {
        refreshIG(igPath, igVersion, includeELM, includeDependencies, includeTerminology, includeTestCases, false);
    }

    public static void refreshIG(String igPath, String igVersion, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases,  Boolean versioned)
    {
        FhirContext context = getIgFhirContext(igVersion);
  
        switch (context.getVersion().getVersion()) {
            case DSTU3:
                refreshStu3IG(context, igPath, includeELM, includeDependencies, includeTerminology, includeTestCases);
                break;
            case R4:
                refreshR4IG(context, igPath, includeELM, includeDependencies, includeTerminology, includeTestCases);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + context.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void refreshStu3IG(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCases)
    {
        refreshStu3IgLibraryContent(fhirContext, igPath, includeELM);
        //refreshMeasureContent();
        if (includeTestCases)
        {
            TestCaseProcessor.refreshTestCases(fhirContext, FilenameUtils.concat(igPath, testCasePathElement));
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

    public static void refreshR4IG(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCasts)
    {
        refreshR4LibraryContent(fhirContext, igPath, includeELM);
        //refreshMeasureContent();
        //refreshTestCases();
    }

    public static final String libraryPathElement = "library/";
    public static void refreshStu3IgLibraryContent(FhirContext fhirContext, String igPath, Boolean includeELM)
    {
        String libraryPath = FilenameUtils.concat(igPath, libraryPathElement);
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<DSTU3>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
    }

    public static void refreshR4LibraryContent(FhirContext fhirContext, String igPath, Boolean includeELM)
    {
        String libraryPath = FilenameUtils.concat(igPath, libraryPathElement);
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<R4>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
    }

    public static FhirContext getIgFhirContext(String igVersion)
    {
        switch (igVersion) {
            case stu3IgVersion:
                return FhirContext.forDstu3();
            case r4IgVersion:
                return FhirContext.forR4();
            default:
                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
        }     
    }

    public static String getIgVersion(String igPath)
    {
        if (IOUtils.pathIncludesElement(igPath, stu3IgVersion))
        {
            return stu3IgVersion;
        }
        else if (IOUtils.pathIncludesElement(igPath, r4IgVersion))
        {
            return r4IgVersion;
        }
        throw new IllegalArgumentException("IG version not found in IG Path.");
    }
}
