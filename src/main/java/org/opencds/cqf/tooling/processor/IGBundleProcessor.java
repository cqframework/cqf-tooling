package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;

import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class IGBundleProcessor {
    public static final String bundleFilesPathElement = "files/";    

    public static void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, Encoding encoding, Boolean includeELM,
            Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean cdsHooksIg,
            FhirContext fhirContext, String fhirUri) {

        MeasureProcessor.bundleMeasures(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                fhirContext, fhirUri, encoding);

        PlanDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned, cdsHooksIg,
                fhirContext, fhirUri, encoding);
    }
}