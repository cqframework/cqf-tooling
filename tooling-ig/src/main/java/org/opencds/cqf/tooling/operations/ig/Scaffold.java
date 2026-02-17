package org.opencds.cqf.tooling.operations.ig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.parameter.ScaffoldParameters;
import org.opencds.cqf.tooling.processor.ScaffoldProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

@Operation(name = "ScaffoldIG")
public class Scaffold implements ExecutableOperation {

    @OperationParam(
            alias = {"ip", "ig-path"},
            setter = "setIgPath",
            required = true,
            description = "Root directory of the IG")
    private String igPath;

    @OperationParam(
            alias = {"iv", "ig-version"},
            setter = "setIgVersion",
            description = "The desired FHIR version")
    private String igVersion;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setOutputEncoding",
            defaultValue = "json",
            description = "JSON|XML - If omitted, output will be generated using JSON encoding")
    private String outputEncoding;

    @OperationParam(
            alias = {"rn", "resource-name"},
            setter = "setResourceNames",
            description = "Use multiple times to define names of resources that should be created")
    private String resourceNames;

    @OperationParam(
            alias = {"software"},
            setter = "setSoftwareSystems",
            description = "Use multiple times to define multiple software systems (format: Name=Version)")
    private String softwareSystems;

    @Override
    public void execute() {
        IOUtils.Encoding outputEncodingEnum = IOUtils.Encoding.JSON;
        if (outputEncoding != null) {
            outputEncodingEnum = IOUtils.Encoding.parse(outputEncoding.toLowerCase());
        }

        List<String> resourceTypesToCreate = new ArrayList<>();
        resourceTypesToCreate.add("Library");
        resourceTypesToCreate.add("Measure");

        Map<String, List<String>> resourcesToCreate = new HashMap<>();
        if (resourceNames != null && !resourceNames.isEmpty()) {
            for (String name : resourceNames.split(",")) {
                resourcesToCreate.put(name.trim(), resourceTypesToCreate);
            }
        }

        List<org.opencds.cqf.tooling.common.SoftwareSystem> softwareSystemsList = new ArrayList<>();
        if (softwareSystems != null && !softwareSystems.isEmpty()) {
            for (String system : softwareSystems.split(",")) {
                String[] parts = system.trim().split("=");
                String name = parts[0];
                String version = parts.length > 1 ? parts[1] : "";
                softwareSystemsList.add(
                        new org.opencds.cqf.tooling.common.SoftwareSystem(name, version, "CQFramework"));
            }
        }

        ScaffoldParameters params = new ScaffoldParameters();
        params.igPath = igPath;
        params.igVersion = igVersion;
        params.outputEncoding = outputEncodingEnum;
        params.resourcesToScaffold = resourcesToCreate;
        params.softwareSystems = softwareSystemsList;

        ScaffoldProcessor scaffoldProcessor = new ScaffoldProcessor();
        scaffoldProcessor.scaffold(params);
    }

    public void setIgPath(String igPath) {
        this.igPath = igPath;
    }

    public void setIgVersion(String igVersion) {
        this.igVersion = igVersion;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setResourceNames(String resourceNames) {
        this.resourceNames = resourceNames;
    }

    public void setSoftwareSystems(String softwareSystems) {
        this.softwareSystems = softwareSystems;
    }
}
