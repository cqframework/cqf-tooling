package org.opencds.cqf.tooling.utilities;

public class IGUtils {
    public static String getImplementationGuideCanonicalBase(String url) {
        String canonicalBase = null;

        if (url != null && !url.isEmpty()) {
            canonicalBase = url.substring(0, url.indexOf("/ImplementationGuide/"));
        }

        return canonicalBase;
    }
}
