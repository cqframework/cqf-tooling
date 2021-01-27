package org.opencds.cqf.terminology.r4;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.*;

@Getter
@Setter
public class OrganizationalMeta {
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

        if (approvalDate != null) {
            valueSet.addExtension(new Extension().setUrl(APPROVAL_DATE_URL).setValue(new DateType(approvalDate)));
        }
        if (effectiveDate != null) {
            valueSet.addExtension(new Extension().setUrl(EFFECTIVE_DATE_URL).setValue(new DateType(effectiveDate)));
        }
        if (lastReviewDate != null) {
            valueSet.addExtension(new Extension().setUrl(LAST_REVIEW_DATE_URL).setValue(new DateType(lastReviewDate)));
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

            valueSet.addExtension(new Extension().setUrl(AUTHOR_URL).setValue(authorDetail));
        }
    }
}
