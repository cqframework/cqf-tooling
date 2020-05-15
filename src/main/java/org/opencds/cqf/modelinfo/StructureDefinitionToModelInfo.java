package org.opencds.cqf.modelinfo;

import javax.xml.bind.Marshaller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.hl7.elm_modelinfo.r1.*;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.nio.file.Paths;
import java.util.Map;

import org.opencds.cqf.Operation;
import org.opencds.cqf.modelinfo.fhir.FHIRClassInfoBuilder;
import org.opencds.cqf.modelinfo.fhir.FHIRModelInfoBuilder;
import org.opencds.cqf.modelinfo.qicore.QICoreClassInfoBuilder;
import org.opencds.cqf.modelinfo.qicore.QICoreModelInfoBuilder;
import org.opencds.cqf.modelinfo.quick.QuickClassInfoBuilder;
import org.opencds.cqf.modelinfo.quick.QuickModelInfoBuilder;
import org.opencds.cqf.modelinfo.uscore.USCoreClassInfoBuilder;
import org.opencds.cqf.modelinfo.uscore.USCoreModelInfoBuilder;

public class StructureDefinitionToModelInfo extends Operation {
    /*
        resourcePaths: Semi-colon delimited list of paths to directories containing the resource definition files
            This directory should contain the unzipped contents of the definitions.json.zip or definitions.xml.zip files
                (i.e. all conformance resources published as part of the specification or ig)

        Arguments for producing FHIR Model Info
            -resourcePaths="4.0.1"
            -modelName="FHIR"
            -modelVersion="4.0.1"

        Arguments for producing USCore 3.0.0 Model Info
            -resourcePaths="4.0.0;US-Core/3.0.0
            -modelName="USCore"
            -modelVersion="3.0.0

        Arguments for producing QICore 3.3.0 Model Info
            -resourcePaths="4.0.0;US-Core/3.0.0;QI-Core/3.3.0"
            -modelName="QICore"
            -modelVersion="3.3.0"

        Arguments for producing USCore 3.1.0 Model Info
            -resourcePaths="4.0.1;US-Core/3.1.0"
            -modelName="USCore"
            -modelVersion="3.1.0"

        Arguments for producing QICore 4.0.0 Model Info
            -resourcePaths="4.0.1;US-Core/3.1.0;QI-Core/4.0.0"
            -modelName="QICore"
            -modelVersion="4.0.0"

     */
    @Override
    public void execute(String[] args) {
        String inputPath = Paths.get("..", "FHIR-Spec").toString();
        if (args.length > 1) {
            inputPath = args[1];
        }

        if (args.length > 2) {
            setOutputPath(args[2]);
        }
        else {
            setOutputPath("../cqf-tooling/src/main/resources/org/opencds/cqf/modelinfo");
        }

        String resourcePaths = "4.0.1";
        //String resourcePaths = "4.0.1;US-Core/3.1.0";
        //String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.0.0";
        if (args.length > 3) {
            resourcePaths = args[3];
        }

        // TODO : Can we autodetect this from the structure defintions?
        // Yes, would need to be an extension definition on the ImplementationGuide...
        String modelName = "FHIR";
        //String modelName = "USCore";
        //String modelName = "QICore";
        //String modelName = "QUICK";
        if (args.length > 4) {
            modelName = args[4];
        }
        String modelVersion = "4.0.1";
        //String modelVersion = "3.1.0";
        //String modelVersion = "4.0.0";
        //String modelVersion = "3.3.0";
        if (args.length > 5) {
            modelVersion = args[5];
        }        

        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);

        ModelInfoBuilder miBuilder;
        ModelInfo mi;

        if (modelName.equals("FHIR")) {
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(atlas.getStructureDefinitions());
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String fhirHelpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new FHIRModelInfoBuilder(modelVersion, typeInfos, atlas, fhirHelpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("USCore")) {
            ClassInfoBuilder ciBuilder = new USCoreClassInfoBuilder(atlas.getStructureDefinitions());
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String helpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new USCoreModelInfoBuilder(modelVersion, typeInfos, atlas, helpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("QICore")) {
            ClassInfoBuilder ciBuilder = new QICoreClassInfoBuilder(atlas.getStructureDefinitions());
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String helpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new QICoreModelInfoBuilder(modelVersion, typeInfos, atlas, helpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("QUICK")) {
            ClassInfoBuilder ciBuilder = new QuickClassInfoBuilder(atlas.getStructureDefinitions());
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            miBuilder = new QuickModelInfoBuilder(modelVersion, typeInfos.values());
            mi = miBuilder.build();
        }
        else {
            //should blowup
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(atlas.getStructureDefinitions());
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

            String fileName = modelName.toLowerCase() + "-" + "modelinfo" + "-" + modelVersion + ".xml";
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
