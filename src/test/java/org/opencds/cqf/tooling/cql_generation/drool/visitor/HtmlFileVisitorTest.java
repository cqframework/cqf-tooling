package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.io.File;
import java.util.List;

import org.cdsframework.dto.ConditionDTO;
import org.testng.annotations.Test;
import org.opencds.cqf.tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.tooling.cql_generation.drool.traversal.DroolTraverser;

public class HtmlFileVisitorTest {
    // @Test
    public void test_worked() {
        String encodingPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        File file = new File(encodingPath);
        readAndGenerateCQL(file);
    }

    private void readAndGenerateCQL(File file) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit("../CQLGenerationDocs/GeneratedDocs/Html", conditions);
    }

    private void doVisit(String outputPath, List<ConditionDTO> rootNode) {
        Visitor visitor = new HtmlFileVisitor(outputPath); 
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        traverser.traverse(rootNode);
    }
}
