package org.opencds.cqf.tooling.acceleratorkit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;

import ca.uhn.fhir.context.FhirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Bryn on 8/18/2019.
 */
public class Processor extends Operation {

    private static final Logger logger = LoggerFactory.getLogger(Processor.class);
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)
    private String scopes; // -scopes (-s)

    // Data Elements
    private String dataElementPages; // -dataelementpages (-dep) comma-separated list of the names of pages in the
                                     // workbook to be processed

    // Test Cases
    private String testCaseInput; // -testcases (-tc) path to a spreadsheet containing test case data

    // TODO: These need to be per scope
    private String dataElementIdentifierSystem = "http://fhir.org/guides/who/anc-cds/Identifier/anc-data-elements";
    private String activityCodeSystem = "http://fhir.org/guides/who/anc-cds/CodeSystem/anc-activity-codes";
    private String projectCodeSystemBase;

    private int questionnaireItemLinkIdCounter = 1;

    // Canonical Base
    private String canonicalBase = null;
    public void setCanonicalBase(String value) {
        canonicalBase = value;
        projectCodeSystemBase = canonicalBase;// + "/CodeSystem/anc-custom-codes"; //http://fhir.org/guides/who/anc-cds/CodeSystem/anc-custom-codes
    }
    private Map<String, String> scopeCanonicalBaseMap = new LinkedHashMap<String, String>();

    private String openMRSSystem = "http://openmrs.org/concepts";

    // NOTE: for now, disable open MRS system/codes
    private boolean enableOpenMRS = false;
    private Map<String, String> supportedCodeSystems = new LinkedHashMap<String, String>();

    // private Map<String, StructureDefinition> fhirModelStructureDefinitions = new LinkedHashMap<String, StructureDefinition>();
    private Map<String, DictionaryElement> elementMap = new LinkedHashMap<String, DictionaryElement>();
    private Map<String, DictionaryElement> elementsById = new HashMap<>();
    private Map<String, Integer> elementIds = new LinkedHashMap<String, Integer>();
    private Map<String, Coding> activityMap = new LinkedHashMap<String, Coding>();
    private List<DictionaryProfileElementExtension> profileExtensions = new ArrayList<>();
    private List<StructureDefinition> extensions = new ArrayList<StructureDefinition>();
    private List<StructureDefinition> profiles = new ArrayList<StructureDefinition>();
    private Map<String, Resource> examples = new HashMap<String, Resource>();
    private Map<String, List<Resource>> testCases = new LinkedHashMap<String, List<Resource>>();
    private Map<String, StructureDefinition> profilesByElementId = new HashMap<String, StructureDefinition>();
    private Map<String, List<DictionaryElement>> elementsByProfileId = new LinkedHashMap<String, List<DictionaryElement>>();
    private Map<String, List<StructureDefinition>> profilesByActivityId = new LinkedHashMap<String, List<StructureDefinition>>();
    private Map<String, List<StructureDefinition>> profilesByParentProfile = new LinkedHashMap<String, List<StructureDefinition>>();
    private List<CodeSystem> codeSystems = new ArrayList<CodeSystem>();
    private List<Questionnaire> questionnaires = new ArrayList<Questionnaire>();
    private List<ValueSet> valueSets = new ArrayList<ValueSet>();
    private Map<String, String> valueSetNameMap = new HashMap<String, String>();
    private Map<String, ConceptMap> conceptMaps = new LinkedHashMap<String, ConceptMap>();
    private Map<String, Coding> concepts = new LinkedHashMap<String, Coding>();
    private Map<String, String> conceptNameMap = new HashMap<String, String>();
    private List<RetrieveInfo> retrieves = new ArrayList<RetrieveInfo>();
    private List<String> igJsonFragments = new ArrayList<String>();
    private List<String> igResourceFragments = new ArrayList<String>();
    private CanonicalResourceAtlas atlas;

    private Row currentInputOptionParentRow;

    private class RetrieveInfo {
        public RetrieveInfo(StructureDefinition structureDefinition, String terminologyIdentifier, DictionaryFhirElementPath fhirElementPath) {
            this.structureDefinition = structureDefinition;
            this.terminologyIdentifier = terminologyIdentifier;
            this.fhirElementPath = fhirElementPath;
        }

        private StructureDefinition structureDefinition;
        // public StructureDefinition getStructureDefinition() {
        //     return structureDefinition;
        // }
        private String terminologyIdentifier;
        public String getTerminologyIdentifier() {
            return terminologyIdentifier;
        }

        private DictionaryFhirElementPath fhirElementPath;
        public DictionaryFhirElementPath getFhirElementPath() { return this.fhirElementPath; }
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/acceleratorkit/output"); // default
        for (String arg : args) {
            if (arg.equals("-ProcessAcceleratorKit"))
                continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "scopes":
                case "s":
                    scopes = value;
                    break; // -scopes (-s)
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "pathtospreadsheet":
                case "pts":
                    pathToSpreadsheet = value;
                    break; // -pathtospreadsheet (-pts)
                case "encoding":
                case "e":
                    encoding = value.toLowerCase();
                    break; // -encoding (-e)
                case "dataelementpages":
                case "dep":
                    dataElementPages = value;
                    break; // -dataelementpages (-dep)
                case "testcases":
                case "tc":
                    testCaseInput = value;
                    break; // -testcases (-tc)
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        registerScopes();
        registerCodeSystems();

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);

        // loadFHIRModel();

        if (scopes == null) {
            processScope(workbook, null);
        } else {
            for (String scope : scopes.split(",")) {
                processScope(workbook, scope);
            }
        }
    }

    private void registerCodeSystems() {
        if (enableOpenMRS) {
            supportedCodeSystems.put("OpenMRS", openMRSSystem);
        }
        supportedCodeSystems.put("ICD-10", "http://hl7.org/fhir/sid/icd-10");
        supportedCodeSystems.put("SNOMED-CT", "http://snomed.info/sct");
        supportedCodeSystems.put("LOINC", "http://loinc.org");
        supportedCodeSystems.put("RxNorm", "http://www.nlm.nih.gov/research/umls/rxnorm");
        supportedCodeSystems.put("CPT", "http://www.ama-assn.org/go/cpt");
        supportedCodeSystems.put("HCPCS", "https://www.cms.gov/Medicare/Coding/HCPCSReleaseCodeSets");

        // TODO: Determine and add correct URLS for these Systems
        supportedCodeSystems.put("CIEL", "http://hl7.org/fhir/sid/ciel");
        supportedCodeSystems.put("ICD-11", "http://hl7.org/fhir/sid/icd-11");
        supportedCodeSystems.put("ICHI", "https://mitel.dimi.uniud.it/ichi/#http://id.who.int/ichi");
        supportedCodeSystems.put("ICF", "http://hl7.org/fhir/sid/icf-nl");
        supportedCodeSystems.put("NDC", "http://hl7.org/fhir/sid/ndc");
    }

    private void registerScopes() {
        scopeCanonicalBaseMap.put("core", "http://fhir.org/guides/who/core");
        scopeCanonicalBaseMap.put("anc", "http://fhir.org/guides/who/anc-cds");
        scopeCanonicalBaseMap.put("fp", "http://fhir.org/guides/who/fp-cds");
        scopeCanonicalBaseMap.put("sti", "http://fhir.org/guides/who/sti-cds");
        scopeCanonicalBaseMap.put("cr", "http://fhir.org/guides/cqframework/cr");
		scopeCanonicalBaseMap.put("hiv", "http://fhir.org/guides/nachc/hiv-cds");
    }

    // private void loadFHIRModel() {
    // //TODO: Expose as an arg
    // String inputPath =
    // Paths.get("/Users/Adam/Src/cqframework/FHIR-Spec").toString();
    // String resourcePaths = "4.0.0/StructureDefinition";
    //
    // ResourceLoader loader = new ResourceLoader();
    // fhirModelStructureDefinitions = loader.loadPaths(inputPath, resourcePaths);
    // }

    private void processScope(Workbook workbook, String scope) {
        // reset variables
        elementMap = new LinkedHashMap<>();
        profileExtensions = new ArrayList<>();
        extensions = new ArrayList<>();
        profiles = new ArrayList<>();
        codeSystems = new ArrayList<>();
        questionnaires = new ArrayList<>();
        valueSets = new ArrayList<>();
        igJsonFragments = new ArrayList<>();
        igResourceFragments = new ArrayList<>();

        // ensure scope folder exists
        // String scopePath = getScopePath(scope);
        // ensurePath(scopePath);

        String outputPath = getOutputPath();
        ensurePath(outputPath);

        if (scope != null && scope.length() > 0) {
            setCanonicalBase(scopeCanonicalBaseMap.get(scope.toLowerCase()));
        }

        // process workbook
        for (String page : dataElementPages.split(",")) {
            processDataElementPage(workbook, page.trim(), scope);
        }

        // process element map
        processElementMap();

        // attached the generated extensions to the profiles that reference them
        attachExtensions();

        // process questionnaires
        processQuestionnaires();

        // process example resources
        processExamples();

        // write all resources
        writeExtensions(outputPath);
        writeProfiles(outputPath);
        writeCodeSystems(outputPath);
        writeValueSets(outputPath);
        writeConceptMaps(outputPath);
        writeQuestionnaires(outputPath);
        writeExamples(outputPath);

        processTestCases();
        writeTestCases(outputPath);

        // write concepts CQL
        writeConcepts(scope, outputPath);

        // write DataElements CQL
        writeDataElements(scope, outputPath);

        //ig.json is deprecated and resources a located by convention. If our output isn't satisfying convention, we should
        //modify the tooling to match the convention.
        //writeIgJsonFragments(scopePath);
        //writeIgResourceFragments(scopePath);
    }

    private ElementDefinition getDifferentialElement(StructureDefinition sd, String elementId) {
        ElementDefinition element = null;
        for (ElementDefinition ed : sd.getDifferential().getElement()) {
            if (ed.getId().equals(elementId)) {
                element = ed;
                break;
            }
        }
        return element;
    }

    private void attachExtensions() {
        // Add extensions to the appropriate profiles
        for (DictionaryProfileElementExtension profileElementExtension : profileExtensions) {
            for (StructureDefinition profile : profiles) {
                if (profile.getId().equals(profileElementExtension.getProfileId())) {
                    StructureDefinition extensionDefinition = profileElementExtension.getExtension();

                    String extensionName = getExtensionName(profileElementExtension.getResourcePath(),
                            profile.getName());

                    ElementDefinition extensionBaseElement = getDifferentialElement(extensionDefinition, "Extension.extension");

                    String resourcePath = profileElementExtension.getResourcePath();
                    String pathToElementBeingExtended = resourcePath.substring(0,
                            resourcePath.indexOf(extensionName) - 1);
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
                    applyDataElementToElementDefinition(profileElementExtension.getElement(), profile, extensionElement);
                }
            }
        }
    }

    private void ensurePath(String path) {
        // Creating a File object
        java.io.File scopeDir = new java.io.File(path);
        // Creating the directory
        if (!scopeDir.exists()) {
            if (!scopeDir.mkdirs()) {
                // TODO: change this to an IOException
                throw new IllegalArgumentException("Could not create directory: " + path);
            }
        }
    }

    // private String getScopePath(String scope) {
    // if (scope == null) {
    // return getOutputPath();
    // }
    // else {
    // return getOutputPath() + "/" + scope;
    // }
    // }

    private void ensureExtensionsPath(String scopePath) {
        String extensionsPath = getExtensionsPath(scopePath);
        ensurePath(extensionsPath);
    }

    private String getExtensionsPath(String scopePath) {
        return scopePath + "/input/extensions";
    }

    private void ensureExamplesPath(String scopePath) {
        String examplesPath = getExamplesPath(scopePath);
        ensurePath(examplesPath);
    }

    private String getExamplesPath(String scopePath) {
        return scopePath + "/input/examples";
    }

    private void ensureTestsPath(String scopePath) {
        String testsPath = getTestsPath(scopePath);
        ensurePath(testsPath);
    }

    private String getTestsPath(String scopePath) {
        return scopePath + "/input/tests";
    }

    private void ensureTestPath(String scopePath, String testId) {
        String testPath = getTestPath(scopePath, testId);
        ensurePath(testPath);
    }

    private String getTestPath(String scopePath, String testId) {
        return scopePath + "/input/tests/" + testId;
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

    private void ensureQuestionnairePath(String scopePath) {
        String questionnairePath = getQuestionnairePath(scopePath);
        ensurePath(questionnairePath);
    }

    private String getQuestionnairePath(String scopePath) {
        return scopePath + "/input/resources/questionnaire";
    }

    private void ensureValueSetPath(String scopePath) {
        String valueSetPath = getValueSetPath(scopePath);
        ensurePath(valueSetPath);
    }

    private void ensureConceptMapPath(String scopePath) {
        String conceptMapPath = getConceptMapPath(scopePath);
        ensurePath(conceptMapPath);
    }

    private String getValueSetPath(String scopePath) {
        return scopePath + "/input/vocabulary/valueset";
    }

    private String getConceptMapPath(String scopePath) {
        return scopePath + "/input/vocabulary/conceptmap";
    }

    private String getCqlPath(String scopePath) {
        return scopePath + "/input/cql";
    }

    private void ensureCqlPath(String scopePath) {
        String cqlPath = getCqlPath(scopePath);
        ensurePath(cqlPath);
    }

    private Coding getActivityCoding(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            return null;
        }

        int i = activityId.indexOf(" ");
        if (i <= 1) {
            return null;
        }

        String activityCode = activityId.substring(0, i);
        String activityDisplay = activityId.substring(i + 1);

        if (activityCode.isEmpty() || activityDisplay.isEmpty()) {
            return null;
        }

        if (activityCode.endsWith(".")) {
            activityCode = activityCode.substring(0, activityCode.length() - 1);
        }

        Coding activity = activityMap.get(activityCode);

        if (activity == null) {
            activity = new Coding().setCode(activityCode).setSystem(activityCodeSystem).setDisplay(activityDisplay);
            activityMap.put(activityCode, activity);
        }

        return activity;
    }

    private String getNextElementId(String activityCode) {
        if (activityCode == null) {
            return null;
        }

        if (!elementIds.containsKey(activityCode)) {
            elementIds.put(activityCode, 1);
        }

        Integer nextId = elementIds.get(activityCode);
        elementIds.put(activityCode, nextId + 1);

        return activityCode + ".DE" + nextId.toString();
    }

    private String getActivityID(Row row, HashMap<String, Integer> colIds) {
        String activityID = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "ActivityID"));
        return activityID;
    }

    private String getDataElementID(Row row, HashMap<String, Integer> colIds) {
        String dataElementID = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "DataElementID"));
        return dataElementID;
    }

    private String getDataElementLabel(Row row, HashMap<String, Integer> colIds) {
        String dataElementLabel = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "DataElementLabel"));
        dataElementLabel = dataElementLabel
                .replace("?", "")
                .replace("–", "-");
        return dataElementLabel;
    }

    private String getLabel(Row row, HashMap<String, Integer> colIds) {
        String label = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Label"));
        label = label
                .replace("?", "")
                .replace("–", "-");
        return label;
    }

    private String getName(Row row, HashMap<String, Integer> colIds) {
        String name = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Name"));
        name = name
                .replace("?", "")
                .replace("–", "-");
        return name;
    }

    private String getMasterDataType(Row row, HashMap<String, Integer> colIds) {
        String masterDataType = null;
        String activityID = getActivityID(row, colIds);
        if (activityID != null && !activityID.isEmpty()) {
            String multipleChoiceType = getMultipleChoiceType(row, colIds);
            masterDataType = multipleChoiceType != null && multipleChoiceType.equalsIgnoreCase("Input Option") ? "Input Option" : "Data Element";
        }

        return masterDataType;
    }

    private String getMultipleChoiceType(Row row, HashMap<String, Integer> colIds) {
        String multipleChoiceType = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MultipleChoiceType"));
        return multipleChoiceType;
    }

    private String cleanseCodeComments(String rawComment) {
        String result = null;
        if (rawComment != null) {
            result = rawComment
                    .replace("Code title: ", "")
                    .replace("Code LongName: ", "");
        }

        return result;
    }

    private String getCodeComments(Row row, HashMap<String, Integer> colIds, String colName) {
        String comments = SpreadsheetHelper.getCellAsString(row, getColId(colIds, colName));
        comments = cleanseCodeComments(comments);
        return comments;
    }

    private String getCodeSystemCommentColName(String codeSystem) {
        switch (codeSystem) {
            case "ICD-10": return "ICD-10Comments";
            case "ICD-11": return "ICD-11Comments";
            case "ICHI": return "ICHIComments";
            case "ICF": return "ICFComments";
            case "SNOMED-CT": return "SNOMEDComments";
            case "LOINC": return "LOINCComments";
            case "RXNorm": return "RXNormComments";
            case "CPT": return "CPTComments";
            case "HCPCS": return "HCPCSComments";
            case "NDC": return "NDCComments";
        }
        throw new IllegalArgumentException(String.format("Unknown code system key %s", codeSystem));
    }

    private List<DictionaryCode> cleanseCodes(List<DictionaryCode> codes) {
        // Remove "Not classifiable in" instances
        codes.removeIf(c -> c.getCode().startsWith("Not classifiable in"));
        return codes;
    }

    private List<DictionaryCode> getTerminologyCodes(String codeSystemKey, String id, String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = supportedCodeSystems.get(codeSystemKey);
        String codeListString = SpreadsheetHelper.getCellAsString(row, getColId(colIds, codeSystemKey));
        if (codeListString != null && !codeListString.isEmpty()) {
            List<String> codesList = Arrays.asList(codeListString.split(";"));
            String display = null;
            for (String c : codesList) {
                display = getCodeComments(row, colIds, getCodeSystemCommentColName(codeSystemKey));
                int bestFitIndex = display != null ? display.toLowerCase().indexOf("?best fit") : -1;
                if (bestFitIndex < 0) {
                    bestFitIndex = display != null ? display.toLowerCase().indexOf("??note: best fit") : -1;
                }
                String equivalence = "equivalent";
                if (bestFitIndex > 0) {
                    display = display.substring(0, bestFitIndex);
                    equivalence = "relatedto";
                }
                codes.add(getCode(system, id, label, display, c, null, equivalence));
            }
        }

        return cleanseCodes(codes);
    }

    private List<DictionaryCode> getFhirCodes(String id, String label, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirCodeSystem"));
        // If this is an input option with a custom code, add codes for the input options
        if (system == null || system.isEmpty()) {
            system = SpreadsheetHelper.getCellAsString(currentInputOptionParentRow, getColId(colIds, "FhirCodeSystem"));
        }
        String display = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4CodeDisplay"));
        // If there is no display, use the data element label
        if (display == null || display.isEmpty()) {
            display = label;
        }
        String parentId = null;
        String parentLabel = null;
        String parentName = getDataElementLabel(currentInputOptionParentRow, colIds).trim();
        if (parentName != null && !parentName.trim().isEmpty()) {
            parentName = parentName.trim();
            DictionaryElement currentElement = elementMap.get(parentName);
            if (currentElement != null) {
                parentId = currentElement.getId();
                parentLabel = currentElement.getDataElementLabel();
            }
        }
        if (system != null && !system.isEmpty()) {
            String codeListString = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4Code"));
            // If there is no code, use the data element label, prefixed with the parentLabel, if there is one
            if (codeListString == null || codeListString.isEmpty()) {
                codeListString = id;
                //codeListString = parentId != null ? (parentId + '-' + id) : id;
            }
            if (codeListString != null && !codeListString.isEmpty()) {
                List<String> codesList = Arrays.asList(codeListString.split(";"));
                for (String c : codesList) {
                    codes.add(getCode(system, id, label, display, c, null, null));
                }
            }

            if (system.startsWith(projectCodeSystemBase)) {
                CodeSystem codeSystem = null;
                for (CodeSystem cs : codeSystems) {
                    if (cs.getUrl().equals(system)) {
                        codeSystem = cs;
                    }
                }

                if (codeSystem == null) {
                    String codeSystemName = system.substring(system.indexOf("CodeSystem/") + "CodeSystem/".length());
                    codeSystem = createCodeSystem(codeSystemName, projectCodeSystemBase, "Extended Codes CodeSystem",
                            "Set of codes representing all concepts used in the implementation guide");
                }

                for (DictionaryCode code : codes) {
                    CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
                    concept.setCode(code.getCode());
                    concept.setDisplay(code.getLabel());

                    String definition = parentLabel != null ? String.format("%s - %s", parentLabel, code.getLabel())
                            : code.getLabel();
                    concept.setDefinition(definition);
                    codeSystem.addConcept(concept);
                }
            }
        }
        return cleanseCodes(codes);
    }

    private List<DictionaryCode> getOpenMRSCodes(String elementId, String elementLabel, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes = new ArrayList<>();
        String system = openMRSSystem;
        String parent = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityParent"));
        String display = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntity"));
        String codeListString = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "OpenMRSEntityId"));
        if (codeListString != null && !codeListString.isEmpty()) {
            List<String> codesList = Arrays.asList(codeListString.split(";"));

            for (String c : codesList) {
                codes.add(getCode(system, elementId, elementLabel, display, c, parent, "equivalent"));
            }
        }
        return cleanseCodes(codes);
    }

    private List<DictionaryCode> getPrimaryCodes(String elementId, String elementLabel, Row row, HashMap<String, Integer> colIds) {
        List<DictionaryCode> codes;
        codes = getDataElementCodes(row, colIds, elementId, elementLabel);
        return codes;
    }

    private DictionaryCode getCode(String system, String id, String label, String display, String codeValue, String parent, String equivalence) {
        DictionaryCode code = new DictionaryCode();
        code.setId(id);
        code.setLabel(label);
        code.setSystem(system);
        code.setDisplay(display);
        code.setCode(codeValue);
        code.setParent(parent);
        code.setEquivalence(equivalence);
        return code;
    }

    private DictionaryFhirElementPath getFhirElementPath(Row row, HashMap<String, Integer> colIds) {
        DictionaryFhirElementPath fhirType = null;
        String resource = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4Resource"));

        if (resource != null && !resource.isEmpty()) {
            resource = resource.trim();
            fhirType = new DictionaryFhirElementPath();
            fhirType.setResource(resource);
            fhirType.setFhirElementType(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4ResourceType")));
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
        String type = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Type"));
        if (type != null) {
            type = type.trim();
            if (type.equals("Coding")) {
                String choiceType = getMultipleChoiceType(row, colIds);
                if (choiceType != null) {
                    choiceType = choiceType.trim();
                    type = type + " - " + choiceType;
                }
            }
        }
        String name = getName(row, colIds);
        if (name.isEmpty()) {
            return null;
        }
        name = name.trim();
        String label = name;

        // TODO: should we throw if a duplicate is found within the same scope?
        // TODO: (core, anc, sti, fp, etc)
        if (elementMap.containsKey(name)) {
            // throw new IllegalArgumentException("Duplicate Name encountered: " + name);
            return null;
        }

        String activity = getActivityID(row, colIds);
        Coding activityCoding = getActivityCoding(activity);
        //String id = getNextElementId(activityCoding.getCode());
        String id = getDataElementID(row, colIds);

        DictionaryElement e = new DictionaryElement(id, name);

        // Populate based on the row
        e.setPage(page);
        e.setGroup(group);
        e.setActivity(activity);
        e.setLabel(label);
        e.setType(type);
        e.setMasterDataType(getMasterDataType(row, colIds));
        e.setInfoIcon(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "InfoIcon")));
        e.setDue(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Due")));
        e.setRelevance(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Relevance")));
        e.setDescription(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Description")));
        e.setDataElementLabel(getDataElementLabel(row, colIds) != null ? getDataElementLabel(row, colIds).trim() : null);
        e.setDataElementName(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "DataElementName")));
        e.setNotes(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Notes")));
        e.setCalculation(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Calculation")));
        e.setConstraint(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Constraint")));
        e.setRequired(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Required")));
        e.setEditable(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Editable")));
        e.setScope(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Scope")));
        e.setContext(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Context")));
        e.setSelector(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Selector")));
        //TODO: Get all codes specified on the element, create a valueset and bind to it. Required
        e.setPrimaryCodes(getPrimaryCodes(id, name, row, colIds));

        DictionaryFhirElementPath fhirElementPath = getFhirElementPath(row, colIds);
        if (fhirElementPath != null) {
            e.setFhirElementPath(fhirElementPath);
        }
        e.setMasterDataElementPath(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "MasterDataElementPath")));
        e.setBaseProfile(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4BaseProfile")));
        e.setCustomProfileId(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "CustomProfileId")));
        e.setVersion(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4VersionNumber")));
        e.setCustomValueSetName(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "CustomValueSetName")));
        e.setBindingStrength(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "BindingStrength")));
        e.setUnitOfMeasure(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "UnitOfMeasure")));
        e.setExtensionNeeded(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "ExtensionNeeded")));

        e.setAdditionalFHIRMappingDetails(SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirR4AdditionalFHIRMappingDetails")));

        return e;
    }

    private void addInputOptionToParentElement(Row row, HashMap<String, Integer> colIds) {
        String parentId = getDataElementID(currentInputOptionParentRow, colIds).trim();
        String parentName = getDataElementLabel(currentInputOptionParentRow, colIds).trim();

        if ((parentId != null && !parentId.isEmpty()) || (parentName != null && !parentName.isEmpty()))
        {
            DictionaryElement parentElement = elementMap.get(parentName);
            if (parentElement != null) {
                // The choices FHIR Element Path is set by the first "Input Option" row in the group and will NOT be
                // overridden, if set, by subsequent input option rows.
                DictionaryFhirElementPath parentChoicesFhirElementPath = parentElement.getChoices().getFhirElementPath();
                if (parentChoicesFhirElementPath == null) {
                    DictionaryFhirElementPath parentElementFhirElementPath = parentElement.getFhirElementPath();
                    parentChoicesFhirElementPath = getFhirElementPath(row, colIds);

                    if (parentChoicesFhirElementPath == null
                            && parentElementFhirElementPath != null
                            && parentElementFhirElementPath.getResourceTypeAndPath().equals("Observation.value[x]")) {
                        parentChoicesFhirElementPath = parentElementFhirElementPath;
                    }

                    parentElement.getChoices().setFhirElementPath(parentChoicesFhirElementPath);
                }

                Map<String, List<DictionaryCode>> valueSetCodes = parentElement.getChoices().getValueSetCodes();
                if (valueSetCodes != null) {
                    String inputOptionValueSetName = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "CustomValueSetName"));
                    if (inputOptionValueSetName == null || inputOptionValueSetName.isEmpty()) {
                        inputOptionValueSetName = parentName;
                    }

                    if (!inputOptionValueSetName.endsWith("Choices")) {
                        inputOptionValueSetName = inputOptionValueSetName + " Choices";
                    }

                    String optionId = getDataElementID(row, colIds);
                    String optionLabel = getDataElementLabel(row, colIds);
                    List<DictionaryCode> inputOptionCodes = getDataElementCodes(row, colIds,
                            optionId != null && !optionId.isEmpty() ? optionId : parentId,
                            optionLabel != null && !optionLabel.isEmpty() ? optionLabel : parentName);

                    if (!valueSetNameMap.containsKey(inputOptionValueSetName)) {
                        valueSetNameMap.put(inputOptionValueSetName, optionId);
                    }

                    if (valueSetCodes.containsKey(inputOptionValueSetName)) {
                        List<DictionaryCode> entryCodes = valueSetCodes.get(inputOptionValueSetName);
                        for (DictionaryCode code: inputOptionCodes) {
                            if (entryCodes.stream().noneMatch(c -> c.getCode().equals(code.getCode())
                                                                && c.getSystem().equals(code.getSystem()))) {
                                entryCodes.add(code);
                            }

                        }
                    } else {
                        valueSetCodes.put(inputOptionValueSetName, inputOptionCodes);
                    }
                }
            }
        }
    }

    private List<DictionaryCode> getDataElementCodes(Row row, HashMap<String, Integer> colIds, String elementId, String elementLabel) {
        List<DictionaryCode> codes = new ArrayList<>();

        if (enableOpenMRS) {
            // Open MRS choices
            List<DictionaryCode> mrsCodes = getOpenMRSCodes(elementId, elementLabel, row, colIds);
            codes.addAll(mrsCodes);
        }

        // FHIR choices
        //String fhirCodeSystem = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "FhirCodeSystem"));
        //if (fhirCodeSystem != null && !fhirCodeSystem.isEmpty()) {
            List<DictionaryCode> fhirCodes = getFhirCodes(elementId, elementLabel, row, colIds);
            codes.addAll(fhirCodes);
        //}

        // Other Terminology choices
        for (String codeSystemKey : supportedCodeSystems.keySet()) {
            List<DictionaryCode> codeSystemCodes = getTerminologyCodes(codeSystemKey, elementId, elementLabel, row, colIds);
            if (codes != codeSystemCodes && !codeSystemCodes.isEmpty()) {
                for (DictionaryCode c : codes) {
                    c.getMappings().addAll(codeSystemCodes);
                }
            }
        }

        return codes;
    }

    private void processDataElementPage(Workbook workbook, String page, String scope) {
        Sheet sheet = workbook.getSheet(page);
        if (sheet == null) {
            logger.info(String.format("Sheet %s not found in the Workbook, so no processing was done.", page));
        }

        questionnaireItemLinkIdCounter = 1;
        Questionnaire questionnaire = createQuestionnaireForPage(sheet);

        Iterator<Row> it = sheet.rowIterator();
        HashMap<String, Integer> colIds = new LinkedHashMap<String, Integer>();
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
                    String header = SpreadsheetHelper.getCellAsString(cell)
                            .toLowerCase()
                            .trim()
                            .replace("–", "-");
                    switch (header) {
                        case "[anc] data element id":
                        case "data element id":
                            colIds.put("DataElementID", cell.getColumnIndex());
                            break;
                        case "[anc] activity id":
                        case "activity id":
                            colIds.put("ActivityID", cell.getColumnIndex());
                            break;
                        case "core, fp, sti":
                        case "scope":
                            colIds.put("Scope", cell.getColumnIndex());
                            break;
                        case "context":
                            colIds.put("Context", cell.getColumnIndex());
                            break;
                        case "selector":
                            colIds.put("Selector", cell.getColumnIndex());
                            break;
                        case "in new dd":
                            colIds.put("InNewDD", cell.getColumnIndex());
                            break;
//                        case "master data type":
//                            colIds.put("MasterDataType", cell.getColumnIndex());
//                            break;
//                        case "master data element label":
//                            colIds.put("Name", cell.getColumnIndex());
//                            colIds.put("Label", cell.getColumnIndex());
//                            break;
//                        case "data element parent for input options":
//                            colIds.put("InputOptionParent", cell.getColumnIndex());
//                            break;
                        // no group column in old or new spreadsheet? Ask Bryn?
                        // case "group": colIds.put("Group", cell.getColumnIndex()); break;
                        // case "data element name": colIds.put("Name", cell.getColumnIndex()); break;
                        case "due":
                            colIds.put("Due", cell.getColumnIndex());
                            break;
                        // no frequency column in new master spreadsheet?
                        // case "frequency": colIds.put("Due", cell.getColumnIndex()); break;
                        // relevance not used in FHIR?
                        // case "relevance": colIds.put("Relevance", cell.getColumnIndex()); break;
                        // info icon not used in FHIR?
                        //case "info icon": colIds.put("InfoIcon", cell.getColumnIndex()); break;
                        case "description and definition":
                        case "description": colIds.put("Description", cell.getColumnIndex()); break;
                        case "data element label":
                            colIds.put("DataElementLabel", cell.getColumnIndex());
                            colIds.put("Name", cell.getColumnIndex());
                            colIds.put("Label", cell.getColumnIndex());
                            break;
                        case "data element name": colIds.put("DataElementName", cell.getColumnIndex()); break;
                        case "notes": colIds.put("Notes", cell.getColumnIndex()); break;
                        case "data type": colIds.put("Type", cell.getColumnIndex()); break;
                        case "multiple choice":
                        case "multiple choice type":
                        case "multiple choice (if applicable)":
                        case "multiple choice type ?(if applicable)":
                            colIds.put("MultipleChoiceType", cell.getColumnIndex()); break;
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
                        case "hl7 fhir r4 code display": colIds.put("FhirR4CodeDisplay", cell.getColumnIndex()); break;
                        case "hl7 fhir r4 code definition": colIds.put("FhirR4CodeDefinition", cell.getColumnIndex()); break;
                        case "icd-10-who":
                        case "icd-10 code":
                        case "icd-10?code": colIds.put("ICD-10", cell.getColumnIndex()); break;
                        case "icd-10?comments / considerations": colIds.put("ICD-10Comments", cell.getColumnIndex()); break;
                        case "icf?code": colIds.put("ICF", cell.getColumnIndex()); break;
                        case "icf?comments / considerations": colIds.put("ICFComments", cell.getColumnIndex()); break;
                        case "ichi?code":
                        case "ichi (beta 3)?code": colIds.put("ICHI", cell.getColumnIndex()); break;
                        case "ichi?comments / considerations": colIds.put("ICHIComments", cell.getColumnIndex()); break;
                        case "snomed-ct":
                        case "snomed-ct code":
                        case "snomed ct":
                        case "snomed ct?code":
                        case "snomed ct international version?code": colIds.put("SNOMED-CT", cell.getColumnIndex()); break;
                        case "snomed ct international version?comments / considerations": colIds.put("SNOMEDComments", cell.getColumnIndex()); break;
                        case "loinc":
                        case "loinc code":
                        case "loinc version 2.68?code": colIds.put("LOINC", cell.getColumnIndex()); break;
                        case "loinc version 2.68?comments / considerations": colIds.put("LOINCComments", cell.getColumnIndex()); break;
                        case "rxnorm":
                        case "rxnorm code":
                        case "rxnorm?code": colIds.put("RxNorm", cell.getColumnIndex()); break;
                        case "rxnorm?comments / considerations": colIds.put("RXNormComments", cell.getColumnIndex()); break;
                        case "icd-11":
                        case "icd-11 code":
                        case "icd-11?code":colIds.put("ICD-11", cell.getColumnIndex()); break;
                        case "icd-11?comments / considerations": colIds.put("ICD-11Comments", cell.getColumnIndex()); break;
                        case "ciel": colIds.put("CIEL", cell.getColumnIndex()); break;
                        case "openmrs entity parent": colIds.put("OpenMRSEntityParent", cell.getColumnIndex()); break;
                        case "openmrs entity": colIds.put("OpenMRSEntity", cell.getColumnIndex()); break;
                        case "openmrs entity id": colIds.put("OpenMRSEntityId", cell.getColumnIndex()); break;
                        case "cpt":
                        case "cpt code":
                        case "cpt?code": colIds.put("CPT", cell.getColumnIndex()); break;
                        case "cpt?comments / considerations": colIds.put("CPTComments", cell.getColumnIndex()); break;
                        case "hcpcs":
                        case "hcpcs code":
                        case "hcpcs?code":
                        case "hcpcs level ii code":
                        case "hcpcs?level ii code": colIds.put("HCPCS", cell.getColumnIndex()); break;
                        case "hcpcs?comments / considerations": colIds.put("HCPCSComments", cell.getColumnIndex()); break;
                        case "ndc":
                        case "ndc code":
                        case "ndc?code": colIds.put("NDC", cell.getColumnIndex()); break;
                        case "ndc?comments / considerations": colIds.put("NDCComments", cell.getColumnIndex()); break;
                    }
                }
                continue;
            }

            String rowScope = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "Scope"));
            boolean scopeIsNull = scope == null;
            boolean scopeMatchesRowScope = rowScope != null && scope.toLowerCase().equals(rowScope.toLowerCase());

            String inNewDD = SpreadsheetHelper.getCellAsString(row, getColId(colIds, "InNewDD"));
            boolean shouldInclude = inNewDD == null || inNewDD.equals("ST") || inNewDD.equals("1");

            if (shouldInclude && (scopeIsNull || scopeMatchesRowScope)) {
                String masterDataType = getMasterDataType(row, colIds);
                if (masterDataType != null) {
                    switch (masterDataType) {
                        case "Data Element":
                        case "Slice":
                            currentInputOptionParentRow = row;
                            DictionaryElement e = createDataElement(page, currentGroup, row, colIds);
                            if (e != null) {
                                elementMap.put(e.getName(), e);
                                elementsById.put(e.getId(), e);
                                updateQuestionnaireForDataElement(e, questionnaire);
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
        questionnaires.add(questionnaire);
    }

    private Questionnaire createQuestionnaireForPage(Sheet sheet) {
        Questionnaire questionnaire = new Questionnaire();
        Coding activityCoding = getActivityCoding(sheet.getSheetName());
        questionnaire.setId(toUpperId(activityCoding.getCode()));

        questionnaire.getExtension().add(
                new Extension("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability", new CodeType("shareable")));
        questionnaire.getExtension().add(
                new Extension("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability", new CodeType("computable")));
        questionnaire.getExtension().add(
                new Extension("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability", new CodeType("publishable")));
        questionnaire.getExtension().add(
                new Extension("http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel", new CodeType("structured")));

        questionnaire.setUrl(String.format("%s/Questionnaire/%s", canonicalBase, questionnaire.getId()));
        questionnaire.setName(questionnaire.getId());
        questionnaire.setTitle(sheet.getSheetName());
        questionnaire.setStatus(Enumerations.PublicationStatus.ACTIVE);
        questionnaire.setExperimental(false);
        questionnaire.setDescription("TODO: description goes here");

        Coding useContextCoding = new Coding("http://terminology.hl7.org/CodeSystem/usage-context-type", "task", "Workflow Task");
        CodeableConcept useContextValue = new CodeableConcept(new Coding(activityCoding.getSystem(), activityCoding.getCode(), activityCoding.getDisplay()));
        UsageContext useContext = new UsageContext(useContextCoding, useContextValue);
        questionnaire.getUseContext().add(useContext);

        return questionnaire;
    }

    private Questionnaire.QuestionnaireItemType getQuestionnaireItemType(DictionaryElement dataElement) {
        Questionnaire.QuestionnaireItemType type = null;

        String typeString = null;
        if (dataElement.getFhirElementPath() != null) {
            typeString = dataElement.getFhirElementPath().getFhirElementType();
        }

        if (typeString == null || typeString.isEmpty()) {
            typeString = dataElement.getType();
        }

        if (typeString == null || typeString.isEmpty()) {
            logger.info(String.format("Could not determine type for Data Element: %s.", dataElement.getDataElementLabel()));
            return type;
        }

        if (typeString.toLowerCase().trim().startsWith("reference(")) {
            type = Questionnaire.QuestionnaireItemType.REFERENCE;
            return type;
        }

        switch (typeString.toLowerCase().trim()) {
            case "annotation":
            case "id":
            case "note":
            case "string":
            case "text":
                type = Questionnaire.QuestionnaireItemType.STRING;
                break;
            case "boolean":
                type = Questionnaire.QuestionnaireItemType.BOOLEAN;
                break;
            case "date":
                type = Questionnaire.QuestionnaireItemType.DATE;
                break;
            case "datetime":
                type = Questionnaire.QuestionnaireItemType.DATETIME;
                break;
            case "code":
            case "coded":
            case "codes":
            case "coding":
            case "codeableconcept":
            case "coding - n/a":
            case "coding (select all that apply":
            case "coding - select all that apply":
            case "coding - select one":
                type = Questionnaire.QuestionnaireItemType.CHOICE;
                break;
            case "int":
            case "integer":
                type = Questionnaire.QuestionnaireItemType.INTEGER;
                break;
            case "quantity":
                type = Questionnaire.QuestionnaireItemType.QUANTITY;
                break;
            default:
                logger.info(String.format("Questionnaire Item Type not mapped: %s.", typeString));
        }

        return type;
    }

    private void updateQuestionnaireForDataElement(DictionaryElement dataElement, Questionnaire questionnaire) {
        Questionnaire.QuestionnaireItemComponent questionnaireItem = new Questionnaire.QuestionnaireItemComponent();
        questionnaireItem.setLinkId(String.valueOf(questionnaireItemLinkIdCounter));
        String definition = dataElement.getId();
        questionnaireItem.setDefinition(definition);
        questionnaireItem.setText(dataElement.getDataElementLabel());
        Questionnaire.QuestionnaireItemType questionnaireItemType = getQuestionnaireItemType(dataElement);
        if (questionnaireItemType != null) {
            questionnaireItem.setType(questionnaireItemType);
        } else {
            logger.info(String.format("Unable to determine questionnaire item type for item '%s'.", dataElement.getDataElementLabel()));
        }

        questionnaire.getItem().add(questionnaireItem);

        questionnaireItemLinkIdCounter = questionnaireItemLinkIdCounter + 1;
    }

    private void processElementMap() {
        for (DictionaryElement element : elementMap.values()) {
            if (requiresProfile(element)) {
                 ensureProfile(element);
            }
        }
    }

    private boolean requiresExtension(DictionaryElement element) {
        String extensionNeededValue = element.getExtensionNeeded();
        boolean isExtensionNeeded = toBoolean(extensionNeededValue);
        return isExtensionNeeded;
    }

    private boolean requiresProfile(DictionaryElement element) {
        if (element == null
                || element.getMasterDataType() == null
                || element.getFhirElementPath() == null) {
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

    private String toUpperId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (name.endsWith(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }

        return name.trim()
                // remove these characters
                .replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace("\n", "")
                .replace(":", "")
                .replace(",", "")
                .replace("_", "")
                .replace("/", "")
                .replace(" ", "")
                .replace(".", "")
                .replace("-", "")
                .replace(">", "")
                .replace("<", "");
    }

    private String toName(String name) {
        String result = toUpperId(name);
        if (result.isEmpty()) {
            return result;
        }
        if (Character.isDigit(result.charAt(0))) {
            return "_" + result;
        }
        return result;
    }

    private String toId(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (name.endsWith(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }

        return name.toLowerCase().trim()
                // remove these characters
                .replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace("\n", "")
                // replace these with ndash
                .replace(":", "-")
                .replace(",", "-")
                .replace("_", "-")
                .replace("/", "-")
                .replace(" ", "-")
                .replace(".", "-")
                // remove multiple ndash
                .replace("----", "-").replace("---", "-").replace("--", "-").replace(">", "greater-than")
                .replace("<", "less-than");
    }

    private boolean toBoolean(String value) {
        return
            value != null && !value.isEmpty()
                && ("Yes".equalsIgnoreCase(value) || "R".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
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
            // String resourceType = elementPath.getResourceType().trim();
            // StructureDefinition sd = fhirModelStructureDefinitions.get(resourceType);
            //
            // if (sd == null) {
            // System.out.println("StructureDefinition not found - " + resourceType);
            // return null;
            // }
            String type = null;
            if (isChoiceType(elementPath)) {
                type = cleanseFhirType(elementPath.getFhirElementType());
            }
            return type;

            // List<ElementDefinition> snapshotElements = sd.getSnapshot().getElement();
            // ElementDefinition typeElement = null;
            // for (ElementDefinition elementDef : snapshotElements) {
            // if
            // (elementDef.toString().toLowerCase().equals(elementPath.getResourceTypeAndPath().toLowerCase()))
            // {
            // typeElement = elementDef;
            // }
            // }

            // if (typeElement != null) {
            // String elementType = typeElement.getType().get(0).getCode();
            // return elementType;
            // } else {
            // System.out.println("Could not find element: " +
            // elementPath.getResourceTypeAndPath());
            // return null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchElementException(
                    "Unable to determine FHIR Type for: " + elementPath.getResourceTypeAndPath());
        }
    }

    private boolean isChoiceType(DictionaryFhirElementPath elementPath) {
        return elementPath.getResourcePath().indexOf("[x]") >= 0;
    }

    private boolean isBindableType(DictionaryElement element) {
        String type = null;
        if (element.getType() != null) {
            type = element.getType().toLowerCase();
        }
        String mappedType = null;
        if (element.getFhirElementPath() != null) {
            mappedType = element.getFhirElementPath().getFhirElementType();
        }

        boolean isBindable =
            (type != null && type.contains("codings"))
            || (mappedType != null && (mappedType.equalsIgnoreCase("CodeableConcept") || mappedType.equalsIgnoreCase("Code")));

        return isBindable;
    }

    private StructureDefinition createExtensionStructureDefinition(DictionaryElement element, String extensionId) {
        // DictionaryFhirElementPath elementPath = element.getFhirElementPath();

        StructureDefinition sd;
        sd = new StructureDefinition();
        sd.setId(extensionId);
        sd.setUrl(String.format("%s/StructureDefinition/%s", canonicalBase, sd.getId()));
        // TODO: version

        String extensionName = getExtensionName(element.getFhirElementPath().getResourcePath(),
                element.getDataElementName());
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
        List<StructureDefinition.StructureDefinitionContextComponent> contextList = new ArrayList<>();
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

        ensureTerminologyAndBindToElement(element, sd, valueElement, null, null,false);

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

        String extensionName = getExtensionName(element.getFhirElementPath().getResourcePath(),
                element.getDataElementName());

        // Search for extension and use it if it exists already.
        String extensionId = toId(extensionName);
        if (extensionId != null && extensionId.length() > 0) {
            for (StructureDefinition existingExtension : extensions) {
                if (existingExtension.getId().equals(existingExtension)) {
                    sd = existingExtension;
                }
            }
        } else {
            throw new IllegalArgumentException("No name specified for the element");
        }

        // If the extension doesn't exist, create it with the root element.
        if (sd == null) {
            sd = createExtensionStructureDefinition(element, extensionId);
        }

        ensureChoicesDataElement(element, sd);

        if (!extensions.contains(sd)) {
            extensions.add(sd);
        }

        return sd;
    }

    @Nonnull
    private StructureDefinition createProfileStructureDefinition(DictionaryElement element, String customProfileId) {
        DictionaryFhirElementPath elementPath = element.getFhirElementPath();
        String customProfileIdRaw = element.getCustomProfileId();
        Boolean hasCustomProfileIdRaw = customProfileIdRaw != null && !customProfileIdRaw.isEmpty();
        String resourceType = elementPath.getResourceType().trim();
        Coding activityCoding = getActivityCoding(element.getActivity());

        StructureDefinition sd;
        sd = new StructureDefinition();
        sd.setId(customProfileId);
        sd.setUrl(String.format("%s/StructureDefinition/%s", canonicalBase, customProfileId));
        // TODO: version (I think this needs to come from the IG version, we don't need to set that here)
        sd.setName(toName(hasCustomProfileIdRaw ? customProfileIdRaw : element.getName()));
        sd.setTitle(hasCustomProfileIdRaw ? customProfileIdRaw : element.getLabel());

        //if (element.getId() != null) {
        //    sd.addIdentifier(new Identifier().setUse(Identifier.IdentifierUse.OFFICIAL)
        //            .setSystem(dataElementIdentifierSystem)
        //            .setValue(element.getId())
        //    );
        //}

        StructureDefinition.StructureDefinitionMappingComponent mapping = new StructureDefinition.StructureDefinitionMappingComponent();
        mapping.setIdentity(element.getScope());
        // TODO: Data Element mapping...
        mapping.setUri("https://www.who.int/publications/i/item/9789240020306");
        mapping.setName("Digital Adaptation Kit for Antenatal Care");
        sd.addMapping(mapping);

        sd.setStatus(Enumerations.PublicationStatus.DRAFT);
        sd.setExperimental(false);
        // TODO: date // Should be set by publication tooling
        // TODO: publisher // Should be set by publication tooling
        // TODO: contact // Should be set by publication tooling
        if (hasCustomProfileIdRaw) {
            sd.setDescription(customProfileIdRaw);
        }
        else {
            sd.setDescription(element.getDescription());
        }

        // TODO: What to do with Notes? // We should add any warnings generated during this process to the notes...
        sd.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        sd.setKind(StructureDefinition.StructureDefinitionKind.RESOURCE);
        sd.setAbstract(false);

        if (activityCoding != null) {
            sd.addUseContext(new UsageContext()
                    .setCode(new Coding()
                            .setCode("task")
                            .setSystem("http://terminology.hl7.org/CodeSystem/usage-context-type")
                            .setDisplay("Workflow Task")
                    ).setValue(new CodeableConcept().addCoding(activityCoding)));
        }

        sd.setType(resourceType);

        String baseResource = "http://hl7.org/fhir/StructureDefinition/" + resourceType;
        String baseProfileValue = element.getBaseProfile();
        if (baseProfileValue == null || baseProfileValue.isEmpty() || requiresExtension(element) || baseProfileValue.toLowerCase().equals("fhir")) {
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
        ed.setPath(resourceType);
        ed.setMustSupport(false);
        sd.getDifferential().addElement(ed);
        // If this data element is only a root data element, apply the element information here
        //if (element.getFhirElementPath() != null && element.getFhirElementPath().getResourceTypeAndPath().equals(resourceType)) {
        //    ed.setShort(element.getDataElementLabel());
        //    ed.setLabel(element.getDataElementName());
        //    ed.setComment(element.getNotes());
        //}

        // TODO: status
        // TODO: category
        // TODO: subject
        // TODO: effective[x]

        return sd;
    }

    private int getElementIndex(StructureDefinition sd, String path) {
        for (int i = 0; i < sd.getDifferential().getElement().size(); i++) {
            if (sd.getDifferential().getElement().get(i).getPath().equals(path)) {
                return i;
            }
        }

        return -1;
    }

    private void addAfter(StructureDefinition sd, ElementDefinition ed, String afterPath) {
        int targetIndex = getElementIndex(sd, afterPath);
        if (targetIndex >= 0) {
            sd.getDifferential().getElement().add(targetIndex + 1, ed);
        }
        else {
            sd.getDifferential().getElement().add(ed);
        }
    }

    private void addElementToStructureDefinition(StructureDefinition sd, ElementDefinition ed) {
        if (sd == null) {
            throw new IllegalArgumentException("sd is null");
        }
        if (ed == null) {
            throw new IllegalArgumentException("ed is null");
        }
        if (sd.getDifferential() == null) {
            throw new IllegalArgumentException("sd.differential is null");
        }
        // Add the element at the appropriate place based on the path
        // Ideally this code would be informed by the base StructureDefinition(s), but in the absence of that,
        // hard-coding some orders here based on current content patterns:
        switch (ed.getPath()) {
            case "MedicationRequest.dosageInstruction.timing": addAfter(sd, ed, "MedicationRequest.dosageInstruction"); break;
            case "MedicationRequest.dosageInstruction.timing.repeat": addAfter(sd, ed, "MedicationRequest.dosageInstruction.timing"); break;
            case "MedicationRequest.dosageInstruction.timing.repeat.periodUnit": addAfter(sd, ed, "MedicationRequest.dosageInstruction.timing.repeat"); break;
            case "MedicationRequest.statusReason": addAfter(sd, ed, "MedicationRequest"); break;
            case "Immunization.vaccineCode": addAfter(sd, ed, "Immunization"); break;
            case "Immunization.statusReason": addAfter(sd, ed, "Immunization"); break;
            case "ServiceRequest.code": addAfter(sd, ed, "ServiceRequest"); break;
            case "ServiceRequest.occurrence[x]": addAfter(sd, ed, "ServiceRequest.code"); break;
            case "ServiceRequest.requester": addAfter(sd, ed, "ServiceRequest.authoredOn"); break;
            case "ServiceRequest.locationReference": addAfter(sd, ed, "ServiceRequest.authoredOn"); break;
            case "Procedure.code": addAfter(sd, ed, "Procedure"); break;
            case "Procedure.statusReason": addAfter(sd, ed, "Procedure"); break;
            default: sd.getDifferential().addElement(ed); break;
        }
    }

    private void applyDataElementToElementDefinition(DictionaryElement element, StructureDefinition sd, ElementDefinition ed) {
        ed.setShort(element.getDataElementLabel());
        ed.setLabel(element.getDataElementName());
        ed.setComment(element.getNotes());
        ElementDefinition.ElementDefinitionMappingComponent mapping = new ElementDefinition.ElementDefinitionMappingComponent();
        mapping.setIdentity(element.getScope());
        mapping.setMap(element.getId());
        ed.addMapping(mapping);

        // Add the element to set of elements for this profile
        List<DictionaryElement> lde = elementsByProfileId.get(sd.getId());
        if (lde == null) {
            lde = new ArrayList<DictionaryElement>();
            elementsByProfileId.put(sd.getId(), lde);
            lde.add(element);
        }
        else {
            if (!lde.contains(element)) {
                lde.add(element);
            }
        }

        // Record the profile in which the data element is present:
        profilesByElementId.put(element.getId(), sd);
    }

    private void ensureProfile(DictionaryElement element) {
        StructureDefinition sd = null;
//        List<ElementDefinition> elementDefinitions = new ArrayList<>();

        // If custom profile is specified, search for if it exists already.
        String customProfileIdRaw = element.getCustomProfileId();
        String profileId = toId(customProfileIdRaw != null && !customProfileIdRaw.isEmpty() ? customProfileIdRaw : element.getId());
        for (StructureDefinition profile : profiles) {
            if (profile.getId().equals(profileId)) {
                sd = profile;
            }
        }

        // If the profile doesn't exist, create it with the root element.
        if (sd == null) {
            sd = createProfileStructureDefinition(element, profileId);
        }

        if (requiresExtension(element)) {
            StructureDefinition extension = ensureExtension(element);
            DictionaryProfileElementExtension profileElementExtensionEntry = new DictionaryProfileElementExtension();
            profileElementExtensionEntry.setProfileId(profileId);
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
    }

    private boolean isRequiredElement(DictionaryElement element) {
        if (element != null && element.getFhirElementPath() != null && element.getFhirElementPath().getResourceTypeAndPath() != null) {
            switch (element.getFhirElementPath().getResourceTypeAndPath()) {
                case "MedicationRequest.medication": return true;
                case "MedicationRequest.medication[x]": return true;
                case "Condition.code": return true;
                case "Procedure.code": return true;
                case "Immunization.statusReason": return true;
                case "ServiceRequest.code": return true;
                case "Immunization.vaccineCode": return true;
                case "Patient.contact.name": return true;
                case "Procedure.performed": return true;
                case "Procedure.performed[x]": return true;
                case "MedicationRequest.dosageInstruction.doseAndRate": return true;
                case "Immunization.occurrence": return true;
                case "Immunization.occurrence[x]": return true;
            }
        }
        return false;
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
            case "Consent":
            case "Coverage":
            case "DeviceUseStatement":
            case "DocumentReference":
            case "Encounter":
            case "HealthcareService":
            case "Immunization":
            case "Location":
            case "Medication":
            case "MedicationAdministration":
            case "MedicationDispense":
            case "MedicationRequest":
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

        try {
            // if (codePath != null && choicesPath.equals(codePath)) {
            //     Consolidate getPrimaryCodes() and choicesCodes somehow and bind that VS to the choicesPath

            // For Observations, it is a valid scenario for the Data Dictionary (DD) to not have a Data Element entry for the primary code path element - Observation.code.
            // In this case, the tooling should ensure that this element is created. The code element should be bound to a ValueSet that contains all codes
            // specified by the Data Element record mapped to Observation.value[x]. For all other resource types it is invalid to not have a primary
            // code path element entry in the DD
            Boolean primaryCodePathElementAdded = false;
            if (codePath != null
                    && !codePath.isEmpty()
                    && element.getPrimaryCodes() != null) {
//                    && element.getPrimaryCodes().getCodes().size() > 0
//                    && !choicesPath.equalsIgnoreCase(codePath)) {
                String elementId = String.format("%s.%s", resourceType, codePath);
                String primaryCodePath = String.format("%s.%s", resourceType, codePath);

                ElementDefinition existingPrimaryCodePathElement = getDifferentialElement(sd, elementId);

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
                        ensureTerminologyAndBindToElement(element, sd, ed, null, null, true);
                        primaryCodePathElementAdded = true;
                    }

                    addElementToStructureDefinition(sd, ed);
                    applyDataElementToElementDefinition(element, sd, ed);
                } else {
                    Type existingCode = existingPrimaryCodePathElement.getFixed();
                    // The code in the Primary Code Path Data Element entry should always have priority over the preferred (value[x])
                    if ((existingCode == null || isPrimaryCodePath) && (isPreferredCodePath)) {
                        //TODO: Bind to valueset rather than fixed code
                        existingPrimaryCodePathElement.setFixed(element.getPrimaryCodes().getCodes().get(0).toCodeableConcept());
                    }
                }
            }

            Boolean isSlice = element.getMasterDataType().toLowerCase().equals("slice");
            String masterDataElementPath = element.getMasterDataElementPath();
            Boolean isElementOfSlice = !isSlice && masterDataElementPath != null && !masterDataElementPath.isEmpty() && masterDataElementPath.indexOf(".") > 0;

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
            } else {
//                if (isChoiceType(elementPath)) {
//                    String elementFhirType = getFhirTypeOfTargetElement(elementPath);
//                    elementFhirType = elementFhirType.substring(0, 1).toUpperCase() + elementFhirType.substring(1);
//                    elementId = elementPath.getResourceTypeAndPath().replace("[x]", elementFhirType);
//                } else {
                    elementId = String.format("%s.%s", resourceType, choicesPath);
//                }
            }

            ElementDefinition existingElement = getDifferentialElement(sd, elementId);

            // if the element doesn't exist, create it
            if (existingElement == null) {
                if (isSlice) {
                    ensureSliceAndBaseElementWithSlicing(element, elementPath, sd, elementId, sliceName, null);
                } else {
                    String elementFhirType = getFhirTypeOfTargetElement(elementPath);

                    // Split the elementPath on . then ensure an element for all between 1st and last.
                    String[] pathParts = elementPath.getResourcePath().split("\\.");
                    if (pathParts.length > 1) {
                        List<String> pathPartsCumulative = new ArrayList<>();
                        pathPartsCumulative.add(elementPath.getResourceType());
                        for (int i = 0; i < pathParts.length - 1; i++) {
                            pathPartsCumulative.add(pathParts[i]);
                            String path = String.join(".", String.join(".", pathPartsCumulative));
                            String id = path;
                            ElementDefinition pathElement = new ElementDefinition();
                            pathElement.setId(id);
                            pathElement.setPath(path);

                            ElementDefinition existing = getDifferentialElement(sd, id);
                            if (existing == null) {
                                addElementToStructureDefinition(sd, pathElement);
                            }
                        }
                    }

                    ElementDefinition ed = new ElementDefinition();
                    ed.setId(elementId);
                    ed.setPath(elementId);
                    ed.setMin((toBoolean(element.getRequired()) || isRequiredElement(element)) ? 1 : 0);
                    // BTR-> This will almost always be 1, and I don't think we currently have any where it wouldn't, because a
                    // multiple choice element would actually be multiple observations, rather than a single observation with multiple values
                    ed.setMax("1"); //isMultipleChoiceElement(element) ? "*" : "1");
                    ed.setMustSupport(true);

                    ElementDefinition.TypeRefComponent edtr = new ElementDefinition.TypeRefComponent();
                    if (elementFhirType != null && elementFhirType.length() > 0) {
                        edtr.setCode(elementFhirType);
                        ed.addType(edtr);
                    }

                    // If this is an Observation and we've already created the primary code path element, do not bind the
                    // targeted/mapped element (e.g., value[x]) to the primary codes valueset - that was done above
                    if (!primaryCodePathElementAdded) {// && codePath != null) {
                        ensureTerminologyAndBindToElement(element, sd, ed, null, null, true);
                    }
                    addElementToStructureDefinition(sd, ed);
                    applyDataElementToElementDefinition(element, sd, ed);

                    // UnitOfMeasure-specific block
                    String unitOfMeasure = element.getUnitOfMeasure();
                    Boolean hasUnitOfMeasure = unitOfMeasure != null && !unitOfMeasure.isEmpty();
                    if (isChoiceType(elementPath) && hasUnitOfMeasure) {
                        ElementDefinition unitElement = new ElementDefinition();
                        unitElement.setId(elementId + ".unit");
                        unitElement.setPath(elementId + ".unit");
                        unitElement.setMin(1);
                        unitElement.setMax("1");
                        unitElement.setMustSupport(true);

                        //TODO: This should be a code, not fixed string
                        ElementDefinition.TypeRefComponent uitr = new ElementDefinition.TypeRefComponent();
                        if (elementFhirType != null && elementFhirType.length() > 0) {
                            uitr.setCode("string");
                            unitElement.addType(uitr);
                        }
                        unitElement.setFixed(new StringType(unitOfMeasure));

                        addElementToStructureDefinition(sd, unitElement);
                    }
                }
            } else {
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

                    // If this is an Observation and we've already created the primary code path element, do not bind the
                    // targeted/mapped element (e.g., value[x]) to the primary codes valueset - that was done above
                    if (!primaryCodePathElementAdded) {// && codePath != null) {
                        ensureTerminologyAndBindToElement(element, sd, existingElement, null, null, true);
                    }
                }
            }

            ensureChoicesDataElement(element, sd);

        } catch (Exception e) {
            logger.error(String.format("Error ensuring element for '%s'. Error: %s ", element.getLabel(), e));
        }
    }

    private String getValueSetId(String valueSetName) {
        String id = valueSetNameMap.get(valueSetName);
        if (id == null) {
            id = valueSetName;
        }
        return toId(id);
    }

    private void ensureChoicesDataElement(DictionaryElement dictionaryElement, StructureDefinition sd) {
        if (dictionaryElement.getChoices() != null && dictionaryElement.getChoices().getFhirElementPath() != null) {
            String choicesElementId = dictionaryElement.getChoices().getFhirElementPath().getResourceTypeAndPath();
            ElementDefinition existingChoicesElement = getDifferentialElement(sd, choicesElementId);

            ValueSet valueSetToBind = null;

            Map<String, List<DictionaryCode>> valueSetCodes = dictionaryElement.getChoices().getValueSetCodes();
            String parentCustomValueSetName = dictionaryElement.getCustomValueSetName();
            if (parentCustomValueSetName == null || parentCustomValueSetName.isEmpty()) {
                parentCustomValueSetName = dictionaryElement.getDataElementLabel();
            }

            List<ValueSet> valueSets = new ArrayList<ValueSet>();
            for (Map.Entry<String, List<DictionaryCode>> vs: valueSetCodes.entrySet()) {
                ValueSet valueSet = ensureValueSetWithCodes(getValueSetId(vs.getKey()), vs.getKey(), new CodeCollection(vs.getValue()));
                valueSets.add(valueSet);

                valueSetToBind = valueSet;
            }

            if (valueSetCodes != null && valueSetCodes.size() > 1) {
                String choicesGrouperValueSetName =  parentCustomValueSetName + " Choices Grouper";
                String choicesGrouperValueSetId = dictionaryElement.getId() + "-choices-grouper";
                valueSetNameMap.put(choicesGrouperValueSetName, choicesGrouperValueSetId);
                valueSetToBind = createGrouperValueSet(getValueSetId(choicesGrouperValueSetName), choicesGrouperValueSetName, valueSets);
            }

            //TODO: Include the primaryCodes valueset in the grouper. Add the codes to the VS in the single VS case.
//            if (dictionaryElement.getFhirElementPath().getResourceTypeAndPath().equalsIgnoreCase(choicesElementId)) {
//                List<DictionaryCode> primaryCodes = dictionaryElement.getPrimaryCodes().getCodes();
//                codes.getCodes().addAll(primaryCodes);
//            }

            if (existingChoicesElement != null) {
                ElementDefinition.ElementDefinitionBindingComponent existingBinding = existingChoicesElement.getBinding();
                if (existingBinding == null || existingBinding.getId() == null) {
                    bindValueSetToElement(existingChoicesElement, valueSetToBind, dictionaryElement.getBindingStrength());
                    bindQuestionnaireItemAnswerValueSet(dictionaryElement, valueSetToBind);
                }
            } else {
                ElementDefinition ed = new ElementDefinition();
                ed.setId(choicesElementId);
                String choicesElementPath = choicesElementId;

                // If the Id is one of an extension element, that path should not include the slice name
                if (choicesElementId.contains(":")) {
                    String[] pathParts = choicesElementId.split("\\.");
                    List<String> outputPathParts = new ArrayList<>();
                    for (String pathElement : pathParts) {
                        String[] components = pathElement.split("\\:");
                        outputPathParts.add(components[0]);
                    }

                    choicesElementPath = String.join(".", outputPathParts);
                }

                ed.setPath(choicesElementPath);
                ed.setMin(1);
                // BTR-> This will almost always be 1, and I don't think we currently have any where it wouldn't, because a
                // multiple choice element would actually be multiple observations, rather than a single observation with multiple values
                ed.setMax("1"); //isMultipleChoiceElement(dictionaryElement) ? "*" : "1");
                ed.setMustSupport(true);

                String elementFhirType = getFhirTypeOfTargetElement(dictionaryElement.getFhirElementPath());
                ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
                if (elementFhirType != null && elementFhirType.length() > 0) {
                    tr.setCode(elementFhirType);
                    ed.addType(tr);
                }

                bindQuestionnaireItemAnswerValueSet(dictionaryElement, valueSetToBind);

                bindValueSetToElement(ed, valueSetToBind, dictionaryElement.getBindingStrength());
                addElementToStructureDefinition(sd, ed);
                applyDataElementToElementDefinition(dictionaryElement, sd, ed);
            }
        }
    }

    private void bindQuestionnaireItemAnswerValueSet(DictionaryElement dictionaryElement, ValueSet valueSetToBind) {
        Questionnaire questionnaire =
                questionnaires.stream().filter(q -> q.getId().equalsIgnoreCase(toUpperId(getActivityCoding(dictionaryElement.getPage()).getCode()))).findFirst().get();
        Questionnaire.QuestionnaireItemComponent questionnaireItem =
                questionnaire.getItem().stream().filter(i -> i.getText().equalsIgnoreCase(dictionaryElement.getLabel())).findFirst().get();
        questionnaireItem.setAnswerValueSet(valueSetToBind.getUrl());
    }

    private void ensureSliceAndBaseElementWithSlicing(DictionaryElement dictionaryElement, DictionaryFhirElementPath elementPath,
        StructureDefinition sd, String elementId, String sliceName, ElementDefinition elementDefinition) {

        // Ensure the base definition exists
        String baseElementId = elementId.replace(":" + sliceName, "");
        ElementDefinition existingBaseDefinition = getDifferentialElement(sd, baseElementId);

        ElementDefinition.DiscriminatorType discriminatorType = ElementDefinition.DiscriminatorType.VALUE;
        String discriminatorPath = dictionaryElement.getAdditionalFHIRMappingDetails().split("=")[0].trim();
        String resourceTypePath = elementPath.getResourceTypeAndPath();
        discriminatorPath = discriminatorPath.replaceAll(resourceTypePath + ".", "");

        if (existingBaseDefinition != null) {
            ensureElementHasSlicingWithDiscriminator(existingBaseDefinition, discriminatorType, discriminatorPath);
        }
        else {
            ElementDefinition ed = new ElementDefinition();
            ed.setId(baseElementId);
            ed.setPath(elementPath.getResourceTypeAndPath());
            ed.setMin((toBoolean(dictionaryElement.getRequired()) || isRequiredElement(dictionaryElement)) ? 1 : 0);
            //ed.setMax(isMultipleChoiceElement(dictionaryElement) ? "*" : "1");
            ed.setMax("*");
            ed.setMustSupport(true);

            ElementDefinition.TypeRefComponent tr = new ElementDefinition.TypeRefComponent();
            String elementFhirType = getFhirTypeOfTargetElement(elementPath);
            if (elementFhirType != null && elementFhirType.length() > 0) {
                tr.setCode(elementFhirType);
                ed.addType(tr);
            }

            ensureElementHasSlicingWithDiscriminator(ed, discriminatorType, discriminatorPath);

            addElementToStructureDefinition(sd, ed);
        }

        /* Add the actual Slice (e.g., telecom:Telephone1) */
        String discriminatorValue = dictionaryElement.getAdditionalFHIRMappingDetails().split("=")[1].trim();
        ElementDefinition sliceElement = new ElementDefinition();
        sliceElement.setId(elementId);
        sliceElement.setSliceName(sliceName);
//        sliceElement.setBase()
        sliceElement.setPath(elementPath.getResourceTypeAndPath());
        // NOTE: Passing everything through as a string for now.
        sliceElement.setFixed(new StringType(discriminatorValue));
        sliceElement.setMin(toBoolean(dictionaryElement.getRequired()) ? 1 : 0);
        sliceElement.setMax(isMultipleChoiceElement(dictionaryElement) ? "*" : "1");

        addElementToStructureDefinition(sd, sliceElement);
        applyDataElementToElementDefinition(dictionaryElement, sd, sliceElement);
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

    private void ensureTerminologyAndBindToElement(DictionaryElement dictionaryElement, StructureDefinition targetStructureDefinition,
                                                   ElementDefinition targetElement, CodeCollection codes, String customValueSetName,
                                                   Boolean isPrimaryDataElement) {
        // Can only bind bindable types (e.g., CodeableConcept).
        // Observation.code is special case - if mapping is Observation.value[x] with a non-bindable type, we'll still need
        // to allow for binding of Observation.code (the primary code path)
        if (isBindableType(dictionaryElement) || targetElement.getPath().equals("Observation.code")) {
            String valueSetId = toId(dictionaryElement.getId());
            String valueSetLabel = dictionaryElement.getLabel();
            String valueSetName = null;

            if (customValueSetName != null && !customValueSetName.isEmpty()) {
                valueSetName = customValueSetName;
                valueSetLabel = customValueSetName;
            }

            if (valueSetName == null || valueSetName.isEmpty()) {
                valueSetName = dictionaryElement.getCustomValueSetName();
                valueSetLabel = dictionaryElement.getCustomValueSetName();
            }

            if (valueSetName == null || valueSetName.isEmpty()) {
                valueSetName = dictionaryElement.getName();
                valueSetLabel = dictionaryElement.getName();
            }

            CodeCollection codesToBind = codes;
            if (codesToBind == null || codesToBind.size() == 0) {
                codesToBind = dictionaryElement.getPrimaryCodes();
            }

            valueSetNameMap.put(valueSetName, valueSetId);
            ValueSet valueSet = null;
            if (codesToBind != null) {
                valueSet = ensureValueSetWithCodes(getValueSetId(valueSetName), valueSetLabel, codesToBind);
            }

            if (valueSet != null) {

                Enumerations.BindingStrength bindingStrength = dictionaryElement.getBindingStrength();
                // Bind the current element to the valueSet
                bindValueSetToElement(targetElement, valueSet, bindingStrength);

                if (!targetElement.getPath().equalsIgnoreCase("observation.code")) {
                    bindQuestionnaireItemAnswerValueSet(dictionaryElement, valueSet);
                }

                if (isPrimaryDataElement) {
                    valueSetLabel = valueSetId;
                    for (ValueSet vs : valueSets) {
                        if (vs.getId().equals(valueSetId)) {
                            valueSetLabel = vs.getTitle();
                        }
                    }

                    dictionaryElement.setTerminologyIdentifier(valueSetLabel);

                    DictionaryFhirElementPath retrieveFhirElementPath = null;
                    MultipleChoiceElementChoices choices = dictionaryElement.getChoices();
                    // If element has choices, set the choices FhirElementPath in the retrieveInfo
                    if (choices.getFhirElementPath() != null && choices.getValueSetCodes().size() > 0) {
                        retrieveFhirElementPath = dictionaryElement.getFhirElementPath();
                    }
                    retrieves.add(new RetrieveInfo(targetStructureDefinition, valueSetLabel, retrieveFhirElementPath));
                }
            }
        }
    }

    private void bindValueSetToElement(ElementDefinition targetElement, ValueSet valueSet, Enumerations.BindingStrength bindingStrength) {
        ElementDefinition.ElementDefinitionBindingComponent binding =
            new ElementDefinition.ElementDefinitionBindingComponent();
        binding.setStrength(bindingStrength);
        binding.setValueSet(valueSet.getUrl());
        binding.addExtension("http://hl7.org/fhir/StructureDefinition/elementdefinition-bindingName", valueSet.getTitleElement());
        targetElement.setBinding(binding);
    }

    @Nonnull
    private ValueSet ensureValueSetWithCodes(String valueSetId, String valueSetLabel, CodeCollection codes) {
        // Ensure the ValueSet
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
            valueSet.setUrl(String.format("%s/ValueSet/%s", canonicalBase, valueSetId));
            valueSet.setName(toName(valueSetLabel));
            valueSet.setTitle(String.format("%s", valueSetLabel));
            valueSet.setStatus(Enumerations.PublicationStatus.DRAFT);
            valueSet.setExperimental(false);
            valueSet.setDescription(String.format("Codes representing possible values for the %s element", valueSetLabel));
            valueSet.setImmutable(true);
        }

        // Ensure Compose element
        ValueSet.ValueSetComposeComponent compose = valueSet.getCompose();
        if (compose == null) {
            compose = new ValueSet.ValueSetComposeComponent();
            valueSet.setCompose(compose);
        }

        // Group by Supported Terminology System
        for (String codeSystemUrl : codes.getCodeSystemUrls()) {
            List<DictionaryCode> systemCodes = codes.getCodesForSystem(codeSystemUrl);

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
                    conceptReference.setDisplay(code.getDisplay());

                    // Only add the concept if it does not already exist in the ValueSet (based on both Code and Display)
                    if (conceptReferences.stream().noneMatch(o -> o.getCode().equals(conceptReference.getCode())
                            && o.getDisplay().equals(conceptReference.getDisplay()))) {
                        conceptSet.addConcept(conceptReference);
                    }

                    // Add mappings for this code to the appropriate concept map
                    addConceptMappings(code);
                }
            }
        }

        // If the ValueSet did not already exist, add it to the valueSets collection
        if (!valueSetExisted) {
            valueSets.add(valueSet);
        }
        return valueSet;
    }

    private String getCodeSystemLabel(String systemUrl) {
        for (Map.Entry<String, String> entry : supportedCodeSystems.entrySet()) {
            if (entry.getValue().equals(systemUrl)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /*
    Not guaranteed to return a concept map, will only return for known supported code systems
     */
    private ConceptMap getConceptMapForSystem(String systemUrl) {
        ConceptMap cm = conceptMaps.get(systemUrl);
        if (cm == null) {
            String codeSystemLabel = getCodeSystemLabel(systemUrl);
            if (codeSystemLabel != null) {
                cm = new ConceptMap();
                cm.setId(codeSystemLabel);
                cm.setUrl(String.format("%s/ConceptMap/%s", canonicalBase, codeSystemLabel));
                cm.setName(codeSystemLabel);
                cm.setTitle(String.format("%s", codeSystemLabel));
                cm.setStatus(Enumerations.PublicationStatus.DRAFT);
                cm.setExperimental(false);
                cm.setDescription(String.format("Concept mapping from content extended codes to %s", codeSystemLabel));
                conceptMaps.put(systemUrl, cm);
            }
        }

        return cm;
    }

    private ConceptMap.ConceptMapGroupComponent getConceptMapGroupComponent(ConceptMap cm, String sourceUri) {
        for (ConceptMap.ConceptMapGroupComponent cmg : cm.getGroup()) {
            if (cmg.getSource().equals(sourceUri)) {
                return cmg;
            }
        }

        return null;
    }

    private void addConceptMappings(DictionaryCode code) {
        CodeCollection mappings = new CodeCollection(code.getMappings());
        for (String codeSystemUrl : mappings.getCodeSystemUrls()) {
            List<DictionaryCode> systemCodes = mappings.getCodesForSystem(codeSystemUrl);

            ConceptMap cm = getConceptMapForSystem(codeSystemUrl);
            if (cm != null) {
                ConceptMap.ConceptMapGroupComponent cmg = getConceptMapGroupComponent(cm, code.getSystem());
                if (cmg == null) {
                    cmg = cm.addGroup().setSource(code.getSystem()).setTarget(codeSystemUrl);
                }

                ConceptMap.SourceElementComponent sec = cmg.addElement().setCode(code.getCode()).setDisplay(code.getDisplay());
                for (DictionaryCode systemCode : systemCodes) {
                    sec.addTarget()
                            .setCode(systemCode.getCode())
                            .setDisplay(systemCode.getDisplay())
                            .setEquivalence(systemCode.getEquivalence() != null
                                    ? Enumerations.ConceptMapEquivalence.fromCode(systemCode.getEquivalence())
                                    : null);
                }
            }
        }
    }

    @Nonnull
    private ValueSet createGrouperValueSet(String valueSetId, String valueSetLabel, List<ValueSet> valueSetsToGroup) {
        // Ensure the ValueSet
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
            valueSet.setUrl(String.format("%s/ValueSet/%s", canonicalBase, valueSetId));
            valueSet.setName(toName(valueSetLabel));
            valueSet.setTitle(valueSetLabel);
            valueSet.setStatus(Enumerations.PublicationStatus.DRAFT);
            valueSet.setExperimental(false);
            valueSet.setDescription(String.format("Group Valueset with codes representing possible values for the %s element", valueSetLabel));
            valueSet.setImmutable(true);
        }

        valueSet.setDate(java.util.Date.from(Instant.now()));

        // Ensure Compose element
        ValueSet.ValueSetComposeComponent compose = valueSet.getCompose();
        if (compose == null) {
            compose = new ValueSet.ValueSetComposeComponent();
            valueSet.setCompose(compose);
        }

        // Ensure Expansion element
        ValueSet.ValueSetExpansionComponent targetExpansion = valueSet.getExpansion();
        if (targetExpansion == null) {
            targetExpansion = new ValueSet.ValueSetExpansionComponent();
            valueSet.setExpansion(targetExpansion);
        }
        targetExpansion.setTimestamp(java.util.Date.from(Instant.now()));

        // Add source valueset urls to compose of the grouper and all of the compose codes to the expansion of the grouper
        List<ValueSet.ConceptSetComponent> includes = valueSet.getCompose().getInclude();
//        ValueSet.ValueSetExpansionComponent targetExpansion = valueSet.getExpansion();
        List<ValueSet.ValueSetExpansionContainsComponent> targetContains = targetExpansion.getContains();

        for (ValueSet vs: valueSetsToGroup) {
            // Add source ValueSet URLs to grouper Compose
            if (includes.stream().noneMatch(i -> i.hasValueSet(vs.getUrl()))) {
                ValueSet.ConceptSetComponent include = new ValueSet.ConceptSetComponent();
                include.addValueSet(vs.getUrl());
                valueSet.getCompose().addInclude(include);
            }

            // NOTE: Very naive implementation that assumes a compose made up of actual include concepts. That is
            // a safe assumption in context of this Processor though and the ValueSets it creates at the time of this
            // implementation.
            if (vs.hasCompose() && vs.getCompose().hasInclude()) {
                for (ValueSet.ConceptSetComponent sourceInclude : vs.getCompose().getInclude()) {
                    String system = sourceInclude.getSystem();
                    for (ValueSet.ConceptReferenceComponent concept : sourceInclude.getConcept()) {
                        if (targetContains.stream().noneMatch(c -> c.getSystem().equals(system) && c.getCode().equals(concept.getCode()))) {
                            ValueSet.ValueSetExpansionContainsComponent newContains = new ValueSet.ValueSetExpansionContainsComponent();
                            newContains.setSystem(system);
                            newContains.setCode(concept.getCode());
                            newContains.setDisplay(concept.getDisplay());
                            targetContains.add(newContains);
                        }
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

    @Nonnull
    private CodeSystem createCodeSystem(String name, String canonicalBase, String title, String description) {
        CodeSystem codeSystem = new CodeSystem();

        codeSystem.setId(toId(name));
        codeSystem.setUrl(String.format("%s/CodeSystem/%s", canonicalBase, codeSystem.getId()));
        // TODO: version
        codeSystem.setName(toName(name));
        codeSystem.setTitle(String.format("%s", title != null ? title : name));
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

    private CanonicalResourceAtlas getAtlas() {
        if (atlas == null) {
            atlas =
                    new CanonicalResourceAtlas()
                            .setValueSets(new InMemoryCanonicalResourceProvider<ValueSet>(this.valueSets))
                            .setCodeSystems(new InMemoryCanonicalResourceProvider<CodeSystem>(this.codeSystems))
                            .setConceptMaps(new InMemoryCanonicalResourceProvider<ConceptMap>(this.conceptMaps.values()));
        }
        return atlas;
    }

    public void processTestCases() {
        if (testCaseInput != null && !testCaseInput.isEmpty()) {
            TestCaseProcessor tcp = new TestCaseProcessor();
            tcp.setAtlas(getAtlas());
            tcp.setProfilesByElementId(profilesByElementId);
            testCases = tcp.process(testCaseInput);
        }
    }

    // Generate example resources for each profile
    public void processExamples() {
        ExampleBuilder eb = new ExampleBuilder();
        eb.setAtlas(getAtlas());
        eb.setPatientContext("anc-patient-example");
        eb.setEncounterContext("anc-encounter-example");
        eb.setLocationContext("anc-location-example");
        eb.setPractitionerContext("anc-practitioner-example");
        eb.setPractitionerRoleContext("anc-practitionerrole-example");
        for (StructureDefinition sd : profiles) {
            examples.put(sd.getUrl(), eb.build(sd));
        }
    }

    /* Write Methods */
    public void writeResource(String path, Resource resource) {
        String outputFilePath = path + "/" + resource.getResourceType().toString().toLowerCase() + "-" + resource.getIdElement().getIdPart() + "." + encoding;
        try (FileOutputStream writer = new FileOutputStream(outputFilePath)) {
            writer.write(
                encoding.equals("json")
                    ? FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                    : FhirContext.forR4Cached().newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing resource: " + resource.getIdElement().getIdPart());
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

    private void indexProfileByActivity(Coding activityCoding, StructureDefinition sd) {
        String activityId = activityCoding.getCode();
        if (activityId != null) {
            List<StructureDefinition> sds = profilesByActivityId.get(activityId);
            if (sds == null) {
                sds = new ArrayList<StructureDefinition>();
                profilesByActivityId.put(activityId, sds);
            }
            if (!sds.contains(sd)) {
                sds.add(sd);
            }
        }
    }

    private void indexProfileByParent(String parentUrl, StructureDefinition sd) {
        List<StructureDefinition> sds = profilesByParentProfile.get(parentUrl);
        if (sds == null) {
            sds = new ArrayList<StructureDefinition>();
            profilesByParentProfile.put(parentUrl, sds);
        }
        if (!sds.contains(sd)) {
            sds.add(sd);
        }
    }

    private void indexProfile(StructureDefinition sd) {
        // Index the profile by Activity Id
        Coding activityCoding = getActivityCoding(sd);
        if (activityCoding != null) {
            indexProfileByActivity(activityCoding, sd);
        }
        // Index the profile by Parent profile
        String parentUrl = sd.getBaseDefinition();
        if (parentUrl != null) {
            indexProfileByParent(parentUrl, sd);
        }
    }

    public void writeExamples(String scopePath) {
        if (examples != null && examples.size() > 0) {
            String examplesPath = getExamplesPath(scopePath);
            ensureExamplesPath(scopePath);
            for (Map.Entry<String, Resource> entry : examples.entrySet()) {
                writeResource(examplesPath, entry.getValue());
            }
        }
    }

    public void writeTestCases(String scopePath) {
        if (testCases != null && testCases.size() > 0) {
            String testsPath = getTestsPath(scopePath);
            ensureTestsPath(scopePath);
            for (Map.Entry<String, List<Resource>> entry : testCases.entrySet()) {
                String testPath = getTestPath(scopePath, entry.getKey());
                ensureTestPath(scopePath, entry.getKey());
                for (Resource r : entry.getValue()) {
                    writeResource(testPath, r);
                }
            }
        }
    }

    public void writeProfiles(String scopePath) {
        if (profiles != null && profiles.size() > 0) {
            String profilesPath = getProfilesPath(scopePath);
            ensureProfilesPath(scopePath);

            Comparator<ElementDefinition> compareById = Comparator.comparing(Element::getId);

            for (StructureDefinition sd : profiles) {
                //sd.getDifferential().getElement().sort(compareById);
                indexProfile(sd);
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

    public void processQuestionnaires() {
        for (Questionnaire q : questionnaires) {
            for (Questionnaire.QuestionnaireItemComponent item : q.getItem()) {
                if (item.hasDefinition()) {
                    String definition = item.getDefinition();
                    DictionaryElement de = elementsById.get(definition);
                    if (de != null) {
                        StructureDefinition sd = profilesByElementId.get(de.getId());
                        if (sd != null) {
                            if (de.getFhirElementPath() != null && de.getFhirElementPath().getResourcePath() != null) {
                                item.setDefinition(String.format("%s#%s", sd.getUrl(), de.getFhirElementPath().getResourceTypeAndPath()));
                            }
                            else {
                                item.setDefinition(sd.getUrl());
                            }
                        }
                        else {
                            item.setDefinition(null);
                        }
                    }
                    else {
                        item.setDefinition(null);
                    }
                }
            }
        }
    }

    public void writeQuestionnaires(String scopePath) {
        if (questionnaires != null && questionnaires.size() > 0) {
            String questionnairePath = getQuestionnairePath(scopePath);
            ensureQuestionnairePath(scopePath);

            for (Questionnaire q : questionnaires) {
                writeResource(questionnairePath, q);

                // Generate JSON fragment for inclusion in the IG:
                /*
                    "Questionnaire/<id>": {
                        "source": "questionnaire/questionnaire-<id>.json",
                        "base": "Questionnaire-<id>.html"
                    }
                 */
                igJsonFragments.add(String.format("\t\t\"Questionnaire/%s\": {\r\n\t\t\t\"source\": \"questionnaire/questionnaire-%s.json\",\r\n\t\t\t\"base\": \"Questionnaire-%s.html\"\r\n\t\t}",
                        q.getId(), q.getId(), q.getId()));

                // Generate XML fragment for the IG resource:
                /*
                    <resource>
                        <reference>
                            <reference value="Questionnaire/<id>"/>
                        </reference>
                        <groupingId value="main"/>
                    </resource>
                 */
                igResourceFragments.add(String.format("\t\t\t<resource>\r\n\t\t\t\t<reference>\r\n\t\t\t\t\t<reference value=\"Questionnaire/%s\"/>\r\n\t\t\t\t</reference>\r\n\t\t\t\t<groupingId value=\"main\"/>\r\n\t\t\t</resource>", q.getId()));
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

    public void writeConceptMaps(String scopePath) {
        if (conceptMaps != null && conceptMaps.size() > 0) {
            String conceptMapPath = getConceptMapPath(scopePath);
            ensureConceptMapPath(scopePath);

            for (ConceptMap cm : conceptMaps.values()) {
                writeResource(conceptMapPath, cm);

                // Generate JSON fragment for inclusion in the IG:
                /*
                    "ConceptMap/<id>": {
                        "source": "conceptmap/conceptmap-<id>.json",
                        "base": "ConceptMap-<id>.html"
                    }
                 */
                igJsonFragments.add(String.format("\t\t\"ConceptMap/%s\": {\r\n\t\t\t\"source\": \"conceptmap/conceptmap-%s.json\",\r\n\t\t\t\"base\": \"ConceptMap-%s.html\"\r\n\t\t}",
                        cm.getId(), cm.getId(), cm.getId()));

                // Generate XML fragment for the IG resource:
                /*
                    <resource>
                        <reference>
                            <reference value="ConceptMap/<id>"/>
                        </reference>
                        <groupingId value="main"/>
                    </resource>
                 */
                igResourceFragments.add(String.format("\t\t\t<resource>\r\n\t\t\t\t<reference>\r\n\t\t\t\t\t<reference value=\"ConceptMap/%s\"/>\r\n\t\t\t\t</reference>\r\n\t\t\t\t<groupingId value=\"main\"/>\r\n\t\t\t</resource>", cm.getId()));
            }
        }
    }

    public String getCodeSystemIdentifier(CodeSystem cs) {
        if (cs != null) {
            String identifier = cs.hasTitle() ? cs.getTitle() : cs.getName();
            if (cs.hasVersion()) {
                identifier = String.format("%s (%s)", identifier, cs.getVersion());
            }

            return identifier;
        }

        return null;
    }

    public String getCodeSystemIdentifier(String url) {
        for (Map.Entry<String, String> e : supportedCodeSystems.entrySet()) {
            if (e.getValue().equals(url)) {
                return e.getKey();
            }
        }

        return null;
    }

    public String getCodeSystemIdentifier(Coding coding) {
        CodeSystem result = null;
        for (CodeSystem cs : codeSystems) {
            if (coding.getSystem().equals(cs.getUrl())) {
                if (coding.hasVersion() && cs.hasVersion() && coding.getVersion().equals(cs.getVersion())) {
                    result = cs;
                    break;
                }

                if (!coding.hasVersion() && !cs.hasVersion()) {
                    result = cs;
                    break;
                }

                // TODO: Use a terminology service to resolve this?
            }
        }

        if (result != null) {
            return getCodeSystemIdentifier(result);
        }

        return getCodeSystemIdentifier(coding.getSystem());
    }

    public void writeConcepts(String scope, String scopePath) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("library %sConcepts", scope));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        sb.append("// Code Systems");
        sb.append(System.lineSeparator());
        // Supported code systems
        for (Map.Entry<String, String> entry : supportedCodeSystems.entrySet()) {
            sb.append(String.format("codesystem \"%s\": '%s'", entry.getKey(), entry.getValue()));
            sb.append(System.lineSeparator());
        }
        // For each code system, generate a codesystem CQL entry:
        // codesystem "CodeSystem.title": 'CodeSystem.url' [version 'CodeSystem.version']
        for (CodeSystem cs : codeSystems) {
            String identifier = getCodeSystemIdentifier(cs);
            sb.append(String.format("codesystem \"%s\": '%s'", identifier, cs.getUrl()));
            if (cs.hasVersion()) {
                sb.append(String.format(" version '%s'", cs.getVersion()));
            }
            sb.append(System.lineSeparator());
        }

        sb.append(System.lineSeparator());
        sb.append("// Value Sets");
        sb.append(System.lineSeparator());
        // For each value set generate a valueset CQL entry:
        // valueset "ValueSet.title": 'ValueSet.url' [version 'ValueSet.version']
        for (ValueSet vs : valueSets) {
            sb.append(String.format("valueset \"%s\": '%s'", vs.hasTitle() ? vs.getTitle() : vs.getName(), vs.getUrl()));
            if (vs.hasVersion()) {
                sb.append(String.format(" version '%s'", vs.getVersion()));
            }
            sb.append(System.lineSeparator());
        }

        sb.append(System.lineSeparator());
        sb.append("// Codes");
        sb.append(System.lineSeparator());
        // For each concept generate a code entry:
        // code "ConceptName": 'Coding.value' from 'getCodeSystemName(Coding.system)' display 'Coding.display'
        for (Map.Entry<String, Coding> entry : concepts.entrySet()) {
            sb.append(String.format("code \"%s\": '%s' from \"%s\"", entry.getKey(), entry.getValue().getCode(), getCodeSystemIdentifier(entry.getValue())));
            if (entry.getValue().hasDisplay()) {
                sb.append(String.format(" display '%s'", entry.getValue().getDisplay()));
            }
            sb.append(System.lineSeparator());
        }

        ensureCqlPath(scopePath);
        try (FileOutputStream writer = new FileOutputStream(getCqlPath(scopePath) + "/" + scope + "Concepts.cql")) {
            writer.write(sb.toString().getBytes());
            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing concepts library source");
        }
    }

    private Identifier getDataElementIdentifier(Iterable<Identifier> identifiers) {
        for (Identifier i : identifiers) {
            if (i.hasSystem() && i.getSystem().equals(dataElementIdentifierSystem)) {
                return i;
            }
        }

        return null;
    }

    private Coding getActivityCoding(CodeableConcept concept) {
        if (concept.hasCoding()) {
            for (Coding c : concept.getCoding()) {
                if (activityCodeSystem.equals(c.getSystem())) {
                    return c;
                }
            }
        }

        return null;
    }

    private Coding getActivityCoding(StructureDefinition sd) {
        if (sd.hasUseContext()) {
            for (UsageContext uc : sd.getUseContext()) {
                if ("http://terminology.hl7.org/CodeSystem/usage-context-type".equals(uc.getCode().getSystem())
                        && "task".equals(uc.getCode().getCode())) {
                    return getActivityCoding(uc.getValueCodeableConcept());
                }
            }
        }

        return null;
    }

    private Comparator<String> activityIdComparator = new Comparator<String>() {
        private int indexOfFirstDigit(String s) {
            for (int i = 0; i < s.length(); i++) {
                if (Character.isDigit(s.charAt(i))) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public int compare(String s1, String s2) {
            int s1i = indexOfFirstDigit(s1);
            int s2i = indexOfFirstDigit(s2);
            if (s1i <= 0 || s2i <= 0) {
                return 0;
            }

            String s1a = s1.substring(0, s1i);
            String s2a = s2.substring(0, s2i);
            int ac = s1a.compareTo(s2a);
            if (ac == 0) {
                String s1b = s1.substring(s1i);
                String s2b = s2.substring(s2i);
                String[] s1parts = s1b.split("\\.");
                String[] s2parts = s2b.split("\\.");
                for (int partIndex = 0; partIndex < s1parts.length; partIndex++) {
                    if (partIndex >= s2parts.length) {
                        return 1;
                    }
                    ac = Integer.compare(Integer.valueOf(s1parts[partIndex]), Integer.valueOf(s2parts[partIndex]));
                    if (ac != 0) {
                        return ac;
                    }
                }
                if (s2parts.length > s1parts.length) {
                    return -1;
                }

                return ac;
            }
            else {
                return ac;
            }
        }
    };

    private void writeDataElement(StringBuilder sb, StructureDefinition sd, String context) {
        // TODO: Consider writing this to an extension on the structuredefinition instead of to the retrieveInfo like this
        //for (RetrieveInfo retrieve : retrieves) {
        //    if (retrieve.structureDefinition.getId().equals(sd.getId())) {
        // BTR -> Switched to drive off the data elements mapped into this profile
        List<DictionaryElement> lde = elementsByProfileId.get(sd.getId());
        if (lde != null) {
            for (DictionaryElement de : lde) {
                //String title = sd.hasTitle() ? sd.getTitle() : sd.hasName() ? sd.getName() : sd.getId();
                String title = de.getDataElementLabel();
                sb.append("/*");
                sb.append(System.lineSeparator());
                sb.append("  @dataElement: ");
                sb.append(String.format("%s ", de.getId()));
                //Identifier dataElementIdentifier = getDataElementIdentifier(sd.getIdentifier());
                //if (dataElementIdentifier != null) {
                //    sb.append(String.format("%s ", dataElementIdentifier.getValue()));
                //}
                //sb.append(title);
                sb.append(de.getDataElementLabel());
                sb.append(System.lineSeparator());

                Coding activityCoding = getActivityCoding(sd);
                if (activityCoding != null) {
                    sb.append(String.format("  @activity: %s %s", activityCoding.getCode(), activityCoding.getDisplay()));
                    sb.append(System.lineSeparator());
                }

                if (sd.hasDescription()) {
                    //sb.append(String.format("  @description: %s", sd.getDescription()));
                    sb.append(String.format("  @description: %s", de.getDescription()));
                    sb.append(System.lineSeparator());
                }
                sb.append("*/");
                sb.append(System.lineSeparator());
                sb.append(String.format("define \"%s\":", title));
                sb.append(System.lineSeparator());
                // If we are generating for the context specified for the data element, and there is a selector, use it
                boolean inContext = (de.getContext() != null && de.getContext().equals(context))
                        || (de.getContext() == null && context.equals("Patient"));
                boolean useSelector = inContext && de.getSelector() != null;
                if (useSelector) {
                    sb.append(String.format("  WC.%s(", de.getSelector()));
                    sb.append(System.lineSeparator());
                }
                if (de.getTerminologyIdentifier() != null && !de.getTerminologyIdentifier().isEmpty()) {
                    sb.append(String.format("  [%s: Cx.\"%s\"]", sd.getType(), de.getTerminologyIdentifier()));
                }
                else {
                    sb.append(String.format("  [%s]", sd.getType()));
                }

                DictionaryFhirElementPath fhirElementPath = de.getFhirElementPath();

                // TODO: Switch on sd.baseDefinition to provide filtering here (e.g. status = 'not-done')
                String alias;
                switch (sd.getBaseDefinition()) {
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-base-patient":
                        alias = "P";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-base-encounter":
                        alias = "E";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("    where %s.id = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-condition":
                        alias = "C";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.clinicalStatus in FC.\"Active Condition\"", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("      and %s.verificationStatus ~ FC.\"confirmed\"", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            // TODO: Should this contextualize to encounter?
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-immunization":
                        alias = "I";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status = 'completed'", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-immunizationnotdone":
                        alias = "IND";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status = 'not-done'", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-medicationrequest":
                        alias = "MR";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append("    where MR.status in { 'draft', 'active', 'on-hold', 'completed' }");
                        sb.append(System.lineSeparator());
                        sb.append("      and Coalesce(MR.doNotPerform, false) is false");
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-medicationnotrequested":
                        alias = "MR";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append("    where MR.status in { 'draft', 'active', 'on-hold', 'completed' }");
                        sb.append(System.lineSeparator());
                        sb.append("      and MR.doNotPerform is true");
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-observation":
                        alias = "O";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status in { 'final', 'amended', 'corrected' }", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("      and Coalesce(WC.ModifierExtension(%s, 'who-notDone').value, false) is false", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-observationnotdone":
                        alias = "OND";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where WC.ModifierExtension(%s, 'who-notDone').value is true", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-procedure":
                        alias = "P";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status in { 'preparation', 'in-progress', 'on-hold', 'completed' }", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-procedurenotdone":
                        alias = "PND";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status = 'not-done'", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-servicerequest":
                        alias = "SR";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status in { 'draft', 'active', 'on-hold', 'completed' }", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("      and Coalesce(%s.doNotPerform, false) is false", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    case "http://fhir.org/guides/who/anc-cds/StructureDefinition/anc-servicenotrequested":
                        alias = "SNR";
                        sb.append(String.format(" %s", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("    where %s.status in { 'draft', 'active', 'on-hold', 'completed' }", alias));
                        sb.append(System.lineSeparator());
                        sb.append(String.format("      and %s.doNotPerform is true", alias));
                        sb.append(System.lineSeparator());
                        if (context.equals("Encounter")) {
                            sb.append(String.format("      and Last(Split(%s.encounter.reference, '/')) = EncounterId", alias));
                            sb.append(System.lineSeparator());
                        }
                        appendReturnClause(sb, fhirElementPath, alias, inContext, useSelector);
                        break;
                    default:
                        break;
                }
                sb.append(System.lineSeparator());
                sb.append(System.lineSeparator());
            }
        }
    }

    private void appendReturnClause(StringBuilder sb, DictionaryFhirElementPath fhirElementPath, String alias, boolean inContext, boolean useSelector) {
        if (useSelector) {
            sb.append("  )");
        }
        //TODO: If an extension, append the extension-specific return clause
        // P.extension E where E.url = 'http://fhir.org/guides/who-int/anc-cds/StructureDefinition/occupation' return E.value as CodeableConcept
        if (fhirElementPath != null) {
            String returnElementPath = fhirElementPath.getResourcePath();
            String cast = "";
            if (isChoiceType(fhirElementPath)) {
                returnElementPath = fhirElementPath.getResourcePath().replace("[x]", "");
                cast = String.format(" as FHIR.%s", fhirElementPath.getFhirElementType());
            }

            if (useSelector) {
                sb.append(String.format(".%s%s", returnElementPath, cast));
                sb.append(System.lineSeparator());
            }
            else if (inContext) {
                sb.append(String.format("    return %s.%s%s", alias, returnElementPath, cast));
                sb.append(System.lineSeparator());
            }

        }
    }

    private void writeActivityIndexHeader(StringBuilder activityIndex, String activityId) {
        activityIndex.append(System.lineSeparator());
        activityIndex.append(String.format("#### %s ", activityId));
        Coding activityCoding = activityMap.get(activityId);
        if (activityCoding != null) {
            activityIndex.append(activityCoding.getDisplay());
        }
        activityIndex.append(System.lineSeparator());
        activityIndex.append(System.lineSeparator());
        if (activityCoding != null) {
            String questionnaireId = toUpperId(activityCoding.getCode());
            activityIndex.append(String.format("Data elements for this activity can be collected using the [%s](Questionnaire-%s.html)", questionnaireId, questionnaireId));
            activityIndex.append(System.lineSeparator());
            activityIndex.append(System.lineSeparator());
        }
        activityIndex.append("|Id|Label|Description|Type|Profile Path|");
        activityIndex.append(System.lineSeparator());
        activityIndex.append("|---|---|---|---|---|");
        activityIndex.append(System.lineSeparator());
    }

    private void writeActivityIndexEntry(StringBuilder activityIndex, StructureDefinition sd) {
        List<DictionaryElement> lde = elementsByProfileId.get(sd.getId());
        if (lde != null) {
            for (DictionaryElement de : lde) {
                String path = de.getFhirElementPath() != null ? de.getFhirElementPath().getResourceTypeAndPath() : "";
                String type = de.getFhirElementPath() != null ? de.getFhirElementPath().getFhirElementType() : de.getType();
                activityIndex.append(String.format("|%s|%s|%s|%s|[%s](StructureDefinition-%s.html)|",
                        de.getId(), de.getDataElementLabel(), de.getDescription(), type, path, sd.getId()));
                activityIndex.append(System.lineSeparator());
            }
        }
    }

    public void writeDataElements(String scope, String scopePath) {
        writeDataElements(scope, scopePath, "Patient");
        writeDataElements(scope, scopePath, "Encounter");
    }

    public void writeDataElements(String scope, String scopePath, String context) {
        StringBuilder sb = new StringBuilder();
        StringBuilder activityIndex = new StringBuilder();

        sb.append(String.format("library %s%sDataElements", scope, context.equals("Encounter") ? "Contact" : ""));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        sb.append(String.format("using FHIR version '4.0.1'"));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(String.format("include FHIRHelpers version '4.0.1'"));
        sb.append(System.lineSeparator());
        sb.append(String.format("include FHIRCommon called FC"));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        sb.append("include WHOCommon called WC");
        sb.append(System.lineSeparator());
        sb.append(String.format("include %sCommon called AC", scope));
        sb.append(System.lineSeparator());
        sb.append(String.format("include %sConcepts called Cx", scope));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        // Context is always patient, will simulate Encounter context with parameterization...
        if (context != null && context.equals("Encounter")) {
            sb.append("parameter EncounterId String");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        sb.append("context Patient");
        //sb.append(String.format("context %s", context != null ? context : "Patient"));
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());

        // For each StructureDefinition, generate an Expression Definition:
        /*
        // @dataElement: StructureDefinition.identifier
        // @activity: StructureDefinition.useContext[task]
        // @description: StructureDefinition.description
        define "StructureDefinition.title":
          [StructureDefinition.resourceType: terminologyIdentifier]
         */

        List<String> activityIds = new ArrayList<String>(profilesByActivityId.keySet());
        activityIds.sort(activityIdComparator);
        for (String activityId : activityIds) {
            writeActivityIndexHeader(activityIndex, activityId);

            List<StructureDefinition> sds = profilesByActivityId.get(activityId);
            sds.sort(Comparator.comparing(StructureDefinition::getId));
            for (StructureDefinition sd : sds) {
                writeDataElement(sb, sd, context);
                writeActivityIndexEntry(activityIndex, sd);
            }
        }

        ensureCqlPath(scopePath);
        try (FileOutputStream writer = new FileOutputStream(getCqlPath(scopePath) + "/" + scope + (context.equals("Encounter") ? "Contact" : "") + "DataElements.cql")) {
            writer.write(sb.toString().getBytes());
            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing concepts library source");
        }

        try (FileOutputStream writer = new FileOutputStream(getCqlPath(scopePath) + "/" + scope + "DataElementsByActivity.md")) {
            writer.write(activityIndex.toString().getBytes());
            writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing profile activity index");
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
