package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PlanDefinitionProcessor {
    public static final String ResourcePrefix = "plandefinition-";
    public static final String PlanDefinitionTestGroupName = "plandefinition";
    private LibraryProcessor libraryProcessor;
    private CDSHooksProcessor cdsHooksProcessor;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        this.libraryProcessor = libraryProcessor;
        this.cdsHooksProcessor = cdsHooksProcessor;
    }

    public void bundlePlanDefinitions(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Boolean includeDependencies,
                                      Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, Boolean addBundleTimestamp,
                                      FhirContext fhirContext, String fhirUri, Encoding encoding) {

        Map<String, IBaseResource> planDefinitions = IOUtils.getPlanDefinitions(fhirContext);

        List<String> bundledPlanDefinitions = new ArrayList<String>();

        //let OS handle threading:
        ExecutorService executorService = Executors.newCachedThreadPool();

        //build list of tasks via for loop:
        List<Callable<Void>> tasks = new ArrayList<>();
        try {
            for (Map.Entry<String, IBaseResource> planDefinitionEntry : planDefinitions.entrySet()) {
                LogUtils.info("bundlePlanDefinitions, collecting task: " + planDefinitionEntry.getKey());
                tasks.add(() -> {
                    LogUtils.info("bundlePlanDefinitions, task running: " + planDefinitionEntry.getKey());
                    try {
                        String planDefinitionSourcePath = IOUtils.getPlanDefinitionPathMap(fhirContext).get(planDefinitionEntry.getKey());

                        // Assumption - File name matches planDefinition.name
                        String planDefinitionName = FilenameUtils.getBaseName(planDefinitionSourcePath).replace(PlanDefinitionProcessor.ResourcePrefix, "");
                        try {
                            Map<String, IBaseResource> resources = new HashMap<String, IBaseResource>();

                            Boolean shouldPersist = ResourceUtils.safeAddResource(planDefinitionSourcePath, resources, fhirContext);
                            if (!resources.containsKey("PlanDefinition/" + planDefinitionEntry.getKey())) {
                                throw new IllegalArgumentException(String.format("Could not retrieve base resource for PlanDefinition %s", planDefinitionName));
                            }
                            IBaseResource planDefinition = resources.get("PlanDefinition/" + planDefinitionEntry.getKey());
                            String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(planDefinition, fhirContext);
                            IBaseResource primaryLibrary;
                            if (primaryLibraryUrl.startsWith("http")) {
                                primaryLibrary = IOUtils.getLibraryUrlMap(fhirContext).get(primaryLibraryUrl);
                            } else {
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

                            String cqlFileName = IOUtils.formatFileName(primaryLibraryName, Encoding.CQL, fhirContext);

                            String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(primaryLibraryName, cqlFileName, binaryPaths);

                            if (cqlLibrarySourcePath == null) {
                                throw new IllegalArgumentException(String.format("Could not determine CqlLibrarySource path for library %s", primaryLibraryName));
                            }

                            if (includeTerminology) {
                                boolean result = ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
                                if (shouldPersist && !result) {
                                    LogUtils.info("PlanDefinition will not be bundled because ValueSet bundling failed.");
                                }
                                shouldPersist = shouldPersist & result;
                            }

                            if (includeDependencies) {
                                boolean result = libraryProcessor.bundleLibraryDependencies(primaryLibrarySourcePath, fhirContext, resources, encoding, includeVersion);
                                if (shouldPersist && !result) {
                                    LogUtils.info("PlanDefinition will not be bundled because Library Dependency bundling failed.");
                                }
                                shouldPersist = shouldPersist & result;
                            }

                            if (includePatientScenarios) {
                                boolean result = TestCaseProcessor.bundleTestCases(igPath, PlanDefinitionTestGroupName, primaryLibraryName, fhirContext, resources);
                                if (shouldPersist && !result) {
                                    LogUtils.info("PlanDefinition will not be bundled because Test Case bundling failed.");
                                }
                                shouldPersist = shouldPersist & result;
                            }

                            List<String> activityDefinitionPaths = CDSHooksProcessor.bundleActivityDefinitions(planDefinitionSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);

                            if (shouldPersist) {
                                String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), PlanDefinitionTestGroupName), planDefinitionName);
                                persistBundle(igPath, bundleDestPath, planDefinitionName, encoding, fhirContext, new ArrayList<IBaseResource>(resources.values()), fhirUri, addBundleTimestamp);
                                bundleFiles(igPath, bundleDestPath, primaryLibraryName, binaryPaths, planDefinitionSourcePath, primaryLibrarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion, addBundleTimestamp);
                                cdsHooksProcessor.addActivityDefinitionFilesToBundle(igPath, bundleDestPath, activityDefinitionPaths, fhirContext, encoding);
                                bundledPlanDefinitions.add(planDefinitionSourcePath);
                            }
                        } catch (Exception e) {
                            LogUtils.putException(planDefinitionName, e);
                        } finally {
                            LogUtils.warn(planDefinitionName);
                        }
                        //end for

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    LogUtils.info("bundlePlanDefinitions, task complete: " + planDefinitionEntry.getKey());
                    //task requires a return statement
                    return null;
                });

            }//end for loop

            // Submit tasks and obtain futures
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                futures.add(executorService.submit(task));
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                try {
                    future.get(); // This will block until the task is complete
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            // Shutdown the executor when you're done, even if an exception occurs
            executorService.shutdown();
        }

        String message = "\r\n" + bundledPlanDefinitions.size() + " PlanDefinitions successfully bundled:";
        for (String bundledPlanDefinition : bundledPlanDefinitions) {
            message += "\r\n     " + bundledPlanDefinition + " BUNDLED";
        }

        List<String> planDefinitionPathLibraryNames = new ArrayList<>(IOUtils.getPlanDefinitionPaths(fhirContext));
        ArrayList<String> failedPlanDefinitions = new ArrayList<>(planDefinitionPathLibraryNames);
        planDefinitionPathLibraryNames.removeAll(bundledPlanDefinitions);
        planDefinitionPathLibraryNames.retainAll(refreshedLibraryNames);
        message += "\r\n" + planDefinitionPathLibraryNames.size() + " PlanDefinitions refreshed, but not bundled (due to issues):";
        for (String notBundled : planDefinitionPathLibraryNames) {
            message += "\r\n     " + notBundled + " REFRESHED";
        }

        failedPlanDefinitions.removeAll(bundledPlanDefinitions);
        failedPlanDefinitions.removeAll(planDefinitionPathLibraryNames);
        message += "\r\n" + failedPlanDefinitions.size() + " PlanDefinitions failed refresh:";
        for (String failed : failedPlanDefinitions) {
            message += "\r\n     " + failed + " FAILED";
        }

        LogUtils.info(message);
    }

    private void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IBaseResource> resources, String fhirUri, Boolean addBundleTimestamp) {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext, addBundleTimestamp);
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.equals("")) {
            try {
                HttpClientUtils.post(fhirUri, (IBaseResource) bundle, encoding, fhirContext);
            } catch (IOException e) {
                LogUtils.putException(((IBaseResource) bundle).getIdElement().getIdPart(), "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
                File dir = new File("C:\\src\\GitHub\\logs");
                dir.mkdir();
                IOUtils.writeBundle(bundle, dir.getAbsolutePath(), encoding, fhirContext);
            }
        }
    }

    private void bundleFiles(String igPath, String bundleDestPath, String libraryName, List<String> binaryPaths, String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios, Boolean includeVersion, Boolean addBundleTimestamp) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, FilenameUtils.getBaseName(bundleDestPath) + "-" + IGBundleProcessor.bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL, fhirContext);
        String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(libraryName, cqlFileName, binaryPaths);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {
            try {
                Map<String, IBaseResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IBaseResource>(valuesets.values()), fhirContext, addBundleTimestamp);
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
                }
            } catch (Exception e) {
                LogUtils.putException(libraryName, e.getMessage());
            }
        }

        if (includeDependencies) {
            Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding, includeVersion, logger);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IBaseResource>(depLibraries.values()), fhirContext, addBundleTimestamp);
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
            }
        }

        if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, "plandefinition", libraryName, bundleDestFilesPath, fhirContext);
        }
    }
}