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

    String systemName;
    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetValidateVSandCS.class);

    private FhirContext fhirContext;
    FhirTerminologyClient fhirClient = null;
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String urlToTestServer; // -urltotestserver (-uts)  server to validate
    private boolean hasHeader = true; // -hasheader (-hh)
    private String pathToIG; // -pathToIG (-ptig) path to IG - files installed using "npm --registry https://packages.simplifier.net install hl7.fhir.us.qicore@4.1.1" (or other package)
    private Map<String, CodeSystem> csMap;
    private Map<String, ValueSet> vsMap;
    private static final String newLine = System.getProperty("line.separator");
    private Set<String> csNotPresentInIG;
    private Set<String> vsNotPresentInIG;
    private Set<String> vsNotInTestServer;
    private Set<String> csNotInTestServer;
    private Set<String> spreadSheetErrors;
    private Map<String, Object> vsFailureReport = new HashMap<>();
    private Map<String, Object> csFailureReport = new HashMap<>();

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
        vsNotInTestServer = new HashSet<>();
        spreadSheetErrors = new HashSet<>();

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
        reportResults();
        System.out.println("Finished with the validation.");
    }

    private void validateSpreadsheet(String userName, String password) {
        int firstSheet = 0;
        int idCellNumber = 1;
        int valueSetCellNumber = 3;
        int valueSetURLCellNumber = 4;
        int versionCellNumber = 5;
        int codeSystemURLCellNumber = 6;

        String resourcePaths = "4.0.1;US-Core/3.1.0;QI-Core/4.1.1;THO/3.1.0";
        Atlas atlas = new Atlas();
        atlas.loadPaths(pathToIG, resourcePaths);
        csMap = atlas.getCodeSystems();
        vsMap = atlas.getValueSets();

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        Sheet sheet = workbook.getSheetAt(firstSheet);

        Iterator<Row> rows = sheet.rowIterator();
        Row header = null;
        LocalDateTime begin = java.time.LocalDateTime.now();

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
                if (row.getCell(idCellNumber) != null) {
                    id = row.getCell(idCellNumber).getStringCellValue();
                }
                if (row.getCell(valueSetURLCellNumber) != null) {
                    valueSetURL = row.getCell(valueSetURLCellNumber).getStringCellValue();
                }
                if (row.getCell(versionCellNumber) != null) {
                    version = row.getCell(versionCellNumber).getStringCellValue();
                }
                if (row.getCell(codeSystemURLCellNumber) != null) {
                    codeSystemURL = row.getCell(codeSystemURLCellNumber).getStringCellValue();
                }
                validateRow(valueSetURL, codeSystemURL, fhirClient, row.getRowNum() + 1); //add one row number due to header row
            } catch (NullPointerException | ConfigurationException ex) {
                logger.debug(ex.getMessage());
                logger.debug(ex.toString());
                logger.debug(ex.getStackTrace().toString());
            }
        }

        LocalDateTime end = java.time.LocalDateTime.now();
        System.out.println("Beginning Time: " + begin + "     End time: " + end);

    }

    private void validateRow(String valueSetURL, String codeSystemURL, FhirTerminologyClient fhirClient, int rowNumber) {
        String vsServerUrl = urlToTestServer + "ValueSet/?url=" + valueSetURL;
        ValueSet vsToValidate = (ValueSet) fhirClient.getResource(vsServerUrl);
        if (vsToValidate != null) {
            ValueSet vsSourceOfTruth = vsMap.get(vsToValidate.getId().substring(vsToValidate.getId().lastIndexOf(File.separator) + 1));
            if (vsSourceOfTruth != null) {
                compareValueSets(vsToValidate, vsSourceOfTruth);
            } else {
                vsNotPresentInIG.add(vsToValidate.getId().substring(vsToValidate.getId().lastIndexOf(File.separator) + 1));
            }
        } else {
            vsNotInTestServer.add(vsServerUrl.substring(vsServerUrl.lastIndexOf("/") + 1));

        }
        if (codeSystemURL == null || codeSystemURL.isEmpty()) {
            spreadSheetErrors.add("Row " + rowNumber + " does not contain a codeSystem");
        } else {
            if (codeSystemURL.contains(";")) {
                String[] codeSystemURLs = codeSystemURL.split(";");
                Arrays.stream(codeSystemURLs).forEach(singleCodeSystemURL -> {
                    processCodeSystemURL(singleCodeSystemURL);
                });
            } else {
                processCodeSystemURL(codeSystemURL);
            }
        }
    }

    private void processCodeSystemURL(String codeSystemURL) {
        if (codeSystemNotInOutSideLocations(codeSystemURL)) {
            String csServerUrl = urlToTestServer + "CodeSystem/?url=" + codeSystemURL;
            CodeSystem csToValidate = (CodeSystem) fhirClient.getResource(csServerUrl);
            if (csToValidate != null) {
                CodeSystem csSourceOfTruth = csMap.get(csToValidate.getId().substring(csToValidate.getId().lastIndexOf(File.separator) + 1));
                if (csSourceOfTruth == null || csSourceOfTruth.isEmpty()) {
                    csNotPresentInIG.add(csToValidate.getName());
                    return;
                }
                compareCodeSystems(csToValidate, csSourceOfTruth);

            } else {
                csNotInTestServer.add(codeSystemURL);
            }
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

    private void compareCodeSystems(CodeSystem terminologyServerCodeSystem, CodeSystem sourceOfTruthCodeSystem) {
        Set<Map<String, String>> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerCodeSystem.getUrl().equals(sourceOfTruthCodeSystem.getUrl())) {
            Map<String, String> urlFailure = new HashMap<>();
            urlFailure.put("URL", "\"" + terminologyServerCodeSystem.getUrl() + "\" Does not equal IG URL \"" + sourceOfTruthCodeSystem.getUrl() + "\"" + newLine);
            fieldsWithErrors.add(urlFailure);
        }
        if (!terminologyServerCodeSystem.getVersion().equals(sourceOfTruthCodeSystem.getVersion())) {
            Map<String, String> versionFailure = new HashMap<>();
            versionFailure.put("Version", "\"" + terminologyServerCodeSystem.getVersion() + "\" Does not equal IG Version \"" + sourceOfTruthCodeSystem.getVersion() + "\"" + newLine);
            fieldsWithErrors.add(versionFailure);
        }
        if (!terminologyServerCodeSystem.getStatus().equals(sourceOfTruthCodeSystem.getStatus())) {
            Map<String, String> statusFailure = new HashMap<>();
            statusFailure.put("Status", "\"" + terminologyServerCodeSystem.getStatus() + "\" Does not equal IG Status \"" + sourceOfTruthCodeSystem.getStatus() + "\"" + newLine);
            fieldsWithErrors.add(statusFailure);
        }
        if (!terminologyServerCodeSystem.getExperimental() == sourceOfTruthCodeSystem.getExperimental()) {
            Map<String, String> experimentalFailure = new HashMap<>();
            experimentalFailure.put("Experimental", "\"" + terminologyServerCodeSystem.getExperimental() + "\" Does not equal IG Experimental \"" + sourceOfTruthCodeSystem.getExperimental() + "\"" + newLine);
            fieldsWithErrors.add(experimentalFailure);
        }
        if (!terminologyServerCodeSystem.getName().equals(sourceOfTruthCodeSystem.getName())) {
            Map<String, String> nameFailure = new HashMap<>();
            nameFailure.put("Name", "\"" + terminologyServerCodeSystem.getName() + "\" Does not equal IG Name \"" + sourceOfTruthCodeSystem.getName() + "\"" + newLine);
            fieldsWithErrors.add(nameFailure);
        }
        if (!terminologyServerCodeSystem.getTitle().equals(sourceOfTruthCodeSystem.getTitle())) {
            Map<String, String> titleFailure = new HashMap<>();
            titleFailure.put("Title", "\"" + terminologyServerCodeSystem.getTitle() + "\" Does not equal IG Title \"" + sourceOfTruthCodeSystem.getTitle() + "\"" + newLine);
            fieldsWithErrors.add(titleFailure);
        }
        if (!terminologyServerCodeSystem.getPublisher().equals(sourceOfTruthCodeSystem.getPublisher())) {
            Map<String, String> publisherFailure = new HashMap<>();
            publisherFailure.put("Publisher", "\"" + terminologyServerCodeSystem.getPublisher() + "\" Does not equal IG Publisher \"" + sourceOfTruthCodeSystem.getPublisher() + "\"" + newLine);
            fieldsWithErrors.add(publisherFailure);
        }
        if (!terminologyServerCodeSystem.getContent().equals(sourceOfTruthCodeSystem.getContent())) {
            Map<String, String> contentFailure = new HashMap<>();
            contentFailure.put("Content", "\"" + terminologyServerCodeSystem.getContent() + "\" Does not equal IG Content \"" + sourceOfTruthCodeSystem.getContent() + "\"" + newLine);
            fieldsWithErrors.add(contentFailure);
        }
        if (terminologyServerCodeSystem.getCount() != sourceOfTruthCodeSystem.getCount()) {
            Map<String, String> countFailure = new HashMap<>();
            countFailure.put("Count", "\"" + terminologyServerCodeSystem.getCount() + "\" Does not equal IG Count \"" + sourceOfTruthCodeSystem.getCount() + "\"" + newLine);
            fieldsWithErrors.add(countFailure);
        }
        Map<String, String> conceptErrors = new HashMap<>();
        if (!compareCodeSystemConcepts(terminologyServerCodeSystem.getConcept(), sourceOfTruthCodeSystem.getConcept(), conceptErrors)) {
            fieldsWithErrors.add(conceptErrors);
        }
        if (!fieldsWithErrors.isEmpty()) {
            csFailureReport.put(terminologyServerCodeSystem.getName(), fieldsWithErrors);
        }
    }

    private boolean compareCodeSystemConcepts(List<CodeSystem.ConceptDefinitionComponent> terminologyConcepts, List<CodeSystem.ConceptDefinitionComponent> truthConcepts, Map<String, String> conceptErrors) {
        AtomicBoolean conceptsMatch = new AtomicBoolean(true);
        if ((terminologyConcepts != null && truthConcepts != null) && (terminologyConcepts.size() == truthConcepts.size())) {
            Map<String, CodeSystem.ConceptDefinitionComponent> terminologyConceptsMap = createConceptMap(terminologyConcepts);
            Map<String, CodeSystem.ConceptDefinitionComponent> truthConceptsMap = createConceptMap(truthConcepts);
            conceptErrors.put("Concept", "");
            terminologyConceptsMap.forEach((conceptCode, termConcept) -> {
                boolean falseFound = false;
                if (truthConceptsMap.containsKey(conceptCode) && conceptsMatch.get()) {
                    CodeSystem.ConceptDefinitionComponent truthConcept = truthConceptsMap.get(conceptCode);
                    if (termConcept != null && truthConcept != null) {
                        if(!compareStrings(termConcept.getCode().trim(), truthConcept.getCode().trim())){
                            falseFound = true;
                            conceptErrors.put("Code:", "\t \"" + termConcept.getCode() + "\" does not match the IG code \""  + truthConcept.getCode() + "\"" + newLine);
                        }
                        if(!compareStrings(termConcept.getDisplay().trim(), truthConcept.getDisplay().trim())){
                            falseFound = true;
                            conceptErrors.put("Display:", "\t \"" + termConcept.getDisplay() + "\" does not match the IG display \""  + truthConcept.getDisplay() + "\""  + newLine);
                        }
                        if(!compareStrings(termConcept.getDefinition().trim(), truthConcept.getDefinition().trim())){
                            falseFound = true;
                            conceptErrors.put("Definition", "\t \"" + termConcept.getDefinition() + "\" does not match the IG definition \""  + truthConcept.getDefinition() + "\""  + newLine);
                        }
                        if(falseFound){
                            conceptsMatch.set(false);
                        }
                    } else {
                        conceptsMatch.set(false);
                        if (termConcept == null) {
                            conceptErrors.put("Concepts Null", " concept is null and IG concept is not null." + newLine);
                        } else {
                            conceptErrors.put("Concepts Null", " concept is not null and IG concept is null." + newLine);
                        }

                    }
                    if(termConcept.getConcept() != null){
                        conceptsMatch.set(compareCodeSystemConcepts(termConcept.getConcept(), truthConcept.getConcept(), conceptErrors));
                    }
                } else {
                    conceptsMatch.set(false);
                    conceptErrors.put("Code", "\tThe concept code \"" + conceptCode + "\" from the terminology server does not match the concept code \"" + truthConceptsMap.get(conceptCode) + "\" from the IG." + newLine);
                }
            });
        } else {
            conceptsMatch.set(false);
            if (terminologyConcepts == null) {
                conceptErrors.put("Concepts", "\tThe terminology concept is not present, but the IG contains one." + newLine);
            }
            if (truthConcepts == null) {
                conceptErrors.put("Concepts", "\tThe terminology concept is present, but the IG does not contains one." + newLine);
            }
            if (terminologyConcepts.size() != truthConcepts.size()) {
                conceptErrors.put("Concepts", "\tThe terminology concept and the IG concept sizes do not match ." + newLine);
            }
        }
        return conceptsMatch.get();
    }

    private boolean compareStrings(String terminologyString, String truthString) {
        if ((terminologyString != null && truthString != null) || (terminologyString == null && truthString == null)) {
            if (terminologyString == null && truthString == null) {
                return true;
            }
            if ((terminologyString != null) && (terminologyString.equals(truthString))) {
                return true;
            } else {
                return false;
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

    private void compareValueSets(ValueSet terminologyServerValueSet, ValueSet sourceOfTruthValueSet) {

        Set<Map<String, String>> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerValueSet.getUrl()
                .equals(sourceOfTruthValueSet.getUrl())) {
            Map<String, String> urlFailure = new HashMap<>();
            urlFailure.put("URL", terminologyServerValueSet.getUrl() + " Does not equal IG URL " + sourceOfTruthValueSet.getUrl() + newLine);
            fieldsWithErrors.add(urlFailure);
        }
        if (!terminologyServerValueSet.getVersion().equals(sourceOfTruthValueSet.getVersion())) {
            Map<String, String> versionFailure = new HashMap<>();
            versionFailure.put("Version", "\"" + terminologyServerValueSet.getVersion() + "\" Does not equal IG Version \"" + sourceOfTruthValueSet.getVersion() + "\"" + newLine);
            fieldsWithErrors.add(versionFailure);
        }
        if (!terminologyServerValueSet.getStatus().equals(sourceOfTruthValueSet.getStatus())) {
            Map<String, String> statusFailure = new HashMap<>();
            statusFailure.put("Status", "\"" + terminologyServerValueSet.getStatus() + "\" Does not equal IG Status \"" + sourceOfTruthValueSet.getStatus() + "\"" + newLine);
            fieldsWithErrors.add(statusFailure);
        }
        if (!terminologyServerValueSet.getExperimental() == sourceOfTruthValueSet.getExperimental()) {
            Map<String, String> experimentalFailure = new HashMap<>();
            experimentalFailure.put("Experimental", "\"" + terminologyServerValueSet.getExperimental() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getExperimental() + "\"" + newLine);
            fieldsWithErrors.add(experimentalFailure);
        }
        if (!terminologyServerValueSet.getName().equals(sourceOfTruthValueSet.getName())) {
            Map<String, String> nameFailure = new HashMap<>();
            nameFailure.put("Name", "\"" + terminologyServerValueSet.getName() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getName() + "\"" + newLine);
            fieldsWithErrors.add(nameFailure);
        }
        if (!terminologyServerValueSet.getTitle().equals(sourceOfTruthValueSet.getTitle())) {
            Map<String, String> titleFailure = new HashMap<>();
            titleFailure.put("Status", "\"" + terminologyServerValueSet.getTitle() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getTitle() + "\"" + newLine);
            fieldsWithErrors.add(titleFailure);
        }
        if (!terminologyServerValueSet.getPublisher().equals(sourceOfTruthValueSet.getPublisher())) {
            Map<String, String> publisherFailure = new HashMap<>();
            publisherFailure.put("Publisher", "\"" + terminologyServerValueSet.getPublisher() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getPublisher() + "\"" + newLine);
            fieldsWithErrors.add(publisherFailure);
        }
/*        TODO: add contact verification method
        if (!terminologyServerValueSet.getContact().equals(sourceOfTruthValueSet.getContact())) {
            Map<String, String> publisherFailure = new HashMap<>();
            publisherFailure.put("Publisher", terminologyServerValueSet.getPublisher() + " Does not equal IG Experimental " + sourceOfTruthValueSet.getPublisher());
            fieldsWithErrors.add(publisherFailure);
            fieldsWithErrors.add("Status");
        }
 */
        if (!compareComposes(terminologyServerValueSet.getCompose(), sourceOfTruthValueSet.getCompose())) {
            //TODO: fix up compose comparison
//            fieldsWithErrors.add("Compose");
        }
        if (!fieldsWithErrors.isEmpty()) {
            vsFailureReport.put(terminologyServerValueSet.getName(), fieldsWithErrors);
        }
    }

/*
    private void addToReport(Set<String> errorSet, String name) {
        report.append("\t" + name + " does not match the IG for the following fields:");
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
*/

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

    private void reportResults() {
        StringBuilder report = new StringBuilder();
        report.append("Validation Failure Report for Server " + urlToTestServer + " compared to " + pathToIG + newLine + newLine);
        handleFailureReport(report, "valueset");
        if (!vsNotInTestServer.isEmpty()) {
            report.append(newLine);
            report.append("\tValueSets not found in Test Server:" + newLine);
            vsNotInTestServer.forEach(vsName -> {
                report.append("\t\t" + vsName + newLine);
            });
        }
        if (!vsNotPresentInIG.isEmpty()) {
            report.append(newLine);
            report.append("\tValueSets not in IG:" + newLine);
            vsNotPresentInIG.forEach(vsName -> {
                report.append("\t\t" + vsName + newLine);
            });
        }

        handleFailureReport(report, "codesystem");
        if (!csNotPresentInIG.isEmpty()) {
            report.append(newLine);
            report.append("\tCodeSystems not in IG:" + newLine);
            csNotPresentInIG.forEach(csName -> {
                report.append("\t\t" + csName + newLine);
            });
        }
        if (!spreadSheetErrors.isEmpty()) {
            report.append(newLine + newLine);
            report.append("SpreadSheet Errors:" + newLine);
            spreadSheetErrors.forEach(sheetError -> {
                report.append("\t\t" + sheetError + newLine);
            });
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getOutputPath() + File.separator + "validationReport.txt"))) {
            writer.write(report.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFailureReport(StringBuilder report, String whichReport) {
        String headerText = "";
        Map<String, Object> failureReport = new HashMap<>();
        StringBuilder newReport = new StringBuilder();
        if (whichReport.equalsIgnoreCase("valueset")) {
            headerText = "\tValueSet Failures" + newLine;
            failureReport = vsFailureReport;
        } else if (whichReport.equalsIgnoreCase("codesystem")) {
            headerText = newLine + "\tCodeSystem Failures" + newLine;
            failureReport = csFailureReport;
        }
        newReport.append(headerText);
        if (!failureReport.isEmpty()) {
            failureReport.forEach((setName, failureSet) -> {
                newReport.append("\t\t" + setName + ":" + newLine);
                ((Set<?>) failureSet).forEach((fieldWithErrors) -> {
                    if(((Map<?, ?>)fieldWithErrors).get("Concept") != null){
                        newReport.append("\t\t\t" + "Concept:" + newLine);
                        ((Map<?, ?>) fieldWithErrors).forEach((field, reason) -> {
                            if(!(field.equals("Concept"))) {
                                newReport.append("\t\t\t\t" + field + ": " + newLine);
                                newReport.append("\t\t\t\t\t" + reason);
                            }
                        });

                    }else {
                        ((Map<?, ?>) fieldWithErrors).forEach((field, reason) -> {
                            newReport.append("\t\t\t" + field + ": " + newLine);
                            newReport.append("\t\t\t\t" + reason);
                        });
                    }
                });

            });
            report.append(newReport);
        }
    }
}
