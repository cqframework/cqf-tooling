package org.opencds.cqf.individual_tooling.cql_generation.drool.serialization;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

public class UnixTimeStampDeserializer extends DateDeserializer {

    private static final long serialVersionUID = -2275951539867772400L;

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        if (jp.getCurrentTokenId() == JsonTokenId.ID_NUMBER_INT) {
            try {

                BigInteger dateLong = jp.getBigIntegerValue();
                
                Instant instant = Instant.ofEpochMilli(dateLong.longValue());
                return Date.from(instant);
            } catch (Exception e) {
                return super.deserialize(jp, ctxt);
            }
        } else {
            return super.deserialize(jp, ctxt);
        }
    }
}
