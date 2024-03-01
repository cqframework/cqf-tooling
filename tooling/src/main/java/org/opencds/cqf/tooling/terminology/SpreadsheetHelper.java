package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class SpreadsheetHelper {

    public static Workbook getWorkbook(String pathToSpreadsheet) {
        try {
            FileInputStream spreadsheetStream = new FileInputStream(new File(pathToSpreadsheet));
            return new XSSFWorkbook(spreadsheetStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading the spreadsheet: " + e.getMessage());
        }
    }

    private static String cleanseString(String rawValue) {
        StringBuilder newString = new StringBuilder(rawValue.length());
        for (int offset = 0; offset < rawValue.length();)
        {
            int codePoint = rawValue.codePointAt(offset);
            offset += Character.charCount(codePoint);

            // Replace invisible control characters and unused code points
            switch (Character.getType(codePoint))
            {
                case Character.CONTROL:     // \p{Cc}
                case Character.FORMAT:      // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE:   // \p{Cs}
                case Character.UNASSIGNED:  // \p{Cn}
                    newString.append('?');
                    break;
                default:
                    newString.append(Character.toChars(codePoint));
                    break;
            }
        }
        return newString.toString();
    }

    private static String cleanseStringNoReplacement(String rawValue) {
        StringBuilder newString = new StringBuilder(rawValue.length());
        for (int offset = 0; offset < rawValue.length();)
        {
            int codePoint = rawValue.codePointAt(offset);
            offset += Character.charCount(codePoint);

            // Replace invisible control characters and unused code points
            switch (Character.getType(codePoint))
            {
                case Character.CONTROL:     // \p{Cc}
                case Character.FORMAT:      // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE:   // \p{Cs}
                case Character.UNASSIGNED:  // \p{Cn}
                    //just skip it
                    break;
                default:
                    newString.append(Character.toChars(codePoint));
                    break;
            }
        }
        return newString.toString();
    }

    public static String protectedString(String rawValue, boolean replace) {
        String result = rawValue;
        if (result == null) {
            return result;
        }
        result = result.trim();
        if(replace) {
            result = result.replaceAll("\\p{Cntrl}", "?");
            result = result.replaceAll("\\p{C}", "?");
            result = SpreadsheetHelper.cleanseString(result);
        }
        else{
            result = result.replaceAll("\\p{Cntrl}", "");
            result = result.replaceAll("\\p{C}", "");
            result = SpreadsheetHelper.cleanseStringNoReplacement(result);
        }
        return result;
    }

    private static DataFormatter dataFormatter;
    public static DataFormatter getDataFormatter() {
        if (dataFormatter == null) {
            dataFormatter = new DataFormatter();
        }
        return dataFormatter;
    }

    public static String getCellAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String valueAsString = SpreadsheetHelper.getDataFormatter().formatCellValue(cell);
        return SpreadsheetHelper.protectedString(valueAsString, true);
    }

    public static String getCellAsStringNoReplacement(Cell cell) {
        if (cell == null) {
            return null;
        }
        String valueAsString = SpreadsheetHelper.getDataFormatter().formatCellValue(cell);
        return SpreadsheetHelper.protectedString(valueAsString, false);
    }

    public static Integer getCellAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }
        return Double.valueOf(cell.getNumericCellValue()).intValue();
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

    //name.matches('[A-Z]([A-Za-z0-9_]){0,254}')
    public static String getFHIRName(String value) {
        String name = value.replaceAll("[^A-Za-z0-9_]", "");
        while (name.length() > 0 && !Character.isAlphabetic(name.charAt(0))) {
            name = name.substring(1);
        }
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        name = name.length() <= 254 ? name : name.substring(0, 254);
        return name;
    }

    public static void resolveValueSet(org.hl7.fhir.dstu3.model.ValueSet vs, Map<Integer, ValueSet> codesBySystem) {
        vs.setCompose(new org.hl7.fhir.dstu3.model.ValueSet.ValueSetComposeComponent());
        for (Map.Entry<Integer, org.opencds.cqf.tooling.terminology.ValueSet> entry : codesBySystem.entrySet()) {
            org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent component = new org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent();
            component.setSystem(entry.getValue().getSystem()).setVersion(entry.getValue().getVersion()).setConcept(entry.getValue().getCodes());
            vs.setCompose(vs.getCompose().addInclude(component));
        }
    }

    public static void writeValueSetToFile(org.hl7.fhir.dstu3.model.ValueSet vs, String encoding, String outputPath) {
        String fileName = vs.getTitle() != null ? vs.getTitle().replaceAll("\\s", "").concat("." + encoding) : "valueset".concat("." + encoding);
        IParser parser =
                encoding == null
                        ? FhirContext.forDstu3Cached().newJsonParser()
                        : encoding.toLowerCase().startsWith("j")
                        ? FhirContext.forDstu3Cached().newJsonParser()
                        : FhirContext.forDstu3Cached().newXmlParser();
        try (FileOutputStream writer = new FileOutputStream(IOUtils.concatFilePath(outputPath, fileName))) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(vs).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }
}
