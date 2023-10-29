package org.opencds.cqf.tooling.cli;

//import org.opencds.cqf.tooling.jsonschema.SchemaGenerator;
import org.opencds.cqf.tooling.casereporting.transformer.ErsdTransformer;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.operation.*;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.TemplateToValueSetGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.DTProcessor;
import org.opencds.cqf.tooling.acceleratorkit.Processor;
import org.opencds.cqf.tooling.measure.r4.RefreshR4MeasureOperation;
import org.opencds.cqf.tooling.measure.stu3.RefreshStu3MeasureOperation;
import org.opencds.cqf.tooling.modelinfo.StructureDefinitionToModelInfo;
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


class OperationFactory {

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
            case "JsonSchemaGenerator":
//                return new SchemaGenerator();
            case "BundleIg":
                return new IgBundler();
//            case "PackageIG":
//                return new PackageOperation();
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
            case "MakeTransaction":
                return new BundleToTransactionOperation();
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
