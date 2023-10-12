package org.opencds.cqf.tooling.terminology;

import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.hl7.fhir.utilities.json.JsonTrackingParser;
import org.opencds.cqf.tooling.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class SpreadsheetValidateVSandCS extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetValidateVSandCS.class);
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String urlToTestServer; // -urltotestserver (-uts)
    private boolean hasHeader = true; // -hasheader (-hh)

    // The file name of the input spreadsheet
    private String spreadsheetName;

    private String getHeader(Row header, int columnIndex) {
        if (header != null) {
            return SpreadsheetHelper.getCellAsString(header, columnIndex).trim();
        } else {
            return CellReference.convertNumToColString(columnIndex);
        }
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default
        String userName = "";
        String password = "";

        for (String arg : args) {
            if (arg.equals("-SpreadsheetValidateVSandCS")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtospreadsheet":
                case "pts":
                    pathToSpreadsheet = value;
                    break; // -pathtospreadsheet (-pts)
                case "hasheader":
                case "hh":
                    hasHeader = Boolean.valueOf(value);
                    break; // -hasheader (-hh)
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "urlToTestServer":
                case "uts":
                    urlToTestServer = value;
                    break; // -urltotestserver (-uts)
                case "userName":
                case "un":
                    userName = value;
                    break; // -userName (-un)
                case "password":
                case "pw":
                    password = value;
                    break; // -password (-pw)
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }
        validateSpreadsheet(userName, password);
    }

    private void validateSpreadsheet(String userName, String password) {
        int firstSheet = 0;
        int idCellNumber = 1;
        int valueSetCellNumber = 3;
        int valueSetURLCellNumber = 4;
        int versionCellNumber = 5;
        int codeSystemURLCellNumber = 6;

        spreadsheetName = new File(pathToSpreadsheet).getName();
        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        Sheet sheet = workbook.getSheetAt(firstSheet);

        Iterator<Row> rows = sheet.rowIterator();
        Row header = null;
        while (rows.hasNext()) {
            Row row = rows.next();

            if (header == null && hasHeader) {
                header = row;
                continue;
            }
//                Iterator<Cell> cells = row.cellIterator();
//                Cell cell = cells.next();
            try {
                String id = row.getCell(idCellNumber).getStringCellValue();
                String valueSetName = row.getCell(valueSetCellNumber).getStringCellValue();
                String valueSetURL = row.getCell(valueSetURLCellNumber).getStringCellValue();
                String version = row.getCell(versionCellNumber).getStringCellValue();
                String codeSystemURL = row.getCell(codeSystemURLCellNumber).getStringCellValue();
                validateRow(id, valueSetName, valueSetURL, version, codeSystemURL, userName, password);
            } catch (Exception ex) {
                System.out.println("Row " + row.getRowNum() + " has an empty cell.");
                logger.debug("Row " + row.getRowNum() + " has an empty cell.");
            }
        }
    }

    private void validateRow(String id, String valueSetName, String valueSetURL, String version, String codeSystemURL, String userName, String password){
        /*
        Get valueset from server, Get package from NPM, run validate with ResourceValidator code
        Get codesystem from server, Get package from NPM???, validate with ResourceValidator code
         */
        urlToTestServer
    }

    /*
    From NpmPackageManager
     */
    private JsonObject fetchJson(String source) throws IOException {
        URL url = new URL(source + "?nocache=" + System.currentTimeMillis());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setInstanceFollowRedirects(true);
        return JsonTrackingParser.parseJson(c.getInputStream());
    }
}
