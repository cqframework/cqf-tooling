package org.opencds.cqf.measure;

import org.opencds.cqf.Operation;

public abstract class RefreshGeneratedContent extends Operation {

    private String pathToMeasures; // -ptm
    private String pathToLibraries; // -ptl
    private String operationName;

    private String encoding; // -e (json|xml)

    public RefreshGeneratedContent(String outputPath, String operationName) {
        setOutputPath(outputPath);
        this.operationName = operationName;
    }

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals(operationName)) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "pathtomeasures":
                case "ptm":
                    pathToMeasures = value;
                    break;
                case "pathToLibraries":
                case "ptl":
                    pathToLibraries = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }
    }

    public abstract void refreshGeneratedContent();
}
