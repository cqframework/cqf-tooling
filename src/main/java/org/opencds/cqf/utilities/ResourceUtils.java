package org.opencds.cqf.utilities;

import java.util.List;

import org.hl7.fhir.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;

public class ResourceUtils 
{
    public static final String dstu3 = "dstu3";
    public static final String r4 = "r4";
    
    public static void setId(Resource resource)
    {
        setId(resource, false);
    }
   
    public static void setId(Resource resource, Boolean includeVersion)
    {
      //  resource.setId((resource.getType() + "-" + resource.getName() + includeVersion ? "-" + resource.getMeta().getVersionId() : "").replace('_', '-'));
    }

    public static FhirContext getFhirContext(String fhirVersion) {
      switch (fhirVersion) {
        case dstu3:
          return FhirContext.forDstu3();
        case r4:
          return FhirContext.forR4();
        default:
          throw new IllegalArgumentException("Unknown FHIR version: " + fhirVersion);
      }
    }
}
