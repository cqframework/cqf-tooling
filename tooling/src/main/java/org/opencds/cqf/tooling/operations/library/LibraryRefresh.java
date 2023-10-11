package org.opencds.cqf.tooling.operations.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.Library;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

@Operation(name = "RefreshLibrary")
public class LibraryRefresh implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(LibraryRefresh.class);

   @OperationParam(alias = { "ptl", "pathtolibrary" }, setter = "setPathToLibrary", required = true,
           description = "Path to the FHIR Library resource to refresh (required).")
   private String pathToLibrary;
   @OperationParam(alias = { "ptcql", "pathtocql" }, setter = "setPathToCql", required = true,
           description = "Path to the CQL content referenced or depended on by the FHIR Library resource to refresh (required).")
   private String pathToCql;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR Library { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           description = "The directory path to which the generated FHIR resources should be written (default is to replace existing resources within the IG)")
   private String outputPath;

   private FhirContext fhirContext;
   private ModelManager modelManager;
   private LibraryManager libraryManager;
   private CqlTranslatorOptions translatorOptions = CqlTranslatorOptions.defaultOptions();

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);

      IBaseResource libraryToRefresh = IOUtils.readResource(pathToLibrary, fhirContext);
      if (!libraryToRefresh.fhirType().equalsIgnoreCase("library")) {
         throw new InvalidOperationArgs("Expected resource of type Library, found " + libraryToRefresh.fhirType());
      }

      try {
         modelManager = new ModelManager();
         translatorOptions = ResourceUtils.getTranslatorOptions(pathToCql);
         libraryManager = new LibraryManager(modelManager, translatorOptions.getCqlCompilerOptions());
         libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(Paths.get(pathToCql)));
         libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
         refreshLibrary(libraryToRefresh);

         if (outputPath == null) {
            outputPath = pathToLibrary;
         }

         IOUtils.writeResource(libraryToRefresh, outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
      } catch (Exception e) {
         logger.error("Error refreshing library: {}", pathToLibrary, e);
      }
   }

   // Library access method
   public IBaseResource refreshLibrary(IBaseResource libraryToRefresh) {
      Library r5LibraryToRefresh = (Library) ResourceAndTypeConverter.convertToR5Resource(fhirContext, libraryToRefresh);

      try {
         String cql = new String(libraryManager.getLibrarySourceLoader().getLibrarySource(ResourceUtils.getIdentifier(libraryToRefresh, fhirContext)).readAllBytes());
         CqlTranslator translator = CqlTranslator.fromText(cql, libraryManager);
         DataRequirementsProcessor drp = new DataRequirementsProcessor();
         Library drLibrary = drp.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(),
                 translatorOptions.getCqlCompilerOptions(), null, true, false);

         r5LibraryToRefresh.setDate(new Date());
         refreshContent(r5LibraryToRefresh, cql, translator.toXml(), translator.toJson());
         r5LibraryToRefresh.setDataRequirement(drLibrary.getDataRequirement());
         r5LibraryToRefresh.setRelatedArtifact(drLibrary.getRelatedArtifact());
         r5LibraryToRefresh.setParameter(drLibrary.getParameter());
      } catch (Exception e) {
         logger.error("Error refreshing library: {}", pathToLibrary, e);
         return null;
      }

      return ResourceAndTypeConverter.convertFromR5Resource(fhirContext, r5LibraryToRefresh);
   }

   private String getCqlFromLibrary(Library library) {
      for (var content : library.getContent()) {
         if (content.hasContentType() && content.getContentType().equalsIgnoreCase("text/cql")) {
            return new String(content.getData());
         }
      }
      return null;
   }

   private void refreshContent(Library library, String cql, String elmXml, String elmJson) {
      library.setContent(Arrays.asList(
              new Attachment().setContentType("text/cql").setData(cql.getBytes()),
              new Attachment().setContentType("application/elm+xml").setData(elmXml.getBytes()),
              new Attachment().setContentType("application/elm+json").setData(elmJson.getBytes())));
   }

   public String getPathToLibrary() {
      return pathToLibrary;
   }

   public void setPathToLibrary(String pathToLibrary) {
      this.pathToLibrary = pathToLibrary;
   }

   public String getPathToCql() {
      return pathToCql;
   }

   public void setPathToCql(String pathToCql) {
      this.pathToCql = pathToCql;
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

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputPath) {
      this.outputPath = outputPath;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }

   public ModelManager getModelManager() {
      return modelManager;
   }

   public void setModelManager(ModelManager modelManager) {
      this.modelManager = modelManager;
   }

   public LibraryManager getLibraryManager() {
      return libraryManager;
   }

   public void setLibraryManager(LibraryManager libraryManager) {
      this.libraryManager = libraryManager;
   }

   public CqlTranslatorOptions getTranslatorOptions() {
      return translatorOptions;
   }

   public void setTranslatorOptions(CqlTranslatorOptions translatorOptions) {
      this.translatorOptions = translatorOptions;
   }
}
