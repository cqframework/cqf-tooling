package org.opencds.cqf.tooling;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author Adam Stevenson
 */
public abstract class Operation {

    private String outputPath = Paths.get("src/main/resources/org/opencds/cqf/tooling/terminology/output").toAbsolutePath().toString();
    protected String getOutputPath() {
        return outputPath;
    }
    protected void setOutputPath(String outputPath) {
        this.outputPath = Paths.get(outputPath).toAbsolutePath().toString();
        File outputFile = new File(this.outputPath);
        outputFile.mkdirs();
        if (!outputFile.isDirectory()) {
            throw new IllegalArgumentException(String.format("Specified output path is not a directory: %s", outputPath));
        }
    }

    public abstract void execute(String[] args);
}
