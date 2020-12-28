package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.io.IOException;

import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.CriteriaDTO;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;

public class CqlFileVisitor extends DefinitionBlockVisitor {
    private String outputDirectoryPath;
    private static int index = 0;

    public CqlFileVisitor(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        super.visit(conditionCriteriaRel);
        String libraryName = null;
        libraryName = inferLibraryName(conditionCriteriaRel);
        String cqlFileName = outputDirectoryPath + "/" + libraryName + ".cql";
        File file = new File(cqlFileName);
        if (!file.exists()) {
            // file.mkdirs();
            try {
                file.createNewFile();
                String cql = (conditionCriteriaRel.getCriteriaDTO() != null) ? context.buildCql(libraryName, "1.0.0", "FHIR", "4.0.1", "Patient",
                conditionCriteriaRel.getLabel() + " " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString()) 
                :
                context.buildCql(libraryName, "1.0.0", "FHIR", "4.0.1", "Patient", conditionCriteriaRel.getLabel());
                IOUtil.writeToFile(cqlFileName, cql);
                context.cqlStack.push(cql);
                index++;
                context.printMap.clear();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Duplicate Library: " + libraryName);
        }
    }

    private String inferLibraryName(ConditionCriteriaRelDTO conditionCriteriaRel) {
        String libraryName;
        if (conditionCriteriaRel.getCriteriaDTO() != null) {
            return inferLibraryName(conditionCriteriaRel, conditionCriteriaRel.getCriteriaDTO());
        }
        try {
            if (conditionCriteriaRel.getLabel().length() < 20 && !conditionCriteriaRel.getLabel().contains("=")
                    && !conditionCriteriaRel.getLabel().contains("(")) {
                libraryName = conditionCriteriaRel.getLabel().replaceAll(" ", "").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "") + "_"
                        + conditionCriteriaRel.getUuid().toString().substring(0, 5);
            } else {
                libraryName = "GeneratedCql" + index;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + index;
        }
        return libraryName;
    }

    private String inferLibraryName(ConditionCriteriaRelDTO conditionCriteriaRel, CriteriaDTO criteriaDTO) {
        String libraryName;
        try {
            if (conditionCriteriaRel.getLabel().length() < 20 && !conditionCriteriaRel.getLabel().contains("=")
                    && !conditionCriteriaRel.getLabel().contains("(")) {
                libraryName = conditionCriteriaRel.getLabel().replaceAll(" ", "").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "") + "_"
                        + criteriaDTO.getCriteriaType().toString();
            } else {
                libraryName = "GeneratedCql" + index;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + index;
        }
        return libraryName;
    }
}
