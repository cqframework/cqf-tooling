package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase;

public enum OutputPath {

    PATIENT("Patient/");

    private String output;

    OutputPath(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

}
