package org.opencds.cqf.tooling.fhir.api;

import org.opencds.cqf.tooling.parameter.FileFhirPlatformParameters;

public interface FileFhirPlatformFactory {
  FileFhirPlatform create(FileFhirPlatformParameters fileFhirPlatformParameters);
}
