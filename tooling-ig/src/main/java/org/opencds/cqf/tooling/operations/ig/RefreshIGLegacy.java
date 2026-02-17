package org.opencds.cqf.tooling.operations.ig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Operation(name = "RefreshIGLegacy")
public class RefreshIGLegacy implements ExecutableOperation {
    private static final Logger logger = LoggerFactory.getLogger(RefreshIGLegacy.class);

    @OperationParam(
            alias = {"ini"},
            setter = "setIni",
            required = true,
            description = "Path to the IG ini file")
    private String ini;

    @OperationParam(
            alias = {"root-dir", "rd"},
            setter = "setRootDir",
            description = "Root directory of the IG")
    private String rootDir;

    @OperationParam(
            alias = {"ip", "ig-path"},
            setter = "setIgPath",
            description = "Path to the IG, relative to the root directory")
    private String igPath;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setOutputEncoding",
            defaultValue = "json",
            description = "If omitted, output will be generated using JSON encoding")
    private String outputEncoding;

    @OperationParam(
            alias = {"s", "skip-packages"},
            setter = "setSkipPackages",
            defaultValue = "false",
            description = "Specifies whether to skip packages building")
    private String skipPackages;

    @OperationParam(
            alias = {"elm", "include-elm"},
            setter = "setIncludeELM",
            defaultValue = "false",
            description = "If omitted ELM will not be produced or packaged")
    private String includeELM;

    @OperationParam(
            alias = {"d", "include-dependencies"},
            setter = "setIncludeDependencies",
            defaultValue = "false",
            description = "If omitted only the primary CQL library will be packaged")
    private String includeDependencies;

    @OperationParam(
            alias = {"t", "include-terminology"},
            setter = "setIncludeTerminology",
            defaultValue = "false",
            description = "If omitted terminology will not be packaged")
    private String includeTerminology;

    @OperationParam(
            alias = {"p", "include-patients"},
            setter = "setIncludePatientScenarios",
            defaultValue = "false",
            description = "If omitted patient scenario information will not be packaged")
    private String includePatientScenarios;

    @OperationParam(
            alias = {"v", "versioned"},
            setter = "setVersioned",
            defaultValue = "false",
            description = "If omitted resources must be uniquely named")
    private String versioned;

    @OperationParam(
            alias = {"uv", "updated-version"},
            setter = "setUpdatedVersion",
            description = "Version for the new libraries")
    private String updatedVersion;

    @OperationParam(
            alias = {"fs", "fhir-uri"},
            setter = "setFhirUri",
            description = "If omitted the final bundle will not be loaded to a FHIR server")
    private String fhirUri;

    @OperationParam(
            alias = {"mtrp", "measure-to-refresh-path"},
            setter = "setMeasureToRefreshPath",
            description = "Path to Measure to refresh")
    private String measureToRefreshPath;

    @OperationParam(
            alias = {"rp", "resourcepath"},
            setter = "setResourcePaths",
            description = "Use multiple times to define multiple resource directories, relative to the root directory")
    private String resourcePaths;

    @OperationParam(
            alias = {"lp", "librarypath"},
            setter = "setLibraryPath",
            description = "Single path, relative to the root directory, for library resources")
    private String libraryPath;

    @OperationParam(
            alias = {"libraryOutput", "libraryOutputPath", "lop"},
            setter = "setLibraryOutputPath",
            description = "If omitted, the libraries will overwrite any existing libraries")
    private String libraryOutputPath;

    @OperationParam(
            alias = {"measureOutput", "measureOutputPath", "mop"},
            setter = "setMeasureOutputPath",
            description = "If omitted, the measures will overwrite any existing measures")
    private String measureOutputPath;

    @OperationParam(
            alias = {"ss", "stamp"},
            setter = "setShouldApplySoftwareSystemStamp",
            defaultValue = "true",
            description = "Indicates whether refreshed resources should be stamped with the cqf-tooling stamp")
    private String shouldApplySoftwareSystemStamp;

    @OperationParam(
            alias = {"ts", "timestamp"},
            setter = "setAddBundleTimestamp",
            defaultValue = "false",
            description = "Indicates whether refreshed Bundle should attach timestamp of creation")
    private String addBundleTimestamp;

    @OperationParam(
            alias = {"x", "include-errors"},
            setter = "setVerboseMessaging",
            defaultValue = "false",
            description = "Indicates that a complete list of errors during refresh are included upon failure")
    private String verboseMessaging;

    @OperationParam(
            alias = {"pldr", "popDataRequirements"},
            setter = "setIncludePopulationLevelDataRequirements",
            defaultValue = "false",
            description = "If omitted, the measures will not include population-level data requirements")
    private String includePopulationLevelDataRequirements;

    @Override
    public void execute() {
        IOUtils.Encoding outputEncodingEnum = IOUtils.Encoding.JSON;
        if (outputEncoding != null) {
            outputEncodingEnum = IOUtils.Encoding.parse(outputEncoding.toLowerCase());
        }

        List<String> paths = new ArrayList<>();
        if (resourcePaths != null && !resourcePaths.isEmpty()) {
            for (String path : resourcePaths.split(",")) {
                paths.add(path.trim());
            }
        }
        if (libraryPath != null && !libraryPath.isEmpty()) {
            paths.add(libraryPath);
        }

        RefreshIGParameters params = new RefreshIGParameters();
        params.ini = ini;
        params.rootDir = rootDir;
        params.igPath = igPath;
        params.outputEncoding = outputEncodingEnum;
        params.skipPackages = Boolean.parseBoolean(skipPackages);
        params.includeELM = Boolean.parseBoolean(includeELM);
        params.includeDependencies = Boolean.parseBoolean(includeDependencies);
        params.includeTerminology = Boolean.parseBoolean(includeTerminology);
        params.includePatientScenarios = Boolean.parseBoolean(includePatientScenarios);
        params.versioned = Boolean.parseBoolean(versioned);
        params.shouldApplySoftwareSystemStamp = Boolean.parseBoolean(shouldApplySoftwareSystemStamp);
        params.addBundleTimestamp = Boolean.parseBoolean(addBundleTimestamp);
        params.libraryPath = libraryPath;
        params.resourceDirs = paths;
        params.fhirUri = fhirUri;
        params.measureToRefreshPath = measureToRefreshPath;
        params.libraryOutputPath = libraryOutputPath != null ? libraryOutputPath : "";
        params.measureOutputPath = measureOutputPath != null ? measureOutputPath : "";
        params.updatedVersion = updatedVersion != null ? updatedVersion : "";
        params.verboseMessaging = Boolean.parseBoolean(verboseMessaging);
        params.includePopulationLevelDataRequirements = Boolean.parseBoolean(includePopulationLevelDataRequirements);

        if (params.verboseMessaging == null || !params.verboseMessaging) {
            logger.info("Re-run with -x to for expanded reporting of errors, warnings, and informational messages.");
        }

        try {
            new IGProcessor().publishIG(params);
        } catch (IOException e) {
            logger.error("Error refreshing IG: ", e);
        }
    }

    public void setIni(String ini) {
        this.ini = ini;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void setIgPath(String igPath) {
        this.igPath = igPath;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setSkipPackages(String skipPackages) {
        this.skipPackages = skipPackages;
    }

    public void setIncludeELM(String includeELM) {
        this.includeELM = includeELM;
    }

    public void setIncludeDependencies(String includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    public void setIncludeTerminology(String includeTerminology) {
        this.includeTerminology = includeTerminology;
    }

    public void setIncludePatientScenarios(String includePatientScenarios) {
        this.includePatientScenarios = includePatientScenarios;
    }

    public void setVersioned(String versioned) {
        this.versioned = versioned;
    }

    public void setUpdatedVersion(String updatedVersion) {
        this.updatedVersion = updatedVersion;
    }

    public void setFhirUri(String fhirUri) {
        this.fhirUri = fhirUri;
    }

    public void setMeasureToRefreshPath(String measureToRefreshPath) {
        this.measureToRefreshPath = measureToRefreshPath;
    }

    public void setResourcePaths(String resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    public void setLibraryOutputPath(String libraryOutputPath) {
        this.libraryOutputPath = libraryOutputPath;
    }

    public void setMeasureOutputPath(String measureOutputPath) {
        this.measureOutputPath = measureOutputPath;
    }

    public void setShouldApplySoftwareSystemStamp(String shouldApplySoftwareSystemStamp) {
        this.shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStamp;
    }

    public void setAddBundleTimestamp(String addBundleTimestamp) {
        this.addBundleTimestamp = addBundleTimestamp;
    }

    public void setVerboseMessaging(String verboseMessaging) {
        this.verboseMessaging = verboseMessaging;
    }

    public void setIncludePopulationLevelDataRequirements(String includePopulationLevelDataRequirements) {
        this.includePopulationLevelDataRequirements = includePopulationLevelDataRequirements;
    }
}
