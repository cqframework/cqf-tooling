package org.opencds.cqf.tooling.operations.library;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.TerserUtil;
import ca.uhn.fhir.util.UrlUtil;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.cqframework.fhir.npm.NpmPackageManager;
import org.cqframework.fhir.utilities.exception.IGInitializationException;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.igtools.IGLoggingService;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

   private IGUtils.IGInfo igInfo;
   private final FhirContext fhirContext;
   private ModelManager modelManager;
   private LibraryManager libraryManager;
   private CqlTranslatorOptions translatorOptions = CqlTranslatorOptions.defaultOptions();
   private NpmPackageManager npmPackageManager;
   private CqlProcessor cqlProcessor;
   private final List<LibraryPackage> libraryPackages;

   public LibraryRefresh(FhirContext fhirContext, String pathToCql) {
      this.fhirContext = fhirContext;
      this.pathToCql = pathToCql;
      this.libraryPackages = new ArrayList<>();
      LibraryLoader libraryLoader = new LibraryLoader(this.fhirContext.getVersion().getVersion().getFhirVersionString());
      UcumEssenceService ucumService;
      try {
         ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
      } catch (UcumException e) {
         throw new IGInitializationException("Could not create UCUM validation service", e);
      }
      this.cqlProcessor = new CqlProcessor(null,
              Collections.singletonList(this.pathToCql), libraryLoader, new IGLoggingService(logger),
              ucumService, null, null);
   }

   public LibraryRefresh(IGUtils.IGInfo igInfo) {
      this.igInfo = igInfo;
      this.fhirContext = igInfo.getFhirContext();
      this.npmPackageManager = new NpmPackageManager(igInfo.getIgResource(), null);
      this.libraryPackages = new ArrayList<>();
      LibraryLoader libraryLoader = new LibraryLoader(igInfo.getFhirContext().getVersion().getVersion().getFhirVersionString());
      UcumEssenceService ucumService;
      try {
         ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
      } catch (UcumException e) {
         throw new IGInitializationException("Could not create UCUM validation service", e);
      }
      this.cqlProcessor = new CqlProcessor(cleanPackageList(this.npmPackageManager.getNpmList()),
              Collections.singletonList(igInfo.getCqlBinaryPath()), libraryLoader, new IGLoggingService(logger),
              ucumService, igInfo.getPackageId(), igInfo.getCanonical());
   }

   @Override
   public void execute() {
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

   // Directory Case
   public List<IBaseResource> refreshLibraries(IGUtils.IGInfo igInfo) {
      List<IBaseResource> refreshedLibraries = new ArrayList<>();
      cqlProcessor.execute();
      if (igInfo.isRefreshLibraries()) {
         logger.info("Refreshing Libraries...");
         for (var library : RefreshUtils.getResourcesOfTypeFromDirectory(fhirContext,
                 "Library", igInfo.getLibraryResourcePath())) {
            refreshedLibraries.add(refreshLibrary(library));
         }
         resolveLibraryPackages();
      }
      return refreshedLibraries;
   }

   // Library access method
   public IBaseResource refreshLibrary(IBaseResource libraryToRefresh) {
      cqlProcessor.execute();
      String name = ResourceUtils.getName(libraryToRefresh, fhirContext);

      logger.info("Refreshing {}", libraryToRefresh.getIdElement());

      for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
         if (info.getIdentifier().getId().endsWith(name)) {
            // TODO: should likely verify or resolve/refresh the following elements:
            //  cpg-knowledgeCapability, cpg-knowledgeRepresentationLevel, url, identifier, status,
            //  experimental, type, publisher, contact, description, useContext, jurisdiction,
            //  and profile(s) (http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-shareablelibrary)
            RefreshUtils.refreshDate(fhirContext, libraryToRefresh);
            refreshContent(libraryToRefresh, info);
            refreshDataRequirements(libraryToRefresh, info);
            refreshRelatedArtifacts(libraryToRefresh, info);
            refreshParameters(libraryToRefresh, info);
            this.libraryPackages.add(new LibraryPackage(libraryToRefresh, fhirContext, info));
         }
      }

      logger.info("Success!");

      return libraryToRefresh;
   }

   private void resolveLibraryPackages() {
      // See the comment below regarding terminology resolution below
      List<IBaseResource> sourceIGValueSets =
              RefreshUtils.getResourcesOfTypeFromDirectory(fhirContext, "ValueSet", igInfo.getValueSetResourcePath());
      List<IBaseResource> sourceIGCodeSystems =
              RefreshUtils.getResourcesOfTypeFromDirectory(fhirContext, "CodeSystem", igInfo.getCodeSystemResourcePath());
      this.libraryPackages.forEach(
              libraryPackage -> {
                 libraryPackage.setDependsOnValueSets(sourceIGValueSets);
                 libraryPackage.setDependsOnCodeSystems(sourceIGCodeSystems);
                 libraryPackage.getCqlFileInfo().getRelatedArtifacts().forEach(
                         relatedArtifact -> {
                            if (relatedArtifact.hasResource() && UrlUtil.isValid(relatedArtifact.getResource())) {
                               VersionedIdentifier identifier;
                               if (relatedArtifact.getResource().contains("/Library/")) {
                                  identifier = CanonicalUtils.toVersionedIdentifier(relatedArtifact.getResource());
                                  if (identifier.getSystem().equals(igInfo.getCanonical())) {
                                     // retrieve from existing packages (source IG)
                                     libraryPackage.addDependsOnLibrary(getLibraryPackage(identifier).getLibrary());
                                  }
                                  else {
                                     // retrieve from local NPM packages
                                     libraryPackage.addDependsOnLibrary(getLibraryFromNpmPackage(identifier));
                                  }
                               }
                               // TODO: resolve terminology from source IG - currently just including all terminology
                               //  due to some limitations in data requirements processing
                               else if (relatedArtifact.getResource().contains("/ValueSet/")) {
                                  identifier = CanonicalUtils.toVersionedIdentifierAnyResource(relatedArtifact.getResource());
                                  if (!identifier.getSystem().equals(igInfo.getCanonical())) {
                                     libraryPackage.addDependsOnValueSet(getValueSetFromNpmPackage(identifier));
                                  }
                               }
                               else if (relatedArtifact.getResource().contains("/CodeSystem/")) {
                                  identifier = CanonicalUtils.toVersionedIdentifierAnyResource(relatedArtifact.getResource());
                                  if (!identifier.getSystem().equals(igInfo.getCanonical())) {
                                     libraryPackage.addDependsOnCodeSystem(getCodeSystemFromNpmPackage(identifier));
                                  }
                               }
                            }
                         }
                 );
              }
      );
   }

   private LibraryPackage getLibraryPackage(VersionedIdentifier identifier) {
      return this.libraryPackages.stream().filter(
              pkg -> pkg.getCqlFileInfo().getIdentifier().equals(identifier)).findFirst().orElse(null);
   }

   Map<String, List<IBaseResource>> npmPackageLibraryCache = new HashMap<>();
   private IBaseResource getLibraryFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "Library", npmPackageLibraryCache);
   }

   private IBaseResource getResourceFromNpmPackage(VersionedIdentifier identifier, String resourceType,
                                                   Map<String, List<IBaseResource>> resourceCache) {
      String url;
      if ((resourceType.equals("ValueSet") || resourceType.equals("CodeSystem"))
              && identifier.getSystem().equals("http://terminology.hl7.org")) {
         url = "http://hl7.org/fhir";
      }
      else {
         url = identifier.getSystem();
      }
      if (!resourceCache.containsKey(url)) {
         NpmPackage npmPackage = getNpmPackage(url);
         if (npmPackage != null && npmPackage.getFolders().containsKey("package") ) {
            String path = FilenameUtils.concat(npmPackage.getPath(), "package");
            try {
               if (npmPackage.getFolders().get("package").getTypes().containsKey(resourceType)) {
                  resourceCache.put(url,
                          npmPackage.getFolders().get("package").getTypes().get(resourceType).stream().map(
                                  fileName -> IOUtils.readJsonResourceIgnoreElements(
                                          FilenameUtils.concat(path, fileName), fhirContext, "text"))
                                  .collect(Collectors.toList()));
               }
            } catch (IOException e) {
               logger.error("Encountered Error when retrieving NPM Package Types: {}", e.getMessage());
            }

         }
      }
      if (resourceCache.containsKey(url)) {
         return resourceCache.get(url).stream().filter(
                 resource -> {
                    VersionedIdentifier cachedIdentifier = ResourceUtils.getIdentifier(resource, fhirContext);
                    if (identifier.getVersion() == null) {
                       // non-versioned urls - typically for terminology resources
                       cachedIdentifier.setVersion(null);
                    }
                    return cachedIdentifier.equals(identifier);
                 }).findFirst().orElse(null);
      }
      logger.warn("Could not resolve {} from local packages", identifier);
      return null;
   }

   private NpmPackage getNpmPackage(String url) {
      Optional<NpmPackage> npmPackage = this.npmPackageManager.getNpmList().stream()
              .filter(pkg -> pkg.getNpm().has("canonical")
                      && pkg.getNpm().getJsonObject("canonical").asString().equals(url))
              .findFirst();
      if (npmPackage.isPresent()) {
         logger.warn("Could not resolve canonical url {} from local packages", url);
      }
      return npmPackage.orElse(null);
   }

   private List<NpmPackage> cleanPackageList(List<NpmPackage> originalPackageList) {
      Set<String> pathSet = new HashSet<>();
      return originalPackageList.stream().filter(e -> pathSet.add(e.getPath()))
              .collect(Collectors.toList());
   }

   Map<String, List<IBaseResource>> npmPackageValueSetCache = new HashMap<>();
   private IBaseResource getValueSetFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "ValueSet", npmPackageValueSetCache);
   }

   Map<String, List<IBaseResource>> npmPackageCodeSystemCache = new HashMap<>();
   private IBaseResource getCodeSystemFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "CodeSystem", npmPackageCodeSystemCache);
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

   private void refreshContent(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      Attachment cql = new Attachment().setContentType("text/cql").setData(info.getCql());
      Attachment elmXml = new Attachment().setContentType("application/elm+xml").setData(info.getElm());
      Attachment elmJson = new Attachment().setContentType("application/elm+json").setData(info.getJsonElm());
      TerserUtil.clearField(fhirContext, library, "content");
      TerserUtil.setField(fhirContext, "content", library,
              ResourceAndTypeConverter.convertType(fhirContext, cql),
              ResourceAndTypeConverter.convertType(fhirContext, elmXml),
              ResourceAndTypeConverter.convertType(fhirContext, elmJson));
   }

   private void refreshDataRequirements(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] dataRequirements = info.getDataRequirements().stream()
              .map(dataRequirement -> ResourceAndTypeConverter.convertType(fhirContext, dataRequirement))
              .toArray(IBase[]::new);
      TerserUtil.clearField(fhirContext, library, "dataRequirement");
      TerserUtil.setField(fhirContext, "dataRequirement", library, dataRequirements);
   }

   private void refreshRelatedArtifacts(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] relatedArtifacts = info.getRelatedArtifacts().stream()
              .map(relatedArtifact -> ResourceAndTypeConverter.convertType(fhirContext, relatedArtifact))
              .toArray(IBase[]::new);
      TerserUtil.clearField(fhirContext, library, "relatedArtifact");
      TerserUtil.setField(fhirContext, "relatedArtifact", library, relatedArtifacts);
   }

   private void refreshParameters(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] parameters = info.getParameters().stream()
              .map(parameter -> ResourceAndTypeConverter.convertType(fhirContext, parameter))
              .toArray(IBase[]::new);
      TerserUtil.clearField(fhirContext, library, "parameter");
      TerserUtil.setField(fhirContext, "parameter", library, parameters);
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

   public CqlProcessor getCqlProcessor() {
      return this.cqlProcessor;
   }

   public List<LibraryPackage> getLibraryPackages() {
      return this.libraryPackages;
   }
}
