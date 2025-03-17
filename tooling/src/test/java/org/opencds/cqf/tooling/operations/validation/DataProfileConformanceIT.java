package org.opencds.cqf.tooling.operations.validation;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class DataProfileConformanceIT {

   private final FhirContext fhirContext = FhirContext.forR4Cached();

   @Test
   void testInvalidQiCorePatient() {
      DataProfileConformance dpc = new DataProfileConformance();
      dpc.setFhirContext(fhirContext);
      dpc.setPackageUrlsList(List.of("https://packages.simplifier.net/hl7.fhir.us.qicore/4.1.1"));
      dpc.setGeneralValidator();

      Bundle bundle = new Bundle();
      bundle.setType(Bundle.BundleType.COLLECTION);
      bundle.addEntry().setResource(invalidQiCorePatient());

      List<IBaseResource> validationResults = dpc.validatePatientData(bundle);
      Assert.assertEquals(validationResults.size(), 1);
      Assert.assertTrue(validationResults.get(0) instanceof Patient);
      Patient patient = (Patient) validationResults.get(0);
      Assert.assertTrue(patient.hasContained());
      Assert.assertEquals(patient.getContained().size(), 1);
      Assert.assertTrue(patient.getContained().get(0) instanceof OperationOutcome);
      OperationOutcome outcome = (OperationOutcome) patient.getContained().get(0);
      Assert.assertTrue(outcome.hasIssue());
      Assert.assertEquals((int) outcome.getIssue().stream().filter(issue -> issue.getSeverity().equals(OperationOutcome.IssueSeverity.ERROR)).count(), 2);
   }

   @Test
   void testValidQiCorePatient() {
      DataProfileConformance dpc = new DataProfileConformance();
      dpc.setFhirContext(fhirContext);
      dpc.setPackageUrlsList(List.of("https://packages.simplifier.net/hl7.fhir.us.qicore/4.1.1"));
      dpc.setGeneralValidator();

      Bundle bundle = new Bundle();
      bundle.setType(Bundle.BundleType.COLLECTION);
      bundle.addEntry().setResource(validQiCorePatient());

      List<IBaseResource> validationResults = dpc.validatePatientData(bundle);
      Assert.assertEquals(validationResults.size(), 1);
      Assert.assertTrue(validationResults.get(0) instanceof Patient);
      Patient patient = (Patient) validationResults.get(0);
      Assert.assertFalse(patient.hasContained());
   }

   private Patient invalidQiCorePatient() {
      // missing identifier and name
      Patient patient = new Patient();
      patient.setId("invalid");
      patient.setGender(Enumerations.AdministrativeGender.FEMALE);

      return patient;
   }

   private Patient validQiCorePatient() {
      // missing identifier and name
      Patient patient = new Patient();
      patient.setId("valid");
      patient.addIdentifier().setSystem("urn:oid:1.2.36.146.595.217.0.1").setValue("12345");
      patient.addName().setFamily("Chalmers").addGiven("Peter").addGiven("James");
      patient.setGender(Enumerations.AdministrativeGender.FEMALE);

      return patient;
   }
}
