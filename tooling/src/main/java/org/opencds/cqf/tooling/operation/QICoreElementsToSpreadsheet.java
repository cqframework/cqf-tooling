package org.opencds.cqf.tooling.operation;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementVisitor;
import org.opencds.cqf.tooling.terminology.SpreadsheetCreatorHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ModelCanonicalAtlasCreator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class QICoreElementsToSpreadsheet extends StructureDefinitionToSpreadsheetBase {

    private int ConstraintColumn = 7;
    private int ConstraintColumnWidth = 85 * 256;

    // example call: -QICoreElementsToSpreadsheet -ip=/Users/bryantaustin/Projects/FHIR-Spec -op=output -rp="4.0.1;US-Core/3.1.0;QI-Core/4.1.0" -sp=true -mn=QICore -mv=4.1.0
    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-QICoreElementsToSpreadsheet"))
                continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "inputpath":                   // path to spec files directory
                case "ip":
                    inputPath = value;
                    break;
                case "outputpath":                  // directory to save output to
                case "op":                          // the name of the file is the modelName + modelVersion + .xslx
                    setOutputPath(value);
                    break;
                case "resourcepaths":               // path to the individual specs and versions to use
                case "rp":                          // see org.opencds.cqf.tooling.modelinfo.StructureDefinitionToModelInfo comments for usage
                    resourcePaths = value;
                    break;
                case "modelName":                   // name of the model to parse
                case "mn":
                    modelName = value;
                    break;
                case "modelVersion":                // version of the model to parse
                case "mv":
                    modelVersion = value;
                    break;
                case "snapshotOnly":                // flag to determine if the differential should be traversed: false == traverse the differential
                case "sp":
                    snapshotOnly = value.equalsIgnoreCase("true");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
        if (!isParameterListComplete()) {
            return;
        }

        List<StructureDefinitionElementObject> elementObjects;
        elementObjects = getElementObjects();
        if (null != elementObjects && !elementObjects.isEmpty()) {
            createOutput(elementObjects);
        }
    }

    private void createOutput(List<StructureDefinitionElementObject> elementObjects) {
        XSSFWorkbook workBook = SpreadsheetCreatorHelper.createWorkbook();
        XSSFSheet firstSheet = workBook.createSheet(WorkbookUtil.createSafeSheetName("Profile Attribute List"));
        helper = workBook.getCreationHelper();
        linkStyle = SpreadsheetCreatorHelper.createLinkStyle(workBook, XSSFFont.U_SINGLE, HSSFColor.HSSFColorPredefined.BLUE.getIndex());

        AtomicInteger rowCount = new AtomicInteger(0);
        IntBinaryOperator ibo = (x, y) -> (x + y);
        XSSFRow currentRow = firstSheet.createRow(rowCount.getAndAccumulate(1, ibo));
        SpreadsheetCreatorHelper.createHeaderRow(workBook, createHeaderNameList(), currentRow);
        elementObjects.forEach((elementObject) -> {
            addElementObjectRowDataToCurrentSheet(workBook, firstSheet, rowCount.getAndAccumulate(1, ibo), elementObject);
        });
        firstSheet.setColumnWidth(ConstraintColumn, ConstraintColumnWidth);
//        firstSheet.autoSizeColumn(ConstraintColumn);
        SpreadsheetCreatorHelper.writeSpreadSheet(workBook,
                IOUtils.concatFilePath(getOutputPath(), modelName + modelVersion + " Data Elements" + ".xlsx"));
    }

    private List<String> createHeaderNameList() {
        List<String> headerNameList = new ArrayList<String>() {{
            add("QI Core Profile");
            add("Id");
            add("Must Support Y/N");
            add("Review Notes");
            add("Cardinality");
            add("Type");
            add("Description");
            add("Constraints");
        }};
        return headerNameList;
    }

    private void addElementObjectRowDataToCurrentSheet(XSSFWorkbook workBook, XSSFSheet currentSheet, int rowCount, StructureDefinitionElementObject eo) {
        XSSFRow currentRow = currentSheet.createRow(rowCount++);
        XSSFHyperlink link = (XSSFHyperlink) helper.createHyperlink(HyperlinkType.URL);
        int cellCount = 0;

        XSSFCell currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getSdName());
        link.setAddress(eo.getSdURL());
        currentCell.setHyperlink(link);
        currentCell.setCellStyle(linkStyle);

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getElementId());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getMustSupport());

        currentCell = currentRow.createCell(cellCount++);
        if (null != eo.getMustSupport() && eo.getMustSupport().equalsIgnoreCase("Y")) {
            currentCell.setCellValue("Needed");
        }

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getCardinality());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getElementType());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getElementDescription());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(eo.getConstraint());
        CellStyle cs = workBook.createCellStyle();
        cs.setWrapText(true);
        currentCell.setCellStyle(cs);
    }

    private List<StructureDefinitionElementObject> getElementObjects() {
        canonicalResourceAtlas = ModelCanonicalAtlasCreator.createMainCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);
        canonicalResourceDependenciesAtlas = ModelCanonicalAtlasCreator.createDependenciesCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);

        if (null != canonicalResourceAtlas && null != canonicalResourceDependenciesAtlas) {
            StructureDefinitionElementVisitor sdbv = new StructureDefinitionElementVisitor(canonicalResourceAtlas, canonicalResourceDependenciesAtlas);
            Map<String, StructureDefinitionElementObject> elementObjects = sdbv.visitCanonicalAtlasStructureDefinitions(snapshotOnly);
            List<StructureDefinitionElementObject> elementObjectsList = elementObjects
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            return elementObjectsList
                    .stream()
                    .sorted(Comparator.comparing(StructureDefinitionElementObject::getSdName)
                            .thenComparing(StructureDefinitionElementObject::getElementId))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
