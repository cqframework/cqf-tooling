package org.opencds.cqf.tooling.parameter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;

public class FileFhirPlatformParameters {
  public FhirContext fhirContext;
  public EncodingEnum encoding;
  public String resourceDir;
}
