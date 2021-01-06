package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.util.List;

import org.cdsframework.dto.ConditionDTO;
import org.junit.Test;

import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;

public class FHIRModelMappingVisitorTest {
    @Test
    public void test_worked() {
        String encodingPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        File file = new File(encodingPath);
        readAndGenerateCQL(file);
    }

    private void readAndGenerateCQL(File file) {
        Deserializer deserializer = new Deserializer(file);
        List<ConditionDTO> conditions = deserializer.deserialize();
        doVisit(conditions);
    }

    private void doVisit(List<ConditionDTO> rootNode) {
        Visitor visitor = new FHIRModelMappingVisitor(); 
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor);
        traverser.traverse(rootNode);
    }
}
