package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeCompositeDatatypeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource path discovery and caching, extracted from IOUtils.
 * Provides methods for finding and caching paths to FHIR resources
 * (Libraries, Measures, ValueSets, PlanDefinitions, etc.) across resource directories.
 */
public class ResourceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDiscovery.class);

    private ResourceDiscovery() {}

    // --- CQL Library Paths ---

    private static final Set<String> cqlLibraryPaths = new LinkedHashSet<>();
    public static Set<String> getCqlLibraryPaths() {
        if (cqlLibraryPaths.isEmpty()) {
            setupCqlLibraryPaths();
        }
        return cqlLibraryPaths;
    }
    private static void setupCqlLibraryPaths() {
        for (String dir : IOUtils.resourceDirectories) {
            List<String> filePaths = IOUtils.getFilePaths(dir, true);
            filePaths.stream().filter(path -> path.contains(".cql")).forEach(cqlLibraryPaths::add);
        }
    }

    public static String getCqlLibrarySourcePath(String libraryName, String cqlFileName, List<String> binaryPaths) {
        List<String> cqlLibrarySourcePaths = ResourceDiscovery.getCqlLibraryPaths().stream()
                .filter(path -> path.endsWith(cqlFileName))
                .collect(Collectors.toList());
        String cqlLibrarySourcePath = (cqlLibrarySourcePaths.isEmpty()) ? null : cqlLibrarySourcePaths.get(0);

        try {
            if (cqlLibrarySourcePath == null) {
                for (String path : binaryPaths) {
                    File f = new File(Utilities.path(path, cqlFileName));
                    if (f.exists()) {
                        cqlLibrarySourcePath = f.getAbsolutePath();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            LogUtils.putException(libraryName, e);
        }

        return cqlLibrarySourcePath;
    }

    // --- Terminology Paths ---

    private static final Set<String> terminologyPaths = new LinkedHashSet<>();
    public static Set<String> getTerminologyPaths(FhirContext fhirContext) {
        if (terminologyPaths.isEmpty()) {
            setupTerminologyPaths(fhirContext);
        }
        return terminologyPaths;
    }
    private static void setupTerminologyPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for (String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    if (path.toLowerCase().contains("valuesets") || path.toLowerCase().contains("valueset")) {
                        logger.error("Error reading in Terminology from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            RuntimeResourceDefinition valuesetDefinition = ResourceUtils.getResourceDefinition(fhirContext, "ValueSet");
            RuntimeCompositeDatatypeDefinition conceptDefinition = (RuntimeCompositeDatatypeDefinition)ResourceUtils.getElementDefinition(fhirContext, "CodeableConcept");
            RuntimeCompositeDatatypeDefinition codingDefinition = (RuntimeCompositeDatatypeDefinition)ResourceUtils.getElementDefinition(fhirContext, "Coding");
            String valuesetClassName = valuesetDefinition.getImplementingClass().getName();
            String conceptClassName = conceptDefinition.getImplementingClass().getName();
            String codingClassName = codingDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->
                            valuesetClassName.equals(entry.getValue().getClass().getName())
                                    || conceptClassName.equals(entry.getValue().getClass().getName())
                                    || codingClassName.equals(entry.getValue().getClass().getName())
                    )
                    .forEach(entry -> terminologyPaths.add(entry.getKey()));
        }
    }

    // --- Library Paths ---

    public static IBaseResource getLibraryByUrl(FhirContext fhirContext, String url) {
        IBaseResource library = getLibraryUrlMap(fhirContext).get(url);
        if (library == null) {
            throw new IllegalArgumentException(String.format("Could not load library with url %s", url));
        }
        return library;
    }

    private static final Set<String> libraryPaths = new LinkedHashSet<>();
    public static Set<String> getLibraryPaths(FhirContext fhirContext) {
        if (libraryPaths.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryPaths;
    }
    private static final Map<String, IBaseResource> libraryUrlMap = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getLibraryUrlMap(FhirContext fhirContext) {
        if (libraryPathMap.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryUrlMap;
    }
    private static final Map<String, String> libraryPathMap = new LinkedHashMap<>();
    public static Map<String, String> getLibraryPathMap(FhirContext fhirContext) {
        if (libraryPathMap.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraryPathMap;
    }
    private static final Map<String, IBaseResource> libraries = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getLibraries(FhirContext fhirContext) {
        if (libraries.isEmpty()) {
            setupLibraryPaths(fhirContext);
        }
        return libraries;
    }
    private static void setupLibraryPaths(FhirContext fhirContext) {
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    resources.put(path, resource);
                } catch (Exception e) {
                    if(path.toLowerCase().contains("library")) {
                        logger.error("Error reading in Library from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            RuntimeResourceDefinition libraryDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Library");
            String libraryClassName = libraryDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  libraryClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        libraryPaths.add(entry.getKey());
                        libraries.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        libraryPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                        String url = ResourceUtils.getUrl(entry.getValue(), fhirContext);
                        var version = ResourceUtils.getVersion(entry.getValue(), fhirContext);
                        if (url != null) {
                            libraryUrlMap.put(ResourceUtils.getUrl(entry.getValue(), fhirContext), entry.getValue());
                            if (version != null) {
                                libraryUrlMap.put(ResourceUtils.getUrl(entry.getValue(), fhirContext) + "|" + version, entry.getValue());
                            }
                        }
                    });
        }
    }

    // --- Measure Paths ---

    private static final Set<String> measurePaths = new LinkedHashSet<>();
    public static Set<String> getMeasurePaths(FhirContext fhirContext) {
        if (measurePaths.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measurePaths;
    }
    private static final Map<String, String> measurePathMap = new LinkedHashMap<>();
    public static Map<String, String> getMeasurePathMap(FhirContext fhirContext) {
        if (measurePathMap.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measurePathMap;
    }
    private static final Map<String, IBaseResource> measures = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getMeasures(FhirContext fhirContext) {
        if (measures.isEmpty()) {
            setupMeasurePaths(fhirContext);
        }
        return measures;
    }
    private static void setupMeasurePaths(FhirContext fhirContext) {
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    resources.put(path, resource);
                } catch (Exception e) {
                    if(path.toLowerCase().contains("measure")) {
                        logger.error("Error reading in Measure from path: " + path, e);
                    }
                }
            }
            RuntimeResourceDefinition measureDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Measure");
            String measureClassName = measureDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  measureClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        measurePaths.add(entry.getKey());
                        measures.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        measurePathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    // --- MeasureReport Paths ---

    private static final Set<String> measureReportPaths = new LinkedHashSet<>();
    public static Set<String> getMeasureReportPaths(FhirContext fhirContext) {
        if (measureReportPaths.isEmpty()) {
            setupMeasureReportPaths(fhirContext);
        }
        return measureReportPaths;
    }
    private static void setupMeasureReportPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            RuntimeResourceDefinition measureReportDefinition = ResourceUtils.getResourceDefinition(fhirContext, "MeasureReport");
            String measureReportClassName = measureReportDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  measureReportClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> measureReportPaths.add(entry.getKey()));
        }
    }

    // --- PlanDefinition Paths ---

    private static final Set<String> planDefinitionPaths = new LinkedHashSet<>();
    public static Set<String> getPlanDefinitionPaths(FhirContext fhirContext) {
        if (planDefinitionPaths.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitionPaths;
    }
    private static final Map<String, String> planDefinitionPathMap = new LinkedHashMap<>();
    public static Map<String, String> getPlanDefinitionPathMap(FhirContext fhirContext) {
        if (planDefinitionPathMap.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitionPathMap;
    }
    private static final Map<String, IBaseResource> planDefinitions = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getPlanDefinitions(FhirContext fhirContext) {
        if (planDefinitions.isEmpty()) {
            setupPlanDefinitionPaths(fhirContext);
        }
        return planDefinitions;
    }
    private static void setupPlanDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    logger.error("Error setting PlanDefinition paths while reading resource at: {}. Error: {}", path, e.getMessage());
                }
            }
            RuntimeResourceDefinition planDefinitionDefinition = ResourceUtils.getResourceDefinition(fhirContext, "PlanDefinition");
            String planDefinitionClassName = planDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  planDefinitionClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        planDefinitionPaths.add(entry.getKey());
                        planDefinitions.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        planDefinitionPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    // --- Questionnaire Paths ---

    private static final Set<String> questionnairePaths = new LinkedHashSet<>();
    public static Set<String> getQuestionnairePaths(FhirContext fhirContext) {
        if (questionnairePaths.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnairePaths;
    }

    private static final Map<String, String> questionnairePathMap = new LinkedHashMap<>();
    public static Map<String, String> getQuestionnairePathMap(FhirContext fhirContext) {
        if (questionnairePathMap.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnairePathMap;
    }

    private static final Map<String, IBaseResource> questionnaires = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getQuestionnaires(FhirContext fhirContext) {
        if (questionnaires.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return questionnaires;
    }

    private static void setupQuestionnairePaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    logger.error("Error setting Questionnaire paths while reading resource at: {}. Error: {}", path, e.getMessage());
                }
            }
            RuntimeResourceDefinition questionnaireDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Questionnaire");
            String questionnaireClassName = questionnaireDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  questionnaireClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        questionnairePaths.add(entry.getKey());
                        questionnaires.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        questionnairePathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    // --- ActivityDefinition Paths ---

    private static final Map<String, String> activityDefinitionPathMap = new LinkedHashMap<>();
    public static Map<String, String> getActivityDefinitionPathMap(FhirContext fhirContext) {
        if (activityDefinitionPathMap.isEmpty()) {
            setupQuestionnairePaths(fhirContext);
        }
        return activityDefinitionPathMap;
    }

    private static final Map<String, IBaseResource> activityDefinitions = new LinkedHashMap<>();
    public static Map<String, IBaseResource> getActivityDefinitions(FhirContext fhirContext) {
        if (activityDefinitions.isEmpty()) {
            setupActivityDefinitionPaths(fhirContext);
        }
        return activityDefinitions;
    }

    private static final Set<String> activityDefinitionPaths = new LinkedHashSet<>();
    public static Set<String> getActivityDefinitionPaths(FhirContext fhirContext) {
        if (activityDefinitionPaths.isEmpty()) {
            logger.info("Reading activitydefinitions");
            setupActivityDefinitionPaths(fhirContext);
        }
        return activityDefinitionPaths;
    }

    private static void setupActivityDefinitionPaths(FhirContext fhirContext) {
        HashMap<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    resources.put(path, IOUtils.readResource(path, fhirContext, true));
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
            RuntimeResourceDefinition activityDefinitionDefinition = ResourceUtils.getResourceDefinition(fhirContext, "ActivityDefinition");
            String activityDefinitionClassName = activityDefinitionDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  activityDefinitionClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> {
                        activityDefinitionPaths.add(entry.getKey());
                        activityDefinitions.put(entry.getValue().getIdElement().getIdPart(), entry.getValue());
                        activityDefinitionPathMap.put(entry.getValue().getIdElement().getIdPart(), entry.getKey());
                    });
        }
    }

    // --- Device Paths ---

    private static Set<String> devicePaths;
    public static Set<String> getDevicePaths(FhirContext fhirContext) {
        if (devicePaths == null) {
            setupDevicePaths(fhirContext);
        }
        return devicePaths;
    }

    public static void clearDevicePaths() {
        devicePaths = null;
    }

    private static void setupDevicePaths(FhirContext fhirContext) {
        devicePaths = new LinkedHashSet<>();
        Map<String, IBaseResource> resources = new LinkedHashMap<>();
        for (String dir : IOUtils.resourceDirectories) {
            for(String path : IOUtils.getFilePaths(dir, true)) {
                try {
                    IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
                    if (resource != null) {
                        resources.put(path, resource);
                    }
                } catch (Exception e) {
                    if(path.toLowerCase().contains("device")) {
                        logger.error("Error reading in Device from path: {} \n {}", path, e.getMessage());
                    }
                }
            }
            RuntimeResourceDefinition deviceDefinition = ResourceUtils.getResourceDefinition(fhirContext, "Device");
            String deviceClassName = deviceDefinition.getImplementingClass().getName();
            resources.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry ->  deviceClassName.equals(entry.getValue().getClass().getName()))
                    .forEach(entry -> devicePaths.add(entry.getKey()));
        }
    }

    /**
     * Cleans up all cached data — resource discovery caches, CQL caches, and core IOUtils caches.
     * This provides the same behavior as the original IOUtils.cleanUp().
     */
    public static void cleanUp() {
        // Clean core IOUtils caches
        IOUtils.cleanUp();
        // Clean CQL caches
        CqlIOUtils.cleanUp();
        // Clean resource discovery caches
        cqlLibraryPaths.clear();
        terminologyPaths.clear();
        libraryPaths.clear();
        libraryUrlMap.clear();
        libraryPathMap.clear();
        libraries.clear();
        measurePaths.clear();
        measurePathMap.clear();
        measures.clear();
        measureReportPaths.clear();
        planDefinitionPaths.clear();
        planDefinitionPathMap.clear();
        planDefinitions.clear();
        questionnairePaths.clear();
        questionnairePathMap.clear();
        questionnaires.clear();
        activityDefinitionPaths.clear();
    }
}
