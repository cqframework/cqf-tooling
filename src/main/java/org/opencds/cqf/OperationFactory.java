package org.opencds.cqf;

//import org.opencds.cqf.jsonschema.SchemaGenerator;
import org.opencds.cqf.acceleratorkit.Processor;
import org.opencds.cqf.bundler.BundleResources;
import org.opencds.cqf.igtools.IgBundler;
import org.opencds.cqf.library.R4LibraryGenerator;
import org.opencds.cqf.library.STU3LibraryGenerator;
import org.opencds.cqf.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.qdm.QdmToQiCore;
import org.opencds.cqf.quick.QuickPageGenerator;
import org.opencds.cqf.terminology.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
                return new STU3LibraryGenerator();
            case "CqlToR4Library":
                return new R4LibraryGenerator();
            case "UpdateCql":
                return new LibraryGenerator();
            case "JsonSchemaGenerator":
//                return new SchemaGenerator();
            case "BundleIg":
                return new IgBundler();
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
