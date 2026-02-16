package org.opencds.cqf.tooling.processor;

import java.util.List;
import java.util.Map;

import org.opencds.cqf.tooling.common.SoftwareSystem;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
import org.opencds.cqf.tooling.parameter.ScaffoldParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import ca.uhn.fhir.context.FhirContext;

public class ScaffoldProcessor extends BaseProcessor {
    private static String LibraryPath = "/input/resources/library";
    private static String MeasurePath = "/input/resources/measure";

    private FhirContext fhirContext;
    private List<SoftwareSystem> softwareSystems;
    private String igPath;
    private org.opencds.cqf.tooling.utilities.IOUtils.Encoding outputEncoding;

    public void scaffold(ScaffoldParameters params) {
        softwareSystems = params.softwareSystems;
        igPath = params.igPath;
        outputEncoding = params.outputEncoding;

        EnsureLibraryPath();
        EnsureMeasurePath();
        fhirContext = ResourceUtils.getFhirContext(ResourceUtils.FhirVersion.parse(params.igVersion));

        for (Map.Entry<String, List<String>> resourceEntry : params.resourcesToScaffold.entrySet()) {
            String resourceName = resourceEntry.getKey();
            List<String> typesToCreateForResource = resourceEntry.getValue();
            for (String resourceType : typesToCreateForResource) {
                switch (resourceType.toLowerCase()) {
                    case "library":
                        createLibrary(resourceName);
                        break;
                    case "measure":
                        createMeasure(resourceName);
                        break;
                    default:

                }
            }
        }
    }

    private void EnsureLibraryPath() {
        IOUtils.ensurePath(igPath + LibraryPath);
    }

    private void EnsureMeasurePath() {
        IOUtils.ensurePath(igPath + MeasurePath);
    }

    public void createLibrary(String name) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                internalCreateSTU3Library(name);
                break;
            case R4:
                internalCreateR4Library(name);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public void createMeasure(String name) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                internalCreateSTU3Measure(name);
                break;
            case R4:
                internalCreateR4Measure(name);
                break;
            default:
                throw new IllegalArgumentException("Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    /* Move to specific version-specific "Engine" or processor */
    public void internalCreateSTU3Library(String name) {
        org.hl7.fhir.dstu3.model.Library newLibrary = new org.hl7.fhir.dstu3.model.Library();
        newLibrary.setId(name);
        newLibrary.setName(name);
        org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper cqfmHelper = new org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper(igPath);
        cqfmHelper.ensureSoftwareSystemExtensionAndDevice(newLibrary, softwareSystems, fhirContext);

        IOUtils.writeResource(newLibrary, igPath + LibraryPath, outputEncoding, fhirContext);
    }

    public void internalCreateR4Library(String name) {
        org.hl7.fhir.r4.model.Library newLibrary = new org.hl7.fhir.r4.model.Library();
        newLibrary.setId(name);
        newLibrary.setName(name);
        SoftwareSystemHelper cqfmHelper = new SoftwareSystemHelper(igPath);
        cqfmHelper.ensureSoftwareSystemExtensionAndDevice(newLibrary, softwareSystems, fhirContext);

        IOUtils.writeResource(newLibrary, igPath + LibraryPath, outputEncoding, fhirContext);
    }

    public void internalCreateSTU3Measure(String name) {
        org.hl7.fhir.dstu3.model.Measure newMeasure = new org.hl7.fhir.dstu3.model.Measure();
        newMeasure.setId(name);
        newMeasure.setName(name);
        org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper cqfmHelper = new org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper(igPath);
        cqfmHelper.ensureSoftwareSystemExtensionAndDevice(newMeasure, softwareSystems, fhirContext);

        IOUtils.writeResource(newMeasure, igPath + MeasurePath, outputEncoding, fhirContext);
    }

    public void internalCreateR4Measure(String name) {
        org.hl7.fhir.r4.model.Measure newMeasure = new org.hl7.fhir.r4.model.Measure();
        newMeasure.setId(name);
        newMeasure.setName(name);
        SoftwareSystemHelper cqfmHelper = new SoftwareSystemHelper(igPath);
        cqfmHelper.ensureSoftwareSystemExtensionAndDevice(newMeasure, softwareSystems, fhirContext);

        IOUtils.writeResource(newMeasure, igPath + MeasurePath, outputEncoding, fhirContext);
    }
}
