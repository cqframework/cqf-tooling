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
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.individual_tooling.cql_generation.builder.ModelElmBuilder;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.DefinitionAdapter;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.DroolPredicateToElmExpressionAdapter;
import org.opencds.cqf.individual_tooling.cql_generation.drool.adapter.LibraryAdapter;

/**
 * Implements the {@link Visitor Visitor} Interface and uses the DefinitionAdapter 
 * DroolPredicateToElmExpressionAdapter and LibraryAdapter to build out Elm Libraries and write them to
 * a given output directory.  The ganularity of the libraries can be toggled by CONDITION or CONDITIONREL
 * using the {@link CQLTYPES CQLTYPES} enumeration.
 * @author  Joshua Reynolds
 * @since   2021-02-24 
 */
public class DroolToElmVisitor implements Visitor {
    public enum CQLTYPES {
        CONDITION,
        CONDITIONREL
    }
    private Enum<CQLTYPES> type = null;
    private ModelElmBuilder modelBuilder;
    private ElmContext context;
    private String outputPath;
    private DroolPredicateToElmExpressionAdapter expressionBodyAdapter;
    private DefinitionAdapter definitionAdapter = new DefinitionAdapter();
    private LibraryAdapter libraryAdapter = new LibraryAdapter();

    /**
     * Default to CONDITION granularity.
     * @param modelBuilder modelBuilder
     * @param outputPath outputPath
     */
    public DroolToElmVisitor(ModelElmBuilder modelBuilder, String outputPath) {
        this.outputPath = outputPath;
        this.type = CQLTYPES.CONDITION;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
    }

    public DroolToElmVisitor(Enum<CQLTYPES> type, ModelElmBuilder modelBuilder, String outputPath) {
        this.outputPath = outputPath;
        this.type = type;
        this.modelBuilder = modelBuilder;
        context = new ElmContext(modelBuilder);
        expressionBodyAdapter = new DroolPredicateToElmExpressionAdapter(modelBuilder);
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
        expressionBodyAdapter.adapt(sourcePredicatePartDTO, context);
    }

    @Override
    public void visit(OpenCdsConceptDTO openCdsConceptDTO) {
        expressionBodyAdapter.adapt(openCdsConceptDTO, context);
    }

    @Override
    public void visit(DataInputNodeDTO dIN) {
        expressionBodyAdapter.adapt(dIN, context);
    }

    @Override
    public void visit(CriteriaResourceParamDTO criteriaResourceParamDTO) {
        expressionBodyAdapter.adapt(criteriaResourceParamDTO, context);
    }

    @Override
    public void visit(ConditionCriteriaPredicatePartDTO predicatePart) {
        expressionBodyAdapter.adapt(predicatePart, context);
        definitionAdapter.adapt(predicatePart, context);
    }

    @Override
    public void visit(ConditionCriteriaPredicateDTO predicate) {
        expressionBodyAdapter.adapt(predicate, context);
        definitionAdapter.adapt(predicate, context);
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            definitionAdapter.conditionCriteriaMetExpression(context);
            context.buildLibrary();
        }
    }

    @Override
    public void peek(ConditionCriteriaRelDTO conditionCriteriaRel) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITIONREL)) {
            libraryAdapter.adapt(conditionCriteriaRel, context);
        }
    }

    @Override
    public void visit(CdsCodeDTO cdsCodeDTO) {
        expressionBodyAdapter.adapt(cdsCodeDTO, context);
    }

    @Override
    public void visit(CriteriaResourceDTO criteriaResourceDTO) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            definitionAdapter.conditionCriteriaMetExpression(context);
            context.buildLibrary();
        }
    }

    @Override
    public void peek(ConditionDTO conditionDTO) {
        if (this.type != null && this.type.equals(CQLTYPES.CONDITION)) {
            libraryAdapter.adapt(conditionDTO, context);
        }

    }

    @Override
    public void visit(List<ConditionDTO> rootNode) {
        libraryAdapter.adapt(rootNode, context);
        context.writeElm(outputPath);
        //TODO: remove after resolving missing valuesets
        context.writeValueSets(expressionBodyAdapter.valueSetIds);
    }

    public ElmContext getContext() {
        return context;
    }
}
