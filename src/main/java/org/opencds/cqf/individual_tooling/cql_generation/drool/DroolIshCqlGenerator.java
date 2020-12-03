package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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

import org.cdsframework.base.BaseDTO;
import org.cdsframework.dto.ConditionCategoryDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.cdsframework.dto.ConditionDeploymentLogDTO;
import org.cdsframework.dto.ConditionReferenceDTO;
import org.cdsframework.dto.ConditionReferenceFileDTO;
import org.cdsframework.dto.ConditionSpecificationDTO;
import org.cdsframework.dto.DeploymentQueueDTO;
import org.cdsframework.dto.JurisdictionConditionDTO;
import org.cdsframework.dto.JurisdictionContactDTO;
import org.cdsframework.dto.JurisdictionDTO;
import org.cdsframework.dto.JurisdictionRoutingEntityDTO;
import org.cdsframework.dto.JurisdictionZipCodeDTO;
import org.cdsframework.dto.LogicSetDTO;
import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;

public class DroolIshCqlGenerator implements CqlGenerator {
    private String outputPath;
    private Map<String, Object> printMap = new HashMap<String, Object>();
    private ObjectMapper objectMapper = initializeObjectMapper();

    public DroolIshCqlGenerator(String outputPath) {
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
        List<Class<? extends BaseDTO>> dtoClasses = new ArrayList<Class<? extends BaseDTO>>();
        dtoClasses.add(ConditionCriteriaPredicateDTO.class);
        dtoClasses.add(ConditionCriteriaPredicatePartConceptDTO.class);
        dtoClasses.add(ConditionCriteriaPredicatePartDTO.class);
        dtoClasses.add(ConditionCriteriaRelDTO.class);
        dtoClasses.add(ConditionDTO.class);
        dtoClasses.add(ConditionReferenceDTO.class);
        dtoClasses.add(ConditionReferenceFileDTO.class);
        dtoClasses.add(ConditionSpecificationDTO.class);
        dtoClasses.add(ConditionDeploymentLogDTO.class);
        dtoClasses.add(ConditionCategoryDTO.class);
        dtoClasses.add(DeploymentQueueDTO.class);
        dtoClasses.add(JurisdictionConditionDTO.class);
        dtoClasses.add(JurisdictionDTO.class);
        dtoClasses.add(JurisdictionContactDTO.class);
        dtoClasses.add(JurisdictionZipCodeDTO.class);
        dtoClasses.add(JurisdictionRoutingEntityDTO.class);
        dtoClasses.add(LogicSetDTO.class);
        JacksonProvider jacksonProvider = new JacksonProvider();
        jacksonProvider.registerDTOs(dtoClasses);
        ObjectMapper objectMapper = jacksonProvider.getContext(ObjectMapper.class)
            .registerModule(new SimpleModule().addDeserializer(Date.class, new UnixTimeStampDeserializer()))
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
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

        if (jsonNode != null) {
            ConditionCriteriaRelDTOWrapper conditionCriteriaRel = null;
            try {
                conditionCriteriaRel = objectMapper.treeToValue(jsonNode, ConditionCriteriaRelDTOWrapper.class);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (conditionCriteriaRel != null) {
                HackyVisitor hackyVisitor = new HackyVisitor(printMap);
                hackyVisitor.visit(conditionCriteriaRel);
                print(outputPath);
    
                System.out.println(conditionCriteriaRel.getLabel());
            }
        }
    }

    private void print(String filePath) {
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DirectReferenceCode)
            .forEach(entry -> IOUtil.writeToFile(filePath, entry.getValue().toString()));
        IOUtil.writeToFile(filePath, "\n\n");
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DefineBlock)
            .forEach(entry -> IOUtil.writeToFile(filePath, entry.getValue().toString()));
    }
}
