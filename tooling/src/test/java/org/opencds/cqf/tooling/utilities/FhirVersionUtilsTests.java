package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirVersionEnum;

public class FhirVersionUtilsTests {

    @Test
    public void TestNumericVersions() {
        FhirVersionEnum version = FhirVersionUtils.getVersionEnum("3");
        assertEquals(FhirVersionEnum.DSTU3, version);

        version = FhirVersionUtils.getVersionEnum("4.0");
        assertEquals(FhirVersionEnum.R4, version);

        version = FhirVersionUtils.getVersionEnum("0.0.0.0");
        assertNull(version);
    }

    @Test
    public void TestReleaseVersions() {
        FhirVersionEnum version = FhirVersionUtils.getVersionEnum("R4");
        assertEquals(FhirVersionEnum.R4, version);

        version = FhirVersionUtils.getVersionEnum("DSTU3");
        assertEquals(FhirVersionEnum.DSTU3, version);

        version = FhirVersionUtils.getVersionEnum("dstu3");
        assertEquals(FhirVersionEnum.DSTU3, version);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void TestNullVersion() {
        FhirVersionUtils.getVersionEnum(null);
    }

    public void TestGarbageIn() {
        FhirVersionEnum version = FhirVersionUtils.getVersionEnum("not-a-version");
        assertNull(version);
    }
}