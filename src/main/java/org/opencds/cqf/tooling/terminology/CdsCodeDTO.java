package org.opencds.cqf.tooling.terminology;

public class CdsCodeDTO {
    private String code;
    private String codeSystem;
    private String displayName;

    public String getCode() {
        return code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return getCode() + ", " + getCodeSystem() + ", " + getDisplayName();
    }
}
