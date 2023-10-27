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
import java.nio.file.Paths;
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
    private String jarPath;  // -jarPath (-jp)  path to validator_cli.jar, including jar name
    private String pathToIG; // -pathToIG (-ptig) path to IG - files installed using "npm --registry https://packages.simplifier.net install hl7.fhir.us.qicore@4.1.1" (or other package)
    private String fhirVersion = "4.0.1";
    Map<String, CodeSystem> csMap;
    Map<String, ValueSet> vsMap;
    private static final String newLine = System.getProperty("line.separator");


    // The file name of the input spreadsheet
    private String spreadsheetName;

    private String getHeader(Row header, int columnIndex) {
        if (header != null) {
            return SpreadsheetHelper.getCellAsString(header, columnIndex).trim();
        } else {
            return CellReference.convertNumToColString(columnIndex);
        }
    }

    @Override
    public void execute(String[] args) {
        fhirContext = FhirContext.forR4Cached();
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default
        String userName = "";
        String password = "";

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
                case "jarPath":
                case "jp":
                    jarPath = Paths.get(value).toAbsolutePath().toString();
                    break; // -jarPath (-jp)
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
                case "fhirVersion":
                case "fv":
                    fhirVersion = value;
                    break; // -fhirversion (-fv)
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
//        Map<String, StructureDefinition> defMap = atlas.getStructureDefinitions();
        csMap = atlas.getCodeSystems();
        vsMap = atlas.getValueSets();

        spreadsheetName = new File(pathToSpreadsheet).getName();
        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        Sheet sheet = workbook.getSheetAt(firstSheet);
        // this might need to move yet again

        Iterator<Row> rows = sheet.rowIterator();
        Row header = null;
        LocalDateTime begin = java.time.LocalDateTime.now();
        StringBuilder report = new StringBuilder();
        report.append("Validation Failure Report for Server urlToTestServer compared to " + pathToIG + newLine + newLine);
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
//                Iterator<Cell> cells = row.cellIterator();
//                Cell cell = cells.next();
            try {
                String id = null;
                String valueSetURL = null;
                String version = null;
                String codeSystemURL = null;
                if (row.getCell(idCellNumber).getStringCellValue() != null) {
                    id = row.getCell(idCellNumber).getStringCellValue();
                }
//                String valueSetName = row.getCell(valueSetCellNumber).getStringCellValue();
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
        report.append(csReport);
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
        compareValueSets(vsToValidate, vsSourceOfTruth, vsReport);
        if(codeSystemURL == null || codeSystemURL.isEmpty()) {
            csReport.append("Row " + rowNumber + " does not contain a codeSystem;" + newLine);
        }else{
            if (codeSystemURL.contains(";")) {
                String[] codeSystemURLs = codeSystemURL.split(";");
                Arrays.stream(codeSystemURLs).forEach(singleCodeSystemURL -> {
                    processCodeSystemURL(singleCodeSystemURL, csReport);
                });
            }else{
                processCodeSystemURL(codeSystemURL, csReport);
            }
        }
    }

    private void processCodeSystemURL(String codeSystemURL, StringBuilder report){
        if(codeSystemNotInOutSideLocations(codeSystemURL)) {
            String csServerUrl = urlToTestServer + "CodeSystem/?url=" + codeSystemURL;
            CodeSystem csToValidate = (CodeSystem) fhirClient.getResource(csServerUrl);
            CodeSystem csSourceOfTruth = csMap.get(csToValidate.getId().substring(csToValidate.getId().lastIndexOf(File.separator) + 1));
            if (csSourceOfTruth == null || csSourceOfTruth.isEmpty()) {
                report.append(csToValidate.getName() + " is not present in the IG" + newLine);
                return;
            }
            compareCodeSystems(csToValidate, csSourceOfTruth, report);
        }
    }

    private boolean codeSystemNotInOutSideLocations(String codeSystemURL) {
        if(codeSystemURL.toLowerCase().contains("snomed") ||
                codeSystemURL.toLowerCase().contains("rxnorm") ||
                codeSystemURL.toLowerCase().contains("unitsofmeasure") ||
                codeSystemURL.toLowerCase().contains("nucc")) {
            return false;
        }
        return true;
    }

    private void compareCodeSystems(CodeSystem terminologyServerCodeSystem, CodeSystem sourceOfTruthCodeSystem, StringBuilder report) {
        /*
        url
        version
        name
        title
        status
        experimental
        publisher
        content
        count
        concept
         */
        int matchCount = 0;
        boolean urlsMatch = terminologyServerCodeSystem.getUrl().equals(sourceOfTruthCodeSystem.getUrl());
        boolean versionsMatch = terminologyServerCodeSystem.getVersion().equals(sourceOfTruthCodeSystem.getVersion());
        boolean statusMatch = terminologyServerCodeSystem.getStatus().equals(sourceOfTruthCodeSystem.getStatus());
        boolean experimentalMatch = terminologyServerCodeSystem.getExperimental() == sourceOfTruthCodeSystem.getExperimental();
        boolean namesMatch = terminologyServerCodeSystem.getName().equals(sourceOfTruthCodeSystem.getName());
        boolean titlesMatch = terminologyServerCodeSystem.getTitle().equals(sourceOfTruthCodeSystem.getTitle());
        boolean publishersMatch = terminologyServerCodeSystem.getPublisher().equals(sourceOfTruthCodeSystem.getPublisher());
        boolean contentsMatch = terminologyServerCodeSystem.getContent().equals(sourceOfTruthCodeSystem.getContent());
        boolean countsMatch = terminologyServerCodeSystem.getCount() == sourceOfTruthCodeSystem.getCount();
        boolean conceptsMatch = compareConcepts(terminologyServerCodeSystem.getConcept(), sourceOfTruthCodeSystem.getConcept());
        if (!urlsMatch || !versionsMatch || !statusMatch || !experimentalMatch || !namesMatch || !titlesMatch || !publishersMatch ||
                !contentsMatch || !countsMatch || !conceptsMatch) {
            String nonMatchingElements = "";
            if (!urlsMatch) {
                nonMatchingElements = "URL";
                matchCount++;
            }
            if (!versionsMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Version";
                matchCount++;
            }
            if (!statusMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Status";
                matchCount++;
            }
            if (!experimentalMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Experimental";
                matchCount++;
            }
            if (!namesMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Name";
                matchCount++;
            }
            if (!titlesMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Title";
                matchCount++;
            }
            if (!publishersMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Publisher";
                matchCount++;
            }
            if (!contentsMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Content";
                matchCount++;
            }
            if (!countsMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Count";
                matchCount++;
            }
            if (!conceptsMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Compose";
                matchCount++;
            }
            report.append(terminologyServerCodeSystem.getName() + " does not match the IG for the following fields: " + nonMatchingElements + newLine);
        }
    }

    private boolean compareConcepts(List<CodeSystem.ConceptDefinitionComponent> terminologyConcept, List<CodeSystem.ConceptDefinitionComponent> truthConcept1) {
        return true;
    }

    private void compareValueSets(ValueSet terminologyServerValueSet, ValueSet sourceOfTruthValueSet, StringBuilder report) {
        /*
        url
        version
        status
        experimental
        name
        title
        publisher
        contact
        compose
         */
        int matchCount = 0;
        boolean urlsMatch = terminologyServerValueSet.getUrl().equals(sourceOfTruthValueSet.getUrl());
        boolean versionsMatch = terminologyServerValueSet.getVersion().equals(sourceOfTruthValueSet.getVersion());
        boolean statusMatch = terminologyServerValueSet.getStatus().equals(sourceOfTruthValueSet.getStatus());
        boolean experimentalMatch = terminologyServerValueSet.getExperimental() == sourceOfTruthValueSet.getExperimental();
        boolean namesMatch = terminologyServerValueSet.getName().equals(sourceOfTruthValueSet.getName());
        boolean titlesMatch = terminologyServerValueSet.getTitle().equals(sourceOfTruthValueSet.getTitle());
        boolean publishersMatch = terminologyServerValueSet.getPublisher().equals(sourceOfTruthValueSet.getPublisher());
        boolean composesMatch = compareComposes(terminologyServerValueSet.getCompose(), sourceOfTruthValueSet.getCompose());
        if (!urlsMatch || !versionsMatch || !statusMatch || !experimentalMatch || !namesMatch || !titlesMatch || !publishersMatch || !composesMatch) {
            String nonMatchingElements = "";
            if (!urlsMatch) {
                nonMatchingElements = "URL";
                matchCount++;
            }
            if (!versionsMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Version";
                matchCount++;
            }
            if (!statusMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Status";
                matchCount++;
            }
            if (!experimentalMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Experimental";
                matchCount++;
            }
            if (!namesMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Name";
                matchCount++;
            }
            if (!titlesMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Title";
                matchCount++;
            }
            if (!publishersMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Publisher";
                matchCount++;
            }
            if (!composesMatch) {
                if (matchCount > 0) {
                    nonMatchingElements = ", ";
                }
                nonMatchingElements = "Compose";
                matchCount++;
            }
            report.append(terminologyServerValueSet.getName() + " does not match the IG for the following fields: " + nonMatchingElements + newLine);
        }
    }

    private boolean compareComposes(ValueSet.ValueSetComposeComponent terminologyServerComposeComponent, ValueSet.ValueSetComposeComponent sourceOfTruthComposeComponent) {
        AtomicBoolean composesMatch = new AtomicBoolean(true);
        List<ValueSet.ConceptSetComponent> terminologyServerIncludes = terminologyServerComposeComponent.getInclude();
        Map<?, ?> terminologyServerIncludesMap = createIncludesMap(terminologyServerIncludes);
        List<ValueSet.ConceptSetComponent> sourceOfTruthIncludes = sourceOfTruthComposeComponent.getInclude();
        Map<?, ?> sourceOfTruthIncludesMap = createIncludesMap(terminologyServerIncludes);
        if (!terminologyServerIncludesMap.isEmpty() && !sourceOfTruthIncludesMap.isEmpty()) {
            if (terminologyServerIncludesMap.size() == sourceOfTruthIncludesMap.size()) {
                terminologyServerIncludesMap.forEach((terminologyIncludeKey, terminologyIncludeValue) -> {
                    if (sourceOfTruthIncludesMap.containsKey(terminologyIncludeKey)) {
                        terminologyServerIncludesMap.forEach((terminologyIncludesKey, terminologyIncludesValue) -> {
                            HashMap<?, ?> terminologyConceptsMap = (HashMap) (terminologyServerIncludesMap.get(terminologyIncludeKey));
                            HashMap<?, ?> truthConceptsMap = (HashMap) (sourceOfTruthIncludesMap.get(terminologyIncludeKey));
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
    // for each include
    //      loop through comparing system
    //      loop through each concept
    //          comparing code and display


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

/*
    private void getVSFromTerminologyServer() {
        Bundle readBundle = this.client.search().byUrl(url).returnBundle(Bundle.class).execute();
        if (readBundle.hasEntry()) {
            ValueSet vsToValidate = (ValueSet) readBundle.getEntry().get(0).getResource();
            String vsURL = null;

        }
    }
*/

    /*
    From NpmPackageManager
     */
/*
    private JsonObject fetchJson(String source) throws IOException {
        URL url = new URL(source + "?nocache=" + System.currentTimeMillis());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setInstanceFollowRedirects(true);
        return JsonTrackingParser.parseJson(c.getInputStream());
    }
*/
}
