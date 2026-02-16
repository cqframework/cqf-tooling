package org.opencds.cqf.tooling.casereporting.transformer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.parameter.TransformErsdParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ErsdTransformerIT {

    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformerIT.class);

    private Bundle transformBundle(TransformErsdParameters params, String outputBundleFileName) throws Exception {
        ErsdTransformer ersdTransformer = new ErsdTransformer();
        ersdTransformer.transform(params);

        String expectedOutputFilePath = java.nio.file.Path.of(params.outputPath, outputBundleFileName).toString();

        File bundleFile = new File(expectedOutputFilePath);
        Bundle outputBundle = null;
        if (bundleFile != null) {
            if (bundleFile.getName().endsWith("json")) {
                outputBundle = (Bundle)((JsonParser)FhirContext.forR4Cached().newJsonParser()).parseResource(new FileInputStream(bundleFile));
            }
            else if (bundleFile.getName().endsWith("xml")) {
                outputBundle = (Bundle)((XmlParser)FhirContext.forR4Cached().newXmlParser()).parseResource(new FileInputStream(bundleFile));
            }
        }

        bundleFile.delete();
        return outputBundle;
    }

    @Test
    public void testOutputFileName() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.json";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        params.outputFileName = "test_file_name";
        params.pathToV2PlanDefinition = "src/test/resources/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";
        params.outputFileEncodings = new HashSet<>();
        params.outputFileEncodings.add(IOUtils.Encoding.XML);
        params.prettyPrintOutput = true;

        Bundle outputBundle = transformBundle(params, "test_file_name.xml");

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "us-ecr-specification");

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerPlanDefinitionReplacedJSONInput() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.json";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        params.pathToV2PlanDefinition = "src/test/resources/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";
        String outputBundleFileName = "rctc-release-2022-10-19-Bundle-rctc.json";

        Bundle outputBundle = transformBundle(params, outputBundleFileName);

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "us-ecr-specification");

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerPlanDefinitionReplacedXMLInput() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.xml";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        params.pathToV2PlanDefinition = "src/test/resources/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";
        String outputBundleFileName = "rctc-release-2022-10-19-Bundle-rctc.json";

        Bundle outputBundle = transformBundle(params, outputBundleFileName);

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "us-ecr-specification");

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerPlanDefinitionNotReplaced() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.json";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        String outputBundleFileName = "rctc-release-2022-10-19-Bundle-rctc.json";

        Bundle outputBundle = transformBundle(params, outputBundleFileName);

        assertNotNull(outputBundle);
        assertEquals(outputBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count(), 1);
        assertEquals(outputBundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .findFirst().get().getResource().getIdElement().getIdPart(), "plandefinition-ersd-skeleton");

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerEmergentPriorityUseContextPreserved() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.json";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        String outputBundleFileName = "rctc-release-2022-10-19-Bundle-rctc.json";
        params.pathToV2PlanDefinition = "src/test/resources/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";

        Bundle outputBundle = transformBundle(params, outputBundleFileName);

        assertNotNull(outputBundle);
        ValueSet dxtcValueSet = (ValueSet)outputBundle.getEntry().stream().filter(x -> x.hasResource()
                && x.getResource().fhirType().equals("ValueSet")
                && x.getResource().getId().equals("http://hl7.org/fhir/us/ecr/ValueSet/dxtc")).findFirst().get().getResource();

        assertNotNull(dxtcValueSet);

        List<UsageContext> usageContexts = dxtcValueSet.getUseContext();
        UsageContext usageContext = usageContexts.stream().filter(x -> x.hasCode()
                        && x.getCode().getCode().equals("priority")
                        && x.hasValueCodeableConcept()
                        && x.getValueCodeableConcept().getCodingFirstRep().getCode().equals("emergent")).findFirst().get();
        assertNotNull(usageContext);

        logger.info("Transform");
    }

    @Test
    public void testErsdTransformerManifestElementPresence() throws Exception {
        TransformErsdParameters params = new TransformErsdParameters();
        params.pathToBundle = "src/test/resources/casereporting/transformer/eRSDv1bundle.json";
        params.outputPath = "src/test/resources/casereporting/transformer/output";
        String outputBundleFileName = "rctc-release-2022-10-19-Bundle-rctc.json";
        params.pathToV2PlanDefinition = "src/test/resources/casereporting/transformer/eRSDv2PlanDefinition/plandefinition-us-ecr-specification.json";

        Bundle outputBundle = transformBundle(params, outputBundleFileName);

        assertNotNull(outputBundle);
        Library manifestLibrary;
        manifestLibrary = (Library)outputBundle.getEntry().stream().filter(x -> x.hasResource()
                && x.getResource().fhirType().equals("Library")
                && x.getResource().getId().equals("Library/ersd-specification-library")).findFirst().get().getResource();

        assertNotNull(manifestLibrary);

        List<UsageContext> useContexts = manifestLibrary.getUseContext();
        UsageContext reportingUseContext = useContexts.stream().filter(x -> x.hasCode()
                && x.getCode().getCode().equals("reporting")
                && x.hasValueCodeableConcept()
                && x.getValueCodeableConcept().getCodingFirstRep().getCode().equals("triggering")).findFirst().get();
        assertNotNull(reportingUseContext);

        UsageContext specificationTypeUseContext = useContexts.stream().filter(x -> x.hasCode()
                && x.getCode().getCode().equals("reporting")
                && x.hasValueCodeableConcept()
                && x.getValueCodeableConcept().getCodingFirstRep().getCode().equals("triggering")).findFirst().get();
        assertNotNull(specificationTypeUseContext);

        assertNotNull(manifestLibrary.getDate());
        assertNotNull(manifestLibrary.getEffectivePeriod());
        assertNotNull(manifestLibrary.getEffectivePeriod().getStart());
        assertNotNull(manifestLibrary.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel"));

        logger.info("Transform");
    }
}
