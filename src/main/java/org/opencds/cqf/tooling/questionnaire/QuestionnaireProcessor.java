package org.opencds.cqf.tooling.questionnaire;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.*;
import org.opencds.cqf.tooling.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionnaireProcessor {
    public static final String ResourcePrefix = "questionnaire-";
    public static final String QuestionnaireTestGroupName = "questionnaire";
    private LibraryProcessor libraryProcessor;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public QuestionnaireProcessor(LibraryProcessor libraryProcessor) {
        this.libraryProcessor = libraryProcessor;
    }

    public void bundleQuestionnaires(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Boolean includeDependencies,
                                      Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion,
                                      FhirContext fhirContext, String fhirUri, IOUtils.Encoding encoding) {

        Map<String, IBaseResource> questionnaires = IOUtils.getQuestionnaires(fhirContext);

        List<String> bundledQuestionnaires = new ArrayList<String>();
        for (Map.Entry<String, IBaseResource> questionnaireEntry : questionnaires.entrySet()) {
            String questionnaireSourcePath = IOUtils.getQuestionnairePathMap(fhirContext).get(questionnaireEntry.getKey());

            // Assumption - File name matches questionnaire.name
            String questionnaireName = FilenameUtils.getBaseName(questionnaireSourcePath).replace(org.opencds.cqf.tooling.questionnaire.QuestionnaireProcessor.ResourcePrefix, "");
            try {
                Map<String, IBaseResource> resources = new HashMap<String, IBaseResource>();

                Boolean shouldPersist = ResourceUtils.safeAddResource(questionnaireSourcePath, resources, fhirContext);
                if (!resources.containsKey("Questionnaire/" + questionnaireEntry.getKey())) {
                    throw new IllegalArgumentException(String.format("Could not retrieve base resource for Questionnaire %s", questionnaireName));
                }
                IBaseResource questionnaire = resources.get("Questionnaire/" + questionnaireEntry.getKey());

                String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(questionnaire, fhirContext);
                LogUtils.info(String.format("primaryLibraryUrl: %s", primaryLibraryUrl));

                IBaseResource primaryLibrary;
                if (primaryLibraryUrl.startsWith("http")) {
                    primaryLibrary = IOUtils.getLibraryUrlMap(fhirContext).get(primaryLibraryUrl);
                }
                else {
                    primaryLibrary = IOUtils.getLibraries(fhirContext).get(primaryLibraryUrl);
                }

                if (primaryLibrary == null)
                    throw new IllegalArgumentException(String.format("Could not resolve library url %s", primaryLibraryUrl));

                String primaryLibrarySourcePath = IOUtils.getLibraryPathMap(fhirContext).get(primaryLibrary.getIdElement().getIdPart());
                String primaryLibraryName = ResourceUtils.getName(primaryLibrary, fhirContext);
                if (includeVersion) {
                    primaryLibraryName = primaryLibraryName + "-" +
                            fhirContext.newFhirPath().evaluateFirst(primaryLibrary, "version", IBase.class).get().toString();
                }

                shouldPersist = shouldPersist
                        & ResourceUtils.safeAddResource(primaryLibrarySourcePath, resources, fhirContext);

                String cqlFileName = IOUtils.formatFileName(primaryLibraryName, IOUtils.Encoding.CQL, fhirContext);

                String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(primaryLibraryName, cqlFileName, binaryPaths);

                if (cqlLibrarySourcePath == null) {
                    throw new IllegalArgumentException(String.format("Could not determine CqlLibrarySource path for library %s", primaryLibraryName));
                }

                if (includeTerminology) {
                    boolean result = ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
                    if (shouldPersist && !result) {
                        LogUtils.info("Questionnaire will not be bundled because ValueSet bundling failed.");
                    }
                    shouldPersist = shouldPersist & result;
                }

                if (includeDependencies) {
                    boolean result = libraryProcessor.bundleLibraryDependencies(primaryLibrarySourcePath, fhirContext, resources, encoding, includeVersion);
                    if (shouldPersist && !result) {
                        LogUtils.info("Questionnaire will not be bundled because Library Dependency bundling failed.");
                    }
                    shouldPersist = shouldPersist & result;
                }

                if (includePatientScenarios) {
                    boolean result = TestCaseProcessor.bundleTestCases(igPath, QuestionnaireTestGroupName, primaryLibraryName, fhirContext, resources);
                    if (shouldPersist && !result) {
                        LogUtils.info("Questionnaire will not be bundled because Test Case bundling failed.");
                    }
                    shouldPersist = shouldPersist & result;
                }

                if (shouldPersist) {
                    String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), QuestionnaireTestGroupName), questionnaireName);
                    persistBundle(igPath, bundleDestPath, questionnaireName, encoding, fhirContext, new ArrayList<IBaseResource>(resources.values()), fhirUri);
                    bundleFiles(igPath, bundleDestPath, primaryLibraryName, binaryPaths, questionnaireSourcePath, primaryLibrarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion);
                    bundledQuestionnaires.add(questionnaireSourcePath);
                }
            } catch (Exception e) {
                LogUtils.putException(questionnaireName, e);
            } finally {
                LogUtils.warn(questionnaireName);
            }
        }

        String message = "\r\n" + bundledQuestionnaires.size() + " Questionnaires successfully bundled:";
        for (String bundledQuestionnaire : bundledQuestionnaires) {
            message += "\r\n     " + bundledQuestionnaire + " BUNDLED";
        }

        List<String> questionnairePathLibraryNames = new ArrayList<>(IOUtils.getQuestionnairePaths(fhirContext));
        ArrayList<String> failedQuestionnaires = new ArrayList<>(questionnairePathLibraryNames);
        questionnairePathLibraryNames.removeAll(bundledQuestionnaires);
        questionnairePathLibraryNames.retainAll(refreshedLibraryNames);
        message += "\r\n" + questionnairePathLibraryNames.size() + " Questionnaires refreshed, but not bundled (due to issues):";
        for (String notBundled : questionnairePathLibraryNames) {
            message += "\r\n     " + notBundled + " REFRESHED";
        }

        failedQuestionnaires.removeAll(bundledQuestionnaires);
        failedQuestionnaires.removeAll(questionnairePathLibraryNames);
        message += "\r\n" + failedQuestionnaires.size() + " Questionnaires failed refresh:";
        for (String failed : failedQuestionnaires) {
            message += "\r\n     " + failed + " FAILED";
        }

        LogUtils.info(message);
    }

    private void persistBundle(String igPath, String bundleDestPath, String libraryName, IOUtils.Encoding encoding, FhirContext fhirContext, List<IBaseResource> resources, String fhirUri) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext, null);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, (IBaseResource) bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putException(((IBaseResource)bundle).getIdElement().getIdPart(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
                File dir = new File("C:\\src\\GitHub\\logs");
                dir.mkdir();
                IOUtils.writeBundle(bundle, dir.getAbsolutePath(), encoding, fhirContext);
            }
        }
    }

    private void bundleFiles(String igPath, String bundleDestPath, String libraryName, List<String> binaryPaths, String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext, IOUtils.Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios, Boolean includeVersion) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, FilenameUtils.getBaseName(bundleDestPath) + "-" + IGBundleProcessor.bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(libraryName, IOUtils.Encoding.CQL, fhirContext);
        String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(libraryName, cqlFileName, binaryPaths);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {
            try {
                Map<String, IBaseResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IBaseResource>(valuesets.values()), fhirContext, null);
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
                }
            }  catch (Exception e) {
                LogUtils.putException(libraryName, e.getMessage());
            }
        }

        if (includeDependencies) {
            Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding, includeVersion, logger);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IBaseResource>(depLibraries.values()), fhirContext, null);
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
            }
        }

        if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, "questionnaire", libraryName, bundleDestFilesPath, fhirContext);
        }
    }
}