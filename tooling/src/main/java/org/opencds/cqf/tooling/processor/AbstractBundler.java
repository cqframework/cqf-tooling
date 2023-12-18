package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.cql.exception.CqlTranslatorException;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.MeasureBundler;
import org.opencds.cqf.tooling.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An abstract base class for bundlers that handle the bundling of various types of resources within an ig.
 * This class provides methods for bundling resources, including dependencies and test cases, and handles the execution of associated tasks.
 * Subclasses must implement specific methods for gathering, processing, and persisting resources.
 */
public abstract class AbstractBundler {
    public static final String separator = System.getProperty("file.separator");
    public static final String NEWLINE_INDENT2 = "\n\t\t";
    public static final String NEWLINE_INDENT = "\r\n\t";
    public static final String INDENT = "\t";
    public static final String NEWLINE = "\r\n";
    /**
     * The logger for logging messages specific to the implementing class.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The resource type constant for Questionnaire.
     */
    protected final String TYPE_QUESTIONNAIRE = "Questionnaire";

    /**
     * The resource type constant for PlanDefinition.
     */
    protected final String TYPE_PLAN_DEFINITION = "PlanDefinition";

    /**
     * The resource type constant for Measure.
     */
    protected final String TYPE_MEASURE = "Measure";
    private List<Object> identifiers;
    private CDSHooksProcessor cdsHooksProcessor;
    private LibraryProcessor libraryProcessor;

    /**
     * Sets the LibraryProcessor for handling library-related tasks.
     *
     * @param libraryProcessor The LibraryProcessor instance to set.
     */
    protected void setLibraryProcessor(LibraryProcessor libraryProcessor) {
        this.libraryProcessor = libraryProcessor;
    }

    /**
     * Sets the CDSHooksProcessor for handling CDS Hooks-related tasks.
     *
     * @param cdsHooksProcessor The CDSHooksProcessor instance to set.
     */
    protected void setCDSHooksProcessor(CDSHooksProcessor cdsHooksProcessor) {
        this.cdsHooksProcessor = cdsHooksProcessor;
    }

    protected List<Object> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new CopyOnWriteArrayList<>();
        }
        return identifiers;
    }

    private String getResourcePrefix() {
        return getResourceBundlerType().toLowerCase() + "-";
    }

    protected abstract Set<String> getPaths(FhirContext fhirContext);

    protected abstract String getSourcePath(FhirContext fhirContext, Map.Entry<String, IBaseResource> resourceEntry);

    /**
     * Handled by the child class in gathering specific IBaseResources
     */
    protected abstract Map<String, IBaseResource> getResources(FhirContext fhirContext);

    protected abstract String getResourceBundlerType();

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * Bundles resources within an Implementation Guide based on specified options.
     *
     * @param refreshedLibraryNames   A list of refreshed library names.
     * @param igPath                  The path to the IG.
     * @param binaryPaths             The list of binary paths.
     * @param includeDependencies     Flag indicating whether to include dependencies.
     * @param includeTerminology      Flag indicating whether to include terminology.
     * @param includePatientScenarios Flag indicating whether to include patient scenarios.
     * @param includeVersion          Flag indicating whether to include version information.
     * @param addBundleTimestamp      Flag indicating whether to add a timestamp to the bundle.
     * @param fhirContext             The FHIR context.
     * @param fhirUri                 The FHIR server URI.
     * @param encoding                The encoding type for processing resources.
     */
    public void bundleResources(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Boolean includeDependencies,
                                Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, Boolean addBundleTimestamp,
                                FhirContext fhirContext, String fhirUri, IOUtils.Encoding encoding, Boolean verboseMessaging) {
        System.out.println("\r\n[Bundling " + getResourceBundlerType() + "s]\r\n");

        final List<String> bundledResources = new CopyOnWriteArrayList<>();

        //for keeping track of progress:
        final List<String> processedResources = new CopyOnWriteArrayList<>();

        //for keeping track of failed reasons:
        final Map<String, String> failedExceptionMessages = new ConcurrentHashMap<>();

        //keeping track of error list returned during cql translation:
        final Map<String, List<CqlCompilerException>> cqlTranslatorErrorMessages = new ConcurrentHashMap<>();

        //used to summarize file count user can expect to see in POST queue for each resource:
        final Map<String, Integer> persistedFileReport = new ConcurrentHashMap<>();

        //build list of executable tasks to be sent to thread pool:
        List<Callable<Void>> tasks = new ArrayList<>();

        try {
            final Map<String, IBaseResource> resourcesMap = new ConcurrentHashMap<>(getResources(fhirContext));
            final Map<String, IBaseResource> libraryUrlMap = new ConcurrentHashMap<>(IOUtils.getLibraryUrlMap(fhirContext));
            final Map<String, IBaseResource> libraries = new ConcurrentHashMap<>(IOUtils.getLibraries(fhirContext));
            final Map<String, String> libraryPathMap = new ConcurrentHashMap<>(IOUtils.getLibraryPathMap(fhirContext));

            if (resourcesMap.isEmpty()) {
                System.out.println("[INFO] No " + getResourceBundlerType() + "s found. Continuing...");
                return;
            }

            for (Map.Entry<String, IBaseResource> resourceEntry : resourcesMap.entrySet()) {
                String resourceId = "";

                if (resourceEntry.getValue() != null) {
                    resourceId = resourceEntry.getValue()
                            .getIdElement().getIdPart();
                } else {
                    continue;
                }

                //no path for this resource:
                if (resourceEntry.getKey() == null ||
                        resourceEntry.getKey().equalsIgnoreCase("null")) {
                    if (resourceId != null && !resourceId.isEmpty()) {
                        failedExceptionMessages.put(resourceId, "Path is null for " + resourceId);
                    }
                    continue;
                }

                final String resourceSourcePath = getSourcePath(fhirContext, resourceEntry);
                tasks.add(() -> {
                    //check if resourceSourcePath has been processed before:
                    if (processedResources.contains(resourceSourcePath)) {
                        System.out.println(getResourceBundlerType() + " processed already: " + resourceSourcePath);
                        return null;
                    }
                    String resourceName = FilenameUtils.getBaseName(resourceSourcePath).replace(getResourcePrefix(), "");

                    try {
                        Map<String, IBaseResource> resources = new ConcurrentHashMap<>();
                        Boolean shouldPersist = ResourceUtils.safeAddResource(resourceSourcePath, resources, fhirContext);
                        if (!resources.containsKey(getResourceBundlerType() + "/" + resourceEntry.getKey())) {
                            throw new IllegalArgumentException(String.format("Could not retrieve base resource for " + getResourceBundlerType() + " %s", resourceName));
                        }

                        IBaseResource resource = resources.get(getResourceBundlerType() + "/" + resourceEntry.getKey());
                        String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(resource, fhirContext);
                        IBaseResource primaryLibrary;
                        if (primaryLibraryUrl != null && primaryLibraryUrl.startsWith("http")) {
                            primaryLibrary = libraryUrlMap.get(primaryLibraryUrl);
                        } else {
                            primaryLibrary = libraries.get(primaryLibraryUrl);
                        }

                        if (primaryLibrary == null)
                            throw new IllegalArgumentException(String.format("Could not resolve library url %s", primaryLibraryUrl));

                        String primaryLibrarySourcePath = libraryPathMap.get(primaryLibrary.getIdElement().getIdPart());
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
                            failedExceptionMessages.put(resourceSourcePath, String.format("Could not determine CqlLibrarySource path for library %s", primaryLibraryName));
                            //exit from task:
                            return null;
                        }

                        if (includeTerminology) {
                            //throws CQLTranslatorException if failed with severe errors, which will be logged and reported it in the final summary
                            try {
                                ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
                            } catch (CqlTranslatorException cqlTranslatorException) {
                                cqlTranslatorErrorMessages.put(primaryLibraryName, cqlTranslatorException.getErrors());
                            }
                        }

                        if (includeDependencies) {
                            if (libraryProcessor == null) libraryProcessor = new LibraryProcessor();
                            try {
                                libraryProcessor.bundleLibraryDependencies(primaryLibrarySourcePath, fhirContext, resources, encoding, includeVersion);
                            } catch (Exception bre) {
                                failedExceptionMessages.put(resourceSourcePath, getResourceBundlerType() + " will not be bundled because Library Dependency bundling failed: " + bre.getMessage());
                                //exit from task:
                                return null;
                            }
                        }

                        if (includePatientScenarios) {
                            try {
                                TestCaseProcessor.bundleTestCases(igPath, getResourceTestGroupName(), primaryLibraryName, fhirContext, resources);
                            } catch (Exception tce) {
                                failedExceptionMessages.put(resourceSourcePath, getResourceBundlerType() + " will not be bundled because Test Case bundling failed: " + tce.getMessage());
                                //exit from task:
                                return null;
                            }
                        }

                        if (shouldPersist) {
                            String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), getResourceTestGroupName()), resourceName);

                            persistBundle(bundleDestPath, resourceName, encoding, fhirContext, new ArrayList<IBaseResource>(resources.values()), fhirUri, addBundleTimestamp);

                            bundleFiles(igPath, bundleDestPath, resourceName, binaryPaths, resourceSourcePath,
                                    primaryLibrarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios,
                                    includeVersion, addBundleTimestamp, cqlTranslatorErrorMessages);

                            //If user supplied a fhir server url, inform them of total # of files to be persisted to the server:
                            if (fhirUri != null && !fhirUri.isEmpty()) {
                                persistedFileReport.put(resourceName,
                                        //+1 to account for -bundle
                                        persistFilesFolder(bundleDestPath, resourceName, encoding, fhirContext, fhirUri) + 1);
                            }

                            if (cdsHooksProcessor != null) {
                                List<String> activityDefinitionPaths = CDSHooksProcessor.bundleActivityDefinitions(resourceSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);
                                cdsHooksProcessor.addActivityDefinitionFilesToBundle(igPath, bundleDestPath, activityDefinitionPaths, fhirContext, encoding);
                            }

                            bundledResources.add(resourceSourcePath);
                        }


                    } catch (Exception e) {
                        PrintWriter pw = new PrintWriter(new StringWriter());
                        e.printStackTrace(pw);

                        String failMsg = "";
                        if (e.getMessage() != null) {
                            failMsg = e.getMessage();
                        } else {
                            failMsg = e.getClass().getName() ;
                        }
                        failedExceptionMessages.put(resourceSourcePath, failMsg + ":\r\n" + pw.toString());
                    }

                    processedResources.add(resourceSourcePath);
                    reportProgress(processedResources.size(), tasks.size());

                    //task requires return statement
                    return null;
                });

            }//end for loop

            ThreadUtils.executeTasks(tasks);

        } catch (Exception e) {
            LogUtils.putException("bundleResources: " + getResourceBundlerType(), e);
        }

        //Prepare final report:
        StringBuilder message = new StringBuilder(NEWLINE);

        //Give user a snapshot of the files each resource will have persisted to their FHIR server (if fhirUri is provided)
        final int persistCount = persistedFileReport.size();
        if (persistCount > 0) {
            message.append(NEWLINE).append(persistCount).append(" ").append(getResourceBundlerType()).append("(s) have POST tasks in the queue for ").append(fhirUri).append(": ");
            int totalQueueCount = 0;
            for (String library : persistedFileReport.keySet()) {
                totalQueueCount = totalQueueCount + persistedFileReport.get(library);
                message.append(NEWLINE_INDENT)
                        .append(persistedFileReport.get(library))
                        .append(" File(s): ")
                        .append(library);
            }
            message.append(NEWLINE_INDENT)
                    .append("Total: ")
                    .append(totalQueueCount)
                    .append(" File(s)");
        }


        final int bundledCount = bundledResources.size();
        if (bundledCount > 0) {
            message.append(NEWLINE).append(bundledCount).append(" ").append(getResourceBundlerType()).append("(s) successfully bundled:");
            for (String bundledResource : bundledResources) {
                message.append(NEWLINE_INDENT).append(bundledResource).append(" BUNDLED");
            }
        }


        List<String> resourcePathLibraryNames = new ArrayList<>(getPaths(fhirContext));

        //gather which resources didn't make it
        List<String> failedResources = new ArrayList<>(resourcePathLibraryNames);
        resourcePathLibraryNames.removeAll(bundledResources);
        resourcePathLibraryNames.retainAll(refreshedLibraryNames);

        final int refreshedNotBundledCount = resourcePathLibraryNames.size();
        if (refreshedNotBundledCount > 0) {
            message.append(NEWLINE).append(refreshedNotBundledCount).append(" ").append(getResourceBundlerType()).append("(s) refreshed, but not bundled (due to issues):");
            for (String notBundled : resourcePathLibraryNames) {
                message.append(NEWLINE_INDENT).append(notBundled).append(" REFRESHED");
            }
        }

        //attempt to give some kind of informational message:
        failedResources.removeAll(bundledResources);
        failedResources.removeAll(resourcePathLibraryNames);

        final int failedCount = failedResources.size();
        if (failedCount > 0) {
            message.append(NEWLINE).append(failedCount).append(" ").append(getResourceBundlerType()).append("(s) failed refresh:");
            for (String failed : failedResources) {
                if (failedExceptionMessages.containsKey(failed) && verboseMessaging) {
                    message.append(NEWLINE_INDENT).append(failed).append(" FAILED: ").append(failedExceptionMessages.get(failed));
                } else {
                    message.append(NEWLINE_INDENT).append(failed).append(" FAILED");
                }
            }
        }

        //Exceptions stemming from IOUtils.translate that did not prevent process from completing for file:
        final int translateErrorCount = cqlTranslatorErrorMessages.size();
        if (translateErrorCount > 0) {
            message.append(NEWLINE).append(cqlTranslatorErrorMessages.size()).append(" ").append(getResourceBundlerType()).append("(s) encountered CQL translator errors:");

            for (String library : cqlTranslatorErrorMessages.keySet()) {
                message.append(NEWLINE_INDENT).append(
                        CqlProcessor.buildStatusMessage(cqlTranslatorErrorMessages.get(library), library, verboseMessaging, false, NEWLINE_INDENT2)
                );
            }
        }

        System.out.println(message);
    }


    private void reportProgress(int count, int total) {
        double percentage = (double) count / total * 100;
        System.out.print("\rBundle " + getResourceBundlerType() + "s: " + String.format("%.2f%%", percentage) + " processed.");
    }

    private String getResourceTestGroupName() {
        return getResourceBundlerType().toLowerCase();
    }

    private void persistBundle(String bundleDestPath, String libraryName,
                               IOUtils.Encoding encoding, FhirContext fhirContext,
                               List<IBaseResource> resources, String fhirUri,
                               Boolean addBundleTimestamp) throws IOException {
        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext, addBundleTimestamp, this.getIdentifiers());
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        if (fhirUri != null && !fhirUri.isEmpty()) {
            String resourceWriteLocation = bundleDestPath + separator + libraryName + "-bundle." + encoding;
            HttpClientUtils.post(fhirUri, (IBaseResource) bundle, encoding, fhirContext, resourceWriteLocation, true);
        }
    }


    protected abstract int persistFilesFolder(String bundleDestPath, String libraryName, IOUtils.Encoding encoding, FhirContext fhirContext, String fhirUri);

    private void bundleFiles(String igPath, String bundleDestPath, String primaryLibraryName, List<String> binaryPaths, String resourceFocusSourcePath,
                             String librarySourcePath, FhirContext fhirContext, IOUtils.Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios,
                             Boolean includeVersion, Boolean addBundleTimestamp, Map<String, List<CqlCompilerException>> translatorWarningMessages) {

        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, primaryLibraryName + "-" + IGBundleProcessor.bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(FilenameUtils.getBaseName(librarySourcePath), IOUtils.Encoding.CQL, fhirContext);
        if (cqlFileName.toLowerCase().startsWith("library-")) {
            cqlFileName = cqlFileName.substring(8);
        }
        String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(primaryLibraryName, cqlFileName, binaryPaths);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {
            try {
                Map<String, IBaseResource> valueSets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
                if (!valueSets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(primaryLibraryName), new ArrayList<IBaseResource>(valueSets.values()), fhirContext, addBundleTimestamp, this.getIdentifiers());
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
                }
            } catch (CqlTranslatorException cqlTranslatorException) {
                translatorWarningMessages.put(primaryLibraryName, cqlTranslatorException.getErrors());
            }
        }

        if (includeDependencies) {
            Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding, includeVersion, logger);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + primaryLibraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IBaseResource>(depLibraries.values()), fhirContext, addBundleTimestamp, this.getIdentifiers());
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
            }
        }

        if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, getResourceTestGroupName(), primaryLibraryName, bundleDestFilesPath, fhirContext);
        }

    }


}