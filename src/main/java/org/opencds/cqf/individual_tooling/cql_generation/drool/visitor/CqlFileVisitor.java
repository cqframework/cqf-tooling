package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cdsframework.dto.ConditionCriteriaRelDTO;
import org.cdsframework.dto.ConditionDTO;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;

public class CqlFileVisitor extends DefinitionBlockVisitor {
    // IN order to look through simple cql an option for CONDITIONREL is here for now.
    public enum CQLTYPES {
        CONDITION,
        CONDITIONREL
    }
    protected String outputDirectoryPath;
    private Map<String, String> libraries = new HashMap<String, String>();
    private static int index = 0;
    private Enum<CQLTYPES> type = null;

    public CqlFileVisitor(String outputDirectoryPath, Enum<CQLTYPES> type) {
        this.outputDirectoryPath = outputDirectoryPath;
        this.type = type;
    }

    @Override
	public void visit(List<ConditionDTO> rootNode) {
        libraries.entrySet().stream().forEach(entry -> {
            File file = new File(outputDirectoryPath + "/" + entry.getKey().replaceAll("_", "-") + ".cql");
            IOUtil.writeToFile(file, entry.getValue());
        });
    }

    @Override
    public void visit(ConditionDTO conditionDTO) {
        super.visit(conditionDTO);
        if (type != null && type.equals(CQLTYPES.CONDITION)) {
            String libraryName = null;
            String header = null;
            if (conditionDTO.getCdsCodeDTO() != null) {
                libraryName = inferLibraryName(conditionDTO.getCdsCodeDTO().getDisplayName());
                header = conditionDTO.getCdsCodeDTO().getDisplayName();
            } else {
                throw new RuntimeException("Unable to infer library name for condition " + conditionDTO.getUuid().toString());
            }
            resolveContext(libraryName, header);
        }
    }

    @Override
    public void visit(ConditionCriteriaRelDTO conditionCriteriaRel) {
        super.visit(conditionCriteriaRel);
        if (type != null && type.equals(CQLTYPES.CONDITIONREL)) {
            String libraryName = inferLibraryName(conditionCriteriaRel.getLabel());
            String header = conditionCriteriaRel.getLabel();
            if (conditionCriteriaRel.getCriteriaDTO() != null) {
                libraryName = libraryName.concat( "_" + conditionCriteriaRel.getUuid().toString().substring(0, 5));
                header = header.concat(" " + conditionCriteriaRel.getCriteriaDTO().getCriteriaType().toString());
            }
            resolveContext(libraryName, header);
        }
    }

    private String inferLibraryName(String libraryName) {
        try {
            if (libraryName.replaceAll(" ", "").length() < 50 && !libraryName.contains("=")) {
                libraryName = libraryName.replaceAll(" ", "_").replaceAll("<", "")
                        .replaceAll(">=", "").replaceAll(",", "").replaceAll(":", "").replaceAll("#", "")
                        .replaceAll("TEST", "").replaceAll("TEST2", "").replaceAll("TEST3", "")
                        .replaceAll("TESTExample-Daryl", "").replaceAll("[()]", "");
            } else {
                libraryName = "GeneratedCql" + index;
            }
        } catch (Exception e) {
            libraryName = "ErrorWhileGenerated" + index;
        }
        return libraryName;
    }

    private void resolveContext(String libraryName, String header) {
        String cql = context.buildCql(libraryName, "1.0.0", "FHIR", "4.0.1", "Patient", header);
        libraries.put(libraryName, cql);
        context.cqlStack.push(cql);
        index++;
        context.printMap.clear();
    }
}
