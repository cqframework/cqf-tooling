package org.opencds.cqf.tooling.constants;

public class CaseReporting {

    private CaseReporting() {}

    public static final String PUBLISHER = "Association of Public Health Laboratories (APHL)";
    public static final String VALUESETAUTHOREXTENSIONURL = "http://hl7.org/fhir/StructureDefinition/valueset-author";
    public static final String VALUESETSTEWARDEXTENSIONURL = "http://hl7.org/fhir/StructureDefinition/valueset-steward";
    public static final String CONDITIONGROUPERVALUESETAUTHOR = "CSTE Author";
    public static final String CONDITIONGROUPERVALUESETSTEWARD = "CSTE Steward";
    public static final String CANONICALBASE = "https://tes.tools.aimsplatform.org/api/fhir";
    public static final String MANIFESTID = "tes-content-library";
    public static final String MANIFESTURL = CANONICALBASE + "/" + MANIFESTID;
    public static final String VSMUSAGECONTEXTTYPESYSTEMURL = "http://aphl.org/fhir/vsm/CodeSystem/usage-context-type";
    public static final String USAGECONTEXTTYPESYSTEMURL = "http://terminology.hl7.org/CodeSystem/usage-context-type";
    public static final String USPHUSAGECONTEXTURL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context";
    public static final String SEARCHPARAMSYSTEMLIBRARYDEPENDSON = "http://hl7.org/fhir/Library#relatedArtifact.dependsOn";
    public static final String SEARCHPARAMSYSTEMLIBRARYCONTEXTTYPEVALUE = "http://hl7.org/fhir/ValueSet#useContext.context-type-value";
    public static final String SEARCHPARAMUSECONTEXTVALUEGROUPERTYPECONDITIONGROUPER = "grouper-type$http://aphl.org/fhir/vsm/CodeSystem/usage-context-type|condition-grouper";
    public static final int CONDITIONGROUPINGSSHEETINDEX = 0;
    public static final int CONDITIONGROUPINGTITLEINDEX = 1;
    public static final int REPORTINGSPECIFICATIONNAMEINDEX = 2;
    public static final int REPORTINGSPECIFICATIONCONDITIONCODEINDEX = 3;
    public static final int REPORTINGSPECIFICATIONCONDITIONDESCRIPTIONINDEX = 4;
}