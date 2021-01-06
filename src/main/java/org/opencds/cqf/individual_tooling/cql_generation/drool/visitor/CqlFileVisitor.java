package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;

public class CqlFileVisitor extends DefinitionBlockVisitor {
    protected String outputDirectoryPath;
    private Map<String, String> libraries = new HashMap<String, String>();
    private static int index = 0;

    public CqlFileVisitor(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        super.visit(conditionCriteriaRel);

        String libraryName = inferLibraryName(conditionCriteriaRel);
        String header = conditionCriteriaRel.getLabel();
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            header = header.concat(" " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString());
        }
        String cql = context.buildCql(libraryName, "1.0.0", "FHIR", "4.0.1", "Patient", header);
        libraries.put(libraryName, cql);
        context.cqlStack.push(cql);
        index++;
        context.printMap.clear();
    }

	public void visit(List<ConditionDTO> rootNode) {
        libraries.entrySet().stream().forEach(entry -> {
            File file = new File(outputDirectoryPath + "/" + entry.getKey() + ".cql");
            IOUtil.writeToFile(file, entry.getValue());
        });
    }

    private String inferLibraryName(ConditionCriteriaRelDTO conditionCriteriaRel) {
        String libraryName = buildName(conditionCriteriaRel);
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            libraryName = libraryName.concat( "_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
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
}
