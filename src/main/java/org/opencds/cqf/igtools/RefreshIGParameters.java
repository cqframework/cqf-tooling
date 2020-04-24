package org.opencds.cqf.igtools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.igtools.IGProcessor.IGVersion;
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
    public String fhirUri;
    public ArrayList<String> resourceDirs;
    public Boolean conformant;
}