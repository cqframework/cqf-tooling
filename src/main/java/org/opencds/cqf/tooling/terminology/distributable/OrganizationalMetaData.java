package org.opencds.cqf.tooling.terminology.distributable;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ValueSet;

import org.hl7.fhir.r4.model.DateType;

public class OrganizationalMetaData {

    private String canonicalUrlBase;
    private String copyright;
    private String jurisdiction;
    private String publisher;
    private String approvalDate;
    private String effectiveDate;
    private String lastReviewDate;
    private String authorName;
    private String authorTelecomSystem;
    private String authorTelecomValue;

    private String snomedVersion;

    private final String JURISDICTION_URL = "urn:iso:std:iso:3166";
    private final String APPROVAL_DATE_URL = "http://hl7.org/fhir/StructureDefinition/resource-approvalDate";
    private final String EFFECTIVE_DATE_URL = "http://hl7.org/fhir/StructureDefinition/valueset-effectiveDate";
    private final String LAST_REVIEW_DATE_URL = "http://hl7.org/fhir/StructureDefinition/resource-lastReviewDate";
    private final String AUTHOR_URL = "http://hl7.org/fhir/StructureDefinition/valueset-author";

    public void populate(ValueSet valueSet, String outputVersion) {
        if (!valueSet.hasId()) {
            throw new RuntimeException("Metadata template must include an id");
        }
        valueSet.setUrl(canonicalUrlBase + "/ValueSet/" + valueSet.getId());
        valueSet.setCopyright(copyright);
        valueSet.addJurisdiction(
                new CodeableConcept().addCoding(new Coding().setSystem(JURISDICTION_URL).setCode(jurisdiction))
        );
        valueSet.setPublisher(publisher);

        if (outputVersion.equalsIgnoreCase("r4")) {
           if (approvalDate != null) {
                valueSet.addExtension(new Extension().setUrl(APPROVAL_DATE_URL).setValue(new DateType(approvalDate)));
            }
            if (effectiveDate != null) {
                valueSet.addExtension(new Extension().setUrl(EFFECTIVE_DATE_URL).setValue(new DateType(effectiveDate)));
            }
            if (lastReviewDate != null) {
                valueSet.addExtension(new Extension().setUrl(LAST_REVIEW_DATE_URL).setValue(new DateType(lastReviewDate)));
            }
        }
        if (authorName != null || authorTelecomSystem != null || authorTelecomValue != null) {
            ContactDetail authorDetail =
                    new ContactDetail()
                            .setName(authorName)
                            .addTelecom(
                                    new ContactPoint()
                                            .setSystem(ContactPoint.ContactPointSystem.fromCode(authorTelecomSystem))
                                            .setValue(authorTelecomValue)
                            );
            if (outputVersion.equalsIgnoreCase("r4")) {
                valueSet.addExtension(new Extension().setUrl(AUTHOR_URL).setValue(authorDetail));
            }
            if (outputVersion.equalsIgnoreCase("stu3")) {
                valueSet.addContact(authorDetail);
            }
        }
    }

    public String getCanonicalUrlBase() {
        return canonicalUrlBase;
    }

    public void setCanonicalUrlBase(String canonicalUrlBase) {
        this.canonicalUrlBase = canonicalUrlBase;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorTelecomSystem() {
        return authorTelecomSystem;
    }

    public void setAuthorTelecomSystem(String authorTelecomSystem) {
        this.authorTelecomSystem = authorTelecomSystem;
    }

    public String getAuthorTelecomValue() {
        return authorTelecomValue;
    }

    public void setAuthorTelecomValue(String authorTelecomValue) {
        this.authorTelecomValue = authorTelecomValue;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getLastReviewDate() {
        return lastReviewDate;
    }

    public void setLastReviewDate(String lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }

    public String getSnomedVersion() {
        return snomedVersion;
    }

    public void setSnomedVersion(String snomedVersion) {
        this.snomedVersion = snomedVersion;
    }
}
