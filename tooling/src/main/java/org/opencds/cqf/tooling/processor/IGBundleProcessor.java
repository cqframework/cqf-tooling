package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;
import java.util.List;

import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class IGBundleProcessor {
    public static final String bundleFilesPathElement = "files/";  
    MeasureProcessor measureProcessor;
    PlanDefinitionProcessor planDefinitionProcessor;
    QuestionnaireProcessor questionnaireProcessor;

    public IGBundleProcessor(MeasureProcessor measureProcessor, PlanDefinitionProcessor planDefinitionProcessor, QuestionnaireProcessor questionnaireProcessor) {
        this.measureProcessor = measureProcessor;
        this.planDefinitionProcessor = planDefinitionProcessor;
        this.questionnaireProcessor = questionnaireProcessor;
    }

    public void bundleIg(List<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Encoding encoding, Boolean includeELM,
    Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean addBundleTimestamp,
    FhirContext fhirContext, String fhirUri) {

        measureProcessor.bundleMeasures(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                addBundleTimestamp,fhirContext, fhirUri, encoding);

        planDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
        includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);

        questionnaireProcessor.bundleQuestionnaires(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);
    }
}