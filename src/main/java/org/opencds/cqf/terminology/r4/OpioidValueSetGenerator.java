package org.opencds.cqf.terminology.r4;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.Operation;
import org.opencds.cqf.terminology.SpreadsheetHelper;

import java.util.*;

public class OpioidValueSetGenerator extends Operation {

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    private final int VSLIST_IDX = 20;

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/terminology/r4/output");

        for (String arg : args) {
            if (arg.equals("-OpioidXlsxToValueSet")) continue;
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
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }

            if (pathToSpreadsheet == null) {
                throw new IllegalArgumentException("The path to the spreadsheet is required");
            }

            Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

            OrganizationalMeta meta = resolveOrganizationalMeta(workbook.getSheetAt(0));
            Map<String, Integer> vsMap = resolveVsMap(workbook.getSheetAt(0));


        }
    }

    private OrganizationalMeta resolveOrganizationalMeta(Sheet sheet) {
        OrganizationalMeta organizationalMeta = new OrganizationalMeta();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (SpreadsheetHelper.getCellAsString(row.getCell(0))) {
                case "Canonical URL":
                    organizationalMeta.setCanonicalUrl(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Copyright":
                    organizationalMeta.setCopyright(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Jurisdiction":
                    organizationalMeta.setJurisdiction(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Publisher":
                    organizationalMeta.setPublisher(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "approvalDate":
                    organizationalMeta.setApprovalDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "effectivePeriod.start":
                    organizationalMeta.setEffectivePeriodStart(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "effectivePeriod.end":
                    organizationalMeta.setEffectivePeriodEnd(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "lastReviewDate":
                    organizationalMeta.setLastReviewDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.name":
                    organizationalMeta.setAuthorName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.telecom.system":
                    organizationalMeta.setAuthorTelecomSystem(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.telecom.value":
                    organizationalMeta.setAuthorTelecomValue(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Snomed Version":
                    organizationalMeta.setSnomedVersion(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                default:
                    break;
            }
        }

        return organizationalMeta;
    }

    private Map<String, Integer> resolveVsMap(Sheet sheet) {
        Map<String, Integer> vsMap = new HashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.next().getRowNum() < VSLIST_IDX) {
            //
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            vsMap.put(SpreadsheetHelper.getCellAsString(row.getCell(0)), SpreadsheetHelper.getCellAsInteger(row.getCell(1)) - 1);
        }

        return vsMap;
    }

    private List<ValueSet> resolveValueSets(OrganizationalMeta meta, Map<String, Integer> vsMap, Sheet sheet) {
        List<ValueSet> valueSets = new ArrayList<>();
        for (Map.Entry<String, Integer> entrySet : vsMap.entrySet()) {

        }

        // TODO
        return null;
    }

    private ValueSet getValueSetWithOrganizationalMeta(OrganizationalMeta meta) {
        ValueSet valueSet = new ValueSet();
        valueSet.setUrl(meta.getCanonicalUrl());
        valueSet.setCopyright(meta.getCopyright());
        valueSet.addJurisdiction(new CodeableConcept().addCoding(new Coding().setSystem("urn:iso:std:iso:3166").setCode(meta.getJurisdiction())));
        valueSet.setPublisher(meta.getPublisher());
//        valueSet.set

        // TODO
        return null;
    }
}
