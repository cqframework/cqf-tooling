package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.cdsframework.dto.ConditionDTO;
import org.hl7.elm.r1.Library;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.builder.VmrToFhirElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Serializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.HtmlFileVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

/**
 * Implements the {@link CqlGenerator CqlGenerator} Interface, {@link Deserializer Deserializes} the {@link ConditionDTO ConditionDTO}
 * objects and Traverses the object graph by setting up the {@link VmrToModelElmBuilder ModelElmBuilder},
 * {@link Visitor Visitor}, and {@link DroolTraverser DroolTraverser}
 * May toggle Elm Library granularity with {@link CQLTYPES CQLTYPES}
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DroolCqlGenerator implements CqlGenerator {
    private CQLTYPES type;

    public DroolCqlGenerator(CQLTYPES type) {
        this.type = type;
    }

    @Override
    public void generateAndWriteToFile(String inputPath, String outputPath, String fhirVersion) {
        File input = new File(inputPath);
        File output = new File(outputPath);
        if (!output.isFile()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        VmrToModelElmBuilder modelBuilder = resolveModel(fhirVersion);
        ElmContext context = readAndGenerateCQL(input, modelBuilder);
        writeElm(context, modelBuilder, output);
    }

    @Override
    public Map<String, Library> generate(String inputPath, String fhirVersion) {
        File input = new File(inputPath);
        VmrToModelElmBuilder modelBuilder = resolveModel(fhirVersion);
        ElmContext context = readAndGenerateCQL(input, modelBuilder);
        return context.libraries;
    }

    @Override
    public void generateAndWriteToFile(URI inputURI, URI outputURI, String fhirVersion) {
        File file = new File(inputURI.getPath());
        File output = new File(outputURI.getPath());
        VmrToModelElmBuilder modelBuilder = resolveModel(fhirVersion);
        ElmContext context = readAndGenerateCQL(file, modelBuilder);
        writeElm(context, modelBuilder, output);  
    }

    @Override
    public Map<String, Library> generate(URI inputURI, String fhirVersion) {
        File file = new File(inputURI.getPath());
        VmrToModelElmBuilder modelBuilder = resolveModel(fhirVersion);
        ElmContext context = readAndGenerateCQL(file, modelBuilder);
        return context.libraries;
    }

    private ElmContext readAndGenerateCQL(File input, VmrToModelElmBuilder modelBuilder) {
        Deserializer deserializer = new Deserializer(input);
        List<ConditionDTO> conditions = deserializer.deserialize();
        return doVisit(conditions, modelBuilder);
    }

    private ElmContext doVisit(List<ConditionDTO> rootNode, VmrToModelElmBuilder modelBuilder) {
        Visitor visitor = new DroolToElmVisitor(type, modelBuilder);
        // visitor = new HtmlFileVisitor(outputPath);
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        return traverser.traverse(rootNode);
    }

    private VmrToModelElmBuilder resolveModel(String fhirVersion) {
        return new VmrToFhirElmBuilder(fhirVersion, new DecimalFormat("#.#"));
    }

    private void writeElm(ElmContext context, VmrToModelElmBuilder modelBuilder, File outpuDirectory) {
        Serializer serializer = new Serializer(Library.class);
        context.libraries.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey());
            try {
                String elm = serializer.convertToXml(modelBuilder.of.createLibrary(entry.getValue()), serializer.getJaxbContext());
                if (outpuDirectory.isDirectory()) {
                    File outputFile = new File(outpuDirectory.getAbsolutePath() + "/" + entry.getKey() + ".xml");
                    IOUtil.writeToFile(outputFile, elm);
                } else {
                    System.out.println("Output directory is not a directory: " + outpuDirectory.getAbsolutePath());
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }
}
