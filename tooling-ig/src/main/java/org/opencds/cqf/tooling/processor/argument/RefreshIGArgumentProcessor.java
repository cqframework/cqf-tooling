package org.opencds.cqf.tooling.processor.argument;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;


public class RefreshIGArgumentProcessor {
    public static final String[] OPERATION_OPTIONS = {"RefreshIG", "NewRefreshIG"};
    public static final String[] INI_OPTIONS = {"ini"};
    public static final String[] ROOT_DIR_OPTIONS = {"root-dir", "rd"};
    public static final String[] IG_PATH_OPTIONS = {"ip", "ig-path"};
    public static final String[] IG_OUTPUT_ENCODING = {"e", "encoding"};

    public static final String[] SKIP_PACKAGES_OPTIONS = {"s", "skip-packages"};
    public static final String[] INCLUDE_ELM_OPTIONS = {"elm", "include-elm"};
    public static final String[] INCLUDE_DEPENDENCY_LIBRARY_OPTIONS = {"d", "include-dependencies"};
    public static final String[] INCLUDE_TERMINOLOGY_OPTIONS = {"t", "include-terminology"};
    public static final String[] INCLUDE_PATIENT_SCENARIOS_OPTIONS = {"p", "include-patients"};
    public static final String[] VERSIONED_OPTIONS = {"v", "versioned"};
    public static final String[] UPDATED_VERSION_OPTIONS = {"uv", "updated-version"};
    public static final String[] FHIR_URI_OPTIONS = {"fs", "fhir-uri"};
    public static final String[] MEASURE_TO_REFRESH_PATH = {"mtrp", "measure-to-refresh-path"};
    public static final String[] RESOURCE_PATH_OPTIONS = {"rp", "resourcepath"};
    public static final String[] LIBRARY_PATH_OPTIONS = {"lp", "librarypath"};
    public static final String[] LIBRARY_OUTPUT_PATH_OPTIONS = {"libraryOutput", "libraryOutputPath", "lop"};
    public static final String[] MEASURE_OUTPUT_PATH_OPTIONS = {"measureOutput", "measureOutputPath", "mop"};
    public static final String[] SHOULD_APPLY_SOFTWARE_SYSTEM_STAMP_OPTIONS = { "ss", "stamp" };
    public static final String[] SHOULD_ADD_TIMESTAMP_OPTIONS = { "ts", "timestamp" };
    public static final String[] SHOULD_INCLUDE_ERRORS = { "x", "include-errors" };
    public static final String[] INCLUDE_POP_LEVEL_DATA_REQUIREMENTS_OPTIONS = { "pldr", "popDataRequirements" };


    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder iniBuilder = parser.acceptsAll(asList(INI_OPTIONS), "Path to ig ini file");
        OptionSpecBuilder rootDirBuilder = parser.acceptsAll(asList(ROOT_DIR_OPTIONS), "Root directory of the ig");
        OptionSpecBuilder updatedVersionBuilder = parser.acceptsAll(asList(UPDATED_VERSION_OPTIONS), "Version for the new libraries");
        OptionSpecBuilder igPathBuilder = parser.acceptsAll(asList(IG_PATH_OPTIONS),"Path to the IG, relative to the root directory");
        OptionSpecBuilder resourcePathBuilder = parser.acceptsAll(asList(RESOURCE_PATH_OPTIONS),"Use multiple times to define multiple resource directories, relative to the root directory.");
        OptionSpecBuilder libraryPathBuilder = parser.acceptsAll(asList(LIBRARY_PATH_OPTIONS), "Provide a single path, relative to the root directory, for library resources. The path will be added to the resource directories available to the refresh processing.");
        OptionSpecBuilder igOutputEncodingBuilder = parser.acceptsAll(asList(IG_OUTPUT_ENCODING), "If omitted, output will be generated using JSON encoding.");
        OptionSpecBuilder fhirUriBuilder = parser.acceptsAll(asList(FHIR_URI_OPTIONS),"If omitted the final bundle will not be loaded to a FHIR server.");
        OptionSpecBuilder measureToRefreshPathBuilder = parser.acceptsAll(asList(MEASURE_TO_REFRESH_PATH), "Path to Measure to refresh.");
        OptionSpecBuilder libraryOutputPathBuilder = parser.acceptsAll(asList(LIBRARY_OUTPUT_PATH_OPTIONS),"If omitted, the libraries will overwrite any existing libraries");
        OptionSpecBuilder measureOutputPathBuilder = parser.acceptsAll(asList(MEASURE_OUTPUT_PATH_OPTIONS),"If omitted, the measures will overwrite any existing measures");
        OptionSpecBuilder shouldApplySoftwareSystemStampBuilder = parser.acceptsAll(asList(SHOULD_APPLY_SOFTWARE_SYSTEM_STAMP_OPTIONS),"Indicates whether refreshed Measure and Library resources should be stamped with the 'cqf-tooling' stamp via the crmi-softwaresystem Extension.");
        OptionSpecBuilder shouldAddTimestampBuilder = parser.acceptsAll(asList(SHOULD_ADD_TIMESTAMP_OPTIONS),"Indicates whether refreshed Bundle should attach timestamp of creation.");
        OptionSpecBuilder shouldVerboseMessaging = parser.acceptsAll(asList(SHOULD_APPLY_SOFTWARE_SYSTEM_STAMP_OPTIONS),"Indicates that a complete list of errors during library, measure, and test case refresh are included upon failure.");

        OptionSpec<String> ini = iniBuilder.withRequiredArg().describedAs("Path to the IG ini file");
        OptionSpec<String> updatedVersion = updatedVersionBuilder.withOptionalArg().describedAs("Updated version of the IG");
        OptionSpec<String> rootDir = rootDirBuilder.withOptionalArg().describedAs("Root directory of the IG");
        OptionSpec<String> igPath = igPathBuilder.withRequiredArg().describedAs("Path to the IG, relative to the root directory");
        OptionSpec<String> resourcePath = resourcePathBuilder.withOptionalArg().describedAs("directory of resources");
        OptionSpec<String> libraryPath = libraryPathBuilder.withOptionalArg().describedAs("directory of library resources");
        OptionSpec<String> igOutputEncoding = igOutputEncodingBuilder.withOptionalArg().describedAs("desired output encoding for resources");
        OptionSpec<String> measureToRefreshPath = measureToRefreshPathBuilder.withOptionalArg().describedAs("Path to Measure to refresh.");
        OptionSpec<String> libraryOutputPath = libraryOutputPathBuilder.withOptionalArg().describedAs("path to the output directory for updated libraries");
        OptionSpec<String> measureOutputPath = measureOutputPathBuilder.withOptionalArg().describedAs("path to the output directory for updated measures");
        OptionSpec<String> shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStampBuilder.withOptionalArg().describedAs("Indicates whether refreshed Measure and Library resources should be stamped with the 'cqf-tooling' stamp via the crmi-softwaresystem Extension");
        OptionSpec<String> shouldAddTimestampOptions = shouldAddTimestampBuilder.withOptionalArg().describedAs("Indicates whether refreshed Bundle should attach timestamp of creation");
        OptionSpec<String> shouldVerboseMessagingOptions = shouldVerboseMessaging.withOptionalArg().describedAs("Indicates that a complete list of errors during library, measure, and test case refresh are included upon failure.");


        //TODO: FHIR user / password (and other auth options)
        OptionSpec<String> fhirUri = fhirUriBuilder.withOptionalArg().describedAs("uri of fhir server");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");
        parser.acceptsAll(asList(SKIP_PACKAGES_OPTIONS), "Specifies whether to skip packages building.");
        parser.acceptsAll(asList(INCLUDE_ELM_OPTIONS),"If omitted ELM will not be produced or packaged.");
        parser.acceptsAll(asList(INCLUDE_DEPENDENCY_LIBRARY_OPTIONS),"If omitted only the primary CQL library will be packaged.");
        parser.acceptsAll(asList(INCLUDE_TERMINOLOGY_OPTIONS),"If omitted terminology will not be packaged.");
        parser.acceptsAll(asList(INCLUDE_PATIENT_SCENARIOS_OPTIONS),"If omitted patient scenario information will not be packaged.");
        parser.acceptsAll(asList(VERSIONED_OPTIONS),"If omitted resources must be uniquely named.");
        parser.acceptsAll(asList(SHOULD_INCLUDE_ERRORS),"Specifies whether to show errors during library, measure, and test case refresh.");
        parser.acceptsAll(asList(INCLUDE_POP_LEVEL_DATA_REQUIREMENTS_OPTIONS), "If omitted, the measures will not include population-level data requirements");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public RefreshIGParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String ini = (String)options.valueOf(INI_OPTIONS[0]);
        String rootDir = (String)options.valueOf(ROOT_DIR_OPTIONS[0]);
        String igPath = (String)options.valueOf(IG_PATH_OPTIONS[0]);

        List<String> resourcePaths = ArgUtils.getOptionValues(options, RESOURCE_PATH_OPTIONS[0]);

        List<String> libraryPaths = ArgUtils.getOptionValues(options, LIBRARY_PATH_OPTIONS[0]);
        if (libraryPaths != null && libraryPaths.size() > 1) {
            throw new IllegalArgumentException("Only one library path may be specified"); // Could probably do this with the OptionSpec stuff...
        }
        String libraryPath = null;
        if (libraryPaths != null && libraryPaths.size() == 1) {
            libraryPath = libraryPaths.get(0);
        }

        //could not easily use the built-in default here because it is based on the value of the igPath argument.
        String igEncoding = (String)options.valueOf(IG_OUTPUT_ENCODING[0]);
        Encoding outputEncodingEnum = Encoding.JSON;
        if (igEncoding != null) {
            outputEncodingEnum = Encoding.parse(igEncoding.toLowerCase());
        }
        boolean skipPackages = options.has(SKIP_PACKAGES_OPTIONS[0]);
        boolean includeELM = options.has(INCLUDE_ELM_OPTIONS[0]);
        boolean includeDependencies = options.has(INCLUDE_DEPENDENCY_LIBRARY_OPTIONS[0]);
        boolean includeTerminology = options.has(INCLUDE_TERMINOLOGY_OPTIONS[0]);
        boolean includePatientScenarios = options.has(INCLUDE_PATIENT_SCENARIOS_OPTIONS[0]);
        boolean versioned = options.has(VERSIONED_OPTIONS[0]);
        boolean includePopLevelDataRequirements = options.has(INCLUDE_POP_LEVEL_DATA_REQUIREMENTS_OPTIONS[0]);
        String fhirUri = (String)options.valueOf(FHIR_URI_OPTIONS[0]);
        String measureToRefreshPath = (String)options.valueOf(MEASURE_TO_REFRESH_PATH[0]);

        String updatedVersion = (String)options.valueOf(UPDATED_VERSION_OPTIONS[0]);
        if(updatedVersion == null) {
            updatedVersion = "";
        }

        String libraryOutputPath = (String)options.valueOf(LIBRARY_OUTPUT_PATH_OPTIONS[0]);
        if (libraryOutputPath == null) {
            libraryOutputPath = "";
        }

        String measureOutputPath = (String)options.valueOf(MEASURE_OUTPUT_PATH_OPTIONS[0]);
        if (measureOutputPath == null) {
            measureOutputPath = "";
        }

        boolean shouldApplySoftwareSystemStamp = true;
        String shouldApplySoftwareSystemStampValue = (String)options.valueOf(SHOULD_APPLY_SOFTWARE_SYSTEM_STAMP_OPTIONS[0]);

        if ((shouldApplySoftwareSystemStampValue != null) && shouldApplySoftwareSystemStampValue.equalsIgnoreCase("false")) {
            shouldApplySoftwareSystemStamp = false;
        }

        boolean addBundleTimestamp = false;
        String addBundleTimestampValue = (String)options.valueOf(SHOULD_ADD_TIMESTAMP_OPTIONS[0]);

        if ((addBundleTimestampValue != null) && addBundleTimestampValue.equalsIgnoreCase("true")) {
            addBundleTimestamp = true;
        }

        boolean verboseMessaging = options.has(SHOULD_INCLUDE_ERRORS[0]);

        ArrayList<String> paths = new ArrayList<>();
        if (resourcePaths != null && !resourcePaths.isEmpty()) {
            paths.addAll(resourcePaths);
        }

        if (libraryPaths != null) {
            paths.addAll(libraryPaths);
        }

        RefreshIGParameters ip = new RefreshIGParameters();
        ip.ini = ini;
        ip.rootDir = rootDir;
        ip.igPath = igPath;
        ip.outputEncoding = outputEncodingEnum;
        ip.skipPackages = skipPackages;
        ip.includeELM = includeELM;
        ip.includeDependencies = includeDependencies;
        ip.includeTerminology = includeTerminology;
        ip.includePatientScenarios = includePatientScenarios;
        ip.versioned = versioned;
        ip.shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStamp;
        ip.addBundleTimestamp = addBundleTimestamp;
        ip.libraryPath = libraryPath;
        ip.resourceDirs = paths;
        ip.fhirUri = fhirUri;
        ip.measureToRefreshPath = measureToRefreshPath;
        ip.libraryOutputPath = libraryOutputPath;
        ip.measureOutputPath = measureOutputPath;
        ip.updatedVersion = updatedVersion;
        ip.verboseMessaging = verboseMessaging;
        ip.includePopulationLevelDataRequirements = includePopLevelDataRequirements;
        return ip;
    }
}
