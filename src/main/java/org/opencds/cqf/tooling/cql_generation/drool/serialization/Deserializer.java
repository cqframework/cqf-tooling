package org.opencds.cqf.tooling.cql_generation.drool.serialization;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.cdsframework.dto.ConditionDTO;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Deserializes RCKMS Drool data
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class Deserializer {

    private ObjectMapper objectMapper = initializeObjectMapper();
    private File file;

    public Deserializer(File file) {
        this.file = file;
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

    public List<ConditionDTO> deserialize() {
        List<ConditionDTO> conditions = null;
        try {
            conditions = objectMapper.readValue(JsonFactory.builder().build().createParser(file), new TypeReference<List<ConditionDTO>>(){});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conditions;
    }

    public List<Object> deserialize(String nodeName, Class<?> objectClass) {
        List<Object> objects = new LinkedList<Object>();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(JsonFactory.builder().build().createParser(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (jsonNode != null) {
            List<JsonNode> nodes = jsonNode.findValues(nodeName);
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    node.forEach(item -> {
                        Object nodeObject = null;
                        try {
                            nodeObject = objectMapper.treeToValue(item, objectClass);
                            objects.add(nodeObject);
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    });
                } else {
                    if (node != null) {
                        Object nodeObject = null;
                        try {
                            nodeObject = objectMapper.treeToValue(node, objectClass);
                            objects.add(nodeObject);
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return objects;
    }
}
