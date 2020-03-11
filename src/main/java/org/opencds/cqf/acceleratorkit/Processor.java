package org.opencds.cqf.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.*;

import org.jetbrains.annotations.NotNull;
import org.opencds.cqf.Operation;
import org.opencds.cqf.modelinfo.*;
import org.opencds.cqf.terminology.SpreadsheetHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Bryn on 8/18/2019.
 */
public class Processor extends Operation {
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)
    private String scopes; // -scopes (-s)

    // Data Elements
    private String dataElementPages; // -dataelementpages (-dep) comma-separated list of the names of pages in the workbook to be processed

    // Canonical Base
    private String canonicalBase = null;
    private Map<String, String> scopeCanonicalBaseMap = new HashMap<String, String>();

    private String openMRSSystem = "http://openmrs.org/concepts";
    // NOTE: for now, disable open MRS system/codes
    private boolean enableOpenMRS = false;
    private Map<String, String> supportedCodeSystems = new HashMap<String, String>();

    private Map<String, StructureDefinition> fhirModelStructureDefinitions = new HashMap<String, StructureDefinition>();
    private Map<String, DictionaryElement> elementMap = new HashMap<String, DictionaryElement>();
    private List<StructureDefinition> extensions = new ArrayList<StructureDefinition>();
    private List<StructureDefinition> profiles = new ArrayList<StructureDefinition>();
    private List<CodeSystem> codeSystems = new ArrayList<CodeSystem>();
    private List<ValueSet> valueSets = new ArrayList<ValueSet>();
    private List<String> igJsonFragments = new ArrayList<String>();
    private List<String> igResourceFragments = new ArrayList<String>();

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/acceleratorkit/output"); // default
        for (String arg : args) {
            if (arg.equals("-ProcessAcceleratorKit")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break; // -pathtospreadsheet (-pts)
                case "encoding": case "e": encoding = value.toLowerCase(); break; // -encoding (-e)
                case "dataelementpages": case "dep": dataElementPages = value; break; // -dataelementpages (-dep)
                case "scopes": case "s": scopes = value; break; // -scopes (-s)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        scopeCanonicalBaseMap.put("core", "http://hl7.org/fhir/dbcg/core/ImplementationGuide/core");
        scopeCanonicalBaseMap.put("fp", "http://hl7.org/fhir/dbcg/fp-cds/ImplementationGuide/fp-cds");
        scopeCanonicalBaseMap.put("sti", "http://hl7.org/fhir/dbcg/sti-cds/ImplementationGuide/sti-cds");

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        if (enableOpenMRS) {
            supportedCodeSystems.put("OpenMRS", openMRSSystem);
        }
        supportedCodeSystems.put("ICD-10-WHO", "http://hl7.org/fhir/sid/icd-10");
        supportedCodeSystems.put("SNOMED-CT", "http://snomed.info/sct");
        supportedCodeSystems.put("LOINC", "http://loinc.org");
        supportedCodeSystems.put("RxNorm", "http://www.nlm.nih.gov/research/umls/rxnorm");

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        loadFHIRModel();

        if (scopes == null) {
            processScope(workbook, null);
        }
        else {
            for (String scope : scopes.split(",")) {
                processScope(workbook, scope);
            }
        }
    }

    private void loadFHIRModel() {
        //TODO: Expose as an arg
        String inputPath = Paths.get("/Users/Adam/Src/cqframework/FHIR-Spec").toString();
        String resourcePaths = "4.0.0/StructureDefinition";

        ResourceLoader loader = new ResourceLoader();
        fhirModelStructureDefinitions = loader.loadPaths(inputPath, resourcePaths);
    }

    private void processScope(Workbook workbook, String scope) {
        // reset variables
        elementMap = new HashMap<>();
        extensions = new ArrayList<>();
        profiles = new ArrayList<>();
        codeSystems = new ArrayList<>();
        valueSets = new ArrayList<>();
        igJsonFragments = new ArrayList<>();
        igResourceFragments = new ArrayList<>();

        // ensure scope folder exists
        String scopePath = getScopePath(scope);
        ensurePath(scopePath);

        if (scope != null && scope.length() > 0) {
            canonicalBase = scopeCanonicalBaseMap.get(scope.toLowerCase());
        }

        // process workbook
        for (String page : dataElementPages.split(",")) {
            processDataElementPage(workbook, page, scope);
        }

        // process element map
        processElementMap();

        // write all resources
        //writeExtensions(scopePath);
        writeProfiles(scopePath);
        writeCodeSystems(scopePath);
        writeValueSets(scopePath);

        //ig.json is deprecated and resources a located by convention. If our output isn't satisfying convention, we should
        //modify the tooling to match the convention.
        //writeIgJsonFragments(scopePath);
        //writeIgResourceFragments(scopePath);
    }

    private void ensurePath(String path) {
        //Creating a File object
        java.io.File scopeDir = new java.io.File(path);
        //Creating the directory
        if (!scopeDir.exists()) {
            if (!scopeDir.mkdirs()) {
                // TODO: change this to an IOException
                throw new IllegalArgumentException("Could not create directory: " + path);
            }
        }
    }

    private String getScopePath(String scope) {
        if (scope == null) {
            return getOutputPath();
        }
        else {
            return getOutputPath() + "/" + scope;
        }
    }

    private void ensureExtensionsPath(String scopePath) {
        String extensionsPath = getProfilesPath(scopePath);
        ensurePath(extensionsPath);
    }

    private String getExtensionsPath(String scopePath) {
        return scopePath + "/input/extensions";
    }

    private void ensureProfilesPath(String scopePath) {
        String profilesPath = getProfilesPath(scopePath);
        ensurePath(profilesPath);
    }

    private String getProfilesPath(String scopePath) {
        return scopePath + "/input/profiles";
    }

    private void ensureCodeSystemPath(String scopePath) {
        String codeSystemPath = getCodeSystemPath(scopePath);
        ensurePath(codeSystemPath);
    }

    private String getCodeSystemPath(String scopePath) {
        return scopePath + "/input/vocabulary/codesystem";
    }

    private void ensureValueSetPath(String scopePath) {
        String valueSetPath = getValueSetPath(scopePath);
        ensurePath(valueSetPath);
    }

    private String getValueSetPath(String scopePath) {
        return scopePath + "/input/vocabulary/valueset";
    }

    private DictionaryCode getTerminologyCode(String codeSystemKey, String label, Row row, HashMap<String, Integer> colIds) {
        String system = supportedCodeSystems.get(codeSystemKey);
        String codeValue = SpreadsheetHelper.getCellAsString(row, getColId(colIds, codeSystemKey));
        String display = String.format("%s (%s)", label, codeSystemKey);
        if (codeValue != null && !codeValue.isEmpty()) {
            return getCode(system, label, display, codeValue, null);
        }
        return null;
    }

    private DictionaryCode getFhirCode(String label, Row row, HashMap<String, Integer> colIds) {
        String system = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirCodeSystem"));
        String display = String.format("%s (%s)", label, "FHIR");
        if (system != null && !system.isEmpty()) {
            String codeValue = SpreadsheetHelper.getCellAsString(row, getColId(colIds,"FhirR4Code"));
            if (codeValue != null && !codeValue.isEmpty()) {
                return getCode(system, label, display, codeValue, null);
            }
        }
        return null;
    }

    private DictionaryCode getOpenMRSCode(String label, Row row, HashMap<String, Integer> colIds) {
        String system = openMRSSystem;
        String parent = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityParent"));
        String display = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntity"));
        String codeValue = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityId"));
        if (codeValue != null && !codeValue.isEmpty()) {
            return getCode(system, display, label, codeValue, parent);
        }
        return null;
    }

    private DictionaryCode getPrimaryCode(String label, Row row, HashMap<String, Integer> colIds) {
        DictionaryCode code = null;
        if (enableOpenMRS) {
            code = getOpenMRSCode(label, row, colIds);
        }
        if (code == null) {
            code = getFhirCode(label, row, colIds);
        }
        if (code == null) {
            for (String codeSystemKey : supportedCodeSystems.keySet()) {
                code = getTerminologyCode(codeSystemKey, label, row, colIds);
                if (code != null) {
                    break;
                }
            }
        }
        return code;
    }

    private DictionaryCode getCode(String system, String label, String display, String codeValue, String parent) {
        DictionaryCode code = new DictionaryCode();
        code.setLabel(label);
        code.setSystem(system);
        code.setDisplay(display);
        code.setCode(codeValue);
        code.setParent(parent);
        return code;
    }

    private DictionaryFhirElementPath getFhirElementPath(Row row, HashMap<String, Integer> colIds) {
        DictionaryFhirElementPath fhirType = new DictionaryFhirElementPath();
        String resource = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4Resource"));
        if (resource != null && !resource.isEmpty()) {
            fhirType.setResource(resource);
            fhirType.setMasterDataElementPath(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MasterDataElementPath")));
            fhirType.setFhirElementType(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4ResourceType")));
            fhirType.setBaseProfile(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4BaseProfile")));
            fhirType.setVersion(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4VersionNumber")));
            fhirType.setCustomProfileId(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "CustomProfileId")));
            fhirType.setCustomValueSetName(SpreadsheetHelper.getCellAsString(row, getColId(colIds, " CustomValueSetName")));
            fhirType.setExtensionNeeded(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "ExtensionNeeded")));
            fhirType.setAdditionalFHIRMappingDetails(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4AdditionalFHIRMappingDetails")));
        }
        return fhirType;
    }

    private int getColId(HashMap<String, Integer> colIds, String colName) {
        if (colIds.containsKey(colName)) {
            return colIds.get(colName);
        }

        return -1;
    }

    private DictionaryElement createDataElement(String page, String group, Row row, HashMap<String, Integer> colIds) {
        //String label = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Label"));
        String type = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Type"));
        if (type != null) {
            type = type.trim();
            if (type.equals("Coding")) {
                String choiceType = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MultipleChoiceType"));
                if (choiceType != null) {
                    choiceType = choiceType.trim();
                    type = type + " - " + choiceType;
                }
            }
        }
        String name = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Name"));
        if (name.isEmpty()) {
            return null;
        }
        name = name.trim();
        String label = name;

        // TODO: should we throw if a duplicate is found within the same scope?
        // TODO: (core, anc, sti, fp, etc)
        if (elementMap.containsKey(name)) {
            //throw new IllegalArgumentException("Duplicate Name encountered: " + name);
            return null;
        }

        DictionaryElement e = new DictionaryElement(name);

        // Populate based on the row
        e.setPage(page);
        e.setGroup(group);
        e.setLabel(label);
        e.setType(type);
        e.setMasterDataType(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MasterDataType")));
        e.setInfoIcon(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "InfoIcon")));
        e.setDue(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Due")));
        e.setRelevance(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Relevance")));
        e.setDescription(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Description")));
        e.setNotes(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Notes")));
        e.setCalculation(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Calculation")));
        e.setConstraint(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Constraint")));
        e.setRequired(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Required")));
        e.setEditable(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Editable")));
        e.setScope(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Scope")));
        e.setCode(getPrimaryCode(name, row, colIds));
        e.setFhirElementPath(getFhirElementPath(row, colIds));

        return e;
    }

    private void addInputOptionToParentElement(Row row, HashMap<String, Integer> colIds) {
        String parentName = SpreadsheetHelper.getCellAsString(row, getColId(colIds,"InputOptionParent"));
        if (parentName != null || !parentName.isEmpty())
        {
            DictionaryElement currentElement = elementMap.get(parentName);
            if (currentElement != null) {
                String choices = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Name"));
                if (choices != null && !choices.isEmpty()) {
                    choices = choices.trim();

                    DictionaryCode code;
                    if (enableOpenMRS) {
                        // Open MRS choices
                        code = getOpenMRSCode(choices, row, colIds);
                        if (code != null) {
                            currentElement.getChoices().add(code);
                        }
                    }

                    // FHIR choices
                    String fhirCodeSystem = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirCodeSystem"));
                    if (fhirCodeSystem != null && !fhirCodeSystem.isEmpty()) {
                        code = getFhirCode(choices, row, colIds);
                        if (code != null) {
                            currentElement.getChoices().add(code);
                        }
                    }

                    // Other Terminology choices
                    for (String codeSystemKey : supportedCodeSystems.keySet()) {
                        code = getTerminologyCode(codeSystemKey, choices, row, colIds);
                        if (code != null) {
                            currentElement.getChoices().add(code);
                        }
                    }
                }
            }
        }
    }

    private void processDataElementPage(Workbook workbook, String page, String scope) {
        Sheet sheet = workbook.getSheet(page);
        Iterator<Row> it = sheet.rowIterator();
        HashMap<String, Integer> colIds = new HashMap<String, Integer>();
        String currentGroup = null;
        while (it.hasNext()) {
            Row row = it.next();
            int headerRow = 1;
            // Skip rows prior to header row
            if (row.getRowNum() < headerRow) {
                continue;
            }
            // Create column id map
            else if (row.getRowNum() == headerRow) {
                Iterator<Cell> colIt = row.cellIterator();
                while (colIt.hasNext()) {
                    Cell cell = colIt.next();
                    String header = SpreadsheetHelper.getCellAsString(cell).toLowerCase();
                    switch (header) {
                        case "core, fp, sti":
                        case "scope":
                            colIds.put("Scope", cell.getColumnIndex());
                            break;
                        case "master data type":
                            colIds.put("MasterDataType", cell.getColumnIndex());
                            break;
                        case "master data element label":
                            colIds.put("Name", cell.getColumnIndex());
                            colIds.put("Label", cell.getColumnIndex());
                            break;
                        case "data element parent for input options":
                            colIds.put("InputOptionParent", cell.getColumnIndex());
                            break;
                        // no group column in old or new spreadsheet? Ask Bryn?
                        //case "group": colIds.put("Group", cell.getColumnIndex()); break;
                        //case "data element name": colIds.put("Name", cell.getColumnIndex()); break;
                        case "due": colIds.put("Due", cell.getColumnIndex()); break;
                        // no frequency column in new master spreadsheet?
                        //case "frequency": colIds.put("Due", cell.getColumnIndex()); break;
                        // relevance not used in FHIR?
                        //case "relevance": colIds.put("Relevance", cell.getColumnIndex()); break;
                        // info icon not used in FHIR?
                        //case "info icon": colIds.put("InfoIcon", cell.getColumnIndex()); break;
                        case "description": colIds.put("Description", cell.getColumnIndex()); break;
                        case "notes": colIds.put("Notes", cell.getColumnIndex()); break;
                        case "data type": colIds.put("Type", cell.getColumnIndex()); break;
                        case "multiple choice": colIds.put("MultipleChoiceType", cell.getColumnIndex()); break;
                        case "input options": colIds.put("Choices", cell.getColumnIndex()); break;
                        case "calculation": colIds.put("Calculation", cell.getColumnIndex()); break;
                        case "validation required": colIds.put("Constraint", cell.getColumnIndex()); break;
                        case "required": colIds.put("Required", cell.getColumnIndex()); break;
                        case "editable": colIds.put("Editable", cell.getColumnIndex()); break;
                        case "openmrs entity parent": colIds.put("OpenMRSEntityParent", cell.getColumnIndex()); break;
                        case "openmrs entity": colIds.put("OpenMRSEntity", cell.getColumnIndex()); break;
                        case "openmrs entity id": colIds.put("OpenMRSEntityId", cell.getColumnIndex()); break;

                        case "custom profile id": colIds.put("CustomProfileId", cell.getColumnIndex()); break;
                        case "binding or custom value set name or reference": colIds.put("CustomValueSetName", cell.getColumnIndex()); break;
                        case "extension needed": colIds.put("ExtensionNeeded", cell.getColumnIndex()); break;

                        // fhir resource details
                        case "master data element path": colIds.put("MasterDataElementPath", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 - resource": colIds.put("FhirR4Resource", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 - resource type": colIds.put("FhirR4ResourceType", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 - base profile": colIds.put("FhirR4BaseProfile", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 - version number": colIds.put("FhirR4VersionNumber", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 - additional fhir mapping details": colIds.put("FhirR4AdditionalFHIRMappingDetails", cell.getColumnIndex()); break;


                        // terminology
                        case "fhir code system": colIds.put("FhirCodeSystem", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 code": colIds.put("FhirR4Code", cell.getColumnIndex()); break;
                        case "icd-10-who": colIds.put("ICD-10-WHO", cell.getColumnIndex()); break;
                        case "snomed-ct": colIds.put("SNOMED-CT", cell.getColumnIndex()); break;
                        case "loinc": colIds.put("LOINC", cell.getColumnIndex()); break;
                        case "rxnorm": colIds.put("RxNorm", cell.getColumnIndex()); break;
                    }
                }
                continue;
            }

            String rowScope = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Scope"));
            boolean scopeIsNull = scope == null;
            boolean scopeMatchesRowScope = rowScope != null && scope.toLowerCase().equals(rowScope.toLowerCase());

            if (scopeIsNull || scopeMatchesRowScope) {
                String masterDataType = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MasterDataType"));
                switch(masterDataType) {
                    case "Data Element":
                    case "Slice":
                        DictionaryElement e = createDataElement(page, currentGroup, row, colIds);
                        if (e != null) {
                            elementMap.put(e.getName(), e);
                        }
                        break;
                    case "Input Option":
                        addInputOptionToParentElement(row, colIds);
                        break;
                    case "Calculation":
                    case "UI Element":
                        break;
                    default:
                        // Currently unsupported/undocumented
                        break;
                }
            }
        }
    }

//    private boolean shouldCreateExtension(String baseProfile) {
//        if (baseProfile == null) {
//            return false;
//        }
//
//        switch (baseProfile.toLowerCase().trim()) {
//            case "extension needed":
//                return true;
//            default:
//                return false;
//        }
//    }

    private boolean requiresProfile(DictionaryElement element) {
        if (element == null
            || element.getMasterDataType() == null
            || element.getFhirElementPath() == null
            || (element.getFhirElementPath().getBaseProfile() != null
                    && element.getFhirElementPath().getBaseProfile().toLowerCase().trim().equals("extension needed"))
        ) {
            return false;
        }

        switch (element.getMasterDataType().toLowerCase().trim()) {
            case "data element":
            case "input option":
                return true;
            case "calculation":
            case "slice":
                // TODO: Do we need to do anything with these?
                return true;
            case "ui element":
                return false;
            default:
                return false;
        }
    }

    private boolean isMultipleChoiceElement(DictionaryElement element) {
        if (element.getType() == null) {
            return false;
        }

        switch (element.getType().toLowerCase()) {
            case "mc (select multiple)":
            case "coding - select all that apply":
            case "coding - (select all that apply":
                return true;
            default:
                return false;
        }
    }

    private void processElementMap() {
        for (DictionaryElement element : elementMap.values()) {
            if (requiresProfile(element)) {
                StructureDefinition profile = ensureProfile(element);
                profiles.add(profile);
            }
//            else if (shouldCreateExtension(element.getType())) {
//                extensions.add(createExtension(element));
//            }
        }
    }

    private String toId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return name
                .toLowerCase()
                .trim()
                // remove these characters
                .replace("(", "")
                .replace(")", "")
                .replace("[", "")
                .replace("]", "")
                .replace("\n", "")
                // replace these with ndash
                .replace(":", "-")
                .replace(",", "-")
                .replace("_", "-")
                .replace("/", "-")
                .replace(" ", "-")
                // remove multiple ndash
                .replace("----", "-")
                .replace("---", "-")
                .replace("--", "-")
                .replace(">", "greater-than")
                .replace("<", "less-than");
    }

    private boolean toBoolean(String value) {
        return "Yes".equals(value);
    }

    private String cleanseFhirType(String type) {
        if (type != null && type.length() > 0) {
            switch (type) {
                case "Integer":
                    return "integer";
                default:
                    return type;
            }
        } else {
            return type;
        }
    }

    private String getFhirTypeOfTargetElement(DictionaryFhirElementPath elementPath) {
        try {
            String resourceType = elementPath.getResourceType();
            StructureDefinition sd = fhirModelStructureDefinitions.get(resourceType);

            if (sd == null) {
                System.out.println("StructureDefinition not found - " + resourceType);
                return null;
            }

            if (isChoiceType(elementPath)) {
                String typePortion = cleanseFhirType(elementPath.getFhirElementType());
                return typePortion;
            }

            List<ElementDefinition> snapshotElements = sd.getSnapshot().getElement();

            //Start of refactor to get away from bad Optional<> pattern
//            for (ElementDefinition elementDef : snapshotElements) {
//                if (elementDef.toString().toLowerCase().equals(elementPath.getResourceTypeAndPath().toLowerCase())) {
//                    List<ElementDefinition.TypeRefComponent> types = elementDef.getType();
//                    if ()
//                }
//            }

            Optional<ElementDefinition> type = snapshotElements.stream().filter(e -> e.toString().toLowerCase().equals(elementPath.getResourceTypeAndPath().toLowerCase())).findFirst();

            if (!type.isEmpty()) {
                String elementType = type.get().getType().get(0).getCode();
                return elementType;
            } else {
                System.out.println("Could not find element: " + elementPath.getResourceTypeAndPath());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchElementException("Unable to determine FHIR Type for: " + elementPath.getResourceTypeAndPath());
        }
    }

    private boolean isChoiceType(DictionaryFhirElementPath elementPath) {
        return elementPath.getResourcePath().indexOf("[x]") >= 0;
    }

    private StructureDefinition createExtensionStructureDefinition(DictionaryElement element) {
        throw new NotImplementedException();
    }

    @NotNull
    private StructureDefinition createProfileStructureDefinition(DictionaryElement element, String customProfileId) {

        DictionaryFhirElementPath elementPath = element.getFhirElementPath();
        String resourceType = elementPath.getResourceType();

        StructureDefinition sd;
        sd = new StructureDefinition();
        sd.setId(toId((customProfileId != null && customProfileId.length() > 0) ? customProfileId : element.getName()));
        sd.setUrl(String.format("%s/StructureDefinition/%s", canonicalBase, sd.getId()));
        // TODO: version
        sd.setName((customProfileId != null && customProfileId.length() > 0) ? customProfileId : element.getName());
        sd.setTitle((customProfileId != null && customProfileId.length() > 0) ? customProfileId : element.getLabel());
        sd.setStatus(Enumerations.PublicationStatus.DRAFT);
        sd.setExperimental(false);
        // TODO: date
        // TODO: publisher
        // TODO: contact
        sd.setDescription((customProfileId != null && customProfileId.length() > 0) ? customProfileId : element.getDescription());
        // TODO: What to do with Notes?
        sd.setFhirVersion(Enumerations.FHIRVersion._4_0_0);
        sd.setKind(StructureDefinition.StructureDefinitionKind.RESOURCE);
        sd.setAbstract(false);
        // TODO: Support resources other than Observation
        sd.setType(resourceType);
        // TODO: Use baseDefinition to derive from less specialized profiles
        sd.setBaseDefinition(elementPath.getBaseProfile());
        sd.setDerivation(StructureDefinition.TypeDerivationRule.CONSTRAINT);
        sd.setDifferential(new StructureDefinition.StructureDefinitionDifferentialComponent());

        // Add root element
        ElementDefinition ed = new ElementDefinition();
        ed.setId(resourceType);
        ed.setPath(resourceType);
        ed.setMustSupport(false);
        sd.getDifferential().addElement(ed);

        // TODO: status
        // TODO: category
        // TODO: subject
        // TODO: effective[x]

        return sd;
    }
    
    private StructureDefinition ensureProfile(DictionaryElement element) {
        StructureDefinition sd = null;
        List<ElementDefinition> elementDefinitions = new ArrayList<>();

        // If custom profile is specified, search for if it exists already.
        String customProfileId = element.getFhirElementPath().getCustomProfileId();
        if (customProfileId != null && customProfileId.length() > 0) {
            customProfileId = toId(customProfileId);

            for (StructureDefinition existingSD : profiles) {
                if (existingSD.getId().equals(customProfileId)) {
                    sd = existingSD;
                }
            }
        }

        // If the profile doesn't exist, create it with the root element.
        if (sd == null) {
            sd = createProfileStructureDefinition(element, customProfileId);
        }

        // Ensure that the element is added to the StructureDefinition
        ensureElement(element, sd);

        return sd;
    }

    private void ensureElement(DictionaryElement element, StructureDefinition sd) {
        String codePath = null;
        String choicesPath;

        DictionaryFhirElementPath elementPath = element.getFhirElementPath();
        String resourceType = elementPath.getResourceType();

        switch (resourceType) {
            case "Observation":
                // For observations...
                codePath = "code";
                choicesPath = elementPath.getResourcePath(); //"value[x]";
                break;
            case "AllergyIntolerance":
            case "Appointment":
            case "CarePlan":
            case "Communication":
            case "Condition":
            case "Consent":
            case "Coverage":
            case "DeviceUseStatement":
            case "DocumentReference":
            case "Encounter":
            case "HealthcareService":
            case "Location":
            case "Medication":
            case "MedicationAdministration":
            case "MedicationDispense":
            case "MedicationStatement":
            case "OccupationalData":
            case "Patient":
            case "Practitioner":
            case "PractitionerRole":
            case "Procedure":
            case "ServiceRequest":
            case "Specimen":
                choicesPath = elementPath.getResourcePath();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized baseType: " + resourceType.toString());
        }

        if (codePath != null && !codePath.isEmpty() && element.getCode() != null) {
            // code - Fixed to the value of the OpenMRS code for this DictionaryElement
            ElementDefinition ed = new ElementDefinition();
            ed.setId(String.format("%s.%s", resourceType, codePath));
            ed.setPath(String.format("%s.%s", resourceType, codePath));
            ed.setMin(1);
            ed.setMax("1");
            ed.setMustSupport(true);
            ed.setFixed(element.getCode().toCodeableConcept());
            sd.getDifferential().addElement(ed);
        }
        else {
            Boolean isSlice = element.getMasterDataType().toLowerCase().equals("slice");
            String masterDataElementPath = elementPath.getMasterDataElementPath();
            Boolean isElementOfSlice = !isSlice &&  masterDataElementPath!= null && !masterDataElementPath.isEmpty() && masterDataElementPath.indexOf(".") > 0;

            String elementId;
            String sliceName = null;
            String slicePath = null;
            if (isSlice || isElementOfSlice) {
                int periodIndex = masterDataElementPath.indexOf(".");
                sliceName = periodIndex > 0 ? masterDataElementPath.substring(0, periodIndex) : masterDataElementPath;
                slicePath = periodIndex > 0 ? masterDataElementPath.substring(periodIndex + 1) : masterDataElementPath;

                String resource = elementPath.getResourceTypeAndPath();
                int elementPathStartIndex = resource.indexOf(slicePath);
                if (isSlice) {
                    elementId = String.format("%s:%s", resource, sliceName);
                } else {
                    elementId = String.format("%s:%s.%s", resource.substring(0, elementPathStartIndex - 1), sliceName, resource.substring(elementPathStartIndex));
                }
            } else {
                elementId = String.format("%s.%s", resourceType, choicesPath);
            }

            ElementDefinition existingElement = null;
            for (ElementDefinition elementDef : sd.getDifferential().getElement()) {
                if (elementDef.getId().equals(elementId)) {
                    existingElement = elementDef;
                    break;
                }
            }

            // if the element doesn't exist, create it
            if (existingElement == null) {
                if (isSlice) {
                    ensureSliceAndBaseElementWithSlicing(element, elementPath, sd, elementId, sliceName, null);
                } else {
                    ElementDefinition ed = new ElementDefinition();
                    ed.setId(elementId);
                    ed.setPath(elementPath.getResourceTypeAndPath());
                    ed.setMin(toBoolean(element.getRequired()) ? 1 : 0);
                    ed.setMax(isMultipleChoiceElement(element) ? "*" : "1");
                    ed.setMustSupport(true);

                    ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
                    String elementFhirType = getFhirTypeOfTargetElement(elementPath);
                    if (elementFhirType != null && elementFhirType.length() > 0) {
                        tr.setCode(elementFhirType);
                        ed.addType(tr);
                    }

                    ensureAndBindElementTerminology(element, sd, ed);
                    sd.getDifferential().addElement(ed);
                }
            }
        }
    }

    private void ensureSliceAndBaseElementWithSlicing(DictionaryElement dictionaryElement, DictionaryFhirElementPath elementPath,
        StructureDefinition sd, String elementId, String sliceName, ElementDefinition elementDefinition) {

        // Ensure the base definition exists
        String baseElementId = elementId.replace(":" + sliceName, "");
        ElementDefinition existingBaseDefinition = null;
        for (ElementDefinition ed : sd.getDifferential().getElement()) {
            if (ed.getId().equals(baseElementId)) {
                existingBaseDefinition = ed;
            }
        }

        if (existingBaseDefinition != null) {
            ElementDefinition.DiscriminatorType discriminatorType = ElementDefinition.DiscriminatorType.VALUE;
            String discriminatorPath = elementPath.getAdditionalFHIRMappingDetails().split("=")[0].trim();
            String resourceTypePath = elementPath.getResourceTypeAndPath();
            discriminatorPath = discriminatorPath.replaceAll(resourceTypePath + ".", "");

            ensureElementHasSlicingWithDiscriminator(existingBaseDefinition, discriminatorType, discriminatorPath);
        }
        else {
            ElementDefinition ed = new ElementDefinition();
            ed.setId(baseElementId);
            ed.setPath(elementPath.getResourceTypeAndPath());
            ed.setMin(toBoolean(dictionaryElement.getRequired()) ? 1 : 0);
            //ed.setMax(isMultipleChoiceElement(dictionaryElement) ? "*" : "1");
            ed.setMax("*");
            ed.setMustSupport(true);

            ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
            String elementFhirType = getFhirTypeOfTargetElement(elementPath);
            if (elementFhirType != null && elementFhirType.length() > 0) {
                tr.setCode(elementFhirType);
                ed.addType(tr);
            }

            ElementDefinition.DiscriminatorType discriminatorType = ElementDefinition.DiscriminatorType.VALUE;
            String discriminatorPath = elementPath.getAdditionalFHIRMappingDetails().split("=")[0].trim();
            String resourceTypePath = elementPath.getResourceTypeAndPath();
            discriminatorPath = discriminatorPath.replaceAll(resourceTypePath + ".", "");

            ensureElementHasSlicingWithDiscriminator(ed, discriminatorType, discriminatorPath);

            sd.getDifferential().addElement(ed);
        }

        // Add the actual slice

        /* Add the actual Slice (e.g., telecom:Telephone1) */
        String discriminatorValue = elementPath.getAdditionalFHIRMappingDetails().split("=")[1].trim();
        ElementDefinition sliceElement = new ElementDefinition();
        sliceElement.setId(elementId);
        sliceElement.setSliceName(sliceName);
//        sliceElement.setBase()
        sliceElement.setPath(elementPath.getResourceTypeAndPath());
        // NOTE: Passing everything through as a string for now.
        sliceElement.setFixed(new StringType(discriminatorValue));
        sliceElement.setMin(toBoolean(dictionaryElement.getRequired()) ? 1 : 0);
        sliceElement.setMax(isMultipleChoiceElement(dictionaryElement) ? "*" : "1");

        sd.getDifferential().addElement(sliceElement);
    }

    private void ensureElementHasSlicingWithDiscriminator(ElementDefinition element, ElementDefinition.DiscriminatorType discriminatorType, String discriminatorPath) {
        // If the element has a slicing component, ensure the discriminator exists on it.
        if (element.hasSlicing()) {
            // If discriminator does not exist on the slicing component add it else do nothing
            ElementDefinition.ElementDefinitionSlicingComponent existingSlicingComponent = element.getSlicing();
            ensureSlicingHasDiscriminator(existingSlicingComponent, discriminatorType, discriminatorPath);
        } else {
            /* Add Slicing to base element if it's not already there */
            ElementDefinition.ElementDefinitionSlicingComponent slicingComponent = new ElementDefinition.ElementDefinitionSlicingComponent();
            ensureSlicingHasDiscriminator(slicingComponent, discriminatorType, discriminatorPath);
            element.setSlicing(slicingComponent);
        }
    }

    private void ensureSlicingHasDiscriminator(ElementDefinition.ElementDefinitionSlicingComponent slicingComponent,
        ElementDefinition.DiscriminatorType discriminatorType, String discriminatorPath) {

        ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent discriminator = null;
        if (slicingComponent.getDiscriminator().stream().noneMatch(d -> d.getType() == discriminatorType && d.getPath().toLowerCase().equals(discriminatorPath.toLowerCase()))) {
            discriminator = new ElementDefinition.ElementDefinitionSlicingDiscriminatorComponent();
            discriminator.setType(discriminatorType);
            discriminator.setPath(discriminatorPath);

            slicingComponent.addDiscriminator(discriminator);
        }
    }

    private void ensureAndBindElementTerminology(DictionaryElement element, StructureDefinition sd, ElementDefinition ed) {
        // binding and CodeSystem/ValueSet for MultipleChoice elements
        if (element.getChoices().size() > 0) {
            CodeSystem codeSystem = new CodeSystem();
            if (enableOpenMRS && element.getChoicesForSystem(openMRSSystem).size() > 0) {
                codeSystem.setId(sd.getId() + "-codes");
                codeSystem.setUrl(String.format("%s/CodeSystem/%s", canonicalBase, codeSystem.getId()));
                // TODO: version
                codeSystem.setName(element.getName() + "_codes");
                codeSystem.setTitle(String.format("%s codes", element.getLabel()));
                codeSystem.setStatus(Enumerations.PublicationStatus.DRAFT);
                codeSystem.setExperimental(false);
                // TODO: date
                // TODO: publisher
                // TODO: contact
                codeSystem.setDescription(String.format("Codes representing possible values for the %s element", element.getLabel()));
                codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
                codeSystem.setCaseSensitive(true);

                // collect all the OpenMRS choices to add to the codeSystem
                for (DictionaryCode code : element.getChoicesForSystem(openMRSSystem)) {
                    CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                    concept.setCode(code.getCode());
                    concept.setDisplay(code.getLabel());
                    codeSystem.addConcept(concept);
                }

                codeSystems.add(codeSystem);
            }

            ValueSet valueSet = new ValueSet();
            valueSet.setId(toId(element.getName()) + "-values");//sd.getId() + "-values");
            valueSet.setUrl(String.format("%s/ValueSet/%s", canonicalBase, valueSet.getId()));
            // TODO: version
            valueSet.setName(toId(element.getName()) + "-values");
            valueSet.setTitle(String.format("%s values", element.getLabel()));
            valueSet.setStatus(Enumerations.PublicationStatus.DRAFT);
            valueSet.setExperimental(false);
            // TODO: date
            // TODO: publisher
            // TODO: contact
            valueSet.setDescription(String.format("Codes representing possible values for the %s element", element.getLabel()));
            valueSet.setImmutable(true);
            ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
            valueSet.setCompose(compose);

            // Group by Supported Terminology System
            for (String codeSystemUrl : element.getCodeSystemUrls()) {
                List<DictionaryCode> systemCodes = element.getChoicesForSystem(codeSystemUrl);

                if (systemCodes.size() > 0) {
                    ValueSet.ConceptSetComponent conceptSet = new ValueSet.ConceptSetComponent();
                    compose.addInclude(conceptSet);
                    conceptSet.setSystem(codeSystemUrl);

                    for (DictionaryCode code : systemCodes) {
                        ValueSet.ConceptReferenceComponent conceptReference = new ValueSet.ConceptReferenceComponent();
                        conceptReference.setCode(code.getCode());
                        conceptReference.setDisplay(code.getLabel());
                        conceptSet.addConcept(conceptReference);
                    }
                }
            }

            if (element.getChoicesForSystem(openMRSSystem).size() == element.getChoices().size()) {
                codeSystem.setValueSet(valueSet.getUrl());
            }

            valueSets.add(valueSet);

            ElementDefinition.ElementDefinitionBindingComponent binding = new ElementDefinition.ElementDefinitionBindingComponent();
            binding.setStrength(Enumerations.BindingStrength.REQUIRED);
            binding.setValueSet(valueSet.getUrl());
            ed.setBinding(binding);
        }
    }

    /* Write Methods */
    public void writeResource(String path, Resource resource) {
        String outputFilePath = path + "/" + resource.getResourceType().toString().toLowerCase() + "-" + resource.getId() + "." + encoding;
        try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
            writer.write(
                encoding.equals("json")
                    ? FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                    : FhirContext.forR4().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing resource: " + resource.getId());
        }
    }

    public void writeProfiles(String scopePath) {
        if (profiles != null && profiles.size() > 0) {
            String profilesPath = getProfilesPath(scopePath);
            ensureProfilesPath(scopePath);

            Comparator<ElementDefinition> compareById = Comparator.comparing(Element::getId);

            for (StructureDefinition sd : profiles) {
                sd.getDifferential().getElement().sort(compareById);
                writeResource(profilesPath, sd);

                // Generate JSON fragment for inclusion in the IG:
                /*
                    "StructureDefinition/<id>": {
                        "source": "structuredefinition/structuredefinition-<id>.json",
                        "defns": "StructureDefinition-<id>-definitions.html",
                        "base": "StructureDefinition-<id>.html"
                    }
                 */
                igJsonFragments.add(String.format("\t\t\"StructureDefinition/%s\": {\r\n\t\t\t\"source\": \"structuredefinition/structuredefinition-%s.json\",\r\n\t\t\t\"defns\": \"StructureDefinition-%s-definitions.html\",\r\n\t\t\t\"base\": \"StructureDefinition-%s.html\"\r\n\t\t}",
                        sd.getId(), sd.getId(), sd.getId(), sd.getId()));

                // Generate XML fragment for the IG resource:
                /*
                    <resource>
                        <reference>
                            <reference value="StructureDefinition/<id>"/>
                        </reference>
                        <groupingId value="main"/>
                    </resource>
                 */
                igResourceFragments.add(String.format("\t\t\t<resource>\r\n\t\t\t\t<reference>\r\n\t\t\t\t\t<reference value=\"StructureDefinition/%s\"/>\r\n\t\t\t\t</reference>\r\n\t\t\t\t<groupingId value=\"main\"/>\r\n\t\t\t</resource>", sd.getId()));
            }
        }
    }

    public void writeCodeSystems(String scopePath) {
        if (codeSystems != null && codeSystems.size() > 0) {
            String codeSystemPath = getCodeSystemPath(scopePath);
            ensureCodeSystemPath(scopePath);

            for (CodeSystem cs : codeSystems) {
                writeResource(codeSystemPath, cs);

                // Generate JSON fragment for inclusion in the IG:
                /*
                    "CodeSystem/<id>": {
                        "source": "codesystem/codesystem-<id>.json",
                        "base": "CodeSystem-<id>.html"
                    }
                 */
                igJsonFragments.add(String.format("\t\t\"CodeSystem/%s\": {\r\n\t\t\t\"source\": \"codesystem/codesystem-%s.json\",\r\n\t\t\t\"base\": \"CodeSystem-%s.html\"\r\n\t\t}",
                        cs.getId(), cs.getId(), cs.getId()));

                // Generate XML fragment for the IG resource:
                /*
                    <resource>
                        <reference>
                            <reference value="CodeSystem/<id>"/>
                        </reference>
                        <groupingId value="main"/>
                    </resource>
                 */
                igResourceFragments.add(String.format("\t\t\t<resource>\r\n\t\t\t\t<reference>\r\n\t\t\t\t\t<reference value=\"CodeSystem/%s\"/>\r\n\t\t\t\t</reference>\r\n\t\t\t\t<groupingId value=\"main\"/>\r\n\t\t\t</resource>", cs.getId()));
            }
        }
    }

    public void writeValueSets(String scopePath) {
        if (valueSets != null && valueSets.size() > 0) {
            String valueSetPath = getValueSetPath(scopePath);
            ensureValueSetPath(scopePath);

            for (ValueSet vs : valueSets) {
                writeResource(valueSetPath, vs);

                // Generate JSON fragment for inclusion in the IG:
                /*
                    "ValueSet/<id>": {
                        "source": "valueset/valueset-<id>.json",
                        "base": "ValueSet-<id>.html"
                    }
                 */
                igJsonFragments.add(String.format("\t\t\"ValueSet/%s\": {\r\n\t\t\t\"source\": \"valueset/valueset-%s.json\",\r\n\t\t\t\"base\": \"ValueSet-%s.html\"\r\n\t\t}",
                        vs.getId(), vs.getId(), vs.getId()));

                // Generate XML fragment for the IG resource:
                /*
                    <resource>
                        <reference>
                            <reference value="ValueSet/<id>"/>
                        </reference>
                        <groupingId value="main"/>
                    </resource>
                 */
                igResourceFragments.add(String.format("\t\t\t<resource>\r\n\t\t\t\t<reference>\r\n\t\t\t\t\t<reference value=\"ValueSet/%s\"/>\r\n\t\t\t\t</reference>\r\n\t\t\t\t<groupingId value=\"main\"/>\r\n\t\t\t</resource>", vs.getId()));
            }
        }
    }

    public void writeIgJsonFragments(String path) {
        try (FileOutputStream writer = new FileOutputStream(path + "/ig.json")) {
            writer.write(String.format("{\r\n%s\r\n}", String.join(",\r\n", igJsonFragments)).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ig.json fragment");
        }
    }

    public void writeIgResourceFragments(String path) {
        try (FileOutputStream writer = new FileOutputStream(path + "/ig.xml")) {
            writer.write(String.format(String.join("\r\n", igResourceFragments)).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ig.xml fragment");
        }
    }
}
