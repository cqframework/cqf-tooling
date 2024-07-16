package org.opencds.cqf.tooling.cql_generation.drool;

import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.cdsframework.dto.ConditionDTO;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.tooling.cql_generation.IOUtil;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToFhirElmBuilder;
import org.opencds.cqf.tooling.cql_generation.builder.VmrToModelElmBuilder;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.tooling.cql_generation.drool.serialization.Serializer;
import org.opencds.cqf.tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.ElmToCqlVisitor;
import org.opencds.cqf.tooling.cql_generation.drool.visitor.Visitor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;

/**
 * Implements the {@link CqlGenerator CqlGenerator} Interface, {@link Deserializer Deserializes} the {@link ConditionDTO ConditionDTO}
 * objects and Traverses the object graph by setting up the {@link VmrToModelElmBuilder ModelElmBuilder},
 * {@link Visitor Visitor}, and {@link DroolTraverser DroolTraverser}
 * May toggle Elm Library granularity with {@link CQLTYPES CQLTYPES}
 * @author  Joshua Reynolds
 * @since   2021-02-24
 */
public class DroolCqlGenerator implements CqlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DroolCqlGenerator.class);
    private CQLTYPES type;
    private File cqlOutput;

    public DroolCqlGenerator(CQLTYPES type) {
        this.type = type;
    }

    @Override
    public void generateAndWriteToFile(String inputPath, String outputPath, String fhirVersion) {
        File input = new File(inputPath);
        File output = new File(outputPath);
        if (!output.isDirectory()) {
            output.mkdirs();
        }
        this.cqlOutput = new File(output.getAbsolutePath() + "/cql");
        cqlOutput.mkdirs();
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
        this.cqlOutput = new File(output.getAbsolutePath() + "/cql");
        cqlOutput.mkdirs();
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
        ElmContext context = traverser.traverse(rootNode);
        context.libraries.values().forEach(library -> {
            ElmToCqlVisitor elmVisitor = new ElmToCqlVisitor();
            elmVisitor.visitLibrary(library, context);
            context.cqlStrings.push(elmVisitor.getOutput());
            writeCql(context, cqlOutput);
        });
        return context;
    }

    private VmrToModelElmBuilder resolveModel(String fhirVersion) {
        return new VmrToFhirElmBuilder(fhirVersion, new DecimalFormat("#.#"));
    }

    private void writeCql(ElmContext context, File outpuDirectory) {
        context.cqlStrings.forEach(cql -> {
            if (outpuDirectory.isDirectory()) {
                VersionedIdentifier vi = new VersionedIdentifier();
                vi.setId(getIdFromSource(cql));
                vi.setVersion(getVersionFromSource(cql));
                File outputFile = new File(IOUtils.concatFilePath(outpuDirectory.getAbsolutePath(),
                        vi.getId() + "-" + vi.getVersion() + ".cql"));
                IOUtil.writeToFile(outputFile, cql);
            } else {
                throw new IllegalArgumentException("Output directory is not a directory: " + outpuDirectory.getAbsolutePath());
            }
        });
    }

    private void writeElm(ElmContext context, VmrToModelElmBuilder modelBuilder, File outpuDirectory) {
        Serializer serializer = new Serializer(Library.class);
        context.libraries.entrySet().stream().forEach(entry -> {
            logger.info(entry.getKey());
            try {
                String elm = serializer.convertToXml(modelBuilder.of.createLibrary(entry.getValue()), serializer.getJaxbContext());
                if (outpuDirectory.isDirectory()) {
                    File outputFile = new File(IOUtils.concatFilePath(outpuDirectory.getAbsolutePath(),
                            entry.getKey() + ".xml"));
                    IOUtil.writeToFile(outputFile, elm);
                } else {
                    throw new IllegalArgumentException("Output directory is not a directory: " + outpuDirectory.getAbsolutePath());
                }
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        });
    }

    private String getIdFromSource(String cql) {
        if (cql.startsWith("library")) {
            return getNameFromSource(cql);
        }

        throw new RuntimeException("This tool requires cql libraries to include a named/versioned identifier");
    }

    private String getNameFromSource(String cql) {
        return cql.replaceFirst("library ", "").split(" version")[0].replaceAll("\"", "");
    }
    private String getVersionFromSource(String cql) {
        return cql.split("version")[1].split("'")[1];
    }
}
