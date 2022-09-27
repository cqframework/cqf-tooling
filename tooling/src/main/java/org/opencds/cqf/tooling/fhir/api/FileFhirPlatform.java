package org.opencds.cqf.tooling.fhir.api;

import org.cqframework.fhir.api.FhirCapabilities;
import org.cqframework.fhir.api.FhirPlatform;
import org.cqframework.fhir.api.FhirService;
import org.cqframework.fhir.api.FhirTransactions;
import org.opencds.cqf.tooling.parameter.FileFhirPlatformParameters;

public class FileFhirPlatform implements FhirPlatform{
  private FileFhirPlatformParameters platformParams;

  public FileFhirPlatform(FileFhirPlatformParameters platformParams){
    this.platformParams = platformParams;
  }

  @Override
  public FileFhirDal dal(){
    return new FileFhirDal(this.platformParams);
  }

  @Override
  public FhirCapabilities capabilities() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends FhirService> T getService() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FhirTransactions transactions() {
    // TODO Auto-generated method stub
    return null;
  }
}