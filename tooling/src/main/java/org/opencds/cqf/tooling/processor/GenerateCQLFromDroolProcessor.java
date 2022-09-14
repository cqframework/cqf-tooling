package org.opencds.cqf.tooling.processor;

import org.opencds.cqf.tooling.cql_generation.CqlGenerator;
import org.opencds.cqf.tooling.cql_generation.drool.DroolCqlGenerator;
import org.opencds.cqf.tooling.parameter.GenerateCQLFromDroolParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
/**
 * @author Joshua Reynolds
 */
public class GenerateCQLFromDroolProcessor {
	public static void generate(GenerateCQLFromDroolParameters params) {
		CqlGenerator generator = new DroolCqlGenerator(params.type);
		if (params.encoding.equals(IOUtils.Encoding.JSON)) {
			generator.generateAndWriteToFile(params.inputFilePath, params.outputPath, params.fhirVersion);
		} else {
			throw new IllegalArgumentException("encoding " + params.encoding + " not supported yet.");
		}
	}
}
