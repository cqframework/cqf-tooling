package org.opencds.cqf.tooling.cli;

//import org.opencds.cqf.tooling.jsonschema.SchemaGenerator;
import org.apache.commons.lang.NotImplementedException;
import org.opencds.cqf.tooling.casereporting.transformer.ErsdTransformer;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.TemplateToValueSetGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.DTProcessor;
import org.opencds.cqf.tooling.acceleratorkit.Processor;
import org.opencds.cqf.tooling.dateroller.DataDateRollerOperation;
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
import org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.TemplateToValueSetGenerator;


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
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
