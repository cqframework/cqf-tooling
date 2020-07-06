package org.opencds.cqf.terminology.distributable;

import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang.WordUtils;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class DistributableValueSetMeta {

    private String id;
    private String version;
    private String identifier;
    private String name;
    private String title;
    private String status;
    private String experimental;
    private String date;
    private String description;
    private String purpose;
    private String compose;

    private String rulesText;
    private String warning;
    private String purposeClinicalFocus;
    private String purposeDataElementScope;
    private String purposeInclusionCriteria;
    private String purposeExclusionCriteria;

    final String RULES_TEXT_URL = "http://hl7.org/fhir/StructureDefinition/valueset-rules-text";
    final String WARNING_URL = "http://hl7.org/fhir/StructureDefinition/valueset-warning";
    final String CLINICAL_FOCUS_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus";
    final String DATA_ELEMENT_SCOPE_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope";
    final String INCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria";
    final String EXCLUSION_CRITERIA_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria";
    final String DISTRIBUTABLE_PROFILE_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-distributablevalueset";
    final String PUBLISHABLE_PROFILE_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-publishablevalueset";

    public ValueSet populate(FhirContext fhirContext) {
        ValueSet vs = new ValueSet();
        vs.setId(id);
        vs.setMeta(new Meta().addProfile(DISTRIBUTABLE_PROFILE_URL).addProfile(PUBLISHABLE_PROFILE_URL));
        vs.setVersion(version);
        vs.setName(name == null || name.isEmpty() ? WordUtils.capitalizeFully(id.replaceAll("-", " ")).replaceAll(" ", "_") : WordUtils.capitalizeFully(name));
        vs.setTitle(title == null || title.isEmpty() ? WordUtils.capitalizeFully(id.replaceAll("-", " ")) : WordUtils.capitalizeFully(title));
        vs.setStatus(status == null || status.isEmpty() ? Enumerations.PublicationStatus.ACTIVE : Enumerations.PublicationStatus.fromCode(status));
        vs.setExperimental(experimental == null || experimental.isEmpty() || Boolean.parseBoolean(experimental));
        vs.setDate(date == null ? Date.from(Instant.now()) : new DateType(date).getValue());
        vs.setDescription(description);
        vs.setPurpose(purpose);

        if (rulesText != null) {
            vs.addExtension(new Extension().setUrl(RULES_TEXT_URL).setValue(new MarkdownType(rulesText)));
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
                //
                System.out.println("Error parsing compose for: " + vs.getId());
            }
        }

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public String getRulesText() {
        return rulesText;
    }

    public void setRulesText(String rulesText) {
        this.rulesText = rulesText;
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
