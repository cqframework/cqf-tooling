package org.opencds.cqf.tooling.terminology.templateToValueSetGenerator;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;

public class CPGMeta {

    private static final Logger logger = LoggerFactory.getLogger(CPGMeta.class);

    private String id;
    private String version;
    private String name;
    private String title;
    private String status;
    private String experimental;
    private String date;
    private String description;
    private String purpose;
    private String compose;

    private String keyword;
    private String rulesText;
    private String expressionDescription;
    private String expressionName;
    private String expressionLanguage;
    private String expressionExpression;
    private String expressionReference;
    private String warning;
    private String purposeClinicalFocus;
    private String purposeDataElementScope;
    private String purposeInclusionCriteria;
    private String purposeExclusionCriteria;

    private final String KEYWORD_URL            = "http://hl7.org/fhir/StructureDefinition/valueset-keyWord";
    private final String WARNING_URL            = "http://hl7.org/fhir/StructureDefinition/valueset-warning";
    //R4 only
    private final String RULES_TEXT_URL         = "http://hl7.org/fhir/StructureDefinition/valueset-rules-text";
    private final String EXPRESSION_URL         = "http://hl7.org/fhir/StructureDefinition/valueset-expression";
    private final String CLINICAL_FOCUS_URL     = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus";
    private final String DATA_ELEMENT_SCOPE_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope";
    private final String INCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria";
    private final String EXCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria";

    public ValueSet populate(FhirContext fhirContext, String outputVersion) {
        ValueSet vs = new ValueSet();
        vs.setId(id);
        vs.setVersion(version);
        vs.setName(name == null ? id.replaceAll("-", "_") : name);
        vs.setTitle(title == null ? id.replaceAll("-", " ") : title);
        vs.setStatus(status == null ? Enumerations.PublicationStatus.ACTIVE : Enumerations.PublicationStatus.fromCode(status));
        vs.setExperimental(status == null ? true : Boolean.valueOf(experimental));
        vs.setDate(date == null ? Date.from(Instant.now()) : new DateType(date).getValue());
        vs.setDescription(description);
        vs.setPurpose(purpose);

        if (keyword != null) {
            vs = setExtension(vs, KEYWORD_URL, keyword);
        }
        if (warning != null) {
            vs = setExtension(vs, WARNING_URL, warning);
        }
        if (outputVersion.equalsIgnoreCase("r4")) {
            if (rulesText != null) {
                vs.addExtension(new Extension().setUrl(RULES_TEXT_URL).setValue(new MarkdownType(rulesText)));
            }
            if (purposeClinicalFocus != null) {
                vs = setExtension(vs, CLINICAL_FOCUS_URL, purposeClinicalFocus);
            }
            if (purposeDataElementScope != null) {
                vs = setExtension(vs, DATA_ELEMENT_SCOPE_URL, purposeDataElementScope);
            }
            if (purposeInclusionCriteria != null) {
                vs = setExtension(vs, INCLUSION_CRITERIA_URL, purposeInclusionCriteria);
            }
            if (purposeExclusionCriteria != null) {
                vs = setExtension(vs, EXCLUSION_CRITERIA_URL, purposeExclusionCriteria);
            }
            if (expressionDescription != null || expressionName != null || expressionLanguage != null
                    || expressionExpression != null || expressionReference != null)
            {
                vs.addExtension(
                        new Extension()
                                .setUrl(EXPRESSION_URL)
                                .setValue(
                                        new Expression()
                                                .setDescription(expressionDescription)
                                                .setName(expressionName)
                                                .setLanguage(expressionLanguage)
                                                .setExpression(expressionExpression)
                                                .setReference(expressionReference)
                                )
                );
            }
        }
        if (compose != null) {
            try {
                ValueSet tempVs = fhirContext.newXmlParser().parseResource(ValueSet.class, "<ValueSet>" + compose + "</ValueSet>");
                vs.setCompose(tempVs.getCompose());
            } catch (Exception e) {
                logger.info("An error occurred in the compose for the sheet with id: {}", this.id);
                logger.error(e.getMessage());
            }
        }

        return vs;
    }

    private ValueSet setExtension(ValueSet vs, String url, String valueToSet){
        vs.addExtension(new Extension().setUrl(url).setValue(new StringType(valueToSet)));
        return vs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExperimental() {
        return experimental;
    }

    public void setExperimental(String experimental) {
        this.experimental = experimental;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCompose() {
        return compose;
    }

    public void setCompose(String compose) {
        this.compose = compose;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRulesText() {
        return rulesText;
    }

    public void setRulesText(String rulesText) {
        this.rulesText = rulesText;
    }

    public String getExpressionDescription() {
        return expressionDescription;
    }

    public void setExpressionDescription(String expressionDescription) {
        this.expressionDescription = expressionDescription;
    }

    public String getExpressionName() {
        return expressionName;
    }

    public void setExpressionName(String expressionName) {
        this.expressionName = expressionName;
    }

    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public String getExpressionExpression() {
        return expressionExpression;
    }

    public void setExpressionExpression(String expressionExpression) {
        this.expressionExpression = expressionExpression;
    }

    public String getExpressionReference() {
        return expressionReference;
    }

    public void setExpressionReference(String expressionReference) {
        this.expressionReference = expressionReference;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getPurposeClinicalFocus() {
        return purposeClinicalFocus;
    }

    public void setPurposeClinicalFocus(String purposeClinicalFocus) {
        this.purposeClinicalFocus = purposeClinicalFocus;
    }

    public String getPurposeDataElementScope() {
        return purposeDataElementScope;
    }

    public void setPurposeDataElementScope(String purposeDataElementScope) {
        this.purposeDataElementScope = purposeDataElementScope;
    }

    public String getPurposeInclusionCriteria() {
        return purposeInclusionCriteria;
    }

    public void setPurposeInclusionCriteria(String purposeInclusionCriteria) {
        this.purposeInclusionCriteria = purposeInclusionCriteria;
    }

    public String getPurposeExclusionCriteria() {
        return purposeExclusionCriteria;
    }

    public void setPurposeExclusionCriteria(String purposeExclusionCriteria) {
        this.purposeExclusionCriteria = purposeExclusionCriteria;
    }
}
