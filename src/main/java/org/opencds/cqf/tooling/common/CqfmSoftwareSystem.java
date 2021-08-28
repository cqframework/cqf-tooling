package org.opencds.cqf.tooling.common;


public class CqfmSoftwareSystem {
    protected String name;
    public String getName() { return this.name; }

    protected String version;
    public String getVersion() { return this.version; }

    protected String manufacturer;
    public String getManufacturer() { return this.manufacturer; }

    public CqfmSoftwareSystem(String name, String version, String manufacturer) {
        this.name = name;
        this.version = version;
        this.manufacturer = manufacturer;

        if (this.version == null) {
            this.version = "1.3.1-SNAPSHOT";
        }

        if (this.manufacturer == null) {
            this.manufacturer = "CQFramework";
        }
    }
}
