package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.LogUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IGBundleProcessor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
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


        logger.info("\r\n  [Bundle Measures has started]");
        measureProcessor.bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding);

        logger.info("\r\n  [Bundle Measures has finished]\r\n");

        //this message can be moved to any point of this process, but so far it's just the bundle measure process
        //that will persist test files. If Questionnaires and PlanDefinitions should ever need test files as well
        //persistTestFiles can be moved to AbstractResourceProcessor from MeasureProcessor instead of abstract sig
        logger.info("Total \"tests-*\" files copied: " + IOUtils.copyFileCounter() + ". " +
                (fhirUri != null && !fhirUri.isEmpty() ? "These files will be posted to " + fhirUri : "")
        );
        

        logger.info("\r\n  [Bundle PlanDefinitions has started]");
        planDefinitionProcessor.bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding);
        logger.info("\r\n  [Bundle PlanDefinitions has finished]\r\n");

        logger.info("\r\n  [Bundle Questionnaires has started]");
        questionnaireProcessor.bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding);
        logger.info("\r\n  [Bundle Questionnaires has finished]\r\n");



        //run collected post calls last:
        if (HttpClientUtils.hasPostTasksInQueue()) {
            logger.info("\r\n  [HttpClientUtils.hasPostTasksInQueue = true. HttpClientUtils.postTaskCollection() has started]");
            HttpClientUtils.postTaskCollection();
            logger.info("\r\n  [HttpClientUtils.postTaskCollection() has finished]");
        }

        // run cleanup (maven runs all tests sequentially and static member variables retain values from previous tests)
        IOUtils.cleanUp();
        ResourceUtils.cleanUp();
        TestCaseProcessor.cleanUp();
    }

}