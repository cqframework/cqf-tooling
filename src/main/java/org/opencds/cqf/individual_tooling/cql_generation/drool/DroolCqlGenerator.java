package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import org.cdsframework.dto.ConditionDTO;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.builder.FhirModelElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.builder.ModelElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.HtmlFileVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;

/**
 * Implements the {@link CqlGenerator CqlGenerator} Interface, {@link Deserializer Deserializes} the {@link ConditionDTO ConditionDTO}
 * objects and Traverses the object graph by setting up the {@link ModelElmBuilder ModelElmBuilder},
 * {@link Visitor Visitor}, and {@link DroolTraverser DroolTraverser}
 * May toggle Elm Library granularity with {@link CQLTYPES CQLTYPES}
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DroolCqlGenerator implements CqlGenerator {
    private String outputPath;
    private CQLTYPES type;

    public DroolCqlGenerator(String outputPath, CQLTYPES type) {
        this.outputPath = outputPath;
        this.type = type;
    }

    @Override
    public void generate(String inputPath, String fhirVersion) {
        File file = new File(inputPath);
        readAndGenerateCQL(file, fhirVersion);
    }

    @Override
    public void generate(URI inputURI, String fhirVersion) {
        File file = new File(inputURI.getPath());
        readAndGenerateCQL(file, fhirVersion);  
    }

    private void readAndGenerateCQL(File file, String fhirVersion) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit(conditions, fhirVersion);
    }

    private void doVisit(List<ConditionDTO> rootNode, String fhirVersion) {
        ModelElmBuilder modelBuilder = resolveModel(fhirVersion);
        Visitor visitor = new DroolToElmVisitor(type, modelBuilder, outputPath);
        // visitor = new HtmlFileVisitor(outputPath);
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        traverser.traverse(rootNode);
    }

    private ModelElmBuilder resolveModel(String fhirVersion) {
        return new FhirModelElmBuilder(fhirVersion, new DecimalFormat("#.#"));
    }
}
