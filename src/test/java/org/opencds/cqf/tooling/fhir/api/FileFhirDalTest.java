package org.opencds.cqf.tooling.fhir.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.model.Patient;
import org.junit.BeforeClass;
import org.opencds.cqf.tooling.CqfmSoftwareSystemTest;
import org.opencds.cqf.tooling.parameter.FileFhirPlatformParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;


public class FileFhirDalTest implements CqfmSoftwareSystemTest {
  private FileFhirPlatform platform;
  private FileFhirDal dal;
  private Patient patient;
  private final String resourceDir = "target/FileFhirDalTest";
  private Logger logger;

  public FileFhirDalTest() {
    FileFhirPlatformParameters platformParams = new FileFhirPlatformParameters();
    platformParams.fhirContext = FhirContext.forR4();
    platformParams.encoding = EncodingEnum.JSON;
    platformParams.resourceDir = this.resourceDir;

    this.platform = new FileFhirPlatform(platformParams);
    this.dal = platform.dal();

    this.patient = (Patient) new Patient().setId("TestPatient");
    this.patient.setId(this.patient.getIdElement().withResourceType("Patient"));

    this.logger = LoggerFactory.getLogger(this.getClass());
  }

  @BeforeClass
  public void setup() {
    File testDir = new File(resourceDir);
    if(!testDir.exists()){
      testDir.mkdirs();
    }
    logger.info("Beginning Test: FileFhirDalTest");
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
    logger.info("Finished Test: FileFhirDalTest");
  }
  
  @Test(priority = 1) // Create
  public void create(){
    logger.info("Running: FileFhirDalTest.create...");

    this.patient.setActive(false);

    dal.create(this.patient);

    File file = new File(resourceDir + "/Patient/TestPatient.json");

    logger.info(String.format("%s", file.exists()));

    assertTrue(file.exists());
  }

  @Test(priority = 2) // Read
  public void read(){
    logger.info("Running: FileFhirDalTest.read...");

    Patient readPatient = (Patient) dal.read(this.patient.getIdElement());

    assertFalse(readPatient.getActive());
  }

  @Test(priority = 3) // Update
  public void update() {
    logger.info("Running: FileFhirDalTest.update...");
    this.patient.setActive(true);

    dal.update(this.patient);

    Patient updatedPatient = (Patient) dal.read(this.patient.getIdElement());

    assertTrue(updatedPatient.getActive());
  }

  @Test(priority = 4) // Delete
  public void delete(){
    logger.info("Running: FileFhirDalTest.delete...");

    dal.delete(this.patient.getIdElement());

    File file = new File(resourceDir + "/Patient/TestPatient.json");

    assertFalse(file.exists());
  }

  @Test //No ResourceType
  public void noResourceType(){
    logger.info("Running: FileFhirDalTest.noResourceType...");
    
    Patient patient = (Patient) new Patient().setId("NoResourcePatient");
    dal.create(patient);

    File file = new File(resourceDir + "/Patient/NoResourcePatient.json");

    assertFalse(file.exists());
  }

}
