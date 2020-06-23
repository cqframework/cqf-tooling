package org.opencds.cqf.parameter;

import java.util.ArrayList;

import org.opencds.cqf.processor.IGProcessor.IGVersion;
import org.opencds.cqf.utilities.IOUtils;

public class RefreshIGParameters {
    public String igResourcePath;
    public String igPath;
    public IGVersion igVersion;
    public IOUtils.Encoding outputEncoding;
    public Boolean includeELM;
    public Boolean includeDependencies;
    public Boolean includeTerminology;
    public Boolean includePatientScenarios;
    public Boolean versioned;
    public Boolean cdsHooksIg;
    public String fhirUri;
    public ArrayList<String> resourceDirs;
    public Boolean conformant;
    public String measureToRefreshPath;
}