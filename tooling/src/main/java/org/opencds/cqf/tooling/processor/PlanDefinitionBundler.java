package org.opencds.cqf.tooling.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public class PlanDefinitionBundler extends AbstractBundler {

    @SuppressWarnings("this-escape")
    public PlanDefinitionBundler(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
        setLibraryProcessor(libraryProcessor);
        setCDSHooksProcessor(cdsHooksProcessor);
    }

    //abstract methods to override:
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
        //do nothing
        return 0;
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getPlanDefinitionPaths(fhirContext);
    }

}