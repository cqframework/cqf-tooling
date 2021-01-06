package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class Expression {
    protected String left = null;
    protected String right = null;
    protected String operator = null;
    protected String resourceType = null;
    protected String path = null;
    protected String concept = null;


    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    private void ensureLeft() {
        if (resourceType == null || path == null ) {
            return;
        }
        this.left = "     where " + resourceType + "." + path;
    }

    private void ensureRight() {
        if (concept == null) {
            return;
        }
        this.right = "\"" + concept + "\"\n";
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
        this.ensureLeft();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.ensureLeft();
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
        this.ensureRight();
    }

    public String retrieveString() {
        String content = "[" + resourceType + "] " + resourceType + "\n";
        return content;
    }

    @Override
    public String toString() {
        if (resourceType == null) {
            return left + " " + operator + " " + right;
        } else if (path.contains("focus") || path.contains("?") || right == null) {
            return operator.equals("==") ?  "     " + retrieveString() + "     " +  ("//" + left + " " + "in" + " " + right) : "     " + retrieveString() + "     " +  ("//" + left + " " + operator + " " + right);
            // exists (
            //     Observation.interpretation Interpretation
            //       where Interpretation in "Abnormal Interpretation of an Observation"
            //   )
        } else if (path.contains("interpretation")) {
            return operator.equals("==") ?  "     " + retrieveString() + "     where exists (\n          " +  (resourceType + "." + path + " Interpretation \n              where Interpretation " + "in" + " " + right + "\n     )") : "     " + retrieveString() + "     exists (" +  (resourceType + path + " Interpretation \n         where Interpretation" + operator + " " + right + "\n     )");
        } else {
            return operator.equals("==") ?  "     " + retrieveString() + "     " +  (left + " " + "in" + " " + right) : "     " + retrieveString() + "     " +  (left + " " + operator + " " + right);
        }
    }
}
