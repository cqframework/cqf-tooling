package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.processor.AbstractBundler;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeasureBundler extends AbstractBundler {
    public static final String ResourcePrefix = "measure-";
    public static final String LIBRARY_DEPS = "library-deps-";
    public static final String VALUESETS = "valuesets-";
    public static final String TESTS = "tests-";
    public static final String GROUP = "group-";

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

    //so far only the Measure Bundle process needs to persist extra files:
    /**
     * This method will group files of similar naming to attempt to post the resources in an order that the fhir endpoint
     * can understand. For instance, attempting to post a Group file before the Patient resource is posted can result
     * in a reference error.
     *
     * @param bundleDestPath
     * @param libraryName
     * @param encoding
     * @param fhirContext
     * @param fhirUri
     * @return
     */
    @Override
    protected int persistFilesFolder(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri) {
//        List<String> persistedResources = new ArrayList<>();
//        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
//        File directory = new File(filesLoc);
//        if (directory.exists()) {
//            File[] filesInDir = directory.listFiles();
//            if (!(filesInDir == null || filesInDir.length == 0)) {
//                for (File file : filesInDir) {
//
//                    if (!file.getName().toLowerCase().endsWith(".json") && !file.getName().toLowerCase().endsWith(".xml")){
//                        continue;
//                    }
//
//                    try {
//                        IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext, true);
//                        HttpClientUtils.sendToServer(fhirUri, resource, encoding, fhirContext, file.getAbsolutePath());
//                        persistedResources.add(file.getAbsolutePath());
//                    } catch (Exception e) {
//                        //resource is likely not IBaseResource
//                        logger.error("MeasureBundler.persistFilesFolder", e);
//                    }
//                }
//            }
//        }
//        return persistedResources.size();
        return 0;
    }
}
