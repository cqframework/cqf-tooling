package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.io.IOException;

import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.context.Context;

public class CqlFileVisitor extends DefinitionBlockVisitor {
    private String outputDirectoryPath;

    public CqlFileVisitor(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
    }
    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel, Context context) {
        super.visit(conditionCriteriaRel, context);
        String cqlFileName = null;
        try {
            if (conditionCriteriaRel.getLabel().length() < 20) {
                cqlFileName = outputDirectoryPath + "/" + conditionCriteriaRel.getLabel().replaceAll(" ", "")
                        .replaceAll("<", "").replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "")
                        .replaceAll("#", "").replaceAll("TEST", "").replaceAll("TEST2", "")
                        .replaceAll("TEST3", "").replaceAll("TESTExample-Daryl", "") + "-" + conditionCriteriaRel.getUuid().toString().substring(0, 5)
                        + ".cql";
            } else {
                cqlFileName = outputDirectoryPath + "/" + conditionCriteriaRel.getUuid().toString() + ".cql";
            }
        } catch (Exception e) {
            cqlFileName = outputDirectoryPath + "/" + conditionCriteriaRel.getUuid().toString() + ".cql";
        }
        File file = new File(cqlFileName);
        if (!file.exists()) {
            // file.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String cql = context.buildCql();
        IOUtil.writeToFile(cqlFileName, cql);
        context.cqlStack.push(cql);
        context.printMap.clear();
    }  
}
