package org.opencds.cqf.modelinfo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.utilities.CanonicalUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

public class Atlas {

    public Atlas() {
        resources = new HashMap<>();
        capabilityStatements = new HashMap<>();
        compartmentDefinitions = new HashMap<>();
        structureDefinitions = new HashMap<>();
        operationDefinitions = new HashMap<>();
        searchParameters = new HashMap<>();
        implementationGuides = new HashMap<>();
        codeSystems = new HashMap<>();
        valueSets = new HashMap<>();
        conceptMaps = new HashMap<>();
        namingSystems = new HashMap<>();
    }

    private Map<String, Resource> resources;
    public Map<String, Resource> getResources() {
        return resources;
    }

    private Map<String, CapabilityStatement> capabilityStatements;
    public Map<String, CapabilityStatement> getCapabilityStatements() {
        return capabilityStatements;
    }

    private Map<String, CompartmentDefinition> compartmentDefinitions;
    public Map<String, CompartmentDefinition> getCompartmentDefinitions() {
        return compartmentDefinitions;
    }

    private Map<String, StructureDefinition> structureDefinitions;
    public Map<String, StructureDefinition> getStructureDefinitions() {
        return structureDefinitions;
    }

    private Map<String, OperationDefinition> operationDefinitions;
    public Map<String, OperationDefinition> getOperationDefinitions() {
        return operationDefinitions;
    }

    private Map<String, SearchParameter> searchParameters;
    public Map<String, SearchParameter> getSearchParameters() {
        return searchParameters;
    }

    public SearchParameter resolveSearchParameter(String resourceTypeName, String searchParameterName) {
        Optional<SearchParameter> result =
            searchParameters.values().stream().filter(x -> x.getBase().stream().anyMatch(t -> t.toString().equals(resourceTypeName))
                && x.getName().equals(searchParameterName)).findFirst();

        return result.isPresent() ? result.get() : null;
    }

    private Map<String, ImplementationGuide> implementationGuides;
    public Map<String, ImplementationGuide> getImplementationGuides() {
        return implementationGuides;
    }

    private Map<String, CodeSystem> codeSystems;
    public Map<String, CodeSystem> getCodeSystems() {
        return codeSystems;
    }

    private Map<String, ValueSet> valueSets;
    public Map<String, ValueSet> getValueSets() {
        return valueSets;
    }

    private Map<String, ConceptMap> conceptMaps;
    public Map<String, ConceptMap> getConceptMaps() {
        return conceptMaps;
    }

    private Map<String, NamingSystem> namingSystems;
    public Map<String, NamingSystem> getNamingSystems() {
        return namingSystems;
    }

    public void loadPaths(String basePath, String resourcePaths) {
        String[] paths = resourcePaths.split(";");
        for (String path : paths) {
            System.out.println("Reading " + path + " Conformance Resources");
            readConformanceResourcesFromFolder(Paths.get(basePath, path).toString());
        }
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

    private void indexCapabilityStatement(CapabilityStatement capabilityStatement) {
        if (!resources.containsKey(capabilityStatement.getUrl())) {
            resources.put(capabilityStatement.getUrl(), capabilityStatement);
            String id = CanonicalUtils.getTail(capabilityStatement.getUrl());
            if (!capabilityStatements.containsKey(id)) {
                capabilityStatements.put(id, capabilityStatement);
            }
            else {
                System.out.println("Duplicate CapabilityStatement with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + capabilityStatement.getUrl());
        }
    }

    private void indexCompartmentDefinition(CompartmentDefinition compartmentDefinition) {
        if (!resources.containsKey(compartmentDefinition.getUrl())) {
            resources.put(compartmentDefinition.getUrl(), compartmentDefinition);
            String id = CanonicalUtils.getTail(compartmentDefinition.getUrl());
            if (!compartmentDefinitions.containsKey(id)) {
                compartmentDefinitions.put(id, compartmentDefinition);
            }
            else {
                System.out.println("Duplicate CompartmentDefinition with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + compartmentDefinition.getUrl());
        }
    }

    private void indexStructureDefinition(StructureDefinition structureDefinition) {
        if (!resources.containsKey(structureDefinition.getUrl())) {
            resources.put(structureDefinition.getUrl(), structureDefinition);
            String id = CanonicalUtils.getTail(structureDefinition.getUrl());
            if (!structureDefinitions.containsKey(id)) {
                structureDefinitions.put(id, structureDefinition);
            }
            else {
                System.out.println("Duplicate StructureDefinition with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + structureDefinition.getUrl());
        }
    }

    private void indexOperationDefinition(OperationDefinition operationDefinition) {
        if (!resources.containsKey(operationDefinition.getUrl())) {
            resources.put(operationDefinition.getUrl(), operationDefinition);
            String id = CanonicalUtils.getTail(operationDefinition.getUrl());
            if (!operationDefinitions.containsKey(id)) {
                operationDefinitions.put(id, operationDefinition);
            }
            else {
                System.out.println("Duplicate OperationDefinition with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + operationDefinition.getUrl());
        }
    }

    private void indexSearchParameter(SearchParameter searchParameter) {
        if (!resources.containsKey(searchParameter.getUrl())) {
            resources.put(searchParameter.getUrl(), searchParameter);
            String id = CanonicalUtils.getTail(searchParameter.getUrl());
            if (!searchParameters.containsKey(id)) {
                searchParameters.put(id, searchParameter);
            }
            else {
                System.out.println("Duplicate SearchParameter with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + searchParameter.getUrl());
        }
    }

    private void indexImplementationGuide(ImplementationGuide implementationGuide) {
        if (!resources.containsKey(implementationGuide.getUrl())) {
            resources.put(implementationGuide.getUrl(), implementationGuide);
            String id = CanonicalUtils.getTail(implementationGuide.getUrl());
            if (!implementationGuides.containsKey(id)) {
                implementationGuides.put(id, implementationGuide);
            }
            else {
                System.out.println("Duplicate ImplementationGuide with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + implementationGuide.getUrl());
        }
    }

    private void indexCodeSystem(CodeSystem codeSystem) {
        if (!resources.containsKey(codeSystem.getUrl())) {
            resources.put(codeSystem.getUrl(), codeSystem);
            String id = CanonicalUtils.getTail(codeSystem.getUrl());
            if (!codeSystems.containsKey(id)) {
                codeSystems.put(id, codeSystem);
            }
            else {
                System.out.println("Duplicate CodeSystem with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + codeSystem.getUrl());
        }
    }

    private void indexValueSet(ValueSet valueSet) {
        if (!resources.containsKey(valueSet.getUrl())) {
            resources.put(valueSet.getUrl(), valueSet);
            String id = CanonicalUtils.getTail(valueSet.getUrl());
            if (!valueSets.containsKey(id)) {
                valueSets.put(id, valueSet);
            }
            else {
                System.out.println("Duplicate ValueSet with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + valueSet.getUrl());
        }
    }

    private void indexConceptMap(ConceptMap conceptMap) {
        if (!resources.containsKey(conceptMap.getUrl())) {
            resources.put(conceptMap.getUrl(), conceptMap);
            String id = CanonicalUtils.getTail(conceptMap.getUrl());
            if (!conceptMaps.containsKey(id)) {
                conceptMaps.put(id, conceptMap);
            }
            else {
                System.out.println("Duplicate ConceptMap with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + conceptMap.getUrl());
        }
    }

    private void indexNamingSystem(NamingSystem namingSystem) {
        if (!resources.containsKey(namingSystem.getUrl())) {
            resources.put(namingSystem.getUrl(), namingSystem);
            String id = CanonicalUtils.getTail(namingSystem.getUrl());
            if (!namingSystems.containsKey(id)) {
                namingSystems.put(id, namingSystem);
            }
            else {
                System.out.println("Duplicate NamingSystem with id " + id);
            }
        }
        else {
            System.out.println("Duplicate url found for: " + namingSystem.getUrl());
        }
    }

    private void indexResource(IBaseResource resource) {
        if (resource instanceof CapabilityStatement) {
            indexCapabilityStatement((CapabilityStatement)resource);
        }
        else if (resource instanceof CompartmentDefinition) {
            indexCompartmentDefinition((CompartmentDefinition)resource);
        }
        else if (resource instanceof StructureDefinition) {
            indexStructureDefinition((StructureDefinition)resource);
        }
        else if (resource instanceof OperationDefinition) {
            indexOperationDefinition((OperationDefinition)resource);
        }
        else if (resource instanceof SearchParameter) {
            indexSearchParameter((SearchParameter)resource);
        }
        else if (resource instanceof ImplementationGuide) {
            indexImplementationGuide((ImplementationGuide)resource);
        }
        else if (resource instanceof CodeSystem) {
            indexCodeSystem((CodeSystem)resource);
        }
        else if (resource instanceof ValueSet) {
            indexValueSet((ValueSet)resource);
        }
        else if (resource instanceof ConceptMap) {
            indexConceptMap((ConceptMap)resource);
        }
        else if (resource instanceof NamingSystem) {
            indexNamingSystem((NamingSystem)resource);
        }
        else {
            System.out.println("Resource with id " + resource.getIdElement().toString() + " skipped");
        }
    }

    private void readConformanceResourcesFromFolder(String path) {
        Collection<File> files = getFiles(path);

        IParser parser = FhirContext.forR4().newJsonParser();

        List<StructureDefinition> objects = new ArrayList<StructureDefinition>();

        for (File f : files) {

            try {
                String content = Files.asCharSource(f, Charset.forName("UTF-8")).read();
                IBaseResource resource = parser.parseResource(content);

                if (resource instanceof Bundle) {
                    for (IBaseResource R : unrollBundles((Bundle)resource)) {
                        indexResource(R);
                    }
                }
                else {
                    indexResource(resource);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Collection<File> getFiles(String path) {
        File folder = new File(path);
        return FileUtils.listFiles(folder, new WildcardFileFilter("*.json"), null);
    }

    private List<IBaseResource> unrollBundles(Bundle bundle) {
        List<IBaseResource> resources = new ArrayList<IBaseResource>();
        if (bundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.hasResource()) {
                    Resource r = entry.getResource();
                    if (r.getResourceType() == ResourceType.Bundle) {
                        resources.addAll(unrollBundles((Bundle)r));
                    }
                    else {
                        resources.add(r);
                    }
                }
            }
        }

        return resources;
    }
}
