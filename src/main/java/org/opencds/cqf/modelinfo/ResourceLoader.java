package org.opencds.cqf.modelinfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ResourceLoader {

    public Map<String, StructureDefinition> loadPaths(String basePath, String resourcePaths) {

        List<StructureDefinition> resources = new ArrayList<StructureDefinition>();

        String[] paths = resourcePaths.split(";");
        for (String path : paths) {
            System.out.println("Reading " + path + " StructureDefinitions");
            resources.addAll(this.readStructureDefFromFolder(Paths.get(basePath, path).toString()));
        }

        System.out.println("Indexing StructureDefinitions by Id");
        return this.indexResources(resources);
    }

    private String urlToId(String url) {
        int index = url.lastIndexOf("/StructureDefinition/");
        if (index > -1) {
            return url.substring(index + 21, url.length());
        }

        return url;

    }

    private Map<String, StructureDefinition> indexResources(List<StructureDefinition> resources) {
        Map<String, StructureDefinition> resourcesById = new HashMap<String, StructureDefinition>();
        for (StructureDefinition sd : resources) {
            String id = urlToId(sd.getUrl());
            if (!resourcesById.containsKey(id)) {
                resourcesById.put(id, sd);
            } else {
                System.out.println("Duplicate url found for: " + sd.getUrl());
            }
        }

        return resourcesById;
    }

    private List<StructureDefinition> readStructureDefFromFolder(String path) {
        Collection<File> files = getFiles(path);

        IParser parser = FhirContext.forR4().newJsonParser();

        List<StructureDefinition> objects = new ArrayList<StructureDefinition>();

        for (File f : files) {

            try {
                String content = Files.asCharSource(f, Charset.forName("UTF-8")).read();
                IBaseResource resource = parser.parseResource(content);

                if (resource instanceof StructureDefinition) {
                    objects.add((StructureDefinition) resource);
                } else if (resource instanceof Bundle) {
                    objects.addAll(unrollBundles((Bundle) resource));
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        return objects;
    }

    private Collection<File> getFiles(String path) {
        File folder = new File(path);
        return FileUtils.listFiles(folder, new WildcardFileFilter("*.json"), null);
    }

    private List<StructureDefinition> unrollBundles(Bundle bundle) {
        List<StructureDefinition> resources = new ArrayList<StructureDefinition>();
        if (bundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.hasResource()) {
                    Resource r = entry.getResource();
                    if (r.getResourceType() == ResourceType.StructureDefinition) {
                        resources.add((StructureDefinition) r);
                    } else if (r.getResourceType() == ResourceType.Bundle) {
                        resources.addAll(unrollBundles((Bundle) r));
                    }
                }
            }
        }

        return resources;
    }

}