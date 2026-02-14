package org.opencds.cqf.tooling.utilities;

import java.util.Objects;

import ca.uhn.fhir.context.FhirVersionEnum;

public class FhirVersionUtils {

    private FhirVersionUtils() {}

    /**
     * This method handles numeric versions (3, 2.0, 4.0.1, etc.) and release versions (R4, DSTU3, etc.)
     * Partial versions are allowed. The minimum compatible version supported by the tooling  returned 
     * in the event of a partial version.
     * 
     * If an exact version is specified (e.g. "4.0.1") that is not supported by the tooling null is returned.
     *  
     * @param fhirVersion String representing the version of FHIR (e.g. "DSTU3", "2.0", etc.)
     * @return FhirContext corresponding to fhirVersion
     */
    public static FhirVersionEnum getVersionEnum(String fhirVersion) {
        Objects.requireNonNull(fhirVersion, "fhirVersion can not be null");

        fhirVersion = fhirVersion.trim().toUpperCase();

        FhirVersionEnum versionEnum = FhirVersionEnum.forVersionString(fhirVersion);
        if (versionEnum != null) {
            return versionEnum;
        }

        for (FhirVersionEnum value : FhirVersionEnum.values()) {
            if(value.name().equals(fhirVersion)){
                return value;
            }

        }

        return null;
    }
}