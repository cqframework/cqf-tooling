package org.opencds.cqf.tooling.acceleratorkit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.testng.annotations.BeforeClass;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;

/**
 * This class scaffolds the test setup for the AcceleratorKitProcessor.
 * Extend this class to create a test for a specific accelerator kit
 * spreadsheet.
 */
public abstract class BaseContentTest {
    private static final String resourcesPath = "src/test/resources";
    private static final String tempPath = "target/test-output";

    private final Spreadsheet spreadsheet;
    private final FhirContext fhirContext;

    private Path outputPath;
    private Processor processor;

    protected BaseContentTest(Spreadsheet spreadsheet) {
        this(spreadsheet, FhirVersionEnum.R4);
    }

    protected BaseContentTest(Spreadsheet spreadsheet, FhirVersionEnum fhirVersion) {
        Objects.requireNonNull(spreadsheet, "spreadsheet is required");
        Objects.requireNonNull(spreadsheet.path, "spreadsheet path is required");
        Objects.requireNonNull(spreadsheet.dataDictionarySheets, "data dictionary sheets are required");
        Objects.requireNonNull(spreadsheet.scope, "scope is required");

        Objects.requireNonNull(fhirVersion, "fhir version is required");

        this.fhirContext = FhirContext.forCached(fhirVersion);
        this.spreadsheet = spreadsheet;
    }

    @BeforeClass
    protected void init() {
        try {
            outputPath = Files.createTempDirectory(Path.of(tempPath).toAbsolutePath(), "content-test-").toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        processor = new Processor();
        processor.execute(args());
    }

    protected String command() {
        return "-ProcessAcceleratorKit";
    }

    /**
     * Add new fields to this class to support additional command line arguments.
     */
    protected static class Spreadsheet {
        String path;
        String dataDictionarySheets;
        String encoding;
        String scope;
        String dataElementPages;
        String testCases;
    }

    protected String[] params() {
        return new String[] {
            "-s", scope(),
            "-pts", spreadsheetPath().toAbsolutePath().toString(),
            "-dep", dataDictionarySheets(),
            "-op", outputPath().toAbsolutePath().toString(),
            "-e", encoding(),
            "-tc", testCases()};
    };

    protected String[] args() {

        var params = params();
        if (params.length % 2 != 0) {
            throw new RuntimeException("Invalid number of command line arguments. Each argument must have a value");
        }

        var args = new ArrayList<String>();
        args.add(command());

        // get only the key-value pairs where the value is set,
        // create command line arguments (key=value) from them
        for (int i = 0; i < params.length; i += 2) {
            if (params[i + 1] != null) {
                args.add(params[i] + "=" + params[i + 1]);
            }
        }

        return args.toArray(String[]::new);
    }

    // Input params accessors

    protected String testCases() {
        return spreadsheet.testCases;
    }

    protected String encoding() {
        return spreadsheet.encoding;
    }

    protected String scope() {
        return spreadsheet.scope;
    }

    protected Path spreadsheetPath() {
        return Path.of(resourcesPath, spreadsheet.path);
    }

    protected String dataDictionarySheets() {
        return spreadsheet.dataDictionarySheets;
    }

    // FHIR context accessors
    protected FhirContext fhirContext() {
        return fhirContext;
    }

    // Directory accessors

    protected Path outputPath() {
        return outputPath;
    }

    protected Path inputPath() {
        return outputPath().resolve("input");
    }

    protected Path profilesPath() {
        return inputPath().resolve("profiles");
    }

    protected Path cqlPath() {
        return inputPath().resolve("cql");
    }

    protected Path examplesPath() {
        return inputPath().resolve("examples");
    }

    protected Path extensionsPath() {
        return inputPath().resolve("extensions");
    }

    protected Path resourcesPath() {
        return inputPath().resolve("resources");
    }

    protected Path testsPath() {
        return inputPath().resolve("tests");
    }

    protected Path vocabularyPath() {
        return inputPath().resolve("vocabulary");
    }

    // Resource helpers

    protected <T extends IBaseResource> T resourceAtPath(Class<T> resourceClass, Path resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath is required");
        Objects.requireNonNull(resourceClass, "resourceClass is required");

        var file = resourcePath.toFile();
        if (!file.exists()) {
            throw new RuntimeException("Resource file does not exist: " + resourcePath);
        }

        IParser parser = null;
        if (file.getName().endsWith(".json")) {
            parser = fhirContext().newJsonParser();
        } else if (file.getName().endsWith(".xml")) {
            parser = fhirContext().newXmlParser();
        } else {
            throw new RuntimeException("Unsupported resource file type: " + resourcePath);
        }

        try {
            return parser.parseResource(resourceClass, new BufferedReader(new FileReader(file)));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing resource file: " + resourcePath, e);
        }
    }
}
