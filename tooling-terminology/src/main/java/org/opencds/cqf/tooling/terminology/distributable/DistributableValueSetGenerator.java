package org.opencds.cqf.tooling.terminology.distributable;

import ca.uhn.fhir.context.FhirContext;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class DistributableValueSetGenerator extends Operation {

    private FhirContext fhirContext;

    public DistributableValueSetGenerator() {
        this.fhirContext = FhirContext.forR4Cached();
    }

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    private final String CANONICAL_URL = "Canonical URL";
    private final String COPYRIGHT = "Copyright";
    private final String JURISDICTION = "Jurisdiction";
    private final String PUBLISHER = "Publisher";
    private final String AUTHOR_NAME = "author.name";
    private final String AUTHOR_TELECOM_SYSTEM = "author.telecom.system";
    private final String AUTHOR_TELECOM_VALUE = "author.telecom.value";

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output");

        for (String arg : args) {
            if (arg.equals("-DistributableXlsxToValueSet")) continue;
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
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        CommonMetaData commonMetaData = processCommonMetaData(workbook.getSheetAt(0));
        output(resolveValueSets(commonMetaData, workbook));
    }

    private CommonMetaData processCommonMetaData(Sheet commonMetaDataSheet) {
        CommonMetaData commonMetaData = new CommonMetaData();
        Iterator<Row> rowIterator = commonMetaDataSheet.rowIterator();

        boolean isCodeSystem = false;
        boolean isValueSet = false;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String cellAsString = SpreadsheetHelper.getCellAsString(row.getCell(0));
            if (cellAsString == null) continue;

            if (cellAsString.equals("CodeSystems")) {
                isCodeSystem = true;
            }

            else if (cellAsString.equals("ValueSets")) {
                isCodeSystem = false;
                isValueSet = true;
            }

            else if (isCodeSystem) {
                commonMetaData.addCodeSystemMeta(
                        SpreadsheetHelper.getCellAsString(row.getCell(0)),
                        SpreadsheetHelper.getCellAsString(row.getCell(1)),
                        SpreadsheetHelper.getCellAsString(row.getCell(2))
                );
            }

            else if (isValueSet) {
                commonMetaData.addValueSetMeta(
                        SpreadsheetHelper.getCellAsString(row.getCell(0)),
                        SpreadsheetHelper.getCellAsString(row.getCell(2)),
                        SpreadsheetHelper.getCellAsString(row.getCell(3))
                );
            }

            else {
                switch (cellAsString) {
                    case CANONICAL_URL:
                        commonMetaData.getOrganizationalMetaData().setCanonicalUrlBase(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case COPYRIGHT:
                        commonMetaData.getOrganizationalMetaData().setCopyright(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case JURISDICTION:
                        commonMetaData.getOrganizationalMetaData().setJurisdiction(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case PUBLISHER:
                        commonMetaData.getOrganizationalMetaData().setPublisher(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case AUTHOR_NAME:
                        commonMetaData.getOrganizationalMetaData().setAuthorName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case AUTHOR_TELECOM_SYSTEM:
                        commonMetaData.getOrganizationalMetaData().setAuthorTelecomSystem(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    case AUTHOR_TELECOM_VALUE:
                        commonMetaData.getOrganizationalMetaData().setAuthorTelecomValue(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                        break;
                    default:
                        break;
                }
            }
        }

        return commonMetaData;
    }

    private DistributableValueSetMeta resolveDistributableValueSetMeta(Sheet sheet) {
        DistributableValueSetMeta meta = new DistributableValueSetMeta();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String cellString = SpreadsheetHelper.getCellAsString(row.getCell(0));
            if (cellString == null) continue;
            switch (cellString) {
                case "id":
                    meta.setId(SpreadsheetHelper.getCellAsString(row.getCell(1)).toLowerCase());
                    break;
                case "identifier":
                    meta.setIdentifier(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "rules-text":
                    meta.setRulesText(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "warning":
                    meta.setWarning(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "version":
                    meta.setVersion(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "name":
                    meta.setName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "title":
                    meta.setTitle(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "status":
                    meta.setStatus(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "experimental":
                    meta.setExperimental(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "date":
                    meta.setDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "description":
                    meta.setDescription(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "purpose":
                    meta.setPurpose(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "purpose.ClinicalFocus":
                    meta.setPurposeClinicalFocus(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "purpose.DataElementScope":
                    meta.setPurposeDataElementScope(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "purpose.InclusionCriteria":
                    meta.setPurposeInclusionCriteria(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "purpose.ExclusionCriteria":
                    meta.setPurposeExclusionCriteria(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "compose":
                    meta.setCompose(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                default:
                    break;
            }
        }

        return meta;
    }

    private List<ValueSet> resolveValueSets(CommonMetaData meta, Workbook workbook) {
        List<ValueSet> valueSets = new ArrayList<>();
        DistributableValueSetMeta distributableValueSetMeta;
        ValueSet vs;
        for (Map.Entry<String, CommonMetaData.ValueSetMeta> entrySet : meta.getValueSetMeta().entrySet()) {
            if (entrySet.getKey().isEmpty()
                    || entrySet.getValue().getMetaPageName().isEmpty()
                    || entrySet.getValue().getCodeListPageName().isEmpty())
            {
                continue;
            }
            distributableValueSetMeta = resolveDistributableValueSetMeta(workbook.getSheet(entrySet.getValue().getMetaPageName()));
            vs = distributableValueSetMeta.populate(fhirContext);
            meta.getOrganizationalMetaData().populate(vs, fhirContext.getVersion().toString());

            resolveCodeList(meta, workbook.getSheet(entrySet.getValue().getCodeListPageName()), vs);

            valueSets.add(vs);
        }

        return valueSets;
    }

    private void resolveCodeList(CommonMetaData meta, Sheet sheet, ValueSet vs) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        Boolean active = true;
        String system = null;
        String version = null;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String code = SpreadsheetHelper.getCellAsString(row.getCell(0));

            if (code.equals("Code")) continue;

            String description = SpreadsheetHelper.getCellAsString(row.getCell(1));
            active = SpreadsheetHelper.getCellAsString(row.getCell(2)) == null ? active : Boolean.valueOf(SpreadsheetHelper.getCellAsString(row.getCell(2)));
            system = SpreadsheetHelper.getCellAsString(row.getCell(3)) == null ? system : SpreadsheetHelper.getCellAsString(row.getCell(3));

            if (system == null) {
                throw new RuntimeException("A system must be specified in the code list");
            }

            version = SpreadsheetHelper.getCellAsString(row.getCell(4)) == null ? version : SpreadsheetHelper.getCellAsString(row.getCell(4));

            if (!vs.hasExpansion()) {
                vs.setExpansion(
                        new ValueSet.ValueSetExpansionComponent()
                                .setTimestamp(Date.from(Instant.now()))
                                .addContains(
                                        new ValueSet.ValueSetExpansionContainsComponent()
                                                .setSystem(system)
                                                .setCode(code)
                                                .setDisplay(description)
                                                .setVersion(meta.getCodeSystemMeta().get(system).getVersion())
                                )
                );
            }

            else {
                vs.getExpansion()
                        .addContains(
                                new ValueSet.ValueSetExpansionContainsComponent()
                                        .setSystem(system)
                                        .setCode(code)
                                        .setDisplay(description)
                                        .setVersion(meta.getCodeSystemMeta().get(system).getVersion())
                );
            }
        }
    }

    private void output(List<ValueSet> valueSets) {
        for (ValueSet valueSet : valueSets) {
            try (FileOutputStream writer = new FileOutputStream(IOUtils.concatFilePath(getOutputPath(),
                    "valueset-" + valueSet.getId() + "." + encoding))) {
                writer.write(
                        encoding.equals("json")
                                ? fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(valueSet).getBytes()
                                : fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(valueSet).getBytes()
                );
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Error outputting ValueSet: " + valueSet.getId());
            }
        }
    }
}
