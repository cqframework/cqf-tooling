package org.opencds.cqf.processor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.opencds.cqf.parameter.RefreshIGParameters;
import org.opencds.cqf.parameter.RefreshLibraryParameters;
import org.opencds.cqf.processor.IGProcessor.IGVersion;
import org.opencds.cqf.utilities.IGUtils;
import org.opencds.cqf.utilities.IOUtils;
import org.opencds.cqf.utilities.LogUtils;
import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.utilities.ResourceUtils;

public class IGRefreshProcessor {

    public static ArrayList<String> refreshedResourcesNames = new ArrayList<String>();
    public static void refreshIG(RefreshIGParameters params) {

        String igResourcePath = params.igResourcePath;
        String igPath = params.igPath;
        IGVersion igVersion = params.igVersion;
        Encoding encoding = params.outputEncoding;
        Boolean includeELM = params.includeELM;
        Boolean includeDependencies = params.includeDependencies;
        Boolean includeTerminology = params.includeTerminology;
        Boolean includePatientScenarios = params.includePatientScenarios;
        Boolean versioned = params.versioned;
        String fhirUri = params.fhirUri;
        String measureToRefreshPath = params.measureToRefreshPath;
        ArrayList<String> resourceDirs = params.resourceDirs;

        IOUtils.resourceDirectories.addAll(resourceDirs);

        FhirContext fhirContext = IGProcessor.getIgFhirContext(igVersion);
        Boolean igResourcePathIsSpecified = igResourcePath != null && !igResourcePath.isEmpty() && !igResourcePath.isBlank();
        IAnyResource implementationGuide = null;
        String igCanonicalBase = null;

        IGProcessor.ensure(igPath, includePatientScenarios, includeTerminology, IOUtils.resourceDirectories);

        LibraryProcessor libraryProcessor;
        switch (fhirContext.getVersion().getVersion()) {
        case DSTU3:
            libraryProcessor = new STU3LibraryProcessor();
            break;
        case R4:
            libraryProcessor = new R4LibraryProcessor();
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        if (igResourcePathIsSpecified) {
            implementationGuide = IOUtils.readResource(igResourcePath, fhirContext, false);

            Object urlProperty = ResourceUtils.resolveProperty(implementationGuide, "url", fhirContext);
            String urlValue = ResourceUtils.resolveProperty(urlProperty, "value", fhirContext).toString();

            if (urlValue != null && !urlValue.isEmpty() && !urlValue.isBlank()) {
                igCanonicalBase = IGUtils.getImplementationGuideCanonicalBase(urlValue);

            }
        }

        refreshedResourcesNames = refreshIgLibraryContent(igCanonicalBase, libraryProcessor, igPath, encoding, includeELM, versioned, fhirContext, igVersion);

        List<String> refreshedMeasureNames = new ArrayList<String>();
        refreshedMeasureNames = MeasureProcessor.refreshIgMeasureContent(igPath, encoding, versioned, fhirContext, measureToRefreshPath);
        refreshedResourcesNames.addAll(refreshedMeasureNames);

        if (refreshedResourcesNames.isEmpty()) {
            LogUtils.info("No libraries successfully refreshed.");
            return;
        }

        if (includePatientScenarios) {
            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, IGProcessor.testCasePathElement), encoding, fhirContext, refreshedResourcesNames);
        }
    }

    public static ArrayList<String> refreshIgLibraryContent(String igCanonicalBase, LibraryProcessor libraryProcessor, String igPath, Encoding outputEncoding, Boolean includeELM,
            Boolean versioned, FhirContext fhirContext, IGVersion igVersion) {
                ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
                HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
        
                for (String cqlPath : cqlContentPaths) {
                    try {
                        //ask about how to do this better
                        String libraryPath;
                        try {
                            libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(cqlPath, fhirContext);
                        } catch (Exception e) {
                            libraryPath = "";
                        }
                        RefreshLibraryParameters lp = new RefreshLibraryParameters();
                        lp.igCanonicalBase = igCanonicalBase;
                        lp.cqlContentPath = cqlPath;
                        lp.libraryPath = libraryPath;
                        lp.fhirContext = fhirContext;
                        lp.encoding = outputEncoding;
                        lp.versioned = versioned;
                        libraryProcessor.refreshLibraryContent(lp);
                        refreshedLibraryNames.add(FilenameUtils.getBaseName(cqlPath));
                    } catch (Exception e) {
                        LogUtils.putException(cqlPath, e);
                    }
                    LogUtils.warn(cqlPath);
                }
        
                return refreshedLibraryNames;
    }

}