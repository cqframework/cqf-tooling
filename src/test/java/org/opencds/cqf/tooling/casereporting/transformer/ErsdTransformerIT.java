package org.opencds.cqf.tooling.casereporting.transformer;

import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

public class ErsdTransformerIT {

    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformerIT.class);

    @Test
    public void testErsdTransformer() throws Exception {
        Bundle ersdBundle = FhirContext.forR4Cached().newJsonParser().parseResource(Bundle.class,
                ErsdTransformerIT.class.getResourceAsStream("ErsdBundle.json"));
        ErsdTransformer ersdTransformer = new ErsdTransformer();
        Bundle specificationBundle = ersdTransformer.transform(ersdBundle);
        IOUtils.writeBundle(specificationBundle, "C:/Users/jreys/Documents/src/cqf-tooling/src/test/resources/org/opencds/cqf/tooling/casereporting/transformer", Encoding.JSON, FhirContext.forR4Cached());
        logger.info("Transform");
    }
}
