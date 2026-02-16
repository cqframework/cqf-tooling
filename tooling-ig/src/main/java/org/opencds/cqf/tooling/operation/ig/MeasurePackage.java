package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleBuilder;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Measure;

public class MeasurePackage {
    private final Measure r5Measure; // used for packaging
    private final IBaseResource measure;
    private final FhirContext fhirContext;
    private final LibraryPackage libraryPackage;

    public MeasurePackage(Measure r5Measure, IBaseResource measure,
                                 FhirContext fhirContext, LibraryPackage libraryPackage) {
        this.r5Measure = r5Measure;
        this.measure = measure;
        this.fhirContext = fhirContext;
        this.libraryPackage = libraryPackage;
    }

    public IBaseBundle bundleResources() {
        BundleBuilder builder = new BundleBuilder(this.fhirContext);
        builder.addTransactionUpdateEntry(measure);
        builder.addTransactionUpdateEntry(libraryPackage.getLibrary());
        libraryPackage.getDependsOnLibraries().forEach(builder::addTransactionUpdateEntry);
        libraryPackage.getDependsOnValueSets().forEach(builder::addTransactionUpdateEntry);
        libraryPackage.getDependsOnCodeSystems().forEach(builder::addTransactionUpdateEntry);
        return builder.getBundle();
    }

    public Measure getR5PlanDefinition() {
        return r5Measure;
    }

    public IBaseResource getMeasure() {
        return measure;
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    public LibraryPackage getLibraryPackage() {
        return libraryPackage;
    }
}
