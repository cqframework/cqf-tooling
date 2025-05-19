package org.opencds.cqf.tooling.packaging;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import org.opencds.cqf.tooling.packaging.r4.PackagePlanDefinition;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class PackagePlanDefinitions {

    public PackagePlanDefinitions(String igRoot, FhirContext fhirContext, boolean includeDependencies, boolean includeTerminology, boolean includeTests, String fhirServerUrl) {
        // This is expected to be called during refresh - safe to assume the PlanDefinition paths will be present
        var pdResourcePaths = IOUtils.getPlanDefinitionPaths(fhirContext);
        if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
            pdResourcePaths.forEach(
                    path -> new PackagePlanDefinition(igRoot, fhirContext, path, includeDependencies, includeTerminology, includeTests, fhirServerUrl)
                            .packageArtifact());
        } else {
            throw new UnsupportedOperationException("Package operation for PlanDefinition resources is not supported for FHIR version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

}
