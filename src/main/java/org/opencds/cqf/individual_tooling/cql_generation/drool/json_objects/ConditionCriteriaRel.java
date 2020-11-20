package org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects;

public class ConditionCriteriaRel {
    private String relId;
    private String conditionId;
    private Predicate[] predicates;
    private String label;
    private String ruleSetHtml;

    public String getRelId() {
        return relId;
    }

    public void setRelId(String relId) {
        this.relId = relId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public Predicate[] getPredicates() {
        return predicates;
    }

    public void setPredicates(Predicate[] predicates) {
        this.predicates = predicates;
    }

    public boolean hasPredicates() {
        if (this.predicates != null && this.predicates.length > 0) {
            return true;
        }
        else return false;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRuleSetHtml() {
        return ruleSetHtml;
    }

    public void setRuleSetHtml(String ruleSetHtml) {
        this.ruleSetHtml = ruleSetHtml;
    }
}
