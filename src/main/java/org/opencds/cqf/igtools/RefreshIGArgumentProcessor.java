package org.opencds.cqf.igtools;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.igtools.IGProcessor.IGVersion;
import org.opencds.cqf.utilities.ArgUtils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.KeyValuePair;


public class RefreshIGArgumentProcessor {

    public static final String[] OPERATION_OPTIONS = {"RefreshIG"};

    public static final String[] IG_PATH_OPTIONS = {"ip", "ig-path"};
    public static final String[] IG_VERSION_OPTIONS = {"iv", "ig-version"};
    public static final String[] INCLUDE_ELM_OPTIONS = {"e", "include-elm"};
    public static final String[] INCLUDE_DEPENDENCY_LIBRARY_OPTIONS = {"d", "include-dependencies"};
    public static final String[] INCLUDE_TERMINOLOGY_OPTIONS = {"t", "include-terminology"};
    public static final String[] INCLUDE_PATIENT_SCENARIOS_OPTIONS = {"p", "include-patients"};
    public static final String[] VERSIONED_OPTIONS = {"v", "versioned"};
    public static final String[] FHIR_URI_OPTIONS = {"fs", "fhir-uri"};
    public static final String[] RESOURCE_PATH_OPTIONS = {"rp", "resourcepath"};

    public OptionParser build() {
        OptionParser parser = new OptionParser();

        OptionSpecBuilder igPathBuilder = parser.acceptsAll(asList(IG_PATH_OPTIONS),"Limited to a single version of FHIR.");
        OptionSpecBuilder resourcePathBuilder = parser.acceptsAll(asList(RESOURCE_PATH_OPTIONS),"Use multiple times to define multiple resource directories.");
        OptionSpecBuilder igVersionBuilder = parser.acceptsAll(asList(IG_VERSION_OPTIONS),"If ommitted the root of the IG Path will be used.");
        OptionSpecBuilder fhirUriBuilder = parser.acceptsAll(asList(FHIR_URI_OPTIONS),"If ommitted the final bundle will not be loaded to a FHIR server.");
    
        OptionSpec<String> igPath = igPathBuilder.withRequiredArg().describedAs("root directory of the ig");
        OptionSpec<String> resourcePath = resourcePathBuilder.withOptionalArg().describedAs("directory of resources");
        OptionSpec<String> igVersion = igVersionBuilder.withOptionalArg().describedAs("ig fhir version");     

        //TODO: FHIR user / password (and other auth options)
        OptionSpec<String> fhirUri = fhirUriBuilder.withOptionalArg().describedAs("uri of fhir server");  

        parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");
        parser.acceptsAll(asList(INCLUDE_ELM_OPTIONS),"If ommitted ELM will not be produced or packaged.");
        parser.acceptsAll(asList(INCLUDE_DEPENDENCY_LIBRARY_OPTIONS),"If ommitted only the primary CQL library will be packaged.");
        parser.acceptsAll(asList(INCLUDE_TERMINOLOGY_OPTIONS),"If ommitted terminology will not be packaged.");
        parser.acceptsAll(asList(INCLUDE_PATIENT_SCENARIOS_OPTIONS),"If ommitted patient scenario information will not be packaged.");
        parser.acceptsAll(asList(VERSIONED_OPTIONS),"If ommitted resources must be uniquely named.");

        OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

        return parser;
    }

    public RefreshIGParameters parseAndConvert(String[] args) {
        OptionParser parser = build();
        OptionSet options = ArgUtils.parse(args, parser);

        ArgUtils.ensure(OPERATION_OPTIONS[0], options);

        String igPath = (String)options.valueOf(IG_PATH_OPTIONS[0]);
        List<String> resourcePaths = (List<String>)options.valuesOf(RESOURCE_PATH_OPTIONS[0]);
        //could not easily use the built-in default here because it is based on the value of the igPath argument.
        String igVersion = ArgUtils.defaultValue(options, IG_VERSION_OPTIONS[0], IGProcessor.getIgVersion(igPath).toString());
        Boolean includeELM = options.has(INCLUDE_ELM_OPTIONS[0]);  
        Boolean includeDependencies = options.has(INCLUDE_DEPENDENCY_LIBRARY_OPTIONS[0]);
        Boolean includeTerminology = options.has(INCLUDE_TERMINOLOGY_OPTIONS[0]);
        Boolean includePatientScenarios = options.has(INCLUDE_PATIENT_SCENARIOS_OPTIONS[0]);
        Boolean versioned = options.has(VERSIONED_OPTIONS[0]);
        String fhirUri = (String)options.valueOf(FHIR_URI_OPTIONS[0]);

        ArrayList<String> paths = new ArrayList<String>();
        paths.addAll(resourcePaths);
    
        RefreshIGParameters ip = new RefreshIGParameters();
        ip.igPath = igPath;
        ip.igVersion = IGVersion.parse(igVersion);
        ip.includeELM = includeELM;
        ip.includeDependencies = includeDependencies;
        ip.includeTerminology = includeTerminology;
        ip.includePatientScenarios = includePatientScenarios;
        ip.versioned = versioned;
        ip.resourceDirs = paths;
        ip.fhirUri = fhirUri;
       
        return ip;
    }
}