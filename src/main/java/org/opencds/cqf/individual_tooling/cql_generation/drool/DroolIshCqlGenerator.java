package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects.ConditionCriteriaRel;

public class DroolIshCqlGenerator implements CqlGenerator {
    private String outputPath;
    private Map<String, Object> printMap = new HashMap<String, Object>();

    public DroolIshCqlGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void generate(String encoding) {
        Gson gson = new Gson();
        StringBuilder stringBuilder = new StringBuilder();
        JsonReader reader = new JsonReader(new StringReader(stringBuilder.toString()));
        reader.setLenient(true);
        ConditionCriteriaRel conditionCriteriaRel = gson.fromJson(reader, ConditionCriteriaRel.class);

        HackyVisitor hackyVisitor = new HackyVisitor(printMap);
        hackyVisitor.visit(conditionCriteriaRel);
        print(outputPath);

        System.out.println(conditionCriteriaRel.getLabel());
    }

    @Override
    public void generate(URI encodingUri) {
        Gson gson = new Gson();
        File file = new File(encodingUri.getPath());
        List<String> contents = null;
        try {
            contents = Files.readLines(file, StandardCharsets.ISO_8859_1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        contents.forEach(x -> stringBuilder.append(x));
        JsonReader reader = new JsonReader(new StringReader(stringBuilder.toString()));
        reader.setLenient(true);
        ConditionCriteriaRel conditionCriteriaRel = gson.fromJson(reader, ConditionCriteriaRel.class);

        HackyVisitor hackyVisitor = new HackyVisitor(printMap);
        hackyVisitor.visit(conditionCriteriaRel);
        print(outputPath);

        System.out.println(conditionCriteriaRel.getLabel());
    }

    private void print(String filePath) {
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DirectReferenceCode)
            .forEach(entry -> IOUtil.writeToFile(filePath, entry.getValue().toString()));
        IOUtil.writeToFile(filePath, "\n\n");
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof DefineBlock)
            .forEach(entry -> IOUtil.writeToFile(filePath, entry.getValue().toString()));

        /*
        for (Entry<String, List<String>> entry : printMap.entrySet()) {
            if (entry.getKey().equals("DirectReferenceCode")) {
                for (String stringEntry : entry.getValue()) {
                    IOUtil.writeToFile(filePath, stringEntry);
                }
                // entry.getValue().forEach(stringEntry -> IOUtil.writeToFile(filePath, stringEntry));
            }
        }
        for (Entry<String, List<String>> entry : printMap.entrySet()) {
            if (entry.getKey().equals("DefineBlock")) {
                entry.getValue().forEach(stringEntry -> IOUtil.writeToFile(filePath, stringEntry));
            }
        }
        */
    }
    
}
