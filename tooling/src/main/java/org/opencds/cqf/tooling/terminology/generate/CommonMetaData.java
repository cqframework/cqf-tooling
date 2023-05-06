package org.opencds.cqf.tooling.terminology.generate;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ImplementationGuide;

import java.util.Date;
import java.util.List;

public class CommonMetaData {
   private String publisher;
   private ContactDetail contact;
   private List<CodeableConcept> jurisdiction;
   private String copyright;
   private Date date;

   public CommonMetaData(ImplementationGuide ig) {
      publisher = ig.getPublisher();
      contact = ig.getContactFirstRep();
      jurisdiction = ig.getJurisdiction();
      copyright = ig.getCopyright();
      date = new Date();
   }

   public String getPublisher() {
      return publisher;
   }

   public void setPublisher(String publisher) {
      this.publisher = publisher;
   }

   public ContactDetail getContact() {
      return contact;
   }

   public void setContact(ContactDetail contact) {
      this.contact = contact;
   }

   public List<CodeableConcept> getJurisdiction() {
      return jurisdiction;
   }

   public void setJurisdiction(List<CodeableConcept> jurisdiction) {
      this.jurisdiction = jurisdiction;
   }

   public String getCopyright() {
      return copyright;
   }

   public void setCopyright(String copyright) {
      this.copyright = copyright;
   }

   public Date getDate() {
      return date;
   }

   public void setDate(Date date) {
      this.date = date;
   }
}
