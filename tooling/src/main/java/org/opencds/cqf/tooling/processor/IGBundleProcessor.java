package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureBundler;
import org.opencds.cqf.tooling.questionnaire.QuestionnaireBundler;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IGBundleProcessor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final String bundleFilesPathElement = "files/";

    private Boolean includeErrors = true;
    LibraryProcessor libraryProcessor;
    CDSHooksProcessor cdsHooksProcessor;

    public IGBundleProcessor(Boolean includeErrors, LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        this.includeErrors = includeErrors;
        this.libraryProcessor = libraryProcessor;
        this.cdsHooksProcessor = cdsHooksProcessor;
    }

    public void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Encoding encoding, Boolean includeELM,
                         Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, Boolean addBundleTimestamp,
                         FhirContext fhirContext, String fhirUri) {

        System.out.println("\n");

        System.out.println("\r\n[Bundle Measures has started - " + getTime() + "]\r\n");
        new MeasureBundler().bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding, includeErrors);

        //this message can be moved to any point of this process, but so far it's just the bundle measure process
        //that will persist test files. If Questionnaires and PlanDefinitions should ever need test files as well
        //persistTestFiles can be moved to AbstractResourceProcessor from MeasureProcessor instead of abstract sig
        System.out.println("\r\nTotal test files copied: " + IOUtils.copyFileCounter() + ". " +
                (fhirUri != null && !fhirUri.isEmpty() ? "These files will be posted to " + fhirUri : "")
        );
        System.out.println("\r\n[Bundle Measures has finished - " + getTime() + "]\r\n");


        System.out.println("\r\n[Bundle PlanDefinitions has started - " + getTime() + "]\r\n");
        new PlanDefinitionBundler(this.libraryProcessor, this.cdsHooksProcessor).bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding, includeErrors);
        System.out.println("\r\n[Bundle PlanDefinitions has finished - " + getTime() + "]\r\n");



        System.out.println("\r\n[Bundle Questionnaires has started - " + getTime() + "]\r\n");
        new QuestionnaireBundler(this.libraryProcessor).bundleResources(refreshedLibraryNames,
                igPath, binaryPaths, includeDependencies, includeTerminology,
                includePatientScenarios, versioned, addBundleTimestamp, fhirContext,
                fhirUri, encoding, includeErrors);
        System.out.println("\r\n[Bundle Questionnaires has finished - " + getTime() + "]\r\n");



        //run collected post calls last:
        if (HttpClientUtils.hasPostTasksInQueue()) {
            System.out.println("\r\n[POST task(s) found in queue. POST task(s) started - " + getTime() + "]");
            HttpClientUtils.postTaskCollection();
            System.out.println("\r\n[POST task(s) finished - " + getTime() + "]");
        }

        // run cleanup (maven runs all ci tests sequentially and static member variables could retain values from previous tests)
        IOUtils.cleanUp();
        ResourceUtils.cleanUp();
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}