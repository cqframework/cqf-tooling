/*
    This generator loads value sets from a Workbook. If the data for a single sheet is needed, specify the sheet with codeSheetNum.
    Otherwise, this processor will attempt to load data from all the sheets in the given workbook.
*/
package org.opencds.cqf.tooling.terminology;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public abstract class FlatMultiValueSetGeneratorBase extends Operation {

    private String pathToSpreadsheet;        // -pathtospreadsheet (-pts)
    private String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output";  // -outputpath (-op)
    private String encoding = "json";        // -encoding (-e) { "xml", "json" }
    private String publisher;                // -publisher (-p)                   // Publisher name
    private String publisherNamespace;       // -publishernamespace (-pns)        // Publisher namespace
    private String valueSetIdentifierSystem; // -valuesetidentifiersystem (-vsis) // ValueSet.Identifier System

    // Code sheet defaults
    private int codeSheetNum = 2;         // -codesheetnum (-csn)         // Codes Sheet // -1 indicates load all sheets
    private int codeListRow = 1;          // -codelistrow (-clr)          // Row at which the codes start
    private int valueSetTitleCol = 0;     // -valuesettitlecol (-vstc)    // ValueSet Title
    private int valueSetOidCol = 1;       // -valuesetoidcol (-vsoc)      // ValueSet OID
    private int valueSetVersionCol = 2;   // -valuesetversioncol (-vsvc)  // ValueSet Version
    private int codeCol = 3;              // -codecol (-cc)               // Code column
    private int descriptionCol = 4;       // -descriptioncol (-dc)        // Code Description Column
    private int systemNameCol = 5;        // -systemnamecol (-snc)        // Code System Name Column
    private int systemOidCol = 6;         // -systemoidcol (-soc)         // Code System OID Column
    private int versionCol = 7;           // -versioncol (-vc)            // Code System Version Column
    private int expansionIdCol = -1;      // -expansionidcol (-eic)       // Expansion ID Column

    private Map<Integer, ValueSet> valueSets = new HashMap<>();

    public FlatMultiValueSetGeneratorBase(String outputPath, String encoding, String publisher, String publisherNamespace, String valueSetIdentifierSystem,
        int codeSheetNum, int codeListRow, int valueSetTitleCol, int valueSetOidCol, int valueSetVersionCol, int codeCol,
        int descriptionCol, int systemNameCol, int systemOidCol, int versionCol, int expansionIdCol)
    {
        this.outputPath = outputPath;
        this.encoding = encoding;
        this.publisher = publisher;
        this.publisherNamespace = publisherNamespace;
        this.codeSheetNum = codeSheetNum;
        this.codeListRow = codeListRow;
        this.valueSetTitleCol = valueSetTitleCol;
        this.valueSetOidCol = valueSetOidCol;
        this.valueSetVersionCol = valueSetVersionCol;
        this.valueSetIdentifierSystem = valueSetIdentifierSystem;
        this.codeCol = codeCol;
        this.descriptionCol = descriptionCol;
        this.systemNameCol = systemNameCol;
        this.systemOidCol = systemOidCol;
        this.versionCol = versionCol;
        this.expansionIdCol = expansionIdCol;
    }

    @Override
    public void execute(String[] args) {
        setOutputPath(outputPath); // default

        for (String arg : args) {

            if (Arrays.asList("-HedisXlsxToValueSet", "-VsacMultiXlsxToValueSet").contains(arg)) {
                continue;
            }

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
                case "publisher": case "p": publisher = value; break;
                case "publishernamespace": case "pns": publisherNamespace = value; break;
                case "codesheetnum": case "csn": codeSheetNum = Integer.valueOf(value); break;
                case "codelistrow": case "clr": codeListRow = Integer.valueOf(value); break;
                case "valuesettitlecol": case "vstc": valueSetTitleCol = Integer.valueOf(value); break;
                case "valuesetoidcol": case "vsoc": valueSetOidCol = Integer.valueOf(value); break;
                case "valuesetversioncol": case "vsvc" : valueSetVersionCol = Integer.valueOf(value); break;
                case "valuesetidentifiersystem": case "vsis": valueSetIdentifierSystem = value; break;
                case "codecol": case "cc": codeCol = Integer.valueOf(value); break;
                case "descriptioncol": case "dc": descriptionCol = Integer.valueOf(value); break;
                case "systemnamecol": case "snc": systemNameCol = Integer.valueOf(value); break;
                case "systemoidcol": case "soc": systemOidCol = Integer.valueOf(value); break;
                case "versioncol": case "vc": versionCol = Integer.valueOf(value); break;
                case "expansionidcol": case "eic": expansionIdCol = Integer.valueOf(value); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        processWorkbook(workbook);
    }

    protected void processWorkbook(Workbook workbook)
    {
        if (codeSheetNum != -1) {
            loadSheet(workbook.getSheetAt(codeSheetNum));
        }
        else {
            Iterator<Sheet> it = workbook.sheetIterator();
            while (it.hasNext()) {
                Sheet sheet = it.next();
                loadSheet(sheet);
            }
        }
        writeValueSetsToFiles(valueSets);
    }

    protected void loadSheet(Sheet sheet) {
        Iterator<Row> it = sheet.rowIterator();
        while(it.hasNext()) {
            Row row = it.next();
            if (row.getRowNum() < codeListRow) {
                continue;
            }

            // ValueSet.OID.
            String valueSetOid = SpreadsheetHelper.getCellAsString(row.getCell(valueSetOidCol));
            if (valueSetOid == null || valueSetOid.isEmpty()) {
                throw new IllegalArgumentException(String.format("No value set Oid value found on row: %d", row.getRowNum()));
            }
            int valueSetHash = valueSetOid.hashCode();

            // ValueSet.Identifier
            Identifier valueSetIdentifier = new Identifier();
            valueSetIdentifier.setSystem(valueSetIdentifierSystem);
            valueSetIdentifier.setValue(valueSetOid);

            // ValueSet.Version
            String valueSetVersion = SpreadsheetHelper.getCellAsString(row.getCell(valueSetVersionCol));

            // ValueSet.Url
            String valueSetUrl = publisherNamespace.concat("/ValueSet/").concat(valueSetOid);

            // ValueSet.Expansion.Identifier
            String valueSetExpansionId = expansionIdCol >= 0 ? SpreadsheetHelper.getCellAsString(row.getCell(expansionIdCol)) : null;

            // Code
            String code = SpreadsheetHelper.getCellAsString(row.getCell(codeCol));
            if (code == null) {
                throw new IllegalArgumentException(String.format("No code value found on row: %d", row.getRowNum()));
            }
            // Code Description
            String display = SpreadsheetHelper.getCellAsString(row.getCell(descriptionCol));

            // ValueSet.Title
            String valueSetTitle = SpreadsheetHelper.getCellAsString(row.getCell(valueSetTitleCol));
            // ValueSet.Name
            String valueSetName = valueSetTitle.replaceAll("\\s", "").replaceAll("\\/", "_");
            // Code System Name/Url
            String system = getCodeSystemFromRow(row);
            // Code System Version
            String version = SpreadsheetHelper.getCellAsString(row.getCell(versionCol));

            ValueSet.ValueSetExpansionContainsComponent component = new ValueSet.ValueSetExpansionContainsComponent();
            component.setSystem(system);
            component.setVersion(version);
            component.setDisplay(display);
            component.setCode(code);

            // If the ValueSet hasn't yet been visited, add it to the collection with
            // a new Include for the code system with the current Code. Otherwise, locate
            // the already-registered ValueSet and ensure the Include entry for the code system
            // exists and add the current code to it.
            if (!valueSets.containsKey(valueSetHash)) {
                ValueSet vs = new ValueSet();
                vs.setId(valueSetOid);
                vs.setIdentifier(Collections.singletonList(valueSetIdentifier));
                vs.setUrl(valueSetUrl);
                vs.setVersion(valueSetVersion);
                vs.setName(valueSetName);
                vs.setTitle(valueSetTitle);
                vs.setExperimental(false);
                vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
                vs.setPublisher(publisher);
                vs.setExpansion(new ValueSet.ValueSetExpansionComponent());
                vs.getExpansion().setIdentifier(valueSetExpansionId);
                vs.getExpansion().setTimestamp(java.util.Date.from(Instant.now()));
                vs.getExpansion().getContains().add(component);
                valueSets.put(valueSetHash, vs);
            }
            else {
                ValueSet targetValueSet = valueSets.get(valueSetHash);
                targetValueSet.getExpansion().getContains().add(component);
            }
        }
    }

    protected String getCodeSystemFromRow(Row row) {
        String system = SpreadsheetHelper.getCellAsString(row.getCell(systemNameCol));
        if (system == null)  {
            system = SpreadsheetHelper.getCellAsString(row.getCell(systemOidCol));
            if (system == null) {
                throw new IllegalArgumentException(String.format("No system value found on row: %d", row.getRowNum()));
            }
            system = CodeSystemLookupDictionary.getUrlFromOid(system);
        }
        else {
            system = CodeSystemLookupDictionary.getUrlFromName(system);
        }
        return system;
    }

    protected void writeValueSetsToFiles(Map<Integer, ValueSet> valueSets)
    {
        for (Map.Entry<Integer, ValueSet> vsEntry : valueSets.entrySet()) {
            writeValueSetToFile("valueset-" + vsEntry.getValue().getId() + "." + encoding, vsEntry.getValue());
        }
    }

    protected void writeValueSetToFile(String fileName, ValueSet vs) {
        IParser parser =
            encoding == null
                ? FhirContext.forDstu3Cached().newJsonParser()
                : encoding.toLowerCase().startsWith("j")
                ? FhirContext.forDstu3Cached().newJsonParser()
                : FhirContext.forDstu3Cached().newXmlParser();

        try (FileOutputStream writer = new FileOutputStream(IOUtils.concatFilePath(getOutputPath(), fileName))) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(vs).getBytes());
            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }
}

