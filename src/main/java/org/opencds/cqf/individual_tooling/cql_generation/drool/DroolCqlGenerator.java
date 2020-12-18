package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.cdsframework.dto.DataInputNodeDTO;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.ConditionCriteriaRelDTOWrapper;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.Deserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.CqlFileVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.FHIRModelMappingVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.Visitor;

public class DroolCqlGenerator implements CqlGenerator {
    private String outputPath;

    public DroolCqlGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void generate(String encoding) {
        File file = new File(encoding);
        readAndGenerateCQL(file);
    }

    @Override
    public void generate(URI encodingUri) {
        File file = new File(encodingUri.getPath());
        readAndGenerateCQL(file);  
    }

    private void readAndGenerateCQL(File file) {
        Context context = new Context();
        Visitor cqlFileVisitor = new CqlFileVisitor("../CQLGenerationDocs/GeneratedDocs");
        DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(cqlFileVisitor).withContext(context);
        Deserializer deserializer = new Deserializer(file);
        List<Object> conditionCriteriaRelNodes = deserializer.deserialize("conditionCriteriaRels", ConditionCriteriaRelDTOWrapper.class);
        for (Object node : conditionCriteriaRelNodes) {
            if (node != null) {
                if (node instanceof ConditionCriteriaRelDTOWrapper) {
                    ConditionCriteriaRelDTOWrapper conditionCriteriaRel = (ConditionCriteriaRelDTOWrapper)node;
                        traverser.traverse(conditionCriteriaRel);
                        System.out.println(conditionCriteriaRel.getLabel());
                }
            }
        }
        Visitor fhirModelMappingVisitor = new FHIRModelMappingVisitor();
        List<Object> nodes = deserializer.deserialize("dataInputNodeDTO", DataInputNodeDTO.class);
        for (Object node : nodes) {
            if (node != null) {
                if (node instanceof DataInputNodeDTO) {
                    DataInputNodeDTO dataInputNodeDTO = (DataInputNodeDTO)node;
                    fhirModelMappingVisitor.visit(dataInputNodeDTO, context);
                }
            }
        }
        context.writeFHIRModelMapping();     
    }
}
