package org.opencds.cqf.modelinfo;

import javax.xml.bind.Marshaller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.dstu3.model.StructureDefinition.TypeDerivationRule;

import java.util.Map;

import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;

import org.opencds.cqf.Operation;

public class StructureDefinitionToModelInfo extends Operation {
    @Override
    public void execute(String[] args) {
        String inputPath = "../FHIR-Spec";
        if (args.length > 1) {
            inputPath = args[1];
        }

        if (args.length > 2) {
            setOutputPath(args[2]);
        }

        String resourcePaths = "/3.0.1";
        if (args.length > 3) {
            resourcePaths = args[3];
        }

        // TODO : Can we autodetect this from the structure defintions?
        String modelName = "FHIR";
        if (args.length > 4) {
            modelName = args[4];
        }

        String modelVersion = "3.0.1";
        if (args.length > 5) {
            modelName = args[5];
        }

        ResourceLoader loader = new ResourceLoader();
        Map<String, StructureDefinition> structureDefinitions = loader.loadPaths(inputPath, resourcePaths);

        Configuration config = Configuration.DefaultConfiguration;

        ClassInfoBuilder classInfoBuilder = new ClassInfoBuilder(Configuration.DefaultConfiguration,
                structureDefinitions);

        // TODO: This build sequence is model specific. Need to refactor that.
        System.out.println("Building Primitives");
        classInfoBuilder.buildFor(modelName, (x -> x.getKind() == StructureDefinitionKind.PRIMITIVETYPE));

        System.out.println("Building ComplexTypes");
        classInfoBuilder.buildFor(modelName,
                (x -> x.getKind() == StructureDefinitionKind.COMPLEXTYPE && (x.getBaseDefinition() == null
                        || !x.getBaseDefinition().equals("http://hl7.org/fhir/StructureDefinition/Extension"))));

        System.out.println("Building Resources");
        classInfoBuilder.buildFor(modelName, (x -> x.getKind() == StructureDefinitionKind.RESOURCE
                && (!x.hasDerivation() || x.getDerivation() == TypeDerivationRule.SPECIALIZATION)));

        ModelInfoBuilder modelInfoBuilder = new ModelInfoBuilder();
        ModelInfo mi = modelInfoBuilder.build(modelName, modelVersion, config,
                classInfoBuilder.getTypeInfos().values());

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ModelInfo.class, TypeInfo.class, ClassInfo.class,
                    ConversionInfo.class);

            JAXBElement<ModelInfo> jbe = new JAXBElement<ModelInfo>(
                    new QName("urn:hl7-org:elm-modelinfo:r1", "modelInfo", "ns4"), ModelInfo.class, null, mi);

            // Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Print XML String to Console
            StringWriter sw = new StringWriter();

            // Write XML to StringWriter
            jaxbMarshaller.marshal(jbe, sw);

            writeOutput("output.xml", sw.toString());
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
