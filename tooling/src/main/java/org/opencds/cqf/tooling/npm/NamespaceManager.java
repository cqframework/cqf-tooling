package org.opencds.cqf.tooling.npm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamespaceManager extends org.hl7.cql.model.NamespaceManager {
    private final Map<String, List<NamespaceInfo>> namespaces;

    public NamespaceManager() {
        this.namespaces = new HashMap<>();
    }

    @Override
    public boolean hasNamespaces() {
        return !this.namespaces.isEmpty();
    }

    @Override
    public void ensureNamespaceRegistered(org.hl7.cql.model.NamespaceInfo namespaceInfo) {
        if (namespaceInfo == null) {
            throw new IllegalArgumentException("namespaceInfo is required");
        }

        if (!namespaces.containsKey(namespaceInfo.getName())) {
            if (namespaceInfo instanceof NamespaceInfo) {
                addNamespace(namespaceInfo.getName(), namespaceInfo.getUri(), ((NamespaceInfo) namespaceInfo).getVersion());
            } else {
                addNamespace(namespaceInfo.getName(), namespaceInfo.getUri());
            }
        }
    }

    @Override
    public void addNamespace(org.hl7.cql.model.NamespaceInfo namespaceInfo) {
        if (namespaceInfo == null) {
            throw new IllegalArgumentException("namespaceInfo is required");
        }

        if (namespaceInfo instanceof NamespaceInfo) {
            addNamespace(namespaceInfo.getName(), namespaceInfo.getUri(), ((NamespaceInfo) namespaceInfo).getVersion());
        } else {
            addNamespace(namespaceInfo.getName(), namespaceInfo.getUri());
        }
    }

    public void addNamespace(String name, String uri, String version) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Namespace name is required");
        }

        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Namespace uri is required");
        }

        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Namespace version is required");
        }

        namespaces.computeIfAbsent(name, s -> new ArrayList<>()).add(new NamespaceInfo(name, uri, version));
    }

    public String resolveNamespaceUri(String name, String version) {
        if (namespaces.containsKey(name)) {
            return namespaces.get(name).stream().filter(x -> x.getVersion().equals(version)).findFirst().orElseThrow().getUri();
        }

        return null;
    }

    public org.hl7.cql.model.NamespaceInfo getNamespaceInfoFromUri(String uri, String version) {
        return namespaces.values().stream().flatMap(List::stream).filter(
                x -> x.getUri().equals(uri) && x.getVersion().equals(version)).findFirst().orElseThrow();
    }
}
