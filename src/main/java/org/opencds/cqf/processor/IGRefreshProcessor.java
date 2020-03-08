package org.opencds.cqf.processor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FilenameUtils;
import org.opencds.cqf.parameter.RefreshIGParameters;
import org.opencds.cqf.processor.IGProcessor.IGVersion;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class IGRefreshProcessor {

    public static ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
    public static void refreshIG(RefreshIGParameters params) {

        String igPath = params.igPath;
        IGVersion igVersion = params.igVersion;
        Encoding encoding = params.outputEncoding;
        Boolean includeELM = params.includeELM;
        Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        String fhirUri = params.fhirUri;
        ArrayList<String> resourceDirs = params.resourceDirs;

        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = IGProcessor.getIgFhirContext(igVersion);

        igPath = Paths.get(igPath).toAbsolutePath().toString();

        IGProcessor.ensure(igPath, includePatientScenarios, includeTerminology, IOUtils.resourceDirectories);

        LibraryProcessor libraryProcessor;
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            libraryProcessor = new STU3LibraryProcessor();
            refreshedLibraryNames = refreshIgLibraryContent(libraryProcessor, igPath, encoding, includeELM, versioned, fhirContext);
            break;
        case R4:
            libraryProcessor = new R4LibraryProcessor();
            refreshedLibraryNames = refreshIgLibraryContent(libraryProcessor, igPath, encoding, includeELM, versioned, fhirContext);
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
            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), encoding, fhirContext);
        }
    }

    public static ArrayList<String> refreshIgLibraryContent(LibraryProcessor libraryProcessor, String igPath, Encoding outputEncoding, Boolean includeELM,
            Boolean versioned, FhirContext fhirContext) {
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
                        libraryProcessor.refreshLibraryContent(path, libraryPath, fhirContext, outputEncoding, versioned);
                        refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
                    } catch (Exception e) {
                        LogUtils.putWarning(path, e.getMessage());
                    }
                    LogUtils.warn(path);
                }
        
                return refreshedLibraryNames;
    }

}