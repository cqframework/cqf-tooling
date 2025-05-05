package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.List;

public class RefreshIGParameters {
    public String ini;
    public String rootDir;
    public String igPath;
    public IOUtils.Encoding outputEncoding;
    public Boolean skipPackages;
    public Boolean includeELM;
    public Boolean includeDependencies;
    public Boolean includeTerminology;
    public Boolean includePatientScenarios;
    public Boolean versioned;
    public Boolean shouldApplySoftwareSystemStamp;
    public Boolean addBundleTimestamp;
    public String fhirUri;
    public List<String> resourceDirs;
    public Boolean conformant;
    public String measureToRefreshPath;
    public String libraryPath;
    public String planDefinitionToRefreshPath;
    public String libraryOutputPath;
    public String measureOutputPath;
    public String planDefinitionOutputPath;
    public Boolean verboseMessaging;
    public String updatedVersion;
    public Boolean includePopulationLevelDataRequirements;
}
