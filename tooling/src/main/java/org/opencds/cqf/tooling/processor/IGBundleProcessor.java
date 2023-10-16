package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;
import java.util.List;

import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.utilities.LogUtils;

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

    public void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Encoding encoding, Boolean includeELM,
                         Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean addBundleTimestamp,
                         FhirContext fhirContext, String fhirUri) {


        LogUtils.info("\r\n  [measureProcessor.bundleMeasures has started]");
        measureProcessor.bundleMeasures(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                addBundleTimestamp, fhirContext, fhirUri, encoding);
        LogUtils.info("\r\n  [measureProcessor.bundleMeasures has finished]\r\n");

        LogUtils.info("\r\n  [planDefinitionProcessor.bundlePlanDefinitions has started]");
        planDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);
        LogUtils.info("\r\n  [planDefinitionProcessor.bundlePlanDefinitions has finished]\r\n");

        LogUtils.info("\r\n  [questionnaireProcessor.bundleQuestionnaires has started]");
        questionnaireProcessor.bundleQuestionnaires(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);
        LogUtils.info("\r\n  [questionnaireProcessor.bundleQuestionnaires has finished]\r\n");

        //run collected post calls last:
        if (HttpClientUtils.hasPostTasksInQueue()){
            LogUtils.info("\r\n  [HttpClientUtils.hasPostTasksInQueue = true. HttpClientUtils.postTaskCollection() has started]");
            HttpClientUtils.postTaskCollection();
            LogUtils.info("\r\n  [HttpClientUtils.postTaskCollection() has finished]");
        }
    }
}