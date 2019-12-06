package org.opencds.cqf.igtools;

import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class IGProcessor
{    
    public static final String stu3IgVersion = "fhir3";
    public static final String r4IgVersion = "fhir4";

    public static void refreshIG(String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCasts)
    {
        FhirContext context = getIgFhirContext(igPath);
  
        switch (context.getVersion().getVersion()) {
            case DSTU3:
                refreshStu3IG(context, igPath, includeELM, includeDependencies, includeTerminology, includeTestCasts);
                break;
            case R4:
                refreshR4IG(context, igPath, includeELM, includeDependencies, includeTerminology, includeTestCasts);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + context.getVersion().getVersion().getFhirVersionString());
        }
    }

    public static void refreshStu3IG(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCasts)
    {
        refreshStu3IgLibraryContent(fhirContext, igPath, includeELM, includeDependencies, includeTerminology);
        //refreshMeasureContent();
        if (includeTestCasts)
        {
            //refreshTestCases();
        }

        //bundle        

    }

    public static void refreshR4IG(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology, Boolean includeTestCasts)
    {
        refreshR4LibraryContent(fhirContext, igPath, includeELM, includeDependencies, includeTerminology);
        //refreshMeasureContent();
        //refreshTestCases();
    }

    public static void refreshStu3IgLibraryContent(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology)
    {
        String libraryPath = igPath + "/library";
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<DSTU3>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
    }

    public static void refreshR4LibraryContent(FhirContext fhirContext, String igPath, Boolean includeELM, Boolean includeDependencies, Boolean includeTerminology)
    {
        String libraryPath = igPath + "/library";
        //ILibraryProcessor libraryProcessor = new LibraryProcessor<R4>(libraryPath);
        //libraryProcessor.refreshLibraryContent();
    }

    public static FhirContext getIgFhirContext(String igPath)
    {
        if (IOUtils.pathIncludesElement(igPath, stu3IgVersion))
        {
            return FhirContext.forDstu3();
        }
        else if (IOUtils.pathIncludesElement(igPath, r4IgVersion))
        {
            return FhirContext.forR4();
        }
        throw new IllegalArgumentException("IG version not found.");
    }
}
