package org.opencds.cqf.tooling.operation;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.InMemoryCanonicalResourceProvider;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionBindingObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementBindingVisitor;
import org.opencds.cqf.tooling.modelinfo.Atlas;

import java.util.*;

public class ProfilesToSpreadsheet extends Operation {
    private String inputPath;
    private String resourcePaths;
    private String modelName;
    private String modelVersion;
    private CanonicalResourceAtlas canonicalResourceAtlas;

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-ProfilesToSpreadsheet"))
                continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "inputpath":
                case "ip":
                    inputPath = value;
                    break;
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "resourcePaths":
                case "rp=":
                    resourcePaths = value;
                    break;
                case "modelName":
                case "mn":
                    modelName = value;
                    break;
                case "modelVersion":
                case "mv":
                    modelVersion = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        if (!isParameterListComplete()) {
            return;
        }
        createAtlas();
        if (null != canonicalResourceAtlas) {
            Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
            bindingObjects = getBindingObjects();
            if(null != bindingObjects && !bindingObjects.isEmpty()){
                //write these puppies out to the spreadsheet
            }
        }


    }

    private Map<String, StructureDefinitionBindingObject> getBindingObjects() {
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas);
        Iterable structureDefinitions = canonicalResourceAtlas.getStructureDefinitions().get();
        structureDefinitions.forEach((structDefn) -> {
            StructureDefinition sd = (StructureDefinition) structDefn;
            Map<String, StructureDefinitionBindingObject> newBindingObjects = sdbv.visitStructureDefinition(sd);
            if (null != newBindingObjects) {
                bindingObjects.putAll(newBindingObjects);
            }
        });
        return bindingObjects;
    }

    private CanonicalResourceAtlas createAtlas() {
        List<ValueSet> valueSets = new ArrayList<>();
        List<CodeSystem> codeSystems = new ArrayList<>();
        List<StructureDefinition> structureDefinitions = new ArrayList<>();
        Map<String, ConceptMap> conceptMaps;

        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);
        List<StructureDefinition> finalStructureDefinitions = structureDefinitions;
        atlas.getStructureDefinitions().forEach((key, structureDefinition) -> {
            finalStructureDefinitions.add(structureDefinition);
        });

        List<CodeSystem> finalCodeSystems = codeSystems;
        atlas.getCodeSystems().forEach((key, codeSystem) -> {
            finalCodeSystems.add(codeSystem);
        });
        conceptMaps = atlas.getConceptMaps();
        List<ValueSet> finalValueSets = valueSets;
        atlas.getValueSets().forEach((key, valueSet) -> {
            finalValueSets.add(valueSet);
        });
        return new CanonicalResourceAtlas()
                .setStructureDefinitions(new InMemoryCanonicalResourceProvider<StructureDefinition>(finalStructureDefinitions))
                .setValueSets(new InMemoryCanonicalResourceProvider<>(finalValueSets))
                .setConceptMaps(new InMemoryCanonicalResourceProvider<ConceptMap>(conceptMaps.values()))
                .setCodeSystems(new InMemoryCanonicalResourceProvider<>(finalCodeSystems));
    }

    private boolean isParameterListComplete() {
        if (null == inputPath || inputPath.length() < 1 ||
                null == resourcePaths || resourcePaths.length() < 1 ||
                null == modelName || modelName.length() < 1 ||
                null == modelVersion || modelName.length() < 1) {
            System.out.println("These parameters are required: ");
            System.out.println("-modelName/-mn");
            System.out.println("-outputpath/-op");
            System.out.println("-modelVersion/-mv");
            System.out.println("-resourcePaths/-rp");
            return false;
        }
        return true;
    }
}
