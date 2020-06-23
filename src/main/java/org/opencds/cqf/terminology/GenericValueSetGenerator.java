package org.opencds.cqf.terminology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.opencds.cqf.Operation;

public class GenericValueSetGenerator extends Operation {

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    // Meta defaults
    private boolean hasId = false;
    private int idSheet = 0; // -id (-i) = sheet#:row#:column#
    private int idRow = 1;
    private int idCol = 1;

    private boolean hasUrl = false;
    private int urlSheet = 0; // -url (-u) = sheet#:row#:column#
    private int urlRow = 2;
    private int urlCol = 1;

    private boolean hasVersion = false;
    private int versionSheet = 0; // -version (-v) = sheet#:row#:column#
    private int versionRow = 3;
    private int versionCol = 1;

    private boolean hasName = false;
    private int nameSheet = 0; // -name (-n) = sheet#:row#:column#
    private int nameRow = 4;
    private int nameCol = 1;

    private boolean hasTitle = false;
    private int titleSheet = 0; // -title (-t) = sheet#:row#:column#
    private int titleRow = 5;
    private int titleCol = 1;

    private boolean hasPublisher = false;
    private int publisherSheet = 0; // -publisher (-pub) = sheet#:row#:column#
    private int publisherRow = 6;
    private int publisherCol = 1;

    private boolean hasDescription = false;
    private int descriptionSheet = 0; // -description (-d) = sheet#:row#:column#
    private int descriptionRow = 7;
    private int descriptionCol = 1;

    private boolean hasPurpose = false;
    private int purposeSheet = 0; // -purpose (-pur) = sheet#:row#:column#
    private int purposeRow = 8;
    private int purposeCol = 1;

    private boolean hasCopyright = false;
    private int copyrightSheet = 0; // -copyright (-c) = sheet#:row#:column#
    private int copyrightRow = 8;
    private int copyrightCol = 1;

    // Code defaults
    private boolean hasCode = false;
    private int codeSheet = 1; // -code = sheet#:row#:column#
    private int codeRow = 1;
    private int codeCol = 0;

    private boolean hasDisplay = false;
    private int displaySheet = 1; // -display (-dis) = sheet#:row#:column#
    private int displayRow = 1;
    private int displayCol = 1;

    private boolean hasSystem = false;
    private int systemSheet = 1; // -system (-s) = sheet#:row#:column#
    private int systemRow = 1;
    private int systemCol = 2;

    private boolean hasStaticSystem = false;
    private int staticSystemSheet = -1; // -staticsystem (-ss) = sheet#:row#:column#
    private int staticSystemRow = -1;
    private int staticSystemCol = -1;

    private boolean hasCodeVersion = false;
    private int codeVersionSheet = 1; // -codeversion (-cv) = sheet#:row#:column#
    private int codeVersionRow = 1;
    private int codeVersionCol = 3;

    private boolean hasStaticCodeVersion = false;
    private int staticCodeVersionSheet = -1; // -staticversion (-sv) = sheet#:row#:column#
    private int staticCodeVersionRow = -1;
    private int staticCodeVersionCol = -1;

    private Map<Integer, org.opencds.cqf.terminology.ValueSet> codesBySystem = new HashMap<>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/terminology/output"); // default

        for (String arg : args) {
            if (arg.equals("-XlsxToValueSet")) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            String flag = flagAndValue[0];
            String value = flagAndValue.length < 2 ? null : flagAndValue[1];

            if (flag.equals("-pathtospreadsheet") || flag.equals("-pts")) {
                pathToSpreadsheet = value;
            }
            else if (flag.equals("-encoding") || flag.equals("-e")) {
                encoding = value;
            }
            else if (flag.equals("-outputpath") || flag.equals("-op")) {
                setOutputPath(value);
            }
            else {
                String[] sheetRowCol = value == null ? null : value.split(":");

                if (sheetRowCol != null && sheetRowCol.length < 3) {
                    throw new IllegalArgumentException("The following arg must include the sheet#:row#:column# format: " + arg);
                }

                switch (flag.replace("-", "").toLowerCase()) {
                    case "id": case "i":
                        hasId = true;
                        if (sheetRowCol == null) break;
                        idSheet = Integer.valueOf(sheetRowCol[0]);
                        idRow = Integer.valueOf(sheetRowCol[1]);
                        idCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "url": case "u":
                        hasUrl = true;
                        if (sheetRowCol == null) break;
                        urlSheet = Integer.valueOf(sheetRowCol[0]);
                        urlRow = Integer.valueOf(sheetRowCol[1]);
                        urlCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "version": case "v":
                        hasVersion = true;
                        if (sheetRowCol == null) break;
                        versionSheet = Integer.valueOf(sheetRowCol[0]);
                        versionRow = Integer.valueOf(sheetRowCol[1]);
                        versionCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "name": case "n":
                        hasName = true;
                        if (sheetRowCol == null) break;
                        nameSheet = Integer.valueOf(sheetRowCol[0]);
                        nameRow = Integer.valueOf(sheetRowCol[1]);
                        nameCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "title": case "t":
                        hasTitle = true;
                        if (sheetRowCol == null) break;
                        titleSheet = Integer.valueOf(sheetRowCol[0]);
                        titleRow = Integer.valueOf(sheetRowCol[1]);
                        titleCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "publisher": case "pub":
                        hasPublisher = true;
                        if (sheetRowCol == null) break;
                        publisherSheet = Integer.valueOf(sheetRowCol[0]);
                        publisherRow = Integer.valueOf(sheetRowCol[1]);
                        publisherCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "description": case "d":
                        hasDescription = true;
                        if (sheetRowCol == null) break;
                        descriptionSheet = Integer.valueOf(sheetRowCol[0]);
                        descriptionRow = Integer.valueOf(sheetRowCol[1]);
                        descriptionCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "purpose": case "pur":
                        hasPurpose = true;
                        if (sheetRowCol == null) break;
                        purposeSheet = Integer.valueOf(sheetRowCol[0]);
                        purposeRow = Integer.valueOf(sheetRowCol[1]);
                        purposeCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "copyright": case "c":
                        hasCopyright = true;
                        if (sheetRowCol == null) break;
                        copyrightSheet = Integer.valueOf(sheetRowCol[0]);
                        copyrightRow = Integer.valueOf(sheetRowCol[1]);
                        copyrightCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "system": case "s":
                        if (hasStaticSystem) {
                            throw new IllegalArgumentException("Cannot have both system and staticsystem flags set");
                        }
                        hasSystem = true;
                        if (sheetRowCol == null) break;
                        systemSheet = Integer.valueOf(sheetRowCol[0]);
                        systemRow = Integer.valueOf(sheetRowCol[1]);
                        systemCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "staticsystem": case "ss":
                        if (hasSystem) {
                            throw new IllegalArgumentException("Cannot have both system and staticsystem flags set");
                        }
                        hasStaticSystem = true;
                        if (sheetRowCol == null) break;
                        staticSystemSheet = Integer.valueOf(sheetRowCol[0]);
                        staticSystemRow = Integer.valueOf(sheetRowCol[1]);
                        staticSystemCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "codeversion": case "cv":
                        if (hasStaticCodeVersion) {
                            throw new IllegalArgumentException("Cannot have both version and staticversion flags set");
                        }
                        hasCodeVersion = true;
                        if (sheetRowCol == null) break;
                        codeVersionSheet = Integer.valueOf(sheetRowCol[0]);
                        codeVersionRow = Integer.valueOf(sheetRowCol[1]);
                        codeVersionCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "staticversion": case "sv":
                        if (hasCodeVersion) {
                            throw new IllegalArgumentException("Cannot have both version and staticversion flags set");
                        }
                        hasStaticCodeVersion = true;
                        if (sheetRowCol == null) break;
                        staticCodeVersionSheet = Integer.valueOf(sheetRowCol[0]);
                        staticCodeVersionRow = Integer.valueOf(sheetRowCol[1]);
                        staticCodeVersionCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "code":
                        hasCode = true;
                        if (sheetRowCol == null) break;
                        codeSheet = Integer.valueOf(sheetRowCol[0]);
                        codeRow = Integer.valueOf(sheetRowCol[1]);
                        codeCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    case "display": case "dis":
                        hasDisplay = true;
                        if (sheetRowCol == null) break;
                        displaySheet = Integer.valueOf(sheetRowCol[0]);
                        displayRow = Integer.valueOf(sheetRowCol[1]);
                        displayCol = Integer.valueOf(sheetRowCol[2]);
                        break;
                    default: throw new IllegalArgumentException("Unknown arg: " + arg);
                }
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }
        if (!hasCode) {
            throw new IllegalArgumentException("-code flag must be specified");
        }
        if (!hasSystem && !hasStaticSystem) {
            throw new IllegalArgumentException("-system or -staticsystem flag must be specified");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        ValueSet vs = new ValueSet();
        resolveMetaData(vs, workbook);
        resolveCodeList(workbook);
        SpreadsheetHelper.resolveValueSet(vs, codesBySystem);
        SpreadsheetHelper.writeValueSetToFile(vs, encoding, getOutputPath());
    }

    public void resolveMetaData(ValueSet vs, Workbook workbook) {
        vs.setId(hasId ? SpreadsheetHelper.getCellAsString(workbook.getSheetAt(idSheet).getRow(idRow).getCell(idCol)) : "example");
        vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        if (hasUrl) {
            vs.setUrl(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(urlSheet).getRow(urlRow).getCell(urlCol)));
        }
        if (hasVersion) {
            vs.setVersion(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(versionSheet).getRow(versionRow).getCell(versionCol)));
        }
        if (hasName) {
            vs.setName(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(nameSheet).getRow(nameRow).getCell(nameCol)));
        }
        if (hasTitle) {
            vs.setTitle(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(titleSheet).getRow(titleRow).getCell(titleCol)));
        }
        if (hasPublisher) {
            vs.setPublisher(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(publisherSheet).getRow(publisherRow).getCell(publisherCol)));
        }
        if (hasDescription) {
            vs.setDescription(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(descriptionSheet).getRow(descriptionRow).getCell(descriptionCol)));
        }
        if (hasPurpose) {
            vs.setPurpose(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(purposeSheet).getRow(purposeRow).getCell(purposeCol)));
        }
        if (hasCopyright) {
            vs.setCopyright(SpreadsheetHelper.getCellAsString(workbook.getSheetAt(copyrightSheet).getRow(copyrightRow).getCell(copyrightCol)));
        }
    }

    public void resolveCodeList(Workbook workbook) {
        Iterator<Row> codeIterator = workbook.getSheetAt(codeSheet).rowIterator();
        Iterator<Row> displayIterator = hasDisplay ? workbook.getSheetAt(displaySheet).rowIterator() : null;
        Iterator<Row> systemIterator = hasSystem ? workbook.getSheetAt(systemSheet).rowIterator() : null;
        String system = hasStaticSystem
                ? SpreadsheetHelper.getCellAsString(workbook.getSheetAt(staticSystemSheet).getRow(staticSystemRow).getCell(staticSystemCol))
                : null;
        Iterator<Row> versionIterator = hasCodeVersion ? workbook.getSheetAt(codeVersionSheet).rowIterator() : null;
        String version = hasStaticCodeVersion
                ? SpreadsheetHelper.getCellAsString(workbook.getSheetAt(staticCodeVersionSheet).getRow(staticCodeVersionRow).getCell(staticCodeVersionCol))
                : null;

        while (codeIterator.hasNext()) {
            Row row = codeIterator.next();
            if (row.getRowNum() < codeRow) {
                continue;
            }

            if (systemIterator != null) {
                system = getNextValue(systemIterator, systemRow, systemCol);
            }
            if (system == null) {
                throw new IllegalArgumentException("System not provided");
            }
            try {
                system = system.startsWith("http") ? system : CodeSystemLookupDictionary.getUrlFromName(system);
            } catch (IllegalArgumentException e) {
                system = CodeSystemLookupDictionary.getUrlFromOid(system);
            }

            if (versionIterator != null) {
                version = getNextValue(versionIterator, codeVersionRow, codeVersionCol);
            }

            int hash = system.hashCode() * (version != null ? version.hashCode() : 1);

            if (!codesBySystem.containsKey(hash)) {
                codesBySystem.put(hash, new org.opencds.cqf.terminology.ValueSet().setSystem(system).setVersion(version));
            }

            String code = SpreadsheetHelper.getCellAsString(row.getCell(codeCol));

            String display = null;
            if (displayIterator != null) {
                display = getNextValue(displayIterator, displayRow, displayCol);
            }

            ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent().setCode(code).setDisplay(display);
            codesBySystem.get(hash).addCode(concept);
        }
    }

    private String getNextValue(Iterator<Row> it, int rowIdx, int colIdx) {
        if (it.hasNext()) {
            Row row = it.next();
            while (row.getRowNum() < rowIdx) {
                it.next();
            }
            return SpreadsheetHelper.getCellAsString(row.getCell(colIdx));
        }
        return null;
    }
}
