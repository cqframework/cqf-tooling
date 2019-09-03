package org.opencds.cqf.terminology.r4;

import ca.uhn.fhir.context.FhirContext;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.Operation;
import org.opencds.cqf.terminology.SpreadsheetHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class OpioidValueSetGenerator extends Operation {

    private FhirContext fhirContext;

    public OpioidValueSetGenerator() {
        this.fhirContext = FhirContext.forR4();
    }

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
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        OrganizationalMeta organizationalMeta = resolveOrganizationalMeta(workbook.getSheetAt(0));
        Map<String, Integer> vsMap = resolveVsMap(workbook.getSheetAt(0));
        List<ValueSet> valueSets = resolveValueSets(organizationalMeta, vsMap, workbook);
        output(valueSets);
    }

    private OrganizationalMeta resolveOrganizationalMeta(Sheet sheet) {
        OrganizationalMeta organizationalMeta = new OrganizationalMeta();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (SpreadsheetHelper.getCellAsString(row.getCell(0))) {
                case "Canonical URL":
                    organizationalMeta.setCanonicalUrlBase(SpreadsheetHelper.getCellAsString(row.getCell(1)));
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
                case "effectiveDate":
                    organizationalMeta.setEffectiveDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
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

    private CPGMeta resolveCpgMeta(Sheet sheet) {
        CPGMeta meta = new CPGMeta();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (SpreadsheetHelper.getCellAsString(row.getCell(0))) {
                case "id":
                    meta.setId(SpreadsheetHelper.getCellAsString(row.getCell(1)).toLowerCase());
                    break;
                case "keyword":
                    meta.setKeyword(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "rules-text":
                    meta.setRulesText(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "expression.description":
                    meta.setExpressionDescription(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "expression.name":
                    meta.setExpressionName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "expression.language":
                    meta.setExpressionLanguage(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "expression.expression":
                    meta.setExpressionExpression(SpreadsheetHelper.getCellAsString(row.getCell(1)));
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

    private List<ValueSet> resolveValueSets(OrganizationalMeta meta, Map<String, Integer> vsMap, Workbook workbook) {
        List<ValueSet> valueSets = new ArrayList<>();
        CPGMeta cpgMeta;
        ValueSet vs;
        for (Map.Entry<String, Integer> entrySet : vsMap.entrySet()) {
            cpgMeta = resolveCpgMeta(workbook.getSheetAt(entrySet.getValue()));
            vs = cpgMeta.populate(fhirContext);
            meta.populate(vs);

            resolveCodeList(workbook.getSheetAt(entrySet.getValue() + 1), vs, meta.getSnomedVersion());

            valueSets.add(vs);
        }

        return valueSets;
    }

    private void resolveCodeList(Sheet sheet, ValueSet vs, String snomedVersion) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        Boolean active = true;
        String system = null;
        String version = null;

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String code = SpreadsheetHelper.getCellAsString(row.getCell(0));

            if (code.equals("Code")) continue;

            if (code.equals("expansion")) {

            }
            else {
                String description = SpreadsheetHelper.getCellAsString(row.getCell(1));
                active = SpreadsheetHelper.getCellAsString(row.getCell(2)) == null ? active : Boolean.valueOf(SpreadsheetHelper.getCellAsString(row.getCell(2)));
                system = SpreadsheetHelper.getCellAsString(row.getCell(3)) == null ? system : SpreadsheetHelper.getCellAsString(row.getCell(3));
                if (system == null) {
                    throw new RuntimeException("A system must be specified in the code list");
                }
                version = SpreadsheetHelper.getCellAsString(row.getCell(4)) == null ? version : SpreadsheetHelper.getCellAsString(row.getCell(4));

                if (!vs.hasCompose()) {
                    vs.setCompose(
                            new ValueSet.ValueSetComposeComponent()
                                    .addInclude(
                                            new ValueSet.ConceptSetComponent()
                                                    .setSystem(system)
                                                    .setVersion(system.equals("http://snomed.info/sct") ? snomedVersion : version)
                                    )
                    );
                }

                boolean added = false;
                for (ValueSet.ConceptSetComponent include : vs.getCompose().getInclude()) {
                    if (include.getSystem().equals(system) && !include.hasFilter()) {
                        include.addConcept(new ValueSet.ConceptReferenceComponent().setCode(code).setDisplay(description));
                        added = true;
                    }
                }

                if (!added) {
                    vs.getCompose()
                            .addInclude(
                                    new ValueSet.ConceptSetComponent()
                                    .setSystem(system)
                                    .setVersion(system.equals("http://snomed.info/sct") ? snomedVersion : version)
                                            .addConcept(new ValueSet.ConceptReferenceComponent().setCode(code).setDisplay(description))
                            );
                }

            }
        }
    }

    private void output(List<ValueSet> valueSets) {
        for (ValueSet valueSet : valueSets) {
            try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + valueSet.getName() + "." + encoding)) {
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
