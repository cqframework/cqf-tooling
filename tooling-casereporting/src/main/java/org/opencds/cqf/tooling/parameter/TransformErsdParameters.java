package org.opencds.cqf.tooling.parameter;

import java.util.HashSet;
import org.opencds.cqf.tooling.utilities.IOUtils;

public class TransformErsdParameters {
    public String pathToBundle; // -pathtobundle (-ptb)
    public String outputPath; // -outputpath (-op)
    public String outputFileName; // -outputfilename (-ofn)
    public String pathToV2PlanDefinition; // -pathtoplandefinition (-ptpd)
    public HashSet<IOUtils.Encoding> outputFileEncodings; // -encoding (-e)
    public boolean prettyPrintOutput; // -prettyprintoutput (-ppo)
}
