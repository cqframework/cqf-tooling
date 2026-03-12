package org.opencds.cqf.tooling.utilities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.CanonicalType;
import org.opencds.cqf.tooling.exception.InvalidCanonical;
import org.testng.annotations.Test;

public class CanonicalUtilsTest {

    private static final String FULL_CANONICAL = "http://example.com/fhir/Library/my-lib|1.0.0";
    private static final String FULL_CANONICAL_WITH_FRAGMENT = "http://example.com/fhir/Library/my-lib|1.0.0#cql";
    private static final String FRAGMENT_ONLY = "http://example.com/fhir/Library/my-lib#cql";

    // ---- getUrl ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getUrl_null_throws() {
        CanonicalUtils.getUrl((String) null);
    }

    @Test
    public void getUrl_bareId_returnsNull() {
        assertNull(CanonicalUtils.getUrl("my-lib"));
    }

    @Test
    public void getUrl_fullCanonical_stripsVersionAndFragment() {
        assertEquals(CanonicalUtils.getUrl(FULL_CANONICAL_WITH_FRAGMENT), "http://example.com/fhir/Library/my-lib");
    }

    @Test
    public void getUrl_noVersion_stripsFragment() {
        assertEquals(CanonicalUtils.getUrl(FRAGMENT_ONLY), "http://example.com/fhir/Library/my-lib");
    }

    @Test
    public void getUrl_noVersionOrFragment_returnsWhole() {
        assertEquals(
                CanonicalUtils.getUrl("http://example.com/fhir/Library/my-lib"),
                "http://example.com/fhir/Library/my-lib");
    }

    @Test
    public void getUrl_urnUuid_returnsWhole() {
        assertEquals(CanonicalUtils.getUrl("urn:uuid:12345"), "urn:uuid:12345");
    }

    @Test
    public void getUrl_urnUuidWithVersion_stripsVersion() {
        assertEquals(CanonicalUtils.getUrl("urn:uuid:12345|1.0"), "urn:uuid:12345");
    }

    @Test
    public void getUrl_urnOid_returnsWhole() {
        assertEquals(CanonicalUtils.getUrl("urn:oid:1.2.3"), "urn:oid:1.2.3");
    }

    @Test
    public void getUrl_canonicalType_works() {
        CanonicalType ct = new CanonicalType(FULL_CANONICAL);
        assertEquals(CanonicalUtils.getUrl(ct), "http://example.com/fhir/Library/my-lib");
    }

    @Test(expectedExceptions = InvalidCanonical.class)
    public void getUrl_canonicalTypeNoValue_throws() {
        CanonicalUtils.getUrl(new CanonicalType());
    }

    // ---- getResourceType ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getResourceType_null_throws() {
        CanonicalUtils.getResourceType((String) null);
    }

    @Test
    public void getResourceType_noSlash_returnsNull() {
        assertNull(CanonicalUtils.getResourceType("my-lib"));
    }

    @Test
    public void getResourceType_fullCanonical_returnsResourceType() {
        assertEquals(CanonicalUtils.getResourceType(FULL_CANONICAL), "Library");
    }

    @Test
    public void getResourceType_measureCanonical_returnsMeasure() {
        assertEquals(CanonicalUtils.getResourceType("http://example.com/fhir/Measure/my-measure|2.0"), "Measure");
    }

    @Test
    public void getResourceType_duplicatePathSegment_returnsCorrectType() {
        // Regression: String.replace() used to replace ALL occurrences of the tail
        assertEquals(CanonicalUtils.getResourceType("http://example.com/foo/foo"), "foo",
                "Should return 'foo' not 'example.com' when path segment is repeated");
    }

    @Test
    public void getResourceType_idMatchesEarlierSegment_handledCorrectly() {
        // The ID "fhir" also appears in the base URL
        assertEquals(CanonicalUtils.getResourceType("http://example.com/fhir/Library/fhir"), "Library");
    }

    @Test
    public void getResourceType_singleSlash_returnsBase() {
        assertEquals(CanonicalUtils.getResourceType("Library/my-lib"), "Library");
    }

    @Test
    public void getResourceType_withVersionAndFragment_returnsResourceType() {
        assertEquals(CanonicalUtils.getResourceType(FULL_CANONICAL_WITH_FRAGMENT), "Library");
    }

    @Test
    public void getResourceType_canonicalType_works() {
        assertEquals(CanonicalUtils.getResourceType(new CanonicalType(FULL_CANONICAL)), "Library");
    }

    // ---- getIdPart ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getIdPart_null_throws() {
        CanonicalUtils.getIdPart((String) null);
    }

    @Test
    public void getIdPart_bareId_returnsNull() {
        assertNull(CanonicalUtils.getIdPart("my-lib"));
    }

    @Test
    public void getIdPart_fullCanonical_returnsId() {
        assertEquals(CanonicalUtils.getIdPart(FULL_CANONICAL), "my-lib");
    }

    @Test
    public void getIdPart_withFragment_stripsFragment() {
        assertEquals(CanonicalUtils.getIdPart(FULL_CANONICAL_WITH_FRAGMENT), "my-lib");
    }

    @Test
    public void getIdPart_fragmentOnly_stripsFragment() {
        assertEquals(CanonicalUtils.getIdPart(FRAGMENT_ONLY), "my-lib");
    }

    @Test
    public void getIdPart_noVersionOrFragment_returnsId() {
        assertEquals(CanonicalUtils.getIdPart("http://example.com/fhir/Library/my-lib"), "my-lib");
    }

    @Test
    public void getIdPart_canonicalType_works() {
        assertEquals(CanonicalUtils.getIdPart(new CanonicalType(FULL_CANONICAL)), "my-lib");
    }

    // ---- getVersion ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getVersion_null_throws() {
        CanonicalUtils.getVersion((String) null);
    }

    @Test
    public void getVersion_noPipe_returnsNull() {
        assertNull(CanonicalUtils.getVersion("http://example.com/Library/lib"));
    }

    @Test
    public void getVersion_withVersion_returnsVersion() {
        assertEquals(CanonicalUtils.getVersion(FULL_CANONICAL), "1.0.0");
    }

    @Test
    public void getVersion_withFragment_stripsFragment() {
        assertEquals(CanonicalUtils.getVersion(FULL_CANONICAL_WITH_FRAGMENT), "1.0.0");
    }

    @Test
    public void getVersion_trailingPipe_returnsEmpty() {
        assertEquals(CanonicalUtils.getVersion("http://example.com/Library/lib|"), "");
    }

    @Test
    public void getVersion_pipeAtStart_returnsVersion() {
        assertEquals(CanonicalUtils.getVersion("|1.0"), "1.0");
    }

    @Test
    public void getVersion_semanticVersion_parsedFully() {
        assertEquals(CanonicalUtils.getVersion("http://example.com/Library/lib|2.1.0-beta"), "2.1.0-beta");
    }

    @Test
    public void getVersion_canonicalType_works() {
        assertEquals(CanonicalUtils.getVersion(new CanonicalType(FULL_CANONICAL)), "1.0.0");
    }

    // ---- getFragment ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getFragment_null_throws() {
        CanonicalUtils.getFragment((String) null);
    }

    @Test
    public void getFragment_noHash_returnsNull() {
        assertNull(CanonicalUtils.getFragment(FULL_CANONICAL));
    }

    @Test
    public void getFragment_withFragment_returnsFragment() {
        assertEquals(CanonicalUtils.getFragment(FULL_CANONICAL_WITH_FRAGMENT), "cql");
    }

    @Test
    public void getFragment_fragmentOnly_returnsFragment() {
        assertEquals(CanonicalUtils.getFragment(FRAGMENT_ONLY), "cql");
    }

    @Test
    public void getFragment_canonicalType_works() {
        assertEquals(CanonicalUtils.getFragment(new CanonicalType(FULL_CANONICAL_WITH_FRAGMENT)), "cql");
    }

    // ---- getParts ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getParts_null_throws() {
        CanonicalUtils.getParts((String) null);
    }

    @Test
    public void getParts_fullCanonicalWithFragment_allPartsExtracted() {
        CanonicalUtils.CanonicalParts parts = CanonicalUtils.getParts(FULL_CANONICAL_WITH_FRAGMENT);
        assertEquals(parts.url(), "http://example.com/fhir/Library/my-lib");
        assertEquals(parts.idPart(), "my-lib");
        assertEquals(parts.resourceType(), "Library");
        assertEquals(parts.version(), "1.0.0");
        assertEquals(parts.fragment(), "cql");
    }

    @Test
    public void getParts_simpleCanonical_nullsForMissing() {
        CanonicalUtils.CanonicalParts parts = CanonicalUtils.getParts("http://example.com/fhir/Library/my-lib");
        assertEquals(parts.url(), "http://example.com/fhir/Library/my-lib");
        assertEquals(parts.idPart(), "my-lib");
        assertEquals(parts.resourceType(), "Library");
        assertNull(parts.version());
        assertNull(parts.fragment());
    }

    @Test
    public void getParts_canonicalType_works() {
        CanonicalUtils.CanonicalParts parts = CanonicalUtils.getParts(new CanonicalType(FULL_CANONICAL));
        assertEquals(parts.idPart(), "my-lib");
        assertEquals(parts.version(), "1.0.0");
    }

    @Test
    public void getParts_versionWithoutFragment_fragmentNull() {
        CanonicalUtils.CanonicalParts parts = CanonicalUtils.getParts(FULL_CANONICAL);
        assertEquals(parts.version(), "1.0.0");
        assertNull(parts.fragment());
    }

    @Test
    public void getParts_fragmentWithoutVersion_versionNull() {
        CanonicalUtils.CanonicalParts parts = CanonicalUtils.getParts(FRAGMENT_ONLY);
        assertNull(parts.version());
        assertEquals(parts.fragment(), "cql");
    }

    // ---- getHead ----

    @Test
    public void getHead_nullInput_returnsNull() {
        assertNull(CanonicalUtils.getHead(null));
    }

    @Test
    public void getHead_noSlash_returnsNull() {
        assertNull(CanonicalUtils.getHead("no-slash"));
    }

    @Test
    public void getHead_slashAtStart_returnsEmpty() {
        assertEquals(CanonicalUtils.getHead("/foo"), "");
    }

    @Test
    public void getHead_fullUrl_returnsEverythingBeforeLastSlash() {
        assertEquals(
                CanonicalUtils.getHead("http://example.com/fhir/Library/lib-id"), "http://example.com/fhir/Library");
    }

    @Test
    public void getHead_trailingSlash_stripsTrailingSlash() {
        assertEquals(CanonicalUtils.getHead("http://example.com/fhir/"), "http://example.com/fhir");
    }

    // ---- getTail ----

    @Test
    public void getTail_nullInput_returnsNull() {
        assertNull(CanonicalUtils.getTail(null));
    }

    @Test
    public void getTail_noSlash_returnsNull() {
        assertNull(CanonicalUtils.getTail("no-slash"));
    }

    @Test
    public void getTail_fullUrl_returnsLastSegment() {
        assertEquals(CanonicalUtils.getTail("http://example.com/fhir/Library/lib-id"), "lib-id");
    }

    @Test
    public void getTail_trailingSlash_returnsEmpty() {
        assertEquals(CanonicalUtils.getTail("http://example.com/fhir/"), "");
    }

    @Test
    public void getTail_slashAtStart_returnsEmpty() {
        assertEquals(CanonicalUtils.getTail("/"), "");
    }

    // ---- getId (legacy) ----

    @Test(expectedExceptions = NullPointerException.class)
    public void getId_null_throws() {
        CanonicalUtils.getId((String) null);
    }

    @Test
    public void getId_bareId_returnsId() {
        assertEquals(CanonicalUtils.getId("my-lib"), "my-lib");
    }

    @Test
    public void getId_withSlash_returnsLastSegment() {
        assertEquals(CanonicalUtils.getId("http://example.com/Library/my-lib"), "my-lib");
    }

    @Test
    public void getId_withVersion_stripsVersion() {
        assertEquals(CanonicalUtils.getId(FULL_CANONICAL), "my-lib");
    }

    @Test
    public void getId_withFragment_stripsFragment() {
        // Regression: getId used to only strip | but not #
        assertEquals(CanonicalUtils.getId("http://example.com/Library/my-lib#cql"), "my-lib",
                "getId should strip fragment, not return 'my-lib#cql'");
    }

    @Test
    public void getId_withVersionAndFragment_stripsBoth() {
        assertEquals(CanonicalUtils.getId(FULL_CANONICAL_WITH_FRAGMENT), "my-lib");
    }

    @Test
    public void getId_bareIdWithVersion_stripsVersion() {
        assertEquals(CanonicalUtils.getId("my-lib|1.0"), "my-lib");
    }

    @Test
    public void getId_bareIdWithFragment_stripsFragment() {
        assertEquals(CanonicalUtils.getId("my-lib#cql"), "my-lib");
    }

    @Test
    public void getId_canonicalType_extractsId() {
        CanonicalType ct = new CanonicalType("http://example.com/Library/my-lib|1.0");
        assertEquals(CanonicalUtils.getId(ct), "my-lib");
    }

    @Test(expectedExceptions = InvalidCanonical.class)
    public void getId_canonicalTypeNoValue_throws() {
        CanonicalUtils.getId(new CanonicalType());
    }

    // ---- getResourceName (deprecated, delegates to getResourceType) ----

    @Test
    public void getResourceName_fullUrl_returnsResourceType() {
        CanonicalType ct = new CanonicalType("http://example.com/fhir/Library/my-lib");
        assertEquals(CanonicalUtils.getResourceName(ct), "Library");
    }

    @Test
    public void getResourceName_noSlash_returnsNull() {
        CanonicalType ct = new CanonicalType("my-lib");
        assertNull(CanonicalUtils.getResourceName(ct));
    }

    @Test(expectedExceptions = InvalidCanonical.class)
    public void getResourceName_noValue_throws() {
        CanonicalUtils.getResourceName(new CanonicalType());
    }

    // ---- toVersionedIdentifier ----

    @Test(expectedExceptions = NullPointerException.class)
    public void toVersionedIdentifier_null_throws() {
        CanonicalUtils.toVersionedIdentifier(null);
    }

    @Test
    public void toVersionedIdentifier_libraryCanonical_parsesCorrectly() {
        VersionedIdentifier vi = CanonicalUtils.toVersionedIdentifier(FULL_CANONICAL);
        assertEquals(vi.getId(), "my-lib");
        assertEquals(vi.getVersion(), "1.0.0");
        assertEquals(vi.getSystem(), "http://example.com/fhir");
    }

    @Test
    public void toVersionedIdentifier_libraryNoVersion_versionIsNull() {
        VersionedIdentifier vi = CanonicalUtils.toVersionedIdentifier("http://example.com/fhir/Library/my-lib");
        assertEquals(vi.getId(), "my-lib");
        assertNull(vi.getVersion());
    }

    @Test(expectedExceptions = InvalidCanonical.class)
    public void toVersionedIdentifier_nonLibrary_throws() {
        CanonicalUtils.toVersionedIdentifier("http://example.com/fhir/Measure/my-measure");
    }

    @Test(expectedExceptions = InvalidCanonical.class)
    public void toVersionedIdentifier_minimalLibraryUrl_throwsWithoutBase() {
        // "Library/my-lib" has no base URL, so getTail(getHead()) can't find "Library"
        CanonicalUtils.toVersionedIdentifier("Library/my-lib|1.0");
    }

    // ---- toVersionedIdentifierAnyResource ----

    @Test(expectedExceptions = NullPointerException.class)
    public void toVersionedIdentifierAnyResource_null_throws() {
        CanonicalUtils.toVersionedIdentifierAnyResource(null);
    }

    @Test
    public void toVersionedIdentifierAnyResource_measure_parsesCorrectly() {
        VersionedIdentifier vi =
                CanonicalUtils.toVersionedIdentifierAnyResource("http://example.com/fhir/Measure/my-measure|2.0");
        assertEquals(vi.getId(), "my-measure");
        assertEquals(vi.getVersion(), "2.0");
        assertEquals(vi.getSystem(), "http://example.com/fhir");
    }

    @Test
    public void toVersionedIdentifierAnyResource_bareId_parsesCorrectly() {
        VersionedIdentifier vi = CanonicalUtils.toVersionedIdentifierAnyResource("my-lib");
        assertEquals(vi.getId(), "my-lib");
        assertNull(vi.getVersion());
        assertNull(vi.getSystem());
    }

    @Test
    public void toVersionedIdentifierAnyResource_withFragment_idExcludesFragment() {
        VersionedIdentifier vi =
                CanonicalUtils.toVersionedIdentifierAnyResource("http://example.com/fhir/Library/my-lib#cql");
        assertEquals(vi.getId(), "my-lib",
                "Fragment should not be included in the ID");
    }

    @Test
    public void toVersionedIdentifierAnyResource_trailingPipe_versionIsNull() {
        VersionedIdentifier vi =
                CanonicalUtils.toVersionedIdentifierAnyResource("http://example.com/fhir/Library/my-lib|");
        assertEquals(vi.getId(), "my-lib");
        assertNull(vi.getVersion(), "Empty version string should be normalized to null");
    }
}
