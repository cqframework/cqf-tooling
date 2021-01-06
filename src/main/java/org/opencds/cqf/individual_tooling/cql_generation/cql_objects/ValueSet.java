package org.opencds.cqf.individual_tooling.cql_generation.cql_objects;

public class ValueSet {
    String alias;
    String url;

    public ValueSet(String alias, String url) {
        this.alias = alias;
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "valueset \"" + alias + "\": \'" + url + "\'\n";
    }
}
