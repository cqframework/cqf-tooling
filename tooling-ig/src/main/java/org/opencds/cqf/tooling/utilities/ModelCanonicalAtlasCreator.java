package org.opencds.cqf.tooling.utilities;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.InMemoryCanonicalResourceProvider;
import org.opencds.cqf.tooling.modelinfo.Atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelCanonicalAtlasCreator {
    private static List<ValueSet> valueSets;
    private static List<CodeSystem> codeSystems;
    private static List<StructureDefinition> structureDefinitions;
    private static Map<String, ConceptMap> conceptMaps;

    private ModelCanonicalAtlasCreator() {}

    public static CanonicalResourceAtlas createMainCanonicalAtlas (String resourcePaths, String modelName, String modelVersion, String inputPath) {
        String mainResourcePath = getModelResourcePath (resourcePaths, modelName);
        setSystems (inputPath, mainResourcePath);
        return  getCanonicalAtlas ();
    }

    public static CanonicalResourceAtlas createDependenciesCanonicalAtlas (String resourcePaths, String modelName, String modelVersion, String inputPath) {
        String mainResourcePath = getDependenciesResourcePath (resourcePaths, modelName);
        setSystems (inputPath, mainResourcePath);
        return  getCanonicalAtlas ();
    }
    private static void setSystems (String inputPath, String resourcePath) {
        Atlas atlas = new Atlas ();
        atlas.loadPaths (inputPath, resourcePath);
        codeSystems = new ArrayList<> ();
        atlas.getCodeSystems().forEach((key, codeSystem) -> codeSystems.add(codeSystem));
        conceptMaps = atlas.getConceptMaps();
        valueSets = new ArrayList<>();
        atlas.getValueSets().forEach((key, valueSet) -> valueSets.add(valueSet));
        structureDefinitions = new ArrayList<>();
        atlas.getStructureDefinitions().forEach((key, structureDefinition) -> structureDefinitions.add(structureDefinition));
    }

    private static CanonicalResourceAtlas getCanonicalAtlas(){
        return  new CanonicalResourceAtlas()
                .setValueSets(new InMemoryCanonicalResourceProvider<>(valueSets))
                .setCodeSystems(new InMemoryCanonicalResourceProvider<>(codeSystems))
                .setStructureDefinitions(new InMemoryCanonicalResourceProvider<>(structureDefinitions))
                .setConceptMaps(new InMemoryCanonicalResourceProvider<>(conceptMaps.values()));
    }
    private static String getModelResourcePath(String resourcePaths, String modelName){
        String[] paths = resourcePaths.split(";");
        for (String path : paths){
            if(path.replace("-", "").contains(modelName)){
                return path;
            }
        }
        return null;
    }

    private static String getDependenciesResourcePath(String resourcePaths, String modelName){
        String[] paths = resourcePaths.split(";");
        StringBuilder pathsWithoutModel = new StringBuilder();
        for (String path : paths){
            if(!path.contains(modelName)){
                pathsWithoutModel.append(path).append(";");
            }
        }
        return pathsWithoutModel.toString();
    }
}
