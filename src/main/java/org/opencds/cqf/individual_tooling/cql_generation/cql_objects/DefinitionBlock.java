package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class DefinitionBlock {

    private String alias;
    private List<Pair<String, Expression>> expressions; // <conjunction, expression>
    private List<Pair<String, String>> references; // <conjunction, reference>


    public DefinitionBlock(String alias, List<Pair<String, Expression>> expressions) {
        this.alias = alias;
        this.expressions = expressions;
    }

    public DefinitionBlock() {
        this.expressions = new LinkedList<Pair<String, Expression>>();
        this.references = new LinkedList<Pair<String, String>>();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<Pair<String, Expression>> getExpressions() {
        return expressions;
    }

    public List<Pair<String, String>> getReferences() {
        return references;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\ndefine \"" + this.alias + "\":\n");
        expressions.forEach(expression -> { 
            if (expression.getLeft() != null) {
                stringBuilder.append(expression.getLeft().toLowerCase() + expression.getRight());
            } else {
                stringBuilder.append(expression.getRight());
            }
        });
        references.forEach(reference -> { 
            if (reference.getLeft() != null){
                stringBuilder.append(reference.getLeft().toLowerCase() + " (\n     \""  + reference.getRight() + "\"\n )\n");
            } else {
                stringBuilder.append(" (\n     \"" + reference.getRight() + "\"\n )\n");
            }
        });
        String content = stringBuilder.toString();
        return content;
    }
}
