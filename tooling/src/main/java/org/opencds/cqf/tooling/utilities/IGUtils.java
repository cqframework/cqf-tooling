package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.exception.IGInitializationException;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IGUtils {
    private static final Logger logger = LoggerFactory.getLogger(IGUtils.class);
    private IGUtils () {}
    public static String getImplementationGuideCanonicalBase(String url) {
        String canonicalBase = null;

        if (url != null && !url.isEmpty()) {
            canonicalBase = url.substring(0, url.indexOf("/ImplementationGuide/"));
        }

        return canonicalBase;
    }

    public static List<String> extractResourcePaths(String rootDir, ImplementationGuide sourceIg) throws IOException {
        List<String> result = new ArrayList<>();
        for (ImplementationGuide.ImplementationGuideDefinitionParameterComponent p : sourceIg.getDefinition().getParameter()) {
            if (p.getCode().equals("path-resource")) {
                result.add(Utilities.path(rootDir, p.getValue()));
            }
        }

        File resources = new File(Utilities.path(rootDir, "input/resources"));
        if (resources.exists() && resources.isDirectory()) {
            result.add(resources.getAbsolutePath());
        }

        return result;
    }

    /*
    Determines the CQL content path for the given implementation guide
    @rootDir: The root directory of the implementation guide source
    @sourceIg: The implementationGuide (as an R5 resource)
     */
    public static List<String> extractBinaryPaths(String rootDir, ImplementationGuide sourceIg) {
        List<String> result = new ArrayList<>();

        // Although this is the correct way to read the cql path from an implementation guide,
        // the tooling cannot use this method, because if it's present in the IG, the publisher will
        // redo the CQL translation work. Instead, assume a path of input/cql, or input/pagecontent/cql
        /*
        for (ImplementationGuide.ImplementationGuideDefinitionParameterComponent p : sourceIg.getDefinition().getParameter()) {
            // documentation for this list: https://confluence.hl7.org/display/FHIR/Implementation+Guide+Parameters
            if (p.getCode().equals("path-binary")) {
                result.add(Utilities.path(rootDir, p.getValue()));
            }
        }
        */

        File input = tryDirectory(rootDir, "input/cql");
        if (input != null && input.exists() && input.isDirectory()) {
            result.add(input.getAbsolutePath());
        }

        input = tryDirectory(rootDir, "input/pagecontent/cql");
        if (input != null && input.exists() && input.isDirectory()) {
            result.add(input.getAbsolutePath());
        }

        return result;
    }

    /**
     * Tries to create a File for a path that may not exist.
     *
     * @param rootDir base directory for path
     * @param path child directories for path
     * @return File if directory exists, null otherwise
     */
    protected static File tryDirectory(String rootDir, String path) {
        String combinedPath = null;
        try {
            combinedPath = Utilities.path(rootDir, path);
        }
        catch (IOException e) {
            return null;
        }

        return new File(combinedPath);
    }

    public static class IGInfo {
        private final FhirContext fhirContext;
        private final String rootDir;
        private final String iniPath;
        private final String igPath;
        private final String cqlBinaryPath;
        private final String resourcePath;
        private final String libraryResourcePath;
        private boolean refreshLibraries = true;
        private final String planDefinitionResourcePath;
        private boolean refreshPlanDefinitions = true;
        private final String measureResourcePath;
        private boolean refreshMeasures = true;
        private final String valueSetResourcePath;
        private final String codeSystemResourcePath;
        private final String activityDefinitionResourcePath;
        private final String questionnaireResourcePath;
        private final ImplementationGuide igResource;
        private final String packageId;
        private final String canonical;

        @SuppressWarnings("this-escape")
        public IGInfo(FhirContext fhirContext, String rootDir) {
            if (fhirContext == null) {
                this.fhirContext = FhirContext.forR4Cached();
                logger.info("The FHIR context was not provided, using {}",
                        this.fhirContext.getVersion().getVersion().getFhirVersionString());
            }
            else {
                this.fhirContext = fhirContext;
            }
            if (rootDir == null) {
                throw new IGInitializationException("The root directory path for the IG not provided");
            }
            this.rootDir = rootDir;
            this.iniPath = getIniPath();
            this.igPath = getIgPath();
            this.cqlBinaryPath = getCqlBinaryPath();
            this.resourcePath = getResourcePath();
            this.libraryResourcePath = getLibraryResourcePath();
            this.planDefinitionResourcePath = getPlanDefinitionResourcePath();
            this.measureResourcePath = getMeasureResourcePath();
            this.valueSetResourcePath = getValueSetResourcePath();
            this.codeSystemResourcePath = getCodeSystemResourcePath();
            this.activityDefinitionResourcePath = getActivityDefinitionResourcePath();
            this.questionnaireResourcePath = getQuestionnaireResourcePath();
            this.igResource = getIgResource();
            this.packageId = getPackageId();
            this.canonical = getCanonical();
        }

        public FhirContext getFhirContext() {
            return fhirContext;
        }

        public String getRootDir() {
            return rootDir;
        }

        public String getIniPath() {
            if (this.iniPath != null) {
                return this.iniPath;
            }
            try (Stream<Path> walk = Files.walk(Paths.get(this.rootDir))) {
                List<String> pathList = walk.filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString().toLowerCase())
                        .filter(f -> f.endsWith("ig.ini") && !f.endsWith(IOUtils.concatFilePath("template", "ig.ini")))
                        .collect(Collectors.toList());
                if (pathList.isEmpty()) {
                    logger.error("Unable to determine path to IG ini file");
                    throw new IGInitializationException("An IG ini file must be present! See https://build.fhir.org/ig/FHIR/ig-guidance/using-templates.html#igroot for more information.");
                }
                else if (pathList.size() > 1) {
                    logger.warn("Found multiple IG ini files, using {}", pathList.get(0));
                }
                return pathList.get(0);
            } catch (IOException ioe) {
                logger.error("Error determining path to IG ini file");
                throw new IGInitializationException(ioe.getMessage(), ioe);
            }
        }

        public String getIgPath() {
            if (this.igPath != null) {
                return this.igPath;
            }
            try {
                List<String> igList = FileUtils.readLines(new File(iniPath), StandardCharsets.UTF_8)
                        .stream().filter(s -> s.startsWith("ig")).map(
                                s -> StringUtils.deleteWhitespace(s).replace("ig=", ""))
                        .collect(Collectors.toList());
                if (igList.isEmpty()) {
                    logger.error("Unable to determine path to IG resource file");
                    throw new IGInitializationException("An IG resource file must be present! See https://build.fhir.org/ig/FHIR/ig-guidance/using-templates.html#igroot-input for more information.");
                }
                else if (igList.size() > 1) {
                    logger.warn("Found multiple IG resource files, using {}", igList.get(0));
                }
                return FilenameUtils.concat(rootDir, igList.get(0));
            } catch (IOException ioe) {
                logger.error("Error determining path to IG resource file");
                throw new IGInitializationException(ioe.getMessage(), ioe);
            }
        }

        public String getCqlBinaryPath() {
            if (this.cqlBinaryPath != null) {
                return this.cqlBinaryPath;
            }
            // preferred directory structure
            String candidate = FilenameUtils.concat(getRootDir(), "input/cql");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            // support legacy directory structure
            candidate = FilenameUtils.concat(getRootDir(), "input/pagecontent/cql");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                String message = "Unable to locate CQL binary directory, Please see https://github.com/cqframework/sample-content-ig#directory-structure for guidance on content IG directory structure.";
                logger.error(message);
                throw new IGInitializationException(message);
            }
        }

        public String getResourcePath() {
            if (this.resourcePath != null) {
                return this.resourcePath;
            }
            String candidate = FilenameUtils.concat(getRootDir(), "input/resources");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                String message = "Unable to locate the resources directory, Please see https://github.com/cqframework/sample-content-ig#directory-structure for guidance on content IG directory structure.";
                logger.error(message);
                throw new IGInitializationException(message);
            }
        }

        public String getLibraryResourcePath() {
            if (this.libraryResourcePath != null) {
                return this.libraryResourcePath;
            }
            if (refreshLibraries) {
                String candidate = FilenameUtils.concat(getResourcePath(), "library");
                if (new File(candidate).isDirectory()) {
                    return candidate;
                } else {
                    logger.warn("Unable to locate the Library resource directory. The base resources path will be used.");
                    return getResourcePath();
                }
            }
            return null;
        }

        public boolean isRefreshLibraries() {
            return this.refreshLibraries;
        }

        public void setRefreshLibraries(boolean refreshLibraries) {
            this.refreshLibraries = refreshLibraries;
        }

        public String getPlanDefinitionResourcePath() {
            if (this.planDefinitionResourcePath != null) {
                return this.planDefinitionResourcePath;
            }
            if (refreshPlanDefinitions) {
                String candidate = FilenameUtils.concat(getResourcePath(), "plandefinition");
                if (new File(candidate).isDirectory()) {
                    return candidate;
                } else {
                    logger.warn("Unable to locate the PlanDefinition resource directory. The base resources path will be used.");
                    return getResourcePath();
                }
            }
            return null;
        }

        public boolean isRefreshPlanDefinitions() {
            return this.refreshPlanDefinitions;
        }

        public void setRefreshPlanDefinitions(boolean refreshPlanDefinitions) {
            this.refreshPlanDefinitions = refreshPlanDefinitions;
        }

        public String getMeasureResourcePath() {
            if (this.measureResourcePath != null) {
                return this.measureResourcePath;
            }
            if (refreshMeasures) {
                String candidate = FilenameUtils.concat(getResourcePath(), "measure");
                if (new File(candidate).isDirectory()) {
                    return candidate;
                } else {
                    logger.warn("Unable to locate the Measure resource directory. The base resources path will be used.");
                    return getResourcePath();
                }
            }
            return null;
        }

        public boolean isRefreshMeasures() {
            return this.refreshMeasures;
        }

        public void setRefreshMeasures(boolean refreshMeasures) {
            this.refreshMeasures = refreshMeasures;
        }

        public String getValueSetResourcePath() {
            if (this.valueSetResourcePath != null) {
                return this.valueSetResourcePath;
            }
            String candidate = FilenameUtils.concat(getResourcePath(), "vocabulary/valueset");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getResourcePath(), "vocabulary");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getRootDir(), "input/vocabulary/valueset");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getRootDir(), "input/vocabulary");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                logger.warn("Unable to locate the ValueSet resource directory. The base resources path will be used.");
                return getResourcePath();
            }
        }

        public String getCodeSystemResourcePath() {
            if (this.codeSystemResourcePath != null) {
                return this.codeSystemResourcePath;
            }
            String candidate = FilenameUtils.concat(getResourcePath(), "vocabulary/codesystem");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getResourcePath(), "codesystem");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getRootDir(), "input/vocabulary/codesystem");
            if (new File(candidate).isDirectory()) {
                return candidate;
            }
            candidate = FilenameUtils.concat(getRootDir(), "input/codesystem");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                logger.warn("Unable to locate the CodeSystem resource directory. The base resources path will be used.");
                return getResourcePath();
            }
        }

        public String getActivityDefinitionResourcePath() {
            if (this.activityDefinitionResourcePath != null) {
                return this.activityDefinitionResourcePath;
            }
            String candidate = FilenameUtils.concat(getResourcePath(), "activitydefinition");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                logger.warn("Unable to locate the ActivityDefinition resource directory. The base resources path will be used.");
                return getResourcePath();
            }
        }

        public String getQuestionnaireResourcePath() {
            if (this.questionnaireResourcePath != null) {
                return this.questionnaireResourcePath;
            }
            String candidate = FilenameUtils.concat(getResourcePath(), "questionnaire");
            if (new File(candidate).isDirectory()) {
                return candidate;
            } else {
                logger.warn("Unable to locate the Questionnaire resource directory. The base resources path will be used.");
                return getResourcePath();
            }
        }

        public ImplementationGuide getIgResource() {
            if (this.igResource != null) {
                return this.igResource;
            }
            switch (this.fhirContext.getVersion().getVersion()) {
                case DSTU3:
                    return (ImplementationGuide) ResourceAndTypeConverter.stu3ToR5Resource(IOUtils.readResource(igPath, this.fhirContext));
                case R4:
                    return (ImplementationGuide) ResourceAndTypeConverter.r4ToR5Resource(IOUtils.readResource(igPath, this.fhirContext));
                case R5: return (ImplementationGuide) IOUtils.readResource(igPath, this.fhirContext);
                default: throw new IGInitializationException(
                        "Unsupported FHIR context: " + this.fhirContext.getVersion().getVersion().getFhirVersionString());
            }
        }

        public String getPackageId() {
            if (this.packageId != null) {
                return this.packageId;
            }
            if (!getIgResource().hasPackageId()) {
                String message = "A package ID must be present in the IG resource";
                logger.error(message);
                throw new IGInitializationException(message);
            }
            return getIgResource().getPackageId();
        }

        public String getCanonical() {
            if (this.canonical != null) {
                return this.canonical;
            }
            if (!getIgResource().hasUrl()) {
                String message = "A canonical must be present in the IG resource";
                logger.error(message);
                throw new IGInitializationException(message);
            }
            String url = getIgResource().getUrl();
            return url.contains("/ImplementationGuide/") ? url.substring(0, url.indexOf("/ImplementationGuide/")) : url;
        }
    }
}
