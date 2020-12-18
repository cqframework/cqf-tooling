package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class LibraryResourceVisitor extends CqlFileVisitor {
    public LibraryResourceVisitor(String outputDirectoryPath) {
        super(outputDirectoryPath);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel, Context context) {
        super.visit(conditionCriteriaRel, context);
    }
}
