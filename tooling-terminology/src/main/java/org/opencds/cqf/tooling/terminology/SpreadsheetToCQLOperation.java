package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.cqframework.cql.cql2elm.StringEscapeUtils;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class SpreadsheetToCQLOperation extends Operation {

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private boolean hasHeader = true; // -hasheader (-hh)

    // The file name of the input spreadsheet
    private String spreadsheetName;

    private StringBuilder result = new StringBuilder();

    private String getHeader(Row header, int columnIndex) {
        if (header != null && SpreadsheetHelper.getCellAsString(header, columnIndex) != null) {
            return SpreadsheetHelper.getCellAsString(header, columnIndex).trim();
        }
        else {
            return CellReference.convertNumToColString(columnIndex);
        }
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default
        for (String arg : args) {
            if (arg.equals("-SpreadsheetToCQL")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break; // -pathtospreadsheet (-pts)
                case "hasheader": case "hh": hasHeader = Boolean.valueOf(value); break; // -hasheader (-hh)
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        spreadsheetName = new File(pathToSpreadsheet).getName();
        int extensionIndex = spreadsheetName.lastIndexOf(".");
        if (extensionIndex > 0) {
            spreadsheetName = spreadsheetName.substring(0, extensionIndex);
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        // library "Workbook"
        result.append("library ");
        result.append("\"");
        result.append(spreadsheetName);
        result.append("\"");
        result.append(System.lineSeparator());
        result.append(System.lineSeparator());

        result.append("// Generated on ");
        result.append(LocalDateTime.now());
        result.append(System.lineSeparator());
        result.append(System.lineSeparator());

        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);

            // define "Sheet1":
            result.append("define ");
            result.append("\"");
            result.append(sheet.getSheetName());
            result.append("\":");
            result.append(System.lineSeparator());
            result.append("{");
            result.append(System.lineSeparator());

            Iterator<Row> rows = sheet.rowIterator();
            Row header = null;
            boolean firstRow = true;
            while (rows.hasNext()) {
                Row row = rows.next();

                if (header == null && hasHeader) {
                    header = row;
                    continue;
                }

                if (firstRow) {
                    firstRow = false;
                }
                else {
                    result.append(",");
                    result.append(System.lineSeparator());
                }

                result.append("  { ");
                Iterator<Cell> cells = row.cellIterator();
                boolean firstCell = true;
                while (cells.hasNext()) {
                    if (firstCell) {
                        firstCell = false;
                    }
                    else {
                        result.append(", ");
                    }
                    Cell cell = cells.next();
                    SpreadsheetHelper.getCellAsString(cell);
                    result.append("\"");
                    result.append(getHeader(header, cell.getColumnIndex()));
                    result.append("\"");
                    result.append(": ");
                    result.append("'");
                    result.append(StringEscapeUtils.escapeCql(SpreadsheetHelper.getCellAsString(cell)));
                    result.append("'");
                }
                result.append(" }");
            }

            result.append(System.lineSeparator());
            result.append("}");
            result.append(System.lineSeparator());
            result.append(System.lineSeparator());
        }

        try (FileOutputStream writer = new FileOutputStream(IOUtils.concatFilePath(getOutputPath(),spreadsheetName + ".cql"))) {
            writer.write(result.toString().getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing cql output fragment");
        }
    }
}
