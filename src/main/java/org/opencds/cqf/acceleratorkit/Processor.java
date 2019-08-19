package org.opencds.cqf.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.opencds.cqf.Operation;
import org.opencds.cqf.terminology.CodeSystemLookupDictionary;
import org.opencds.cqf.terminology.SpreadsheetHelper;
import org.opencds.cqf.terminology.ValueSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Bryn on 8/18/2019.
 */
public class Processor extends Operation {
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    // Data Elements
    private String dataElementPages; // -dataelementpages (-dep) comma-separated list of the names of pages in the workbook to be processed

    private Map<String, DictionaryElement> elementMap = new HashMap<String, DictionaryElement>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/acceleratorkit/output"); // default

        for (String arg : args) {
            if (arg.equals("-ProcessAcceleratorKit")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break;
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                case "dataelementpages": case "dep": dataElementPages = value; break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        for (String page : dataElementPages.split(",")) {
            processDataElementPage(workbook, page);
        }
    }

    private DictionaryCode getCode(String label, Row row, HashMap<String, Integer> colIds) {
        DictionaryCode code = new DictionaryCode();
        code.setLabel(label);
        code.setOpenMRSEntityParent(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityParent")));
        code.setOpenMRSEntity(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntity")));
        code.setOpenMRSEntityId(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityId")));
        return code;
    }

    private int getColId(HashMap<String, Integer> colIds, String colName) {
        if (colIds.containsKey(colName)) {
            return colIds.get(colName);
        }

        return -1;
    }

    private void processDataElementPage(Workbook workbook, String page) {
        Sheet sheet = workbook.getSheet(page);
        Iterator<Row> it = sheet.rowIterator();
        HashMap<String, Integer> colIds = new HashMap<String, Integer>();
        DictionaryElement currentElement = null;
        while (it.hasNext()) {
            Row row = it.next();
            // Skip header row
            if (row.getRowNum() == 0) {
                Iterator<Cell> colIt = row.cellIterator();
                while (colIt.hasNext()) {
                    Cell cell = colIt.next();
                    String header = SpreadsheetHelper.getCellAsString(cell).toLowerCase();
                    switch (header) {
                        case "label": colIds.put("Label", cell.getColumnIndex()); break;
                        case "group": colIds.put("Group", cell.getColumnIndex()); break;
                        case "name": colIds.put("Name", cell.getColumnIndex()); break;
                        case "due": colIds.put("Due", cell.getColumnIndex()); break;
                        case "frequency": colIds.put("Due", cell.getColumnIndex()); break;
                        case "relevance": colIds.put("Relevance", cell.getColumnIndex()); break;
                        case "info icon": colIds.put("InfoIcon", cell.getColumnIndex()); break;
                        case "description": colIds.put("Description", cell.getColumnIndex()); break;
                        case "notes": colIds.put("Notes", cell.getColumnIndex()); break;
                        case "type": colIds.put("Type", cell.getColumnIndex()); break;
                        case "choices": colIds.put("Choices", cell.getColumnIndex()); break;
                        case "calculation": colIds.put("Calculation", cell.getColumnIndex()); break;
                        case "constraint": colIds.put("Constraint", cell.getColumnIndex()); break;
                        case "required": colIds.put("Required", cell.getColumnIndex()); break;
                        case "editable": colIds.put("Editable", cell.getColumnIndex()); break;
                        case "openmrs entity parent": colIds.put("OpenMRSEntityParent", cell.getColumnIndex()); break;
                        case "openmrs entity": colIds.put("OpenMRSEntity", cell.getColumnIndex()); break;
                        case "openmrs entity id": colIds.put("OpenMRSEntityId", cell.getColumnIndex()); break;
                    }
                }
                continue;
            }

            String name = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Name"));
            if (name == null || name.isEmpty())
            {
                if (currentElement != null) {
                    String choices = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Choices"));
                    if (choices != null && !choices.isEmpty()) {
                        DictionaryCode code = getCode(choices, row, colIds);
                        currentElement.getChoices().add(code);
                    }
                }
                continue;
            }

            if (name.equals("NA")) {
                // TODO: Toaster message: create PlanDefinition
                continue;
            }

            if (currentElement == null || !currentElement.getName().equals(name)) {
                currentElement = new DictionaryElement(name);
                elementMap.put(name, currentElement);

                // Population based on the row:
                currentElement.setPage(page);
                //currentElement.setGroup(group);
                currentElement.setLabel(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Label")));
                currentElement.setInfoIcon(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "InfoIcon")));
                currentElement.setDue(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Due")));
                currentElement.setRelevance(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Relevance")));
                currentElement.setDescription(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Description")));
                currentElement.setNotes(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Notes")));
                currentElement.setType(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Type")));
                currentElement.setCalculation(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Calculation")));
                currentElement.setConstraint(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Constraint")));
                currentElement.setRequired(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Required")));
                currentElement.setEditable(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Editable")));
                currentElement.setCode(getCode(name, row, colIds));
            }
        }
    }
}
