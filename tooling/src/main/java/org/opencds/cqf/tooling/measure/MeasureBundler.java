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
        //persist tests-* before group-* files and make a record of which files were tracked:
        List<String> persistedFiles = persistTestFilesWithPriority(bundleDestPath, libraryName, encoding, fhirContext, fhirUri);
        persistedFiles.addAll(persistEverythingElse(bundleDestPath, libraryName, encoding, fhirContext, fhirUri, persistedFiles));

        return persistedFiles.size();
    }

    private List<String> persistTestFilesWithPriority(String bundleDestPath, String libraryName, Encoding encoding, FhirContext fhirContext, String fhirUri) {
        List<String> persistedResources = new ArrayList<>();
        String filesLoc = bundleDestPath + File.separator + libraryName + "-files";
        File directory = new File(filesLoc);
        if (directory.exists()) {
            File[] filesInDir = directory.listFiles();
            if (!(filesInDir == null || filesInDir.length == 0)) {
                for (File file : filesInDir) {
                    if (file.getName().toLowerCase().startsWith("tests-")) {
                        try {
                            IBaseResource resource = IOUtils.readResource(file.getAbsolutePath(), fhirContext, true);
                            HttpClientUtils.post(fhirUri, resource, encoding, fhirContext, file.getAbsolutePath(), true);
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
                            HttpClientUtils.post(fhirUri, resource, encoding, fhirContext, file.getAbsolutePath(), false);
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
