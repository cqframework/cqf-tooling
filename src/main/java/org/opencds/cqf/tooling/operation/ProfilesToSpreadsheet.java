package org.opencds.cqf.tooling.operation;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionBindingObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementBindingVisitor;
import org.opencds.cqf.tooling.modelinfo.Atlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    }

    private void createAtlas(){
        String inputPath = "/Users/bryantaustin/Projects/FHIR-Spec";
        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);
        this.codeSystems = new ArrayList<>();
        atlas.getCodeSystems().forEach((key, codeSystem)->{
            this.codeSystems.add(codeSystem);
        });
        this.conceptMaps = atlas.getConceptMaps();
        this.valueSets = new ArrayList<>();
        atlas.getValueSets().forEach((key, valueSet)->{
            this.valueSets.add(valueSet);
        });

        CanonicalResourceAtlas canonicalResourceAtlas = getAtlas();

        StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas);
        Map<String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
        Map<String, StructureDefinition>scMap = atlas.getStructureDefinitions();
        scMap.forEach((key, sd)->{
            Map<String, StructureDefinitionBindingObject> newBindingObjects = sdbv.visitStructureDefinition(sd);
            if(null != newBindingObjects){
                bindingObjects.putAll(newBindingObjects);
            }
        });
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
