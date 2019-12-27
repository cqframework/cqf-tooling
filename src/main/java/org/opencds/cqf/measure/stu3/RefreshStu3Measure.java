package org.opencds.cqf.measure.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import org.hl7.fhir.dstu3.model.Measure;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.opencds.cqf.measure.RefreshGeneratedContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

public class RefreshStu3Measure extends RefreshGeneratedContent {

    private FhirContext context = FhirContext.forDstu3();
    private JsonParser jsonParser = (JsonParser) context.newJsonParser();
    private XmlParser xmlParser = (XmlParser) context.newXmlParser();

    public RefreshStu3Measure() {
        super("src/main/resources/org/opencds/cqf/measure/output/stu3", "-RefreshStu3Measure");
    }

    @Override
    public void refreshGeneratedContent() {
        File measureDir = new File(this.getPathToMeasures());
        if (measureDir.isDirectory()) {
            for (File f : Optional.ofNullable(measureDir.listFiles()).orElseThrow()) {
                Measure measure = null;
                if (f.isFile()) {
                    try {
                        if (f.getName().endsWith("xml")) {
                            measure = (Measure) xmlParser.parseResource(new FileInputStream(f));
                        }
                        else {
                            measure = (Measure) jsonParser.parseResource(new FileInputStream(f));
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error parsing " + f.getName());
                    }
                }
                if (measure == null) {
                    continue;
                }
                output(refreshMeasure(measure));
            }
        }
        else {

        }
    }

    public Measure refreshMeasure(Measure measure) {
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
        return null;
    }
}
