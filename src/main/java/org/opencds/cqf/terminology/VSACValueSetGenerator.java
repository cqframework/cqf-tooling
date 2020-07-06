package org.opencds.cqf.terminology;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class VSACValueSetGenerator extends Operation {

    private final String VSAC_BASE_URL = "http://cts.nlm.nih.gov/fhir/ValueSet/";

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    // Meta sheet defaults
    private int metaSheetNum = 0; // -metasheetnum (-msn)
    private int metaNameRow = 1; // -metanamerow (-mnr)
    private int metaOidRow = 3; // -metaoidrow (-mor)
    private int metaStewardRow = 6; // -metastewardrow (-msd)

    // Code sheet defaults
    private int codeSheetNum = 1; // -codesheetnum (-csn)
    private int codeListRow = 13; // -codelistrow (-clr)
    private int codeCol = 0; // -codecol (-cc)
    private int descriptionCol = 1; // -descriptioncol (-dc)
    private int systemNameCol = 2; // -systemnamecol (-snc)
    private int versionCol = 3; // -versioncol (-vc)
    private int systemOidCol = 4; // -systemoidcol (-soc)

    private Map<Integer, org.opencds.cqf.terminology.ValueSet> codesBySystem = new HashMap<>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/terminology/output"); // default

        for (String arg : args) {
            if (arg.equals("-VsacXlsxToValueSet")) continue;
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
                case "metasheetnum": case "msn": metaSheetNum = Integer.valueOf(value); break;
                case "metanamerow": case "mnr": metaNameRow = Integer.valueOf(value); break;
                case "metaoidrow": case "mor": metaOidRow = Integer.valueOf(value); break;
                case "metastewardrow": case "msr": metaStewardRow = Integer.valueOf(value); break;
                case "codesheetnum": case "csn": codeSheetNum = Integer.valueOf(value); break;
                case "codelistrow": case "clr": codeListRow = Integer.valueOf(value); break;
                case "codecol": case "cc": codeCol = Integer.valueOf(value); break;
                case "descriptioncol": case "dc": descriptionCol = Integer.valueOf(value); break;
                case "systemnamecol": case "snc": systemNameCol = Integer.valueOf(value); break;
                case "versioncol": case "vc": versionCol = Integer.valueOf(value); break;
                case "systemoidcol": case "soc": systemOidCol = Integer.valueOf(value); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        ValueSet vs = new ValueSet();
        resolveMetaData(vs, workbook);
        resolveCodeList(workbook);
        resolveValueSet(vs);
        writeValueSetToFile(vs.getTitle() != null ? vs.getTitle().replaceAll("\\s", "").concat("." + encoding) : "valueset".concat("." + encoding), vs);
    }

    private String getSecondStringInRow(Sheet sheet, int rowIdx) {
        int col = 1;
        for (Cell cell : sheet.getRow(rowIdx)) {
            if (cell == null) {
                continue;
            }
            if (col == 2) {
                return SpreadsheetHelper.getCellAsString(cell);
            }
            ++col;
        }
        return null;
    }

    public void resolveMetaData(ValueSet vs, Workbook workbook) {
        Sheet metaSheet = workbook.getSheetAt(metaSheetNum);
        String title = getSecondStringInRow(metaSheet, metaNameRow);
        if (title != null) title = title.replace("/", "");
        if (title != null) {
            vs.setTitle(title);
        }
        String id = getSecondStringInRow(metaSheet, metaOidRow);
        if (id != null) {
            vs.setId(id);
        }
        vs.setUrl(VSAC_BASE_URL + id);
        String publisher = getSecondStringInRow(metaSheet, metaStewardRow);
        if (publisher != null) {
            vs.setPublisher(publisher);
        }
        vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
    }

    public void resolveCodeList(Workbook workbook) {
        Iterator<Row> it = workbook.getSheetAt(codeSheetNum).rowIterator();
        while(it.hasNext()) {
            Row row = it.next();
            if (row.getRowNum() < codeListRow) {
                continue;
            }

            String system = SpreadsheetHelper.getCellAsString(row.getCell(systemNameCol));
            if (system == null) {
                system = SpreadsheetHelper.getCellAsString(row.getCell(systemOidCol));
                if (system == null) {
                    throw new IllegalArgumentException(String.format("No system value found on row: %d", row.getRowNum()));
                }
                system = CodeSystemLookupDictionary.getUrlFromOid(system);
            }
            else {
                system = CodeSystemLookupDictionary.getUrlFromName(system);
            }

            String version = SpreadsheetHelper.getCellAsString(row.getCell(versionCol));
            int hash = system.hashCode() * (version != null ? version.hashCode() : 1);

            if (!codesBySystem.containsKey(hash)) {
                codesBySystem.put(hash, new org.opencds.cqf.terminology.ValueSet().setSystem(system).setVersion(version));
            }

            String code = SpreadsheetHelper.getCellAsString(row.getCell(codeCol));
            if (code == null) {
                throw new IllegalArgumentException(String.format("No code value found on row: %d", row.getRowNum()));
            }

            String display = SpreadsheetHelper.getCellAsString(row.getCell(descriptionCol));

            ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent().setCode(code).setDisplay(display);

            codesBySystem.get(hash).addCode(concept);
        }
    }

    public void resolveValueSet(ValueSet vs) {
        vs.setCompose(new ValueSet.ValueSetComposeComponent());
        for (Map.Entry<Integer, org.opencds.cqf.terminology.ValueSet> entry : codesBySystem.entrySet()) {
            ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
            component.setSystem(entry.getValue().getSystem()).setVersion(entry.getValue().getVersion()).setConcept(entry.getValue().getCodes());
            vs.setCompose(vs.getCompose().addInclude(component));
        }
    }

    //should bundle and store in a bundles dir
    private void writeValueSetToFile(String fileName, ValueSet vs) {
        IParser parser =
                encoding == null
                        ? FhirContext.forDstu3().newJsonParser()
                        : encoding.toLowerCase().startsWith("j")
                                ? FhirContext.forDstu3().newJsonParser()
                                : FhirContext.forDstu3().newXmlParser();
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(vs).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }
}
