package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.processor.AbstractBundler;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeasureBundler extends AbstractBundler {
    public static final String ResourcePrefix = "measure-";

    protected CopyOnWriteArrayList<Object> identifiers;

    public static String getId(String baseId) {
        return ResourcePrefix + baseId;
    }

    @Override
    protected String getSourcePath(FhirContext fhirContext, Map.Entry<String, IBaseResource> resourceEntry) {
        return IOUtils.getMeasurePathMap(fhirContext).get(resourceEntry.getKey());
    }

    @Override
    protected Map<String, IBaseResource> getResources(FhirContext fhirContext) {
        return IOUtils.getMeasures(fhirContext);
    }

    @Override
    protected String getResourceBundlerType() {
        return TYPE_MEASURE;
    }

    @Override
    protected Set<String> getPaths(FhirContext fhirContext) {
        return IOUtils.getMeasurePaths(fhirContext);
    }

}
