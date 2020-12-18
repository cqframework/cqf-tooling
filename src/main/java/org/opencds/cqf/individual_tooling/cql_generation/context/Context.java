package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefinitionBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Expression;

public class Context {
    public Set<Pair<String, String>> fhirModelingSet = new HashSet<Pair<String, String>>();
    public Stack<Pair<String, String>> referenceStack = new Stack<Pair<String, String>>();
    public Stack<Expression> expressionStack = new Stack<Expression>();
    public Stack<DefinitionBlock> definitionBlockStack = new Stack<DefinitionBlock>();
    public Stack<String> cqlStack = new Stack<String>();
    public Map<String, Object> printMap = new HashMap<String, Object>();

    public String buildCql() {
        StringBuilder sb = new StringBuilder();
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue().getClass().equals(DirectReferenceCode.class))
            .forEach(entry -> {
                sb.append(entry.getValue());
        });
        sb.append("\n\n\n");
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue().getClass().equals(DefinitionBlock.class))
            .forEach(entry -> {
                sb.append(entry.getValue());
        });
        return sb.toString();
    }

    public void writeFHIRModelMapping() {
        String fhirModelingMapFilePath = ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\fhirmodelingmap.txt";
        File fhirModelingMapFile = new File(fhirModelingMapFilePath);
        if (fhirModelingMapFile.exists()) {
            fhirModelingMapFile.delete();
        }
        try {
            fhirModelingMapFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fhirModelingSet.stream()
            .forEach(element -> IOUtil.writeToFile(fhirModelingMapFile, element.getLeft() + ":     " + element.getRight() + "\n"));
    }

    public void clearCqlMap() {
        printMap.clear();
    }
}
