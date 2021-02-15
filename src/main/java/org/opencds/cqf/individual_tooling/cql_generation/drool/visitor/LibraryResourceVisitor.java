package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.util.List;

import org.cdsframework.dto.CdsCodeDTO;
import org.cdsframework.dto.ConditionCriteriaPredicateDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.ConditionCriteriaPredicatePartDTO;
import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.cdsframework.dto.CriteriaPredicatePartConceptDTO;
import org.cdsframework.dto.CriteriaPredicatePartDTO;
import org.cdsframework.dto.CriteriaResourceDTO;
import org.cdsframework.dto.CriteriaResourceParamDTO;
import org.cdsframework.dto.DataInputNodeDTO;
import org.cdsframework.dto.OpenCdsConceptDTO;
import org.hl7.fhir.r5.model.Library;
import org.opencds.cqf.tooling.processor.LibraryProcessor;

public class LibraryResourceVisitor implements Visitor {
    private String outputDirectoryPath;

    public LibraryResourceVisitor(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
    }

    @Override
    public void visit(List<ConditionDTO> conditionDTO) {
        LibraryProcessor libraryProcessor = new LibraryProcessor();
        List<Library> libraries = libraryProcessor.refreshGeneratedContent(this.outputDirectoryPath, "4.0.1");
    }

    @Override
    public void visit(CriteriaPredicatePartConceptDTO predicatePartConcepts) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartConceptDTO conditionPredicatePartConcepts) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaPredicatePartDTO sourcePredicatePartDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionDTO conditionDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void peek(ConditionCriteriaRelDTO conditionCriteriaRel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void peek(ConditionDTO conditionDTO) {
        // TODO Auto-generated method stub

    }
}
