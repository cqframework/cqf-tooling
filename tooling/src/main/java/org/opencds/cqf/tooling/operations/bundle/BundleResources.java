package org.opencds.cqf.tooling.operations.bundle;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.util.BundleBuilder;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

@Operation(name = "BundleResources")
public class BundleResources implements ExecutableOperation {
   @OperationParam(alias = { "ptr", "pathtoresources" }, setter = "setPathToResources", required = true)
   private String pathToResources;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4")
   private String version;
   @OperationParam(alias = { "t", "type" }, setter = "setType", defaultValue = "transaction")
   private String type;
   @OperationParam(alias = { "bid", "bundleid" }, setter = "setBundleId")
   private String bundleId;
   @OperationParam(alias = { "od", "outputDir" }, setter = "setOutputDirectory")
   private String outputDirectory;

   @Override
   public void execute() {
      FhirContext context = FhirContextCache.getContext(version);
      IBaseBundle bundle = bundleResources(context, bundleId, BundleTypeEnum.valueOf(type),
              IOUtils.readResources(IOUtils.getFilePaths(pathToResources, true), context));
      IOUtils.writeResource(bundle, outputDirectory == null ? pathToResources : outputDirectory,
              IOUtils.Encoding.parse(encoding), context);
   }

   public static IBaseBundle bundleResources(@Nonnull FhirContext fhirContext,
                                             String bundleId, BundleTypeEnum type,
                                             @Nonnull List<IBaseResource> resourcesToBundle) {
      BundleBuilder builder = new BundleBuilder(fhirContext);
      if (type == BundleTypeEnum.COLLECTION) {
         builder.setType(type.getCode());
         resourcesToBundle.forEach(builder::addCollectionEntry);
      }
      else {
         builder.setType("transaction");
         resourcesToBundle.forEach(builder::addTransactionUpdateEntry);
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

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
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

   public String getOutputDirectory() {
      return outputDirectory;
   }

   public void setOutputDirectory(String outputDirectory) {
      this.outputDirectory = outputDirectory;
   }
}
