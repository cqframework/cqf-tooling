package org.opencds.cqf.tooling.operation;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionBindingObject;
import org.opencds.cqf.tooling.acceleratorkit.StructureDefinitionElementBindingVisitor;
import org.opencds.cqf.tooling.terminology.SpreadsheetCreatorHelper;
import org.opencds.cqf.tooling.utilities.ModelCanonicalAtlasCreator;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class ProfilesToSpreadsheet extends Operation {
    private String inputPath;
    private String resourcePaths;
    private String modelName;
    private String modelVersion;
    private boolean snapshotOnly = true;
    private CanonicalResourceAtlas canonicalResourceAtlas;
    private CanonicalResourceAtlas canonicalResourceDependenciesAtlas;

    private CreationHelper helper;
    private XSSFCellStyle linkStyle;

    public static final String separator = System.getProperty("file.separator");

    // example call: -ProfilesToSpreadsheet -ip=/Users/bryantaustin/Projects/FHIR-Spec -op=output -rp="4.0.1;US-Core/3.1.0;QI-Core/4.1.0" -sp=true -mn=QICore -mv=4.1.0
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

        List<StructureDefinitionBindingObject> bindingObjects;
        bindingObjects = getBindingObjects();
        if (null != bindingObjects && !bindingObjects.isEmpty()) {
            createOutput(bindingObjects);
        }
    }

    private void createOutput(List<StructureDefinitionBindingObject> bindingObjects) {
        XSSFWorkbook workBook = SpreadsheetCreatorHelper.createWorkbook();
        XSSFSheet firstSheet = workBook.createSheet(WorkbookUtil.createSafeSheetName("Profile Attribute List"));
        helper = workBook.getCreationHelper();
        linkStyle = SpreadsheetCreatorHelper.createLinkStyle(workBook, XSSFFont.U_SINGLE, HSSFColor.HSSFColorPredefined.BLUE.getIndex());

        AtomicInteger rowCount = new AtomicInteger(0);
        IntBinaryOperator ibo = (x, y) -> (x + y);
        XSSFRow currentRow = firstSheet.createRow(rowCount.getAndAccumulate(1, ibo));
        SpreadsheetCreatorHelper.createHeaderRow(createHeaderNameList(), currentRow);
        bindingObjects.forEach((bindingObject) -> {
            addBindingObjectRowDataToCurrentSheet(firstSheet, rowCount.getAndAccumulate(1, ibo), bindingObject);
        });
        SpreadsheetCreatorHelper.writeSpreadSheet(workBook, getOutputPath() + separator + modelName + modelVersion + ".xlsx");
    }

    private List<String> createHeaderNameList() {
        List<String> headerNameList = new ArrayList<String>() {{
            add("QI Core Profile");
            add("Id");
            add("Conformance");
            add("ValueSet");
            add("ValueSetURL");
            add("Version");
            add("Code System URLs");
            add("Must Support Y/N");
            add("Cardinality");
            add("Review Notes");
        }};
        return headerNameList;
    }

    private void addBindingObjectRowDataToCurrentSheet(XSSFSheet currentSheet, int rowCount, StructureDefinitionBindingObject bo) {
        XSSFRow currentRow = currentSheet.createRow(rowCount++);
        XSSFHyperlink link = (XSSFHyperlink)helper.createHyperlink(HyperlinkType.URL);
        int cellCount = 0;

        XSSFCell currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getSdName());
        link.setAddress(bo.getSdURL());
        currentCell.setHyperlink(link);
        currentCell.setCellStyle(linkStyle);

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getElementId());

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
        currentCell.setCellValue(bo.getCodeSystemsURLs());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getMustSupport());

        currentCell = currentRow.createCell(cellCount++);
        currentCell.setCellValue(bo.getCardinality());

        currentCell = currentRow.createCell(cellCount++);
        if ((null != bo.getBindingStrength() && bo.getBindingStrength().equalsIgnoreCase("required")) ||
                null != bo.getMustSupport() && bo.getMustSupport().equalsIgnoreCase("Y")) {
            currentCell.setCellValue("Needed");
        }
    }

    private List<StructureDefinitionBindingObject> getBindingObjects() {
        canonicalResourceAtlas = ModelCanonicalAtlasCreator.createMainCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);
        canonicalResourceDependenciesAtlas = ModelCanonicalAtlasCreator.createDependenciesCanonicalAtlas(resourcePaths, modelName, modelVersion, inputPath);

        if (null != canonicalResourceAtlas && null != canonicalResourceDependenciesAtlas) {
            StructureDefinitionElementBindingVisitor sdbv = new StructureDefinitionElementBindingVisitor(canonicalResourceAtlas, canonicalResourceDependenciesAtlas);
            Map<String, StructureDefinitionBindingObject> bindingObjects = sdbv.visitCanonicalAtlasStructureDefinitions(snapshotOnly);
            List<StructureDefinitionBindingObject> bindingObjectsList = bindingObjects
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            return bindingObjectsList
                    .stream()
                    .sorted(Comparator.comparing(StructureDefinitionBindingObject::getSdName)
                            .thenComparing(StructureDefinitionBindingObject::getElementId))
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
