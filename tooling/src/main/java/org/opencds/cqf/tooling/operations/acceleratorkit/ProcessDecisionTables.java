package org.opencds.cqf.tooling.operations.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.TriggerDefinition;
import org.hl7.fhir.r4.model.UsageContext;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opencds.cqf.tooling.utilities.IOUtils.ensurePath;

@Operation(name = "ProcessDecisionTables")
public class ProcessDecisionTables implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(ProcessDecisionTables.class);
   @OperationParam(alias = { "pts", "pathtospreadsheet" }, setter = "setPathToSpreadsheet", required = true)
   private String pathToSpreadsheet;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json")
   private String encoding;
   @OperationParam(alias = { "dtp", "decisiontablepages" }, setter = "setDecisionTablePages",
           description = "comma-separated list of the names of pages in the workbook to be processed")
   private String decisionTablePages;
   @OperationParam(alias = { "dtpf", "decisiontablepageprefix" }, setter = "setDecisionTablePagePrefix",
           description = "all pages with a name starting with this prefix will be processed")
   private String decisionTablePagePrefix;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           defaultValue = "src/main/resources/org/opencds/cqf/tooling/acceleratorkit/output")
   private String outputPath;

   private static final String CANONICAL_BASE = "http://fhir.org/guides/who/anc-cds";
   private final String newLine = System.lineSeparator();

   private final Map<String, PlanDefinition> planDefinitions = new LinkedHashMap<>();
   private final Map<String, Library> libraries = new LinkedHashMap<>();
   private final Map<String, StringBuilder> libraryCQL = new LinkedHashMap<>();
   private final Map<String, Coding> activityMap = new LinkedHashMap<>();
   private final Map<String, Integer> expressionNameCounterMap = new HashMap<>();

   @Override
   public void execute() {
      Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
      processWorkbook(workbook);
   }

   private void processWorkbook(Workbook workbook) {
      ensurePath(outputPath);

      // process workbook
      if (decisionTablePages != null && !decisionTablePages.isEmpty()) {
         for (String page : decisionTablePages.split(",")) {
            processDecisionTablePage(workbook, page);
         }
      }

      if (decisionTablePagePrefix != null && !decisionTablePagePrefix.isEmpty()) {
         Iterator<Sheet> sheets = workbook.sheetIterator();
         while (sheets.hasNext()) {
            Sheet sheet = sheets.next();
            if (sheet.getSheetName() != null && sheet.getSheetName().startsWith(decisionTablePagePrefix)) {
               processDecisionTableSheet(sheet);
            }
         }
      }

      writePlanDefinitions(outputPath);
      writePlanDefinitionIndex(outputPath);
      writeLibraries(outputPath);
      writeLibraryCQL(outputPath);
   }

   private void processDecisionTablePage(Workbook workbook, String page) {
      Sheet sheet = workbook.getSheet(page);
      if (sheet == null) {
         logger.warn("Sheet {} not found in the Workbook, so no processing was done.", page);
      }
      else {
         logger.info("Processing Sheet {}.", page);
         processDecisionTableSheet(sheet);
      }
   }

   private void processDecisionTableSheet(Sheet sheet) {
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
            String cellValue = cell.getStringCellValue().toLowerCase();
            if (cellValue.startsWith("decision")) {
               PlanDefinition planDefinition = processDecisionTable(it, cells);
               planDefinitions.put(planDefinition.getId(), planDefinition);
               generateLibrary(planDefinition);
               break;
            }
         }
      }
   }

   private PlanDefinition processDecisionTable(Iterator<Row> it, Iterator<Cell> cells) {
      PlanDefinition planDefinition = new PlanDefinition();

      if (!cells.hasNext()) {
         throw new IllegalArgumentException("Expected decision title cell");
      }

      Cell cell = cells.next();
      int headerInfoColumnIndex = cell.getColumnIndex();
      String decisionTitle = cell.getStringCellValue().trim();
      int index = decisionTitle.indexOf(' ');
      if (index < 0) {
         throw new IllegalArgumentException("Expected business rule title of the form '<ID> <Title>'");
      }
      String decisionIdentifier = decisionTitle.substring(0, index);
      String decisionId = decisionIdentifier.replace(".", "");

      planDefinition.setTitle(decisionTitle);

      Identifier planDefinitionIdentifier = new Identifier();
      planDefinitionIdentifier.setUse(Identifier.IdentifierUse.OFFICIAL);
      planDefinitionIdentifier.setValue(decisionIdentifier);
      planDefinition.getIdentifier().add(planDefinitionIdentifier);

      planDefinition.setName(decisionId);
      planDefinition.setId(decisionId);
      planDefinition.setUrl(CANONICAL_BASE + "/PlanDefinition/" + decisionId);

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
      planDefinition.setType(new CodeableConcept().addCoding(new Coding().setSystem(
              "http://terminology.hl7.org/CodeSystem/plan-definition-type").setCode("eca-rule")));

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

      Coding activityCoding = getActivityCoding(triggerName);
      if (activityCoding != null) {
         planDefinition.addUseContext(new UsageContext()
                 .setCode(new Coding()
                         .setCode("task")
                         .setSystem("http://terminology.hl7.org/CodeSystem/usage-context-type")
                         .setDisplay("Workflow Task")
                 ).setValue(new CodeableConcept().addCoding(activityCoding)));
      }

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
         if (cell.getStringCellValue().toLowerCase().startsWith("input")
                 || cell.getStringCellValue().toLowerCase().startsWith("inputs")
                 || cell.getStringCellValue().toLowerCase().startsWith("input(s)")) {
            inputColumnIndex = cell.getColumnIndex();
         }
         else if (cell.getStringCellValue().toLowerCase().startsWith("output")
                 || cell.getStringCellValue().toLowerCase().startsWith("outputs")
                 || cell.getStringCellValue().toLowerCase().startsWith("output(s)")) {
            outputColumnIndex = cell.getColumnIndex();
         }
         else if (cell.getStringCellValue().toLowerCase().startsWith("action")
                 || cell.getStringCellValue().toLowerCase().startsWith("actions")
                 || cell.getStringCellValue().toLowerCase().startsWith("action(s)")) {
            actionColumnIndex = cell.getColumnIndex();
         }
         else if (cell.getStringCellValue().toLowerCase().startsWith("annotation")
                 || cell.getStringCellValue().toLowerCase().startsWith("annotations")
                 || cell.getStringCellValue().toLowerCase().startsWith("annotation(s)")) {
            annotationColumnIndex = cell.getColumnIndex();
         }
         else if (cell.getStringCellValue().toLowerCase().startsWith("reference")
                 || cell.getStringCellValue().toLowerCase().startsWith("references")
                 || cell.getStringCellValue().toLowerCase().startsWith("reference(s)")) {
            referenceColumnIndex = cell.getColumnIndex();
            break;
         }
      }

      int actionId = 1;
      PlanDefinition.PlanDefinitionActionComponent currentAction = null;
      String currentAnnotationValue = null;
      for (;;) {
         PlanDefinition.PlanDefinitionActionComponent subAction = processAction(it, inputColumnIndex, outputColumnIndex,
                 actionColumnIndex, annotationColumnIndex, actionId, currentAnnotationValue, referenceColumnIndex);

         if (subAction == null) {
            break;
         }

         if (!actionsEqual(currentAction, subAction)) {
            actionId++;
            currentAction = subAction;

            Integer nextCounter = 1;
            String actionDescription = subAction.getAction().size() > 1
                    ? subAction.getAction().get(0).getTitle().replace(newLine, "")
                    : subAction.getDescription();
            if (!expressionNameCounterMap.containsKey(actionDescription)) {
               expressionNameCounterMap.put(actionDescription, 1);
            }

            nextCounter = expressionNameCounterMap.get(actionDescription);
            expressionNameCounterMap.put(actionDescription, nextCounter + 1);

            actionDescription = actionDescription + (nextCounter > 1 ? String.format(" %s", nextCounter) : "");
            subAction.setDescription(actionDescription);

            currentAnnotationValue = subAction.getTextEquivalent();
            action.getAction().add(subAction);
         }
         else {
            mergeActions(currentAction, subAction);
         }
      }

      return planDefinition;
   }

   private void generateLibrary(PlanDefinition planDefinition) {
      String id = planDefinition.getIdElement().getIdPart();

      Library library = new Library();
      library.getIdentifier().add(planDefinition.getIdentifierFirstRep());
      library.setId(id);
      library.setName(planDefinition.getName());
      library.setUrl(CANONICAL_BASE + "/Library/" + id);
      library.setTitle(planDefinition.getTitle());
      library.setDescription(planDefinition.getDescription());
      library.addContent((Attachment)new Attachment().setId("ig-loader-" + id + ".cql"));

      planDefinition.getLibrary().add((CanonicalType)new CanonicalType().setValue(library.getUrl()));

      StringBuilder cql = new StringBuilder();
      writeLibraryHeader(cql, library);

      for (PlanDefinition.PlanDefinitionActionComponent action : planDefinition.getActionFirstRep().getAction()) {
         if (action.hasCondition()) {
            writeActionCondition(cql, action);
         }
      }

      libraries.put(id, library);
      libraryCQL.put(id, cql);
   }

   private Coding getActivityCoding(String activityId) {
      if (activityId == null || activityId.isEmpty()) {
         return null;
      }

      int i = activityId.indexOf(" ");
      if (i <= 1) {
         return null;
      }

      String activityCode = activityId.substring(0, i);
      String activityDisplay = activityId.substring(i + 1);

      if (activityDisplay.isEmpty()) {
         return null;
      }

      Coding activity = activityMap.get(activityCode);

      if (activity == null) {
         String activityCodeSystem = "http://fhir.org/guides/who/anc-cds/CodeSystem/activity-codes";
         activity = new Coding().setCode(activityCode).setSystem(activityCodeSystem).setDisplay(activityDisplay);
         activityMap.put(activityCode, activity);
      }

      return activity;
   }

   private PlanDefinition.PlanDefinitionActionComponent processAction(
           Iterator<Row> it, int inputColumnIndex, int outputColumnIndex, int actionColumnIndex,
           int annotationColumnIndex, int actionId, String currentAnnotationValue, int referenceColumnIndex) {
      if (it.hasNext()) {
         Row row = it.next();
         // If the row is not valid, do not process it.
         if (!rowIsValid(row, inputColumnIndex, actionColumnIndex, annotationColumnIndex)) {
            return null;
         }

         Cell cell;
         PlanDefinition.PlanDefinitionActionComponent action = new PlanDefinition.PlanDefinitionActionComponent();

         action.setId(Integer.toString(actionId));

         List<String> conditionValues = new ArrayList<>();
         for (int inputIndex = inputColumnIndex; inputIndex < outputColumnIndex; inputIndex++) {
            cell = row.getCell(inputIndex);
            if (cell != null) {
               String inputCondition = cell.getStringCellValue();
               if (inputCondition != null && !inputCondition.isEmpty() && !inputCondition.toLowerCase().startsWith("decision")) {
                  conditionValues.add(inputCondition);
               }
            }
         }

         if (conditionValues.isEmpty()) {
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
                  applicabilityCondition.append(String.format("%n  AND "));
               }
               applicabilityCondition.append("(");
               applicabilityCondition.append(conditionValue);
               applicabilityCondition.append(")");
            }
         }

         PlanDefinition.PlanDefinitionActionConditionComponent condition = new PlanDefinition.PlanDefinitionActionConditionComponent();
         condition.setKind(PlanDefinition.ActionConditionKind.APPLICABILITY);
         condition.setExpression(new Expression().setLanguage("text/cql-identifier").setDescription(applicabilityCondition.toString()));
         action.getCondition().add(condition);

         List<String> actionValues = new ArrayList<>();
         for (int actionIndex = actionColumnIndex; actionIndex < annotationColumnIndex; actionIndex++) {
            cell = row.getCell(actionIndex);
            if (cell != null) {
               String actionValue = cell.getStringCellValue();
               if (actionValue != null && !actionValue.isEmpty()) {
                  actionValues.add(actionValue.replace(System.getProperty(newLine), ""));
               }
            }
         }

         String actionsDescription = String.join(" AND ", actionValues);
         action.setDescription(actionsDescription);

         if (actionValues.size() == 1) {
            action.setTitle(actionValues.get(0).replace(System.getProperty("line.separator"), ""));
         }
         else {
            action.setTitle(actionValues.get(0).replace(System.getProperty("line.separator"), ""));
            for (String actionValue : actionValues) {
               PlanDefinition.PlanDefinitionActionComponent subAction =
                       new PlanDefinition.PlanDefinitionActionComponent();
               subAction.setTitle(actionValue);
               action.getAction().add(subAction);
            }
         }

         if (annotationColumnIndex >= 0) {
            cell = row.getCell(annotationColumnIndex);
            if (cell != null) {
               String annotationValue = cell.getStringCellValue();
               if (annotationValue != null && !annotationValue.isEmpty()) {
                  currentAnnotationValue = annotationValue;
               }
            }
         }

         // TODO: Might not want to duplicate this so much?
         action.setTextEquivalent(currentAnnotationValue);

         // TODO: Link this to the RelatedArtifact for References
         if (referenceColumnIndex >= 0) {
            cell = row.getCell(referenceColumnIndex);
            if (cell != null) {
               // TODO: Should this be set to the reference from the previous line?
               String referenceValue = cell.getStringCellValue();
               RelatedArtifact relatedArtifact = new RelatedArtifact();
               relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.CITATION);
               relatedArtifact.setLabel(referenceValue);
               action.getDocumentation().add(relatedArtifact);
            }
         }

         return action;
      }

      return null;
   }

   // Returns true if the given actions are equal (i.e. are for the same thing, meaning they have the same title, textEquivalent, description, and subactions)
   private boolean actionsEqual(PlanDefinition.PlanDefinitionActionComponent currentAction, PlanDefinition.PlanDefinitionActionComponent newAction) {
      if (currentAction == null) {
         return false;
      }

      List<PlanDefinition.PlanDefinitionActionComponent> currentActionSubs = currentAction.getAction();
      List<PlanDefinition.PlanDefinitionActionComponent> newActionSubs = newAction.getAction();


      String currentActionDescription = currentActionSubs.stream().map(PlanDefinition.PlanDefinitionActionComponent::getTitle)
              .collect(Collectors.joining(" AND "));
      String newActionDescription = newActionSubs.stream().map(PlanDefinition.PlanDefinitionActionComponent::getTitle)
              .collect(Collectors.joining(" AND "));

      return stringsEqual(currentAction.getTitle(), newAction.getTitle())
              && stringsEqual(currentAction.getTextEquivalent(), newAction.getTextEquivalent())
              && stringsEqual(currentActionDescription, newActionDescription)
              && subActionsEqual(currentAction.getAction(), newAction.getAction());
   }

   // Merge action conditions as an Or, given that the actions are equal
   private void mergeActions(PlanDefinition.PlanDefinitionActionComponent currentAction,
                             PlanDefinition.PlanDefinitionActionComponent newAction) {
      PlanDefinition.PlanDefinitionActionConditionComponent currentCondition = currentAction.getConditionFirstRep();
      PlanDefinition.PlanDefinitionActionConditionComponent newCondition = newAction.getConditionFirstRep();

      if (currentCondition == null) {
         currentAction.getCondition().add(newCondition);
      }
      else if (newCondition != null) {
         currentCondition.getExpression().setDescription(String.format("(%s)%n  OR (%s)",
                 currentCondition.getExpression().getDescription(), newCondition.getExpression().getDescription()));
      }
   }

   private void writePlanDefinitions(String outputPath) {
      if (planDefinitions != null && planDefinitions.size() > 0) {
         for (PlanDefinition planDefinition : planDefinitions.values()) {
            String outputFilePath = outputPath + File.separator + "input" + File.separator + "resources" +
                    File.separator + "plandefinition";
            ensurePath(outputFilePath);
            writeResource(outputFilePath, planDefinition);
         }
      }
   }

   public void writePlanDefinitionIndex(String outputPath) {
      String outputFilePath = outputPath + File.separator + "input" + File.separator + "pagecontent"+
              File.separator + "PlanDefinitionIndex.md";
      ensurePath(outputFilePath);

      try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
         writer.write(buildPlanDefinitionIndex().getBytes());
         writer.flush();
      }
      catch (IOException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("Error writing plandefinition index");
      }
   }

   private void writeLibraries(String outputPath) {
      if (libraries.size() > 0) {
         String outputFilePath = outputPath + File.separator + "input" + File.separator + "resources" +
                 File.separator + "library";
         ensurePath(outputFilePath);

         for (Library library : libraries.values()) {
            writeResource(outputFilePath, library);
         }
      }
   }

   private void writeLibraryCQL(String outputPath) {
      if (libraryCQL.size() > 0) {
         for (Map.Entry<String, StringBuilder> entry : libraryCQL.entrySet()) {
            String outputDirectoryPath = outputPath + File.separator + "input" + File.separator + "cql";
            String outputFilePath = outputDirectoryPath + File.separator + entry.getKey() + ".cql";
            ensurePath(outputDirectoryPath);

            try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
               writer.write(entry.getValue().toString().getBytes());
               writer.flush();
            }
            catch (IOException e) {
               e.printStackTrace();
               throw new IllegalArgumentException("Error writing CQL: " + entry.getKey());
            }
         }
      }
   }

   private void writeLibraryHeader(StringBuilder cql, Library library) {
      cql.append("library ").append(library.getName());
      cql.append(newLine).append(newLine);
      cql.append("using FHIR version '4.0.1'");
      cql.append(newLine).append(newLine);
      cql.append("include FHIRHelpers version '4.0.1'");
      cql.append(newLine).append(newLine);
      cql.append("include ANCConfig called Config");
      cql.append(newLine);
      cql.append("include ANCConcepts called Cx");
      cql.append(newLine);
      cql.append("include ANCDataElements called PatientData");
      cql.append(newLine).append(newLine);
      cql.append("context Patient");
      cql.append(newLine).append(newLine);
   }

   private void writeActionCondition(StringBuilder cql, PlanDefinition.PlanDefinitionActionComponent action) {
      PlanDefinition.PlanDefinitionActionConditionComponent condition = action.getConditionFirstRep();
      if (condition.getExpression().getExpression() == null) {
         condition.getExpression().setExpression(
                 String.format("Should %s", action.hasDescription()
                         ? action.getDescription().replace("\"", "\\\"") : "perform action"));
      }
      cql.append("/*");
      cql.append(newLine);
      cql.append(action.getConditionFirstRep().getExpression().getDescription());
      cql.append(newLine);
      cql.append("*/");
      cql.append(newLine);
      cql.append(String.format("define \"%s\":%n", action.getConditionFirstRep().getExpression().getExpression()));
      cql.append("  false"); // Output false, manual process to convert the pseudo-code to CQL
      cql.append(newLine);
      cql.append(newLine);
   }

   private boolean rowIsValid(Row row, int inputColumnIndex, int actionColumnIndex, int annotationColumnIndex) {
      // Currently considered "valid" if any of the four known columns have a non-null, non-empty string value.
      int[] valueColumnIndexes = new int[] { inputColumnIndex, actionColumnIndex, annotationColumnIndex };

      for (int i=0; i < valueColumnIndexes.length - 1; i++) {
         int columnIndex = valueColumnIndexes[i];
         Cell cell = row.getCell(columnIndex);
         if (cell != null) {
            String columnValueString = cell.getStringCellValue();
            if (columnValueString == null || columnValueString.isEmpty()) {
               return false;
            }
         } else {
            return false;
         }

      }

      return true;
   }

   private boolean stringsEqual(String left, String right) {
      return (left == null && right == null) || (left != null && left.equals(right));
   }

   private boolean subActionsEqual(List<PlanDefinition.PlanDefinitionActionComponent> left,
                                   List<PlanDefinition.PlanDefinitionActionComponent> right) {
      if (left == null && right == null) {
         return true;
      }

      if (left != null && right != null) {
         for (int leftIndex = 0; leftIndex < left.size(); leftIndex++) {
            if (leftIndex >= right.size()) {
               return false;
            }
            if (!actionsEqual(left.get(leftIndex), right.get(leftIndex))) {
               return false;
            }
         }
         return true;
      }
      // One has a list, the other doesn't
      return false;
   }

   /* Write Methods */
   public void writeResource(String path, Resource resource) {
      String outputFilePath = path + File.separator + resource.getResourceType().toString().toLowerCase() +
              "-" + resource.getIdElement().getIdPart() + "." + encoding;
      try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
         writer.write(
                 encoding.equals("json")
                         ? FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true).encodeResourceToString(
                                 resource).getBytes()
                         : FhirContext.forR4Cached().newXmlParser().setPrettyPrint(true).encodeResourceToString(
                                 resource).getBytes()
         );
         writer.flush();
      } catch (IOException e) {
         e.printStackTrace();
         throw new IllegalArgumentException("Error writing resource: " + resource.getIdElement().getIdPart());
      }
   }

   private String buildPlanDefinitionIndex() {
      StringBuilder index = new StringBuilder().append("### Plan Definitions by Decision ID");
      index.append(newLine).append(newLine);
      index.append("|Decision Table|Description|");
      index.append(newLine).append("|---|---|").append(newLine);

      for (PlanDefinition pd : planDefinitions.values()) {
         index.append(String.format("|[%s](PlanDefinition-%s.html)|%s|",
                 pd.getTitle(), pd.getId(), pd.getDescription()));
         index.append(newLine);
      }

      return index.toString();
   }

   public String getPathToSpreadsheet() {
      return pathToSpreadsheet;
   }

   public void setPathToSpreadsheet(String pathToSpreadsheet) {
      this.pathToSpreadsheet = pathToSpreadsheet;
   }

   public String getEncoding() {
      return encoding;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public String getDecisionTablePages() {
      return decisionTablePages;
   }

   public void setDecisionTablePages(String decisionTablePages) {
      this.decisionTablePages = decisionTablePages;
   }

   public String getDecisionTablePagePrefix() {
      return decisionTablePagePrefix;
   }

   public void setDecisionTablePagePrefix(String decisionTablePagePrefix) {
      this.decisionTablePagePrefix = decisionTablePagePrefix;
   }

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputPath) {
      this.outputPath = outputPath;
   }
}
