package org.opencds.cqf.tooling.utilities.converters;

import joptsimple.ValueConverter;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

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
