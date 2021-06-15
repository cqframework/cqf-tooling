package org.opencds.cqf.tooling.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
/**
 * @author Joshua Reynolds
 */
public class GenerateCQLFromDroolArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"GenerateCQLFromDrool"};

    public static final String[] OUTPUT_PATH_OPTIONS = {"op", "outputPath", "outputpath", "o", "output"};
    public static final String[] ENCODING_OPTIONS = {"e", "encoding"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhirVersion"};
    public static final String[] INPUT_FILE_PATH_OPTIONS = {"ip", "inputPath", "input-path", "ifp", "inputFilePath", "input-file-path", "input-filePath"};
    public static final String[] CQLTYPES_OPTIONS = {"type", "t"};

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder outputBuilder = parser.acceptsAll(asList(OUTPUT_PATH_OPTIONS),"Will be created if file path does not currently exist.");
        OptionSpecBuilder encodingBuilder = parser.acceptsAll(asList(ENCODING_OPTIONS), "If omitted, encoding input will be expected to be json.");
        OptionSpecBuilder inputFilePathBuilder = parser.acceptsAll(asList(INPUT_FILE_PATH_OPTIONS),"Must be a path to encoded logic export required for cql generation.");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"If omitted, defualt version will be 4.0.0");
        OptionSpecBuilder cqlTypeBuilder = parser.acceptsAll(asList(CQLTYPES_OPTIONS),"If omitted, defualt granularity will be CONDITION");

        OptionSpec<String> outputPath = outputBuilder.withRequiredArg().describedAs("path to desired cql generation output");
        OptionSpec<String> encoding = encodingBuilder.withOptionalArg().describedAs("input encoding (as of now can only be json)").defaultsTo("json"); 
        OptionSpec<String> inputFilePath = inputFilePathBuilder.withRequiredArg().describedAs("input encoded file path");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withOptionalArg().describedAs("FHIR Model Version to map elm to.");
        OptionSpec<String> cqlType = cqlTypeBuilder.withOptionalArg().describedAs("Elm Granularity Option.");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

	public GenerateCQLFromDroolParameters parseAndConvert(String[] args) {
		OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String outputPath = (String)options.valueOf(OUTPUT_PATH_OPTIONS[0]);
        String inputFilePath = (String)options.valueOf(INPUT_FILE_PATH_OPTIONS[0]);
        String fhirVersion = (String)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        if (fhirVersion == null) {
            fhirVersion = "4.0.0";
        }
        String encoding = (String)options.valueOf(ENCODING_OPTIONS[0]);
        Encoding encodingEnum = Encoding.parse(encoding.toLowerCase());
        String cqlTypeString = (String)options.valueOf(CQLTYPES_OPTIONS[0]);
        if (cqlTypeString == null) {
            cqlTypeString = "CONDITION";
        }

        CQLTYPES cqlType;
        switch (cqlTypeString.toUpperCase()) {
            case "CONDITION": cqlType = CQLTYPES.CONDITION; break;
            case "CONDITIONREL": cqlType = CQLTYPES.CONDITIONREL; break;
            default: throw new IllegalArgumentException("Unknown cql type: " + cqlTypeString);
        }
    
        GenerateCQLFromDroolParameters gcdp = new GenerateCQLFromDroolParameters();
        gcdp.outputPath = outputPath;
        gcdp.encoding = encodingEnum;
        gcdp.inputFilePath = inputFilePath;
        gcdp.fhirVersion = fhirVersion;
        gcdp.type = cqlType;
       
        return gcdp;
	}

}
