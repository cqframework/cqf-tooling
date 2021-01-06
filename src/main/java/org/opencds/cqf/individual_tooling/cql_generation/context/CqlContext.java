package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.enumeration.CriteriaPredicateType;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefinitionBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DirectReferenceCode;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Expression;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.ValueSet;

public class CqlContext {
    public Stack<Pair<CriteriaPredicateType, Pair<String, String>>> referenceStack = new Stack<Pair<CriteriaPredicateType, Pair<String, String>>>();
    public Stack<Expression> expressionStack = new Stack<Expression>();
    public Stack<DefinitionBlock> definitionBlockStack = new Stack<DefinitionBlock>();
    public Stack<String> cqlStack = new Stack<String>();
    public Map<String, Object> printMap = new HashMap<String, Object>();

    public String buildCql(String context) {
        StringBuilder sb = new StringBuilder();
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue().getClass().equals(DirectReferenceCode.class))
            .forEach(entry -> {
                sb.append(entry.getValue());
        });
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue().getClass().equals(ValueSet.class))
            .forEach(entry -> {
                sb.append(entry.getValue());
        });
        sb.append("\n" + "context " + context + "\n\n");
        printMap.entrySet().stream()
            .filter(entry -> entry.getValue().getClass().equals(DefinitionBlock.class))
            .forEach(entry -> {
                sb.append(entry.getValue());
        });
        return sb.toString();
    }

    public String buildCql(String libraryName, String libraryVersion, String modelName, String modelVersion,  String context, String headerComment) {
        StringBuilder sb = new StringBuilder();
        if (libraryName != null) {
            sb.append("library " + libraryName);
        }
        if (libraryVersion != null) {
            sb.append(" version \'" + libraryVersion + "\'\n");
        }
        if (headerComment != null) {
            sb.append("\n//" + headerComment + "\n");
        }
        if (modelName != null) {
            sb.append("\nusing " + modelName);
        }
        if (modelVersion != null) {
            sb.append(" version \'" + modelVersion + "\'\n\n");
        }
        if (modelName != null && modelVersion != null && modelName.equals("FHIR")) {
            sb.append("include FHIRHelpers version \'" + modelVersion + "\' \n\n");
        }
        sb.append(buildCql(context));
        return sb.toString();
    }

    public void clearCqlMap() {
        printMap.clear();
    }
}
