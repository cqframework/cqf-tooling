package org.opencds.cqf.terminology.distributable;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ValueSet;

public class OrganizationalMetaData {

    private String canonicalUrlBase;
    private String copyright;
    private String jurisdiction;
    private String publisher;
    private String authorName;
    private String authorTelecomSystem;
    private String authorTelecomValue;

    private final String JURISDICTION_URL = "urn:iso:std:iso:3166";
    private final String AUTHOR_URL = "http://hl7.org/fhir/StructureDefinition/valueset-author";

    public void populate(ValueSet valueSet) {
        if (!valueSet.hasId()) {
            throw new RuntimeException("Metadata template must include an id");
        }
        valueSet.setUrl(canonicalUrlBase + "/ValueSet/" + valueSet.getId());
        valueSet.setCopyright(copyright);
        valueSet.addJurisdiction(
                new CodeableConcept().addCoding(new Coding().setSystem(JURISDICTION_URL).setCode(jurisdiction))
        );
        valueSet.setPublisher(publisher);

        if (authorName != null || authorTelecomSystem != null || authorTelecomValue != null) {
            ContactDetail authorDetail =
                    new ContactDetail()
                            .setName(authorName)
                            .addTelecom(
                                    new ContactPoint()
                                            .setSystem(ContactPoint.ContactPointSystem.fromCode(authorTelecomSystem))
                                            .setValue(authorTelecomValue)
                            );

            valueSet.addExtension(new Extension().setUrl(AUTHOR_URL).setValue(authorDetail));
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

}
