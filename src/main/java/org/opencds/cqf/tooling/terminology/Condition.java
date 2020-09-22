package org.opencds.cqf.tooling.terminology;

public class Condition {
    private String displayName;
    private String jurisdictionCode;

    public String getName() {
        return displayName;
    }

    public String getJurisdiction() {
        return jurisdictionCode;
    }

    public void setName(String displayName) {
        this.displayName = displayName;
    }

    public void setJurisdiction(String jurisdictionCode) {
        this.jurisdictionCode = jurisdictionCode;
    }

    @Override
    public String toString() {
        return getName() + ", " + getJurisdiction();
    }
}
