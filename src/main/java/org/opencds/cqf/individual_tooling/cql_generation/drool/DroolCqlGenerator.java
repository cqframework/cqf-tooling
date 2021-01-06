package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.cdsframework.dto.ConditionDTO;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.CqlFileVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.FHIRModelMappingVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.HtmlFileVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;

public class DroolCqlGenerator implements CqlGenerator {
    private String outputPath;

    public DroolCqlGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void generate(String encoding, String doCommand) {
        File file = new File(encoding);
        readAndGenerateCQL(file, doCommand);
    }

    @Override
    public void generate(URI encodingUri, String doCommand) {
        File file = new File(encodingUri.getPath());
        readAndGenerateCQL(file, doCommand);  
    }

    private void readAndGenerateCQL(File file, String doCommand) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit(doCommand, conditions);
    }

    private void doVisit(String cqlProcessingThingy, List<ConditionDTO> rootNode) {
        Visitor visitor;
        switch(cqlProcessingThingy) {
            case "datainput": {
                visitor = new FHIRModelMappingVisitor();
            } break;
            case "generate cql": {
                visitor = new CqlFileVisitor("../CQLGenerationDocs/GeneratedDocs");
            } break;
            case "html": {
                visitor = new HtmlFileVisitor("../CQLGenerationDocs/GeneratedDocs/Html");
            }
            default: throw new RuntimeException("Unkown cqlProcessingThingy: " + cqlProcessingThingy);
        } 
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        traverser.traverse(rootNode);
    }
}
