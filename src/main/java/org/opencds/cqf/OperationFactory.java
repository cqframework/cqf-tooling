package org.opencds.cqf;

//import org.opencds.cqf.jsonschema.SchemaGenerator;
import org.apache.commons.lang.NotImplementedException;
import org.opencds.cqf.acceleratorkit.Processor;
import org.opencds.cqf.library.r4.LibraryGenerator;
import org.opencds.cqf.measure.r4.RefreshR4Measure;
import org.opencds.cqf.measure.stu3.RefreshStu3Measure;
import org.opencds.cqf.modelinfo.StructureDefinitionToModelInfo;
import org.opencds.cqf.operation.*;
import org.opencds.cqf.qdm.QdmToQiCore;
import org.opencds.cqf.quick.QuickPageGenerator;
import org.opencds.cqf.terminology.*;
import org.opencds.cqf.terminology.distributable.DistributableValueSetGenerator;

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
            case "RefreshLibrary":
                return new RefreshLibraryOperation();
            case "RefreshStu3Measure":
                return new RefreshStu3Measure();
            case "RefreshR4Measure":
                return new RefreshR4Measure();
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
            case "ValidateCqfMeasuresStu3Profiles":
                return new org.opencds.cqf.validation.profiles.cqfmeasures.stu3.CqfMeasureProfileValidation();
            case "ValidateCqfMeasuresR4Profiles":
                return new org.opencds.cqf.validation.profiles.cqfmeasures.r4.CqfMeasureProfileValidation();
            case "PostBundlesInDir":
                return new PostBundlesInDirOperation();

            default:
                throw new IllegalArgumentException("Invalid operation: " + operationName);
        }
    }
}
