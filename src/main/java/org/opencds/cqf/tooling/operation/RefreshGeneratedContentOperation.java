package org.opencds.cqf.tooling.operation;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;

public abstract class RefreshGeneratedContentOperation extends Operation {
    private String igPath;
    private FhirContext fhirContext;
    private String pathToMeasures; // -ptm
    private String pathToLibraries; // -ptl
    private String operationName;

    private String encoding; // -e (json|xml)

    public RefreshGeneratedContentOperation(String outputPath, String operationName, FhirContext fhirContext) {
        setOutputPath(outputPath);
        this.operationName = operationName;
        this.fhirContext = fhirContext;
    }

    public RefreshGeneratedContentOperation(String outputPath, String operationName, FhirContext fhirContext, String pathToLibraries, String pathToMeasures) {
        setOutputPath(outputPath);
        this.operationName = operationName;
        this.fhirContext = fhirContext;
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

    public void output(IBaseResource resource, IOUtils.Encoding encoding) {
        IOUtils.writeResource(resource, pathToMeasures, encoding, fhirContext);
    }

    public abstract void refreshGeneratedContent();

    public String getPathToMeasures() {
        return pathToMeasures;
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }

    public String getPathToLibraries() {
        return pathToLibraries;
    }

    public String getEncoding() {
        return encoding;
    }
}
