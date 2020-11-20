package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class DirectReferenceCode {
    private String alias;
    private String code;

    public DirectReferenceCode(String alias, String code) {
        this.alias = alias;
        this.code = code;
    }

    public DirectReferenceCode() {}

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        String content = "code " + "\"" + alias + "\": " + "'" + code + "'\n";
        return content;
    }
}
