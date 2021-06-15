package org.opencds.cqf.tooling.cql_generation.drool.serialization;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * @author Rckms
 */
@SuppressWarnings("unchecked")
public class PairJacksonProvider extends JsonDeserializer<Pair<Object, Object>> {
        @Override
        public Pair<Object, Object> deserialize( JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            final Object object = jsonParser.readValueAs(Object.class);
            if (object instanceof LinkedHashMap) {
                LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) object;
                List<Pair<Object, Object>> pairs = new ArrayList<Pair<Object, Object>>();
                for (Entry<String, String> entry : map.entrySet()) {
                    pairs.add(Pair.of(entry.getKey(), entry.getValue()));
                }
                if (pairs.size() == 1) {
                    return pairs.get(0);
                } else {
                    throw new RuntimeException("Expected 1 entry per pair, found " + pairs.size());
                }
            } else {
                throw new RuntimeException("Expected LinkedHashMap but found: " + object);
            }
        }
}
