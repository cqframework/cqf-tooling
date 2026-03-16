package org.opencds.cqf.tooling.utilities.converters;

import static org.testng.Assert.*;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.testng.annotations.Test;

public class ResourceAndTypeConverterTest {

    // ========== Resource conversion: to R5 ==========

    @Test
    public void testConvertR4ResourceToR5() {
        FhirContext ctx = FhirContext.forR4Cached();
        org.hl7.fhir.r4.model.Patient r4Patient = new org.hl7.fhir.r4.model.Patient();
        r4Patient.setId("test-patient");
        r4Patient.addName().setFamily("Smith");

        org.hl7.fhir.r5.model.Resource r5 = ResourceAndTypeConverter.convertToR5Resource(ctx, r4Patient);
        assertNotNull(r5);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.Patient);
        assertEquals(((org.hl7.fhir.r5.model.Patient) r5).getNameFirstRep().getFamily(), "Smith");
    }

    @Test
    public void testConvertDstu3ResourceToR5() {
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.Patient stu3Patient = new org.hl7.fhir.dstu3.model.Patient();
        stu3Patient.setId("test-patient");
        stu3Patient.addName().setFamily("Jones");

        org.hl7.fhir.r5.model.Resource r5 = ResourceAndTypeConverter.convertToR5Resource(ctx, stu3Patient);
        assertNotNull(r5);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.Patient);
        assertEquals(((org.hl7.fhir.r5.model.Patient) r5).getNameFirstRep().getFamily(), "Jones");
    }

    @Test
    public void testConvertR5ResourceToR5IsPassthrough() {
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.r5.model.Patient r5Patient = new org.hl7.fhir.r5.model.Patient();
        r5Patient.setId("test-patient");

        org.hl7.fhir.r5.model.Resource result = ResourceAndTypeConverter.convertToR5Resource(ctx, r5Patient);
        assertSame(result, r5Patient, "R5 to R5 should be a passthrough");
    }

    // The default branch (unsupported FHIR version) cannot be tested because
    // FhirContext.forXxxCached() throws IllegalStateException for versions not on the classpath
    // (DSTU2, R4B) before the converter code is reached. Only DSTU3, R4, and R5 are available.

    // ========== Resource conversion: from R5 ==========

    @Test
    public void testConvertR5ResourceToR4() {
        FhirContext ctx = FhirContext.forR4Cached();
        org.hl7.fhir.r5.model.Patient r5Patient = new org.hl7.fhir.r5.model.Patient();
        r5Patient.setId("test-patient");
        r5Patient.addName().setFamily("Brown");

        IBaseResource r4 = ResourceAndTypeConverter.convertFromR5Resource(ctx, r5Patient);
        assertNotNull(r4);
        assertTrue(r4 instanceof org.hl7.fhir.r4.model.Patient);
        assertEquals(((org.hl7.fhir.r4.model.Patient) r4).getNameFirstRep().getFamily(), "Brown");
    }

    @Test
    public void testConvertR5ResourceToDstu3() {
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.r5.model.Patient r5Patient = new org.hl7.fhir.r5.model.Patient();
        r5Patient.setId("test-patient");

        IBaseResource stu3 = ResourceAndTypeConverter.convertFromR5Resource(ctx, r5Patient);
        assertNotNull(stu3);
        assertTrue(stu3 instanceof org.hl7.fhir.dstu3.model.Patient);
    }

    @Test
    public void testConvertFromR5ToR5IsPassthrough() {
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.r5.model.Patient r5Patient = new org.hl7.fhir.r5.model.Patient();

        IBaseResource result = ResourceAndTypeConverter.convertFromR5Resource(ctx, r5Patient);
        assertSame(result, r5Patient);
    }

    // See note above about untestable default branches

    // ========== Type conversion ==========

    @Test
    public void testConvertR4TypeToR5() {
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.r4.model.StringType r4String = new org.hl7.fhir.r4.model.StringType("hello");

        IBaseDatatype r5 = ResourceAndTypeConverter.convertType(ctx, r4String);
        assertNotNull(r5);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.StringType);
        assertEquals(((org.hl7.fhir.r5.model.StringType) r5).getValue(), "hello");
    }

    @Test
    public void testConvertDstu3TypeToR5() {
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.dstu3.model.StringType stu3String = new org.hl7.fhir.dstu3.model.StringType("world");

        IBaseDatatype r5 = ResourceAndTypeConverter.convertType(ctx, stu3String);
        assertNotNull(r5);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.StringType);
        assertEquals(((org.hl7.fhir.r5.model.StringType) r5).getValue(), "world");
    }

    @Test
    public void testConvertR5TypeToR4() {
        FhirContext ctx = FhirContext.forR4Cached();
        org.hl7.fhir.r5.model.StringType r5String = new org.hl7.fhir.r5.model.StringType("test");

        IBaseDatatype r4 = ResourceAndTypeConverter.convertType(ctx, r5String);
        assertNotNull(r4);
        assertTrue(r4 instanceof org.hl7.fhir.r4.model.StringType);
        assertEquals(((org.hl7.fhir.r4.model.StringType) r4).getValue(), "test");
    }

    @Test
    public void testConvertR5TypeToDstu3() {
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.r5.model.StringType r5String = new org.hl7.fhir.r5.model.StringType("stu3");

        IBaseDatatype stu3 = ResourceAndTypeConverter.convertType(ctx, r5String);
        assertNotNull(stu3);
        assertTrue(stu3 instanceof org.hl7.fhir.dstu3.model.StringType);
        assertEquals(((org.hl7.fhir.dstu3.model.StringType) stu3).getValue(), "stu3");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConvertTypeR5ContextWithNonConvertibleType() {
        // R5 context but type is already R5 (not DSTU3 or R4)
        FhirContext ctx = FhirContext.forR5Cached();
        org.hl7.fhir.r5.model.StringType r5String = new org.hl7.fhir.r5.model.StringType("nope");
        ResourceAndTypeConverter.convertType(ctx, r5String);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConvertTypeR4ContextWithNonR5Type() {
        // R4 context but type is R4 (not R5) — can't convert R4 to R4
        FhirContext ctx = FhirContext.forR4Cached();
        org.hl7.fhir.r4.model.StringType r4String = new org.hl7.fhir.r4.model.StringType("nope");
        ResourceAndTypeConverter.convertType(ctx, r4String);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConvertTypeDstu3ContextWithNonR5Type() {
        // DSTU3 context but type is DSTU3 (not R5) — can't convert DSTU3 to DSTU3
        FhirContext ctx = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.StringType stu3String = new org.hl7.fhir.dstu3.model.StringType("nope");
        ResourceAndTypeConverter.convertType(ctx, stu3String);
    }

    // See note above about untestable default branches

    // ========== Direct helper methods ==========

    @Test
    public void testStu3ToR5ResourceDirect() {
        org.hl7.fhir.dstu3.model.Library stu3Lib = new org.hl7.fhir.dstu3.model.Library();
        stu3Lib.setId("lib1");
        org.hl7.fhir.r5.model.Resource r5 = ResourceAndTypeConverter.stu3ToR5Resource(stu3Lib);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.Library);
    }

    @Test
    public void testR5ToStu3ResourceDirect() {
        org.hl7.fhir.r5.model.Library r5Lib = new org.hl7.fhir.r5.model.Library();
        r5Lib.setId("lib1");
        org.hl7.fhir.dstu3.model.Resource stu3 = ResourceAndTypeConverter.r5ToStu3Resource(r5Lib);
        assertTrue(stu3 instanceof org.hl7.fhir.dstu3.model.Library);
    }

    @Test
    public void testR4ToR5ResourceDirect() {
        org.hl7.fhir.r4.model.Library r4Lib = new org.hl7.fhir.r4.model.Library();
        r4Lib.setId("lib1");
        org.hl7.fhir.r5.model.Resource r5 = ResourceAndTypeConverter.r4ToR5Resource(r4Lib);
        assertTrue(r5 instanceof org.hl7.fhir.r5.model.Library);
    }

    @Test
    public void testR5ToR4ResourceDirect() {
        org.hl7.fhir.r5.model.Library r5Lib = new org.hl7.fhir.r5.model.Library();
        r5Lib.setId("lib1");
        org.hl7.fhir.r4.model.Resource r4 = ResourceAndTypeConverter.r5ToR4Resource(r5Lib);
        assertTrue(r4 instanceof org.hl7.fhir.r4.model.Library);
    }
}
