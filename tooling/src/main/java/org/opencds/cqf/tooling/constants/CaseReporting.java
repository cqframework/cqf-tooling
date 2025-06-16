package org.opencds.cqf.tooling.constants;

public class CaseReporting {

    private CaseReporting() {}

    public static final String PUBLISHER = "Association of Public Health Laboratories (APHL)";
    public static final String VALUESETAUTHOREXTENSIONURL = "http://hl7.org/fhir/StructureDefinition/valueset-author";
    public static final String VALUESETSTEWARDEXTENSIONURL = "http://hl7.org/fhir/StructureDefinition/valueset-steward";
    public static final String GROUPERVALUESETAUTHOR = "CSTE Author";
    public static final String GROUPERVALUESETSTEWARD = "CSTE Steward";
    public static final String CANONICALBASE = "https://tes.tools.aimsplatform.org/api/fhir";
    public static final String MANIFESTCANONICALTAIL = "tes-content-library";
    public static final String MANIFESTURL = CANONICALBASE + "/" + MANIFESTCANONICALTAIL;
    public static final String VSMUSAGECONTEXTTYPESYSTEMURL = "http://aphl.org/fhir/vsm/CodeSystem/usage-context-type";
    public static final String USAGECONTEXTTYPESYSTEMURL = "http://terminology.hl7.org/CodeSystem/usage-context-type";
    public static final String USPHUSAGECONTEXTURL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context";
    public static final String SEARCHPARAMSYSTEMLIBRARYDEPENDSON = "http://hl7.org/fhir/Library#relatedArtifact.dependsOn";
    public static final String SEARCHPARAMSYSTEMLIBRARYCONTEXTTYPEVALUE = "http://hl7.org/fhir/ValueSet#useContext.context-type-value";
    public static final String SEARCHPARAMUSECONTEXTVALUEGROUPERTYPECONDITIONGROUPER = "grouper-type$http://aphl.org/fhir/vsm/CodeSystem/usage-context-type|condition-grouper";

    // Condition Groupers Sheet Layout
    public static final int CONDITIONGROUPINGSSHEETINDEX = 1;
    public static final int CONDITIONGROUPINGIDENTIFIERINDEX = 1;
    public static final int CONDITIONGROUPINGGENERATEDNAMEINDEX = 2;
    public static final int CONDITIONGROUPINGTITLEINDEX = 3;
    public static final int REPORTINGSPECIFICATIONTITLEINDEX = 4;
    public static final int REPORTINGSPECIFICATIONCONDITIONCODEINDEX = 5;
    public static final int REPORTINGSPECIFICATIONCONDITIONDESCRIPTIONINDEX = 6;

    // Additional Context Groupers Sheet Layout
    public static final int ADDITIONALCONTEXTGROUPERSHEETINDEX = 2;
    public static final int ADDITIONALCONTEXTGROUPERTARGETCONDITIONGROUPERURLCOLINDEX = 0;
    public static final int ADDITIONALCONTEXTGROUPERTARGETCONDITIONGROUPERTITLECOLINDEX = 1;
    public static final int ADDITIONALCONTEXTGROUPERGENERATEDURLCOLINDEX = 2;
    public static final int ADDITIONALCONTEXTGROUPERGENERATEDTITLECOLINDEX = 3;
    public static final int ADDITIONALCONTEXTGROUPERTITLECOLINDEX = 4;
    public static final int ADDITIONALCONTEXTGROUPERCODECOLINDEX = 5;
    public static final int ADDITIONALCONTEXTGROUPERCODEDISPLAYCOLINDEX = 6;
    public static final int ADDITIONALCONTEXTGROUPERCODESYSTEMURLCOLINDEX = 7;
}