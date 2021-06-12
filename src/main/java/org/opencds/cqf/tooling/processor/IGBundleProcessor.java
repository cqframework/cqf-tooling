package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;
import java.util.List;

import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class IGBundleProcessor {
    public static final String bundleFilesPathElement = "files/";    

    public static void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Encoding encoding, Boolean includeELM,
                                Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned,
                                FhirContext fhirContext, String fhirUri) {

        MeasureProcessor.bundleMeasures(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                fhirContext, fhirUri, encoding);

        PlanDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, fhirContext, fhirUri, encoding);
    }
}