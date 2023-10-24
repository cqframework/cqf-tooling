package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.measure.r4.R4MeasureProcessor;
import org.opencds.cqf.tooling.measure.stu3.STU3MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshMeasureParameters;
import org.opencds.cqf.tooling.processor.*;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeasureProcessor extends BaseProcessor {
    public static final String ResourcePrefix = "measure-";
    public static final String MeasureTestGroupName = "measure";
    protected List<Object> identifiers;

    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<String> refreshIgMeasureContent(BaseProcessor parentContext, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext, String measureToRefreshPath, Boolean shouldApplySoftwareSystemStamp) {
        return refreshIgMeasureContent(parentContext, outputEncoding, null, versioned, fhirContext, measureToRefreshPath, shouldApplySoftwareSystemStamp);
    }

    public List<String> refreshIgMeasureContent(BaseProcessor parentContext, Encoding outputEncoding, String measureOutputDirectory, Boolean versioned, FhirContext fhirContext, String measureToRefreshPath, Boolean shouldApplySoftwareSystemStamp) {

        System.out.println("Refreshing measures...");

        MeasureProcessor measureProcessor;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                measureProcessor = new STU3MeasureProcessor();
                break;
            case R4:
                measureProcessor = new R4MeasureProcessor();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        String measurePath = FilenameUtils.concat(parentContext.getRootDir(), IGProcessor.measurePathElement);
        RefreshMeasureParameters params = new RefreshMeasureParameters();
        params.measurePath = measurePath;
        params.parentContext = parentContext;
        params.fhirContext = fhirContext;
        params.encoding = outputEncoding;
        params.versioned = versioned;
        params.measureOutputDirectory = measureOutputDirectory;
        List<String> contentList = measureProcessor.refreshMeasureContent(params);

        if (!measureProcessor.getIdentifiers().isEmpty()) {
            this.getIdentifiers().addAll(measureProcessor.getIdentifiers());
        }
        return contentList;
    }

    protected List<Object> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new CopyOnWriteArrayList<>();
        }
        return identifiers;
    }

    public void bundleMeasures(ArrayList<String> refreshedLibraryNames, String igPath, List<String> binaryPaths, Boolean includeDependencies,
                               Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, Boolean addBundleTimestamp, FhirContext fhirContext, String fhirUri,
                               Encoding encoding) {


        Map<String, IBaseResource> measures = IOUtils.getMeasures(fhirContext);
        //Map<String, IBaseResource> libraries = IOUtils.getLibraries(fhirContext);

        List<String> bundledMeasures = new CopyOnWriteArrayList<>();

        //for keeping track of progress:
        List<String> processedMeasures = new CopyOnWriteArrayList<>();

        Map<String, String> failedExceptionMessages = new ConcurrentHashMap<>();

        int totalMeasures = measures.size();

        //build list of tasks via for loop:
        List<Callable<Void>> tasks = new ArrayList<>();
        try {
            final Map<String, IBaseResource> libraryUrlMap = IOUtils.getLibraryUrlMap(fhirContext);
            final Map<String, IBaseResource> libraries = IOUtils.getLibraries(fhirContext);
            final Map<String, String> libraryPathMap = IOUtils.getLibraryPathMap(fhirContext);

            for (Map.Entry<String, IBaseResource> measureEntry : measures.entrySet()) {

                if (measureEntry.getKey() == null || measureEntry.getKey().equalsIgnoreCase("null")) {
                    continue;
                }

                final String measureSourcePath = IOUtils.getMeasurePathMap(fhirContext).get(measureEntry.getKey());

                tasks.add(() -> {

                    //check if measureSourcePath has been processed before:
                    if (processedMeasures.contains(measureSourcePath)) {
                        LogUtils.info("Measure processed already: " + measureSourcePath);
                        return null;
                    }

                    // Assumption - File name matches measure.name
                    String measureName = FilenameUtils.getBaseName(measureSourcePath).replace(MeasureProcessor.ResourcePrefix, "");
                    try {
                        Map<String, IBaseResource> resources = new HashMap<String, IBaseResource>();

                        Boolean shouldPersist = ResourceUtils.safeAddResource(measureSourcePath, resources, fhirContext);
                        if (!resources.containsKey("Measure/" + measureEntry.getKey())) {
                            throw new IllegalArgumentException(String.format("Could not retrieve base resource for measure %s", measureName));
                        }
                        IBaseResource measure = resources.get("Measure/" + measureEntry.getKey());
                        String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(measure, fhirContext);
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

                        String cqlFileName = IOUtils.formatFileName(primaryLibraryName, Encoding.CQL, fhirContext);

                        String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(primaryLibraryName, cqlFileName, binaryPaths);

                        if (cqlLibrarySourcePath == null) {
                            throw new IllegalArgumentException(String.format("Could not determine CqlLibrarySource path for library %s", primaryLibraryName));
                        }

                        if (includeTerminology) {
                            //ValueSetsProcessor.bundleValueSets modified to throw Exception so we can collect it and report it in the final "report"
                            boolean result = ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
//                            if (shouldPersist && !result) {
//                                LogUtils.info("Measure will not be bundled because ValueSet bundling failed: " + measureSourcePath  + "\n");
//                            }
                            shouldPersist = shouldPersist & result;
                        }

                        if (includeDependencies) {
                            LibraryProcessor libraryProcessor = new LibraryProcessor();
                            boolean result = libraryProcessor.bundleLibraryDependencies(primaryLibrarySourcePath, fhirContext, resources, encoding, includeVersion);
                            if (shouldPersist && !result) {
                                LogUtils.info("Measure will not be bundled because Library Dependency bundling failed.");
                            }
                            shouldPersist = shouldPersist & result;
                        }

                        if (includePatientScenarios) {
                            boolean result = TestCaseProcessor.bundleTestCases(igPath, MeasureTestGroupName, primaryLibraryName, fhirContext, resources);
                            if (shouldPersist && !result) {
                                LogUtils.info("Measure will not be bundled because Test Case bundling failed.");
                            }
                            shouldPersist = shouldPersist & result;
                        }

                        if (shouldPersist) {
                            String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(IGProcessor.getBundlesPath(igPath), MeasureTestGroupName), measureName);
                            persistBundle(igPath, bundleDestPath, measureName, encoding, fhirContext, new ArrayList<IBaseResource>(resources.values()), fhirUri, addBundleTimestamp);
                            bundleFiles(igPath, bundleDestPath, measureName, binaryPaths, measureSourcePath, primaryLibrarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion, addBundleTimestamp);
                            bundledMeasures.add(measureSourcePath);
                        }


                    } catch (Exception e) {
                        LogUtils.putException(measureName, e);
                        failedExceptionMessages.put(measureSourcePath, e.getMessage());
                    }


                    processedMeasures.add(measureSourcePath);

                    synchronized (this) {
                        double percentage = (double) processedMeasures.size() / totalMeasures * 100;
                        LogUtils.info("Bundle Measures Progress: " + String.format("%.2f%%", percentage) + " PROCESSED: " + measureEntry.getKey());
                    }
                    //task requires return statement
                    return null;
                });

            }//end of for loop

            ThreadUtils.executeTasks(tasks);
        } catch (Exception e) {
            LogUtils.putException("bundleMeasures", e);
        }

        StringBuilder message = new StringBuilder("\r\n" + bundledMeasures.size() + " Measures successfully bundled:");
        for (String bundledMeasure : bundledMeasures) {
            message.append("\r\n     ").append(bundledMeasure).append(" BUNDLED");
        }

        List<String> measurePathLibraryNames = new ArrayList<>(IOUtils.getMeasurePaths(fhirContext));
        //gather which measures didn't make it
        ArrayList<String> failedMeasures = new ArrayList<>(measurePathLibraryNames);

        measurePathLibraryNames.removeAll(bundledMeasures);
        measurePathLibraryNames.retainAll(refreshedLibraryNames);
        message.append("\r\n").append(measurePathLibraryNames.size()).append(" Measures refreshed, but not bundled (due to issues):");
        for (String notBundled : measurePathLibraryNames) {
            message.append("\r\n     ").append(notBundled).append(" REFRESHED");
        }


        //attempt to give some kind of informational message:
        failedMeasures.removeAll(bundledMeasures);
        failedMeasures.removeAll(measurePathLibraryNames);
        message.append("\r\n").append(failedMeasures.size()).append(" Measures failed refresh:");
        for (String failed : failedMeasures) {
            if (failedExceptionMessages.containsKey(failed)) {
                message.append("\r\n     ").append(failed).append(" FAILED: ").append(failedExceptionMessages.get(failed));
            } else {
                message.append("\r\n     ").append(failed).append(" FAILED");
            }
        }

        LogUtils.info(message.toString());
    }

    private void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IBaseResource> resources, String fhirUri, Boolean addBundleTimestamp) {
        //Check for test files in bundleDestPath + "-files", loop through if exists,
        // find all files that start with "tests-", post to fhir server following same folder structure:
        persistTestFiles(bundleDestPath, libraryName, encoding, fhirContext, fhirUri);

        IOUtils.initializeDirectory(bundleDestPath);
        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext, addBundleTimestamp, this.getIdentifiers());
        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

        BundleUtils.postBundle(encoding, fhirContext, fhirUri, (IBaseResource) bundle);
    }

    //    private void persistTestFiles(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri) {
//
//        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
//        File directory = new File(filesLoc);
//        if (directory.exists()) {
//            File[] filesInDir = directory.listFiles();
//            if (!(filesInDir == null || filesInDir.length == 0)) {
//                for (File file : filesInDir) {
//                    if (file.getName().toLowerCase().startsWith("tests-")) {
//                        IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext, true);
//                        BundleUtils.postBundle(encoding, fhirContext, fhirUri, resource);
//                    }
//                }
//            }
//
//        }
//    }
    private void persistTestFiles(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri) {
        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
        File directory = new File(filesLoc);

        if (directory.exists() && directory.isDirectory()) {
            File[] filesInDir = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("tests-");
                }
            });

            if (filesInDir != null) {
                for (File file : filesInDir) {
                    String filePath = file.getAbsolutePath();

                    IBaseResource fileAsResource = IOUtils.readResource(filePath, fhirContext, true);

//                    //might be a transaction containing a sindle bundle of type collection, convert if possible:
//                    try {
//                        IBaseResource convertedBundle = convertedTransaction(fileAsResource);
//                        if (convertedBundle != null) {
//                            BundleUtils.postBundle(encoding, fhirContext, fhirUri, convertedBundle);
//                            continue;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    BundleUtils.postBundle(encoding, fhirContext, fhirUri, fileAsResource);
                }
            }
        }
    }




    private IBaseResource convertedTransaction(IBaseResource inputResource) {
        if (inputResource == null) return null;

        if (inputResource instanceof org.hl7.fhir.dstu3.model.Bundle) {
            org.hl7.fhir.dstu3.model.Bundle collectionBundle = getCollectionBundle((org.hl7.fhir.dstu3.model.Bundle) inputResource);
            if (collectionBundle != null) return convertCollectionToTransaction(collectionBundle);
        } else if (inputResource instanceof org.hl7.fhir.r4.model.Bundle) {
            org.hl7.fhir.r4.model.Bundle collectionBundle = getCollectionBundle((org.hl7.fhir.r4.model.Bundle) inputResource);
            if (collectionBundle != null) return convertCollectionToTransaction(collectionBundle);
        }
        return null;
    }

    private org.hl7.fhir.dstu3.model.Bundle getCollectionBundle(org.hl7.fhir.dstu3.model.Bundle collectionBundle){
        if (collectionBundle.getType().equals(org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION)) {
            return collectionBundle;
        }


        List<org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent> bundleEntries = collectionBundle.getEntry();
        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent bundleEntry : bundleEntries) {
            IBaseResource resource = bundleEntry.getResource();
            if (resource instanceof org.hl7.fhir.dstu3.model.Bundle) {
                if (((org.hl7.fhir.dstu3.model.Bundle)resource).getType().equals(org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION)) {
                    return (org.hl7.fhir.dstu3.model.Bundle)resource;
                }
            }
        }
        return null;
    }
    private org.hl7.fhir.r4.model.Bundle getCollectionBundle(org.hl7.fhir.r4.model.Bundle collectionBundle){
        List<org.hl7.fhir.r4.model.Bundle.BundleEntryComponent> bundleEntries = collectionBundle.getEntry();
        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent bundleEntry : bundleEntries) {
            IBaseResource resource = bundleEntry.getResource();
            if (resource instanceof org.hl7.fhir.r4.model.Bundle) {
                if (((org.hl7.fhir.r4.model.Bundle)resource).getType().equals(org.hl7.fhir.r4.model.Bundle.BundleType.COLLECTION)) {
                    return (org.hl7.fhir.r4.model.Bundle)resource;
                }
            }
        }
        return null;
    }
    public static org.hl7.fhir.dstu3.model.Bundle convertCollectionToTransaction(org.hl7.fhir.dstu3.model.Bundle collectionBundle) {
        if (collectionBundle == null ){
            return null;
        }

        org.hl7.fhir.dstu3.model.Bundle transactionBundle = new org.hl7.fhir.dstu3.model.Bundle();

        transactionBundle.setId(collectionBundle.getId());
        transactionBundle.setLink(collectionBundle.getLink());
        transactionBundle.setIdentifier(collectionBundle.getIdentifier());
        transactionBundle.setSignature(collectionBundle.getSignature());
        transactionBundle.setTotal(collectionBundle.getTotal());
        transactionBundle.setMeta(collectionBundle.getMeta());
        transactionBundle.setLanguageElement(collectionBundle.getLanguageElement());
        transactionBundle.setImplicitRulesElement(collectionBundle.getImplicitRulesElement());
        transactionBundle.setIdElement(collectionBundle.getIdElement());
        transactionBundle.setIdBase(collectionBundle.getIdBase());
        transactionBundle.setLanguage(collectionBundle.getLanguage());

        transactionBundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);

        for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent entry : collectionBundle.getEntry()) {
            // Clone the entry and add it to the transaction bundle
            org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent transactionEntry = entry.copy();

            //build a request entry
            org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent request = new org.hl7.fhir.dstu3.model.Bundle.BundleEntryRequestComponent();
            request.setMethod(org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.POST); // Adjust the HTTP method as needed
            String resourceType = entry.getResource().fhirType();
            request.setUrl(resourceType);
            transactionEntry.setRequest(request);

            transactionBundle.addEntry(transactionEntry);
        }

        return transactionBundle;
    }


    public static org.hl7.fhir.r4.model.Bundle convertCollectionToTransaction(org.hl7.fhir.r4.model.Bundle collectionBundle) {
        if (collectionBundle == null ){
            return null;
        }
        
        org.hl7.fhir.r4.model.Bundle transactionBundle = new org.hl7.fhir.r4.model.Bundle();

        transactionBundle.setId(collectionBundle.getId());
        transactionBundle.setLink(collectionBundle.getLink());
        transactionBundle.setIdentifier(collectionBundle.getIdentifier());
        transactionBundle.setSignature(collectionBundle.getSignature());
        transactionBundle.setTotal(collectionBundle.getTotal());
        transactionBundle.setMeta(collectionBundle.getMeta());
        transactionBundle.setLanguageElement(collectionBundle.getLanguageElement());
        transactionBundle.setImplicitRulesElement(collectionBundle.getImplicitRulesElement());
        transactionBundle.setIdElement(collectionBundle.getIdElement());
        transactionBundle.setIdBase(collectionBundle.getIdBase());
        transactionBundle.setLanguage(collectionBundle.getLanguage());

        transactionBundle.setType(org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION);

        for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent entry : collectionBundle.getEntry()) {
            // Clone the entry and add it to the transaction bundle
            org.hl7.fhir.r4.model.Bundle.BundleEntryComponent transactionEntry = entry.copy();

            //build a request entry
            org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent request = new org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent();
            request.setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST); // Adjust the HTTP method as needed
            String resourceType = entry.getResource().fhirType();
            request.setUrl(resourceType);
            transactionEntry.setRequest(request);

            transactionBundle.addEntry(transactionEntry);
        }

        return transactionBundle;
    }

    private void bundleFiles(String igPath, String bundleDestPath, String libraryName, List<String> binaryPaths, String resourceFocusSourcePath,
                             String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios,
                             Boolean includeVersion, Boolean addBundleTimestamp) {
        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + IGBundleProcessor.bundleFilesPathElement);
        IOUtils.initializeDirectory(bundleDestFilesPath);

        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

        String cqlFileName = IOUtils.formatFileName(FilenameUtils.getBaseName(librarySourcePath), Encoding.CQL, fhirContext);
        if (cqlFileName.toLowerCase().startsWith("library-")) {
            cqlFileName = cqlFileName.substring(8);
        }
        String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(libraryName, cqlFileName, binaryPaths);
        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

        if (includeTerminology) {
            try {
                Map<String, IBaseResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
                if (!valuesets.isEmpty()) {
                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IBaseResource>(valuesets.values()), fhirContext, addBundleTimestamp, this.getIdentifiers());
                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.putException(libraryName, e.getMessage());
            }
        }

        if (includeDependencies) {
            Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding, includeVersion, logger);
            if (!depLibraries.isEmpty()) {
                String depLibrariesID = "library-deps-" + libraryName;
                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IBaseResource>(depLibraries.values()), fhirContext, addBundleTimestamp, this.getIdentifiers());
                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
            }
        }

        if (includePatientScenarios) {
            TestCaseProcessor.bundleTestCaseFiles(igPath, "measure", libraryName, bundleDestFilesPath, fhirContext);
        }
    }

    protected boolean versioned;
    protected FhirContext fhirContext;

    public List<String> refreshMeasureContent(RefreshMeasureParameters params) {
        return new ArrayList<>();
    }

    protected List<Measure> refreshGeneratedContent(List<Measure> sourceMeasures) {
        return internalRefreshGeneratedContent(sourceMeasures);
    }

    private List<Measure> internalRefreshGeneratedContent(List<Measure> sourceMeasures) {
        // for each Measure, refresh the measure based on the primary measure library
        List<Measure> resources = new ArrayList<>();
        for (Measure measure : sourceMeasures) {
            resources.add(refreshGeneratedContent(measure));
        }
        return resources;
    }

    private Measure refreshGeneratedContent(Measure measure) {
        MeasureRefreshProcessor processor = new MeasureRefreshProcessor();
        LibraryManager libraryManager = getCqlProcessor().getLibraryManager();
        CqlTranslatorOptions cqlTranslatorOptions = getCqlProcessor().getCqlTranslatorOptions();
        // Do not attempt to refresh if the measure does not have a library
        if (measure.hasLibrary()) {
            String libraryUrl = ResourceUtils.getPrimaryLibraryUrl(measure, fhirContext);
            VersionedIdentifier primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);
            List<CqlCompilerException> errors = new ArrayList<CqlCompilerException>();
            CompiledLibrary CompiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, cqlTranslatorOptions, errors);
            boolean hasErrors = false;
            if (errors.size() > 0) {
                for (CqlCompilerException e : errors) {
                    if (e.getSeverity() == CqlCompilerException.ErrorSeverity.Error) {
                        hasErrors = true;
                    }
                    logMessage(e.getMessage());
                }
            }
            if (!hasErrors) {
                return processor.refreshMeasure(measure, libraryManager, CompiledLibrary, cqlTranslatorOptions);
            }
        }
        return measure;
    }
}
