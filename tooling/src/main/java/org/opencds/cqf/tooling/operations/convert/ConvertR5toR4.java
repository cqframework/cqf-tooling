package org.opencds.cqf.tooling.operations.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import jakarta.annotation.Nonnull;

@Operation(name = "ConvertR5toR4")
public class ConvertR5toR4 implements ExecutableOperation {

   private static final Logger logger = LoggerFactory.getLogger(ConvertR5toR4.class);
   @OperationParam(alias = { "ptr", "pathtoresources" }, setter = "setPathToResources", required = true,
           description = "Path to the directory containing the resource files to be consolidated into the new bundle (required)")
   private String pathToResources;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting Bundle { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "t", "type" }, setter = "setType", defaultValue = "transaction",
           description = "The Bundle type as defined in the FHIR specification for the Bundle.type element (default transaction)")
   private String type;
   @OperationParam(alias = { "bid", "bundleid" }, setter = "setBundleId",
           description = "A valid FHIR ID to be used as the ID for the resulting FHIR Bundle (optional)")
   private String bundleId;
   @OperationParam(alias = { "op", "outputPath" }, setter = "setOutputPath",
           defaultValue = "src/main/resources/org/opencds/cqf/tooling/convertR5toR4/output",
           description = "The directory path to which the generated Bundle file should be written (default src/main/resources/org/opencds/cqf/tooling/convertR5toR4/output)")
   private String outputPath;

    @Override
    public void execute() {
      FhirContext context = FhirContextCache.getContext("r5");
      BundleTypeEnum bundleType = BundleUtils.getBundleType(type);
      if (bundleType == null) {
         logger.error("Invalid bundle type: {}", type);
      }
      else {
         IBaseBundle bundle = convertResources(context, bundleId, bundleType,
                 IOUtils.readResources(IOUtils.getFilePaths(pathToResources, true), context));
         IOUtils.writeResource(bundle, outputPath == null ? pathToResources : outputPath,
                 IOUtils.Encoding.parse(encoding), context);
      }
   }

    public static IBaseBundle convertResources(@Nonnull FhirContext fhirContext,
                                               String bundleId, BundleTypeEnum type,
                                               @Nonnull List<IBaseResource> resourcesToConvert) {
      VersionConvertor_40_50 versionConverter4050 = new VersionConvertor_40_50(new BaseAdvisor_40_50(true));

      List<org.hl7.fhir.r4.model.Resource> convertedResources = new ArrayList<>();
      for (IBaseResource resource: resourcesToConvert){
        if (resource instanceof org.hl7.fhir.r5.model.Resource) {
            convertedResources.add(versionConverter4050.convertResource((org.hl7.fhir.r5.model.Resource)resourcesToConvert));
        }
      }
        
      BundleBuilder builder = new BundleBuilder(fhirContext);
      if (type == BundleTypeEnum.COLLECTION) {
         builder.setType(type.getCode());
         convertedResources.forEach(builder::addCollectionEntry);
      }
      else {
         builder.setType("transaction");
         convertedResources.forEach(builder::addTransactionUpdateEntry);
      }
      IBaseBundle bundle = builder.getBundle();
      bundle.setId(bundleId == null ? UUID.randomUUID().toString() : bundleId);
      return bundle;
   }

   public String getPathToResources() {
      return pathToResources;
   }

   public void setPathToResources(String pathToResources) {
      this.pathToResources = pathToResources;
   }

   public String getEncoding() {
      return encoding;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public String getBundleId() {
      return bundleId;
   }

   public void setBundleId(String bundleId) {
      this.bundleId = bundleId;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputDirectory) {
      this.outputPath = outputDirectory;
   }
}
