package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.drool.rckms.ConditionCriteriaRelDTOWrapper;
import org.opencds.cqf.individual_tooling.cql_generation.drool.rckms.RCKMSJacksonProvider;
import org.opencds.cqf.individual_tooling.cql_generation.drool.rckms.RCKMSVisitor;
import org.opencds.cqf.individual_tooling.cql_generation.drool.rckms.UnixTimeStampDeserializer;

public class DroolCqlGenerator implements CqlGenerator {
    private String outputPath;
    private Map<String, Object> printMap = new HashMap<String, Object>();
    private ObjectMapper objectMapper = initializeObjectMapper();

    public DroolCqlGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void generate(String encoding) {
        File file = new File(encoding);
        readAndGenerateCQL(objectMapper, file);
    }

    @Override
    public void generate(URI encodingUri) {
        File file = new File(encodingUri.getPath());
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

        DroolToCQLVisitor hackyVisitor = new RCKMSVisitor(printMap);
        if (jsonNode != null) {
            List<JsonNode> nodes = jsonNode.findValues("conditionCriteriaRels");
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    node.forEach(item -> {
                        ConditionCriteriaRelDTOWrapper conditionCriteriaRel = null;
                        try {
                            conditionCriteriaRel = objectMapper.treeToValue(item, ConditionCriteriaRelDTOWrapper.class);
                            
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (conditionCriteriaRel != null) {
                            hackyVisitor.visit(conditionCriteriaRel);
                            System.out.println(conditionCriteriaRel.getLabel());
                        }
                    });
                }
            }
            print(outputPath);
            writeFHIRModelMapping(file, hackyVisitor);
                            
        }
    }

    private void writeFHIRModelMapping(File file, DroolToCQLVisitor hackyVisitor) {
        String fhirModelingMapFilePath = ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\fhirmodelingmap.txt";
        File fhirModelingMapFile = new File(fhirModelingMapFilePath);
        if (file.exists()) {
            fhirModelingMapFile.delete();
        }
        try {
            fhirModelingMapFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        hackyVisitor.getFhirModelingSet().stream()
            .forEach(element -> IOUtil.writeToFile(fhirModelingMapFile, element.getLeft() + ":     " + element.getRight() + "\n"));
    }

    private void print(String filePath) {
        File file = new File(filePath);
        file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DirectReferenceCode)
            .forEach(entry -> IOUtil.writeToFile(file, entry.getValue().toString()));
        IOUtil.writeToFile(file, "\n\n");
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DefineBlock)
            .forEach(entry -> IOUtil.writeToFile(file, entry.getValue().toString()));
    }
}
