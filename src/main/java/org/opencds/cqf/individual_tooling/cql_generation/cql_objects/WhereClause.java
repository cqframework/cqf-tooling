package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class WhereClause {
    private String resourceType;
    private String path;
    private String operator;
    private String concept;

    public WhereClause(String resourceType, String path, String operator, String concept) {
        this.resourceType = resourceType;
        this.path = path;
        this.operator = operator;
        this.concept = concept;
    }

    public WhereClause() {

    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    @Override
    public String toString() {
        String content = "     where " + resourceType + "." + path + " " + operator + " \"" + concept + "\"\n";
        return content;
    }
}