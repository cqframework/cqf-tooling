package org.opencds.cqf.tooling.operations.stripcontent;

public class StripContentParams {

    private String inputDirectory;
    private String outputDirectory;
    private String fhirVersion;
    private String cqlExportDirectory;

    public String inputDirectory() {
        return inputDirectory;
    }

    public StripContentParams inputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
        return this;
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    public StripContentParams outputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public String fhirVersion() {
        return fhirVersion;
    }

    public StripContentParams fhirVersion(String fhirVersion) {
        this.fhirVersion = fhirVersion;
        return this;
    }

    public String cqlExportDirectory() {
        return cqlExportDirectory;
    }

    public StripContentParams cqlExportDirectory(String cqlExportDirectory) {
        this.cqlExportDirectory = cqlExportDirectory;
        return this;
    }
}
