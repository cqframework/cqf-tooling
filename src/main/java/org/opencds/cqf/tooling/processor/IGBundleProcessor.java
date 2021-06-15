package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;

import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;
/**
 * @author Adam Stevenson
 */
public class IGBundleProcessor {
    public static final String bundleFilesPathElement = "files/";  
    MeasureProcessor measureProcessor;
    PlanDefinitionProcessor planDefinitionProcessor;  

    public IGBundleProcessor(MeasureProcessor measureProcessor, PlanDefinitionProcessor planDefinitionProcessor) {
        this.measureProcessor = measureProcessor;
        this.planDefinitionProcessor = planDefinitionProcessor;
    }

    public void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, Encoding encoding, Boolean includeELM,
            Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean cdsHooksIg,
            FhirContext fhirContext, String fhirUri) {

        measureProcessor.bundleMeasures(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                fhirContext, fhirUri, encoding);

        planDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned, cdsHooksIg,
                fhirContext, fhirUri, encoding);
    }
}