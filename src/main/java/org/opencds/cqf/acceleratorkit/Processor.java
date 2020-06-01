package org.opencds.cqf.acceleratorkit;

import ca.uhn.fhir.context.FhirContext;
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
    private String whoCodeSystemBase = "http://who.int/cg";
    // NOTE: for now, disable open MRS system/codes
    private boolean enableOpenMRS = false;
    private Map<String, String> supportedCodeSystems = new HashMap<String, String>();

    private Map<String, StructureDefinition> fhirModelStructureDefinitions = new HashMap<String, StructureDefinition>();
    private Map<String, DictionaryElement> elementMap = new HashMap<String, DictionaryElement>();
    private List<DictionaryProfileElementExtension> profileExtensions = new ArrayList<>();
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
                case "scopes": case "s": scopes = value; break; // -scopes (-s)
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break; // -pathtospreadsheet (-pts)
                case "encoding": case "e": encoding = value.toLowerCase(); break; // -encoding (-e)
                case "dataelementpages": case "dep": dataElementPages = value; break; // -dataelementpages (-dep)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        scopeCanonicalBaseMap.put("core", "http://fhir.org/guides/who/core");
        scopeCanonicalBaseMap.put("fp", "http://fhir.org/guides/who/fp-cds");
        scopeCanonicalBaseMap.put("sti", "http://fhir.org/guides/who/sti-cds");
        scopeCanonicalBaseMap.put("cr", "http://fhir.org/guides/cqframework/cr");

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

        //TODO: Determing and add correct URLS for these Systems
        supportedCodeSystems.put("CIEL", "http://hl7.org/fhir/sid/ciel");
        supportedCodeSystems.put("ICD-11", "http://hl7.org/fhir/sid/icd-11");


        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

//        loadFHIRModel();

        if (scopes == null) {
            processScope(workbook, null);
        }
        else {
            for (String scope : scopes.split(",")) {
                processScope(workbook, scope);
            }
        }
    }

//    private void loadFHIRModel() {
//        //TODO: Expose as an arg
//        String inputPath = Paths.get("/Users/Adam/Src/cqframework/FHIR-Spec").toString();
//        String resourcePaths = "4.0.0/StructureDefinition";
//
//        ResourceLoader loader = new ResourceLoader();
//        fhirModelStructureDefinitions = loader.loadPaths(inputPath, resourcePaths);
//    }

    private void processScope(Workbook workbook, String scope) {
        // reset variables
        elementMap = new HashMap<>();
        profileExtensions = new ArrayList<>();
        extensions = new ArrayList<>();
        profiles = new ArrayList<>();
        codeSystems = new ArrayList<>();
        valueSets = new ArrayList<>();
        igJsonFragments = new ArrayList<>();
        igResourceFragments = new ArrayList<>();

        // ensure scope folder exists
//        String scopePath = getScopePath(scope);
//        ensurePath(scopePath);

        String outputPath = getOutputPath();
        ensurePath(outputPath);

        if (scope != null && scope.length() > 0) {
            canonicalBase = scopeCanonicalBaseMap.get(scope.toLowerCase());
        }

        // process workbook
        for (String page : dataElementPages.split(",")) {
            processDataElementPage(workbook, page, scope);
        }

        // process element map
        processElementMap();

        // attached the generated extensions to the profiles that reference them
        attachExtensions();

        // write all resources
        writeExtensions(outputPath);
        writeProfiles(outputPath);
        writeCodeSystems(outputPath);
        writeValueSets(outputPath);

        //ig.json is deprecated and resources a located by convention. If our output isn't satisfying convention, we should
        //modify the tooling to match the convention.
        //writeIgJsonFragments(scopePath);
        //writeIgResourceFragments(scopePath);
    }

    private void attachExtensions() {
        // Add extensions to the appropriate profiles
        for (DictionaryProfileElementExtension profileElementExtension : profileExtensions) {
            for (StructureDefinition profile : profiles) {
                if (profile.getId().equals(profileElementExtension.getProfileId())) {
                    StructureDefinition extensionDefinition = profileElementExtension.getExtension();

                    String extensionName = getExtensionName(profileElementExtension.getResourcePath(), profile.getName());

                    List<ElementDefinition> extensionDifferential =  extensionDefinition.getDifferential().getElement();

                    ElementDefinition extensionBaseElement = null;
                    for (ElementDefinition ed : extensionDifferential) {
                        if (ed.getId().equals("Extension.extension")) {
                            extensionBaseElement = ed;
                            break;
                        }
                    }

                    String resourcePath = profileElementExtension.getResourcePath();
                    String pathToElementBeingExtended = resourcePath.substring(0, resourcePath.indexOf(extensionName) - 1);
                    String extensionId = pathToElementBeingExtended + ".extension:" + extensionName;

                    ElementDefinition extensionElement = new ElementDefinition();
                    extensionElement.setId(extensionId);
                    extensionElement.setPath(pathToElementBeingExtended + ".extension");
                    extensionElement.setSliceName(extensionName);
                    extensionElement.setMin(extensionBaseElement.getMin());
                    extensionElement.setMax(extensionBaseElement.getMax());

                    ElementDefinition.TypeRefComponent typeRefComponent = new ElementDefinition.TypeRefComponent();
                    List<CanonicalType> typeProfileList = new ArrayList<>();
                    typeProfileList.add(new CanonicalType(extensionDefinition.getUrl()));
                    typeRefComponent.setProfile(typeProfileList);
                    typeRefComponent.setCode("Extension");

                    List<ElementDefinition.TypeRefComponent> typeRefList = new ArrayList<>();
                    typeRefList.add(typeRefComponent);

                    extensionElement.setType(typeRefList);

                    profile.getDifferential().addElement(extensionElement);
                }
            }
        }
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

//    private String getScopePath(String scope) {
//        if (scope == null) {
//            return getOutputPath();
//        }
//        else {
//            return getOutputPath() + "/" + scope;
//        }
//    }

    private void ensureExtensionsPath(String scopePath) {
        String extensionsPath = getExtensionsPath(scopePath);
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

    private List<DictionaryCode> getTerminologyCodes(String codeSystemKey, String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = supportedCodeSystems.get(codeSystemKey);
        String codeListString = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, codeSystemKey));
        if (codeListString != null && !codeListString.isEmpty()) {
            List<String> codesList = Arrays.asList(codeListString.split(";"));
            String display;
            for (String c : codesList) {
                //TODO: This is wrong. We need a solution for constructing Display. Likely needs to be in input in the Data Dictionary.
                display = String.format("%s (%s)", label, codeSystemKey);
                codes.add(getCode(system, label, display, c, null));
            }
        }

        return codes;
    }

    private List<DictionaryCode> getFhirCodes(String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirCodeSystem"));
        String display = String.format("%s (%s)", label, "FHIR");
        if (system != null && !system.isEmpty()) {
            String codeListString = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds,"FhirR4Code"));
            if (codeListString != null && !codeListString.isEmpty()) {
                List<String> codesList = Arrays.asList(codeListString.split(";"));
                for (String c : codesList) {
                    codes.add(getCode(system, label, display, c, null));
                }
            }

            if (system.startsWith(whoCodeSystemBase)) {
                CodeSystem codeSystem = null;
                for (CodeSystem cs : codeSystems) {
                    if (cs.getUrl().equals(system + "-codes")) {
                        codeSystem = cs;
                    }
                }

                if (codeSystem == null)
                {
                    String codeSystemName = system.substring(system.indexOf("CodeSystem/") + "CodeSystem/".length());
                    codeSystem = createCodeSystem(codeSystemName, whoCodeSystemBase, "Extended Codes CodeSystem", "Set of codes identified as being needed but not found in existing Code Systems");
                }

                for (DictionaryCode code : codes) {
                    CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                    concept.setCode(code.getCode());
                    concept.setDisplay(code.getLabel());

                    String parentLabel = null;
                    String parentName = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds,"InputOptionParent"));
                    if (parentName != null && !parentName.trim().isEmpty()) {
                        parentName = parentName.trim();
                        DictionaryElement currentElement = elementMap.get(parentName);
                        if (currentElement != null) {
                            parentLabel = currentElement.getDataElementLabel();
                        }
                    }

                    String definition = parentLabel != null ? String.format("%s - %s", parentLabel, code.getLabel()) : code.getLabel();
                    concept.setDefinition(definition);
                    codeSystem.addConcept(concept);
                }
            }
        }
        return codes;
    }

    private List<DictionaryCode> getOpenMRSCodes(String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = openMRSSystem;
        String parent = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "OpenMRSEntityParent"));
        String display = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "OpenMRSEntity"));
        String codeListString = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "OpenMRSEntityId"));
        if (codeListString != null && !codeListString.isEmpty()) {
            List<String> codesList = Arrays.asList(codeListString.split(";"));

            for (String c : codesList) {
                codes.add(getCode(system, label, display, c, parent));
            }
        }
        return codes;
    }

    private DictionaryCode getPrimaryCode(String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        DictionaryCode primaryCode = null;
        if (enableOpenMRS) {
            List<DictionaryCode> openMRSCodes = getOpenMRSCodes(label, row, colIds);
            if (openMRSCodes != null && !openMRSCodes.isEmpty()) {
                primaryCode = openMRSCodes.get(0);
                codes.addAll(openMRSCodes);
            }
        }

        List<DictionaryCode> fhirCodes = getFhirCodes(label, row, colIds);
        if (fhirCodes != null && !fhirCodes.isEmpty()) {
            if (primaryCode == null) {
                primaryCode = fhirCodes.get(0);
            }
            codes.addAll(fhirCodes);
        }

        for (String codeSystemKey : supportedCodeSystems.keySet()) {
            List<DictionaryCode> terminologyCodes = getTerminologyCodes(codeSystemKey, label, row, colIds);
            if (terminologyCodes != null && !terminologyCodes.isEmpty()) {
                if (primaryCode == null) {
                    primaryCode = terminologyCodes.get(0);
                }
                codes.addAll(terminologyCodes);
            }
        }

        // If more than one code is specified for the element, raise a warning.
        if (codes != null && codes.size() > 1) {
            System.out.println(String.format("Element %s - \"%s\" has multiple codes. %s was selected as the Primary code", row.getCell(0).toString().replace(".0", ""), label, primaryCode.getCode()));
        }

        return primaryCode;
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
        String resource = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirR4Resource")).trim();
        if (resource != null && !resource.isEmpty()) {
            fhirType.setResource(resource);
            fhirType.setMasterDataElementPath(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "MasterDataElementPath")));
            fhirType.setFhirElementType(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirR4ResourceType")));
            fhirType.setBaseProfile(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirR4BaseProfile")));
            fhirType.setVersion(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirR4VersionNumber")));
            fhirType.setCustomProfileId(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "CustomProfileId")));
            fhirType.setCustomValueSetName(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "CustomValueSetName")));
            fhirType.setBindingStrength(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "BindingStrength")));
            fhirType.setUnitOfMeasure(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "UnitOfMeasure")));
            fhirType.setExtensionNeeded(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "ExtensionNeeded")));
            fhirType.setAdditionalFHIRMappingDetails(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirR4AdditionalFHIRMappingDetails")));
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
        //String label = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Label"));
        String type = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Type"));
        if (type != null) {
            type = type.trim();
            if (type.equals("Coding")) {
                String choiceType = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "MultipleChoiceType"));
                if (choiceType != null) {
                    choiceType = choiceType.trim();
                    type = type + " - " + choiceType;
                }
            }
        }
        String name = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Name"));
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
        e.setMasterDataType(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "MasterDataType")));
        e.setInfoIcon(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "InfoIcon")));
        e.setDue(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Due")));
        e.setRelevance(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Relevance")));
        e.setDescription(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Description")));
        e.setDataElementLabel(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "DataElementLabel")) != null
            ? SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "DataElementLabel")).trim()
            : null);
        e.setDataElementName(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "DataElementName")));
        e.setNotes(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Notes")));
        e.setCalculation(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Calculation")));
        e.setConstraint(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Constraint")));
        e.setRequired(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Required")));
        e.setEditable(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Editable")));
        e.setScope(SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Scope")));
        e.setCode(getPrimaryCode(name, row, colIds));
        e.setFhirElementPath(getFhirElementPath(row, colIds));

        return e;
    }

    private void addInputOptionToParentElement(Row row, HashMap<String, Integer> colIds) {
        String parentName = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds,"InputOptionParent")).trim();
        if (parentName != null || !parentName.isEmpty())
        {
            DictionaryElement currentElement = elementMap.get(parentName);
            if (currentElement != null) {
                String choices = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Name"));
                if (choices != null && !choices.isEmpty()) {
                    choices = choices.trim();

                    List<DictionaryCode> codes;
                    if (enableOpenMRS) {
                        // Open MRS choices
                        codes = getOpenMRSCodes(choices, row, colIds);
                        if (codes != null && !codes.isEmpty()) {
                            currentElement.getChoices().addAll(codes);
                        }
                    }

                    // FHIR choices
                    String fhirCodeSystem = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "FhirCodeSystem"));
                    if (fhirCodeSystem != null && !fhirCodeSystem.isEmpty()) {
                        codes = getFhirCodes(choices, row, colIds);
                        if (codes != null && !codes.isEmpty()) {
                            currentElement.getChoices().addAll(codes);
                        }
                    }

                    // Other Terminology choices
                    for (String codeSystemKey : supportedCodeSystems.keySet()) {
                        codes = getTerminologyCodes(codeSystemKey, choices, row, colIds);
                        if (codes != null && !codes.isEmpty()) {
                            currentElement.getChoices().addAll(codes);
                        }
                    }
                }
            }
        }
    }

    private void processDataElementPage(Workbook workbook, String page, String scope) {
        Sheet sheet = workbook.getSheet(page);
        if (sheet == null) {
            System.out.println(String.format("Sheet %s not found in the Workbook, so no processing was done.", page));
        }

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
                        case "in new dd":
                            colIds.put("InNewDD", cell.getColumnIndex());
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
                        case "data element label": colIds.put("DataElementLabel", cell.getColumnIndex()); break;
                        case "data element name": colIds.put("DataElementName", cell.getColumnIndex()); break;
                        case "notes": colIds.put("Notes", cell.getColumnIndex()); break;
                        case "data type": colIds.put("Type", cell.getColumnIndex()); break;
                        case "multiple choice": colIds.put("MultipleChoiceType", cell.getColumnIndex()); break;
                        case "input options": colIds.put("Choices", cell.getColumnIndex()); break;
                        case "calculation": colIds.put("Calculation", cell.getColumnIndex()); break;
                        case "validation required": colIds.put("Constraint", cell.getColumnIndex()); break;
                        case "required": colIds.put("Required", cell.getColumnIndex()); break;
                        case "editable": colIds.put("Editable", cell.getColumnIndex()); break;
                        case "custom profile id": colIds.put("CustomProfileId", cell.getColumnIndex()); break;
                        case "binding or custom value set name or reference": colIds.put("CustomValueSetName", cell.getColumnIndex()); break;
                        case "binding strength": colIds.put("BindingStrength", cell.getColumnIndex()); break;
                        case "ucum": colIds.put("UnitOfMeasure", cell.getColumnIndex()); break;
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
                        case "icd-11": colIds.put("ICD-11", cell.getColumnIndex()); break;
                        case "ciel": colIds.put("CIEL", cell.getColumnIndex()); break;
                        case "openmrs entity parent": colIds.put("OpenMRSEntityParent", cell.getColumnIndex()); break;
                        case "openmrs entity": colIds.put("OpenMRSEntity", cell.getColumnIndex()); break;
                        case "openmrs entity id": colIds.put("OpenMRSEntityId", cell.getColumnIndex()); break;
                    }
                }
                continue;
            }

            String rowScope = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "Scope"));
            boolean scopeIsNull = scope == null;
            boolean scopeMatchesRowScope = rowScope != null && scope.toLowerCase().equals(rowScope.toLowerCase());

            String inNewDD = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "InNewDD"));
            boolean shouldInclude = inNewDD == null || (inNewDD.equals("ST") || inNewDD.equals("1"));

            if (shouldInclude && (scopeIsNull || scopeMatchesRowScope)) {
                String masterDataType = SpreadsheetHelper.getCellAsStringTrimmed(row, getColId(colIds, "MasterDataType"));
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

    private void processElementMap() {
        for (DictionaryElement element : elementMap.values()) {
            if (requiresProfile(element)) {
                StructureDefinition profile = ensureProfile(element);
            }
        }
    }

    private boolean requiresExtension(DictionaryElement element) {
        String baseProfile = element.getFhirElementPath().getBaseProfile();
        if (baseProfile != null && baseProfile.toLowerCase().trim().equals("extension needed")) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean requiresProfile(DictionaryElement element) {
        if (element == null
            || element.getMasterDataType() == null
            || element.getFhirElementPath() == null
//            || (element.getFhirElementPath().getBaseProfile() != null
//                && element.getFhirElementPath().getBaseProfile().toLowerCase().trim().equals("extension needed"))
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
                case "Boolean":
                    return "boolean";
                case "Coded":
                    return "code";
                case "Coded variables":
                case "CodableConcept":
                    return "CodeableConcept";
                case "DateTime":
                case "DD/MM/YYYY":
                    return "dateTime";
                case "Integer":
                case "###":
                    return "integer";
                case "Free text":
                case "free text":
                case "Text":
                    return "string";
                default:
                    return type;
            }
        } else {
            return type;
        }
    }

    // TODO: why is this different from "cleanse.." above?
    private String getExtensionFhirType(String type) {
        if (type != null && type.length() > 0) {
            switch (type) {
                case "Boolean":
                    return "boolean";
                case "Coded":
                    return "code";
                case "Integer":
                    return "integer";
                case "Note":
                case "Text":
                case "text":
                    return "string";
                case "Time":
                    return "time";
                case "Coding":
                case "Coding (Select all that apply":
                case "Coding - Select all that apply":
                case "Coding - Select One":
                case "Coding - Select one":
                    return "CodeableConcept";
                default:
                    return type;
            }
        } else {
            return type;
        }
    }

    private String getFhirTypeOfTargetElement(DictionaryFhirElementPath elementPath) {
        try {
//            String resourceType = elementPath.getResourceType().trim();
//            StructureDefinition sd = fhirModelStructureDefinitions.get(resourceType);
//
//            if (sd == null) {
//                System.out.println("StructureDefinition not found - " + resourceType);
//                return null;
//            }
            String type = null;
            if (isChoiceType(elementPath)) {
                type = cleanseFhirType(elementPath.getFhirElementType());
            }
            return type;

//            List<ElementDefinition> snapshotElements = sd.getSnapshot().getElement();
//            ElementDefinition typeElement = null;
//            for (ElementDefinition elementDef : snapshotElements) {
//                if (elementDef.toString().toLowerCase().equals(elementPath.getResourceTypeAndPath().toLowerCase())) {
//                    typeElement = elementDef;
//                }
//            }

//            if (typeElement != null) {
//                String elementType = typeElement.getType().get(0).getCode();
//                return elementType;
//            } else {
//                System.out.println("Could not find element: " + elementPath.getResourceTypeAndPath());
//                return null;
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchElementException("Unable to determine FHIR Type for: " + elementPath.getResourceTypeAndPath());
        }
    }

    private boolean isChoiceType(DictionaryFhirElementPath elementPath) {
        return elementPath.getResourcePath().indexOf("[x]") >= 0;
    }

    private StructureDefinition createExtensionStructureDefinition(DictionaryElement element, String extensionId) {
        DictionaryFhirElementPath elementPath = element.getFhirElementPath();

        StructureDefinition sd;
        sd = new StructureDefinition();
        sd.setId(extensionId);
        sd.setUrl(String.format("%s/StructureDefinition/%s", canonicalBase, sd.getId()));
        // TODO: version

        String extensionName = getExtensionName(element.getFhirElementPath().getResourcePath(), element.getDataElementName());
        sd.setName(extensionName);
        sd.setTitle(element.getLabel());
        sd.setStatus(Enumerations.PublicationStatus.DRAFT);
        sd.setExperimental(false);
        // TODO: date
        // TODO: publisher
        // TODO: contact
        sd.setDescription(element.getDescription());
        // TODO: What to do with Notes?
        sd.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        sd.setKind(StructureDefinition.StructureDefinitionKind.COMPLEXTYPE);
        sd.setAbstract(false);

        StructureDefinition.StructureDefinitionContextComponent context = new StructureDefinition.StructureDefinitionContextComponent();
        context.setType(StructureDefinition.ExtensionContextType.ELEMENT);
        context.setExpression(element.getFhirElementPath().getResourceType());
        List<StructureDefinition.StructureDefinitionContextComponent> contextList = new ArrayList();
        contextList.add(context);
        sd.setContext(contextList);

        sd.setType("Extension");
        sd.setBaseDefinition("http://hl7.org/fhir/StructureDefinition/Extension");
        sd.setDerivation(StructureDefinition.TypeDerivationRule.CONSTRAINT);
        sd.setDifferential(new StructureDefinition.StructureDefinitionDifferentialComponent());

        // TODO: status
        // TODO: category
        // TODO: subject
        // TODO: effective[x]

        // Add root element
        ElementDefinition rootElement = new ElementDefinition();
        rootElement.setId("Extension");
        rootElement.setPath("Extension");
        rootElement.setShort(element.getDataElementLabel());
        rootElement.setLabel(element.getDataElementName());
        rootElement.setComment(element.getNotes());
        rootElement.setDefinition(element.getDescription());
        rootElement.setMin(toBoolean(element.getRequired()) ? 1 : 0);
        rootElement.setMax(isMultipleChoiceElement(element) ? "*" : "1");

        if (element.getCode() != null) {
            rootElement.setFixed(element.getCode().toCodeableConcept());
        }

        sd.getDifferential().addElement(rootElement);

        // Add extension element
        ElementDefinition extensionElement = new ElementDefinition();
        extensionElement.setId("Extension.extension");
        extensionElement.setPath("Extension.extension");
        extensionElement.setMin(toBoolean(element.getRequired()) ? 1 : 0);
        extensionElement.setMax(isMultipleChoiceElement(element) ? "*" : "1");
        sd.getDifferential().addElement(extensionElement);

        // Add url element
        ElementDefinition urlElement = new ElementDefinition();
        urlElement.setId("Extension.url");
        urlElement.setPath("Extension.url");
        urlElement.setFixed(new UriType(sd.getUrl()));
        sd.getDifferential().addElement(urlElement);

        // Add value[x] element
        ElementDefinition valueElement = new ElementDefinition();
        valueElement.setId("Extension.value[x]");
        valueElement.setPath("Extension.value[x]");

        ElementDefinition.TypeRefComponent valueTypeRefComponent = new ElementDefinition.TypeRefComponent();
        String fhirType = cleanseFhirType(getExtensionFhirType(element.getType()));

        if (fhirType != null && !fhirType.isEmpty()) {
            valueTypeRefComponent.setCode(fhirType);
            List<ElementDefinition.TypeRefComponent> valueTypeList = new ArrayList<ElementDefinition.TypeRefComponent>();
            valueTypeList.add(valueTypeRefComponent);
            valueElement.setType(valueTypeList);
        }

        ensureAndBindElementTerminology(element, sd, valueElement);

        valueElement.setShort(element.getLabel());
        valueElement.setDefinition(element.getDescription());
        valueElement.setMin(1);
        sd.getDifferential().addElement(valueElement);

        return sd;
    }

    private String getExtensionName(String resourcePath, String dataElementName) {
        String extensionName = null;
        String[] resourcePathComponents = resourcePath.split("\\.");
        if (resourcePathComponents.length == 1) {
            extensionName = resourcePathComponents[0];
        } else if (resourcePathComponents.length > 1) {
            extensionName = resourcePathComponents[resourcePathComponents.length - 1];
        } else {
            extensionName = dataElementName;
        }
        return extensionName;
    }

    private StructureDefinition ensureExtension(DictionaryElement element) {
        StructureDefinition sd = null;

        String extensionName = getExtensionName(element.getFhirElementPath().getResourcePath(), element.getDataElementName());

        // Search for extension and use it if it exists already.
        String extensionId = toId(extensionName);
        if (extensionId != null && extensionId.length() > 0) {
            for (StructureDefinition existingExtension : extensions) {
                if (existingExtension.getId().equals(existingExtension)) {
                    sd = existingExtension;
                }
            }
        }
        else {
            throw new IllegalArgumentException("No name specified for the element");
        }

        // If the extension doesn't exist, create it with the root element.
        if (sd == null) {
            sd = createExtensionStructureDefinition(element, extensionId);
        }

        if (!extensions.contains(sd)) {
            extensions.add(sd);
        }

        return sd;
    }

    @NotNull
    private StructureDefinition createProfileStructureDefinition(DictionaryElement element, String customProfileId) {
        DictionaryFhirElementPath elementPath = element.getFhirElementPath();
        String customProfileIdRaw = elementPath.getCustomProfileId();
        Boolean hasCustomProfileIdRaw = customProfileIdRaw != null && !customProfileIdRaw.isEmpty() && !customProfileIdRaw.isBlank();
        String resourceType = elementPath.getResourceType().trim();

        StructureDefinition sd;
        sd = new StructureDefinition();
        sd.setId(customProfileId);
        sd.setUrl(String.format("%s/StructureDefinition/%s", canonicalBase, customProfileId));
        // TODO: version
        sd.setName(hasCustomProfileIdRaw ? customProfileIdRaw : element.getName());
        sd.setTitle(hasCustomProfileIdRaw ? customProfileIdRaw : element.getLabel());

        sd.setStatus(Enumerations.PublicationStatus.DRAFT);
        sd.setExperimental(false);
        // TODO: date
        // TODO: publisher
        // TODO: contact
        sd.setDescription(element.getDescription());
        // TODO: What to do with Notes?
        sd.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        sd.setKind(StructureDefinition.StructureDefinitionKind.RESOURCE);
        sd.setAbstract(false);
        // TODO: Support resources other than Observation
        sd.setType(resourceType);

        String baseResource = "http://hl7.org/fhir/StructureDefinition/" + resourceType;
        String baseProfileValue = elementPath.getBaseProfile();
        if (baseProfileValue == null || baseProfileValue.isEmpty() || baseProfileValue.toLowerCase().equals("extension needed") || baseProfileValue.toLowerCase().equals("fhir")) {
            sd.setBaseDefinition(baseResource);
        }
        else {
            sd.setBaseDefinition(baseProfileValue);
        }

        sd.setDerivation(StructureDefinition.TypeDerivationRule.CONSTRAINT);
        sd.setDifferential(new StructureDefinition.StructureDefinitionDifferentialComponent());

        // Add root element
        ElementDefinition ed = new ElementDefinition();
        ed.setId(resourceType);
        ed.setShort(element.getDataElementLabel());
        ed.setLabel(element.getDataElementName());
        ed.setComment(element.getNotes());
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
        String customProfileIdRaw = element.getFhirElementPath().getCustomProfileId();
        Boolean hasCustomProfileIdRaw = customProfileIdRaw != null && !customProfileIdRaw.isBlank() && !customProfileIdRaw.isEmpty();
        String customProfileId = toId(hasCustomProfileIdRaw ? customProfileIdRaw : element.getName());
        for (StructureDefinition existingSD : profiles) {
            if (existingSD.getId().equals(customProfileId)) {
                sd = existingSD;
            }
        }

        // If the profile doesn't exist, create it with the root element.
        if (sd == null) {
            sd = createProfileStructureDefinition(element, customProfileId);
        }

        if (requiresExtension(element)) {
            StructureDefinition extension = ensureExtension(element);
            DictionaryProfileElementExtension profileElementExtensionEntry = new DictionaryProfileElementExtension();
            profileElementExtensionEntry.setProfileId(customProfileId);
            profileElementExtensionEntry.setResourcePath(element.getFhirElementPath().getResourceTypeAndPath());
            profileElementExtensionEntry.setElement(element);
            profileElementExtensionEntry.setExtension(extension);
            profileExtensions.add(profileElementExtensionEntry);
        }
        else {
            // Ensure that the element is added to the StructureDefinition
            ensureElement(element, sd);
        }

        if (!profiles.contains(sd)) {
            profiles.add(sd);
        }

        return sd;
    }

    private void ensureElement(DictionaryElement element, StructureDefinition sd) {
        String codePath = null;
        String choicesPath;

        DictionaryFhirElementPath elementPath = element.getFhirElementPath();
        String resourceType = elementPath.getResourceType().trim();

        switch (resourceType) {
            case "AllergyIntolerance":
            case "Observation":
                codePath = "code";
                choicesPath = elementPath.getResourcePath();
                break;
            case "Appointment":
            case "CarePlan":
            case "Communication":
            case "Condition":
                choicesPath = elementPath.getResourcePath();
                break;
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
            case "Organization":
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

        // For Observations, it is a valid scenario for the Data Dictionary (DD) to not have a Data Element entry for the primary code path element - Observation.code.
        // In this case, the tooling should ensure that this element is created. The fixed code for this element should be the code specified by the Data
        // Element record mapped to Observation.value[x]. For all other resource types it is invalid to not have a primary code path element entry in the DD
        if (codePath != null && !codePath.isEmpty() && element.getCode() != null) {
            String elementId = String.format("%s.%s", resourceType, codePath);
            String primaryCodePath = String.format("%s.%s", resourceType, codePath);

            ElementDefinition existingPrimaryCodePathElement = null;
            for (ElementDefinition elementDef : sd.getDifferential().getElement()) {
                if (elementDef.getId().equals(elementId)) {
                    existingPrimaryCodePathElement = elementDef;
                    break;
                }
            }

            Boolean isPrimaryCodePath = element.getFhirElementPath().getResourceTypeAndPath().equals(primaryCodePath);
            Boolean isPreferredCodePath = isPrimaryCodePath || element.getFhirElementPath().getResourceTypeAndPath().equals("Observation.value[x]");

            if (existingPrimaryCodePathElement == null) {
                ElementDefinition ed = new ElementDefinition();
                ed.setId(elementId);
                ed.setPath(elementId);
                ed.setMin(1);
                ed.setMax("1");
                ed.setMustSupport(true);
                if (isPreferredCodePath) {
                    ed.setFixed(element.getCode().toCodeableConcept());
                }

                sd.getDifferential().addElement(ed);
            }
            else {
                Type existingCode = existingPrimaryCodePathElement.getFixed();
                // The code in the Primary Code Path Data Element entry should always have priority over the preferred (value[x])
                if ((existingCode == null || isPrimaryCodePath) && (isPreferredCodePath)) {
                    existingPrimaryCodePathElement.setFixed(element.getCode().toCodeableConcept());
                }
            }
        }

        Boolean isSlice = element.getMasterDataType().toLowerCase().equals("slice");
        String masterDataElementPath = elementPath.getMasterDataElementPath();
        Boolean isElementOfSlice = !isSlice &&  masterDataElementPath!= null && !masterDataElementPath.isEmpty() && masterDataElementPath.indexOf(".") > 0;

        String elementId;
        String slicePath;
        String sliceName = null;
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
        }
        else {
            if (isChoiceType(elementPath)) {
                String elementFhirType = getFhirTypeOfTargetElement(elementPath);
                elementFhirType = elementFhirType.substring(0, 1).toUpperCase() + elementFhirType.substring(1);
                elementId = elementPath.getResourceTypeAndPath().replace("[x]", elementFhirType);
            }
            else {
                elementId = String.format("%s.%s", resourceType, choicesPath);
            }
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
                String elementFhirType = getFhirTypeOfTargetElement(elementPath);

                ElementDefinition ed = new ElementDefinition();
                ed.setId(elementId);
                ed.setPath(elementId);
                ed.setMin(toBoolean(element.getRequired()) ? 1 : 0);
                ed.setMax(isMultipleChoiceElement(element) ? "*" : "1");
                ed.setMustSupport(true);

                String unitOfMeasure = element.getFhirElementPath().getUnitOfMeasure();
                Boolean hasUnitOfMeasure = unitOfMeasure != null && !unitOfMeasure.isBlank() && !unitOfMeasure.isEmpty();
                if (isChoiceType(elementPath) && hasUnitOfMeasure) {
                    ElementDefinition unitElement = new ElementDefinition();
                    unitElement.setId(elementId + ".unit");
                    unitElement.setPath(elementId + ".unit");
                    unitElement.setMin(1);
                    unitElement.setMax("1");
                    unitElement.setMustSupport(true);

                    //TODO: This should be a code, not fixed string
                    ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
                    if (elementFhirType != null && elementFhirType.length() > 0) {
                        tr.setCode("string");
                        unitElement.addType(tr);
                    }
                    unitElement.setFixed(new StringType(unitOfMeasure));

                    sd.getDifferential().addElement(unitElement);
                }

                ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
                if (elementFhirType != null && elementFhirType.length() > 0) {
                    tr.setCode(elementFhirType);
                    ed.addType(tr);
                }

                ensureAndBindElementTerminology(element, sd, ed);
                sd.getDifferential().addElement(ed);
            }
        }
        else {
            // If this is a choice type, append the current element's type to the type list.
            if (isChoiceType(elementPath)) {
                List<ElementDefinition.TypeRefComponent> existingTypes = existingElement.getType();

                ElementDefinition.TypeRefComponent elementType = null;
                String elementFhirType = getFhirTypeOfTargetElement(elementPath);
                if (elementFhirType != null && elementFhirType.length() > 0) {
                    for (ElementDefinition.TypeRefComponent type : existingTypes) {
                        if (type.getCode().equals(elementFhirType)) {
                            elementType = type;
                            break;
                        }
                    }
                }

                if (elementType == null) {
                    elementType = new ElementDefinition.TypeRefComponent();
                    elementType.setCode(elementFhirType);
                    existingElement.addType(elementType);
                }

                ensureAndBindElementTerminology(element, sd, existingElement);
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
        String customValueSetName = element.getFhirElementPath().getCustomValueSetName();
        Boolean hasCustomValueSetName = customValueSetName != null && !customValueSetName.isBlank() && !customValueSetName.isEmpty();

        //TODO: hasCustomValueSetName might be sufficient here?
        if (element.getChoices().size() > 0 || hasCustomValueSetName) {
            String valueSetUrl = null;
            if (hasCustomValueSetName && element.getChoices().size() == 0 && customValueSetName.startsWith("http")) {
                valueSetUrl = customValueSetName;
            }
            else {
                //TOOD: This needs some TLC, but had to decouple createCodeSystem from openMRS codes to facilitate the extended codes pattern.
                CodeSystem codeSystem = null;
                if (enableOpenMRS && element.getChoicesForSystem(openMRSSystem).size() > 0) {
                    codeSystem = createCodeSystem(element.getName(), canonicalBase, null, null);
                    // collect all the OpenMRS choices to add to the codeSystem
                    for (DictionaryCode code : element.getChoicesForSystem(openMRSSystem)) {
                        CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                        concept.setCode(code.getCode());
                        concept.setDisplay(code.getLabel());
                        codeSystem.addConcept(concept);
                    }
                }

                ValueSet valueSet = ensureValueSet(element);

                if (codeSystem != null && element.getChoicesForSystem(openMRSSystem).size() == element.getChoices().size()) {
                    codeSystem.setValueSet(valueSet.getUrl());
                }
                valueSetUrl = valueSet.getUrl();
            }
            // Bind the current element to the valueSet
            ElementDefinition.ElementDefinitionBindingComponent binding = new ElementDefinition.ElementDefinitionBindingComponent();
            binding.setStrength(element.getFhirElementPath().getBindingStrength());
            binding.setValueSet(valueSetUrl);
            ed.setBinding(binding);
        }
        // if the element is not a multiple choice element or an extension and has as code, bind it as the fixed value for the element.
        if (element.getChoices().size() == 0 && element.getCode() != null && !requiresExtension(element)) {
            DictionaryCode code = element.getCode();
            List<Coding> codes = new ArrayList<>();
            Coding coding = new Coding();
            coding.setCode(code.getCode());
            coding.setSystem(code.getSystem());
            coding.setDisplay(code.getDisplay());

            codes.add(coding);

            ed.setCode(codes);
        }
    }

    @NotNull
    private ValueSet ensureValueSet(DictionaryElement element) {
        // Ensure the ValueSet
        String valueSetName = element.getFhirElementPath().getCustomValueSetName();
        if (valueSetName == null || valueSetName.isEmpty() || valueSetName.isBlank()) {
            valueSetName = toId(element.getName());
        }

        String valueSetId = toId(valueSetName) + "-values";
        ValueSet valueSet = null;
        Boolean valueSetExisted = false;
        for (ValueSet vs : valueSets) {
            if (vs.getId().equals(valueSetId)) {
                valueSet = vs;
                valueSetExisted = true;
            }
        }

        if (valueSet == null) {
            valueSet = new ValueSet();
            valueSet.setId(valueSetId);
            valueSet.setUrl(String.format("%s/ValueSet/%s", canonicalBase, valueSet.getId()));
            // TODO: version
            valueSet.setName(valueSetId);
            valueSet.setTitle(String.format("%s values", element.getLabel()));
            valueSet.setStatus(Enumerations.PublicationStatus.DRAFT);
            valueSet.setExperimental(false);
            // TODO: date
            // TODO: publisher
            // TODO: contact
            valueSet.setDescription(String.format("Codes representing possible values for the %s element", element.getLabel()));
            valueSet.setImmutable(true);
        }

        // Ensure Compose element
        ValueSet.ValueSetComposeComponent compose = valueSet.getCompose();
        if (compose == null) {
            compose = new ValueSet.ValueSetComposeComponent();
            valueSet.setCompose(compose);
        }

        // Group by Supported Terminology System
        for (String codeSystemUrl : element.getCodeSystemUrls()) {
            List<DictionaryCode> systemCodes = element.getChoicesForSystem(codeSystemUrl);

            if (systemCodes.size() > 0) {
                List<ValueSet.ConceptSetComponent> conceptSets = compose.getInclude();
                ValueSet.ConceptSetComponent conceptSet = null;
                for (ValueSet.ConceptSetComponent cs : conceptSets) {
                    if (cs.getSystem().equals(codeSystemUrl)) {
                        conceptSet = cs;
                    }
                }

                if (conceptSet == null) {
                    conceptSet = new ValueSet.ConceptSetComponent();
                    compose.addInclude(conceptSet);
                    conceptSet.setSystem(codeSystemUrl);
                }

                for (DictionaryCode code : systemCodes) {
                    List<ValueSet.ConceptReferenceComponent> conceptReferences = conceptSet.getConcept();
                    ValueSet.ConceptReferenceComponent conceptReference = new ValueSet.ConceptReferenceComponent();
                    conceptReference.setCode(code.getCode());
                    conceptReference.setDisplay(code.getLabel());

                    if (!conceptReferences.contains(conceptReference)) {
                        conceptSet.addConcept(conceptReference);
                    }
                }
            }
        }

        // If the ValueSet did not already exist, add it to the valueSets collection
        if (!valueSetExisted) {
            valueSets.add(valueSet);
        }
        return valueSet;
    }

    @NotNull
    private CodeSystem createCodeSystem(String name, String canonicalBase, String title, String description) {
        CodeSystem codeSystem = new CodeSystem();

        codeSystem.setId(toId(name) + "-codes");
        codeSystem.setUrl(String.format("%s/CodeSystem/%s", canonicalBase, codeSystem.getId()));
        // TODO: version
        codeSystem.setName(name + "_codes");
        codeSystem.setTitle(String.format("%s codes", title != null ? title : name));
        codeSystem.setStatus(Enumerations.PublicationStatus.DRAFT);
        codeSystem.setExperimental(false);
        // TODO: date
        // TODO: publisher
        // TODO: contact
        codeSystem.setDescription(description != null ? description : String.format("Codes representing possible values for the %s element", name));
        codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        codeSystem.setCaseSensitive(true);

        codeSystems.add(codeSystem);

        return codeSystem;
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

    public void writeExtensions(String scopePath) {
        if (extensions != null && extensions.size() > 0) {
            String extensionsPath = getExtensionsPath(scopePath);
            ensureExtensionsPath(scopePath);

            for (StructureDefinition sd : extensions) {
                writeResource(extensionsPath, sd);

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
