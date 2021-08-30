package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.Resource;

import java.util.List;

public interface CanonicalResourceProvider<T extends Resource> {
    Iterable<T> get();
    Iterable<T> getByCanonicalUrl(String url);
    T getByCanonicalUrlWithVersion(String urlWithVersion);
    T getByCanonicalUrlAndVersion(String url, String version);
}
