package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.GuidanceResponse;
import org.hl7.fhir.r4.model.Reference;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase.DataElement;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase.TestCase;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseGenerator extends Operation {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseGenerator.class);

    private final FhirContext ctx = FhirContext.forR4Cached();
    private final IParser parser = ctx.newJsonParser();
    private final List<GuidanceResponse> guidanceResponses = new ArrayList<>();

    // Constants
    private final String INPUTPARAMURL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/inputParameters";

    // Flag values
    private String logicInputPath;
    private String dictInputPath;
    private String inputPath;
    private String outputPath = "src/main/resources/org/opencds/cqf/testcases/output";
    private String outputPrefix = "case-";
    private final String fileFormat = "xlsx";

    // Spreadsheet related
    private Workbook logicWorkbook;
    private Workbook dictWorkbook;
    private Workbook inputWorkbook;

    private HashMap<String, Integer> sortedLogicSheets;
    private HashMap<String, Integer> sortedDictSheets;

    private final String[] sheetOperands = { "if", "=", "-and/or-", "on more than one contact", ","};
    private final String[] dciPossibleInputs = {
            "More than 2 cups of coffee (brewed, filter, instant or espresso)",
            "More than 4 cups of tea",
            "More than 12 bars (50g) of chocolate",
            "More than one can of soda or energy drink",
            "None of the above daily caffeine intake",
    };

    @Override
    public void execute(String[] args) {


        // Parsing run args
        for (String arg : args) {
            if (arg.equals("-GenerateTestCase")) continue;

            String flag = arg.split("=")[0];
            String value = arg.split("=")[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":        case "op":  outputPath     = value; break;
                case "outputprefix":      case "opp": outputPrefix   = value; break;
                case "logicinputpath":    case "lip": logicInputPath = value; break;
                case "dictinputpath":     case "dip": dictInputPath  = value; break;
                case "input":             case "in":  inputPath      = value; break;

                default: throw new IllegalArgumentException("Unknown -> " + flag);
            }
        }

        // Instantiating workbook objects from user args
        logicWorkbook = SpreadsheetHelper.getWorkbook(logicInputPath);
        dictWorkbook  = SpreadsheetHelper.getWorkbook(dictInputPath);
        inputWorkbook = SpreadsheetHelper.getWorkbook(inputPath);

        Sheet logicSheet = logicWorkbook.getSheet("ANC.DT.15 Behaviour counselling");
        String activityId = Helper.getIdFromTrigger(logicSheet);

        HashMap<String, TestCase> caseMap     = getInputTestCases(inputWorkbook.getSheet("Test Case List"));
        HashMap<String, DataElement> logicMap = getInputLogic(inputWorkbook.getSheet("Test Case Logic"));
        performLogic(caseMap, logicMap);

        /*
         * Problem: Input looks like "Daily caffeine intake" = "More than 2 cups of coffee (brewed, filter, instant or espresso)".
         * Want: That to be "dci-more-than-2-cups-of-coffee-brewed-filter-instant-or-espresso" while still being identifiable by
         * the original unaltered input string.
         * Solution: Hashmap logic below.
         */
        HashMap<String, String>logicInputCorrelations = buildInputCorrelatedHashmap(logicSheet, true, 5);

        for (Map.Entry<String, String> i : logicInputCorrelations.entrySet())
            logger.info("{} : {}", i.getKey(), i.getValue());

        sortedLogicSheets = Helper.getSortedSheets(logicWorkbook);
        sortedDictSheets  = Helper.getSortedSheets(dictWorkbook);
        Sheet triggerDict = dictWorkbook.getSheet(Helper.substrToSheetName(sortedDictSheets, "ANC.B10"));
        HashMap<String, String> correlatedMap = buildInputCorrelatedHashmap(logicSheet, true, 5);



    }

    private HashMap<String, TestCase> getInputTestCases(Sheet listSheet) {
        HashMap<String, TestCase> testCases = new HashMap<>();
        Iterator<Row> rowIterator = listSheet.rowIterator();

        // Skip 0 and 1 rows.
        for (int i = 0; i < 2; i++) rowIterator.next();


        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            TestCase tmpCase = new TestCase();

            if (Helper.isRowEmpty(currentRow)) continue;

            tmpCase.setId(String.valueOf(Helper.properlyGetCell(currentRow, 0).getNumericCellValue()));
            tmpCase.setName(Helper.properlyGetCell(currentRow, 1).getStringCellValue());
            tmpCase.setDescription(Helper.properlyGetCell(currentRow, 2).getStringCellValue());
            tmpCase.setInput(Helper.properlyGetCell(currentRow, 3).getStringCellValue());

            testCases.put(tmpCase.getInput(), tmpCase);
        }
        return testCases;
    }

    private HashMap<String, DataElement> getInputLogic(Sheet logicSheet) {
        HashMap<String, DataElement> logicMap = new HashMap<>();
        Iterator<Row> rowIterator = logicSheet.rowIterator();

        // Skip 0 and 1 rows.
        for (int i = 0; i < 2; i++) rowIterator.next();

        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
//            ElementDefinition elementDefinition = new ElementDefinition();
            DataElement elementDefinition = new DataElement();

            if (Helper.isRowEmpty(currentRow))
                continue;

            elementDefinition.setId(Helper.properlyGetCell(currentRow, 0).getStringCellValue());
//            elementDefinition.setLabel(new org.hl7.fhir.String().withValue(TestCaseGenerator.properlyGetCell(currentRow, 1).getStringCellValue()));
            elementDefinition.setLabel(Helper.properlyGetCell(currentRow, 1).getStringCellValue());
//            elementDefinition.setValue(new org.hl7.fhir.String().withValue(TestCaseGenerator.properlyGetCell(currentRow, 2).getStringCellValue()));
            elementDefinition.setValue(Helper.properlyGetCell(currentRow, 2).getStringCellValue());

//            DataElement dataElement = new DataElement().withElement(elementDefinition);

            logicMap.put(elementDefinition.getId(), elementDefinition);
        }

        return logicMap;
    }
    /**
     * Iterates over index 1 in given sheet.
     * Constructs HashMap {UnalteredCellString: AlteredCellString}
     * @param sheet
     * @param rowSkipCount
     * @return
     */
    private HashMap<String, String> buildInputCorrelatedHashmap(Sheet sheet, boolean shouldRemoveOperands, int rowSkipCount) {
        HashMap<String, String> ret = new HashMap<>();

        Iterator<Row> rowIterator = skipIterations(sheet.rowIterator(), 5);
        while (rowIterator.hasNext()) {
           Row currentRow = rowIterator.next();
           Cell inputCell;
           String unalteredInput;
           String alteredInput;

           if (Helper.isRowEmpty(currentRow)) continue;

            inputCell = Helper.properlyGetCell(currentRow, 1);
            unalteredInput = inputCell.getStringCellValue().toLowerCase();

            alteredInput = removeOperands(unalteredInput);

            ret.put(unalteredInput, alteredInput);
            buildGuidanceResponse(currentRow, alteredInput);
        }
        return ret;
    }

    private void performLogic(HashMap<String, TestCase> caseMap, HashMap<String, DataElement> logicMap) {
        for (Map.Entry<String, TestCase> testCase : caseMap.entrySet()) {
            DataElement relativeDataElement = logicMap.get(testCase.getValue().getInput());
            DataElement expectedResult = new DataElement();
            String counselType = getCounselType(relativeDataElement.getLabel());
            TestCase copyTestCase = testCase.getValue();

            // TODO: Rather than hard-coding these I plan on pulling them automatically from the spreadsheet itself.
            if (counselType.equalsIgnoreCase("caffeine"))
                expectedResult.setValue("Conduct counseling on caffeine reduction");
            else if (counselType.equalsIgnoreCase("tobacco"))
                expectedResult.setValue("Conduct counseling on tobacco cessation");
            else if (counselType.equalsIgnoreCase("condom"))
                expectedResult.setValue("Conduct counselling on condom use");
            else if (counselType.equalsIgnoreCase("alcohol"))
                expectedResult.setValue("Conduct counseling on alcohol/substance use");
            else if (counselType.equalsIgnoreCase("smoke"))
                expectedResult.setValue("Conduct counseling on second-hand smoke");

            expectedResult.setLabel(relativeDataElement.getLabel());
            expectedResult.setId(relativeDataElement.getId());
            copyTestCase.setExpectedResult(expectedResult);
            copyTestCase.setInputDataElement(relativeDataElement);
            output(copyTestCase);
        }
    }

    private void buildGuidanceResponse(Row row, String alteredInput) {
        GuidanceResponse guidanceResponse = new GuidanceResponse();
        Cell nextCell = Helper.properlyGetCell(row, 1);

        if (alteredInput.charAt(0) == '-')
            guidanceResponse.setId("patient" + alteredInput);
        else
            guidanceResponse.setId("patient-" + alteredInput);

        guidanceResponse.addExtension(new Extension()
                .setUrl(INPUTPARAMURL)
                .setValue(new Reference("Parameters/patient" + alteredInput))
        );
        // I don't understand this 100%. Talk to Bryn or someone.
        // Surely this must be the expected result given a specific reference..?
        guidanceResponse.setStatus(GuidanceResponse.GuidanceResponseStatus.fromCode("success"));
        guidanceResponse.setSubject(new Reference("Patient/patient" + alteredInput));
        guidanceResponse.setOutputParameters(new Reference("Parameters/patient" + alteredInput + "-output"));

        guidanceResponses.add(guidanceResponse);
    }

    private String getCounselType(String checkAgainst) {
        String ret = "";

        if (checkAgainst.toLowerCase().contains("tobacco"))
            ret = "tobacco";
        else if (checkAgainst.toLowerCase().contains("caffeine"))
            ret = "caffeine";
        else if (checkAgainst.toLowerCase().contains("condom"))
            ret = "condom";
        else if (checkAgainst.toLowerCase().contains("alcohol"))
            ret = "alcohol";
        else if (checkAgainst.toLowerCase().contains("smoke"))
            ret = "smoke";

        return ret;
    }

    private boolean shouldCounselCaffeine(String checkAgainst) {
        boolean isDci = false;
        for (String possibility : dciPossibleInputs) {
            if (checkAgainst.equalsIgnoreCase(possibility)) {
                isDci = true;
                break;
            }
        }
        return isDci;
    }

    private void output(TestCase testCase) {
        String guidanceResponsePath = outputPath + "/testCase-" + testCase.getId().replace("/", "-").toLowerCase() + ".json";
        logger.info(testCase.getId());

        if (guidanceResponsePath.contains("="))
            guidanceResponsePath = guidanceResponsePath.replace("=", "");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileOutputStream fStream = new FileOutputStream(guidanceResponsePath)) {
            fStream.write(gson.toJson(testCase).getBytes());
            fStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String removeInputFormatting(String input) {
        return input
                .replace("\"", "")
                .replace(" ", "-")
                .replace("(", "-")
                .replace(")", "");
    }

    private Iterator<Row> skipIterations(Iterator<Row> iterator, int count) {
        for (int i = 0; i < count; i++) {
            iterator.next();
        }
        return iterator;
    }

    public String removeOperands(String inputString) {
        inputString = removeInputFormatting(inputString);

        for (String operand : sheetOperands) {
            if (inputString.toLowerCase().contains(operand)) {
                inputString = inputString.replace(operand, "");
            }
        }

        if (inputString.contains("--"))
            inputString = inputString.replace("--", "-");

        return inputString;
    }
}
