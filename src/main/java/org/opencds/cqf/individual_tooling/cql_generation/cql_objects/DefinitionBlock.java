package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.enumeration.CriteriaPredicateType;

public class DefinitionBlock {

    private String alias;
    private List<Pair<String, Expression>> expressions; // <conjunction, expression>
    private List<Pair<CriteriaPredicateType, Pair<String, String>>> references; // <conjunction, reference>


    public DefinitionBlock(String alias, List<Pair<String, Expression>> expressions) {
        this.alias = alias;
        this.expressions = expressions;
        this.references = new LinkedList<Pair<CriteriaPredicateType, Pair<String, String>>>();
    }

    public DefinitionBlock(String alias) {
        this.alias = alias;
        this.expressions = new ArrayList<Pair<String, Expression>>();
        this.references = new LinkedList<Pair<CriteriaPredicateType, Pair<String, String>>>();
    }

    public DefinitionBlock() {
        this.expressions = new LinkedList<Pair<String, Expression>>();
        this.references = new LinkedList<Pair<CriteriaPredicateType, Pair<String, String>>>();
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

    public List<Pair<CriteriaPredicateType, Pair<String, String>>> getReferences() {
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
            Pair<String, String> aliasContext = reference.getRight();
            if (aliasContext.getLeft() != null) {
                if (reference.getLeft().equals(CriteriaPredicateType.Predicate)) {
                    stringBuilder.append(aliasContext.getLeft().toLowerCase() + " (\n     exists \""  + aliasContext.getRight() + "\"\n )\n");
                } else {
                    stringBuilder.append(aliasContext.getLeft().toLowerCase() + " (\n     \""  + aliasContext.getRight() + "\"\n )\n");
                }
            } else {
                if (reference.getLeft().equals(CriteriaPredicateType.Predicate)) {
                    stringBuilder.append(" (\n     exists \"" + aliasContext.getRight() + "\"\n )\n");
                } else {
                    stringBuilder.append(" (\n     \"" + aliasContext.getRight() + "\"\n )\n");
                }
            }
        });
        String content = stringBuilder.toString();
        return content;
    }
}
