package org.opencds.cqf.tooling.npm;

import java.util.Objects;

public class NamespaceInfo extends org.hl7.cql.model.NamespaceInfo {
    private String version;
    public NamespaceInfo(String name, String uri, String version) {
        super(name, uri);
        if (version != null && !version.isEmpty()) {
            this.version = version;
        } else {
            throw new IllegalArgumentException("Version is required");
        }
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version);
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof NamespaceInfo) {
            NamespaceInfo thatInfo = (NamespaceInfo)that;
            return this.getName().equals(thatInfo.getName()) && this.getUri().equals(thatInfo.getUri())
                    && this.getVersion().equals(thatInfo.getVersion());
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: %s|%s", getName(), getUri(), getVersion());
    }
}
