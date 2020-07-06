package org.opencds.cqf.measure;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.opencds.cqf.Operation;
import org.opencds.cqf.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public abstract class RefreshGeneratedContent extends Operation {

    private FhirContext context;
    private String pathToMeasures; // -ptm
    private String pathToLibraries; // -ptl
    private String operationName;

    private String encoding; // -e (json|xml)

    public RefreshGeneratedContent(String outputPath, String operationName, FhirContext context) {
        setOutputPath(outputPath);
        this.operationName = operationName;
        this.context = context;
    }

    public RefreshGeneratedContent(String outputPath, String operationName, FhirContext context, String pathToLibraries, String pathToMeasures) {
        setOutputPath(outputPath);
        this.operationName = operationName;
        this.context = context;
        this.pathToLibraries = pathToLibraries;
        this.pathToMeasures = pathToMeasures;
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

        refreshGeneratedContent();
    }

    public void output(IAnyResource resource, IOUtils.Encoding encoding) {
        IOUtils.writeResource(resource, pathToMeasures, encoding, context);
    }

    public abstract void refreshGeneratedContent();

    public String getPathToMeasures() {
        return pathToMeasures;
    }

    public FhirContext getContext() {
        return context;
    }

    public String getPathToLibraries() {
        return pathToLibraries;
    }

    public String getEncoding() {
        return encoding;
    }
}
