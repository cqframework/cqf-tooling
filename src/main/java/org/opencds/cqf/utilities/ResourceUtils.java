package org.opencds.cqf.utilities;

import java.util.List;

import org.hl7.fhir.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;

import ca.uhn.fhir.context.FhirContext;

public class ResourceUtils 
{
    public enum FhirVersion 
    { 
        DSTU3("dstu3"), R4("r4"); 

        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private FhirVersion(String string) 
        { 
            this.string = string; 
        }

        public static FhirVersion parse(String value) {
            switch (value) {
                case "dstu3": 
                    return DSTU3;
                case "r4":
                    return R4;
                default: 
                    throw new RuntimeException("Unable to parse FHIR version value:" + value);
            }
        }
    }
    
    public static void setId(Resource resource)
    {
        setId(resource, false);
    }
   
    public static void setId(Resource resource, Boolean includeVersion)
    {
      //  resource.setId((resource.getType() + "-" + resource.getName() + includeVersion ? "-" + resource.getMeta().getVersionId() : "").replace('_', '-'));
    }

    public static FhirContext getFhirContext(FhirVersion fhirVersion) {
      switch (fhirVersion) {
        case DSTU3:
          return FhirContext.forDstu3();
        case R4:
          return FhirContext.forR4();
        default:
          throw new IllegalArgumentException("Unknown FHIR version: " + fhirVersion);
      }
    }
}
