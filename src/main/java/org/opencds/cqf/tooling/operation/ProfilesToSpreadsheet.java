package org.opencds.cqf.tooling.operation;

import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.InMemoryCanonicalResourceProvider;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionBindingObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementBindingVisitor;
import org.opencds.cqf.tooling.modelinfo.Atlas;
import org.opencds.cqf.tooling.terminology.SpreadsheetCreatorHelper;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class ProfilesToSpreadsheet extends Operation {
    private String inputPath;
    private String resourcePaths;
    private String modelName;
    private String modelVersion;
    private CanonicalResourceAtlas canonicalResourceAtlas;

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-ProfilesToSpreadsheet"))
                continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "inputpath":
                case "ip":
                    inputPath = value;
                    break;
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "resourcepaths":
                case "rp":
                    resourcePaths = value;
                    break;
                case "modelName":
                case "mn":
                    modelName = value;
                    break;
                case "modelVersion":
                case "mv":
                    modelVersion = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        if (!isParameterListComplete()) {
            return;
        }

        List <StructureDefinitionBindingObject> bindingObjects;
        bindingObjects = getBindingObjects();
        if (null != bindingObjects && !bindingObjects.isEmpty()) {
            createOutput(bindingObjects);
//                writeOutput();
            //write these puppies out to the spreadsheet
        }
    }

    private void createOutput(List <StructureDefinitionBindingObject> bindingObjects) {
        XSSFWorkbook workBook = SpreadsheetCreatorHelper.createWorkbook();
        XSSFSheet firstSheet = workBook.createSheet(WorkbookUtil.createSafeSheetName("Profile Attribute List"));
        AtomicInteger rowCount = new AtomicInteger(0);
        IntBinaryOperator ibo = (x, y)->(x + y);
        XSSFRow currentRow = firstSheet.createRow(rowCount.getAndAccumulate(1, ibo));
        createHeaderRow(currentRow);
        bindingObjects.forEach((bindingObject)->{
            addRowDataToCurrentSheet(firstSheet, rowCount.getAndAccumulate(1, ibo), bindingObject);
        });
        writeSpreadSheet(workBook);
    }

    private void writeSpreadSheet(XSSFWorkbook workBook) {
        File outputFile = new File(getOutputPath() + ".xlsx");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createHeaderRow(XSSFRow currentRow) {
        int cellCount = 0;
        XSSFCell currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("QI Core Profile");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Path");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Conformance");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("ValueSet");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("ValueSetURL");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Version");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Must Support Y/N");
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Review Notes");
    }

    private void addRowDataToCurrentSheet(XSSFSheet currentSheet, int rowCount, StructureDefinitionBindingObject bo) {
        XSSFRow currentRow = currentSheet.createRow(rowCount++);
        int cellCount = 0;
        XSSFCell currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getSdName());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getElementPath());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingStrength());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetName());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetURL());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetVersion());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getMustSupport());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue("Needed");
    }

    private List <StructureDefinitionBindingObject> getBindingObjects() {
        canonicalResourceAtlas = createAtlas();
        if (null != canonicalResourceAtlas) {

            Map <String, StructureDefinitionBindingObject> bindingObjects = new HashMap<>();
            StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas);
            Iterable<StructureDefinition> structureDefinitions = canonicalResourceAtlas.getStructureDefinitions().get();
            try {
                structureDefinitions.forEach((structDefn) -> {
                    StructureDefinition sd = structDefn;
                    Map <String, StructureDefinitionBindingObject> newBindingObjects = sdbv.visitStructureDefinition(sd);
                    if (null != newBindingObjects) {
                        bindingObjects.putAll(newBindingObjects);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            List<StructureDefinitionBindingObject> bindingObjectsList = bindingObjects
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            return bindingObjectsList
                    .stream()
                    .sorted(Comparator.comparing(StructureDefinitionBindingObject::getSdName)
                            .thenComparing(StructureDefinitionBindingObject::getElementPath))
                    .collect(Collectors.toList());
        }
        return null;
    }


    /**
     *    private List<StructureDefinitionBindingObject> createSortedBindingList(Map<String, StructureDefinitionBindingObject> bindingObjects) {
     *         List<StructureDefinitionBindingObject> bindingObjectsList = new ArrayList<>(bindingObjects.values());
     *
     *         List<StructureDefinitionBindingObject> sortedBindingObjectsList = bindingObjectsList.stream()
     * //                .map(x -> (x))
     *                 .sorted(Comparator.comparing(StructureDefinitionBindingObject::getElementPath))
     *                 .collect(Collectors.toList());
     *         return sortedBindingObjectsList;
     *     }
     * @return
     */

    private CanonicalResourceAtlas createAtlas() {
        List<ValueSet> valueSets = new ArrayList<>();
        List<CodeSystem> codeSystems = new ArrayList<>();
        List<StructureDefinition> structureDefinitions = new ArrayList<>();
        Map<String, ConceptMap> conceptMaps;

        Atlas atlas = new Atlas();
        atlas.loadPaths(inputPath, resourcePaths);
        List<StructureDefinition> finalStructureDefinitions = structureDefinitions;
        atlas.getStructureDefinitions().forEach((key, structureDefinition) -> {
            finalStructureDefinitions.add(structureDefinition);
        });

        List<CodeSystem> finalCodeSystems = codeSystems;
        atlas.getCodeSystems().forEach((key, codeSystem) -> {
            finalCodeSystems.add(codeSystem);
        });
        conceptMaps = atlas.getConceptMaps();
        List<ValueSet> finalValueSets = valueSets;
        atlas.getValueSets().forEach((key, valueSet) -> {
            finalValueSets.add(valueSet);
        });
        return new CanonicalResourceAtlas()
                .setStructureDefinitions(new InMemoryCanonicalResourceProvider<StructureDefinition>(finalStructureDefinitions))
                .setValueSets(new InMemoryCanonicalResourceProvider<>(finalValueSets))
                .setConceptMaps(new InMemoryCanonicalResourceProvider<ConceptMap>(conceptMaps.values()))
                .setCodeSystems(new InMemoryCanonicalResourceProvider<>(finalCodeSystems));
    }

    private boolean isParameterListComplete() {
        if (null == inputPath || inputPath.length() < 1 ||
//                null == modelName || modelName.length() < 1 ||
//                null == modelVersion || modelName.length() < 1
                null == resourcePaths || resourcePaths.length() < 1) {
            System.out.println("These parameters are required: ");
//            System.out.println("-modelName/-mn");
//            System.out.println("-modelVersion/-mv");
            System.out.println("-outputpath/-op");
            System.out.println("-resourcePaths/-rp");
            return false;
        }
        return true;
    }
}
