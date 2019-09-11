package org.opencds.cqf.modelinfo;

import javax.xml.bind.Marshaller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.hl7.fhir.dstu3.model.StructureDefinition;

import java.util.Map;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;

import org.opencds.cqf.Operation;
import org.opencds.cqf.modelinfo.fhir.FHIRClassInfoBuilder;
import org.opencds.cqf.modelinfo.fhir.FHIRModelInfoBuilder;
import org.opencds.cqf.modelinfo.quick.QuickClassInfoBuilder;
import org.opencds.cqf.modelinfo.quick.QuickModelInfoBuilder;

public class StructureDefinitionToModelInfo extends Operation {
    @Override
    public void execute(String[] args) {
        String inputPath = "../FHIR-Spec";
        if (args.length > 1) {
            inputPath = args[0];
        }

        if (args.length > 2) {
            setOutputPath(args[1]);
        }
        else {
            setOutputPath("../cqf-tooling/src/main/resources/org/opencds/cqf/model-info");
        }

        String resourcePaths = "/3.0.1;/US-Core/1.0.1";
        if (args.length > 3) {
            resourcePaths = args[2];
        }

        // TODO : Can we autodetect this from the structure defintions?
        String modelName = "FHIR";
        if (args.length > 4) {
            modelName = args[3];
        }

        String modelVersion = "3.0.1";
        if (args.length >= 5) {
            modelVersion = args[4];
        }        

        ResourceLoader loader = new ResourceLoader();
        Map<String, StructureDefinition> structureDefinitions = loader.loadPaths(inputPath, resourcePaths);

        ModelInfoBuilder miBuilder;
        ModelInfo mi;

        if(modelName.matches("FHIR"))
        {
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(structureDefinitions);
            Map<String, TypeInfo> typeInfos = ciBuilder.build();

            String fhirHelpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new FHIRModelInfoBuilder(modelVersion, typeInfos.values(), fhirHelpersPath);
            mi = miBuilder.build();
        }
        else if(modelName.matches("QUICK"))
        {
            ClassInfoBuilder ciBuilder = new QuickClassInfoBuilder(structureDefinitions);
            Map<String, TypeInfo> typeInfos = ciBuilder.build();

            miBuilder = new QuickModelInfoBuilder(modelVersion, typeInfos.values());
            mi = miBuilder.build();
        }
        else
        {
            //should blowup
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(structureDefinitions);
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            miBuilder = new ModelInfoBuilder(typeInfos.values());
            mi = miBuilder.build();
        }


        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ModelInfo.class, TypeInfo.class, ClassInfo.class,
                    ConversionInfo.class);

            JAXBElement<ModelInfo> jbe = new JAXBElement<ModelInfo>(
                    new QName("urn:hl7-org:elm-modelinfo:r1", "modelInfo"), ModelInfo.class, null, mi);

            // Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Print XML String to Console
            StringWriter sw = new StringWriter();

            //Write XML to StringWriter

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(jbe, sw);

            String fileName = modelName + "-" + "modelinfo" + "-" + modelVersion + ".xml";
            writeOutput(fileName, sw.toString());
        } catch (Exception e) {
            System.err.println("error" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeOutput(String fileName, String content) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(content.getBytes());
            writer.flush();
        }
    }
    
    public static void main(String[] args) {
        Operation op = new StructureDefinitionToModelInfo();
        op.execute(args);
    }
}
