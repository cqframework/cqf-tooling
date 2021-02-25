package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.individual_tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

public class GenerateCQLFromDroolParameters {
    public String outputPath;
    public String inputFilePath;
    public Encoding encoding;
    public String fhirVersion;
    public CQLTYPES type;
}
