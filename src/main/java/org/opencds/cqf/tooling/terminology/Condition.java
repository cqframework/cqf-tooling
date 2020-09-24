package org.opencds.cqf.tooling.terminology;

public class Condition {
    private CdsCodeDTO cdsCodeDTO;
    private String jurisdictionIdentifier;
    private String version;

    public CdsCodeDTO getCdsCodeDTO() {
        return cdsCodeDTO;
    }

    public String getJurisdictionIdentifier() {
        return jurisdictionIdentifier;
    }

    public String getVersion() {
        return version;
    }

    public void setCdsCodeDTO(CdsCodeDTO cdsCodeDTO) {
        this.cdsCodeDTO = cdsCodeDTO;
    }

    public void setJurisdictionIdentifier(String jurisdictionIdentifier) {
        this.jurisdictionIdentifier = jurisdictionIdentifier;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cdsCodeDTO: " + getCdsCodeDTO() + "\n");
        sb.append("responsibleAgencies: " + getJurisdictionIdentifier() + "\n");
        sb.append("version: " + getVersion() + "\n");
        return sb.toString();
    }
}
