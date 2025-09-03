package org.opencds.cqf.tooling.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.processor.AbstractBundler;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.Map;
import java.util.Set;

public class PlanDefinitionBundler extends AbstractBundler {

    public PlanDefinitionBundler(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        setLibraryProcessor(libraryProcessor);
        setCDSHooksProcessor(cdsHooksProcessor);
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getPlanDefinitionPaths(fhirContext);
    }

    @Override
    protected String getSourcePath(FhirContext fhirContext, Map.Entry<String, IBaseResource> resourceEntry) {
        return IOUtils.getPlanDefinitionPathMap(fhirContext).get(resourceEntry.getKey());
    }

    @Override
    protected Map<String, IBaseResource> getResources(FhirContext fhirContext) {
        return IOUtils.getPlanDefinitions(fhirContext);
    }

    @Override
    protected String getResourceBundlerType() {
        return TYPE_PLAN_DEFINITION;
    }

    @Override
    protected int persistFilesFolder(String bundleDestPath, String libraryName, IOUtils.Encoding encoding, FhirContext fhirContext, String fhirUri) {
        return 0;
    }
}
