package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class NpmUtils {
   private static final Logger logger = LoggerFactory.getLogger(NpmUtils.class);

   private NpmUtils() {}

   public static class PackageLoaderValidationSupport extends PrePopulatedValidationSupport {

      public PackageLoaderValidationSupport(FhirContext fhirContext) {
         super(fhirContext);
      }

      public void loadPackage(NpmPackage npmPackage) throws IOException {
         if (npmPackage.getFolders().containsKey("package")) {
            loadResourcesFromPackage(npmPackage);
            loadBinariesFromPackage(npmPackage);
         }
      }

      private void loadResourcesFromPackage(NpmPackage thePackage) {
         NpmPackage.NpmPackageFolder packageFolder = thePackage.getFolders().get("package");

         for (String nextFile : packageFolder.listFiles()) {
            if (nextFile.toLowerCase(Locale.US).endsWith(".json")) {
               String input = new String(packageFolder.getContent().get(nextFile), StandardCharsets.UTF_8);
               IBaseResource resource = getFhirContext().newJsonParser().parseResource(input);
               super.addResource(resource);
            }
         }
      }

      private void loadBinariesFromPackage(NpmPackage thePackage) throws IOException {
         List<String> binaries = thePackage.list("other");
         for (String binaryName : binaries) {
            addBinary(TextFile.streamToBytes(thePackage.load("other", binaryName)), binaryName);
         }
      }
   }

   public static PackageLoaderValidationSupport getNpmPackageLoaderValidationSupport(FhirContext fhirContext, List<String> packages) {
      NpmPackage npmPackage;
      PackageLoaderValidationSupport validationSupport = new NpmUtils.PackageLoaderValidationSupport(fhirContext);
      for (String packageUrl : packages) {
         try {
            npmPackage = NpmPackage.fromUrl(packageUrl);
            validationSupport.loadPackage(npmPackage);
         } catch (IOException e) {
            logger.warn("Encountered an issue when attempting to resolve package from URL: {}", packageUrl, e);
         }
      }
      return validationSupport;
   }

   public static org.hl7.fhir.r4.context.SimpleWorkerContext getR4WorkerContext(String packageUrl) {
      try {
         NpmPackage npmPackage = NpmPackage.fromUrl(packageUrl);
         org.hl7.fhir.r4.context.SimpleWorkerContext workerContext = org.hl7.fhir.r4.context.SimpleWorkerContext.fromPackage(npmPackage);
         workerContext.setExpansionProfile(new Parameters());
         return workerContext;
      } catch (IOException e) {
         logger.warn("Encountered an issue when attempting to resolve package from URL: {}", packageUrl, e);
      }
      return null;
   }
}
