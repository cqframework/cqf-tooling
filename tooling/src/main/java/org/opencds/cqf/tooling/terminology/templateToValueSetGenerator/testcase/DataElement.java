package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase;


public class DataElement {
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    private String id;
    private String label;
    private String value;
}
