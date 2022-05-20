package org.opencds.cqf.tooling.parameter;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.HashSet;

public class TransformErsdParameters {
    public String pathToBundle; // -pathtobundle (-ptb)
    public String outputPath; // -outputpath (-op)
    public String pathToV2PlanDefinition; // -pathtoplandefinition (-ptpd)
    public HashSet<IOUtils.Encoding> encodings; // -encoding (-e)
}