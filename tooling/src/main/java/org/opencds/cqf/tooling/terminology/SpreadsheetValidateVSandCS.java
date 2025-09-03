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
import org.opencds.cqf.tooling.terminology.compatators.CodeSystemComparator;
import org.opencds.cqf.tooling.terminology.compatators.ValuesetComparator;
import org.opencds.cqf.tooling.terminology.fhirservice.FhirTerminologyClient;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/*
Parameters as CLI arguments:
        -SpreadsheetValidateVSandCS             This operation
        -pts=/Users/myname/hold/QICore.xlsx     The path and name of the spreadsheet to test
        -hh=true                                boolean for if the sheet contains a header row
        -uts=https://uat-cts.nlm.nih.gov/fhir/  The url of the server to test
        -un=<test server username>              The test server usernam
        -pw=<test server password>              The test server password
        -op=/Users/myname/hold/                 The output path to put the result text file
        -ptig=/Users/myname/Projects/FHIR-Spec  The path to the base of the resource path
        -rp=4.0.1;US-Core/3.1.1;QI-Core/4.1.1;THO/3.1.0     The resourcepath - which versions and which IGs to use to validate against
                                                            Known here internally as the Source of Truth
This class takes a QICore spreadsheet as one of the arguments, and parses it.
    For each row, it takes the ValueSet and CodeSystem URLs, grabs them from the test server, and compares them to the ones in the "Source of Truth".
    A final report (validationReport.txt) is written to the output path.
 */
public class SpreadsheetValidateVSandCS extends Operation {

    private static final Logger logger = LoggerFactory.getLogger(SpreadsheetValidateVSandCS.class);

    private FhirContext fhirContext;
    FhirTerminologyClient fhirClient = null;
    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String urlToTestServer; // -urltotestserver (-uts)  server to validate
    private boolean hasHeader = true; // -hasheader (-hh)
    private String pathToIG; // -pathToIG (-ptig) path to IG - files installed using "npm --registry https://packages.simplifier.net install hl7.fhir.us.qicore@4.1.1" (or other package)
    private String resourcePaths; // -resourcePaths (-rp)
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

    @Override
    public void execute(String[] args) {
        fhirContext = FhirContext.forR4Cached();
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default
        resourcePaths = "4.0.1;US-Core/3.1.1;QI-Core/4.1.1;THO/3.1.0"; // default

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
                case "resourcepaths":
                case "rp":
                    resourcePaths = value;
                    break; // -resourcePaths (-rp)
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
                validateRow(valueSetURL, version, codeSystemURL, fhirClient, row.getRowNum() + 1); //add one row number due to header row
            } catch (NullPointerException | ConfigurationException ex) {
                logger.debug(ex.getMessage());
                logger.debug(ex.toString());
            }
        }
        LocalDateTime end = java.time.LocalDateTime.now();
        System.out.println("Beginning Time: " + begin + "     End time: " + end);

    }

    private void validateRow(String valueSetURL, String version, String codeSystemURL, FhirTerminologyClient fhirClient, int rowNumber) {
        String vsServerUrl = urlToTestServer + "ValueSet/?url=" + valueSetURL;
        ValueSet vsToValidate = (ValueSet) fhirClient.getResource(vsServerUrl);
        if (vsToValidate != null) {
            ValueSet vsSourceOfTruth = vsMap.get(vsToValidate.getId().substring(vsToValidate.getId().lastIndexOf(File.separator) + 1));
            if (vsSourceOfTruth != null) {
                ValuesetComparator vsCompare = new ValuesetComparator();
                vsCompare.compareValueSets(vsToValidate, vsSourceOfTruth, vsFailureReport);
            } else {
                vsNotPresentInIG.add(vsToValidate.getUrl() + "|" + vsToValidate.getVersion());
            }
        } else {
            vsNotInTestServer.add(vsServerUrl.substring(vsServerUrl.lastIndexOf("url=") + 4) + "|" + version);
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
        if (codeSystemNotReachable(codeSystemURL)) {
            String csServerUrl = urlToTestServer + "CodeSystem/?url=" + codeSystemURL;
            CodeSystem csToValidate = (CodeSystem) fhirClient.getResource(csServerUrl);
            if (csToValidate != null) {
                CodeSystem csSourceOfTruth = csMap.get(CanonicalUtils.getTail(csToValidate.getUrl()));
                if (csSourceOfTruth == null || csSourceOfTruth.isEmpty()) {
                    csNotPresentInIG.add(csToValidate.getName());
                    return;
                }
                CodeSystemComparator csCompare = new CodeSystemComparator();
                csCompare.compareCodeSystems(csToValidate, csSourceOfTruth, csFailureReport);
            } else {
                csNotInTestServer.add(codeSystemURL);
            }
        }
    }

    private boolean codeSystemNotReachable(String codeSystemURL) {
        if (codeSystemURL.toLowerCase().contains("snomed") ||
                codeSystemURL.toLowerCase().contains("rxnorm") ||
                codeSystemURL.toLowerCase().contains("unitsofmeasure") ||
                codeSystemURL.toLowerCase().contains("loinc") ||
                codeSystemURL.toLowerCase().contains("nucc")) {
            return false;
        }
        return true;
    }

    private void reportResults() {
        StringBuilder report = new StringBuilder();
        report.append("Validation Failure Report for Server " + urlToTestServer + " compared to " + pathToIG + newLine);
        report.append("\tUsing resource paths of " + resourcePaths + newLine + newLine);
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
                    if (((Map<?, ?>) fieldWithErrors).get("Concept") != null) {
                        newReport.append("\t\t\t" + "Concept:" + newLine);
                        ((Map<?, ?>) fieldWithErrors).forEach((field, reason) -> {
                            if (!(field.equals("Concept"))) {
                                newReport.append("\t\t\t\t" + field + ": " + newLine);
                                newReport.append("\t\t\t\t\t" + reason);
                            }
                        });

                    } else {
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
