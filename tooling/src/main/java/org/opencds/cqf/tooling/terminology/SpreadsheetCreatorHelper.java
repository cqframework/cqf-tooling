package org.opencds.cqf.tooling.terminology;

import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;

public class SpreadsheetCreatorHelper {

    public static XSSFWorkbook createWorkbook() {
        XSSFWorkbook workBook = null;
        workBook = new XSSFWorkbook();
        return workBook;
    }
}
