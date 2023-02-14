package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirVersionEnum;

public class MeasureTestParameters {
    public String contentPath;
    public String testPath;
    public String fhirServer;
    public FhirVersionEnum fhirVersion;
    public Encoding encoding;
}