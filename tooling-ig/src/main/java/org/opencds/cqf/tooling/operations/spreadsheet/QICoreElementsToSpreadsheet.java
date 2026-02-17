package org.opencds.cqf.tooling.operations.spreadsheet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementVisitor;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetCreatorHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ModelCanonicalAtlasCreator;

@SuppressWarnings("checkstyle:MemberName")
@Operation(name = "QICoreElementsToSpreadsheet")
public class QICoreElementsToSpreadsheet extends SpreadsheetBase {

    private int ConstraintColumn = 7;
    private int ConstraintColumnWidth = 85 * 256;

    @Override
    public void execute() {
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
        linkStyle = SpreadsheetCreatorHelper.createLinkStyle(
                workBook, XSSFFont.U_SINGLE, HSSFColor.HSSFColorPredefined.BLUE.getIndex());

        AtomicInteger rowCount = new AtomicInteger(0);
        IntBinaryOperator ibo = (x, y) -> (x + y);
        XSSFRow currentRow = firstSheet.createRow(rowCount.getAndAccumulate(1, ibo));
        SpreadsheetCreatorHelper.createHeaderRow(workBook, createHeaderNameList(), currentRow);
        elementObjects.forEach((elementObject) -> {
            addElementObjectRowDataToCurrentSheet(
                    workBook, firstSheet, rowCount.getAndAccumulate(1, ibo), elementObject);
        });
        firstSheet.setColumnWidth(ConstraintColumn, ConstraintColumnWidth);
        SpreadsheetCreatorHelper.writeSpreadSheet(
                workBook, IOUtils.concatFilePath(outputPath, modelName + modelVersion + " Data Elements" + ".xlsx"));
    }

    private List<String> createHeaderNameList() {
        List<String> headerNameList = new ArrayList<String>() {
            {
                add("QI Core Profile");
                add("Id");
                add("Must Support Y/N");
                add("Review Notes");
                add("Cardinality");
                add("Type");
                add("Description");
                add("Constraints");
            }
        };
        return headerNameList;
    }

    private void addElementObjectRowDataToCurrentSheet(
            XSSFWorkbook workBook, XSSFSheet currentSheet, int rowCount, StructureDefinitionElementObject eo) {
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
        canonicalResourceAtlas =
                ModelCanonicalAtlasCreator.createMainCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);
        canonicalResourceDependenciesAtlas = ModelCanonicalAtlasCreator.createDependenciesCanonicalAtlas(
                resourcePaths, modelName, modelVersion, inputPath);

        if (null != canonicalResourceAtlas && null != canonicalResourceDependenciesAtlas) {
            StructureDefinitionElementVisitor sdbv =
                    new StructureDefinitionElementVisitor(canonicalResourceAtlas, canonicalResourceDependenciesAtlas);
            Map<String, StructureDefinitionElementObject> elementObjects =
                    sdbv.visitCanonicalAtlasStructureDefinitions(snapshotOnly);
            List<StructureDefinitionElementObject> elementObjectsList =
                    elementObjects.values().stream().collect(Collectors.toList());

            return elementObjectsList.stream()
                    .sorted(Comparator.comparing(StructureDefinitionElementObject::getSdName)
                            .thenComparing(StructureDefinitionElementObject::getElementId))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
