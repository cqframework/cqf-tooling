package org.opencds.cqf.tooling.utilities;

import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r4.model.CanonicalType;
import org.opencds.cqf.tooling.exception.InvalidCanonical;

public class CanonicalUtils {

    private CanonicalUtils() {}

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

    public static String getVersion(String url) {
        int index = url.lastIndexOf("|");
        if (index == -1) {
            return null;
        }
        else if (index > 0) {
            return url.substring(index + 1);
        }
        else {
            return "";
        }
    }

    public static String getId(String url) {
        String temp = url.contains("/") ? url.substring(url.lastIndexOf("/") + 1) : url;
        return temp.split("\\|")[0];
    }

    public static String getId(CanonicalType canonical) {
        if (canonical.hasValue()) {
            return getId(canonical.getValue());
        }

        throw new InvalidCanonical("CanonicalType must have a value for id extraction");
    }

    public static String getResourceName(CanonicalType canonical) {
        if (canonical.hasValue()) {
            String id = canonical.getValue();
            if (id.contains("/")) {
                id = id.replace(id.substring(id.lastIndexOf("/")), "");
                return id.contains("/") ? id.substring(id.lastIndexOf("/") + 1) : id;
            }
            return null;
        }

        throw new InvalidCanonical("CanonicalType must have a value for resource name extraction");
    }

    public static VersionedIdentifier toVersionedIdentifier(String url) {
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
        String base = getHead(head);
        if ("".equals(base)) {
            base = null;
        }

        return new VersionedIdentifier().withSystem(base).withId(id).withVersion(version);
    }

    public static VersionedIdentifier toVersionedIdentifierAnyResource(String url) {
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
}
