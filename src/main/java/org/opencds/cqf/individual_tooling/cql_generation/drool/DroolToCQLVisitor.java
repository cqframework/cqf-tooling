package org.opencds.cqf.individual_tooling.cql_generation.drool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.CriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineBlock;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineStatement;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.DefineStatementBody;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.Retrieve;
import org.opencds.cqf.individual_tooling.cql_generation.cql_objects.WhereClause;

public abstract class DroolToCQLVisitor {
    protected Map<String, Object> printMap;
    protected static final Set<Pair<String, String>> fhirModelingSet = new HashSet<Pair<String, String>>();

    public DroolToCQLVisitor(Map<String, Object> printMap) {
        this.printMap = printMap;
	}

	public abstract void visit(ConditionCriteriaRelDTO conditionCriteriaRel);

    protected abstract DefineBlock visit(ConditionCriteriaPredicateDTO predicate);

    protected abstract void visit(ConditionCriteriaPredicatePartDTO predicatePart, Retrieve retrieve, WhereClause whereClause, DefineStatementBody defineStatementBody, DefineStatement defineStatement);

    // Operator
    protected abstract void visit(CriteriaResourceParamDTO criteriaResourceParamDTO, WhereClause whereClause);

    // Retrieve and Left Operand (Modeling)
    protected abstract void visit(DataInputNodeDTO dIN, Retrieve retrieve, WhereClause whereClause);

    // Right operand (Terminology)
    protected abstract void visit(OpenCdsConceptDTO openCdsConceptDTO, WhereClause whereClause);

    protected abstract void visit(CriteriaPredicatePartDTO  sourcePredicatePartDTO, WhereClause whereClause);

    protected abstract void visit(ConditionCriteriaPredicatePartConceptDTO predicatePartConcepts, WhereClause whereClause);

    protected abstract void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts, WhereClause whereClause);

    public Map<String, Object> getCqlObjects() {
        return printMap;
    }

    public Set<Pair<String, String>> getFhirModelingSet() {
        return fhirModelingSet;
    }
}
