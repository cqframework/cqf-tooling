package org.opencds.cqf.tooling.acceleratorkit;

import java.io.*;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.security.Provider;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;

import ca.uhn.fhir.context.FhirContext;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;

public class TestCaseProcessor {

    private Workbook workbook;
    private Sheet sheet;
    private Sheet map;
    private Map<String, String> headerMap;

    public TestCaseProcessor() {
    }

    private CanonicalResourceAtlas atlas;
    public CanonicalResourceAtlas getAtlas() {
        return atlas;
    }
    public TestCaseProcessor setAtlas(CanonicalResourceAtlas atlas) {
        this.atlas = atlas;
        return this;
    }

    private Map<String, StructureDefinition> profilesByElementId;
    public Map<String, StructureDefinition> getProfilesByElementId() {
        return profilesByElementId;
    }
    public TestCaseProcessor setProfilesByElementId(Map<String, StructureDefinition> profilesByElementId) {
        this.profilesByElementId = profilesByElementId;
        return this;
    }

    public Map<String, List<Resource>> process(String testCaseInput) {
        this.workbook = SpreadsheetHelper.getWorkbook(testCaseInput);

        // TODO: When finished add input parameter for target sheet.
        this.sheet = this.workbook.getSheet("TestCases");
        this.map = this.workbook.getSheet("Map");

        // Load header map (test case column headers to data element ids)
        this.headerMap = loadMap();

        Iterator<Row> rIterator = sheet.rowIterator();

        Map<String, List<Resource>> results = new HashMap<String, List<Resource>>();

        while (rIterator.hasNext()) {
            // Get current row
            Row currentRow = rIterator.next();

            // Skip the header row
            if (currentRow.getRowNum() == 0) {
                continue;
            }

            if (isRowEmpty(currentRow))
                continue;

            Map<String, Object> testCaseValues = loadTestCaseValues(currentRow);

            Map<StructureDefinition, Map<String, Object>> testCaseProfiles = indexTestCaseValuesByProfile(testCaseValues);

            ExampleBuilder eb = new ExampleBuilder();
            eb.setAtlas(atlas);
            // TODO: Location context?
            eb.setLocationContext("anc-location-example");
            // TODO: Practitioner/PractitionerRole context?
            eb.setPractitionerContext("anc-practitioner-example");
            eb.setPractitionerRoleContext("anc-practitionerrole-example");

            Patient p = generatePatient(eb, testCaseProfiles);
            if (p != null) {
                eb.setIdScope(p.getId());
                eb.setPatientContext(p.getId());

                Encounter e = generateEncounter(eb, testCaseProfiles);
                if (e != null) {
                    eb.setEncounterContext(e.getId());

                    List<Resource> resources = generateResources(eb, testCaseProfiles);
                    resources.add(0, p);
                    resources.add(1, e);
                    results.put(p.getId(), resources);
                }
            }
        }

        return results;
    }

    private Patient generatePatient(ExampleBuilder eb, Map<StructureDefinition, Map<String, Object>> testCaseProfiles) {
        for (Map.Entry<StructureDefinition, Map<String, Object>> e : testCaseProfiles.entrySet()) {
            if (e.getKey().getType().equals("Patient")) {
                Patient p = (Patient)generateResource(eb, e.getKey(), e.getValue());
                if (p != null) {
                    Object identifierValue = e.getValue().get("ANC.A.DE1");
                    if (identifierValue != null && identifierValue instanceof String) {
                        p.setId((String)identifierValue);
                    }
                    return p;
                }
            }
        }
        return null;
    }

    private Encounter generateEncounter(ExampleBuilder eb, Map<StructureDefinition, Map<String, Object>> testCaseProfiles) {
        for (Map.Entry<StructureDefinition, Map<String, Object>> e : testCaseProfiles.entrySet()) {
            if (e.getKey().getType().equals("Encounter")) {
                return (Encounter)generateResource(eb, e.getKey(), e.getValue());
            }
        }
        return null;
    }

    private List<Resource> generateResources(ExampleBuilder eb, Map<StructureDefinition, Map<String, Object>> testCaseProfiles) {
        List<Resource> resources = new ArrayList<Resource>();

        for (Map.Entry<StructureDefinition, Map<String, Object>> e : testCaseProfiles.entrySet()) {
            if (e.getKey().getType().equals("Patient")) {
                continue;
            }

            if (e.getKey().getType().equals("Encounter")) {
                continue;
            }

            Resource r = generateResource(eb, e.getKey(), e.getValue());
            if (r != null) {
                resources.add(r);
            }
        }

        return resources;
    }

    // Returns the element id for the element definition with a mapping to the given data element id
    private String getElementId(StructureDefinition sd, String dataElementId) {
        for (ElementDefinition ed : sd.getDifferential().getElement()) {
            if (ed.hasMapping()) {
                for (ElementDefinition.ElementDefinitionMappingComponent m : ed.getMapping()) {
                    if (dataElementId.equals(m.getMap())) {
                        return ed.getId();
                    }
                }
            }
        }
        return null;
    }

    private Resource generateResource(ExampleBuilder eb, StructureDefinition sd, Map<String, Object> testCaseValues) {
        Map<String, Object> elementValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> e : testCaseValues.entrySet()) {
            String elementId = getElementId(sd, e.getKey());
            if (elementId != null) {
                elementValues.put(elementId, e.getValue());
            }
            else {
                // TODO: Error? Warning? This shouldn't ever happen
            }
        }
        return eb.build(sd, elementValues);
    }

    private Map<StructureDefinition, Map<String, Object>> indexTestCaseValuesByProfile(Map<String, Object> testCaseValues) {
        Map<StructureDefinition, Map<String, Object>> result = new HashMap<StructureDefinition, Map<String, Object>>();
        for (Map.Entry<String, Object> e : testCaseValues.entrySet()) {
            String dataElementId = e.getKey();
            StructureDefinition sd = profilesByElementId.get(dataElementId);
            if (sd != null) {
                Map<String, Object> profileTestValues = result.get(sd);
                if (profileTestValues == null) {
                    profileTestValues = new HashMap<String, Object>();
                    result.put(sd, profileTestValues);
                }

                profileTestValues.put(e.getKey(), e.getValue());
            }
        }

        return result;
    }

    private Map<String, String> loadMap() {
        Map<String, String> result = new HashMap<String, String>();
        Iterator<Row> rIterator = map.rowIterator();
        while (rIterator.hasNext()) {
            Row currentRow = rIterator.next();

            if (currentRow.getRowNum() == 0) {
                continue;
            }

            Cell headerCell = currentRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell elementCell = currentRow.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING
                    && elementCell != null && elementCell.getCellType() == CellType.STRING) {
                String headerCellStr = headerCell.getStringCellValue();
                String elementCellStr = elementCell.getStringCellValue();
                if (headerCellStr != null && !headerCellStr.isEmpty() && elementCellStr != null && !elementCellStr.isEmpty()) {
                    result.put(headerCellStr, elementCellStr);
                }
            }
        }
        return result;
    }

    private Map<String, Object> loadTestCaseValues(Row row) {
        // loadTestCaseValues
        Iterator<Cell> cIterator = row.cellIterator();
        Row headerRow = this.sheet.getRow(0);

        // Test case values by data element
        Map<String, Object> testCaseValues = new HashMap<String, Object>();

        while (cIterator.hasNext()) {
            Cell currentCell = cIterator.next();

            if (currentCell == null || currentCell.getCellType() == CellType.BLANK) {
                continue;
            }

            int cellPosition = currentCell.getColumnIndex();
            Cell respectiveHeaderCell = headerRow.getCell(cellPosition);

            String headerCellStr;
            headerCellStr = respectiveHeaderCell.getStringCellValue();

            // Look up the data element based on the mapping
            String dataElementId = headerMap.get(headerCellStr);
            if (dataElementId == null || dataElementId.isEmpty()) {
                continue;
            }

            Object cellValue = null;
            switch (currentCell.getCellType()) {
                case STRING: cellValue = currentCell.getStringCellValue(); break;
                case BOOLEAN: cellValue = currentCell.getBooleanCellValue(); break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(currentCell)) {
                        // Good hell Java's date libraries suck....
                        LocalDateTime dateTime = currentCell.getLocalDateTimeCellValue();
                        LocalDate date = dateTime.toLocalDate();
                        cellValue = date.format(DateTimeFormatter.ISO_DATE);

                        // This will give an appropriately formatted ISO8601 offset datetime, but the Instant.parse method can't deal with that format
                        // No idea why instants can't deal with offsets, but there it is.
                        // Current tests only have date values, so I'm going to just assume date values
                        // TODO: Support date/time values in future (will need an indicator on the data element to distinguish source values of date, datetime, and time)
                        //LocalDateTime dateTime = currentCell.getLocalDateTimeCellValue();
                        //OffsetDateTime offsetDateTime = dateTime.atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
                        //cellValue = offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                    else {
                        cellValue = new BigDecimal(currentCell.getNumericCellValue());
                    }
                break;
            }

            // Skip if there are no contents for the element
            if (cellValue == null) {
                continue;
            }

            testCaseValues.put(dataElementId, cellValue);
        }

        return testCaseValues;
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    /**
     * Outputs switch statement based on spreadsheet header values.
     * @param row Header row.
     * @param outputPath Path to the file in which the output will be written.
     */
    private void generateSwitchStatement(Row row, String outputPath) {
        // This is either only useful to me in this instance, or actually pretty useful.
        // If it is useful, it should probably be moved to a helper class of some kind.
        Iterator<Cell> cIterator = row.cellIterator();
        String statement = "switch (yourCondition) {\n";
        ArrayList<String> alreadyAdded = new ArrayList<String>();

        while (cIterator.hasNext()) {
            Cell cell = cIterator.next();
            if (cell== null || cell.getCellType() == CellType.BLANK) {
                continue;
            }

            String cellStr = cell.getStringCellValue();
            if (!cellStr.contains("{") && !cellStr.contains("}") || alreadyAdded.contains(cellStr)) {
                continue;
            }

            statement += String.format(
                    "\tcase \"%s\":\n\t\tbreak;\n",
                    cellStr
            );

            alreadyAdded.add(cellStr);
        }

        statement += "}";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputPath));
            out.write(statement);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}