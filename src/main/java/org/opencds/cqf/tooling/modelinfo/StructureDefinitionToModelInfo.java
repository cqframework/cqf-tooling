package org.opencds.cqf.tooling.modelinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Map;

// import javax.xml.bind.JAXBContext;
// import javax.xml.bind.JAXBElement;
// import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.ConversionInfo;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.modelinfo.fhir.FHIRClassInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.fhir.FHIRModelInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.qicore.QICoreClassInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.qicore.QICoreModelInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.quick.QuickClassInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.quick.QuickModelInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.uscore.USCoreClassInfoBuilder;
import org.opencds.cqf.tooling.modelinfo.uscore.USCoreModelInfoBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
/**
 * @author Adam Stevenson
 */
public class StructureDefinitionToModelInfo extends Operation {

    private String inputPath;
    private String resourcePaths;
    private String modelName;
    private String modelVersion;
    private boolean useCQLPrimitives = false;
    private boolean includeMetadata = true;

    /*
    // NOTE: This documentation is present in the Main.java class for the tooling as well, keep these in sync

        - command: mvn exec:java -Dexec.args="
          [-GenerateMIs]
          [-inputPath | -ip]
          [-resourcePaths | -rp]
          [-modelName | -mn]
          [-modelVersion | -mv]
          (-useCqlPrimitives | ucp)
          (-includeMetadata | -im)
          (-outputpath | -op)
        "

        Examples:
          // Build the base pure-FHIR model
          mvn exec:java -Dexec.args"-GenerateMIs -ip=C:\Users\Bryn\Documents\Src\HL7\FHIR-Spec -rp=4.0.1 -mn=FHIR -mv=4.0.1 -im=true"

          // Build the base simple-FHIR model
          mvn exec:java -Dexec.args"-GenerateMIs -ip=C:\Users\Bryn\Documents\Src\HL7\FHIR-Spec -rp=4.0.1 -mn=FHIR -mv=4.0.1-S -ucp=true"

        inputPath: Path to the folder containing spec directories
            If not specified, defaults to a peer directory named FHIR-Spec

        resourcePaths: Semi-colon delimited list of paths (absolute, or relative to inputPath above) to directories containing the resource definition files
            The directories should contain the unzipped contents of the definitions.json.zip or definitions.xml.zip files
                (i.e. all conformance resources published as part of the specification or ig)

        modelName: The name of the model being generated
        modelVersion: The version of the model being generated
        useCqlPrimitives: Determines whether the generated structures should use Cql primitives for "primitive types"
        includeMetadata: Determines whether to include additional (non-structural) information such as definitions, comments, bindings, and constraints
        outputPath: Specifies the output directory for the resulting ModelInfo

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
        inputPath = Paths.get("..", "FHIR-Spec").toString(); // default
        setOutputPath("output"); // default

        for (String arg : args) {
            if (arg.equals("-GenerateMIs")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "inputpath": case "ip": inputPath = value; break; // -inputpath (-ip)
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "resourcepaths": case "rp": resourcePaths = value; break; // -resourcepaths (-rp)
                // TODO : Can we autodetect this from the structure defintions?
                // Yes, would need to be an extension definition on the ImplementationGuide...
                case "modelname": case "mn": modelName = value; break; // -modelname (-mn)
                case "modelversion": case "mv": modelVersion = value; break; // -modelversion (-mv)
                case "usecqlprimitives": case "ucp": useCQLPrimitives = value.toLowerCase().equals("true") ? true : false; break;
                case "includemetadata": case "im": includeMetadata = value.toLowerCase().equals("true") ? true : false; break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        // TODO: Need to load from NPMPackages, not directories...
        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);

        File outputPath = new File(getOutputPath());
        if (!outputPath.exists()) {
            if (!outputPath.mkdirs()) {
                throw new IllegalArgumentException(String.format("Could not create output directory %s", outputPath));
            }
        }

        ModelInfoBuilder miBuilder;
        ModelInfo mi;

        if (modelName.equals("FHIR")) {
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(atlas.getStructureDefinitions());
            ciBuilder.settings.useCQLPrimitives = this.useCQLPrimitives;
            ciBuilder.settings.includeMetaData = this.includeMetadata;
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String fhirHelpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new FHIRModelInfoBuilder(modelVersion, typeInfos, atlas, fhirHelpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("USCore")) {
            ClassInfoBuilder ciBuilder = new USCoreClassInfoBuilder(atlas.getStructureDefinitions());
            ciBuilder.settings.useCQLPrimitives = this.useCQLPrimitives;
            ciBuilder.settings.includeMetaData = this.includeMetadata;
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String helpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new USCoreModelInfoBuilder(modelVersion, typeInfos, atlas, helpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("QICore")) {
            ClassInfoBuilder ciBuilder = new QICoreClassInfoBuilder(atlas.getStructureDefinitions());
            ciBuilder.settings.useCQLPrimitives = this.useCQLPrimitives;
            ciBuilder.settings.includeMetaData = this.includeMetadata;
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            String helpersPath = this.getOutputPath() + "/" + modelName + "Helpers-" + modelVersion + ".cql";
            miBuilder = new QICoreModelInfoBuilder(modelVersion, typeInfos, atlas, helpersPath);
            mi = miBuilder.build();
        }
        else if (modelName.equals("QUICK")) {
            ClassInfoBuilder ciBuilder = new QuickClassInfoBuilder(atlas.getStructureDefinitions());
            ciBuilder.settings.useCQLPrimitives = this.useCQLPrimitives;
            ciBuilder.settings.includeMetaData = this.includeMetadata;
            Map<String, TypeInfo> typeInfos = ciBuilder.build();
            ciBuilder.afterBuild();

            miBuilder = new QuickModelInfoBuilder(modelVersion, typeInfos.values());
            mi = miBuilder.build();
        }
        else {
            //should blowup
            ClassInfoBuilder ciBuilder = new FHIRClassInfoBuilder(atlas.getStructureDefinitions());
            ciBuilder.settings.useCQLPrimitives = this.useCQLPrimitives;
            ciBuilder.settings.includeMetaData = this.includeMetadata;
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
