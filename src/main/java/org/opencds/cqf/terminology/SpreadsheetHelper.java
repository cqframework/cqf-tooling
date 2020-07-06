package org.opencds.cqf.terminology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class SpreadsheetHelper {

    public static Workbook getWorkbook(String pathToSpreadsheet) {
        try {
            FileInputStream spreadsheetStream = new FileInputStream(new File(pathToSpreadsheet));
            return new XSSFWorkbook(spreadsheetStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error reading the spreadsheet: " + e.getMessage());
        }
    }

    public static String getCellAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    public static String getCellAsString(Row row, int cellIndex) {
        if (cellIndex >= 0) {
            Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                return getCellAsString(cell);
            }
        }

        return null;
    }

    public static String getCellAsStringTrimmed(Row row, int cellIndex) {
        String rawValue = getCellAsString(row, cellIndex);
        String trimmedValue = null;
        if (rawValue != null) {
            trimmedValue = rawValue.trim();
        }
        return trimmedValue;
    }

    public static void resolveValueSet(org.hl7.fhir.dstu3.model.ValueSet vs, Map<Integer, ValueSet> codesBySystem) {
        vs.setCompose(new org.hl7.fhir.dstu3.model.ValueSet.ValueSetComposeComponent());
        for (Map.Entry<Integer, org.opencds.cqf.terminology.ValueSet> entry : codesBySystem.entrySet()) {
            org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent component = new org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent();
            component.setSystem(entry.getValue().getSystem()).setVersion(entry.getValue().getVersion()).setConcept(entry.getValue().getCodes());
            vs.setCompose(vs.getCompose().addInclude(component));
        }
    }

    public static void writeValueSetToFile(org.hl7.fhir.dstu3.model.ValueSet vs, String encoding, String outputPath) {
        String fileName = vs.getTitle() != null ? vs.getTitle().replaceAll("\\s", "").concat("." + encoding) : "valueset".concat("." + encoding);
        IParser parser =
                encoding == null
                        ? FhirContext.forDstu3().newJsonParser()
                        : encoding.toLowerCase().startsWith("j")
                        ? FhirContext.forDstu3().newJsonParser()
                        : FhirContext.forDstu3().newXmlParser();
        try (FileOutputStream writer = new FileOutputStream(outputPath + "/" + fileName)) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(vs).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }
}
