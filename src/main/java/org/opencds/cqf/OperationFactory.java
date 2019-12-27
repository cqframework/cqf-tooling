package org.opencds.cqf;

//import org.opencds.cqf.jsonschema.SchemaGenerator;
import org.apache.commons.lang.NotImplementedException;
import org.opencds.cqf.acceleratorkit.Processor;
import org.opencds.cqf.bundler.BundleResources;
import org.opencds.cqf.igtools.IgBundler;
import org.opencds.cqf.igtools.RefreshIGOperation;
import org.opencds.cqf.library.r4.LibraryGenerator;
import org.opencds.cqf.measure.r4.RefreshR4Measure;
import org.opencds.cqf.measure.stu3.RefreshStu3Measure;
import org.opencds.cqf.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.qdm.QdmToQiCore;
import org.opencds.cqf.quick.QuickPageGenerator;
import org.opencds.cqf.terminology.*;

class OperationFactory {

    static Operation createOperation(String operationName) {
        switch (operationName) {
            case "QdmToQiCore":
                return new QdmToQiCore();
            case "QiCoreQUICK":
                return new QuickPageGenerator();
            case "VsacXlsxToValueSet":
                return new VSACValueSetGenerator();
            case "VsacMultiXlsxToValueSet":
                return new CMSFlatMultiValueSetGenerator();
            case "VsacXlsxToValueSetBatch":
                return new VSACBatchValueSetGenerator();
            case "HedisXlsxToValueSet":
                return new HEDISValueSetGenerator();
            case "XlsxToValueSet":
                return new GenericValueSetGenerator();
            case "CqlToSTU3Library":
                return new org.opencds.cqf.library.stu3.LibraryGenerator();
            case "CqlToR4Library":
                return new LibraryGenerator();
            case "UpdateSTU3Cql":
                return new org.opencds.cqf.library.stu3.LibraryGenerator();
            case "UpdateR4Cql":
                return new LibraryGenerator();
            case "JsonSchemaGenerator":
//                return new SchemaGenerator();
            case "BundleIg":
                return new IgBundler();
            case "RefreshIG":
                return new RefreshIGOperation();
            case "RefreshStu3Measure":
                return new RefreshStu3Measure();
            case "RefreshR4Measure":
                return new RefreshR4Measure();
            case "CqlToMeasure":
                throw new NotImplementedException();
            case "BundlesToBundle":
                throw new NotImplementedException();
            case "BundleToResources":
                throw new NotImplementedException();
            case "GenerateMIs":
                return new StructureDefinitionToModelInfo();
            case "ProcessAcceleratorKit":
                return new Processor();
            case "BundleResources":
                return new BundleResources();
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
