package org.opencds.cqf.tooling;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.tooling.fhir.api.FileFhirDal;
import org.opencds.cqf.tooling.fhir.api.FileFhirPlatform;
import org.opencds.cqf.tooling.parameter.FileFhirPlatformParameters;

import org.junit.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;


public class FileFhirDalTest implements CqfmSoftwareSystemTest {
  private FileFhirPlatform platform;
  private FileFhirDal dal;
  private Patient patient;
  private final String resourceDir = "./target/FileFhirDalTest";

  public FileFhirDalTest() {
    FileFhirPlatformParameters platformParams = new FileFhirPlatformParameters();
    platformParams.fhirContext = FhirContext.forR4();
    platformParams.encoding = EncodingEnum.JSON;
    platformParams.resourceDir = this.resourceDir;

    this.platform = new FileFhirPlatform(platformParams);
    this.dal = platform.dal();

    this.patient = (Patient) new Patient().setId("TestPatient");
    this.patient.setId(this.patient.getIdElement().withResourceType("Patient"));
  }

  @BeforeClass
  public void setup() {
    File testDir = new File(resourceDir);
    if(!testDir.exists()){
      testDir.mkdirs();
    }
  }

  @AfterClass
  public void tearDown(){
    File testDir = new File(resourceDir);
    if(testDir.exists()){
      try {
        FileUtils.deleteDirectory(testDir);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Cannot delete " + testDir + "\n" + e.getMessage());
      }
    }
  }
  
  @Test(priority = 1) // Create
  public void create(){
    this.patient.setActive(false);

    dal.create(this.patient);

    File file = new File(resourceDir + "/Patient/TestPatient.json");

    assertTrue(file.exists());
  }

  @Test(priority = 2) // Read
  public void read(){
    Patient readPatient = (Patient) dal.read(this.patient.getIdElement());

    assertFalse(readPatient.getActive());
  }

  @Test(priority = 3) // Update
  public void update() {
    this.patient.setActive(true);

    dal.update(this.patient);

    Patient updatedPatient = (Patient) dal.read(this.patient.getIdElement());

    assertTrue(updatedPatient.getActive());
  }

  @Test(priority = 4) // Delete
  public void delete(){
    dal.delete(this.patient.getIdElement());

    File file = new File(resourceDir + "/Patient/TestPatient.json");

    assertFalse(file.exists());
  }

  @Test //No ResourceType
  public void noResourceType(){
    Patient patient = (Patient) new Patient().setId("TestPatient");
    dal.create(patient);

    File file = new File(resourceDir + "/Patient/TestPatient.json");

    assertFalse(file.exists());
  }

}
