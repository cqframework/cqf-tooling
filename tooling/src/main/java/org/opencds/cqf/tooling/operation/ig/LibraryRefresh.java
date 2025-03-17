package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.util.TerserUtil;
import ca.uhn.fhir.util.UrlUtil;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.fhir.npm.LibraryLoader;
import org.cqframework.fhir.npm.NpmPackageManager;
import org.cqframework.fhir.utilities.exception.IGInitializationException;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryRefresh extends Refresh {
   private static final Logger logger = LoggerFactory.getLogger(LibraryRefresh.class);
   private final CqlProcessor cqlProcessor;
   private final NpmPackageManager npmPackageManager;
   private final List<LibraryPackage> libraryPackages;

   public LibraryRefresh(IGInfo igInfo) {
      super(igInfo);
       this.npmPackageManager = new NpmPackageManager(igInfo.getIgResource());
       this.libraryPackages = new ArrayList<>();
      LibraryLoader libraryLoader = new LibraryLoader(igInfo.getFhirContext().getVersion().getVersion().getFhirVersionString());
      UcumEssenceService ucumService;
      try {
         ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
      } catch (UcumException e) {
         throw new IGInitializationException("Could not create UCUM validation service", e);
      }
      List<NpmPackage> packageList = cleanPackageList(this.npmPackageManager.getNpmList());
      this.cqlProcessor = new CqlProcessor(packageList,
              Collections.singletonList(igInfo.getCqlBinaryPath()), libraryLoader, new IGLoggingService(logger), ucumService,
              igInfo.getPackageId(), igInfo.getCanonical(), true);
   }
   @Override
   public List<IBaseResource> refresh() {
      return refresh(null);
   }

   public List<IBaseResource> refresh(RefreshIGParameters params) {
      List<IBaseResource> refreshedLibraries = new ArrayList<>();
      this.cqlProcessor.execute();
      if (getIgInfo().isRefreshLibraries()) {
         logger.info("Refreshing Libraries...");

         for (var library : getResourcesOfTypeFromDirectory("Library", getIgInfo().getLibraryResourcePath())) {
            String name = ResourceUtils.getName(library, getFhirContext());

            logger.info("Refreshing {}", library.getIdElement());

            for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
               if (info.getIdentifier() == null) {
                  logger.warn("No identifier found for CQL file {}", info.getPath());
               }

               if (info.getIdentifier().getId().endsWith(name)) {
                  // TODO: should likely verify or resolve/refresh the following elements:
                  //  cpg-knowledgeCapability, cpg-knowledgeRepresentationLevel, url, identifier, status,
                  //  experimental, type, publisher, contact, description, useContext, jurisdiction,
                  //  and profile(s) (http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-shareablelibrary)
                  refreshDate(library);
                  refreshContent(library, info);
                  refreshDataRequirements(library, info);
                  refreshRelatedArtifacts(library, info);
                  refreshParameters(library, info);
                  refreshVersion(library, params);
                  refreshedLibraries.add(library);
                  this.libraryPackages.add(new LibraryPackage(library, getFhirContext(), info));
               }
            }

            logger.info("Success!");
         }
         resolveLibraryPackages();
      }
      return refreshedLibraries;
   }

   private void resolveLibraryPackages() {
      // See the comment below regarding terminology resolution below
      List<IBaseResource> sourceIGValueSets =
              getResourcesOfTypeFromDirectory("ValueSet", getIgInfo().getValueSetResourcePath());
      List<IBaseResource> sourceIGCodeSystems =
              getResourcesOfTypeFromDirectory("CodeSystem", getIgInfo().getCodeSystemResourcePath());
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
                                  if (identifier.getSystem().equals(getIgInfo().getCanonical())) {
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
//                               else if (relatedArtifact.getResource().contains("/ValueSet/")) {
//                                  identifier = CanonicalUtils.toVersionedIdentifierAnyResource(relatedArtifact.getResource());
//                                  if (!identifier.getSystem().equals(getIgInfo().getCanonical())) {
//                                     libraryPackage.addDependsOnValueSet(getValueSetFromNpmPackage(identifier));
//                                  }
//                               }
//                               else if (relatedArtifact.getResource().contains("/CodeSystem/")) {
//                                  identifier = CanonicalUtils.toVersionedIdentifierAnyResource(relatedArtifact.getResource());
//                                  if (!identifier.getSystem().equals(getIgInfo().getCanonical())) {
//                                     libraryPackage.addDependsOnCodeSystem(getCodeSystemFromNpmPackage(identifier));
//                                  }
//                               }
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

   private LibraryPackage getLibraryPackage(String url) {
      return this.libraryPackages.stream().filter(
              pkg -> url.endsWith(pkg.getLibrary().getIdElement().getIdPart())).findFirst().orElse(null);
   }

   Map<String, List<IBaseResource>> npmPackageLibraryCache = new HashMap<>();
   private IBaseResource getLibraryFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "Library", npmPackageLibraryCache);
   }

   Map<String, List<IBaseResource>> npmPackageValueSetCache = new HashMap<>();
   private IBaseResource getValueSetFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "ValueSet", npmPackageValueSetCache);
   }

   Map<String, List<IBaseResource>> npmPackageCodeSystemCache = new HashMap<>();
   private IBaseResource getCodeSystemFromNpmPackage(VersionedIdentifier identifier) {
      return getResourceFromNpmPackage(identifier, "CodeSystem", npmPackageCodeSystemCache);
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
                                                  FilenameUtils.concat(path, fileName), getFhirContext(), "text"))
                                  .collect(Collectors.toList()));
               }
            } catch (IOException ioe) {
               logger.warn("Unable to resolve resources of type {}", resourceType);
            }
         }
      }
      if (resourceCache.containsKey(url)) {
         return resourceCache.get(url).stream().filter(
                 resource -> {
                    VersionedIdentifier cachedIdentifier = ResourceUtils.getIdentifier(resource, getFhirContext());
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
                      && pkg.getNpm().getJsonString("canonical").getValue().equals(url))
              .findFirst();
      if (!npmPackage.isPresent()) {
         logger.warn("Could not resolve canonical url {} from local packages", url);
      }
      return npmPackage.orElse(null);
   }

   // TODO: move this deduplication logic to the translator
   private List<NpmPackage> cleanPackageList(List<NpmPackage> originalPackageList) {
      Set<String> pathSet = new HashSet<>();
      return originalPackageList.stream().filter(e -> pathSet.add(e.getPath()))
              .collect(Collectors.toList());
   }

   private List<NpmPackage> clearCollisions(List<NpmPackage> collisionPossibleList, IGInfo igInfo) {
      List<NpmPackage> collisionFreeList = new ArrayList<>();
      for (NpmPackage pkg : collisionPossibleList) {
         for (NpmPackage innerPkg : collisionPossibleList) {
            if (pkg.id().equals(innerPkg.id()) && pkg.canonical().equals(innerPkg.canonical())
                    && !pkg.version().equals(innerPkg.version())) {
               // if igInfo has the version we want, use it, otherwise use latest
               var explicit = igInfo.getIgResource().getDependsOn().stream().filter(x -> x.getPackageId().equals(pkg.id())).findFirst();
               if (explicit.isPresent()) {

               }
            }
         }
      }
      return collisionFreeList;
   }

   private void refreshContent(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      Attachment cql = new Attachment().setContentType("text/cql").setData(info.getCql());
      Attachment elmXml = new Attachment().setContentType("application/elm+xml").setData(info.getElm());
      Attachment elmJson = new Attachment().setContentType("application/elm+json").setData(info.getJsonElm());
      TerserUtil.clearField(getFhirContext(), library, "content");
      TerserUtil.setField(getFhirContext(), "content", library,
              ResourceAndTypeConverter.convertType(getFhirContext(), cql),
              ResourceAndTypeConverter.convertType(getFhirContext(), elmXml),
              ResourceAndTypeConverter.convertType(getFhirContext(), elmJson));
   }

   private void refreshDataRequirements(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] dataRequirements = info.getDataRequirements().stream()
              .map(dataRequirement -> ResourceAndTypeConverter.convertType(getFhirContext(), dataRequirement))
              .toArray(IBase[]::new);
      TerserUtil.clearField(getFhirContext(), library, "dataRequirement");
      TerserUtil.setField(getFhirContext(), "dataRequirement", library, dataRequirements);
   }

   private void refreshRelatedArtifacts(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] relatedArtifacts = info.getRelatedArtifacts().stream()
              .map(relatedArtifact -> ResourceAndTypeConverter.convertType(getFhirContext(), relatedArtifact))
              .toArray(IBase[]::new);
      TerserUtil.clearField(getFhirContext(), library, "relatedArtifact");
      TerserUtil.setField(getFhirContext(), "relatedArtifact", library, relatedArtifacts);
   }

   private void refreshParameters(IBaseResource library, CqlProcessor.CqlSourceFileInformation info) {
      IBase[] parameters = info.getParameters().stream()
              .map(parameter -> ResourceAndTypeConverter.convertType(getFhirContext(), parameter))
              .toArray(IBase[]::new);
      TerserUtil.clearField(getFhirContext(), library, "parameter");
      TerserUtil.setField(getFhirContext(), "parameter", library, parameters);
   }

   public CqlProcessor getCqlProcessor() {
      return this.cqlProcessor;
   }

   public List<LibraryPackage> getLibraryPackages() {
      return libraryPackages;
   }
}
