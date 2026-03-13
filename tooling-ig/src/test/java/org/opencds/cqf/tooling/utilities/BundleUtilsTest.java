package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.testng.annotations.Test;

public class BundleUtilsTest {

    // ── getBundleType ──

    @Test
    public void getBundleType_transaction_returnsTransaction() {
        BundleTypeEnum result = BundleUtils.getBundleType("transaction");
        assertEquals(result, BundleTypeEnum.TRANSACTION);
    }

    @Test
    public void getBundleType_collection_returnsCollection() {
        BundleTypeEnum result = BundleUtils.getBundleType("collection");
        assertEquals(result, BundleTypeEnum.COLLECTION);
    }

    @Test
    public void getBundleType_caseInsensitive_matches() {
        BundleTypeEnum result = BundleUtils.getBundleType("TRANSACTION");
        assertEquals(result, BundleTypeEnum.TRANSACTION);
    }

    @Test
    public void getBundleType_mixedCase_matches() {
        BundleTypeEnum result = BundleUtils.getBundleType("Transaction");
        assertEquals(result, BundleTypeEnum.TRANSACTION);
    }

    @Test
    public void getBundleType_unknownType_returnsNull() {
        BundleTypeEnum result = BundleUtils.getBundleType("not-a-type");
        assertNull(result);
    }

    @Test
    public void getBundleType_null_returnsNullWithoutNpe() {
        // Intent: null input should be handled gracefully, not throw NPE
        BundleTypeEnum result = BundleUtils.getBundleType(null);
        assertNull(result);
    }

    @Test
    public void getBundleType_empty_returnsNull() {
        BundleTypeEnum result = BundleUtils.getBundleType("");
        assertNull(result);
    }

    // ── getResourcesFromBundle ──

    @Test
    public void getResourcesFromBundle_r4_returnsNonBundleEntries() {
        FhirContext fhirContext = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        bundle.addEntry().setResource(patient);

        Library library = new Library();
        library.setId("Library/l1");
        bundle.addEntry().setResource(library);

        List<IBaseResource> resources = BundleUtils.getResourcesFromBundle(fhirContext, bundle);
        assertEquals(resources.size(), 2);
    }

    @Test
    public void getResourcesFromBundle_r4_excludesNestedBundles() {
        FhirContext fhirContext = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        bundle.addEntry().setResource(patient);

        Bundle nestedBundle = new Bundle();
        nestedBundle.setId("Bundle/nested");
        bundle.addEntry().setResource(nestedBundle);

        List<IBaseResource> resources = BundleUtils.getResourcesFromBundle(fhirContext, bundle);
        assertEquals(resources.size(), 1, "Nested bundles should be excluded");
        assertEquals(resources.get(0).getIdElement().getIdPart(), "p1");
    }

    @Test
    public void getResourcesFromBundle_r4_emptyBundle_returnsEmptyList() {
        FhirContext fhirContext = FhirContext.forR4Cached();
        Bundle bundle = new Bundle();
        List<IBaseResource> resources = BundleUtils.getResourcesFromBundle(fhirContext, bundle);
        assertNotNull(resources);
        assertTrue(resources.isEmpty());
    }

    @Test
    public void getResourcesFromBundle_dstu3_returnsNonBundleEntries() {
        FhirContext fhirContext = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();
        patient.setId("Patient/p1");
        bundle.addEntry().setResource(patient);

        List<IBaseResource> resources = BundleUtils.getResourcesFromBundle(fhirContext, bundle);
        assertEquals(resources.size(), 1);
    }

    @Test
    public void getResourcesFromBundle_dstu3_excludesNestedBundles() {
        FhirContext fhirContext = FhirContext.forDstu3Cached();
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();
        patient.setId("Patient/p1");
        bundle.addEntry().setResource(patient);

        org.hl7.fhir.dstu3.model.Bundle nestedBundle = new org.hl7.fhir.dstu3.model.Bundle();
        nestedBundle.setId("Bundle/nested");
        bundle.addEntry().setResource(nestedBundle);

        List<IBaseResource> resources = BundleUtils.getResourcesFromBundle(fhirContext, bundle);
        assertEquals(resources.size(), 1, "DSTU3 should exclude nested bundles");
        assertEquals(resources.get(0).getIdElement().getIdPart(), "p1");
    }

    // ── resourceIsABundle ──

    @Test
    public void resourceIsABundle_r4Bundle_true() {
        assertTrue(BundleUtils.resourceIsABundle(new Bundle()));
    }

    @Test
    public void resourceIsABundle_dstu3Bundle_true() {
        assertTrue(BundleUtils.resourceIsABundle(new org.hl7.fhir.dstu3.model.Bundle()));
    }

    @Test
    public void resourceIsABundle_nonBundle_false() {
        assertFalse(BundleUtils.resourceIsABundle(new Patient()));
    }

    @Test
    public void resourceIsABundle_null_returnsFalse() {
        // Intent: null should not throw, should return false
        // Note: `null instanceof X` returns false in Java, so this works
        assertFalse(BundleUtils.resourceIsABundle(null));
    }

    // ── resourceIsTransactionBundle ──

    @Test
    public void resourceIsTransactionBundle_r4Transaction_true() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        assertTrue(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    @Test
    public void resourceIsTransactionBundle_r4Collection_false() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        assertFalse(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    @Test
    public void resourceIsTransactionBundle_dstu3Transaction_true() {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
        assertTrue(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    @Test
    public void resourceIsTransactionBundle_dstu3Collection_false() {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        bundle.setType(org.hl7.fhir.dstu3.model.Bundle.BundleType.COLLECTION);
        assertFalse(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    @Test
    public void resourceIsTransactionBundle_null_false() {
        assertFalse(BundleUtils.resourceIsTransactionBundle(null));
    }

    @Test
    public void resourceIsTransactionBundle_nonBundle_false() {
        assertFalse(BundleUtils.resourceIsTransactionBundle(new Patient()));
    }

    @Test
    public void resourceIsTransactionBundle_r4NoTypeSet_doesNotThrow() {
        // Intent: a bundle with no type set should return false, not NPE
        Bundle bundle = new Bundle();
        // type not set — getType() returns BundleType.NULL
        assertFalse(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    @Test
    public void resourceIsTransactionBundle_dstu3NoTypeSet_doesNotThrow() {
        org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        assertFalse(BundleUtils.resourceIsTransactionBundle(bundle));
    }

    // ── bundleR4Artifacts ──

    @Test
    public void bundleR4Artifacts_setsTransactionType() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test-bundle", resources, null, false);

        assertEquals(result.getType(), Bundle.BundleType.TRANSACTION);
    }

    @Test
    public void bundleR4Artifacts_setsId() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("my-bundle", resources, null, false);

        assertNotNull(result.getIdElement().getIdPart());
        assertTrue(result.getIdElement().getIdPart().contains("my-bundle"),
                "Bundle ID should contain the provided base id");
    }

    @Test
    public void bundleR4Artifacts_addsTimestamp_whenTrue() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, true);

        assertNotNull(result.getTimestamp(), "Timestamp should be set when addBundleTimestamp is true");
    }

    @Test
    public void bundleR4Artifacts_noTimestamp_whenFalse() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, false);

        assertNull(result.getTimestamp(), "Timestamp should not be set when addBundleTimestamp is false");
    }

    @Test
    public void bundleR4Artifacts_noTimestamp_whenNull() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, null);

        assertNull(result.getTimestamp(), "Timestamp should not be set when addBundleTimestamp is null");
    }

    @Test
    public void bundleR4Artifacts_identifierNotMutated() {
        // Fixed: bundleR4Artifacts now copies the identifier, so the caller's object is not mutated
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Identifier identifier = new Identifier();
        identifier.setSystem("http://example.org");
        identifier.setValue("original-value");
        List<Object> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, identifiers, false);

        assertEquals(identifier.getValue(), "original-value",
                "Caller's identifier should not be mutated");
        assertEquals(result.getIdentifier().getValue(), "original-value-bundle",
                "Bundle's identifier should have '-bundle' appended");
    }

    @Test
    public void bundleR4Artifacts_entriesHaveRequestMethod() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, false);

        assertEquals(result.getEntry().size(), 1);
        assertEquals(result.getEntry().get(0).getRequest().getMethod(), Bundle.HTTPVerb.PUT);
    }

    @Test
    public void bundleR4Artifacts_resourceWithoutResourceType_useFhirType() {
        // When resource ID doesn't have resource type prefix, it should use fhirType()
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("p1"); // no type prefix
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, false);

        String requestUrl = result.getEntry().get(0).getRequest().getUrl();
        assertTrue(requestUrl.startsWith("Patient/"),
                "Request URL should use fhirType when resource type is missing from ID");
    }

    @Test
    public void bundleR4Artifacts_resourceWithResourceType_usesIdValue() {
        List<IBaseResource> resources = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId("Patient/p1"); // has type prefix
        resources.add(patient);

        Bundle result = BundleUtils.bundleR4Artifacts("test", resources, null, false);

        String requestUrl = result.getEntry().get(0).getRequest().getUrl();
        assertEquals(requestUrl, "Patient/p1");
    }

    // ── bundleStu3Artifacts ──

    @Test
    public void bundleStu3Artifacts_setsTransactionType() {
        List<IBaseResource> resources = new ArrayList<>();
        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        org.hl7.fhir.dstu3.model.Bundle result = BundleUtils.bundleStu3Artifacts("test-bundle", resources);

        assertEquals(result.getType(), org.hl7.fhir.dstu3.model.Bundle.BundleType.TRANSACTION);
    }

    @Test
    public void bundleStu3Artifacts_entriesHavePutMethod() {
        List<IBaseResource> resources = new ArrayList<>();
        org.hl7.fhir.dstu3.model.Patient patient = new org.hl7.fhir.dstu3.model.Patient();
        patient.setId("Patient/p1");
        resources.add(patient);

        org.hl7.fhir.dstu3.model.Bundle result = BundleUtils.bundleStu3Artifacts("test", resources);

        assertEquals(result.getEntry().size(), 1);
        assertEquals(result.getEntry().get(0).getRequest().getMethod(),
                org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.PUT);
    }

    // ── extractResources dispatch ──

    @Test
    public void extractResources_versionMismatch_stu3BundleWithR4Version_throws() {
        // Intent: passing a DSTU3 bundle with "r4" version should throw
        org.hl7.fhir.dstu3.model.Bundle stu3Bundle = new org.hl7.fhir.dstu3.model.Bundle();
        assertThrows(IllegalArgumentException.class,
                () -> BundleUtils.extractResources(stu3Bundle, "json", "/tmp", false, "r4"));
    }

    @Test
    public void extractResources_versionMismatch_r4BundleWithStu3Version_throws() {
        // Intent: passing an R4 bundle with "stu3" version should throw
        Bundle r4Bundle = new Bundle();
        assertThrows(IllegalArgumentException.class,
                () -> BundleUtils.extractResources(r4Bundle, "json", "/tmp", false, "stu3"));
    }

    @Test
    public void extractResources_invalidVersion_throws() {
        Bundle r4Bundle = new Bundle();
        assertThrows(IllegalArgumentException.class,
                () -> BundleUtils.extractResources(r4Bundle, "json", "/tmp", false, "r5"));
    }

    @Test
    public void extractResources_uppercaseVersion_matchesCaseInsensitively() {
        // Fixed: version comparison is now case-insensitive
        // Note: can't fully test without a valid output dir and bundle entries,
        // but at minimum "R4" should not throw IllegalArgumentException for version mismatch
        Bundle r4Bundle = new Bundle();
        // Empty bundle + non-existent dir is fine — we just verify no version mismatch exception
        Set<String> result = BundleUtils.extractResources(r4Bundle, "json", "/tmp", false, "R4");
        assertNotNull(result);
    }

    // ── getBundlesInDir ──

    @Test
    public void getBundlesInDir_nonExistentDirectory_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> BundleUtils.getBundlesInDir("/nonexistent/path/that/does/not/exist",
                        ca.uhn.fhir.context.FhirContext.forR4Cached()));
    }
}
