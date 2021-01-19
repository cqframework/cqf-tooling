package org.opencds.cqf.tooling.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.w3._1999.xhtml.P;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.opencds.cqf.tooling.utilities.IOUtils.ensurePath;

public class DTProcessor extends Operation {
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    // Decision Tables
    private String decisionTablePages; // -decisiontablepages (-dtp) comma-separated list of the names of pages in the workbook to be processed

    // Canonical Base
    private String canonicalBase = null;

    private Map<String, PlanDefinition> planDefinitions = new LinkedHashMap<String, PlanDefinition>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/acceleratorkit/output"); // default
        for (String arg : args) {
            if (arg.equals("-ProcessDecisionTables")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break; // -pathtospreadsheet (-pts)
                case "encoding": case "e": encoding = value.toLowerCase(); break; // -encoding (-e)
                case "decisiontablepages": case "dtp": decisionTablePages = value; break; // -decisiontablepages (-dtp)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        canonicalBase = "http://fhir.org/guides/who/anc-cds";

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        processWorkbook(workbook);
    }

    private void processWorkbook(Workbook workbook) {
        String outputPath = getOutputPath();
        try {
            ensurePath(outputPath);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(String.format("Could not ensure output path: %s", e.getMessage()), e);
        }

        // process workbook
        for (String page : decisionTablePages.split(",")) {
            processDecisionTablePage(workbook, page);
        }

        writePlanDefinitions(outputPath);
    }

    private void processDecisionTablePage(Workbook workbook, String page) {
        Sheet sheet = workbook.getSheet(page);
        if (sheet == null) {
            System.out.println(String.format("Sheet %s not found in the Workbook, so no processing was done.", page));
        }
        else {
            /*
            Decision table general format:
            Header rows:
            | Decision ID | <Decision ID> <Decision Title> |
            | Business Rule | <Decision Description> |
            | Trigger | <Workflow Step Reference> |
            | Input(s) | ... | Output | Action | Annotation | Reference |
            | <Condition> | ... | <Action.Description> | <Action.Title> | <Action.TextEquivalent> | <Action.Document> | --> Create a row for each...
             */

            Iterator<Row> it = sheet.rowIterator();

            while (it.hasNext()) {
                Row row = it.next();

                Iterator<Cell> cells = row.cellIterator();
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    if (cell.getStringCellValue().toLowerCase().startsWith("decision")) {
                        PlanDefinition planDefinition = processDecisionTable(workbook, it, cells);
                        if (planDefinition != null) {
                            planDefinitions.put(planDefinition.getId(), planDefinition);
                        }
                        break;
                    }
                }
            }
        }
    }

    private PlanDefinition processDecisionTable(Workbook workbook, Iterator<Row> it, Iterator<Cell> cells) {
        PlanDefinition planDefinition = new PlanDefinition();

        if (!cells.hasNext()) {
            throw new IllegalArgumentException("Expected decision title cell");
        }

        Cell cell = cells.next();
        int headerInfoColumnIndex = cell.getColumnIndex();
        String decisionTitle = cell.getStringCellValue();
        int index = decisionTitle.indexOf(' ');
        if (index < 0) {
            throw new IllegalArgumentException("Expected business rule title of the form '<ID> <Title>'");
        }
        String decisionIdentifier = decisionTitle.substring(0, index);
        String decisionName = decisionTitle.substring(index + 1);
        String decisionId = decisionIdentifier.replace(".", "");

        planDefinition.setTitle(decisionTitle);

        Identifier planDefinitionIdentifier = new Identifier();
        planDefinitionIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        planDefinitionIdentifier.setValue(decisionIdentifier);
        planDefinition.getIdentifier().add(planDefinitionIdentifier);

        planDefinition.setName(decisionName);
        planDefinition.setId(decisionId);
        planDefinition.setUrl(canonicalBase + "/PlanDefinition/" + decisionId);

        if (!it.hasNext()) {
            throw new IllegalArgumentException("Expected Business Rule row");
        }

        Row row = it.next();

        Cell descriptionCell = row.getCell(headerInfoColumnIndex);
        if (descriptionCell == null) {
            throw new IllegalArgumentException("Expected Business Rule description cell");
        }

        String decisionDescription = descriptionCell.getStringCellValue();
        planDefinition.setDescription(decisionDescription);

        planDefinition.setStatus(Enumerations.PublicationStatus.ACTIVE);
        planDefinition.setDate(java.util.Date.from(Instant.now()));
        planDefinition.setExperimental(false);
        planDefinition.setType(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/plan-definition-type").setCode("eca-rule")));

        if (!it.hasNext()) {
            throw new IllegalArgumentException("Expected Trigger row");
        }

        row = it.next();

        Cell triggerCell = row.getCell(headerInfoColumnIndex);
        if (triggerCell == null) {
            throw new IllegalArgumentException("Expected Trigger description cell");
        }

        String triggerName = triggerCell.getStringCellValue();
        PlanDefinition.PlanDefinitionActionComponent action = new PlanDefinition.PlanDefinitionActionComponent();
        planDefinition.getAction().add(action);
        action.setTitle(decisionTitle);

        TriggerDefinition trigger = new TriggerDefinition();
        trigger.setType(TriggerDefinition.TriggerType.NAMEDEVENT);
        trigger.setName(triggerName);
        action.getTrigger().add(trigger);

        if (!it.hasNext()) {
            throw new IllegalArgumentException("Expected decision table header row");
        }

        row = it.next();

        cells = row.cellIterator();
        int inputColumnIndex = -1;
        int outputColumnIndex = -1;
        int actionColumnIndex = -1;
        int annotationColumnIndex = -1;
        int referenceColumnIndex = -1;
        while (cells.hasNext()) {
            cell = cells.next();
            if (cell.getStringCellValue().toLowerCase().startsWith("input")) {
                inputColumnIndex = cell.getColumnIndex();
            }
            else if (cell.getStringCellValue().toLowerCase().startsWith("output")) {
                outputColumnIndex = cell.getColumnIndex();
            }
            else if (cell.getStringCellValue().toLowerCase().startsWith("action")) {
                actionColumnIndex = cell.getColumnIndex();
            }
            else if (cell.getStringCellValue().toLowerCase().startsWith("annotation")) {
                annotationColumnIndex = cell.getColumnIndex();
            }
            else if (cell.getStringCellValue().toLowerCase().startsWith("reference")) {
                referenceColumnIndex = cell.getColumnIndex();
                break;
            }
        }

        String currentAnnotationValue = null;
        for (;;) {
            PlanDefinition.PlanDefinitionActionComponent subAction = processAction(it, inputColumnIndex, outputColumnIndex, actionColumnIndex, annotationColumnIndex, currentAnnotationValue, referenceColumnIndex);
            if (subAction == null) {
                break;
            }
            currentAnnotationValue = subAction.getTextEquivalent();
            action.getAction().add(subAction);
        }

        return planDefinition;
    }

    private PlanDefinition.PlanDefinitionActionComponent processAction(Iterator<Row> it, int inputColumnIndex,
           int outputColumnIndex, int actionColumnIndex, int annotationColumnIndex, String currentAnnotationValue,
           int referenceColumnIndex) {
        if (it.hasNext()) {
            Row row = it.next();
            Cell cell;
            PlanDefinition.PlanDefinitionActionComponent action = new PlanDefinition.PlanDefinitionActionComponent();

            List<String> conditionValues = new ArrayList<String>();
            for (int inputIndex = inputColumnIndex; inputIndex < outputColumnIndex; inputIndex++) {
                cell = row.getCell(inputIndex);
                if (cell != null) {
                    String inputCondition = cell.getStringCellValue();
                    if (inputCondition != null && !inputCondition.isEmpty() && !inputCondition.equals("") && !inputCondition.toLowerCase().startsWith("decision")) {
                        conditionValues.add(inputCondition);
                    }
                }
            }

            if (conditionValues.size() == 0) {
                // No condition, no action, end of decision table
                return null;
            }

            StringBuilder applicabilityCondition = new StringBuilder();
            if (conditionValues.size() == 1) {
                applicabilityCondition.append(conditionValues.get(0));
            }
            else {
                for (String conditionValue : conditionValues) {
                    if (applicabilityCondition.length() > 0) {
                        applicabilityCondition.append("\n  AND ");
                    }
                    applicabilityCondition.append("(");
                    applicabilityCondition.append(conditionValue);
                    applicabilityCondition.append(")");
                }
            }

            PlanDefinition.PlanDefinitionActionConditionComponent condition = new PlanDefinition.PlanDefinitionActionConditionComponent();
            condition.setKind(PlanDefinition.ActionConditionKind.APPLICABILITY);
            condition.setExpression(new Expression().setLanguage("text/x-pseudo").setDescription(applicabilityCondition.toString()));
            action.getCondition().add(condition);


            if (outputColumnIndex >= 0) {
                cell = row.getCell(outputColumnIndex);
                String outputValue = cell.getStringCellValue();
                action.setDescription(outputValue);
            }

            List<String> actionValues = new ArrayList<String>();
            for (int actionIndex = actionColumnIndex; actionIndex < annotationColumnIndex; actionIndex++) {
                cell = row.getCell(actionIndex);
                if (cell != null) {
                    String actionValue = cell.getStringCellValue();
                    if (actionValue != null && !actionValue.isEmpty() && !actionValue.equals("")) {
                        actionValues.add(actionValue);
                    }
                }
            }

            if (actionValues.size() == 1) {
                action.setTitle(actionValues.get(0));
            }
            else {
                for (String actionValue : actionValues) {
                    PlanDefinition.PlanDefinitionActionComponent subAction = new PlanDefinition.PlanDefinitionActionComponent();
                    subAction.setTitle(actionValue);
                    action.getAction().add(subAction);
                }
            }

            if (annotationColumnIndex >= 0) {
                cell = row.getCell(annotationColumnIndex);
                if (cell != null) {
                    String annotationValue = cell.getStringCellValue();
                    if (annotationValue != null && !annotationValue.isEmpty() && !annotationValue.equals("")) {
                        currentAnnotationValue = annotationValue;
                    }
                }
            }

            // TODO: Might not want to duplicate this so much?
            action.setTextEquivalent(currentAnnotationValue);

            // TODO: Link this to the RelatedArtifact for References
            if (referenceColumnIndex >= 0) {
                cell = row.getCell(referenceColumnIndex);
                String referenceValue = cell.getStringCellValue();
                RelatedArtifact relatedArtifact = new RelatedArtifact();
                relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.CITATION);
                relatedArtifact.setLabel(referenceValue);
                action.getDocumentation().add(relatedArtifact);
            }

            return action;
        }

        return null;
    }

    private void writePlanDefinitions(String outputPath) {
        if (planDefinitions != null && planDefinitions.size() > 0) {
            for (PlanDefinition planDefinition : planDefinitions.values()) {
                writeResource(outputPath, planDefinition);
            }
        }
    }

    /* Write Methods */
    public void writeResource(String path, Resource resource) {
        String outputFilePath = path + "/" + resource.getResourceType().toString().toLowerCase() + "-" + resource.getIdElement().getIdPart() + "." + encoding;
        try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
            writer.write(
                    encoding.equals("json")
                            ? FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                            : FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing resource: " + resource.getIdElement().getIdPart());
        }
    }

}
