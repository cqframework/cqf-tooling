package org.opencds.cqf.tooling.casereporting.transformer;

import ca.uhn.fhir.parser.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.opencds.cqf.tooling.parameter.TransformErsdParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

import java.io.File;
import java.io.FileInputStream;

import static org.testng.Assert.*;

public class ErsdTransformerIT {

    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformerIT.class);

    @Test
    public void testErsdTransformerPlanDefinitionReplaced() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/ErsdBundle.json";
        params.outputPath = "src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/output";
        params.pathToV2PlanDefinition = "src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";

        ErsdTransformer ersdTransformer = new ErsdTransformer();
        ersdTransformer.transform(params);

        String expectedOutputFilePath =  params.outputPath + System.getProperty("file.separator") + "rctc-release-2022-03-29-Bundle-rctc.json";
        File bundleFile = new File(expectedOutputFilePath);
        Bundle outputBundle = null;
        if (bundleFile.isFile()) {
            JsonParser jsonParser = (JsonParser)FhirContext.forR4Cached().newJsonParser();
            outputBundle = (Bundle) jsonParser.parseResource(new FileInputStream(bundleFile));
        }

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "plandefinition-us-ecr-specification");

        bundleFile.delete();

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerPlanDefinitionNotReplaced() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/ErsdBundle.json";
        params.outputPath = "src/test/resources/org/opencds/cqf/tooling/casereporting/transformer/output";

        ErsdTransformer ersdTransformer = new ErsdTransformer();
        ersdTransformer.transform(params);

        String expectedOutputFilePath =  params.outputPath + System.getProperty("file.separator") + "rctc-release-2022-03-29-Bundle-rctc.json";
        File bundleFile = new File(expectedOutputFilePath);
        Bundle outputBundle = null;
        if (bundleFile.isFile()) {
            JsonParser jsonParser = (JsonParser)FhirContext.forR4Cached().newJsonParser();
            outputBundle = (Bundle) jsonParser.parseResource(new FileInputStream(bundleFile));
        }

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "plandefinition-ersd-skeleton");

        bundleFile.delete();

        logger.info("Transform");
    }
}
