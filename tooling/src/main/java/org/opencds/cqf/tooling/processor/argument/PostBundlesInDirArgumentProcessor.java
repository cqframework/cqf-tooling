package org.opencds.cqf.tooling.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.parameter.PostBundlesInDirParameters;
import org.opencds.cqf.tooling.processor.PostBundlesInDirProcessor.FHIRVersion;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;


public class PostBundlesInDirArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"PostBundlesInDir"};

    public static final String[] DIRECTORY_PATH_OPTIONS = {"dp", "dirpath"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhir-version"};
    public static final String[] ENCODING_OPTIONS = {"e", "encoding"};
    public static final String[] FHIR_URI_OPTIONS = {"fs", "fhir-uri"};

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder dirPathBuilder = parser.acceptsAll(asList(DIRECTORY_PATH_OPTIONS),"Limited to a single version of FHIR.");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"As of now FHIR DSTU3 and R4 are supported");
        OptionSpecBuilder encodingBuilder = parser.acceptsAll(asList(ENCODING_OPTIONS), "If omitted, output will be generated using JSON encoding.");
        OptionSpecBuilder fhirUriBuilder = parser.acceptsAll(asList(FHIR_URI_OPTIONS),"FHIR server.");

        OptionSpec<String> dirPath = dirPathBuilder.withRequiredArg().describedAs("path to directory containing bundle resources");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withRequiredArg().describedAs("fhir version");
        OptionSpec<String> encoding = encodingBuilder.withOptionalArg().describedAs("encoding for bundle resources"); 
        
        OptionSpec<String> fhirUri = fhirUriBuilder.withRequiredArg().describedAs("uri of fhir server"); 

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public PostBundlesInDirParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String dirPath = (String)options.valueOf(DIRECTORY_PATH_OPTIONS[0]);
        String fhirVersion = (String)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        String outputEncoding = (String)options.valueOf(ENCODING_OPTIONS[0]);
        String fhirUri = (String)options.valueOf(FHIR_URI_OPTIONS[0]);
        Encoding outputEncodingEnum = Encoding.JSON;
        if (outputEncoding != null) {
            outputEncodingEnum = Encoding.parse(outputEncoding.toLowerCase());
        }
    
        PostBundlesInDirParameters pbp = new PostBundlesInDirParameters();
        pbp.directoryPath = dirPath;
        pbp.fhirVersion = FHIRVersion.parse(fhirVersion);
        pbp.encoding = outputEncodingEnum;
        pbp.fhirUri = fhirUri;
       
        return pbp;
    }
}