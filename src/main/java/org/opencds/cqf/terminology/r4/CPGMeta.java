package org.opencds.cqf.terminology.r4;

import ca.uhn.fhir.context.FhirContext;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.*;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
public class CPGMeta {
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

    private final String KEYWORD_URL = "http://hl7.org/fhir/StructureDefinition/valueset-keyWord";
    private final String RULES_TEXT_URL = "http://hl7.org/fhir/StructureDefinition/valueset-rules-text";
    private final String EXPRESSION_URL = "http://hl7.org/fhir/StructureDefinition/valueset-expression";
    private final String WARNING_URL = "http://hl7.org/fhir/StructureDefinition/valueset-warning";
    private final String CLINICAL_FOCUS_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus";
    private final String DATA_ELEMENT_SCOPE_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope";
    private final String INCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria";
    private final String EXCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria";

    public ValueSet populate(FhirContext fhirContext) {
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
            vs.addExtension(new Extension().setUrl(KEYWORD_URL).setValue(new StringType(keyword)));
        }
        if (rulesText != null) {
            vs.addExtension(new Extension().setUrl(RULES_TEXT_URL).setValue(new StringType(rulesText)));
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
                                            .setLanguage(Expression.ExpressionLanguage.fromCode(expressionLanguage))
                                            .setExpression(expressionExpression)
                                            .setReference(expressionReference)
                            )
            );
        }
        if (warning != null) {
            vs.addExtension(new Extension().setUrl(WARNING_URL).setValue(new StringType(warning)));
        }
        if (purposeClinicalFocus != null) {
            vs.addExtension(new Extension().setUrl(CLINICAL_FOCUS_URL).setValue(new StringType(purposeClinicalFocus)));
        }
        if (purposeDataElementScope != null) {
            vs.addExtension(new Extension().setUrl(DATA_ELEMENT_SCOPE_URL).setValue(new StringType(purposeDataElementScope)));
        }
        if (purposeInclusionCriteria != null) {
            vs.addExtension(new Extension().setUrl(INCLUSION_CRITERIA_URL).setValue(new StringType(purposeInclusionCriteria)));
        }
        if (purposeExclusionCriteria != null) {
            vs.addExtension(new Extension().setUrl(EXCLUSION_CRITERIA_URL).setValue(new StringType(purposeExclusionCriteria)));
        }
        if (compose != null) {
            try {
                ValueSet tempVs = fhirContext.newXmlParser().parseResource(ValueSet.class, "<ValueSet>" + compose + "</ValueSet>");
                vs.setCompose(tempVs.getCompose());
            } catch (Exception e) {
                String s = "s";
            }
        }

        return vs;
    }
}
