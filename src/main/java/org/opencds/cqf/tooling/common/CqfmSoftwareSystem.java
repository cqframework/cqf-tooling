package org.opencds.cqf.tooling.common;

public class CqfmSoftwareSystem {
    protected String name;
    public String getName() { return this.name; }

    protected String version;
    public String getVersion() { return this.version; }

    public CqfmSoftwareSystem(String name, String version) {
        this.name = name;
        this.version = version;
    }
}
