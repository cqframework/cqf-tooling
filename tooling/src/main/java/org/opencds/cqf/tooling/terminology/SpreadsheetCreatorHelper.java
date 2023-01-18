package org.opencds.cqf.tooling.terminology;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpreadsheetCreatorHelper {

    public static XSSFWorkbook createWorkbook() {
        XSSFWorkbook workBook = null;
        workBook = new XSSFWorkbook();
        return workBook;
    }

    public static void createHeaderRow(List<String> headerNames, XSSFRow currentRow) {
        AtomicInteger cellCount = new AtomicInteger();
        headerNames.forEach(headerName -> {
            XSSFCell currentCell = currentRow.createCell(cellCount.getAndIncrement());
            currentCell.setCellValue(headerName);
        });
    }

    public static void writeSpreadSheet(XSSFWorkbook workBook, String workbookPath) {
        File outputFile = new File(workbookPath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static XSSFCellStyle createLinkStyle(XSSFWorkbook workBook, byte xssFontEnum, short hsfColorValue){
        XSSFCellStyle linkStyle = workBook.createCellStyle();
        XSSFFont linkFont = workBook.createFont();
        linkFont.setUnderline(xssFontEnum);
        linkFont.setColor(hsfColorValue);
        linkStyle.setFont(linkFont);

        return linkStyle;
    }
}
