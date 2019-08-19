package org.opencds.cqf;

//import org.opencds.cqf.jsonschema.SchemaGenerator;
import org.opencds.cqf.acceleratorkit.Processor;
import org.opencds.cqf.igtools.IgBundler;
import org.opencds.cqf.library.LibraryGenerator;
import org.opencds.cqf.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.qdm.QdmToQiCore;
import org.opencds.cqf.quick.QuickPageGenerator;
import org.opencds.cqf.terminology.GenericValueSetGenerator;
import org.opencds.cqf.terminology.CMSFlatMultiValueSetGenerator;
import org.opencds.cqf.terminology.VSACValueSetGenerator;
import org.opencds.cqf.terminology.HEDISValueSetGenerator;
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
            case "HedisXlsxToValueSet":
                return new HEDISValueSetGenerator();
            case "XlsxToValueSet":
                return new GenericValueSetGenerator();
            case "CqlToLibrary":
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
            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
