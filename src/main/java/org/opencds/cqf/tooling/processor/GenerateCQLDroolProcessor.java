package org.opencds.cqf.tooling.processor;

import org.opencds.cqf.individual_tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.individual_tooling.cql_generation.drool.DroolIshCqlGenerator;
import org.opencds.cqf.tooling.parameter.GenerateCQLDroolParameters;

public class GenerateCQLDroolProcessor {

	public static void generate(GenerateCQLDroolParameters params) {
		CqlGenerator generator = new DroolIshCqlGenerator(params.outputPath);
		generator.generate(params.encoding.toString());
	}

}
