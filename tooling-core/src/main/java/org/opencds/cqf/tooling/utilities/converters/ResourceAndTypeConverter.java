package org.opencds.cqf.tooling.utilities.converters;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_30_50;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv30_50.VersionConvertor_30_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ResourceAndTypeConverter {
   private static final VersionConvertor_30_50 stu3ToR5Converter = new VersionConvertor_30_50(new BaseAdvisor_30_50());
   private static final VersionConvertor_40_50 r4ToR5Converter = new VersionConvertor_40_50(new BaseAdvisor_40_50());

   private ResourceAndTypeConverter() {

   }

   public static IBaseDatatype convertType(FhirContext fhirContext, IBaseDatatype type) {
      switch (fhirContext.getVersion().getVersion()) {
         case R5:
            if (type instanceof org.hl7.fhir.dstu3.model.Type) {
               return stu3ToR5Type((org.hl7.fhir.dstu3.model.Type) type);
            }
            else if (type instanceof org.hl7.fhir.r4.model.Type)  {
               return r4ToR5Type((org.hl7.fhir.r4.model.Type) type);
            }
            else {
               throw new UnsupportedOperationException(
                       "Conversion to R5 type not supported for " + type.getClass().toString());
            }
         case R4:
            if (type instanceof org.hl7.fhir.r5.model.DataType) {
               return r5ToR4Type((org.hl7.fhir.r5.model.DataType) type);
            }
            else {
               throw new UnsupportedOperationException(
                       "Conversion to R4 type not supported for " + type.getClass().toString());
            }
         case DSTU3:
            if (type instanceof org.hl7.fhir.r5.model.DataType) {
               return r5ToStu3Type((org.hl7.fhir.r5.model.DataType) type);
            }
            else {
               throw new UnsupportedOperationException(
                       "Conversion to DSTU3 type not supported for " + type.getClass().toString());
            }
         default:
            throw new UnsupportedOperationException(
                    "Conversion not supported for types using version "
                            + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static org.hl7.fhir.r5.model.Resource convertToR5Resource(FhirContext fhirContext, IBaseResource resource) {
      switch (fhirContext.getVersion().getVersion()) {
         case R5: return (org.hl7.fhir.r5.model.Resource) resource;
         case R4: return r4ToR5Resource(resource);
         case DSTU3: return stu3ToR5Resource(resource);
         default:
            throw new UnsupportedOperationException(
                    "Conversion to R5 not supported for resources using version "
                            + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static IBaseResource convertFromR5Resource(FhirContext fhirContext, org.hl7.fhir.r5.model.Resource resource) {
      switch (fhirContext.getVersion().getVersion()) {
         case R5: return resource;
         case R4: return r5ToR4Resource(resource);
         case DSTU3: return r5ToStu3Resource(resource);
         default:
            throw new UnsupportedOperationException(
                    "Conversion from R5 not supported for resources using version "
                            + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static org.hl7.fhir.r5.model.Resource stu3ToR5Resource(IBaseResource resourceToConvert) {
      return stu3ToR5Converter.convertResource((org.hl7.fhir.dstu3.model.Resource) resourceToConvert);
   }

   public static org.hl7.fhir.dstu3.model.Resource r5ToStu3Resource(IBaseResource resourceToConvert) {
      return stu3ToR5Converter.convertResource((org.hl7.fhir.r5.model.Resource) resourceToConvert);
   }

   public static org.hl7.fhir.r5.model.DataType stu3ToR5Type(org.hl7.fhir.dstu3.model.Type typeToConvert) {
      return stu3ToR5Converter.convertType(typeToConvert);
   }

   public static org.hl7.fhir.dstu3.model.Type r5ToStu3Type(org.hl7.fhir.r5.model.DataType typeToConvert) {
      return stu3ToR5Converter.convertType(typeToConvert);
   }

   public static org.hl7.fhir.r5.model.Resource r4ToR5Resource(IBaseResource resourceToConvert) {
      return r4ToR5Converter.convertResource((org.hl7.fhir.r4.model.Resource) resourceToConvert);
   }

   public static org.hl7.fhir.r4.model.Resource r5ToR4Resource(IBaseResource resourceToConvert) {
      return r4ToR5Converter.convertResource((org.hl7.fhir.r5.model.Resource) resourceToConvert);
   }

   public static org.hl7.fhir.r5.model.DataType r4ToR5Type(org.hl7.fhir.r4.model.Type typeToConvert) {
      return r4ToR5Converter.convertType(typeToConvert);
   }

   public static org.hl7.fhir.r4.model.Type r5ToR4Type(org.hl7.fhir.r5.model.DataType typeToConvert) {
      return r4ToR5Converter.convertType(typeToConvert);
   }

}
