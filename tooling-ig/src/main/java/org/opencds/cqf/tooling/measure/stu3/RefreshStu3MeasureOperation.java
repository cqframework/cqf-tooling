package org.opencds.cqf.tooling.measure.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import com.google.common.base.Strings;
import org.hl7.fhir.dstu3.model.Measure;
import org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper;
import org.opencds.cqf.tooling.operation.RefreshGeneratedContentOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RefreshStu3MeasureOperation extends RefreshGeneratedContentOperation {

    private JsonParser jsonParser;
    private XmlParser xmlParser;
    private SoftwareSystemHelper softwareSystemHelper;

    //NOTE: Only consumed from OperationFactory - that call should come through a proper Operation that calls a processor.
    public RefreshStu3MeasureOperation() {
        super("src/main/resources/org/opencds/cqf/tooling/measure/output/stu3",
                "-RefreshStu3Measure", FhirContext.forCached(FhirVersionEnum.DSTU3));
        softwareSystemHelper = new SoftwareSystemHelper("src/main/resources/org/opencds/cqf/tooling/measure/output/r4");
        jsonParser = (JsonParser)super.fhirContext.newJsonParser();
        xmlParser = (XmlParser)super.fhirContext.newXmlParser();
    }

    @SuppressWarnings("this-escape")
    public RefreshStu3MeasureOperation(String pathToMeasures) {
        super(pathToMeasures, "-RefreshStu3Measure", FhirContext.forCached(FhirVersionEnum.DSTU3),
                null, pathToMeasures);
        if (!Strings.isNullOrEmpty(getOutputPath())) {
            softwareSystemHelper = new SoftwareSystemHelper(getOutputPath());
        } else {
            softwareSystemHelper = new SoftwareSystemHelper();
        }
        jsonParser = (JsonParser)this.getFhirContext().newJsonParser();
        xmlParser = (XmlParser)this.getFhirContext().newXmlParser();
    }

    @Override
    public void refreshGeneratedContent() {
        File measureDir = new File(this.getPathToMeasures());
        if (measureDir.isDirectory()) {
            for (File f : Optional.ofNullable(measureDir.listFiles()).orElseThrow(NoSuchElementException::new)) {
                refreshMeasureFromFile(f);
            }
        }
        else if (measureDir.isFile()){
            refreshMeasureFromFile(measureDir);
        }
    }

    private void refreshMeasureFromFile(File f) {
        Measure measure = null;
        IOUtils.Encoding encoding = null;

        if (f.isFile()) {
            try {
                if (f.getName().endsWith("xml")) {
                    measure = (Measure)xmlParser.parseResource(new FileInputStream(f));
                    encoding = IOUtils.Encoding.XML;
                }
                else {
                    measure = (Measure)jsonParser.parseResource(new FileInputStream(f));
                    encoding = IOUtils.Encoding.JSON;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + f.getName());
            }
        }
        if (measure == null) {
            return;
        }
        output(refreshMeasure(measure), encoding);
    }

    public Measure refreshMeasure(Measure measure) {
        if (shouldApplySoftwareSystemStamp) {
            softwareSystemHelper.ensureCQFToolingExtensionAndDevice(measure, this.getFhirContext());
        }
//        CqfMeasure cqfMeasure = this.dataRequirementsProvider.createCqfMeasure(measure, this.libraryResourceProvider);
//
//        // Ensure All Related Artifacts for all referenced Libraries
//        if (!cqfMeasure.getRelatedArtifact().isEmpty()) {
//            for (RelatedArtifact relatedArtifact : cqfMeasure.getRelatedArtifact()) {
//                boolean artifactExists = false;
//                for (RelatedArtifact resourceArtifact : measure.getRelatedArtifact()) {
//                    if (resourceArtifact.equalsDeep(relatedArtifact)) {
//                        artifactExists = true;
//                        break;
//                    }
//                }
//                if (!artifactExists) {
//                    measure.addRelatedArtifact(relatedArtifact.copy());
//                }
//            }
//        }
//
//        Narrative n = this.narrativeProvider.getNarrative(this.getContext(), cqfMeasure);
//        measure.setText(n.copy());
//        // logger.info("Narrative: " + n.getDivAsString());
//        return measure;
        return measure;
    }
}
