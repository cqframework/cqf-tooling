package org.opencds.cqf.tooling.casereporting.tes;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.HashSet;

public class TESPackageGenerateParameters {
    public String version; // -version (-v)
    public String outputPath; // -outputpath (-op)
    public String outputFileName; // -outputfilename (-ofn)
    public String pathToInputBundle; // -pathtoinputbundle (-ptib)
    public String pathToConditionGrouperWorkbook; // -pathToConditionGrouperWorkbook (-ptcgw)
    public String pathToConditionCodeValueSet; // -pathToConditionCodeValueSet (-ptccvs)
    public HashSet<IOUtils.Encoding> outputFileEncodings; // -encoding (-e)
    public boolean prettyPrintOutput; // -prettyprintoutput (-ppo)
}