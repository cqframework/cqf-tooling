package org.opencds.cqf.tooling;

//import org.opencds.cqf.tooling.jsonschema.SchemaGenerator;
import org.apache.commons.lang.NotImplementedException;
import org.opencds.cqf.tooling.acceleratorkit.Processor;
import org.opencds.cqf.tooling.library.r4.LibraryGenerator;
import org.opencds.cqf.tooling.measure.r4.RefreshR4MeasureOperation;
import org.opencds.cqf.tooling.measure.stu3.RefreshStu3MeasureOperation;
import org.opencds.cqf.tooling.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.tooling.operation.*;
import org.opencds.cqf.tooling.qdm.QdmToQiCore;
import org.opencds.cqf.tooling.quick.QuickPageGenerator;
import org.opencds.cqf.tooling.terminology.*;
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
//            case "TestIG":
//                return new TestIGOperation();
            case "CqlToMeasure":
                throw new NotImplementedException("CqlToMeasure");
            case "BundlesToBundle":
                throw new NotImplementedException("BundlesToBundle");
            case "BundleToResources":
                throw new NotImplementedException("BundleToResources");
            case "GenerateMIs":
                return new StructureDefinitionToModelInfo();
            case "ProcessAcceleratorKit":
                return new Processor();
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
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
