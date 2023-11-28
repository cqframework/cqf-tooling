package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase.generator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase.TestCaseOld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);


    // Assumption made:
    // - That activity id in trigger cell is 7 in length.
    public static String getIdFromTrigger(Sheet sheet) {
        return properlyGetCell(sheet.getRow(3), 2).getStringCellValue().substring(0, 7);
    }

    // Because Row.MissingCellPolicy.CREATE_NULL_AS_BLANK makes me anger.
    public static Cell properlyGetCell(Row row, int idx) {
        return row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }

    // Assumption made:
    // - That no spreadsheet this will be ran on will have 75 populated cells in the same row.
    public static boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 75; i++) {
            if (!isCellEmpty(properlyGetCell(row, i)))
                return false;
        }
        return true;
    }

    private static String parseInputCell(Cell inputCell) {
        String ret = inputCell.getStringCellValue();
        if (ret.contains("TRUE")) return "true";
        if (ret.contains("FALSE")) return "false";

        String[] tmp = ret.split("=");
        logger.info(inputCell.getStringCellValue());
        String name = ret.split("=")[0];
        String condition = ret.split(" = ")[1];


        logger.info("{} RET RET RET", ret);
        return name;
    }

    private static boolean isCellEmpty(Cell cell) {
        boolean empty = false;
        try {
            if (cell.getStringCellValue().length() <= 0)
                empty = true;
        } catch (IllegalStateException e) {
            empty = false;
        }
        return empty;
    }

    private static int inferConditionalCount(Row row) {
        if (!isCellEmpty(Helper.properlyGetCell(row, 1)) && !isCellEmpty(properlyGetCell(row, 2))) {
            return 2;
        }
        return 1;
    }

    private static String formatSheetName(Sheet sheet) {
        return sheet.getSheetName().replace(" ", "-").split("ANC.")[1];
    }

    private static String parseTestCaseName(Cell inputCell) {
        String ret = inputCell.getStringCellValue();
        if (ret.equalsIgnoreCase("")) return "Nope.avi";
        if (ret.contains("true") || ret.contains("false")) return ret.split("~")[0];

        ret = ret.split("\"")[1].split("\"")[0];

//        input[0] = input[0].replace(" ", "-");
//        input[1] = input[1].replace(" ", "-");

        return ret;
    }

    public static HashMap<String, Integer> getSortedSheets(Workbook workbook) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            ret.put(workbook.getSheetAt(i).getSheetName(), i);
        }
        return ret;
    }

    private static HashMap<String, TestCaseOld> sortTestCases(List<TestCaseOld> testCases, String name) {
        HashMap<String, TestCaseOld> ret = new HashMap<String, TestCaseOld>();

        for (TestCaseOld testCase : testCases) {
            String caseName = testCase.getName();
           if (caseName.equalsIgnoreCase(name)) {
               ret.put(caseName, testCase);
           }
        }
        return ret;
    }

    // e.g substrToSheetName(getSortedSheets(dictWorkbook), "ANC.B10");
    // returns the full sheet title string.
    public static String substrToSheetName(HashMap<String, Integer> sortedBook, String substr) {
       for (Map.Entry<String, Integer> i : sortedBook.entrySet()) {
           if (i.getKey().contains(substr))
               return i.getKey();
       }
       return null;
    }
}
