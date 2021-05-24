package org.opencds.cqf.tooling.utilities.converters;

import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.ValueConverter;

public class EncodingConverter implements ValueConverter<Encoding> {

    @Override
    public Encoding convert(String value) {
        return Encoding.parse(value);
    }

    @Override
    public Class<? extends Encoding> valueType() {
        return Encoding.class;
    }

    @Override
    public String valuePattern() {
        return "JSON|XML|CQL";
    }
}