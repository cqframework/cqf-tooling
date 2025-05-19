package org.opencds.cqf.tooling.processor.argument;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.opencds.cqf.tooling.common.SoftwareSystem;
import org.opencds.cqf.tooling.parameter.ScaffoldParameters;
import org.opencds.cqf.tooling.utilities.ArgUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ScaffoldArgumentProcessor {
	public static final String[] OPERATION_OPTIONS = {"ScaffoldIG"};

	public static final String[] IG_PATH_OPTIONS = {"ip", "ig-path"};
	public static final String[] IG_VERSION_OPTIONS = {"iv", "ig-version"};
	public static final String[] IG_OUTPUT_ENCODING = {"e", "encoding"};
	public static final String[] NAME_OF_RESOURCE_CREATE_OPTIONS = {"rn", "resource-name"};
	public static final String[] SOFTWARE_SYSTEM_OPTIONS = {"software"};

	@SuppressWarnings("unused")
	public OptionParser build() {
		OptionParser parser = new OptionParser();

		OptionSpecBuilder igPathBuilder = parser.acceptsAll(asList(IG_PATH_OPTIONS),"Limited to a single version of FHIR.");
		OptionSpecBuilder igVersionBuilder = parser.acceptsAll(asList(IG_VERSION_OPTIONS),"The desired FHIR Version.");
		OptionSpecBuilder igOutputEncodingBuilder = parser.acceptsAll(asList(IG_OUTPUT_ENCODING), "JSON|XML - If omitted, output will be generated using JSON encoding.");
		OptionSpecBuilder nameOfResourceToCreateBuilder = parser.acceptsAll(asList(NAME_OF_RESOURCE_CREATE_OPTIONS),"Use multiple times to define names of resources that should be created.");
		OptionSpecBuilder softwareSystemBuilder = parser.acceptsAll(asList(SOFTWARE_SYSTEM_OPTIONS),"Use multiple times to define multiple software.");

		OptionSpec<String> igPath = igPathBuilder.withRequiredArg().describedAs("root directory of the ig");
		OptionSpec<String> igVersion = igVersionBuilder.withOptionalArg().describedAs("ig fhir version");
		OptionSpec<String> igOutputEncoding = igOutputEncodingBuilder.withOptionalArg().describedAs("desired output encoding for resources");
		OptionSpec<String> resourceNamesToCreate = nameOfResourceToCreateBuilder.withOptionalArg().describedAs("Name to be used for resources created");
		OptionSpec<String> softwareSystem = softwareSystemBuilder.withOptionalArg().describedAs("Software System name and version");

		parser.acceptsAll(asList(OPERATION_OPTIONS),"The operation to run.");

		OptionSpec<Void> help = parser.acceptsAll(asList(ArgUtils.HELP_OPTIONS), "Show this help page").forHelp();

		return parser;
	}

	@SuppressWarnings("unchecked")
	public ScaffoldParameters parseAndConvert(String[] args) {
		OptionParser parser = build();
		OptionSet options = ArgUtils.parse(args, parser);

		ArgUtils.ensure(OPERATION_OPTIONS[0], options);

		String igPath = (String)options.valueOf(IG_PATH_OPTIONS[0]);
		List<String> namesOfResources = (List<String>)options.valuesOf(NAME_OF_RESOURCE_CREATE_OPTIONS[0]);
		List<String> softwareSystems = (List<String>)options.valuesOf(SOFTWARE_SYSTEM_OPTIONS[0]);
		ArrayList<SoftwareSystem> softwareSystemsList = new ArrayList<SoftwareSystem>();
		for (String system : softwareSystems) {
			String name = system.split("=")[0];
			String version = system.split("=")[1];
			SoftwareSystem softwareSystem = new SoftwareSystem(name, version, "CQFramework");
			softwareSystemsList.add(softwareSystem);
		}

		String igVersion = (String)options.valueOf(IG_VERSION_OPTIONS[0]);
		String igEncoding = (String)options.valueOf(IG_OUTPUT_ENCODING[0]);
		IOUtils.Encoding outputEncodingEnum = IOUtils.Encoding.JSON;
		if (igEncoding != null) {
			outputEncodingEnum = IOUtils.Encoding.parse(igEncoding.toLowerCase());
		}

		// TODO: Expose as Arg
		List<String> resourceTypesToCreate = new ArrayList<String>();
		resourceTypesToCreate.add("Library");
		resourceTypesToCreate.add("Measure");

		Map<String, List<String>> resourcesToCreate = new HashMap<String, List<String>>();
		for (String name : namesOfResources) {
			resourcesToCreate.put(name, resourceTypesToCreate);
		}

		ScaffoldParameters sp = new ScaffoldParameters();
		sp.igPath = igPath;
		sp.igVersion = igVersion;
		sp.resourcesToScaffold = resourcesToCreate;
		sp.softwareSystems = softwareSystemsList;
		sp.outputEncoding = outputEncodingEnum;

		return sp;
	}

}
