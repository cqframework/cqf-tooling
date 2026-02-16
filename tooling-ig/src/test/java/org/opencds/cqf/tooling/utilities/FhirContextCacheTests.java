package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class FhirContextCacheTests {

    @Test
    public void TestNumericVersions() {
        FhirContext context = FhirContextCache.getContext("3");
        assertEquals(FhirVersionEnum.DSTU3, context.getVersion().getVersion());

        context = FhirContextCache.getContext("4.0");
        assertEquals(FhirVersionEnum.R4, context.getVersion().getVersion());
    }

    @Test
    public void TestReleaseVersions() {
        FhirContext context = FhirContextCache.getContext("R4");
        assertEquals(FhirVersionEnum.R4, context.getVersion().getVersion());

        context = FhirContextCache.getContext("DSTU3");
        assertEquals(FhirVersionEnum.DSTU3, context.getVersion().getVersion());

        context = FhirContextCache.getContext("dstu3");
        assertEquals(FhirVersionEnum.DSTU3, context.getVersion().getVersion());
    }

    @Test
    public void TestEnumVersions() {
        FhirContext context = FhirContextCache.getContext(FhirVersionEnum.DSTU3);
        assertEquals(FhirVersionEnum.DSTU3, context.getVersion().getVersion());

        context = FhirContextCache.getContext(FhirVersionEnum.R5);
        assertEquals(FhirVersionEnum.R5, context.getVersion().getVersion());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void TestNullVersionString() {
        FhirContextCache.getContext((String) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void TestNullVersionVersion() {
        FhirContextCache.getContext((FhirVersionEnum) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void TestGarbageIn() {
        FhirContextCache.getContext("not-a-version");
    }
}