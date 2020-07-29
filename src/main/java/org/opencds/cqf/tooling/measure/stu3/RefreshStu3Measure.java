package org.opencds.cqf.tooling.measure.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.tooling.Main;
import org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.measure.RefreshGeneratedContent;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RefreshStu3Measure extends RefreshGeneratedContent {

    private JsonParser jsonParser;
    private XmlParser xmlParser;
    private CqfmSoftwareSystemHelper cqfmHelper = new CqfmSoftwareSystemHelper();

    public RefreshStu3Measure() {
        super("src/main/resources/org/opencds/cqf/measure/output/stu3", "-RefreshStu3Measure", FhirContext.forDstu3());
        jsonParser = (JsonParser)this.getContext().newJsonParser();
        xmlParser = (XmlParser)this.getContext().newXmlParser();
    }

    public RefreshStu3Measure(String pathToMeasures) {
        super(pathToMeasures, "-RefreshStu3Measure", FhirContext.forDstu3(), null, pathToMeasures);
        jsonParser = (JsonParser)this.getContext().newJsonParser();
        xmlParser = (XmlParser)this.getContext().newXmlParser();
    }

    @Override
    public void refreshGeneratedContent() {
        File measureDir = new File(this.getPathToMeasures());
        if (measureDir.isDirectory()) {
            for (File f : Optional.ofNullable(measureDir.listFiles()).<NoSuchElementException>orElseThrow(() -> new NoSuchElementException())) {
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
        cqfmHelper.ensureToolingExtensionAndDevice(measure);
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
