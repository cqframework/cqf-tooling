package org.opencds.cqf.modelinfo;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import com.google.common.io.Files;
import com.google.gson.*;


public class StructureDefinitionToModelInfo extends Operation {

    @Override
    public void execute(String[] args) {
        if (args.length > 2) {
            setOutputPath(args[2]);
        }
        else {
            setOutputPath("src/main/resources/org/opencds/cqf/modelinfo/output");
        }

        String inputPath = null;
        if (args.length > 1) {
            inputPath = args[1];
        }
        else {
            inputPath = "../FHIR-Spec";
        }

        List<StructureDefinition> resources = new ArrayList<StructureDefinition>();


        System.out.println("Reading 3.0.1 StructureDefinitions");
        resources.addAll(readStructureDefFromFolder(inputPath + "/3.0.1"));

        // System.out.println("Reading US-Core 1.0.1 StructureDefinitions");
        // resources.addAll(readJsonObjectFromFolder(inputPath + "/US-Core/1.0.1"));

        // System.out.println("Reading QI-Core 2.0.0 StructureDefinitions");
        // resources.addAll(readJsonObjectFromFolder(inputPath + "/QI-Core/2.0.0"));

        // System.out.println("Reading QI-Core 3.1.0 StructureDefinitions");
        // resources.addAll(readJsonObjectFromFolder(inputPath + "/QI-Core/3.1.0"));


        System.out.println("Indexing StructureDefinitions by Id");
        Map<String, StructureDefinition> resourcesById = indexResources(resources);


        System.out.println("Creating dependency graph for StructureDefinitions");
        createDependencyGraph(resourcesById);






        try {
            writeOutput("bubba.txt", "test");
        } catch (IOException e) {
            System.err.println("Encountered the following exception while creating file " + "bubba" + e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    private void writeOutput(String fileName, String content) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(content.getBytes());
            writer.flush();
        }
    }

    private void createDependencyGraph(Map<String, StructureDefinition> resourcesById) {
        Map<String, List<String>> resourceDependenciesById = new HashMap<String, List<String>>();
        for (StructureDefinition sd : resourcesById.values()) {
            List<String> dependencies = new ArrayList<String>();
            List<ElementDefinition> elements = null;
            if (sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE) {
                System.out.println(sd.getUrl() + " is primitive-type. No dependencies.");
                resourceDependenciesById.put(sd.getUrl(), dependencies);
                continue;
            }
            else if (sd.hasSnapshot()) {
                elements = sd.getSnapshot().getElement();
            }
            else {
                System.out.println(sd.getUrl() + " is not understood. Fix this.");
                continue;
            }
            
            for (ElementDefinition ed : elements) {
                for (ElementDefinition.TypeRefComponent trc : ed.getType())
                {
                    System.out.println(trc.getProfile());
                    
                }
            }
        }
    }

    private Map<String, StructureDefinition> indexResources(List<StructureDefinition> resources) {
        Map<String, StructureDefinition> resourcesById = new HashMap<String, StructureDefinition>();
        for (StructureDefinition sd : resources) {
            if (!resourcesById.containsKey(sd.getUrl())) {
                resourcesById.put(sd.getUrl(), sd);
            }
            else {
                System.out.println("Duplicate url found for: " + sd.getUrl());
            }
        }

        return resourcesById;
    }

    private List<StructureDefinition> readStructureDefFromFolder(String path) {
        Collection<File> files = getFiles(path);

        IParser parser = FhirContext.forDstu3().newJsonParser();

        List<StructureDefinition> objects = new ArrayList<StructureDefinition>();

        for (File f : files) {
            
            try {
                String content = Files.asCharSource(f, Charset.forName("UTF-8")).read();
                IBaseResource resource = parser.parseResource(content);

                if (resource instanceof StructureDefinition)
                {
                    objects.add((StructureDefinition)resource);
                }
                else if (resource instanceof Bundle) {
                    objects.addAll(unrollBundles((Bundle)resource));
                }
            }
            catch(IOException e) {
                
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
            for (Bundle.BundleEntryComponent entry : bundle.getEntry())
            {
                if (entry.hasResource()) {
                    Resource r = entry.getResource();
                    if (r.getResourceType() == ResourceType.StructureDefinition) {
                        resources.add((StructureDefinition)r);
                    }
                    else if (r.getResourceType() == ResourceType.Bundle) {
                        resources.addAll(unrollBundles((Bundle)r));
                    }
                }
            }
        }


        return resources;
    }


    public static void main(String[] args) {
        Operation op = new StructureDefinitionToModelInfo();
        op.execute(args);
    }
}
