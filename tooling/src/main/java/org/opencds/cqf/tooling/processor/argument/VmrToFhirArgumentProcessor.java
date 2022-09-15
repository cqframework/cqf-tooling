package org.opencds.cqf.tooling.processor.argument;
import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.parameter.VmrToFhirParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
/**
 * @author Joshua Reynolds
 */
public class VmrToFhirArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"VmrToFhir"};

    public static final String[] OUTPUT_PATH_OPTIONS = {"op", "outputPath", "outputpath", "o", "output"};
    public static final String[] ENCODING_OPTIONS = {"e", "encoding"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhirVersion"};
    public static final String[] INPUT_FILE_PATH_OPTIONS = {"ip", "inputPath", "input-path", "ifp", "inputFilePath", "input-file-path", "input-filePath"};

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder outputBuilder = parser.acceptsAll(asList(OUTPUT_PATH_OPTIONS),"Will be created if file path does not currently exist.");
        OptionSpecBuilder encodingBuilder = parser.acceptsAll(asList(ENCODING_OPTIONS), "If omitted, encoding input will be expected to be xml.");
        OptionSpecBuilder inputFilePathBuilder = parser.acceptsAll(asList(INPUT_FILE_PATH_OPTIONS),"Must be a path to Vmr Data File.");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"If omitted, defualt version will be 4.0.0");

        OptionSpec<String> outputPath = outputBuilder.withRequiredArg().describedAs("path to fhir data output");
        OptionSpec<String> encoding = encodingBuilder.withOptionalArg().describedAs("input encoding (as of now can only be xml)").defaultsTo("xml"); 
        OptionSpec<String> inputFilePath = inputFilePathBuilder.withRequiredArg().describedAs("input vmr file path");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withOptionalArg().describedAs("FHIR Model Version to map elm to.");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

	public VmrToFhirParameters parseAndConvert(String[] args) {
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
    
        VmrToFhirParameters vtfp = new VmrToFhirParameters();
        vtfp.vmrDataPath = inputFilePath;
        vtfp.encoding = encodingEnum;
        vtfp.fhirOutputPath = outputPath;
        vtfp.fhirVersion = fhirVersion;
       
        return vtfp;
	}

}
