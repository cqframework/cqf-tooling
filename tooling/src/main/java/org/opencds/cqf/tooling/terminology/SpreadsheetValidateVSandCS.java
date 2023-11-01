package org.opencds.cqf.tooling.terminology;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.opencds.cqf.tooling.terminology.fhirservice.FhirTerminologyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpreadsheetValidateVSandCS extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetValidateVSandCS.class);

    private FhirContext fhirContext;
    FhirTerminologyClient fhirClient = null;
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String urlToTestServer; // -urltotestserver (-uts)  server to validate
    private boolean hasHeader = true; // -hasheader (-hh)
    private String pathToIG; // -pathToIG (-ptig) path to IG - files installed using "npm --registry https://packages.simplifier.net install hl7.fhir.us.qicore@4.1.1" (or other package)
    Map<String, CodeSystem> csMap;
    Map<String, ValueSet> vsMap;
    private static final String newLine = System.getProperty("line.separator");
    private Set<String> csNotPresentInIG;
    private Set<String> vsNotPresentInIG;


    // The file name of the input spreadsheet
    private String spreadsheetName;

    private String getHeader(Row header, int columnIndex) {
        if (header != null) {
            return SpreadsheetHelper.getCellAsString(header, columnIndex).trim();
        } else {
            return CellReference.convertNumToColString(columnIndex);
        }
    }

    /*
    Command line:
        -SpreadsheetValidateVSandCS -pts=/Users/myname/hold/QICore.xlsx
        -hh=true -uts=https://uat-cts.nlm.nih.gov/fhir/
        -un=<test server username> -pw=<test server password>
        -op=/Users/myname/hold/
        -ptig=/Users/myname/Projects/FHIR-Spec
     */
    @Override
    public void execute(String[] args) {
        fhirContext = FhirContext.forR4Cached();
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default
        String userName = "";
        String password = "";
        csNotPresentInIG = new HashSet<>();
        vsNotPresentInIG = new HashSet<>();

        for (String arg : args) {
            if (arg.equals("-SpreadsheetValidateVSandCS")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "pathtospreadsheet":
                case "pts":
                    pathToSpreadsheet = value;
                    break; // -pathtospreadsheet (-pts)
                case "hasheader":
                case "hh":
                    hasHeader = Boolean.valueOf(value);
                    break; // -hasheader (-hh)
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "urlToTestServer":
                case "uts":
                    urlToTestServer = value;
                    break; // -urltotestserver (-uts)
                case "userName":
                case "un":
                    userName = value;
                    break; // -userName (-un)
                case "password":
                case "pw":
                    password = value;
                    break; // -password (-pw)
                case "pathToIG":
                case "ptig":
                    pathToIG = value;
                    break; // -pathToIG (-ptig)
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }
        Endpoint endpoint = new Endpoint().setAddress(urlToTestServer);
        fhirClient = new FhirTerminologyClient(fhirContext, endpoint, userName, password);

        validateSpreadsheet(userName, password);
        System.out.println("Finished with the validation.");
    }

    private void validateSpreadsheet(String userName, String password) {
        int firstSheet = 0;
        int idCellNumber = 1;
        int valueSetCellNumber = 3;
        int valueSetURLCellNumber = 4;
        int versionCellNumber = 5;
        int codeSystemURLCellNumber = 6;

        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.1.1";
        Atlas atlas = new Atlas();
        atlas.loadPaths(pathToIG, resourcePaths);
        csMap = atlas.getCodeSystems();
        vsMap = atlas.getValueSets();

        spreadsheetName = new File(pathToSpreadsheet).getName();
        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        Sheet sheet = workbook.getSheetAt(firstSheet);

        Iterator<Row> rows = sheet.rowIterator();
        Row header = null;
        LocalDateTime begin = java.time.LocalDateTime.now();
        StringBuilder report = new StringBuilder();
        report.append("Validation Failure Report for Server " + urlToTestServer + " compared to " + pathToIG + newLine + newLine);
        StringBuilder vsReport = new StringBuilder();
        vsReport.append("ValueSets report" + newLine + newLine);
        StringBuilder csReport = new StringBuilder();
        csReport.append("CodeSystems report" + newLine + newLine);

        while (rows.hasNext()) {
            Row row = rows.next();

            if (header == null && hasHeader) {
                header = row;
                continue;
            }
            try {
                String id = null;
                String valueSetURL = null;
                String version = null;
                String codeSystemURL = null;
                if (row.getCell(idCellNumber).getStringCellValue() != null) {
                    id = row.getCell(idCellNumber).getStringCellValue();
                }
                if (row.getCell(valueSetURLCellNumber).getStringCellValue() != null) {
                    valueSetURL = row.getCell(valueSetURLCellNumber).getStringCellValue();
                }
                if (row.getCell(versionCellNumber).getStringCellValue() != null) {
                    version = row.getCell(versionCellNumber).getStringCellValue();
                }
                if (row.getCell(codeSystemURLCellNumber).getStringCellValue() != null) {
                    codeSystemURL = row.getCell(codeSystemURLCellNumber).getStringCellValue();
                }
                validateRow(valueSetURL, codeSystemURL, fhirClient, vsReport, csReport, row.getRowNum());
            } catch (NullPointerException | ConfigurationException ex) {
                logger.debug(ex.getMessage());
                logger.debug(ex.toString());
                logger.debug(ex.getStackTrace().toString());
            }
        }
        report.append(vsReport + newLine);
        if (!vsNotPresentInIG.isEmpty()) {
            report.append(newLine);
            report.append("ValueSets not in IG:" + newLine);
            vsNotPresentInIG.forEach(vsName -> {
                report.append(vsName + newLine);
            });
        }
        report.append(csReport);
        if (!csNotPresentInIG.isEmpty()) {
            report.append(newLine);
            report.append("CodeSystems not in IG:" + newLine);
            csNotPresentInIG.forEach(csName -> {
                report.append(csName + newLine);
            });
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getOutputPath() + File.separator + "validationReport.txt"))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalDateTime end = java.time.LocalDateTime.now();
        System.out.println("Beginning Time: " + begin + "     End time: " + end);

    }

    private void validateRow(String valueSetURL, String codeSystemURL, FhirTerminologyClient fhirClient, StringBuilder vsReport, StringBuilder csReport, int rowNumber) {
        String vsServerUrl = urlToTestServer + "ValueSet/?url=" + valueSetURL;
        ValueSet vsToValidate = (ValueSet) fhirClient.getResource(vsServerUrl);
        ValueSet vsSourceOfTruth = vsMap.get(vsToValidate.getId().substring(vsToValidate.getId().lastIndexOf(File.separator) + 1));
        if (vsSourceOfTruth != null) {
            compareValueSets(vsToValidate, vsSourceOfTruth, vsReport);
            if (codeSystemURL == null || codeSystemURL.isEmpty()) {
                csReport.append("Row " + rowNumber + " does not contain a codeSystem;" + newLine);
            } else {
                if (codeSystemURL.contains(";")) {
                    String[] codeSystemURLs = codeSystemURL.split(";");
                    Arrays.stream(codeSystemURLs).forEach(singleCodeSystemURL -> {
                        processCodeSystemURL(singleCodeSystemURL, csReport);
                    });
                } else {
                    processCodeSystemURL(codeSystemURL, csReport);
                }
            }
        } else {
            vsNotPresentInIG.add(vsToValidate.getId().substring(vsToValidate.getId().lastIndexOf(File.separator) + 1));
        }
    }

    private void processCodeSystemURL(String codeSystemURL, StringBuilder report) {
        if (codeSystemNotInOutSideLocations(codeSystemURL)) {
            String csServerUrl = urlToTestServer + "CodeSystem/?url=" + codeSystemURL;
            CodeSystem csToValidate = (CodeSystem) fhirClient.getResource(csServerUrl);
            CodeSystem csSourceOfTruth = csMap.get(csToValidate.getId().substring(csToValidate.getId().lastIndexOf(File.separator) + 1));
            if (csSourceOfTruth == null || csSourceOfTruth.isEmpty()) {
                csNotPresentInIG.add(csToValidate.getName());
                return;
            }
            compareCodeSystems(csToValidate, csSourceOfTruth, report);
        }
    }

    private boolean codeSystemNotInOutSideLocations(String codeSystemURL) {
        if (codeSystemURL.toLowerCase().contains("snomed") ||
                codeSystemURL.toLowerCase().contains("rxnorm") ||
                codeSystemURL.toLowerCase().contains("unitsofmeasure") ||
                codeSystemURL.toLowerCase().contains("nucc")) {
            return false;
        }
        return true;
    }

    private void compareCodeSystems(CodeSystem terminologyServerCodeSystem, CodeSystem sourceOfTruthCodeSystem, StringBuilder report) {
        Set<String> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerCodeSystem.getUrl().equals(sourceOfTruthCodeSystem.getUrl())) {
            fieldsWithErrors.add("URL");
        }
        if (!terminologyServerCodeSystem.getVersion().equals(sourceOfTruthCodeSystem.getVersion())) {
            fieldsWithErrors.add("Version");
        }
        if (!terminologyServerCodeSystem.getStatus().equals(sourceOfTruthCodeSystem.getStatus())) {
            fieldsWithErrors.add("Status");
        }
        if (!terminologyServerCodeSystem.getExperimental() == sourceOfTruthCodeSystem.getExperimental()) {
            fieldsWithErrors.add("Experimental");
        }
        if (!terminologyServerCodeSystem.getName().equals(sourceOfTruthCodeSystem.getName())) {
            fieldsWithErrors.add("Name");
        }
        if (!terminologyServerCodeSystem.getTitle().equals(sourceOfTruthCodeSystem.getTitle())) {
            fieldsWithErrors.add("Title");
        }
        if (!terminologyServerCodeSystem.getPublisher().equals(sourceOfTruthCodeSystem.getPublisher())) {
            fieldsWithErrors.add("Publisher");
        }
        if (!terminologyServerCodeSystem.getContent().equals(sourceOfTruthCodeSystem.getContent())) {
            fieldsWithErrors.add("Content");
        }
        if (terminologyServerCodeSystem.getCount() != sourceOfTruthCodeSystem.getCount()) {
            fieldsWithErrors.add("Count");
        }
        if (!compareCodeSystemConcepts(terminologyServerCodeSystem.getConcept(), sourceOfTruthCodeSystem.getConcept())) {
            fieldsWithErrors.add("Concepts");
        }
        if (fieldsWithErrors.size() > 0) {
            addToReport(fieldsWithErrors, terminologyServerCodeSystem.getName(), report);
        }
    }

    private boolean compareCodeSystemConcepts(List<CodeSystem.ConceptDefinitionComponent> terminologyConcepts, List<CodeSystem.ConceptDefinitionComponent> truthConcepts) {
        AtomicBoolean conceptsMatch = new AtomicBoolean(true);
        if ((terminologyConcepts != null && truthConcepts != null) && (terminologyConcepts.size() == truthConcepts.size())) {
            Map<String, CodeSystem.ConceptDefinitionComponent> terminologyConceptsMap = createConceptMap(terminologyConcepts);
            Map<String, CodeSystem.ConceptDefinitionComponent> truthConceptsMap = createConceptMap(truthConcepts);
            terminologyConceptsMap.forEach((conceptCode, termConcept) -> {
                if (truthConceptsMap.containsKey(conceptCode) && conceptsMatch.get()) {
                    CodeSystem.ConceptDefinitionComponent truthConcept = truthConceptsMap.get(conceptCode);
                    if (termConcept != null && truthConcept != null) {
                        if(conceptsMatch.get()){conceptsMatch.set(compareStrings(termConcept.getCode(), truthConcept.getCode()));};
                        if(conceptsMatch.get()){conceptsMatch.set(compareStrings(termConcept.getDisplay(), truthConcept.getDisplay()));};
                        if(conceptsMatch.get()){conceptsMatch.set(compareStrings(termConcept.getDefinition(), truthConcept.getDefinition()));};
                    } else {
                        conceptsMatch.set(false);
                    }
                } else {
                    conceptsMatch.set(false);
                }
            });
        } else {
            conceptsMatch.set(false);
        }
        return conceptsMatch.get();
    }

    private boolean compareStrings(String terminologyString, String truthString){
        if((terminologyString != null && truthString != null) || (terminologyString == null && truthString == null)){
            if(terminologyString == null && truthString == null){return true;}
            if((terminologyString != null) && (terminologyString.equals(truthString))){
                return true;
            }
        }
        return false;
    }

    private Map<String, CodeSystem.ConceptDefinitionComponent> createConceptMap(List<CodeSystem.ConceptDefinitionComponent> concepts) {
        Map<String, CodeSystem.ConceptDefinitionComponent> conceptMap = new HashMap<>();
        concepts.forEach(concept -> {
            conceptMap.put(concept.getCode(), concept);
        });
        return conceptMap;
    }

    private void compareValueSets(ValueSet terminologyServerValueSet, ValueSet sourceOfTruthValueSet, StringBuilder report) {
        Set<String> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerValueSet.getUrl().equals(sourceOfTruthValueSet.getUrl())) {
            fieldsWithErrors.add("URL");
        }
        if (!terminologyServerValueSet.getVersion().equals(sourceOfTruthValueSet.getVersion())) {
            fieldsWithErrors.add("Version");
        }
        if (!terminologyServerValueSet.getStatus().equals(sourceOfTruthValueSet.getStatus())) {
            fieldsWithErrors.add("Status");
        }
        if (!terminologyServerValueSet.getExperimental() == sourceOfTruthValueSet.getExperimental()) {
            fieldsWithErrors.add("Experimental");
        }
        if (!terminologyServerValueSet.getName().equals(sourceOfTruthValueSet.getName())) {
            fieldsWithErrors.add("Name");
        }
        if (!terminologyServerValueSet.getTitle().equals(sourceOfTruthValueSet.getTitle())) {
            fieldsWithErrors.add("Title");
        }
        if (!terminologyServerValueSet.getPublisher().equals(sourceOfTruthValueSet.getPublisher())) {
            fieldsWithErrors.add("Publisher");
        }
        if (!terminologyServerValueSet.getStatus().equals(sourceOfTruthValueSet.getStatus())) {
            fieldsWithErrors.add("Status");
        }
        if (!compareComposes(terminologyServerValueSet.getCompose(), sourceOfTruthValueSet.getCompose())) {
            fieldsWithErrors.add("Compose");
        }
        if (fieldsWithErrors.size() > 0) {
            addToReport(fieldsWithErrors, terminologyServerValueSet.getName(), report);
        }
    }

    private void addToReport(Set<String> errorSet, String name, StringBuilder report) {
        report.append(name + " does not match the IG for the following fields:");
        Iterator<String> iterator = errorSet.iterator();
        while (iterator.hasNext()) {
            String fieldName = iterator.next();
            report.append(" " + fieldName);
            if (iterator.hasNext()) {
                report.append(", ");
            }
        }
        report.append(newLine);

    }

    private boolean compareComposes(ValueSet.ValueSetComposeComponent terminologyServerComposeComponent, ValueSet.ValueSetComposeComponent sourceOfTruthComposeComponent) {
        AtomicBoolean composesMatch = new AtomicBoolean(true);
        List<ValueSet.ConceptSetComponent> terminologyServerIncludes = terminologyServerComposeComponent.getInclude();
        Map<String, Object> terminologyServerIncludesMap = createIncludesMap(terminologyServerIncludes);
        List<ValueSet.ConceptSetComponent> sourceOfTruthIncludes = sourceOfTruthComposeComponent.getInclude();
        Map<String, Object> sourceOfTruthIncludesMap = createIncludesMap(sourceOfTruthIncludes);
        if (!terminologyServerIncludesMap.isEmpty() && !sourceOfTruthIncludesMap.isEmpty()) {
            if (terminologyServerIncludesMap.size() == sourceOfTruthIncludesMap.size()) {
                terminologyServerIncludesMap.forEach((terminologyIncludeKey, terminologyIncludeValue) -> {
                    if (sourceOfTruthIncludesMap.containsKey(terminologyIncludeKey)) {
                        terminologyServerIncludesMap.forEach((terminologyIncludesKey, terminologyIncludesValue) -> {
                            Map<?, ?> terminologyConceptsMap = (HashMap) (terminologyServerIncludesMap.get(terminologyIncludeKey));
                            Map<?, ?> truthConceptsMap = (HashMap) (sourceOfTruthIncludesMap.get(terminologyIncludeKey));
                            if (!terminologyConceptsMap.isEmpty() && !truthConceptsMap.isEmpty() &&
                                    terminologyConceptsMap.size() == truthConceptsMap.size()) {
                                terminologyConceptsMap.forEach((terminologyConceptsKey, terminologyConceptsValue) -> {
                                    if (truthConceptsMap.containsKey(terminologyConceptsKey)) {
                                        String truthConceptsValue = (String) (truthConceptsMap.get(terminologyConceptsKey));
                                        if (!truthConceptsValue.equalsIgnoreCase((String) terminologyConceptsValue)) {
                                            composesMatch.set(false);
                                        }

                                    }
                                });
                            }

                        });
                    }
                });
            }
        }
        return composesMatch.get();
    }

    private Map<String, Object> createIncludesMap(List<ValueSet.ConceptSetComponent> includes) {
        HashMap<String, Object> includesMap = new HashMap<>();
        includes.forEach(include -> {
            Map<String, String> conceptMap = new HashMap<>();
            List<ValueSet.ConceptReferenceComponent> concepts = include.getConcept();
            concepts.forEach(concept -> {
                conceptMap.put(concept.getCode(), concept.getDisplay());
            });
            includesMap.put(include.getSystem(), conceptMap);
        });
        return includesMap;
    }
}
