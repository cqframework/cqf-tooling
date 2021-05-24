package org.opencds.cqf.tooling.processor.argument;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.opencds.cqf.tooling.parameter.TestIGParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.ArgUtils;

import static java.util.Arrays.asList;


public class TestIGArgumentsProcessor {

    public static final String[] OPERATION_OPTIONS = {"TestIG"};

    public static final String[] INI = {"ini"};
    public static final String[] ROOT_DIR_OPTIONS = {"root-dir"};
    public static final String[] IG_PATH_OPTIONS = {"ip", "ig-path"};
//    public static final String[] IG_CANONICAL_BASE = {"igcb", "igCanonicalBase"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhir-version"};
    public static final String[] TEST_CASES_PATH_OPTIONS = {"tests", "testsPath", "testCasesPath", "tp", "tcp"};
    public static final String[] FHIR_URI_OPTIONS = {"fs", "fhir-uri"};

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder iniBuilder = parser.acceptsAll(asList(INI), "IG ini file");
        OptionSpecBuilder rootDirBuilder = parser.acceptsAll(asList(ROOT_DIR_OPTIONS), "Root directory of the ig");
        OptionSpecBuilder igPathBuilder = parser.acceptsAll(asList(IG_PATH_OPTIONS),"Path to the IG, relative to the root directory");
//        OptionSpecBuilder igCanonicalBaseBuilder = parser.acceptsAll(asList(IG_CANONICAL_BASE),"resource canonical base");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"Limited to a single version of FHIR.");
        OptionSpecBuilder testCasesPathBuilder = parser.acceptsAll(asList(TEST_CASES_PATH_OPTIONS),"Path to test cases");
        OptionSpecBuilder fhirUriBuilder = parser.acceptsAll(asList(FHIR_URI_OPTIONS),"If omitted the final bundle will not be loaded to a FHIR server.");

        OptionSpec<String> ini = iniBuilder.withOptionalArg().describedAs("IG ini file");
        OptionSpec<String> rootDir = rootDirBuilder.withOptionalArg().describedAs("Root directory of the IG");
        OptionSpec<String> igPath = igPathBuilder.withOptionalArg().describedAs("Path to the IG, relative to the root directory");
//        OptionSpec<String> igCanonicalBasePath = igCanonicalBaseBuilder.withOptionalArg().describedAs("resource canonical base");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withOptionalArg().describedAs("fhir version");
        OptionSpec<String> testCasesPath = testCasesPathBuilder.withRequiredArg().describedAs("path to the test cases");
        OptionSpec<String> fhirServerUri = fhirUriBuilder.withRequiredArg().describedAs("uri for fhir server to test on");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public TestIGParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String ini = (String)options.valueOf(INI[0]);
        String rootDir = (String)options.valueOf(ROOT_DIR_OPTIONS[0]);
        String igPath = (String)options.valueOf(IG_PATH_OPTIONS[0]);
//        String igCanonicalBase = (String)options.valueOf(IG_CANONICAL_BASE[0]);
        String fhirVersion = (String)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        String testCasesPath = (String)options.valueOf(TEST_CASES_PATH_OPTIONS[0]);
        String fhirServerUri = (String)options.valueOf(FHIR_URI_OPTIONS[0]);

        TestIGParameters ip = new TestIGParameters();
        ip.ini = ini;
        ip.rootDir = rootDir;
        ip.igPath = igPath;
//        ip.igCanonicalBase = igCanonicalBase;
        ip.testCasesPath = testCasesPath;
        ip.fhirServerUri = fhirServerUri;
        ip.fhirContext = IGProcessor.getIgFhirContext(fhirVersion);

        return ip;
    }
}