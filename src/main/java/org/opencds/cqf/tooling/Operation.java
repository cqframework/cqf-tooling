package org.opencds.cqf.tooling;

import java.nio.file.Paths;

public abstract class Operation {

    private String outputPath = Paths.get("src/main/resources/org.opencds.cqf.qdm.output").toAbsolutePath().toString();
    protected String getOutputPath() {
        return outputPath;
    }
    protected void setOutputPath(String outputPath) {
        this.outputPath = Paths.get(outputPath).toAbsolutePath().toString();
    }

    public abstract void execute(String[] args);
}
