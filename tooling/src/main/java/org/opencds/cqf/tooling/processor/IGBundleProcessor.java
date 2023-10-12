package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Future<?>> futures = new ArrayList<>();
//
        futures.add(executorService.submit(() -> {
            measureProcessor.bundleMeasures(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology, includePatientScenarios, versioned,
                    addBundleTimestamp, fhirContext, fhirUri, encoding);
        }));

        futures.add(executorService.submit(() -> {
            planDefinitionProcessor.bundlePlanDefinitions(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                    includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);
        }));

        futures.add(executorService.submit(() -> {
            questionnaireProcessor.bundleQuestionnaires(refreshedLibraryNames, igPath, binaryPaths, includeDependencies, includeTerminology,
                    includePatientScenarios, versioned, addBundleTimestamp, fhirContext, fhirUri, encoding);
        }));

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(); // This will block until the task is complete
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // All tasks have completed
        System.out.println("All tasks are done.");

        // Shutdown the executor when you're done
        executorService.shutdown();
    }
}
