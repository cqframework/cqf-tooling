package org.opencds.cqf.tooling.acceleratorkit.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class JsonFlatMapper {

    private JsonFlatMapper() {
        throw new AssertionError("Can't instantiate");
    }

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .filter(objectEntry -> {
                    String key = objectEntry.getKey();
                    if (checkForIgnoredFields(key)) return false;
                    return true;
                })
                .flatMap(JsonFlatMapper::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static boolean checkForIgnoredFields(String key) {
        List<String> ignoreList = Arrays.asList("date", "timestamp", "valueDateTime", "start", "end", "birthDate");
        if(ignoreList.contains(key)){
            return true;
        }
        return false;
    }

    private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) entry.getValue();
            return properties.entrySet()
                    .stream()
                    .filter(e -> {
                        String key = (String) e.getKey();
                        if (checkForIgnoredFields(key)) return false;
                        return true;
                    })
                    .flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
                    .flatMap(JsonFlatMapper::flatten);
        }

        return Stream.of(entry);
    }
}
