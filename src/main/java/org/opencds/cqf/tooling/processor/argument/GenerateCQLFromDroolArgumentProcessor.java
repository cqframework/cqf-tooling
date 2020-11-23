package org.opencds.cqf.tooling.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class GenerateCQLFromDroolArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"GenerateCQLDrool"};

    public static final String[] OUTPUT_PATH_OPTIONS = {"op", "outputPath", "outputpath", "o", "output"};
    public static final String[] ENCODING_OPTIONS = {"e", "encoding"};
    public static final String[] ENCODING_FILE_PATH_OPTIONS = {"ep", "encodingPath", "encoding-path", "efp", "encodingFilePath", "encoding-file-path", "encoding-filePath"};

    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder outputBuilder = parser.acceptsAll(asList(OUTPUT_PATH_OPTIONS),"Will be created if file path does not currently exist.");
        OptionSpecBuilder encodingBuilder = parser.acceptsAll(asList(ENCODING_OPTIONS), "If omitted, encoding input will be expected to be json.");
        OptionSpecBuilder encodingFilePathBuilder = parser.acceptsAll(asList(ENCODING_FILE_PATH_OPTIONS),"Must be a path to encoded logic export required for cql generation.");

        OptionSpec<String> outputPath = outputBuilder.withRequiredArg().describedAs("path to desired cql generation output");
        OptionSpec<String> encoding = encodingBuilder.withOptionalArg().describedAs("input encoding (as of now can only be json)"); 
        OptionSpec<String> encodingFilePath = encodingFilePathBuilder.withRequiredArg().describedAs("input encoding file path");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

	public GenerateCQLFromDroolParameters parseAndConvert(String[] args) {
		OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String outputPath = (String)options.valueOf(OUTPUT_PATH_OPTIONS[0]);
        String encoding = (String)options.valueOf(ENCODING_OPTIONS[0]);
        Encoding encodingEnum = Encoding.JSON;
        if (encoding != null) {
            encodingEnum = Encoding.parse(encoding.toLowerCase());
        }
        String encodingFilePath = (String)options.valueOf(ENCODING_FILE_PATH_OPTIONS[0]);
    
        GenerateCQLFromDroolParameters gcdp = new GenerateCQLFromDroolParameters();
        gcdp.outputPath = outputPath;
        gcdp.encoding = encodingEnum;
        gcdp.encodingFilePath = encodingFilePath;
       
        return gcdp;
	}

}
