package org.opencds.cqf.igtools;

//import org.apache.commons.io.FilenameUtils;
//import org.hl7.fhir.MeasureReport;
//import org.hl7.fhir.MeasureReportStatus;
//import org.hl7.fhir.instance.model.api.IAnyResource;
//import org.hl7.fhir.r4.model.Library;
//import org.opencds.cqf.library.R4LibraryProcessor;
//import org.opencds.cqf.library.STU3LibraryProcessor;
//import org.opencds.cqf.measure.MeasureProcessor;
//import org.opencds.cqf.measure.r4.RefreshR4Measure;
//import org.opencds.cqf.measure.stu3.RefreshStu3Measure;
//import org.opencds.cqf.plandefinition.PlanDefinitionProcessor;
//import org.opencds.cqf.terminology.ValueSetsProcessor;
//import org.opencds.cqf.testcase.TestCaseProcessor;
//import org.opencds.cqf.utilities.*;
//import org.opencds.cqf.utilities.IOUtils.Encoding;
//
//import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.context.RuntimeResourceDefinition;

public class IGProcessor {
//    public enum IGVersion {
//        FHIR3("fhir3"), FHIR4("fhir4");
//
//        private String string;
//
//        public String toString() {
//            return this.string;
//        }
//
//        private IGVersion(String string) {
//            this.string = string;
//        }
//
//        public static IGVersion parse(String value) {
//            switch (value) {
//            case "fhir3":
//                return FHIR3;
//            case "fhir4":
//                return FHIR4;
//            default:
//                throw new RuntimeException("Unable to parse IG version value:" + value);
//            }
//        }
//    }
//
//    public static void refreshIG(RefreshIGParameters params) {
//
//        String igResourcePath = params.igResourcePath;
//        String igPath = params.igPath;
//        IGVersion igVersion = params.igVersion;
//        Encoding encoding = params.outputEncoding;
//        Boolean includeELM = params.includeELM;
//        Boolean includeDependencies = params.includeDependencies;
//        Boolean includeTerminology = params.includeTerminology;
//        Boolean includePatientScenarios = params.includePatientScenarios;
//        Boolean versioned = params.versioned;
//        String fhirUri = params.fhirUri;
//        ArrayList<String> resourceDirs = params.resourceDirs;
//
//        IOUtils.resourceDirectories.addAll(resourceDirs);
//
//        FhirContext fhirContext = getIgFhirContext(igVersion);
//        Boolean igResourcePathIsSpecified = igResourcePath != null && !igResourcePath.isEmpty() && !igResourcePath.isBlank();
//        Object implementationGuide = null;
//        String igCanonicalBase = null;
//
//        igPath = Paths.get(igPath).toAbsolutePath().toString();
//
//        ensure(igPath, includePatientScenarios, includeTerminology, IOUtils.resourceDirectories);
//
//        ArrayList<String> refreshedLibraryNames = null;
//        switch (fhirContext.getVersion().getVersion()) {
//        case DSTU3:
//            if (igResourcePathIsSpecified) {
//                implementationGuide = IOUtils.readResource(igResourcePath, fhirContext, true);
//                if (implementationGuide != null) {
//                    igCanonicalBase = IGUtils.getStu3ImplementationGuideCanonicalBase((org.hl7.fhir.dstu3.model.ImplementationGuide) implementationGuide);
//                }
//            }
//            refreshedLibraryNames = refreshStu3IG(igCanonicalBase, igPath, encoding, includeELM, includeDependencies, includeTerminology,
//                    includePatientScenarios, versioned, fhirContext);
//            break;
//        case R4:
//            if (igResourcePathIsSpecified) {
//                implementationGuide = IOUtils.readResource(igResourcePath, fhirContext, true);
//                if (implementationGuide != null) {
//                    igCanonicalBase = IGUtils.getR4ImplementationGuideCanonicalBase((org.hl7.fhir.r4.model.ImplementationGuide) implementationGuide);
//                }
//            }
//            refreshedLibraryNames = refreshR4IG(igCanonicalBase, igPath, encoding, includeELM, includeDependencies, includeTerminology,
//                    includePatientScenarios, versioned, fhirContext);
//            break;
//        default:
//            throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
//        }
//
//        if (refreshedLibraryNames.isEmpty()) {
//            LogUtils.info("No libraries successfully refreshed.");
//            return;
//        }
//
//        if (includePatientScenarios) {
//            TestCaseProcessor.refreshTestCases(FilenameUtils.concat(igPath, testCasePathElement), encoding, fhirContext);
//        }
//
//        bundleIg(refreshedLibraryNames, igPath, encoding, includeELM, includeDependencies, includeTerminology, includePatientScenarios,
//        versioned, fhirContext, fhirUri);
//    }
//
//    private static ArrayList<String> refreshStu3IG(String igCanonicalBase, String igPath, Encoding outputEncodingEnum, Boolean includeELM, Boolean includeDependencies,
//            Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, FhirContext fhirContext) {
//        ArrayList<String> refreshedResourcesNames = refreshStu3IgLibraryContent(igCanonicalBase, igPath, outputEncodingEnum, includeELM, versioned, fhirContext);
//
//        List<String> refreshedMeasureNames = refreshStu3IgMeasureContent(igPath, outputEncodingEnum, versioned, fhirContext);
//        refreshedResourcesNames.addAll(refreshedMeasureNames);
//        return refreshedResourcesNames;
//    }
//
//    private static ArrayList<String> refreshR4IG(String igCanonicalBase, String igPath, Encoding outputEncodingEnum, Boolean includeELM, Boolean includeDependencies,
//            Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned, FhirContext fhirContext) {
//        ArrayList<String> refreshedResourcesNames = refreshR4LibraryContent(igCanonicalBase, igPath, outputEncodingEnum, includeELM, versioned, fhirContext);
//
//        List<String> refreshedMeasureNames = refreshR4IgMeasureContent(igPath, outputEncodingEnum, versioned, fhirContext);
//        refreshedResourcesNames.addAll(refreshedMeasureNames);
//        return refreshedResourcesNames;
//    }
//
//    public static ArrayList<String> refreshStu3IgLibraryContent(String igCanonicalBase, String igPath, Encoding outputEncoding, Boolean includeELM,
//            Boolean versioned, FhirContext fhirContext) {
//        System.out.println("Refreshing libraries...");
//        ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
//        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
//
//        for (String path : cqlContentPaths) {
//            try {
//                String libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(path, fhirContext);
//
//                STU3LibraryProcessor.refreshLibraryContent(igCanonicalBase, path, libraryPath, fhirContext, outputEncoding, versioned);
//                refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
//            } catch (Exception e) {
//                LogUtils.putException(path, e);
//            }
//            finally {
//                LogUtils.warn(path);
//            }
//        }
//
//        return refreshedLibraryNames;
//    }
//
//    public static ArrayList<String> refreshR4LibraryContent(String igCanonicalBase, String igPath, Encoding outputEncoding, Boolean includeELM,
//            Boolean versioned, FhirContext fhirContext) {
//        System.out.println("Refreshing libraries...");
//        ArrayList<String> refreshedLibraryNames = new ArrayList<String>();
//        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
//
//        for (String path : cqlContentPaths) {
//            try {
//                String libraryPath = IOUtils.getLibraryPathAssociatedWithCqlFileName(path, fhirContext);
//
//                R4LibraryProcessor.refreshLibraryContent(igCanonicalBase, path, libraryPath, fhirContext, outputEncoding, versioned);
//                refreshedLibraryNames.add(FilenameUtils.getBaseName(path));
//            } catch (Exception e) {
//                LogUtils.putException(path, e);
//            }
//            finally {
//                LogUtils.warn(path);
//            }
//        }
//
//        return refreshedLibraryNames;
//    }
//
//    public static ArrayList<String> refreshStu3IgMeasureContent(String igPath, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext) {
//        System.out.println("Refreshing measures...");
//        ArrayList<String> refreshedMeasureNames = new ArrayList<String>();
//        HashSet<String> measurePaths = IOUtils.getMeasurePaths(fhirContext);
//
//        for (String path : measurePaths) {
//            try {
//                RefreshStu3Measure refresher = new RefreshStu3Measure(path);
//                refresher.refreshGeneratedContent();
//                refreshedMeasureNames.add(FilenameUtils.getBaseName(path));
//            } catch (Exception e) {
//                LogUtils.putException(path, e);
//            }
//            finally {
//                LogUtils.warn(path);
//            }
//        }
//
//        return refreshedMeasureNames;
//    }
//
//    public static ArrayList<String> refreshR4IgMeasureContent(String igPath, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext) {
//        System.out.println("Refreshing measures...");
//        ArrayList<String> refreshedMeasureNames = new ArrayList<String>();
//        HashSet<String> measurePaths = IOUtils.getMeasurePaths(fhirContext);
//
//        for (String path : measurePaths) {
//            try {
//                RefreshR4Measure refresher = new RefreshR4Measure(path);
//                refresher.refreshGeneratedContent();
//                refreshedMeasureNames.add(FilenameUtils.getBaseName(path));
//            } catch (Exception e) {
//                LogUtils.putException(path, e);
//            }
//            finally {
//                LogUtils.warn(path);
//            }
//        }
//
//        return refreshedMeasureNames;
//    }
//
//    // TODO: most of the work of the sub methods of this should probably be moved to
//    // their respective resource Processors.
//    // No time for a refactor atm though. So stinky it is!
//    public static void bundleIg(ArrayList<String> refreshedLibraryNames, String igPath, Encoding encoding, Boolean includeELM,
//            Boolean includeDependencies, Boolean includeTerminology, Boolean includePatientScenarios, Boolean versioned,
//            FhirContext fhirContext, String fhirUri) {
//
//        bundleMeasures(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned,
//                fhirContext, fhirUri, encoding);
//
//        bundlePlanDefinitions(refreshedLibraryNames, igPath, includeDependencies, includeTerminology, includePatientScenarios, versioned,
//                fhirContext, fhirUri, encoding);
//    }
//
//    private static void bundleMeasures(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeDependencies,
//            Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, FhirContext fhirContext, String fhirUri,
//            Encoding encoding) {
//        // The set to bundle should be the union of the successfully refreshed Measures and Libraries
//        // Until we have the ability to refresh Measures, the set is the union of existing Measures and successfully refreshed Libraries
//        System.out.println("Bundling measures...");
//        HashSet<String> measureSourcePaths = IOUtils.getMeasurePaths(fhirContext);
//        List<String> measurePathLibraryNames = new ArrayList<String>();
//        for (String measureSourcePath : measureSourcePaths) {
//            measurePathLibraryNames
//                    .add(FilenameUtils.getBaseName(measureSourcePath).replace(MeasureProcessor.ResourcePrefix, ""));
//        }
//
//        List<String> bundledMeasures = new ArrayList<String>();
//        for (String refreshedLibraryName : refreshedLibraryNames) {
//            try {
//                if (!measurePathLibraryNames.contains(refreshedLibraryName)) {
//                    LogUtils.info("Skipped bundling - no Measure resource found for Library: " + refreshedLibraryName);
//                    continue;
//                }
//
//                Map<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
//
//                String refreshedLibraryFileName = IOUtils.formatFileName(refreshedLibraryName, encoding, fhirContext);
//                String librarySourcePath = IOUtils.getLibraryPathAssociatedWithCqlFileName(refreshedLibraryFileName, fhirContext);
//                if (librarySourcePath == null) {
//                    LogUtils.putException(refreshedLibraryName, new FileNotFoundException("Could not find a Library Resource Associated with: " + refreshedLibraryFileName));
//                    continue;
//                }
//
//                String measureSourcePath = "";
//                for (String path : measureSourcePaths) {
//                    if (path.endsWith(refreshedLibraryFileName))
//                    {
//                        measureSourcePath = path;
//                    }
//                }
//
//                Boolean shouldPersist = ResourceUtils.safeAddResource(measureSourcePath, resources, fhirContext);
//                shouldPersist = shouldPersist
//                        & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext);
//
//                String cqlFileName = IOUtils.formatFileName(refreshedLibraryName, Encoding.CQL, fhirContext);
//                List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
//                    .filter(path -> path.endsWith(cqlFileName))
//                    .collect(Collectors.toList());
//                String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
//                if (includeTerminology) {
//                    shouldPersist = shouldPersist
//                        & bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
//                }
//
//                if (includeDependencies) {
//                    shouldPersist = shouldPersist
//                        & bundleDependencies(librarySourcePath, fhirContext, resources, encoding);
//                }
//
//                if (includePatientScenarios) {
//                    shouldPersist = shouldPersist
//                        & bundleTestCases(igPath, refreshedLibraryName, fhirContext, resources);
//                }
//
//                if (shouldPersist) {
//                    String bundleDestPath = FilenameUtils.concat(getBundlesPath(igPath), refreshedLibraryName);
//                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()), fhirUri);
//                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, measureSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion);
//                    bundledMeasures.add(refreshedLibraryName);
//                }
//            } catch (Exception e) {
//                LogUtils.putException(refreshedLibraryName, e);
//            } finally {
//                LogUtils.warn(refreshedLibraryName);
//            }
//        }
//        String message = "\r\n********************MEASURE RESULTS:********************\r\n" + bundledMeasures.size() + " Measures successfully bundled:";
//        for (String bundledMeasure : bundledMeasures) {
//            message += "\r\n     " + bundledMeasure + " BUNDLED";
//        }
//
//        ArrayList<String> failedMeasures = new ArrayList<>(measurePathLibraryNames);
//        measurePathLibraryNames.removeAll(bundledMeasures);
//        measurePathLibraryNames.retainAll(refreshedLibraryNames);
//        message += "\r\n" + measurePathLibraryNames.size() + " Measures refreshed, but not bundled (due to issues):";
//        for (String notBundled : measurePathLibraryNames) {
//            message += "\r\n     " + notBundled + " REFRESHED";
//        }
//
//        failedMeasures.removeAll(bundledMeasures);
//        failedMeasures.removeAll(measurePathLibraryNames);
//        message += "\r\n" + failedMeasures.size() + " Measures failed refresh:";
//        for (String failed : failedMeasures) {
//            message += "\r\n     " + failed + " FAILED";
//        }
//        message += "\r\n********************************************************";
//
//        LogUtils.info(message);
//    }
//
//    private static void bundlePlanDefinitions(ArrayList<String> refreshedLibraryNames, String igPath, Boolean includeDependencies,
//            Boolean includeTerminology, Boolean includePatientScenarios, Boolean includeVersion, FhirContext fhirContext, String fhirUri,
//            Encoding encoding) {
//        System.out.println("Bundling plandefinitions...");
//        HashSet<String> planDefinitionSourcePaths = IOUtils.getPlanDefinitionPaths(fhirContext);
//
//        List<String> planDefinitionPathLibraryNames = new ArrayList<String>();
//        for (String planDefinitionSourcePath : planDefinitionSourcePaths) {
//            String name = FilenameUtils.getBaseName(planDefinitionSourcePath).replace(PlanDefinitionProcessor.ResourcePrefix, "");
//
//            planDefinitionPathLibraryNames.add(name);
//        }
//
//        List<String> bundledPlanDefinitions = new ArrayList<String>();
//        for (String refreshedLibraryName : refreshedLibraryNames) {
//            try {
//                if (!planDefinitionPathLibraryNames.contains(refreshedLibraryName)) {
//                    continue;
//                }
//
//                Map<String, IAnyResource> resources = new HashMap<String, IAnyResource>();
//
//                String refreshedLibraryFileName = IOUtils.formatFileName(refreshedLibraryName, encoding, fhirContext);
//                String librarySourcePath = IOUtils.getLibraryPathAssociatedWithCqlFileName(refreshedLibraryFileName, fhirContext);
//                if (librarySourcePath == null) {
//                    LogUtils.putException(refreshedLibraryName, new FileNotFoundException("Could not find a Library Resource Associated with: " + refreshedLibraryFileName));
//                    continue;
//                }
//
//                String planDefinitionSourcePath = "";
//                for (String path : planDefinitionSourcePaths) {
//                    if (FilenameUtils.removeExtension(path).endsWith(refreshedLibraryName))
//                    {
//                        planDefinitionSourcePath = path;
//                    }
//                }
//
//                Boolean shouldPersist = ResourceUtils.safeAddResource(planDefinitionSourcePath, resources, fhirContext);
//                shouldPersist = shouldPersist
//                        & ResourceUtils.safeAddResource(librarySourcePath, resources, fhirContext);
//
//                String cqlFileName = IOUtils.formatFileName(refreshedLibraryName, Encoding.CQL, fhirContext);
//                List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
//                    .filter(path -> path.endsWith(cqlFileName))
//                    .collect(Collectors.toList());
//                String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
//
//                if (includeTerminology) {
//                    shouldPersist = shouldPersist
//                        & bundleValueSets(cqlLibrarySourcePath, igPath, fhirContext, resources, encoding, includeDependencies, includeVersion);
//                }
//
//                if (includeDependencies) {
//                    shouldPersist = shouldPersist
//                        & bundleDependencies(librarySourcePath, fhirContext, resources, encoding);
//                }
//
//                if (includePatientScenarios) {
//                    shouldPersist = shouldPersist
//                        & bundleTestCases(igPath, refreshedLibraryName, fhirContext, resources);
//                }
//
//                List<String> activityDefinitionPaths =  bundleActivityDefinitions(planDefinitionSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);
//
//                if (shouldPersist) {
//                    String bundleDestPath = FilenameUtils.concat(getBundlesPath(igPath), refreshedLibraryName);
//                    persistBundle(igPath, bundleDestPath, refreshedLibraryName, encoding, fhirContext, new ArrayList<IAnyResource>(resources.values()), fhirUri);
//                    bundleFiles(igPath, bundleDestPath, refreshedLibraryName, planDefinitionSourcePath, librarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies, includePatientScenarios, includeVersion);
//                    addActivityDefinitionFilesToBundle(igPath, bundleDestPath, refreshedLibraryName, activityDefinitionPaths, fhirContext, encoding);
//                    addRequestAndResponseFilesToBundle(igPath, bundleDestPath, refreshedLibraryName);
//                    bundledPlanDefinitions.add(refreshedLibraryName);
//                }
//            } catch (Exception e) {
//                LogUtils.putException(refreshedLibraryName, e);
//            } finally {
//                LogUtils.warn(refreshedLibraryName);
//            }
//        }
//        String message = "\r\n***************PLANDEFINITION RESULTS:******************\r\n" + bundledPlanDefinitions.size() + " PlanDefinitions successfully bundled:";
//        for (String bundledPlanDefinition : bundledPlanDefinitions) {
//            message += "\r\n     " + bundledPlanDefinition + " BUNDLED";
//        }
//
//        ArrayList<String> failedPlanDefinitions = new ArrayList<>(planDefinitionPathLibraryNames);
//        planDefinitionPathLibraryNames.removeAll(bundledPlanDefinitions);
//        planDefinitionPathLibraryNames.retainAll(refreshedLibraryNames);
//        message += "\r\n" + planDefinitionPathLibraryNames.size() + " PlanDefinitions refreshed, but not bundled (due to issues):";
//        for (String notBundled : planDefinitionPathLibraryNames) {
//            message += "\r\n     " + notBundled + " REFRESHED";
//        }
//
//        failedPlanDefinitions.removeAll(bundledPlanDefinitions);
//        failedPlanDefinitions.removeAll(planDefinitionPathLibraryNames);
//        message += "\r\n" + failedPlanDefinitions.size() + " PlanDefinitions failed refresh:";
//        for (String failed : failedPlanDefinitions) {
//            message += "\r\n     " + failed + " FAILED";
//        }
//        message += "\r\n********************************************************";
//
//        LogUtils.info(message);
//    }
//
//    public static final String requestsPathElement = "input/pagecontent/requests/";
//    public static final String responsesPathElement = "input/pagecontent/responses/";
//    public static final String requestFilesPathElement = "requests/";
//    public static final String responseFilesPathElement = "responses/";
//    private static void addRequestAndResponseFilesToBundle(String igPath, String bundleDestPath, String libraryName) {
//        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
//        String requestFilesPath = FilenameUtils.concat(igPath, requestsPathElement);
//        String responseFilesPath = FilenameUtils.concat(igPath, responsesPathElement);
//        String requestFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, requestFilesPathElement);
//        IOUtils.initializeDirectory(requestFilesDirectory);
//        String responseFilesDirectory = FilenameUtils.concat(bundleDestFilesPath, responseFilesPathElement);
//        IOUtils.initializeDirectory(responseFilesDirectory);
//        List<String> requestDirectories = IOUtils.getDirectoryPaths(requestFilesPath, false);
//        for (String dir : requestDirectories) {
//            if (dir.endsWith(libraryName)) {
//                List<String> requestPaths = IOUtils.getFilePaths(dir, true);
//                for (String path : requestPaths) {
//                    IOUtils.copyFile(path, FilenameUtils.concat(requestFilesDirectory, FilenameUtils.getName(path)));
//                }
//            }
//        }
//        List<String> responseDirectories = IOUtils.getDirectoryPaths(responseFilesPath, false);
//        for (String dir : responseDirectories) {
//            if (dir.endsWith(libraryName)) {
//                List<String> responsePaths = IOUtils.getFilePaths(dir, true);
//                for (String path : responsePaths) {
//                    IOUtils.copyFile(path, FilenameUtils.concat(responseFilesDirectory, FilenameUtils.getName(path)));
//                }
//            }
//        }
//    }
//
//    public static Boolean bundleValueSets(String cqlContentPath, String igPath, FhirContext fhirContext,
//            Map<String, IAnyResource> resources, Encoding encoding, Boolean includeDependencies, Boolean includeVersion) {
//        Boolean shouldPersist = true;
//        try {
//            Map<String, IAnyResource> dependencies = ResourceUtils.getDepValueSetResources(cqlContentPath, igPath, fhirContext, includeDependencies, includeVersion);
//            for (IAnyResource resource : dependencies.values()) {
//                resources.putIfAbsent(resource.getId(), resource);
//            }
//        } catch (Exception e) {
//            shouldPersist = false;
//            LogUtils.putException(cqlContentPath, e);
//        }
//        return shouldPersist;
//    }
//
//    public static List<String> bundleActivityDefinitions(String planDefinitionPath, FhirContext fhirContext, Map<String, IAnyResource> resources,
//    Encoding encoding, Boolean includeVersion, Boolean shouldPersist) {
//        List<String> activityDefinitionPaths = new ArrayList<String>();
//        try {
//            Map<String, IAnyResource> activityDefinitions = ResourceUtils.getActivityDefinitionResources(planDefinitionPath, fhirContext, includeVersion);
//            for (Entry<String, IAnyResource> entry : activityDefinitions.entrySet()) {
//                resources.putIfAbsent(entry.getValue().getId(), entry.getValue());
//                activityDefinitionPaths.add(entry.getKey());
//            }
//        } catch (Exception e) {
//            shouldPersist = false;
//            LogUtils.putException(planDefinitionPath, e);
//        }
//        return activityDefinitionPaths;
//    }
//
//    public static Boolean bundleDependencies(String path, FhirContext fhirContext, Map<String, IAnyResource> resources,
//            Encoding encoding) {
//        Boolean shouldPersist = true;
//        try {
//            Map<String, IAnyResource> dependencies = ResourceUtils.getDepLibraryResources(path, fhirContext, encoding);
//
//            String currentResourceID = FilenameUtils.getBaseName(path);
//            for (IAnyResource resource : dependencies.values()) {
//                resources.putIfAbsent(resource.getId(), resource);
//
//                // NOTE: Assuming dependency library will be in directory of dependent.
//                String dependencyPath = path.replace(currentResourceID, FilenameUtils.getBaseName(resource.getId()));
//                bundleDependencies(dependencyPath, fhirContext, resources, encoding);
//            }
//        } catch (Exception e) {
//            shouldPersist = false;
//            LogUtils.putException(path, e);
//        }
//        return shouldPersist;
//    }
//
//    private static Boolean bundleTestCases(String igPath, String libraryName, FhirContext fhirContext,
//            Map<String, IAnyResource> resources) {
//        Boolean shouldPersist = true;
//        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(igPath, testCasePathElement), libraryName);
//
//        // this is breaking for bundle of a bundle. Replace with individual resources
//        // until we can figure it out.
//        // List<String> testCaseSourcePaths = IOUtils.getFilePaths(igTestCasePath,
//        // false);
//        // for (String testCaseSourcePath : testCaseSourcePaths) {
//        // shouldPersist = shouldPersist & safeAddResource(testCaseSourcePath,
//        // resources, fhirContext);
//        // }
//
//        try {
//            List<IAnyResource> testCaseResources = TestCaseProcessor.getTestCaseResources(igTestCasePath, fhirContext);
//            for (IAnyResource resource : testCaseResources) {
//                resources.putIfAbsent(resource.getId(), resource);
//            }
//        } catch (Exception e) {
//            shouldPersist = false;
//            LogUtils.putException(igTestCasePath, e);
//        }
//        return shouldPersist;
//    }
//
//    private static void persistBundle(String igPath, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, List<IAnyResource> resources, String fhirUri) {
//        IOUtils.initializeDirectory(bundleDestPath);
//        Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
//        IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);
//
//        if (fhirUri != null && !fhirUri.equals("")) {
//            try {
//                HttpClientUtils.post(fhirUri, (IAnyResource) bundle, encoding, fhirContext);
//                LogUtils.info("Resource successfully posted to FHIR server (" + fhirUri + "): " + ((IAnyResource)bundle).getId());
//            } catch (Exception e) {
//                LogUtils.putException(((IAnyResource)bundle).getId(), e);
//            }
//        }
//    }
//
//    public static final String bundleFilesPathElement = "files/";
//    private static void addActivityDefinitionFilesToBundle(String igPath, String bundleDestPath, String libraryName, List<String> activityDefinitionPaths, FhirContext fhirContext, Encoding encoding) {
//        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
//        for (String path : activityDefinitionPaths) {
//            IOUtils.copyFile(path, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(path)));
//        }
//    }
//    private static void bundleFiles(String igPath, String bundleDestPath, String libraryName, String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext, Encoding encoding, Boolean includeTerminology, Boolean includeDependencies, Boolean includePatientScenarios, Boolean includeVersion) {
//        String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath, libraryName + "-" + bundleFilesPathElement);
//        IOUtils.initializeDirectory(bundleDestFilesPath);
//
//        IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
//        IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));
//
//        String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL, fhirContext);
//        List<String> cqlLibrarySourcePaths = IOUtils.getCqlLibraryPaths().stream()
//            .filter(path -> path.endsWith(cqlFileName))
//            .collect(Collectors.toList());
//        String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);
//        String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
//        IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);
//
//        if (includeTerminology) {
//            try {
//                Map<String, IAnyResource> valuesets = ResourceUtils.getDepValueSetResources(cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
//                if (!valuesets.isEmpty()) {
//                    Object bundle = BundleUtils.bundleArtifacts(ValueSetsProcessor.getId(libraryName), new ArrayList<IAnyResource>(valuesets.values()), fhirContext);
//                    IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
//                }
//            }  catch (Exception e) {
//                LogUtils.putException(libraryName, e);
//            }
//        }
//
//        if (includeDependencies) {
//            Map<String, IAnyResource> depLibraries = ResourceUtils.getDepLibraryResources(librarySourcePath, fhirContext, encoding);
//
//            //TODO: Needs to be recursive
//            if (!depLibraries.isEmpty()) {
//                String depLibrariesID = "library-deps-" + libraryName;
//                Object bundle = BundleUtils.bundleArtifacts(depLibrariesID, new ArrayList<IAnyResource>(depLibraries.values()), fhirContext);
//                IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
//            }
//        }
//
//         if (includePatientScenarios) {
//            bundleTestCaseFiles(igPath, libraryName, bundleDestFilesPath, fhirContext);
//        }
//    }
//
//    //TODO: the bundle needs to have -expectedresults added too
//    public static void bundleTestCaseFiles(String igPath, String libraryName, String destPath, FhirContext fhirContext) {
//        String igTestCasePath = FilenameUtils.concat(FilenameUtils.concat(igPath, testCasePathElement), libraryName);
//        List<String> testCasePaths = IOUtils.getFilePaths(igTestCasePath, false);
//        for (String testPath : testCasePaths) {
//            String bundleTestDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testPath));
//            IOUtils.copyFile(testPath, bundleTestDestPath);
//
//            List<String> testCaseDirectories = IOUtils.getDirectoryPaths(igTestCasePath, false);
//            for (String testCaseDirectory : testCaseDirectories) {
//                List<String> testContentPaths = IOUtils.getFilePaths(testCaseDirectory, false);
//                for (String testContentPath : testContentPaths) {
//                    Optional<String> matchingMeasureReportPath = IOUtils.getMeasureReportPaths(fhirContext).stream()
//                        .filter(path -> path.equals(testContentPath))
//                        .findFirst();
//                    if (matchingMeasureReportPath.isPresent()) {
//                        IAnyResource measureReport = IOUtils.readResource(testContentPath, fhirContext);
//                        if (!measureReport.getId().startsWith("measurereport") || !measureReport.getId().endsWith("-expectedresults")) {
//                            Object measureReportStatus = ResourceUtils.resolveProperty(measureReport, "status", fhirContext);
//                            String measureReportStatusValue = ResourceUtils.resolveProperty(measureReportStatus, "value", fhirContext).toString();
//                            if (measureReportStatusValue.equals("COMPLETE")) {
//                                String expectedResultsId = FilenameUtils.getBaseName(testContentPath) + (FilenameUtils.getBaseName(testContentPath).endsWith("-expectedresults") ? "" : "-expectedresults");
//                                measureReport.setId(expectedResultsId);
//                            }
//                        }
//                        IOUtils.writeResource(measureReport, destPath, IOUtils.Encoding.JSON, fhirContext);
//                    }
//                    else {
//                        String bundleTestContentDestPath = FilenameUtils.concat(destPath, FilenameUtils.getName(testContentPath));
//                        IOUtils.copyFile(testContentPath, bundleTestContentDestPath);
//                    }
//                }
//            }
//        }
//    }
//
//    public static FhirContext getIgFhirContext(IGVersion igVersion)
//    {
//        switch (igVersion) {
//            case FHIR3:
//                return FhirContext.forDstu3();
//            case FHIR4:
//                return FhirContext.forR4();
//            default:
//                throw new IllegalArgumentException("Unknown IG version: " + igVersion);
//        }
//    }
//
//    public static IGVersion getIgVersion(String igPath){
//        IGVersion igVersion = null;
//        List<File> igPathFiles = IOUtils.getFilePaths(igPath, false).stream()
//            .map(path -> new File(path))
//            .collect(Collectors.toList());
//        for (File file : igPathFiles) {
//            if (FilenameUtils.getExtension(file.getName()).equals("ini")) {
//                igVersion = tryToReadIni(file);
//            }
//        }
//        if (igVersion == null) {
//            throw new IllegalArgumentException("IG version must be configured in ig.ini or provided as an argument.");
//        }
//        else return igVersion;
//    }
//
//    private static IGVersion tryToReadIni(File file) {
//        try {
//            InputStream inputStream = new FileInputStream(file);
//            String igIniContent = new BufferedReader(new InputStreamReader(inputStream))
//                .lines().collect(Collectors.joining("\n"));
//            String[] contentLines = igIniContent.split("\n");
//            inputStream.close();
//            return parseVersion(contentLines);
//        } catch (Exception e) {
//                System.out.println(e.getMessage());
//                return null;
//            }
//    }
//
//    static final String STU3SPECIFIER = "stu3";
//    static final String DSTU3SPECIFIER = "dstu3";
//    static final String R4SPECIFIER = "r4";
//    private static IGVersion parseVersion(String[] contentLines) {
//        for (String line : contentLines) {
//            if (line.toLowerCase().startsWith("fhirspec"))
//            {
//                if (line.toLowerCase().contains(R4SPECIFIER)){
//                    return IGVersion.FHIR4;
//                }
//                else if (line.toLowerCase().contains(STU3SPECIFIER) || line.toLowerCase().contains(DSTU3SPECIFIER)) {
//                    return IGVersion.FHIR3;
//                }
//            }
//        }
//        return null;
//    }
//
//    public static final String bundlePathElement = "bundles/";
//    public static String getBundlesPath(String igPath) {
//        return FilenameUtils.concat(igPath, bundlePathElement);
//    }
//    public static final String cqlLibraryPathElement = "input/pagecontent/cql/";
//    public static final String libraryPathElement = "input/resources/library/";
//    public static final String measurePathElement = "input/resources/measure/";
//    public static final String valuesetsPathElement = "input/vocabulary/valueset/";
//    public static final String testCasePathElement = "input/tests/";
//
//    private static void ensure(String igPath, Boolean includePatientScenarios, Boolean includeTerminology, ArrayList<String> resourcePaths) {
//
//        System.out.println("Enforcing conventions...");
//        File directory = new File(getBundlesPath(igPath));
//        if (!directory.exists()) {
//            directory.mkdir();
//        }
//        if (resourcePaths.isEmpty()) {
//            ensureDirectory(igPath, IGProcessor.cqlLibraryPathElement);
//            ensureDirectory(igPath, IGProcessor.libraryPathElement);
//            ensureDirectory(igPath, IGProcessor.measurePathElement);
//            ensureDirectory(igPath, IGProcessor.valuesetsPathElement);
//            ensureDirectory(igPath, IGProcessor.testCasePathElement);
//        }
//        else {
//            checkForDirectory(igPath, IGProcessor.cqlLibraryPathElement);
//            checkForDirectory(igPath, IGProcessor.libraryPathElement);
//            checkForDirectory(igPath, IGProcessor.measurePathElement);
//            checkForDirectory(igPath, IGProcessor.valuesetsPathElement);
//            checkForDirectory(igPath, IGProcessor.testCasePathElement);
//        }
//        HashSet<String> cqlContentPaths = IOUtils.getCqlLibraryPaths();
//        for (String cqlContentPath : cqlContentPaths) {
//            String cqlLibraryContent = IOUtils.getCqlString(cqlContentPath);
//            if (!cqlLibraryContent.startsWith("library ")) {
//                throw new RuntimeException("Unable to refresh IG.  All Libraries must begin with \"library \": " + cqlContentPath);
//            }
//            String strippedLibraryName = FilenameUtils.getBaseName(cqlContentPath);
//            for (IGProcessor.IGVersion igVersion : IGVersion.values()) {
//                String igVersionToken = "_" + igVersion.toString().toUpperCase();
//
//                if (strippedLibraryName.contains(igVersionToken)) {
//                    strippedLibraryName = strippedLibraryName.replace(igVersionToken, "");
//                    if (strippedLibraryName.contains("_")) {
//                        throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
//                    }
//                }
//            }
//            if (strippedLibraryName.contains("_")) {
//                throw new RuntimeException("Convention only allows a single \"_\" and it must be preceeding the IG Version: " + cqlContentPath);
//            }
//        }
//    }
//
//    private static void ensureDirectory(String igPath, String pathElement) {
//        File directory = new File(FilenameUtils.concat(igPath, pathElement));
//        if (!directory.exists()) {
//            throw new RuntimeException("Convention requires the following directory:" + pathElement);
//        }
//        // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
//        // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
//        // leads to surprising results when bundling like picking up resources from the /tests directory.
//        IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
//    }
//
//    private static void checkForDirectory(String igPath, String pathElement) {
//        File directory = new File(FilenameUtils.concat(igPath, pathElement));
//        if (!directory.exists()) {
//            System.out.println("No directory found by convention for: " + directory.getName());
//        }
//        else {
//            // TODO: This is a concept different from "resource directories". It is expected elsewhere (e.g., IOUtils.setupActivityDefinitionPaths)
//            // that resourceDirectories contains a set of proper "resource" directories. Adding non-resource directories
//            // leads to surprising results when bundling like picking up resources from the /tests directory.
//            IOUtils.resourceDirectories.add(FilenameUtils.concat(igPath, pathElement));
//        }
//    }
}
