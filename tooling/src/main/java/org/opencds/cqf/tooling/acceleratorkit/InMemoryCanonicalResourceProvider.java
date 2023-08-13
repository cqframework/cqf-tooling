package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;

import java.util.*;

public class InMemoryCanonicalResourceProvider<T extends Resource> implements CanonicalResourceProvider<T> {

    private Map<String, T> resources;

    public InMemoryCanonicalResourceProvider(Map<String, T> resources) {
        this.resources = resources;
    }

    public InMemoryCanonicalResourceProvider(Collection<T> resources) {
        this.resources = new HashMap<String, T>();
        for (T r : resources) {
            List<Base> b = r.getNamedProperty("url").getValues();
            if (b == null || b.isEmpty()) {
                throw new IllegalArgumentException("Resources used in a canonical resource provider must have a url");
            }
            // TODO: Append version string
            String url = b.get(0).primitiveValue();
            this.resources.put(url, r);
        }
    }

    @Override
    public Iterable<T> get() {
        return this.resources.values();
    }

    @Override
    public Iterable<T> getByCanonicalUrl(String url) {
        // TODO: Support versioned urls in the map
        T result = resources.get(url);
        if (result != null) {
            return Collections.singletonList(result);
        }
        return Collections.emptyList();
    }

    @Override
    public T getByCanonicalUrlWithVersion(String url) {
        return resources.get(url);
    }

    @Override
    public T getByCanonicalUrlAndVersion(String url, String version) {
        // TODO: Support versioned urls in the map
        return resources.get(url);
    }
}
