package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class NpmUtils {

   private NpmUtils() {}

   public static class PackageLoaderValidationSupport extends PrePopulatedValidationSupport {

      public PackageLoaderValidationSupport(@NotNull FhirContext fhirContext) {
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
}
