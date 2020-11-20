package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class DefineStatement {
    private String alias;

    public DefineStatement(String alias) {
        this.alias = alias;
    }

    public DefineStatement() {

    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        String content = "\ndefine \"" + this.alias + "\":\n";
        return content;
    }
}
