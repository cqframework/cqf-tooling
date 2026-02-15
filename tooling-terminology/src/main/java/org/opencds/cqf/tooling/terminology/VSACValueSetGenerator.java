package org.opencds.cqf.tooling.terminology;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class VSACValueSetGenerator extends Operation {

    public static final String VSAC_BASE_URL = "http://cts.nlm.nih.gov/fhir/ValueSet/";

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)

    // Meta sheet defaults
    private int metaSheetNum = 0; // -metasheetnum (-msn)
    private int metaNameRow = 1; // -metanamerow (-mnr)
    private int metaOidRow = 3; // -metaoidrow (-mor)
    private int metaVersionRow = 5; // -metaversionrow (-mvr)
    private int metaStewardRow = 6; // -metastewardrow (-msd)
    private int metaClinicalFocusRow = 10; // -metaclinicalfocusrow (-mcfr)
    private int metaDataElementScopeRow = 11; // -metascoperow (-msr)
    private int metaInclusionRow = 12; // -metainclusionrow (-mir)
    private int metaExclusionRow = 13; // -metaexclusionrow (-mer)

    // Code sheet defaults
    private int codeSheetNum = 1; // -codesheetnum (-csn)
    private int codeListRow = 13; // -codelistrow (-clr)
    private int codeCol = 0; // -codecol (-cc)
    private int descriptionCol = 1; // -descriptioncol (-dc)
    private int systemNameCol = 2; // -systemnamecol (-snc)
    private int versionCol = 3; // -versioncol (-vc)
    private int systemOidCol = 4; // -systemoidcol (-soc)
    private String baseUrl; // -baseurl (-burl)
    private boolean setName; // -setname (-name)
    private boolean includeCompose = false; // -includecompose (-ic)
    private boolean includeExpansion = true; // -includeexpansion (-ie)
    private boolean declareCPGProfiles = true; // -declarecpg (-cpg)

    private Map<Integer, org.opencds.cqf.tooling.terminology.ValueSet> codesBySystem = new HashMap<>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default

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
                case "metaversionrow": case "mvr": metaVersionRow = Integer.valueOf(value); break;
                case "metastewardrow": case "msd": metaStewardRow = Integer.valueOf(value); break;
                case "metaclinicalfocusrow": case "mcfr": metaClinicalFocusRow = Integer.valueOf(value); break;
                case "metascoperow": case "msr": metaDataElementScopeRow = Integer.valueOf(value); break;
                case "metainclusionrow": case "mir": metaInclusionRow = Integer.valueOf(value); break;
                case "metaexclusionrow": case "mer": metaExclusionRow = Integer.valueOf(value); break;
                case "codesheetnum": case "csn": codeSheetNum = Integer.valueOf(value); break;
                case "codelistrow": case "clr": codeListRow = Integer.valueOf(value); break;
                case "codecol": case "cc": codeCol = Integer.valueOf(value); break;
                case "descriptioncol": case "dc": descriptionCol = Integer.valueOf(value); break;
                case "systemnamecol": case "snc": systemNameCol = Integer.valueOf(value); break;
                case "versioncol": case "vc": versionCol = Integer.valueOf(value); break;
                case "systemoidcol": case "soc": systemOidCol = Integer.valueOf(value); break;
                case "baseurl": case "burl": baseUrl = value; break;
                case "setname": case "name": setName = value.toLowerCase().equals("true") ? true : false; break;
                case "includecompose": case "ic": includeCompose = value.toLowerCase().equals("true") ? true : false; break;
                case "includeexpansion": case "ie": includeExpansion = value.toLowerCase().equals("true") ? true : false; break;
                case "declarecpg": case "cpg": declareCPGProfiles = value.toLowerCase().equals("true") ? true : false; break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }
        if (baseUrl == null) {
            baseUrl = VSAC_BASE_URL;
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        ValueSet vs = new ValueSet();
        try {
            resolveMetaData(vs, workbook);
            resolveCodeList(workbook);
            if (includeCompose) {
                resolveValueSet(vs);
                if (declareCPGProfiles) {
                    vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-computablevalueset");
                    vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("computable"));
                    vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new CodeType("structured"));
                }
            }
            if (includeExpansion) {
                resolveValueSetExpansion(vs);
                if (declareCPGProfiles) {
                    vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-executablevalueset");
                    vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("executable"));
                    vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new CodeType("executable"));
                    vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-usageWarning").setValue(new StringType("This value set contains a point-in-time expansion enumerating the codes that meet the value set intent. As new versions of the code systems used by the value set are released, the contents of this expansion will need to be updated to incorporate newly defined codes that meet the value set intent. Before, and periodically during production use, the value set expansion contents SHOULD be updated. The value set expansion specifies the timestamp when the expansion was produced, SHOULD contain the parameters used for the expansion, and SHALL contain the codes that are obtained by evaluating the value set definition. If this is ONLY an executable value set, a distributable definition of the value set must be obtained to compute the updated expansion."));
                }
            }
            //writeValueSetToFile(vs.getTitle() != null ? vs.getTitle().replaceAll("\\s", "").concat("." + encoding) : "valueset".concat("." + encoding), vs);
            writeValueSetToFile("valueset-" + vs.getId() + "." + encoding, vs);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("%s - ValueSet: %s", e.getMessage(), (vs.getTitle() == null || vs.getTitle().equals("") ? "undefined" : vs.getTitle())));
        }
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
        if (title != null) {
            title = title.replace("/", "");
            vs.setTitle(title);
        }
        String id = getSecondStringInRow(metaSheet, metaOidRow);
        //id isn't required by FHIR, but like system and code, we're requiring it.
        if (id == null || id.equals("")) {
            throw new IllegalArgumentException(String.format("No id value found for ValueSet: %s", vs.getTitle() == null || vs.getTitle().equals("") ? "untitled" : vs.getTitle()));
        }

        vs.setId(id);

        if (setName) {
            vs.setName(SpreadsheetHelper.getFHIRName(title));
        }
    
        vs.setUrl(baseUrl + id);
        String version = getSecondStringInRow(metaSheet, metaVersionRow);
        if (version != null) {
            vs.setVersion(version);
        }
        String publisher = getSecondStringInRow(metaSheet, metaStewardRow);
        if (publisher != null) {
            vs.setPublisher(publisher);
        }
        vs.setStatus(Enumerations.PublicationStatus.ACTIVE);

        String clinicalFocus = getSecondStringInRow(metaSheet, metaClinicalFocusRow);
        if (clinicalFocus != null) {
            vs.setDescription(clinicalFocus);
        }

        String scope = getSecondStringInRow(metaSheet, metaDataElementScopeRow);
        if (scope != null) {
            vs.setPurpose(scope);
        }

        if (declareCPGProfiles) {
            vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-shareablevalueset");
            vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("shareable"));
            vs.setExperimental(false);
            vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel").setValue(new CodeType("narrative"));
            vs.getMeta().addProfile("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-publishablevalueset");
            vs.addExtension().setUrl("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability").setValue(new CodeType("publishable"));
        }

        String inclusion = getSecondStringInRow(metaSheet, metaInclusionRow);
        if (inclusion != null) {
            vs.addExtension(new Extension().setUrl("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion").setValue(new StringType(inclusion)));
        }

        String exclusion = getSecondStringInRow(metaSheet, metaExclusionRow);
        if (exclusion != null) {
            vs.addExtension(new Extension().setUrl("http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion").setValue(new StringType(exclusion)));
        }
    }

    public void resolveCodeList(Workbook workbook) {
        Iterator<Row> it = workbook.getSheetAt(codeSheetNum).rowIterator();
        while(it.hasNext()) {
            Row row = it.next();
            if (row.getRowNum() < codeListRow) {
                continue;
            }

            String version = SpreadsheetHelper.getCellAsString(row.getCell(versionCol));            
            String systemName = SpreadsheetHelper.getCellAsString(row.getCell(systemNameCol));
            String display = SpreadsheetHelper.getCellAsString(row.getCell(descriptionCol));

            String code = SpreadsheetHelper.getCellAsString(row.getCell(codeCol));

            if (code == null) {
                throw new IllegalArgumentException(String.format("No code value found on row: %d", row.getRowNum()));
            }

            if (code.matches("[+-]?\\d(\\.\\d+)?[Ee][+-]?\\d+")) {
                throw new IllegalArgumentException(String.format("Scientific Notation is not allowed for a code: %s", code));
            }

            if ((version == null || version.equals(""))
                && (code == null || code.equals(""))
                && (
                    (systemName == null || systemName.equals("")) 
                        && (SpreadsheetHelper.getCellAsString(row.getCell(systemOidCol)) == null || SpreadsheetHelper.getCellAsString(row.getCell(systemOidCol)).equals(""))
                )
            ) {
                //Protecting against error where last line has no content except hidden characters introduced by copy/paste operations
                continue;
            }

            String system;
            if (systemName == null || systemName.equals("")) {
                system = SpreadsheetHelper.getCellAsString(row.getCell(systemOidCol));
                if (system == null || system.equals("")) {
                    throw new IllegalArgumentException(String.format("No system value found on row: %d", row.getRowNum()));
                }
                system = CodeSystemLookupDictionary.getUrlFromOid(system);
            }
            else {
                system = CodeSystemLookupDictionary.getUrlFromName(systemName);
            }

            if (system == null || system.equals("")) {
                throw new IllegalArgumentException(String.format("No system value found on row: %d", row.getRowNum()));
            }

            int hash = system.hashCode() * (version != null && !version.equals("") ? version.hashCode() : 1);

            if (!codesBySystem.containsKey(hash)) {
                codesBySystem.put(hash, new org.opencds.cqf.tooling.terminology.ValueSet().setSystem(system).setVersion(version));
            }

            ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent().setCode(code).setDisplay(display);

            codesBySystem.get(hash).addCode(concept);
        }
    }

    public void resolveValueSet(ValueSet vs) {
        vs.setCompose(new ValueSet.ValueSetComposeComponent());
        for (Map.Entry<Integer, org.opencds.cqf.tooling.terminology.ValueSet> entry : codesBySystem.entrySet()) {
            ValueSet.ConceptSetComponent component = new ValueSet.ConceptSetComponent();
            component.setSystem(entry.getValue().getSystem()).setVersion(entry.getValue().getVersion()).setConcept(entry.getValue().getCodes());
            vs.setCompose(vs.getCompose().addInclude(component));
        }
    }

    public void resolveValueSetExpansion(ValueSet vs) {
        vs.setExpansion(new ValueSet.ValueSetExpansionComponent());
        vs.getExpansion().setTimestamp(Date.from(Instant.now()));
        for (Map.Entry<Integer, org.opencds.cqf.tooling.terminology.ValueSet> entry : codesBySystem.entrySet()) {
            for (ValueSet.ConceptReferenceComponent crc : entry.getValue().getCodes()) {
                vs.getExpansion().addContains().setCode(crc.getCode()).setSystem(entry.getValue().getSystem()).setVersion(entry.getValue().getVersion()).setDisplay(crc.getDisplay());
            }
        }
    }

    //should bundle and store in a bundles dir
    private void writeValueSetToFile(String fileName, ValueSet vs) {
        IParser parser =
                encoding == null
                        ? FhirContext.forDstu3Cached().newJsonParser()
                        : encoding.toLowerCase().startsWith("j")
                                ? FhirContext.forDstu3Cached().newJsonParser()
                                : FhirContext.forDstu3Cached().newXmlParser();
        try (FileOutputStream writer = new FileOutputStream(IOUtils.concatFilePath(getOutputPath(), fileName))) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(vs).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }
}
