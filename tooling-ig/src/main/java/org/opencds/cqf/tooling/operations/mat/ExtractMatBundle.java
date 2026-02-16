package org.opencds.cqf.tooling.operations.mat;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.ResourceUtil;
import ca.uhn.fhir.util.TerserUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Operation(name = "ExtractMatBundle")
public class ExtractMatBundle implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(ExtractMatBundle.class);

   @OperationParam(alias = { "ptb", "pathtobundle" }, setter = "pathToBundle", required = true,
           description = "Path to the exported MAT FHIR Bundle resource (required)")
   private String pathToBundle;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting extracted FHIR resources { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "sn", "suppressnarrative" }, setter = "setSuppressNarrative", defaultValue = "true",
           description = "Whether or not to suppress Narratives in extracted Measure resources (default true)")
   private Boolean suppressNarrative;
   @OperationParam(alias = { "op", "outputPath" }, setter = "setOutputPath",
           description = "The directory path to which the generated Bundle file should be written (default parent directory of -ptb)")
   private String outputPath;

   private FhirContext fhirContext;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      IBaseResource bundle = IOUtils.readResource(pathToBundle, fhirContext);
      if (outputPath == null) {
         outputPath = new File(pathToBundle).getParent();
      }
      if (bundle instanceof IBaseBundle) {
         processBundle((IBaseBundle) bundle);
      } else if (bundle == null) {
         logger.error("Unable to read Bundle resource at {}", pathToBundle);
      } else {
         String type = bundle.fhirType();
         logger.error("Expected a Bundle resource, found {}", type);
      }
   }

   public void processBundle(IBaseBundle bundle) {
      List<IBaseResource> resources = BundleUtil.toListOfResources(fhirContext, bundle);
      createDirectoryStructure();
      migrateResources(resources);
   }

   // Library access method
   public MatPackage getMatPackage(IBaseBundle bundle) {
      MatPackage matPackage = new MatPackage();
      List<IBaseResource> resources = BundleUtil.toListOfResources(fhirContext, bundle);
      FhirTerser terser = new FhirTerser(fhirContext);
      for (IBaseResource resource : resources) {
         if (resource.fhirType().equalsIgnoreCase("measure")
                 && Boolean.TRUE.equals(suppressNarrative)) {
            ResourceUtil.removeNarrative(fhirContext, resource);
         }
         if (resource.fhirType().equalsIgnoreCase("library")) {
            matPackage.addLibraryPackage(
                    new MatPackage.LibraryPackage().setLibrary(resource).setCql(
                            new String(Base64.decodeBase64(extractCql(resource, terser)))));
         } else if (resource.fhirType().equalsIgnoreCase("measure")) {
            matPackage.addMeasure(resource);
         } else {
            matPackage.addResource(resource);
         }
      }
      return matPackage;
   }

   private void migrateResources(List<IBaseResource> resources) {
      FhirTerser terser = new FhirTerser(fhirContext);
      for (IBaseResource resource : resources) {
         if (resource.fhirType().equalsIgnoreCase("measure")
                 && Boolean.TRUE.equals(suppressNarrative)) {
            ResourceUtil.removeNarrative(fhirContext, resource);
         }
         if (resource.fhirType().equalsIgnoreCase("library")) {
            extractLibrary(resource, terser);
         } else if (resource.fhirType().equalsIgnoreCase("measure")) {
            extractMeasure(resource, terser);
         } else {
            IOUtils.writeResource(resource, resourcesPath.toString(), IOUtils.Encoding.valueOf(encoding), fhirContext);
         }
      }
   }

   private void extractLibrary(IBaseResource library, FhirTerser terser) {
      String name = terser.getSinglePrimitiveValueOrNull(library, "name");
      if (name == null) {
         name = library.getIdElement().getIdPart();
      }
      IOUtils.writeResource(library, libraryOutputPath.toString(), IOUtils.Encoding.valueOf(encoding), fhirContext,
              false, name);
      String cql = extractCql(library, terser);
      if (cql != null) {
         String cqlPath = Paths.get(cqlOutputPath.toString(), name) + ".cql";
         try {
            FileUtils.writeByteArrayToFile(new File(cqlPath), Base64.decodeBase64(cql));
         } catch (IOException e) {
            logger.warn("Error writing CQL file: {}", cqlPath, e);
         }
      } else {
         logger.warn("Unable to extract CQL from Library: {}", name);
      }
   }

   private String extractCql(IBaseResource library, FhirTerser terser) {
      for (IBase attachment : TerserUtil.getValues(fhirContext, library, "content")) {
         String contentType = terser.getSinglePrimitiveValueOrNull(attachment, "contentType");
         if (contentType != null && contentType.equals("text/cql")) {
            return terser.getSinglePrimitiveValueOrNull(attachment, "data");
         }
      }
      return null;
   }

   private void extractMeasure(IBaseResource measure, FhirTerser terser) {
      String name = terser.getSinglePrimitiveValueOrNull(measure, "name");
      if (name == null) {
         name = measure.getIdElement().getIdPart();
      }
      IOUtils.writeResource(measure, measureOutputPath.toString(), IOUtils.Encoding.valueOf(encoding), fhirContext,
              false, name);
   }

   private Path newOutputPath;
   private Path resourcesPath;
   private Path libraryOutputPath;
   private Path measureOutputPath;
   private Path cqlOutputPath;
   private void createDirectoryStructure() {
      String targetDir = "bundles";
      if (!outputPath.contains(targetDir)) {
         newOutputPath = Paths.get(outputPath, targetDir, "input");
      } else {
         newOutputPath = Paths.get(outputPath.substring(0, outputPath.lastIndexOf(targetDir)), "input");
      }
      resourcesPath = Paths.get(newOutputPath.toString(), "resources");
      libraryOutputPath = Paths.get(resourcesPath.toString(), "library");
      measureOutputPath = Paths.get(resourcesPath.toString(), "measure");
      cqlOutputPath = Paths.get(newOutputPath.toString(), "cql");

      String warningMessage = "Unable to create directory at {}";
      if (!libraryOutputPath.toFile().mkdirs()) {
         logger.warn(warningMessage, libraryOutputPath);
      }
      if (!measureOutputPath.toFile().mkdirs()) {
         logger.warn(warningMessage, measureOutputPath);
      }
      if (!cqlOutputPath.toFile().mkdirs()) {
         logger.warn(warningMessage, cqlOutputPath);
      }
   }

   public String getPathToBundle() {
      return pathToBundle;
   }

   public void setPathToBundle(String pathToBundle) {
      this.pathToBundle = pathToBundle;
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

   public Boolean getSuppressNarrative() {
      return suppressNarrative;
   }

   public void setSuppressNarrative(Boolean suppressNarrative) {
      this.suppressNarrative = suppressNarrative;
   }

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputPath) {
      this.outputPath = outputPath;
   }

   public FhirContext getFhirContext() {
      return fhirContext;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }

   public static class MatPackage {
      private final List<LibraryPackage> libraryPackages;
      private final List<IBaseResource> measures;
      private final List<IBaseResource> otherResources;

      public MatPackage() {
         this.libraryPackages = new ArrayList<>();
         this.measures = new ArrayList<>();
         this.otherResources = new ArrayList<>();
      }

      public List<LibraryPackage> getLibraryPackages() {
         return libraryPackages;
      }

      public void addLibraryPackage(LibraryPackage libraryPackage) {
         this.libraryPackages.add(libraryPackage);
      }

      public List<IBaseResource> getMeasures() {
         return measures;
      }

      public void addMeasure(IBaseResource measure) {
         this.measures.add(measure);
      }

      public List<IBaseResource> getOtherResources() {
         return otherResources;
      }

      public void addResource(IBaseResource resource) {
         this.otherResources.add(resource);
      }

      public static class LibraryPackage {
         private IBaseResource library;
         private String cql;

         public IBaseResource getLibrary() {
            return library;
         }

         public LibraryPackage setLibrary(IBaseResource library) {
            this.library = library;
            return this;
         }

         public String getCql() {
            return cql;
         }

         public LibraryPackage setCql(String cql) {
            this.cql = cql;
            return this;
         }
      }
   }
}
