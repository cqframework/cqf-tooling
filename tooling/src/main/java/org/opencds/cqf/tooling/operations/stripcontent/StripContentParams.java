package org.opencds.cqf.tooling.operations.stripcontent;

public class StripContentParams {

    private String inputPath;
    private String outputPath;
    private String version;

    public String inputPath() {
        return inputPath;
    }

    public StripContentParams inputPath(String inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    public String outputPath() {
        return outputPath;
    }

    public StripContentParams outputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    public String version() {
        return version;
    }

    public StripContentParams version(String version) {
        this.version = version;
        return this;
    }
}
