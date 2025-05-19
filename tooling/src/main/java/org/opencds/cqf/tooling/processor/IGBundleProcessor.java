package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.packaging.PackageMeasures;
import org.opencds.cqf.tooling.packaging.PackagePlanDefinitions;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireBundler;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IGBundleProcessor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String bundleFilesPathElement = "files/";

    private final Boolean verboseMessaging;
    LibraryProcessor libraryProcessor;
    CDSHooksProcessor cdsHooksProcessor;

    public IGBundleProcessor(Boolean verboseMessaging, LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        this.verboseMessaging = verboseMessaging;
        this.libraryProcessor = libraryProcessor;
        this.cdsHooksProcessor = cdsHooksProcessor;
    }

    public void bundleIg(List<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Encoding encoding, Boolean includeELM,
                         Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean addBundleTimestamp,
                         FhirContext fhirContext, String fhirUri) {

//        new MeasureBundler().bundleResources(refreshedLibraryNames,
//                igPath, binaryPaths, includeDependencies, includeTerminology,
//                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
//                fhirUri, encoding, verboseMessaging);
        new PackageMeasures(igPath, fhirContext, includeDependencies, includeTerminology, includePatientScenarios, fhirUri);
//        new PlanDefinitionBundler(this.libraryProcessor, this.cdsHooksProcessor).bundleResources(refreshedLibraryNames,
//                igPath, binaryPaths, includeDependencies, includeTerminology,
//                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
//                fhirUri, encoding, verboseMessaging);
        new PackagePlanDefinitions(igPath, fhirContext, includeDependencies, includeTerminology, includePatientScenarios, fhirUri);
        new QuestionnaireBundler(this.libraryProcessor).bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding, verboseMessaging);

        //run collected post calls last:
        if (HttpClientUtils.hasPostTasksInQueue()) {
            logger.info("[Persisting Files to {}]", fhirUri);
            HttpClientUtils.postTaskCollection();
        }

        // run cleanup (maven runs all ci tests sequentially and static member variables could retain values from previous tests)
        IOUtils.cleanUp();
        ResourceUtils.cleanUp();
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
