package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator.testcase;


import org.hl7.fhir.DataElement;

public class TestCaseOld {

    // Logic workbook
    private String name;
    private String condition;
    private String output;
    private String action;

    // Dict workbook
    private String id;
    private String activityId;
    private String description;
    private String dataElementId;
    private String dataElementLabel;
    private String dataElementDefinition;
    private String multipleChoice;
    private String dataType;
    private String inputOptions;
    private String calculation;
    private String qualitySubtype;
    private String valCondition;
    private String editable;
    private String required;
    private String skipLogic;
    private String linkages;
    private String notes;
    private String icd11Code;
    private String icd11Uri;
    private String icd11Comments;
    private String icd10Code;
    private String icd10Comments;
    private String loincCode;
    private String loincComments;
    private String ichiCode;
    private String ichiUri;
    private String ichiComments;
    private String icfCode;
    private String icfComments;
    private String snomedCode;
    private String snomedComments;
    private String section;
    private Boolean grayFlag;
    private ExpectedResult expectedResult;
    private DataElement inputDataElement;

    public void setSection(String section) { this.section = section; }

    public String getSection() { return this.section; }

    public String getId() { return this.id; }

    public String getOutput() { return this.output; }

    public void setOutput(String output) { this.output = output; }

    public String getAction() { return this.action; }

    public void setAction(String action) { this.action = action; }

    public String getCondition() { return this.condition; }

    public void setCondition(String condition) { this.condition = condition; }

    public void setGrayFlag(Boolean grayFlag) { this.grayFlag = grayFlag; }

    public Boolean getGrayFlag() { return this.grayFlag; }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataType() { return dataType; }

    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExpectedResult getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(ExpectedResult expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getDataElementId() { return dataElementId; }

    public void setDataElementId(String dataElementId) { this.dataElementId = dataElementId; }

    public String getDataElementDefinition() { return dataElementDefinition; }

    public void setDataElementDefinition(String dataElementDefinition) { this.dataElementDefinition = dataElementDefinition; }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLinkages() {
        return linkages;
    }

    public void setLinkages(String linkages) {
        this.linkages = linkages;
    }

    public String getSkipLogic() {
        return skipLogic;
    }

    public void setSkipLogic(String skipLogic) {
        this.skipLogic = skipLogic;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }

    public String getValCondition() {
        return valCondition;
    }

    public void setValCondition(String valCondition) {
        this.valCondition = valCondition;
    }

    public String getQualitySubtype() {
        return qualitySubtype;
    }

    public void setQualitySubtype(String qualitySubtype) {
        this.qualitySubtype = qualitySubtype;
    }

    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    public String getInputOptions() {
        return inputOptions;
    }

    public void setInputOptions(String inputOptions) {
        this.inputOptions = inputOptions;
    }

    public String getActivityId() { return this.activityId; }

    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getDataElementLabel() { return this.dataElementLabel; }

    public void setDataElementLabel(String dataElementLabel) { this.dataElementLabel = dataElementLabel; }

    public String getMultipleChoice() { return this.multipleChoice; }

    public void setMultipleChoice(String multipleChoice) { this.multipleChoice = multipleChoice; }

    public String getRequired() { return this.required; }

    public void setRequired(String required) { this.required = required; }

    public String getIcd11Code() {
        return icd11Code;
    }

    public void setIcd11Code(String icd11Code) {
        this.icd11Code = icd11Code;
    }

    public String getIcd11Uri() {
        return icd11Uri;
    }

    public void setIcd11Uri(String icd11Uri) {
        this.icd11Uri = icd11Uri;
    }

    public String getIcd11Comments() {
        return icd11Comments;
    }

    public void setIcd11Comments(String icd11Comments) {
        this.icd11Comments = icd11Comments;
    }

    public String getIcd10Code() {
        return icd10Code;
    }

    public void setIcd10Code(String icd10Code) {
        this.icd10Code = icd10Code;
    }

    public String getIcd10Comments() {
        return icd10Comments;
    }

    public void setIcd10Comments(String icd10Comments) {
        this.icd10Comments = icd10Comments;
    }

    public String getLoincCode() {
        return loincCode;
    }

    public void setLoincCode(String loingCode) {
        this.loincCode = loingCode;
    }

    public String getLoincComments() {
        return loincComments;
    }

    public void setLoincComments(String loincComments) {
        this.loincComments = loincComments;
    }

    public String getIchiCode() {
        return ichiCode;
    }

    public void setIchiCode(String ichiCode) {
        this.ichiCode = ichiCode;
    }

    public String getIchiUri() {
        return ichiUri;
    }

    public void setIchiUri(String ichiUri) {
        this.ichiUri = ichiUri;
    }

    public String getIchiComments() {
        return ichiComments;
    }

    public void setIchiComments(String ichiComments) {
        this.ichiComments = ichiComments;
    }

    public String getIcfCode() {
        return icfCode;
    }

    public void setIcfCode(String icfCode) {
        this.icfCode = icfCode;
    }

    public String getIcfComments() {
        return icfComments;
    }

    public void setIcfComments(String icfComments) {
        this.icfComments = icfComments;
    }

    public String getSnomedCode() {
        return snomedCode;
    }

    public void setSnomedCode(String snomedCode) {
        this.snomedCode = snomedCode;
    }

    public String getSnomedComments() {
        return snomedComments;
    }

    public void setSnomedComments(String snomedComments) {
        this.snomedComments = snomedComments;
    }

    public DataElement getInputDataElement() {
        return inputDataElement;
    }

    public void setInputDataElement(DataElement inputDataElement) {
        this.inputDataElement = inputDataElement;
    }
}
