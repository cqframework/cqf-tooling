package org.opencds.cqf.tooling.processor.argument;

import static java.util.Arrays.asList;

import org.opencds.cqf.tooling.parameter.RefreshLibraryParameters;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;


public class RefreshLibraryArgumentProcessor {
    public static final String[] OPERATION_OPTIONS = {"RefreshLibrary"};

    public static final String[] INI = {"ini"};
    public static final String[] IG_CANONICAL_BASE = {"igcb", "igCanonicalBase"};
    public static final String[] CQL_PATH_OPTIONS = {"cql", "content", "cqlPath", "cqlContentPath", "contentPath", "cp"};
    public static final String[] Library_PATH_OPTIONS = {"library", "libraryPath", "resourcePath", "lp", "cp"};
    public static final String[] Library_Output_DIRECTORY_OPTIONS = {"libraryOutput", "libraryOutputPath", "resourceOutputPath", "lop"};
    public static final String[] FHIR_VERSION_OPTIONS = {"fv", "fhir-version"};
    public static final String[] OUTPUT_ENCODING = {"e", "encoding"};
    public static final String[] VERSIONED_OPTIONS = {"v", "versioned"};
    public static final String[] SOFTWARE_STAMP = { "ss", "stamp" };

    @SuppressWarnings("unused")
    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder iniBuilder = parser.acceptsAll(asList(INI), "IG ini file");
        OptionSpecBuilder igCanonicalBaseBuilder = parser.acceptsAll(asList(IG_CANONICAL_BASE),"resource canonical base");
        OptionSpecBuilder cqlPathBuilder = parser.acceptsAll(asList(CQL_PATH_OPTIONS),"Library will be created in the same folder as the cql");
        OptionSpecBuilder libraryPathBuilder = parser.acceptsAll(asList(Library_PATH_OPTIONS),"If omitted, the library will be created in the same folder as the cql");
        OptionSpecBuilder libraryOutputDirectoryBuilder = parser.acceptsAll(asList(Library_Output_DIRECTORY_OPTIONS),"If omitted, the libraries will overwrite any existing libraries");
        OptionSpecBuilder fhirVersionBuilder = parser.acceptsAll(asList(FHIR_VERSION_OPTIONS),"Limited to a single version of FHIR.");
        OptionSpecBuilder outputEncodingBuilder = parser.acceptsAll(asList(OUTPUT_ENCODING), "If omitted, output will be generated using JSON encoding.");
        OptionSpecBuilder shouldApplySoftwareSystemStampBuilder = parser.acceptsAll(asList(SOFTWARE_STAMP),"Indicates whether refreshed Library resources should be stamped with the 'cqf-tooling' stamp via the crmi-softwaresystem Extension.");

        OptionSpec<String> ini = iniBuilder.withOptionalArg().describedAs("IG ini file");
        OptionSpec<String> igCanonicalBasePath = igCanonicalBaseBuilder.withOptionalArg().describedAs("resource canonical base");
        OptionSpec<String> cqlPath = cqlPathBuilder.withOptionalArg().describedAs("path to the cql content");
        OptionSpec<String> libraryPath = libraryPathBuilder.withRequiredArg().describedAs("path to the library");
        OptionSpec<String> libraryOutputDirectoryPath = libraryOutputDirectoryBuilder.withOptionalArg().describedAs("path to the output directory for updated libraries");
        OptionSpec<String> fhirVersion = fhirVersionBuilder.withOptionalArg().describedAs("fhir version");
        OptionSpec<String> outputEncoding = outputEncodingBuilder.withOptionalArg().describedAs("desired output encoding for resources");
        OptionSpec<String> shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStampBuilder.withOptionalArg().describedAs("Indicates whether refreshed Library resources should be stamped with the 'cqf-tooling' stamp via the crmi-softwaresystem Extension");

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");
        parser.acceptsAll(asList(VERSIONED_OPTIONS),"If omitted resources must be uniquely named.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public RefreshLibraryParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String ini = (String)options.valueOf(INI[0]);
        String igCanonicalBase = (String)options.valueOf(IG_CANONICAL_BASE[0]);
        String cqlPath = (String)options.valueOf(CQL_PATH_OPTIONS[0]);
        String fhirVersion = (String)options.valueOf(FHIR_VERSION_OPTIONS[0]);
        String encoding = (String)options.valueOf(OUTPUT_ENCODING[0]);
        String softwareStamp = (String)options.valueOf(SOFTWARE_STAMP[0]);
        String libraryPath = (String)options.valueOf(Library_PATH_OPTIONS[0]);
        if (libraryPath == null) {
            libraryPath = "";
        }
        String libraryOutputDirectory = (String)options.valueOf(Library_Output_DIRECTORY_OPTIONS[0]);
        if (libraryOutputDirectory == null) {
            libraryOutputDirectory = "";
        }
        Encoding outputEncodingEnum = Encoding.JSON;
        if (encoding != null) {
            outputEncodingEnum = Encoding.parse(encoding.toLowerCase());
        }
        Boolean versioned = options.has(VERSIONED_OPTIONS[0]);
        Boolean shouldApplySoftwareSystemStamp = true;
        if ((softwareStamp != null) && softwareStamp.equalsIgnoreCase("false")) {
            shouldApplySoftwareSystemStamp = false;
        }

        RefreshLibraryParameters lp = new RefreshLibraryParameters();
        lp.ini = ini;
        lp.igCanonicalBase = igCanonicalBase;
        lp.cqlContentPath = cqlPath;
        lp.fhirContext = IGProcessor.getIgFhirContext(fhirVersion);
        lp.encoding = outputEncodingEnum;
        lp.versioned = versioned;
        lp.libraryPath = libraryPath;
        lp.libraryOutputDirectory = libraryOutputDirectory;
        lp.shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStamp;

        return lp;
    }
}
