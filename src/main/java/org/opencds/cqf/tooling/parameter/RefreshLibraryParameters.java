package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class RefreshLibraryParameters {  
    public String igCanonicalBase;
    public String cqlContentPath;
    public FhirContext fhirContext;
    public Encoding encoding;
    public Boolean versioned;
    public String libraryPath;
}