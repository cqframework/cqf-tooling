package org.opencds.cqf.utilities;

public class IGUtils {
    public static String getStu3ImplementationGuideCanonicalBase(org.hl7.fhir.dstu3.model.ImplementationGuide ig) {
        String canonicalBase = null;
        if (ig != null) {
            String url = ig.getUrl();
            canonicalBase = url.substring(0, url.indexOf("/ImplementationGuide/"));
        }
        return canonicalBase;
    }

    public static String getR4ImplementationGuideCanonicalBase(org.hl7.fhir.r4.model.ImplementationGuide ig) {
        String canonicalBase = null;
        if (ig != null) {
            String url = ig.getUrl();
            canonicalBase = url.substring(0, url.indexOf("/ImplementationGuide/"));
        }
        return canonicalBase;
    }
}
