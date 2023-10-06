package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.terminology.distributable.OrganizationalMetaData;

import ca.uhn.fhir.context.FhirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateToValueSetGenerator extends Operation {

    private static final Logger logger = LoggerFactory.getLogger(TemplateToValueSetGenerator.class);

    private FhirContext fhirContext;

    public TemplateToValueSetGenerator() {
        this.fhirContext = FhirContext.forR4Cached();
    }

    private String pathToSpreadsheet;           // -pathtospreadsheet (-pts)
    private String encoding = "json";      // -encoding (-e)
    private String outputPrefix = "valueset-"; // -outputPrefix (-opp)
    private String outputVersion = "r4";        // -opv
    private final static String SNOMEDCT = "http://snomed.info/sct";
    private final static String LOINC = "http://loinc.org";
    private final static String SNOMEDCT_BAD_URL = "SNOMED CT";
    private final static String LOINC_BAD_URL = "LOINC";

    private Map<String, String> codeSystemDataVersionMap = new HashMap<>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/terminology/r4/output");

        for (String arg : args) {
            if (arg.equals("-TemplateToValueSetGenerator")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "outputprefix":
                case "opp":
                    outputPrefix = value;
                    break;
                case "outputversion":
                case "opv":
                    setOutputVersion(value);
                    break;
                case "pathtospreadsheet":
                case "pts":
                    pathToSpreadsheet = value;
                    break;
                case "encoding":
                case "e":
                    encoding = value.toLowerCase();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }

            if (pathToSpreadsheet == null) {
                throw new IllegalArgumentException("The path to the spreadsheet is required");
            }
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        OrganizationalMetaData organizationalMetaData = resolveOrganizationalMeta(workbook.getSheetAt(0));
        Map<String, Integer> vsMap = resolveVsMap(workbook, workbook.getSheetAt(1));
        List<ValueSet> valueSets = resolveValueSets(organizationalMetaData, vsMap, workbook);
        output(valueSets);
    }

    private void setOutputVersion(String value) {
        if (!value.equalsIgnoreCase("r4") && !value.equalsIgnoreCase("stu3"))
            throw new IllegalArgumentException(
                    String.format("outputversion should be R4 or STU3. Unsupported FHIR version specified: %s", value));
        else
            outputVersion = value;
    }

    /**
     * Iterates over each row in sheet params, builds OrganizationalMeta obj with pre-defined strs.
     *
     * @param sheet
     * @return OrganizationalMeta object with properties parsed from given sheet.
     */
    private OrganizationalMetaData resolveOrganizationalMeta(Sheet sheet) {
        OrganizationalMetaData organizationalMetaData = new OrganizationalMetaData();
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            switch (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()) {
                case "Canonical URL":
                    organizationalMetaData.setCanonicalUrlBase(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Copyright":
                    organizationalMetaData.setCopyright(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Jurisdiction":
                    organizationalMetaData.setJurisdiction(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "Publisher":
                    organizationalMetaData.setPublisher(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "approvalDate":
                    organizationalMetaData.setApprovalDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "effectiveDate":
                    organizationalMetaData.setEffectiveDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "lastReviewDate":
                    organizationalMetaData.setLastReviewDate(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.name":
                    organizationalMetaData.setAuthorName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.telecom.system":
                    organizationalMetaData.setAuthorTelecomSystem(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "author.telecom.value":
                    organizationalMetaData.setAuthorTelecomValue(SpreadsheetHelper.getCellAsString(row.getCell(1)));
                    break;
                case "CodeSystems":
                    while (rowIterator.hasNext()) {
                        row = rowIterator.next();
                        String urlCell = SpreadsheetHelper.getCellAsString(row.getCell(1));
                        if (null != urlCell && urlCell.length() > 0) {
                            codeSystemDataVersionMap.put(SpreadsheetHelper.getCellAsString(row.getCell(1)), SpreadsheetHelper.getCellAsString(row.getCell(2)));
                        }

                    }
                    break;
                default:
                    break;
            }
        }
        return organizationalMetaData;
    }

    /**
     * Iterates over each row in sheet params, builds CPGMeta obj with pre-defined strs.
     *
     * @param sheet
     * @return CPGMeta object with properties parsed from given sheet.
     */
    private CPGMeta resolveCpgMeta(Sheet sheet) {
        CPGMeta meta = new CPGMeta();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            CellType cellType = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType();
            String cellString = cellType.compareTo(CellType.STRING) == 0
                    ? row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()
                    : String.valueOf(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue());

            switch (cellString) {
                case "id":
                    meta.setId(SpreadsheetHelper.getCellAsString(row.getCell(1)).toLowerCase());
                    // In the latest workbook we were given, many valuesets had unpopulated name cells.
                    // However each valueset had a populated id cell.
                    meta.setName(meta.getId());
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
//                case "name":
//                    meta.setName(SpreadsheetHelper.getCellAsString(row.getCell(1)));
//                    break;
                case "title":
                    String title = SpreadsheetHelper.getCellAsString(row.getCell(1));
                    meta.setTitle(title);
                    if ((null != title && title.length() > 0) && (null == meta.getId() || meta.getId().length() < 1)) {
                        meta.setId(title.toLowerCase(Locale.ROOT).replace(" ", "-"));
                    }
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
                    if (row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().length() > 1)
                        meta.setCompose(SpreadsheetHelper.getCellAsStringNoReplacement(row.getCell(1)));
                    break;
                default:
                    break;
            }
        }
        return meta;
    }

    private Map<String, Integer> resolveVsMap(Workbook workbook, Sheet sheet) {
        Map<String, Integer> vsMap = new HashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row row = rowIterator.next();// skip first row
        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            String sheetName = SpreadsheetHelper.getCellAsString(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));

            if (sheetName.length() <= 0) continue;

            vsMap.put(
                    SpreadsheetHelper.getCellAsString(row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)),
                    row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getRowIndex());
        }
        return vsMap;
    }

    /**
     * Iterates over vsMap.entrySet() to resolve & populate CPGMeta objects.
     *
     * @param meta
     * @param vsMap
     * @param workbook
     * @return List of ValueSets
     */
    private List<ValueSet> resolveValueSets(OrganizationalMetaData meta, Map<String, Integer> vsMap, Workbook workbook) {
        List<ValueSet> valueSets = new ArrayList<>();
        CPGMeta cpgMeta;
        ValueSet vs;

        for (Map.Entry<String, Integer> entrySet : vsMap.entrySet()) {
            Sheet sheet = workbook.getSheet(entrySet.getKey());
            if (null != sheet) {
                cpgMeta = resolveCpgMeta(sheet);
                try {
                    if (cpgMeta.getTitle().equals("only fill this out"))
                        continue;
                } catch (NullPointerException e) {
                    logger.error("cpg instance had null title");
                }

                vs = cpgMeta.populate(fhirContext, outputVersion);
                meta.populate(vs, outputVersion);
                resolveCodeList(workbook.getSheet(entrySet.getKey().split("-")[0] + "-cl"), vs, meta.getSnomedVersion());

                if (outputVersion.equalsIgnoreCase("r4")) {

                    // bausstin 9/17/2021 cannot separate the 2 extensions. Note that they havedifferent values.
                    if (shouldAddCompose(vs, sheet)) {
                        vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-computablevalueset");
                        vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("computable"));
                        vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new CodeType("structured"));
                    }
                    if (vs.hasExpansion()) {
                        vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-executablevalueset");
                        vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("executable"));
                        vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new CodeType("executable"));
                        vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-usageWarning").setValue(new StringType("This value set contains a point-in-time expansion enumerating the codes that meet the value set intent. As new versions of the code systems used by the value set are released, the contents of this expansion will need to be updated to incorporate newly defined codes that meet the value set intent. Before, and periodically during production use, the value set expansion contents SHOULD be updated. The value set expansion specifies the timestamp when the expansion was produced, SHOULD contain the parameters used for the expansion, and SHALL contain the codes that are obtained by evaluating the value set definition. If this is ONLY an executable value set, a distributable definition of the value set must be obtained to compute the updated expansion."));
                    }
                }
                valueSets.add(vs);
            } else {
                logger.info("Workbook does NOT contain a sheet named {}", entrySet.getKey());
            }
        }
        return valueSets;
    }

    private boolean shouldAddCompose(ValueSet vs, Sheet sheet) {
        if (vs.hasCompose() && outputVersion.equalsIgnoreCase("r4")) {
            return true;
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row thisRow = rowIterator.next();
            Cell firstCell = thisRow.getCell(0);
            Cell secondCell = thisRow.getCell(1);
            if (firstCell.getStringCellValue() != null &&
                    firstCell.getStringCellValue().equalsIgnoreCase("rules-text") &&
                    secondCell != null &&
                    secondCell.getStringCellValue() != null &&
                    secondCell.getStringCellValue().length() > 0) {
                return true;
            }
            if (firstCell.getStringCellValue() != null &&
                    firstCell.getStringCellValue().equalsIgnoreCase("expression") &&
                    secondCell != null &&
                    secondCell.getStringCellValue() != null &&
                    secondCell.getStringCellValue().length() > 0) {
                return true;
            }
        }
        return false;
    }

    //this is ONLY to catch bad data in the spreadsheet where bad systems were inserted
    private String replaceBadSystem(String system) {
        if (system.equalsIgnoreCase(LOINC_BAD_URL)) {
            return LOINC;
        }
        if (system.equalsIgnoreCase(SNOMEDCT_BAD_URL)) {
            return SNOMEDCT;
        }
        return system;
    }

    /**
     * Iterates over -cl sheet adding an expansion and compose when appropriate.
     *
     * @param sheet
     * @param vs
     * @param snomedVersion
     */
    private void resolveCodeList(Sheet sheet, ValueSet vs, String snomedVersion) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        Boolean active = true;
        String system = null;
        String version = null;

        ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
        expansion.setTimestamp(Date.from(Instant.now()));

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String code = SpreadsheetHelper.getCellAsString(row.getCell(0));
            if (code.length() <= 0) continue;

            if (code == null || code.equals("Code")) continue;

            // ???
            if (code.equals("expansion")) {

            } else {
                String description = SpreadsheetHelper.getCellAsString(row.getCell(1));
                active = SpreadsheetHelper.getCellAsString(row.getCell(2)) == null ? active : Boolean.valueOf(SpreadsheetHelper.getCellAsString(row.getCell(2)));
                system = SpreadsheetHelper.getCellAsString(row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)) == null ? system : SpreadsheetHelper.getCellAsString(row.getCell(3));

                if (system == null)
                    throw new RuntimeException("A system must be specified in the code list");
                system = replaceBadSystem(system);
                version = SpreadsheetHelper.getCellAsString(
                        row.getCell(4)) == null
                        ? version : SpreadsheetHelper.getCellAsString(row.getCell(4));
                if (null == version || version.equalsIgnoreCase("")) {
                    version = this.codeSystemDataVersionMap.get(system);
                }
                // Compose
                if (!vs.hasCompose()) {
                    ValueSet.ValueSetComposeComponent vscc = new ValueSet.ValueSetComposeComponent();
                    ValueSet.ConceptSetComponent csc = new ValueSet.ConceptSetComponent();

                    vscc.addInclude(csc.setSystem(system)
                            .setSystem(system)
                            .setVersion(system.equals("http://snomed.info/sct") ? snomedVersion : version));
                }

                // Expansion
                expansion.setIdentifier(UUID.randomUUID().toString());
                expansion.addContains()
                        .setCode(code)
                        .setSystem(system)
                        .setVersion(version)
                        .setDisplay(description);
            }
        }
        vs.setExpansion(expansion);
    }

    /**
     * Writes constructed ValuSets to disk in user-defined format (default being json).
     *
     * @param valueSets
     */
    private void output(List<ValueSet> valueSets) {
        for (ValueSet valueSet : valueSets) {
            String prefixedOutputPath = String.format(
                    "%s/%s%s.%s", getOutputPath(), outputPrefix, valueSet.getName(), encoding);

            if (null != valueSet.getName()) {
                valueSet.setName(valueSet.getName().replace('-', '_'));
            }

            try (FileOutputStream writer = new FileOutputStream(prefixedOutputPath)) {
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
