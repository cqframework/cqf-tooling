package org.opencds.cqf.parameter;

import org.opencds.cqf.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class RefreshLibraryParameters {  
    private String igCanonicalBase;
    public String cqlContentPath;
    public FhirContext fhirContext;
    public Encoding encoding;
    public Boolean versioned;
    public String libraryPath;
}