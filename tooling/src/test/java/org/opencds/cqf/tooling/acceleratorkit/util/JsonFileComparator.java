package org.opencds.cqf.tooling.acceleratorkit.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class JsonFileComparator extends FilesComparator{

    private List<String> ignoreFields = new ArrayList<>();

    public JsonFileComparator() {
        ignoreFields.add("date");
        ignoreFields.add("timestamp");
    }

    @Override
    public void compareFilesAndAssertIfNotEqual(File inputFile, File compareFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, Object>> type = new TypeReference<Map<String, Object>>() {};
        try {
                Map<String, Object> inputMap = JsonFlatMapper.flatten(objectMapper.readValue(inputFile, type));
                Map<String, Object> compareMap = JsonFlatMapper.flatten(objectMapper.readValue(compareFile, type));
                MapDifference<String, Object> difference = Maps.difference(inputMap,
                                                                    compareMap);
                assertEquals(difference.entriesDiffering().size(), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
