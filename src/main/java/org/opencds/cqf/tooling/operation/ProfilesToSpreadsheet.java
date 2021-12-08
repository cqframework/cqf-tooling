package org.opencds.cqf.tooling.operation;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionBindingObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementBindingVisitor;
import org.opencds.cqf.tooling.terminology.SpreadsheetCreatorHelper;
import org.opencds.cqf.tooling.utilities.ModelCanonicalAtlasCreator;


import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class ProfilesToSpreadsheet extends Operation {
    private String inputPath;
    private String resourcePaths;
    private String modelName;
    private String modelVersion;
    private CanonicalResourceAtlas canonicalResourceAtlas;
    private CanonicalResourceAtlas canonicalResourceDependenciesAtlas;

    private CreationHelper helper;
    private XSSFCellStyle linkStyle;
    private XSSFFont linkFont;


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

        List<StructureDefinitionBindingObject> bindingObjects;
        bindingObjects = getBindingObjects();
        if (null != bindingObjects && !bindingObjects.isEmpty()) {
            createOutput(bindingObjects);
//                writeOutput();
            //write these puppies out to the spreadsheet
        }
    }

    private void createOutput(List<StructureDefinitionBindingObject> bindingObjects) {
        XSSFWorkbook workBook = SpreadsheetCreatorHelper.createWorkbook();
        XSSFSheet firstSheet = workBook.createSheet(WorkbookUtil.createSafeSheetName("Profile Attribute List"));
        helper = workBook.getCreationHelper();
        linkStyle = workBook.createCellStyle();
        linkFont = workBook.createFont();
        linkFont.setUnderline(XSSFFont.U_SINGLE);
        linkFont.setColor(HSSFColorPredefined.BLUE.getIndex());
        linkStyle.setFont(linkFont);

        AtomicInteger rowCount = new AtomicInteger(0);
        IntBinaryOperator ibo = (x, y) -> (x + y);
        XSSFRow currentRow = firstSheet.createRow(rowCount.getAndAccumulate(1, ibo));
        createHeaderRow(currentRow);
        bindingObjects.forEach((bindingObject) -> {
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
        XSSFHyperlink link = (XSSFHyperlink)helper.createHyperlink(HyperlinkType.URL);
        int cellCount = 0;

        XSSFCell currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getSdName());
        link.setAddress(bo.getSdURL());
        currentCell.setHyperlink(link);
        currentCell.setCellStyle(linkStyle);

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getElementPath());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingStrength());
        link = (XSSFHyperlink)helper.createHyperlink(HyperlinkType.URL);
        link.setAddress("http://hl7.org/fhir/R4/terminologies.html#" + bo.getBindingStrength());
        currentCell.setHyperlink(link);
        currentCell.setCellStyle(linkStyle);

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetName());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetURL());
        link = (XSSFHyperlink)helper.createHyperlink(HyperlinkType.URL);
        link.setAddress(bo.getBindingValueSetURL());
        currentCell.setHyperlink(link);
        currentCell.setCellStyle(linkStyle);

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getBindingValueSetVersion());
        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getMustSupport());
        currentCell = currentRow.createCell(cellCount++);
        if ((null != bo.getBindingStrength() && bo.getBindingStrength().equalsIgnoreCase("required")) ||
                null != bo.getMustSupport() && bo.getMustSupport().equalsIgnoreCase("Y")) {
            currentCell.setCellValue("Needed");
        }
    }

    private List<StructureDefinitionBindingObject> getBindingObjects() {
        CanonicalResourceAtlas canonicalResourceAtlas = ModelCanonicalAtlasCreator.createMainCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);
        CanonicalResourceAtlas canonicalResourceDependenciesAtlas = ModelCanonicalAtlasCreator.createDependenciesCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);

        if (null != canonicalResourceAtlas && null != canonicalResourceDependenciesAtlas) {
            StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas, canonicalResourceDependenciesAtlas);
            Map<String, StructureDefinitionBindingObject> bindingObjects = sdbv.visitCanonicalAtlasStructureDefinitions();
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

    private boolean isParameterListComplete() {
        if (null == inputPath || inputPath.length() < 1 ||
                null == modelName || modelName.length() < 1 ||
                null == modelVersion || modelName.length() < 1 ||
                null == resourcePaths || resourcePaths.length() < 1) {
            System.out.println("These parameters are required: ");
            System.out.println("-modelName/-mn");
            System.out.println("-modelVersion/-mv");
            System.out.println("-outputpath/-op");
            System.out.println("-resourcePaths/-rp");
            return false;
        }
        return true;
    }
}
