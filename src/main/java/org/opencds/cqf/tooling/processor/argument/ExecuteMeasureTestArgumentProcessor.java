package org.opencds.cqf.tooling.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.parameter.MeasureTestParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.converters.EncodingConverter;
import org.opencds.cqf.tooling.utilities.converters.FhirVersionEnumConverter;

import ca.uhn.fhir.context.FhirVersionEnum;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;


public class ExecuteMeasureTestArgumentProcessor {
    public static final String[] OPERATION_OPTIONS = {"ExecuteMeasureTest"};

    public static final String[] CONTENT_BUNDLE_PATH_OPTIONS = {"content-path", "cp"};
    public static final String[] TEST_BUNDLE_PATH_OPTIONS = {"test-path", "tp"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fhir-version", "fv"};
    public static final String[] FHIR_SERVER_OPTIONS = {"fhir-server", "fs"};
    public static final String[] OUTPUT_ENCODING = {"e", "encoding"};

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder testBundlePathBuilder = parser.acceptsAll(asList(TEST_BUNDLE_PATH_OPTIONS), "Test Bundle must conform to the cqf spec for Measure test cases.");
        OptionSpecBuilder contentBundlePathBuilder = parser.acceptsAll(asList(CONTENT_BUNDLE_PATH_OPTIONS), "Required if running locally. If omitted, existing measure content on the server will be used.");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"Defaults to R4");
        OptionSpecBuilder outputEncodingBuilder = parser.acceptsAll(asList(OUTPUT_ENCODING), "Defaults to JSON.");
        OptionSpecBuilder fhirServerBuilder = parser.acceptsAll(asList(FHIR_SERVER_OPTIONS), "If omitted, the cql-evaluator will be used locally. NOTE: Not yet implemented");

        OptionSpec<String> testBundlePath = testBundlePathBuilder.withRequiredArg().required()
            .describedAs("path to test data bundle");

        OptionSpec<String> fhirServer = fhirServerBuilder.withRequiredArg().describedAs("url of fhir server to use for evaluation.");

        OptionSpec<String> contentBundlePath = contentBundlePathBuilder.requiredUnless(fhirServer).withRequiredArg()
            .describedAs("path to measure content bundle");

        OptionSpec<FhirVersionEnum> fhirVersion = fhirVersionBuilder.withRequiredArg()
            .describedAs("fhir version").withValuesConvertedBy(new FhirVersionEnumConverter()).defaultsTo(FhirVersionEnum.R4);
        OptionSpec<Encoding> outputEncoding = outputEncodingBuilder.withRequiredArg()
            .describedAs("desired output encoding for resources").withValuesConvertedBy(new EncodingConverter()).defaultsTo(Encoding.JSON);


        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public MeasureTestParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        MeasureTestParameters p = new MeasureTestParameters();

        p.testPath = (String)options.valueOf(TEST_BUNDLE_PATH_OPTIONS[0]);
        p.contentPath = (String)options.valueOf(CONTENT_BUNDLE_PATH_OPTIONS[0]);
        p.encoding = (Encoding)options.valueOf(OUTPUT_ENCODING[0]);
        p.fhirServer = (String)options.valueOf(FHIR_SERVER_OPTIONS[0]);
        p.fhirVersion = (FhirVersionEnum)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        return p;
    }
}