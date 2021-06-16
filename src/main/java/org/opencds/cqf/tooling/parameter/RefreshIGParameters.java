package org.opencds.cqf.tooling.parameter;

import java.util.ArrayList;

import org.opencds.cqf.tooling.utilities.IOUtils;
/**
 * @author Adam Stevenson
 */
public class RefreshIGParameters {
    public String ini;
    public String rootDir;
    public String igPath;
    public IOUtils.Encoding outputEncoding;
    public Boolean includeELM;
    public Boolean includeDependencies;
    public Boolean includeTerminology;
    public Boolean includePatientScenarios;
    public Boolean versioned;
    public String fhirUri;
    public ArrayList<String> resourceDirs;
    public Boolean conformant;
    public String measureToRefreshPath;
}