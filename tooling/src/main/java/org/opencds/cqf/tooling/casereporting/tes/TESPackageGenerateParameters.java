package org.opencds.cqf.tooling.casereporting.tes;

import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.Set;

public class TESPackageGenerateParameters {
    public String version; // -version (-v)
    public String releaseLabel; // -releaselabel (-rl)
    public String outputPath; // -outputpath (-op)
    public String outputFileName; // -outputfilename (-ofn)
    public String pathToInputBundle; // -pathtoinputbundle (-ptib)
    public String pathToConditionGrouperWorkbook; // -pathToConditionGrouperWorkbook (-ptcgw)
    public String pathToConditionCodeValueSet; // -pathToConditionCodeValueSet (-ptccvs)
    public Set<IOUtils.Encoding> outputFileEncodings; // -encoding (-e)
    public boolean writeConditionGroupers; // -writeconditiongroupers (-wcg)
    public boolean writeReportingSpecificationGroupers; // -writereportingspecificationgroupers (-wrsg)
    public boolean writeAdditionalContextGroupers; // -writeadditionalcontextgroupers (-wacg)
}