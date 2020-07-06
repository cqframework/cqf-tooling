package org.opencds.cqf.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.parameter.BundleTestCasesParameters;
import org.opencds.cqf.processor.IGProcessor;
import org.opencds.cqf.processor.IGProcessor.IGVersion;
import org.opencds.cqf.utilities.ArgUtils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;


public class BundleTestCasesArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"bundleTests"};
    public static final String[] HELP_OPTIONS = {"h", "help", "?"};
    public static final String[] PATH_OPTIONS = {"p", "path"};
    public static final String[] IG_VERSION_OPTIONS = {"iv", "ig-version"};

    public OptionParser build() {
        OptionParser parser = new OptionParser();
      
        OptionSpecBuilder pathBuilder = parser.acceptsAll(asList(PATH_OPTIONS),"Required.");
        OptionSpecBuilder igVersionBuilder = parser.acceptsAll(asList(IG_VERSION_OPTIONS),"Required.");
    
        OptionSpec<String> path = pathBuilder.withRequiredArg().describedAs("root directory of the test cases");
        OptionSpec<String> igVersion = igVersionBuilder.withRequiredArg().describedAs("ig fhir version");    

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");        
        OptionSpec<Void> help = parser.acceptsAll(asList(HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public BundleTestCasesParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String path = (String)options.valueOf(PATH_OPTIONS[0]);
        //could not easily use the built-in default here because it is based on the value of the igPath argument.
        String igVersion = ArgUtils.defaultValue(options, IG_VERSION_OPTIONS[0], IGProcessor.getIgVersion(path).toString());

        BundleTestCasesParameters ip = new BundleTestCasesParameters();
        ip.path = path;
        ip.igVersion = IGVersion.parse(igVersion);

        return ip;
    }
}