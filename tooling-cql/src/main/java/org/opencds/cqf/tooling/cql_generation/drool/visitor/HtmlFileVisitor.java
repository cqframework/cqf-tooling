package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opencds.cqf.tooling.cql_generation.IOUtil;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;
import org.opencds.cqf.tooling.utilities.IOUtils;

/**
 * Generates html files from an RCKMS Drool Object graph.
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class HtmlFileVisitor implements Visitor {
    private String outputDirectoryPath;
    private Map<String, String> htmlStrings = new HashMap<String, String>();
    private static int index = 0;

    public HtmlFileVisitor(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
        File directory = new File(outputDirectoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
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
        String libraryName = inferLibraryName(conditionCriteriaRel);
        String html = conditionCriteriaRel.getRuleSetHtml();
        htmlStrings.put(libraryName, html);
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
    public ElmContext visit(List<ConditionDTO> rootNode) {
        htmlStrings.entrySet().stream().forEach(entry -> {
            String filePath = IOUtils.concatFilePath(outputDirectoryPath, entry.getKey() + ".html");
            File file = new File(filePath);
            String content = "<html><head><title>" + entry.getKey() + "</title></head><body><p>" + entry.getValue()
                    + "</p></body></html>";
            IOUtil.writeToFile(file, content);
        });
        return null;
    }

    private String inferLibraryName(ConditionCriteriaRelDTO conditionCriteriaRel) {
        String libraryName = buildName(conditionCriteriaRel);
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            libraryName = libraryName.concat("_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
        }
        return libraryName;
    }

    private String buildName(ConditionCriteriaRelDTO conditionCriteriaRel) {
        String libraryName = null;
        try {
            if (conditionCriteriaRel.getLabel().length() < 20 && !conditionCriteriaRel.getLabel().contains("=")
                    && !conditionCriteriaRel.getLabel().contains("(")) {
                libraryName = conditionCriteriaRel.getLabel().replaceAll(" ", "").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "");
            } else {
                libraryName = "GeneratedCql" + index;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + index;
        }
        return libraryName;
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
