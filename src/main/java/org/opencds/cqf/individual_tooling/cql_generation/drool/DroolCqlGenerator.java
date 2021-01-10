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
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.CqlFileVisitor.CQLTYPES;

public class DroolCqlGenerator implements CqlGenerator {
    private String outputPath;

    public DroolCqlGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void generate(String encoding, String command) {
        File file = new File(encoding);
        readAndGenerateCQL(file, command);
    }

    @Override
    public void generate(URI encodingUri, String command) {
        File file = new File(encodingUri.getPath());
        readAndGenerateCQL(file, command);  
    }

    private void readAndGenerateCQL(File file, String command) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit(command, conditions);
    }

    private void doVisit(String command, List<ConditionDTO> rootNode) {
        Visitor visitor;
        switch(command) {
            case "modeling": {
                visitor = new FHIRModelMappingVisitor();
            } break;
            case "cql": {
                visitor = new CqlFileVisitor(outputPath, CQLTYPES.CONDITION);
            } break;
            case "html": {
                visitor = new HtmlFileVisitor(outputPath);
            }
            default: throw new RuntimeException("Unkown Drool Cql Generation command: " + command);
        } 
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        traverser.traverse(rootNode);
    }
}
