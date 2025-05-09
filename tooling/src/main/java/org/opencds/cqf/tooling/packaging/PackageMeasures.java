package org.opencds.cqf.tooling.packaging;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.opencds.cqf.tooling.packaging.r4.PackageMeasure;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class PackageMeasures {

    public PackageMeasures(String igRoot, FhirContext fhirContext, boolean includeDependencies, boolean includeTerminology, boolean includeTests, String fhirServerUrl) {
        // This is expected to be called during refresh - safe to assume the Measure paths will be present
        var measureResourcePaths = IOUtils.getMeasurePaths(fhirContext);
        if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
            measureResourcePaths.forEach(
                    path -> new PackageMeasure(igRoot, fhirContext, path, includeDependencies, includeTerminology, includeTests, fhirServerUrl)
                            .packageArtifact());
        } else {
            throw new UnsupportedOperationException("Package operation for Measure resources is not supported for FHIR version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

}
