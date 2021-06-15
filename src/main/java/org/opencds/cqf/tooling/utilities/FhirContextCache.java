package org.opencds.cqf.tooling.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
/**
 * @author Adam Stevenson
 */
public class FhirContextCache {

    private final static Map<FhirVersionEnum, FhirContext> contextCache = new HashMap<>();

    /**
     * @param fhirVersion The FHIR version to get a context for (e.g. "DSTU3", "4.0", etc.)
     * @return A FhirContext that corresponds to the fhirVersion
     */
    public static FhirContext getContext(String fhirVersion) {
        Objects.requireNonNull(fhirVersion, "fhirVersion can not be null");

        FhirVersionEnum versionEnum = FhirVersionUtils.getVersionEnum(fhirVersion);
        if (versionEnum == null) {
            throw new IllegalArgumentException(String.format("Unable to resolve FHIR version: %s", fhirVersion));
        }

        return getContext(versionEnum);
    }

    /**
     * Fetches a FhirContext from a cache. If a context for a given version doesn't exist, one is created.
     * 
     * @param fhirVersion The FHIR version to get a context for
     * @return A FhirContext that corresponds to the fhirVersion
     */
    public static synchronized FhirContext getContext(FhirVersionEnum fhirVersion) {
        Objects.requireNonNull(fhirVersion, "fhirVersion can not be null");

        if (!contextCache.containsKey(fhirVersion)) {
            contextCache.put(fhirVersion, fhirVersion.newContext());
        }

        return contextCache.get(fhirVersion);
    }
}