package org.opencds.cqf.tooling.terminology.federation;

import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.ValueSet;
import org.testng.annotations.Test;

public class FhirTerminologyClientIT {

    @Test
    public void TestDirectExpand() {
        FhirContext context = FhirContext.forR4();

        // TODO: Need a valid APIKey here
        Endpoint vsacEndpoint = new Endpoint().setAddress("https://uat-cts.nlm.nih.gov/fhir/r4")
                .addHeader("Authorization: Basic YXBpa2V5OmQ2MmMwOGFmLWNjNGItNGE3ZS1hNzUyLWQ4Yjk4NmM5ZWM4ZQ==");
        FhirTerminologyClient ftc = new FhirTerminologyClient(context, vsacEndpoint);
        ValueSet vs = ftc.expand("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.108.12.1018");
        assertTrue(vs != null);
    }
}
