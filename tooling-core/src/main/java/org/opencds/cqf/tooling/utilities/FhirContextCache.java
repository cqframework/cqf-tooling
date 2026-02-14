package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class FhirContextCache {

    private static final Map<FhirVersionEnum, FhirContext> contextCache = new EnumMap<>(FhirVersionEnum.class);

    private FhirContextCache() {}

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
        contextCache.computeIfAbsent(fhirVersion, k -> FhirContext.forVersion(fhirVersion));
        return contextCache.get(fhirVersion);
    }
}
