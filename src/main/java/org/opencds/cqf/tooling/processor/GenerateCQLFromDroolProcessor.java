package org.opencds.cqf.tooling.processor;

import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class GenerateCQLFromDroolProcessor {

	public static void generate(GenerateCQLFromDroolParameters params) {
		CqlGenerator generator = new DroolCqlGenerator(params.outputPath, params.type);
		if (params.encoding.equals(IOUtils.Encoding.JSON)) {
			generator.generate(params.inputFilePath.toString(), params.fhirVersion);
		} else {
			throw new IllegalArgumentException("encoding " + params.encoding.toString() + " not supported yet.");
		}
	}

}
