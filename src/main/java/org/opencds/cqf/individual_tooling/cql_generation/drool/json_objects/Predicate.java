package org.opencds.cqf.individual_tooling.cql_generation.drool.json_objects;

public class Predicate {
    private String predicateType;
    private String predicateConjunction;
    private PredicatePart[] predicateParts;
    private Predicate[] predicates;

    public String getPredicateType() {
        return predicateType;
    }

    public void setPredicateType(String predicateType) {
        this.predicateType = predicateType;
    }

    public String getPredicateConjunction() {
        return predicateConjunction;
    }

    public void setPredicateConjunction(String predicateConjunction) {
        this.predicateConjunction = predicateConjunction;
    }

    public PredicatePart[] getPredicateParts() {
        return predicateParts;
    }

    public void setPredicateParts(PredicatePart[] predicateParts) {
        this.predicateParts = predicateParts;
    }

    public boolean hasPredicateParts() {
        if (this.predicateParts != null && this.predicateParts.length > 0) {
            return true;
        }
        else return false;
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
}
