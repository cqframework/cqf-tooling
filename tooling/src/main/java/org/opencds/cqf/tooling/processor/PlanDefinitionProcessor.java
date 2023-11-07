package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.common.ThreadUtils;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PlanDefinitionProcessor extends AbstractResourceProcessor{
    public PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
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
    protected String getResourceProcessorType() {
        return TYPE_PLAN_DEFINITION;
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getPlanDefinitionPaths(fhirContext);
    }
}