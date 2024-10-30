package org.opencds.cqf.tooling.parameter;

import java.util.ArrayList;

import org.opencds.cqf.tooling.utilities.IOUtils;

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
    public ArrayList<String> resourceDirs;
    public Boolean conformant;
    public String measureToRefreshPath;
    public String libraryPath;
    public String libraryOutputPath;
    public String measureOutputPath;
    public Boolean verboseMessaging;
    public String updatedVersion;
    public Boolean includePopulationLevelDataRequirements;
}
