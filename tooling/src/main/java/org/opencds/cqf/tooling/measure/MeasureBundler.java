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
    @Override
    protected int persistFilesFolder(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri) {
        //files must be persisted in order, starting with valuesets:
        List<String> persistedFiles = new ArrayList<>(persistFilesStartsWith("library-deps-", bundleDestPath, libraryName, encoding, fhirContext, fhirUri, HttpClientUtils.HttpPOSTResourceType.LIBRARY_DEPS));
        //library:
        persistedFiles.addAll(persistFilesStartsWith("valuesets-", bundleDestPath, libraryName, encoding, fhirContext, fhirUri, HttpClientUtils.HttpPOSTResourceType.VALUESETS));
        //test bundles:
        persistedFiles.addAll(persistFilesStartsWith("tests-", bundleDestPath, libraryName, encoding, fhirContext, fhirUri, HttpClientUtils.HttpPOSTResourceType.TESTS));
        //Group files:
        persistedFiles.addAll(persistFilesStartsWith("Group-", bundleDestPath, libraryName, encoding, fhirContext, fhirUri, HttpClientUtils.HttpPOSTResourceType.GROUP));
        //everything else:
        persistedFiles.addAll(persistEverythingElse(bundleDestPath, libraryName, encoding, fhirContext, fhirUri, persistedFiles));
        return persistedFiles.size();
    }


    /**
     * This method will group files of similar naming to attempt to post the resources in an order that the fhir endpoint
     * can understand. For instance, attempting to post a Group file before the Patient resource is posted can result
     * in a reference error. To avoid running refresh twice, this ordering is necessary at POST.
     *
     * @param pattern
     * @param bundleDestPath
     * @param libraryName
     * @param encoding
     * @param fhirContext
     * @param fhirUri
     * @param rank
     * @return
     */
    private List<String> persistFilesStartsWith(String pattern, String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri, HttpClientUtils.HttpPOSTResourceType rank) {
        List<String> persistedResources = new ArrayList<>();
        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
        File directory = new File(filesLoc);
        if (directory.exists()) {
            File[] filesInDir = directory.listFiles();
            if (!(filesInDir == null || filesInDir.length == 0)) {
                for (File file : filesInDir) {
                    if (file.getName().toLowerCase().startsWith(pattern)) {
                        try {
                            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext, true);
                            HttpClientUtils.post(fhirUri, resource, encoding, fhirContext, file.getAbsolutePath(), rank);
                            persistedResources.add(file.getAbsolutePath());
                        } catch (Exception e) {
                            //resource is likely not IBaseResource
                            logger.error("MeasureBundler.persistTestFilesWithPriority", e);
                        }
                    }
                }
            }
        }
        return persistedResources;
    }

    /**
     * Persists any files remaining after cycling through types to post in specific order
     *
     * @param bundleDestPath
     * @param libraryName
     * @param encoding
     * @param fhirContext
     * @param fhirUri
     * @param alreadyPersisted
     * @return
     */
    private List<String> persistEverythingElse(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri, List<String> alreadyPersisted) {
        List<String> persistedResources = new ArrayList<>();
        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
        File directory = new File(filesLoc);
        if (directory.exists()) {

            File[] filesInDir = directory.listFiles();

            if (!(filesInDir == null || filesInDir.length == 0)) {
                for (File file : filesInDir) {
                    //don't post what has already been processed
                    if (alreadyPersisted.contains(file.getAbsolutePath())) {
                        continue;
                    }
                    if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".xml")) {
                        try {
                            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext, true);
                            HttpClientUtils.post(fhirUri, resource, encoding, fhirContext, file.getAbsolutePath());
                            persistedResources.add(file.getAbsolutePath());
                        } catch (Exception e) {
                            //resource is likely not IBaseResource
                            logger.error("persistEverythingElse", e);
                        }
                    }
                }
            }
        }
        return persistedResources;
    }
}
