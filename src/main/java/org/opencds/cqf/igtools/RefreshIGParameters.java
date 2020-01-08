package org.opencds.cqf.igtools;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.igtools.IGProcessor.IGVersion;

public class RefreshIGParameters {  
    public String igPath;
    public IGVersion igVersion;
    public Boolean includeELM;
    public Boolean includeDependencies;
    public Boolean includeTerminology;
    public Boolean includePatientScenarios;
    public Boolean versioned;
    public String fhirUri;
}