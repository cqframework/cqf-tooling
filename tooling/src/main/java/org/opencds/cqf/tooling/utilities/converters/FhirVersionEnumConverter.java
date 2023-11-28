package org.opencds.cqf.tooling.utilities.converters;

import java.util.Objects;

import ca.uhn.fhir.context.FhirVersionEnum;
import joptsimple.ValueConverter;

public class FhirVersionEnumConverter implements ValueConverter<FhirVersionEnum> {

    @Override
    public FhirVersionEnum convert(String value) {
        Objects.requireNonNull(value, "value can not be null");

        switch(value.trim().toUpperCase()) {
            case "DSTU3": return FhirVersionEnum.DSTU3;
            case "R4": return FhirVersionEnum.R4;
            case "R5": return FhirVersionEnum.R5;
            default: throw new IllegalArgumentException(String.format("unknown or unsupported FHIR version %s", value));
        }
    }

    @Override
    public Class<? extends FhirVersionEnum> valueType() {
        return FhirVersionEnum.class;
    }

    @Override
    public String valuePattern() {
        return "DSTU3|R4";
    }
}