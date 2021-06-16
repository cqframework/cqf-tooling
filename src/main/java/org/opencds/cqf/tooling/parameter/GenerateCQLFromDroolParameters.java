package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.cql_generation.drool.visitor.DroolToElmVisitor.CQLTYPES;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
/**
 * @author Joshua Reynolds
 */
public class GenerateCQLFromDroolParameters {
    public String outputPath;
    public String inputFilePath;
    public Encoding encoding;
    public String fhirVersion;
    public CQLTYPES type;
}
