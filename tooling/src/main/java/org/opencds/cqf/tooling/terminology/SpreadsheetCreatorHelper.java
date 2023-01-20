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

    public static void createHeaderRow(XSSFWorkbook workBook, List<String> headerNames, XSSFRow currentRow) {
        AtomicInteger cellCount = new AtomicInteger();
        XSSFCellStyle boldStyle = createBoldStyle(workBook, XSSFFont.U_SINGLE);
        headerNames.forEach(headerName -> {
            XSSFCell currentCell = currentRow.createCell(cellCount.getAndIncrement());
            currentCell.setCellStyle(boldStyle);
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

    public static XSSFCellStyle createBoldStyle(XSSFWorkbook workBook, byte xssFontEnum){
        XSSFCellStyle style = workBook.createCellStyle();
        XSSFFont font = workBook.createFont();
        font.setFontHeightInPoints((short) 15);
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
