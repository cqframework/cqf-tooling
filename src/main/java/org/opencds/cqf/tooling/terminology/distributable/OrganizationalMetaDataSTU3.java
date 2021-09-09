package org.opencds.cqf.tooling.terminology.distributable;

import org.hl7.fhir.dstu3.model.*;

public class OrganizationalMetaDataSTU3 {
    private String canonicalUrlBase;
    private String copyright;
    private String jurisdiction;
    private String publisher;
    private String contactName;
    private String contactSystem;
    private String contactValue;

    private String snomedVersion;

    private final String JURISDICTION_URL = "urn:iso:std:iso:3166";
    private final String CONTACTPOINT_URL = "http://hl7.org/fhir/StructureDefinition/ContactPoint";

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

/*
        org.hl7.fhir.r4.model.ContactDetail r4ContactDetail = new org.hl7.fhir.r4.model.ContactDetail();
        r4ContactDetail.addTelecom()
        org.hl7.fhir.dstu3.model.ContactDetail contactDetail = new ContactDetail();
        contactDetail.addTelecom()
*/

        if (contactName != null || contactSystem != null || contactValue != null) {
            ContactPoint contactPoint = new ContactPoint()
                    .setSystem(ContactPoint
                            .ContactPointSystem.fromCode(contactSystem))
                    .setValue(contactValue);

            valueSet.addExtension(new Extension().setUrl(CONTACTPOINT_URL).setValue(contactPoint));
        }
    }

    public String getContactame() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactTelecomSystem() {
        return contactSystem;
    }

    public void setContactTelecomSystem(String contactSystem) {
        this.contactSystem = contactSystem;
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setContactTelecomValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public String getSnomedVersion() {
        return snomedVersion;
    }

    public void setSnomedVersion(String snomedVersion) {
        this.snomedVersion = snomedVersion;
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


}
