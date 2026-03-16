package org.opencds.cqf.tooling.utilities;

import java.util.Objects;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.opencds.cqf.tooling.exception.InvalidCanonical;

/**
 * Utility class for parsing FHIR canonical URLs into their constituent components.
 *
 * <p>A canonical URL has the general form:
 * <pre>  [base]/[ResourceType]/[id]|[version]#[fragment]</pre>
 *
 * <p>This class is aligned with {@code org.opencds.cqf.fhir.utility.Canonicals} from the
 * clinical-reasoning project and additionally provides {@link #toVersionedIdentifier} and
 * {@link #toVersionedIdentifierAnyResource} for CQL ELM integration.
 */
public class CanonicalUtils {

    private CanonicalUtils() {}

    // ---- URL component extraction (aligned with Canonicals) ----

    /**
     * Gets the URL component of a canonical url — everything before the version and fragment.
     * Includes the base url, resource type, and id if present.
     *
     * @return the URL, or null if the canonical contains no path separator and is not a URN
     */
    public static String getUrl(String canonical) {
        Objects.requireNonNull(canonical);
        if (!canonical.contains("/") && !canonical.startsWith("urn:uuid") && !canonical.startsWith("urn:oid")) {
            return null;
        }
        return canonical.substring(0, calculateLastIndex(canonical));
    }

    public static <T extends IPrimitiveType<String>> String getUrl(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "url extraction");
        return getUrl(canonicalType.getValue());
    }

    /**
     * Gets the Resource type component of a canonical url.
     *
     * @return the resource type, or null if one cannot be parsed
     */
    public static String getResourceType(String canonical) {
        Objects.requireNonNull(canonical);
        if (!canonical.contains("/")) {
            return null;
        }
        int lastSlash = canonical.lastIndexOf("/");
        String withoutTail = canonical.substring(0, lastSlash);
        return withoutTail.contains("/") ? withoutTail.substring(withoutTail.lastIndexOf("/") + 1) : withoutTail;
    }

    public static <T extends IPrimitiveType<String>> String getResourceType(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "resource type extraction");
        return getResourceType(canonicalType.getValue());
    }

    /**
     * Gets the ID component of a canonical url. Requires a path separator; returns null for
     * bare identifiers. Strips version and fragment.
     *
     * @return the id, or null if the canonical contains no path separator
     */
    public static String getIdPart(String canonical) {
        Objects.requireNonNull(canonical);
        if (!canonical.contains("/")) {
            return null;
        }
        int lastIndex = calculateLastIndex(canonical);
        return canonical.substring(canonical.lastIndexOf("/") + 1, lastIndex);
    }

    public static <T extends IPrimitiveType<String>> String getIdPart(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "id extraction");
        return getIdPart(canonicalType.getValue());
    }

    /**
     * Gets the Version component of a canonical url. Strips any trailing fragment.
     *
     * @return the version, or null if no version separator is present
     */
    public static String getVersion(String canonical) {
        Objects.requireNonNull(canonical);
        if (!canonical.contains("|")) {
            return null;
        }
        int lastIndex = canonical.lastIndexOf("#");
        if (lastIndex == -1) {
            lastIndex = canonical.length();
        }
        return canonical.substring(canonical.lastIndexOf("|") + 1, lastIndex);
    }

    public static <T extends IPrimitiveType<String>> String getVersion(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "version extraction");
        return getVersion(canonicalType.getValue());
    }

    /**
     * Gets the Fragment component of a canonical url.
     *
     * @return the fragment, or null if no fragment separator is present
     */
    public static String getFragment(String canonical) {
        Objects.requireNonNull(canonical);
        if (!canonical.contains("#")) {
            return null;
        }
        return canonical.substring(canonical.lastIndexOf("#") + 1);
    }

    public static <T extends IPrimitiveType<String>> String getFragment(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "fragment extraction");
        return getFragment(canonicalType.getValue());
    }

    /**
     * Parses a canonical url into all of its constituent parts.
     */
    public static CanonicalParts getParts(String canonical) {
        Objects.requireNonNull(canonical);
        return new CanonicalParts(
                getUrl(canonical),
                getIdPart(canonical),
                getResourceType(canonical),
                getVersion(canonical),
                getFragment(canonical));
    }

    public static <T extends IPrimitiveType<String>> CanonicalParts getParts(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "parts extraction");
        return getParts(canonicalType.getValue());
    }

    // ---- Path splitting utilities ----

    /**
     * Returns everything before the last path separator.
     *
     * @return the head, empty string if the separator is at position 0, or null if absent or input is null
     */
    public static String getHead(String url) {
        if (url == null) {
            return null;
        }
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(0, index);
        } else {
            return "";
        }
    }

    /**
     * Returns everything after the last path separator.
     *
     * @return the tail, empty string if the separator is at position 0, or null if absent or input is null
     */
    public static String getTail(String url) {
        if (url == null) {
            return null;
        }
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(index + 1);
        } else {
            return "";
        }
    }

    // ---- Legacy methods (backward compatibility) ----

    /**
     * Gets the ID component of a canonical url. Unlike {@link #getIdPart}, this method accepts
     * bare identifiers without a path separator, returning them directly.
     */
    public static String getId(String url) {
        Objects.requireNonNull(url);
        String temp = url.contains("/") ? url.substring(url.lastIndexOf("/") + 1) : url;
        return temp.split("[|#]")[0];
    }

    public static <T extends IPrimitiveType<String>> String getId(T canonicalType) {
        Objects.requireNonNull(canonicalType);
        requireValue(canonicalType, "id extraction");
        return getId(canonicalType.getValue());
    }

    /**
     * @deprecated Use {@link #getResourceType(IPrimitiveType)} instead
     */
    @Deprecated
    public static <T extends IPrimitiveType<String>> String getResourceName(T canonicalType) {
        return getResourceType(canonicalType);
    }

    // ---- CQL ELM integration (unique to this codebase) ----

    /**
     * Parses a Library canonical url into a CQL {@link VersionedIdentifier}.
     *
     * @throws InvalidCanonical if the canonical is not a Library resource
     */
    public static VersionedIdentifier toVersionedIdentifier(String url) {
        Objects.requireNonNull(url);
        String version = getVersion(url);
        if ("".equals(version)) {
            version = null;
        }
        String id = getId(url);
        String head = getHead(url);
        String resourceName = getTail(head);
        if (resourceName == null || !resourceName.equals("Library")) {
            throw new InvalidCanonical("Cannot extract versioned identifier from a non-library canonical");
        }
        // getHead(head) cannot return "" here: that would require head to start with "/",
        // which would make getTail(head) return "" above, triggering the InvalidCanonical throw.
        String base = getHead(head);

        return new VersionedIdentifier().withSystem(base).withId(id).withVersion(version);
    }

    /**
     * Parses a canonical url for any resource type into a CQL {@link VersionedIdentifier}.
     */
    public static VersionedIdentifier toVersionedIdentifierAnyResource(String url) {
        Objects.requireNonNull(url);
        String version = getVersion(url);
        if ("".equals(version)) {
            version = null;
        }
        String id = getId(url);
        String head = getHead(url);
        String base = null;
        if (head != null) {
            base = getHead(head);
        }
        if ("".equals(base)) {
            base = null;
        }

        return new VersionedIdentifier().withSystem(base).withId(id).withVersion(version);
    }

    // ---- Internal helpers ----

    private static int calculateLastIndex(String canonical) {
        int lastIndexOfBar = canonical.lastIndexOf("|");
        int lastIndexOfHash = canonical.lastIndexOf("#");

        if (lastIndexOfBar >= 0 && lastIndexOfHash >= 0) {
            return Math.min(lastIndexOfBar, lastIndexOfHash);
        } else if (lastIndexOfBar >= 0) {
            return lastIndexOfBar;
        } else if (lastIndexOfHash >= 0) {
            return lastIndexOfHash;
        }
        return canonical.length();
    }

    private static void requireValue(IPrimitiveType<String> canonicalType, String operation) {
        if (!canonicalType.hasValue()) {
            throw new InvalidCanonical("CanonicalType must have a value for " + operation);
        }
    }

    // ---- CanonicalParts ----

    public static final class CanonicalParts {
        private final String url;
        private final String idPart;
        private final String resourceType;
        private final String version;
        private final String fragment;

        CanonicalParts(String url, String idPart, String resourceType, String version, String fragment) {
            this.url = url;
            this.idPart = idPart;
            this.resourceType = resourceType;
            this.version = version;
            this.fragment = fragment;
        }

        public String url() {
            return url;
        }

        public String idPart() {
            return idPart;
        }

        public String resourceType() {
            return resourceType;
        }

        public String version() {
            return version;
        }

        public String fragment() {
            return fragment;
        }
    }
}
