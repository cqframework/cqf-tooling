package org.opencds.cqf.utilities;

import ca.uhn.fhir.context.RuntimeResourceDefinition;

public class IGUtils {
    public static String getImplementationGuideCanonicalBase(String url) {
        String canonicalBase = null;

        if (url != null && !url.isEmpty() && !url.isBlank()) {
            canonicalBase = url.substring(0, url.indexOf("/ImplementationGuide/"));
        }

        return canonicalBase;
    }
}
