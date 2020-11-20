package org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects;

public class SourcePredicatePartDTO {
    private String dataInputClassType;
    private PredicatePartConcepts[] predicatePartConcepts;

    public String getDataInputClassType() {
        return dataInputClassType;
    }

    public void setDataInputClassType(String dataInputClassType) {
        this.dataInputClassType = dataInputClassType;
    }

    public PredicatePartConcepts[] getPredicatePartConcepts() {
        return predicatePartConcepts;
    }

    public void setPredicatePartConcepts(PredicatePartConcepts[] predicatePartConcepts) {
        this.predicatePartConcepts = predicatePartConcepts;
    }

    public boolean hasPredicatePartConcepts() {
        if (this.predicatePartConcepts != null && this.predicatePartConcepts.length > 0) {
            return true;
        }
        else return false;
    }
}
