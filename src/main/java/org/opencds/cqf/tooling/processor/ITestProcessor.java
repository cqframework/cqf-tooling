package org.opencds.cqf.tooling.processor;

import org.hl7.fhir.Parameters;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public interface ITestProcessor {
    Parameters executeTest(String testPath, String contentBundlePath, String fhirServer);
    Parameters executeTest(IBaseResource testBundle, IBaseResource contentBundle, String fhirServer);
}
