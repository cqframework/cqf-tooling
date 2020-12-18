package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.ConditionCriteriaRelDTOWrapper;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.RCKMSJacksonProvider;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.UnixTimeStampDeserializer;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.drool.traversal.DepthFirstDroolTraverser;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Expression;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class ExpressionBodyVisitorTest {

    private ObjectMapper objectMapper = initializeObjectMapper();

    @Test
    public void test_worked() {
        String encodingPath = "../CQLGenerationDocs/NonGeneratedDocs/default.json";
        File file = new File(encodingPath);
        readAndGenerateCQL(objectMapper, file);
    }

    private ObjectMapper initializeObjectMapper() {
        RCKMSJacksonProvider jacksonProvider = new RCKMSJacksonProvider();
        ObjectMapper objectMapper = jacksonProvider.createObjectMapper(JsonInclude.Include.NON_NULL, null)
                .registerModule(new SimpleModule().addDeserializer(Date.class, new UnixTimeStampDeserializer()))
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        String dtoClassPackageName = "org.cdsframework.dto";
        jacksonProvider.registerDTOs(dtoClassPackageName);

        return objectMapper;
    }

    private void readAndGenerateCQL(ObjectMapper objectMapper, File file) {
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(JsonFactory.builder().build().createParser(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Context context = new Context();
        Set<Integer> predicatePartsSize = new HashSet<Integer>();
        if (jsonNode != null) {
            List<JsonNode> nodes = jsonNode.findValues("conditionCriteriaRels");
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    // predicatePartsSize.add(node.size());
                    node.forEach(item -> {
                        ConditionCriteriaRelDTOWrapper conditionCriteriaRel = null;
                        try {
                            conditionCriteriaRel = objectMapper.treeToValue(item, ConditionCriteriaRelDTOWrapper.class);
                            Visitor visitor = new ExpressionBodyVisitor();
                            // Don't want to test Visitor with Traverser, but it is way more simple right now....
                            DroolTraverser<Visitor> traverser = new DepthFirstDroolTraverser<Visitor>(visitor).withContext(context);;
                            traverser.traverse(conditionCriteriaRel);
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch( Exception e ) {
                            System.out.println("oopsies.");
                            System.out.println(conditionCriteriaRel);
                        }
                        
                    });
                    
                    if (node.size() > 0) {
                        // System.out.println("================================");
                        predicatePartsSize.add(context.expressionStack.size());
                        if (context.expressionStack.size() > 0) {  
                            do {
                                Expression expression = context.expressionStack.pop();
                                // context.retrieveStack.pop();
                                System.out.println(expression + "\n");
                                
                            } while (context.expressionStack.size() > 0);
                        }
                        
                    }
                }
            }
            // predicatePartsSize.forEach(pair -> System.out.println(" Retrieve: " + pair.getLeft().toString() + "     " + "WhereClause: " + pair.getRight().toString())); 
        }
    }
}
