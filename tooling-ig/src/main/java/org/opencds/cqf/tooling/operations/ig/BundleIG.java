package org.opencds.cqf.tooling.operations.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.IOUtils;

/**
 * The purpose of this operation is to package the examples and artifacts, contained within an IG, into transaction
 * bundles and write to specified output directory.
 */
@Operation(name = "BundleIG")
public class BundleIG implements ExecutableOperation {

    @OperationParam(
            alias = {"ptig", "pathtoig"},
            setter = "setPathToIg",
            required = true,
            description = "Path to ImplementationGuide project root (where ig.json is stored) (required)")
    private String pathToIg;

    @OperationParam(
            alias = {"e", "encoding"},
            setter = "setEncoding",
            defaultValue = "json",
            description = "Preferred encoding { json, xml } (default json)")
    private String encoding = "json";

    @OperationParam(
            alias = {"op", "outputpath"},
            setter = "setOutputPath",
            defaultValue = "src/main/resources/org/opencds/cqf/tooling/igtools/output",
            description =
                    "Path to directory where Bundles will be written (default src/main/resources/org/opencds/cqf/tooling/igtools/output)")
    private String outputPath = "src/main/resources/org/opencds/cqf/tooling/igtools/output";

    private FhirContext fhirContext;
    private IParser jsonParser;
    private IParser xmlParser;

    private final Gson gson = new Gson();

    private final List<String> resourcePaths = new ArrayList<>();
    private final List<String> resourceFiles = new ArrayList<>();

    private final Map<String, IBaseResource> outputBundles = new HashMap<>();

    @Override
    public void execute() {
        if (pathToIg == null) {
            throw new IllegalArgumentException("The path to the IG is required");
        }

        final JsonObject igControl = getIgControl();
        setFhirContext(igControl);
        jsonParser = fhirContext.newJsonParser();
        xmlParser = fhirContext.newXmlParser();
        setResourcePaths(igControl);
        setResourceFiles(igControl);
        resolveDirectoryFiles();
        outputBundles();
    }

    /**
     * Parse the ig.json configuration file.
     *
     * @return JsonObject representation of the ig.json
     */
    private JsonObject getIgControl() {
        try {
            return gson.fromJson(new FileReader(new File(pathToIg + "/ig.json")), JsonObject.class);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading control file: " + e.getMessage());
        }
    }

    /**
     * Determine the FHIR version from the ig.json configuration.
     *
     * @param igControl JsonObject representation of the ig.json
     */
    private void setFhirContext(final JsonObject igControl) {
        if (igControl.has("version") && igControl.get("version").isJsonPrimitive()) {
            final String version = igControl.get("version").getAsString();
            if (version.equals("3.0.0") || version.equals("3.0.1")) {
                fhirContext = FhirContext.forDstu3Cached();
            } else {
                throw new UnsupportedOperationException("The BundleIg operation currently only supports FHIR STU3");
            }
        } else {
            throw new RuntimeException("Version is required to be populated in the ig.json");
        }
    }

    /**
     * Determine the paths to directories containing resources defined by the IG.
     *
     * @param igControl JsonObject representation of the ig.json
     */
    private void setResourcePaths(final JsonObject igControl) {
        if (igControl.has("paths") && igControl.get("paths").isJsonObject()) {
            final JsonObject paths = igControl.get("paths").getAsJsonObject();
            if (paths.has("resources")) {
                final JsonElement resources = paths.get("resources");
                if (resources.isJsonArray()) {
                    for (final JsonElement path : resources.getAsJsonArray()) {
                        if (path.isJsonPrimitive()) {
                            resourcePaths.add(IOUtils.concatFilePath(pathToIg, path.getAsString()));
                            outputBundles.put(path.getAsString(), null);
                        }
                    }
                } else {
                    resourcePaths.add(IOUtils.concatFilePath(pathToIg, resources.getAsString()));
                }
            }
        }
    }

    /**
     * Determine the ig resources file names specified in the ig.json.
     *
     * @param igControl JsonObject representation of the ig.json
     */
    private void setResourceFiles(final JsonObject igControl) {
        if (igControl.has("resources") && igControl.get("resources").isJsonObject()) {
            for (final Map.Entry<String, JsonElement> resources :
                    igControl.get("resources").getAsJsonObject().entrySet()) {
                if (resources.getKey().startsWith("Bundle")
                        || resources.getKey().startsWith("StructureDefinition")) {
                    continue;
                }
                if (resources.getValue().isJsonObject()
                        && resources.getValue().getAsJsonObject().has("source")
                        && resources.getValue().getAsJsonObject().get("source").isJsonPrimitive()) {
                    resourceFiles.add(
                            resources.getValue().getAsJsonObject().get("source").getAsString());
                }
            }
        }
    }

    /**
     * Retrieve the resource files and build Bundle(s)
     */
    private void resolveDirectoryFiles() {
        for (final String path : resourcePaths) {
            final Bundle bundle = new Bundle();
            bundle.setType(Bundle.BundleType.TRANSACTION);

            try (Stream<Path> paths = Files.walk(Paths.get(path))) {
                paths.filter(Files::isRegularFile)
                        .filter(x -> resourceFiles.contains(x.getFileName().toString()))
                        .forEach(x -> addArtifactToBundle(x, bundle));
            } catch (final IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }

            for (final Map.Entry<String, IBaseResource> entry : outputBundles.entrySet()) {
                if (path.endsWith(entry.getKey()) && entry.getValue() == null) {
                    outputBundles.put(entry.getKey(), bundle);
                    break;
                }
            }
        }
    }

    /**
     * Parse artifact file and add to Bundle. NOTE: this method assumes FHIR STU3 artifacts are being used.
     *
     * @param path   Path to artifact
     * @param bundle Bundle to populate
     */
    private void addArtifactToBundle(final Path path, final Bundle bundle) {
        IBaseResource resource;
        try {
            FileReader reader = new FileReader(new File(path.toString()));
            if (path.toString().endsWith(".xml")) {
                resource = xmlParser.parseResource(reader);
            } else if (path.toString().endsWith(".json")) {
                resource = jsonParser.parseResource(reader);
            } else {
                throw new RuntimeException("Unknown file type: " + path.toString());
            }
        } catch (final FileNotFoundException fnfe) {
            throw new RuntimeException("Error reading file: " + path.toString());
        }

        bundle.addEntry(new Bundle.BundleEntryComponent()
                .setResource((Resource) resource)
                .setRequest(new Bundle.BundleEntryRequestComponent()
                        .setMethod(Bundle.HTTPVerb.PUT)
                        .setUrl(((Resource) resource).getId())));
    }

    /**
     * Write Bundles to output directory.
     */
    private void outputBundles() {
        for (final Map.Entry<String, IBaseResource> set : outputBundles.entrySet()) {
            try (FileOutputStream writer =
                    new FileOutputStream(IOUtils.concatFilePath(outputPath, set.getKey() + "." + encoding))) {
                writer.write(
                        encoding.equals("json")
                                ? jsonParser
                                        .setPrettyPrint(true)
                                        .encodeResourceToString(set.getValue())
                                        .getBytes()
                                : xmlParser
                                        .setPrettyPrint(true)
                                        .encodeResourceToString(set.getValue())
                                        .getBytes());
                writer.flush();
            } catch (final IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error writing Bundle to file: " + e.getMessage());
            }
        }
    }

    public void setPathToIg(String pathToIg) {
        this.pathToIg = pathToIg;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
