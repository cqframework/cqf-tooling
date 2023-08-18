package org.opencds.cqf.tooling.cli;

import org.opencds.cqf.tooling.casereporting.transformer.ErsdTransformer;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.exception.OperationNotFound;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.TemplateToValueSetGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.DTProcessor;
import org.opencds.cqf.tooling.acceleratorkit.Processor;
import org.opencds.cqf.tooling.library.r4.LibraryGenerator;
import org.opencds.cqf.tooling.measure.r4.RefreshR4MeasureOperation;
import org.opencds.cqf.tooling.measure.stu3.RefreshStu3MeasureOperation;
import org.opencds.cqf.tooling.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.tooling.operation.BundleResources;
import org.opencds.cqf.tooling.operation.BundleToResources;
import org.opencds.cqf.tooling.operation.ExecuteMeasureTestOperation;
import org.opencds.cqf.tooling.operation.ExtractMatBundleOperation;
import org.opencds.cqf.tooling.operation.GenerateCQLFromDroolOperation;
import org.opencds.cqf.tooling.operation.IgBundler;
import org.opencds.cqf.tooling.operation.PostBundlesInDirOperation;
import org.opencds.cqf.tooling.operation.PostmanCollectionOperation;
import org.opencds.cqf.tooling.operation.StripGeneratedContentOperation;
import org.opencds.cqf.tooling.operation.ProfilesToSpreadsheet;
import org.opencds.cqf.tooling.operation.QICoreElementsToSpreadsheet;
import org.opencds.cqf.tooling.operation.RefreshIGOperation;
import org.opencds.cqf.tooling.operation.RefreshLibraryOperation;
import org.opencds.cqf.tooling.operation.ScaffoldOperation;
import org.opencds.cqf.tooling.operation.TestIGOperation;
import org.opencds.cqf.tooling.operation.VmrToFhirOperation;
import org.opencds.cqf.tooling.qdm.QdmToQiCore;
import org.opencds.cqf.tooling.quick.QuickPageGenerator;
import org.opencds.cqf.tooling.terminology.CMSFlatMultiValueSetGenerator;
import org.opencds.cqf.tooling.terminology.EnsureExecutableValueSetOperation;
import org.opencds.cqf.tooling.terminology.GenericValueSetGenerator;
import org.opencds.cqf.tooling.terminology.HEDISValueSetGenerator;
import org.opencds.cqf.tooling.terminology.RCKMSJurisdictionsGenerator;
import org.opencds.cqf.tooling.terminology.SpreadsheetToCQLOperation;
import org.opencds.cqf.tooling.terminology.ToJsonValueSetDbOperation;
import org.opencds.cqf.tooling.terminology.VSACBatchValueSetGenerator;
import org.opencds.cqf.tooling.terminology.VSACValueSetGenerator;
import org.opencds.cqf.tooling.terminology.distributable.DistributableValueSetGenerator;
import org.opencds.cqf.tooling.utilities.OperationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


class OperationFactory {
    private static final Logger logger = LoggerFactory.getLogger(OperationFactory.class);
    private static String operationName;
    private static Map<String, String> paramMap;
    private static boolean showHelpMenu = false;

    private OperationFactory() {

    }

    private static void processArgs(String[] args) {
        paramMap = new HashMap<>();
        for (int i = 1; i < args.length; ++i) {
            if (OperationUtils.isHelpArg(args[i])) {
                showHelpMenu = true;
                return;
            }
            String[] argAndValue = args[i].split("=", 2);
            if (argAndValue.length == 2) {
                paramMap.put(argAndValue[0].replace("-", ""), argAndValue[1]);
            }
            else {
                throw new InvalidOperationArgs(String.format(
                        "Invalid argument: %s found for operation: %s", args[i], operationName));
            }
        }
    }

    private static ExecutableOperation initialize(ExecutableOperation operation)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Field field : operation.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OperationParam.class)) {
                boolean isInitialized = false;
                for (String alias : field.getAnnotation(OperationParam.class).alias()) {
                    if (paramMap.containsKey(alias)) {
                        Class<?> paramType = OperationUtils.getParamType(operation,
                                field.getAnnotation(OperationParam.class).setter());
                        operation.getClass().getDeclaredMethod(
                                field.getAnnotation(OperationParam.class).setter(), paramType
                        ).invoke(operation, OperationUtils.mapParamType(paramMap.get(alias), paramType));
                        isInitialized = true;
                    }
                }
                if (!isInitialized) {
                    if (field.getAnnotation(OperationParam.class).required()) {
                        throw new InvalidOperationArgs("Missing required argument: " + field.getName());
                    }
                    else if (!field.getAnnotation(OperationParam.class).defaultValue().isEmpty()) {
                        Class<?> paramType = OperationUtils.getParamType(operation,
                                field.getAnnotation(OperationParam.class).setter());
                        operation.getClass().getDeclaredMethod(
                                field.getAnnotation(OperationParam.class).setter(), paramType
                        ).invoke(operation, OperationUtils.mapParamType(
                                field.getAnnotation(OperationParam.class).defaultValue(), paramType));
                    }
                }
            }
        }
        return operation;
    }

    static ExecutableOperation createOperation(String operationName, Class<?> operationClass, String[] args)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (operationClass == null) {
            throw new OperationNotFound("Unable to resolve operation: " + operationName);
        }
        OperationFactory.operationName = operationName;
        processArgs(args);
        if (showHelpMenu) {
            logger.info(OperationUtils.getHelpMenu(
                    (ExecutableOperation) operationClass.getDeclaredConstructor().newInstance()));
            showHelpMenu = false;
            return null;
        }
        return initialize((ExecutableOperation) operationClass.getDeclaredConstructor().newInstance());
    }

    static Operation createOperation(String operationName) {
        switch (operationName) {
            case "QdmToQiCore":
                return new QdmToQiCore();
            case "QiCoreQUICK":
                return new QuickPageGenerator();
            case "VsacXlsxToValueSet":
                return new VSACValueSetGenerator();
            case "DistributableXlsxToValueSet":
                return new DistributableValueSetGenerator();
            case "VsacMultiXlsxToValueSet":
                return new CMSFlatMultiValueSetGenerator();
            case "VsacXlsxToValueSetBatch":
                return new VSACBatchValueSetGenerator();
            case "HedisXlsxToValueSet":
                return new HEDISValueSetGenerator();
            case "XlsxToValueSet":
                return new GenericValueSetGenerator();
            case "TemplateToValueSetGenerator":
                 return new TemplateToValueSetGenerator();
            case "EnsureExecutableValueSet":
            case "EnsureComputableValueSet":
                return new EnsureExecutableValueSetOperation();
            case "ToJsonValueSetDb":
                return new ToJsonValueSetDbOperation();
            case "CqlToSTU3Library":
                return new org.opencds.cqf.tooling.library.stu3.LibraryGenerator();
            case "CqlToR4Library":
                return new LibraryGenerator();
            case "UpdateSTU3Cql":
                return new org.opencds.cqf.tooling.library.stu3.LibraryGenerator();
            case "UpdateR4Cql":
                return new LibraryGenerator();
            case "JsonSchemaGenerator":
//                return new SchemaGenerator();
            case "BundleIg":
                return new IgBundler();
//            case "PackageIG":
//                return new PackageOperation();
            case "GenerateCQLFromDrool":
                return new GenerateCQLFromDroolOperation();
            case "VmrToFhir":
                return new VmrToFhirOperation();
            case "RefreshIG":
                return new RefreshIGOperation();
            case "RefreshLibrary":
                return new RefreshLibraryOperation();
            case "RefreshStu3Measure":
                return new RefreshStu3MeasureOperation();
            case "RefreshR4Measure":
                return new RefreshR4MeasureOperation();
            case "ScaffoldIG":
                return new ScaffoldOperation();
            case "TestIG":
                return new TestIGOperation();
            case "CqlToMeasure":
                throw new NotImplementedException("CqlToMeasure");
            case "BundlesToBundle":
                throw new NotImplementedException("BundlesToBundle");
            case "BundleToResources":
                return new BundleToResources();
            case "ExtractMatBundle":
            	return new ExtractMatBundleOperation();
            case "GenerateMIs":
                return new StructureDefinitionToModelInfo();
            case "ProcessAcceleratorKit":
                return new Processor();
            case "ProcessDecisionTables":
                return new DTProcessor();
            case "BundleResources":
                return new BundleResources();
            case "PostBundlesInDir":
                return new PostBundlesInDirOperation();
            case "JurisdictionsXlsxToCodeSystem":
                return new RCKMSJurisdictionsGenerator();
            case "ExecuteMeasureTest":
                return new ExecuteMeasureTestOperation();
            case "SpreadsheetToCQL":
                return new SpreadsheetToCQLOperation();
            case "PostmanCollection":
                return new PostmanCollectionOperation();
            case "TransformErsd":
                return new ErsdTransformer();
            case "RollTestsDataDates":
                return new DataDateRollerOperation();
            case "ProfilesToSpreadsheet":
                return new ProfilesToSpreadsheet();
            case "QICoreElementsToSpreadsheet":
                return new QICoreElementsToSpreadsheet();
            case "StripGeneratedContent":
                return new StripGeneratedContentOperation();
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
