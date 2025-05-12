package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeChildChoiceDefinition;
import ca.uhn.fhir.context.RuntimeCompositeDatatypeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.util.TerserUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptionsMapper;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseElement;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.opencds.cqf.tooling.cql.exception.CqlTranslatorException;
import org.opencds.cqf.tooling.processor.ValueSetsProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.opencds.cqf.tooling.utilities.CanonicalUtils.getTail;

public class ResourceUtils {
   private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);
   private static final String CQF_LIBRARY_EXT_URL = "http://hl7.org/fhir/StructureDefinition/cqf-library";

   public enum FhirVersion {
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

   public static String getId(String name, String version, boolean versioned) {
      return name.replace("_", "-") + (versioned ? "-" + version.replace("_", ".") : "");
   }

   public static void setIgId(String baseId, IBaseResource resource, Boolean includeVersion)
   {
      String version = Boolean.TRUE.equals(includeVersion) ? resource.getMeta().getVersionId() : "";
      setIgId(baseId, resource,  version);
   }

   public static void setIgId(String baseId, IBaseResource resource, String version)
   {
      String igId = "";
      String resourceName = resource.getClass().getSimpleName().toLowerCase();
      String versionId = (version == null || version.equals("")) ? "" : "-" + version;

      if (resource instanceof org.hl7.fhir.dstu3.model.Bundle || resource instanceof org.hl7.fhir.r4.model.Bundle) {
         igId = baseId + versionId + "-" + resourceName;
      }
      else {
         igId = resourceName + "-" + baseId + versionId;

      }
      igId = igId.replace("_", "-");
      resource.setId(igId);
   }

   public static FhirContext getFhirContext(FhirVersion fhirVersion) {
      switch (fhirVersion) {
         case DSTU3:
            return FhirContext.forDstu3Cached();
         case R4:
            return FhirContext.forR4Cached();
         default:
            throw new IllegalArgumentException("Unsupported FHIR version: " + fhirVersion);
      }
   }

   private static List<RelatedArtifact> getStu3RelatedArtifacts(String pathToLibrary, FhirContext fhirContext) {
      Object mainLibrary = IOUtils.readResource(pathToLibrary, fhirContext);
      if (!(mainLibrary instanceof org.hl7.fhir.dstu3.model.Library)) {
         throw new IllegalArgumentException("pathToLibrary must be a path to a Library type Resource");
      }
      return ((org.hl7.fhir.dstu3.model.Library)mainLibrary).getRelatedArtifact();
   }

   private static List<org.hl7.fhir.r4.model.RelatedArtifact> getR4RelatedArtifacts(String pathToLibrary, FhirContext fhirContext) {
      Object mainLibrary = IOUtils.readResource(pathToLibrary, fhirContext);
      if (!(mainLibrary instanceof org.hl7.fhir.r4.model.Library)) {
         throw new IllegalArgumentException("pathToLibrary must be a path to a Library type Resource");
      }
      return ((org.hl7.fhir.r4.model.Library)mainLibrary).getRelatedArtifact();
   }

   public static Map<String, IBaseResource> getDepLibraryResources(String path, FhirContext fhirContext, Encoding encoding, Boolean versioned, Logger logger) {
      Map<String, IBaseResource> dependencyLibraries = new HashMap<>();
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3:
            return getStu3DepLibraryResources(path, dependencyLibraries, fhirContext, encoding, versioned);
         case R4:
            return getR4DepLibraryResources(path, dependencyLibraries, fhirContext, encoding, versioned, logger);
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static List<String> getDepLibraryPaths(String path, FhirContext fhirContext, Encoding encoding, Boolean versioned) {
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3:
            return getStu3DepLibraryPaths(path, fhirContext, encoding, versioned);
         case R4:
            return getR4DepLibraryPaths(path, fhirContext, encoding,versioned);
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   private static List<String> getStu3DepLibraryPaths(String path, FhirContext fhirContext, Encoding encoding, Boolean versioned) {
      List<String> paths = new ArrayList<>();
      String directoryPath = FilenameUtils.getFullPath(path);
      String fileName = FilenameUtils.getName(path);
      String prefix = fileName.toLowerCase().startsWith("library-") ? fileName.substring(0, 8) : "";
      List<org.hl7.fhir.dstu3.model.RelatedArtifact> relatedArtifacts = getStu3RelatedArtifacts(path, fhirContext);
      for (org.hl7.fhir.dstu3.model.RelatedArtifact relatedArtifact : relatedArtifacts) {
         if (relatedArtifact.getType() == org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType.DEPENDSON) {
            if (relatedArtifact.getResource().getReference().contains("Library/")) {
               String dependencyLibraryName;
               // Issue 96 - Do not include version number in the filename
               if (Boolean.TRUE.equals(versioned)) {
                  dependencyLibraryName = IOUtils.formatFileName(relatedArtifact.getResource().getReference().split("Library/")[1].replace("\\|", "-"), encoding, fhirContext);
               } else {
                  String name = relatedArtifact.getResource().getReference().split("Library/")[1];
                  dependencyLibraryName = IOUtils.formatFileName(name.split("\\|")[0], encoding, fhirContext);
               }
               String dependencyLibraryPath = IOUtils.concatFilePath(directoryPath, prefix + dependencyLibraryName);
               IOUtils.putAllInListIfAbsent(getStu3DepLibraryPaths(dependencyLibraryPath, fhirContext, encoding, versioned), paths);
               IOUtils.putInListIfAbsent(dependencyLibraryPath, paths);
            }
         }
      }
      return paths;
   }

   private static Map<String, IBaseResource> getStu3DepLibraryResources(String path, Map<String, IBaseResource> dependencyLibraries, FhirContext fhirContext, Encoding encoding, Boolean versioned) {
      List<String> dependencyLibraryPaths = getStu3DepLibraryPaths(path, fhirContext, encoding, versioned);
      for (String dependencyLibraryPath : dependencyLibraryPaths) {
         Object resource = IOUtils.readResource(dependencyLibraryPath, fhirContext);
         if (resource instanceof org.hl7.fhir.dstu3.model.Library) {
            org.hl7.fhir.dstu3.model.Library library = (org.hl7.fhir.dstu3.model.Library)resource;
            dependencyLibraries.putIfAbsent(library.getId(), library);
         }
      }
      return dependencyLibraries;
   }

   // if | exists there is a version
   private static List<String> getR4DepLibraryPaths(String path, FhirContext fhirContext, Encoding encoding, Boolean versioned) {
      List<String> paths = new ArrayList<>();
      String directoryPath = FilenameUtils.getFullPath(path);
      String fileName = FilenameUtils.getName(path);
      String prefix = fileName.toLowerCase().startsWith("library-") ? fileName.substring(0, 8) : "";
      List<org.hl7.fhir.r4.model.RelatedArtifact> relatedArtifacts = getR4RelatedArtifacts(path, fhirContext);
      for (org.hl7.fhir.r4.model.RelatedArtifact relatedArtifact : relatedArtifacts) {
         if (relatedArtifact.getType() == org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON) {
            if (relatedArtifact.getResource().contains("Library/")) {
               String dependencyLibraryName;
               // Issue 96 - Do not include version number in the filename
               if (versioned) {
                  dependencyLibraryName = IOUtils.formatFileName(relatedArtifact.getResource().split("Library/")[1].replaceAll("\\|", "-"), encoding, fhirContext);
               } else {
                  String name = relatedArtifact.getResource().split("Library/")[1];
                  dependencyLibraryName = IOUtils.formatFileName(name.split("\\|")[0], encoding, fhirContext);
               }
               String dependencyLibraryPath = IOUtils.concatFilePath(directoryPath, prefix + dependencyLibraryName);
               IOUtils.putInListIfAbsent(dependencyLibraryPath, paths);
            }
         }
      }
      return paths;
   }

   private static Map<String, IBaseResource> getR4DepLibraryResources(String path, Map<String, IBaseResource> dependencyLibraries, FhirContext fhirContext, Encoding encoding, Boolean versioned, Logger logger) {
      List<String> dependencyLibraryPaths = getR4DepLibraryPaths(path, fhirContext, encoding, versioned);
      for (String dependencyLibraryPath : dependencyLibraryPaths) {
         if (dependencyLibraryPath.contains("ModelInfo")) {
            logger.debug("skipping ModelInfo");
         } else {
            Object resource = IOUtils.readResource(dependencyLibraryPath, fhirContext);
            if (resource instanceof org.hl7.fhir.r4.model.Library) {
               org.hl7.fhir.r4.model.Library library = (org.hl7.fhir.r4.model.Library)resource;
               dependencyLibraries.putIfAbsent(library.getId(), library);
            }
         }
      }
      return dependencyLibraries;
   }

   public static List<String> getStu3Dependencies(List<org.hl7.fhir.dstu3.model.RelatedArtifact> relatedArtifacts) {
      List<String> urls = new ArrayList<>();
      for (org.hl7.fhir.dstu3.model.RelatedArtifact relatedArtifact : relatedArtifacts) {
         if (relatedArtifact.hasType() && relatedArtifact.getType() == org.hl7.fhir.dstu3.model.RelatedArtifact.RelatedArtifactType.DEPENDSON) {
            if (relatedArtifact.hasResource() && relatedArtifact.getResource().hasReference()) {
               urls.add(relatedArtifact.getResource().getReference());
            }
         }
      }
      return urls;
   }

   public static List<String> getR4Dependencies(List<org.hl7.fhir.r4.model.RelatedArtifact> relatedArtifacts) {
      List<String> urls = new ArrayList<>();
      for (org.hl7.fhir.r4.model.RelatedArtifact relatedArtifact : relatedArtifacts) {
         if (relatedArtifact.hasType() && relatedArtifact.getType() == org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DEPENDSON) {
            if (relatedArtifact.hasResource()) {
               urls.add(relatedArtifact.getResource());
            }
         }
      }
      return urls;
   }

   public static List<String> getDependencies(IBaseResource resource, FhirContext fhirContext) {
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3:
            switch (resource.fhirType()) {
               case "Library": {
                  return getStu3Dependencies(((org.hl7.fhir.dstu3.model.Library)resource).getRelatedArtifact());
               }
               case "Measure": {
                  return getStu3Dependencies(((org.hl7.fhir.dstu3.model.Measure)resource).getRelatedArtifact());
               }
               default: throw new IllegalArgumentException(String.format("Could not retrieve relatedArtifacts from %s", resource.fhirType()));
            }
         case R4:
            switch (resource.fhirType()) {
               case "Library": {
                  return getR4Dependencies(((org.hl7.fhir.r4.model.Library)resource).getRelatedArtifact());
               }
               case "Measure": {
                  return getR4Dependencies(((org.hl7.fhir.r4.model.Measure)resource).getRelatedArtifact());
               }
               default: throw new IllegalArgumentException(String.format("Could not retrieve relatedArtifacts from %s", resource.fhirType()));
            }
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static List<String> getTerminologyDependencies(IBaseResource resource, FhirContext fhirContext) {
      return
         getDependencies(resource, fhirContext).stream()
                 .filter(url -> url.contains("/CodeSystem") || url.contains("/ValueSet"))
                 .collect(Collectors.toList());
   }

   public static List<String> getLibraryDependencies(IBaseResource resource, FhirContext fhirContext) {
      return
        getDependencies(resource, fhirContext).stream()
                .filter(url -> url.contains("/Library"))
                .collect(Collectors.toList());
   }

   public static Map<String, IBaseResource> getDepLibraryResources(IBaseResource resource, FhirContext fhirContext, Boolean includeDependencies, Boolean includeVersion, Set<String> missingDependencies) {
      Map<String, IBaseResource> libraryResources = new HashMap<>();

      List<String> libraryUrls = getLibraryDependencies(resource, fhirContext);

      for (String libraryUrl : libraryUrls) {
         IBaseResource library = IOUtils.getLibraryUrlMap(fhirContext).get(libraryUrl);
         if (library == null) {
            var id = CanonicalUtils.getId(libraryUrl);
            var version = CanonicalUtils.getVersion(libraryUrl);
            library = IOUtils.getLibraries(fhirContext).get(id);
            if (library != null) {
               var libraryVersion = ResourceUtils.getVersion(library, fhirContext);
               if (libraryVersion != null && !libraryVersion.equals(version)) {
                  logger.warn("Mismatch library version for {}, expected {}, found {}", libraryUrl, version, libraryVersion);
                  library = null;
               }
            }
         }
         if (library != null) {
            libraryResources.putIfAbsent(libraryUrl, library);

            if (includeDependencies) {
               Map<String, IBaseResource> libraryDependencies = getDepLibraryResources(library, fhirContext, includeDependencies, includeVersion, missingDependencies);
               for (Entry<String, IBaseResource> entry : libraryDependencies.entrySet()) {
                  libraryResources.putIfAbsent(entry.getKey(), entry.getValue());
               }
            }
         }
         else {
            missingDependencies.add(libraryUrl);
         }
      }

      return libraryResources;
   }

   public static Map<String, IBaseResource> getDepValueSetResources(IBaseResource resource, FhirContext fhirContext, Boolean includeDependencies, Set<String> missingDependencies) {
      Map<String, IBaseResource> valueSetResources = new HashMap<>();

      List<String> valueSetUrls = getTerminologyDependencies(resource, fhirContext);

      for (String valueSetUrl : valueSetUrls) {
         var cachedVs = ValueSetsProcessor.getCachedValueSets(fhirContext);
         if (cachedVs.entrySet().stream().filter(
                 entry -> entry.getKey().equals(valueSetUrl)).findAny().isEmpty()) {
            missingDependencies.add(valueSetUrl);
         } else {
              cachedVs.entrySet().stream().filter(entry -> entry.getKey().equals(valueSetUrl))
                      .forEach(entry -> valueSetResources.put(entry.getKey(), entry.getValue()));
         }
      }
      Set<String> dependencies = new HashSet<>(valueSetUrls);

      if (includeDependencies) {
         List<String> libraryUrls = getLibraryDependencies(resource, fhirContext);
         for (String url : libraryUrls) {
            IBaseResource library = IOUtils.getLibraryUrlMap(fhirContext).get(url);
            if (library == null) {
               var id = CanonicalUtils.getId(url);
               var version = CanonicalUtils.getVersion(url);
               library = IOUtils.getLibraries(fhirContext).get(id);
               if (library != null) {
                  var libraryVersion = ResourceUtils.getVersion(library, fhirContext);
                  if (libraryVersion != null && !libraryVersion.equals(version)) {
                     logger.warn("Mismatch library version for {}, expected {}, found {}", url, version, libraryVersion);
                     library = null;
                  }
               }
            }
            if (library != null) {
               Map<String, IBaseResource> dependencyValueSets = getDepValueSetResources(library, fhirContext, includeDependencies, missingDependencies);
               dependencies.addAll(dependencyValueSets.keySet());
               for (Entry<String, IBaseResource> entry : dependencyValueSets.entrySet()) {
                  valueSetResources.putIfAbsent(entry.getKey(), entry.getValue());
               }
            }
            else {
               missingDependencies.add(url);
            }
         }
      }

      if (dependencies.size() != valueSetResources.size()) {
         dependencies.removeAll(valueSetResources.keySet());
         for (String valueSetUrl : dependencies) {
            missingDependencies.add(valueSetUrl);
         }
      }
      return valueSetResources;
   }

   public static Map<String, IBaseResource> getDepValueSetResources(String cqlContentPath, String igPath, FhirContext fhirContext, boolean includeDependencies, Boolean includeVersion) throws CqlTranslatorException {
      Map<String, IBaseResource> valueSetResources = new HashMap<>();

      List<String> valueSetDefIDs = getDepELMValueSetDefIDs(cqlContentPath);

      for (String valueSetUrl : valueSetDefIDs) {
         ValueSetsProcessor.getCachedValueSets(fhirContext).entrySet().stream()
                 .filter(entry -> entry.getKey().equals(valueSetUrl))
                 .forEach(entry -> valueSetResources.put(entry.getKey(), entry.getValue()));
      }
      Set<String> dependencies = new HashSet<>(valueSetDefIDs);

      if (includeDependencies) {
         List<String> dependencyCqlPaths = IOUtils.getDependencyCqlPaths(cqlContentPath, includeVersion);
         for (String path : dependencyCqlPaths) {
            Map<String, IBaseResource> dependencyValueSets = getDepValueSetResources(path, igPath, fhirContext, includeDependencies, includeVersion);
            dependencies.addAll(dependencyValueSets.keySet());
            for (Entry<String, IBaseResource> entry : dependencyValueSets.entrySet()) {
               valueSetResources.putIfAbsent(entry.getKey(), entry.getValue());
            }
         }
      }

      if (dependencies.size() != valueSetResources.size()) {
         List<String> missingValueSets = new ArrayList<>();
         dependencies.removeAll(valueSetResources.keySet());
         for (String valueSetUrl : dependencies) {
            missingValueSets.add(valueSetUrl + " MISSING");
         }
         throw new CqlTranslatorException(missingValueSets, CqlCompilerException.ErrorSeverity.Warning);
      }
      return valueSetResources;
   }

   public static List<String> getIncludedLibraryNames(String cqlContentPath, Boolean includeVersion) throws CqlTranslatorException {
      List<String> includedLibraryNames = new ArrayList<>();
      List<IncludeDef> includedDefs = getIncludedDefs(cqlContentPath);
      for (IncludeDef def : includedDefs) {
         //TODO: replace true with versioned variable
         IOUtils.putInListIfAbsent(getId(def.getPath(), def.getVersion(), includeVersion), includedLibraryNames);
      }
      return includedLibraryNames;
   }

   public static List<String> getDepELMValueSetDefIDs(String cqlContentPath) throws CqlTranslatorException {
      List<String> includedValueSetDefIDs = new ArrayList<>();
      List<ValueSetDef> valueSetDefs = getValueSetDefs(cqlContentPath);
      for (ValueSetDef def : valueSetDefs) {
         IOUtils.putInListIfAbsent(def.getId(), includedValueSetDefIDs);
      }
      return includedValueSetDefIDs;
   }

   public static List<IncludeDef> getIncludedDefs(String cqlContentPath) throws CqlTranslatorException {
      ArrayList<IncludeDef> includedDefs = new ArrayList<>();
      org.hl7.elm.r1.Library elm = getElmFromCql(cqlContentPath);
      if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
         includedDefs.addAll(elm.getIncludes().getDef());
      }
      return includedDefs;
   }

   public static List<ValueSetDef> getValueSetDefs(String cqlContentPath) throws CqlTranslatorException {
      ArrayList<ValueSetDef> valueSetDefs = new ArrayList<>();
      org.hl7.elm.r1.Library elm;
      elm = getElmFromCql(cqlContentPath);
      if (elm.getValueSets() != null && !elm.getValueSets().getDef().isEmpty()) {
         valueSetDefs.addAll(elm.getValueSets().getDef());
      }
      return valueSetDefs;
   }

   public static CqlTranslatorOptions getTranslatorOptions(String folder) {
      String optionsFileName = IOUtils.concatFilePath(folder,"cql-options.json");
      CqlTranslatorOptions options;
      File file = new File(optionsFileName);
      if (file.exists()) {
         options = CqlTranslatorOptionsMapper.fromFile(file.getAbsolutePath());
         logger.debug("cql-options loaded from: {}", file.getAbsolutePath());
      }
      else {
         options = CqlTranslatorOptions.defaultOptions();
         if (!options.getFormats().contains(CqlTranslatorOptions.Format.XML)) {
            options.getFormats().add(CqlTranslatorOptions.Format.XML);
         }
         logger.debug("cql-options not found. Using default options.");
      }

      return options;
   }

   public static CqlTranslator getCQLCqlTranslator(String cqlContentPath) throws CqlTranslatorException {
      String folder = IOUtils.getParentDirectoryPath(cqlContentPath);
      CqlTranslatorOptions options = ResourceUtils.getTranslatorOptions(folder);
      ModelManager modelManager = new ModelManager();
      LibraryManager libraryManager = new LibraryManager(modelManager);
      libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
      libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(Paths.get(folder)));
      return  IOUtils.translate(new File(cqlContentPath), libraryManager);
   }

   private static Map<String, org.hl7.elm.r1.Library> cachedElm = new HashMap<>();
   public static org.hl7.elm.r1.Library getElmFromCql(String cqlContentPath) throws CqlTranslatorException {
      org.hl7.elm.r1.Library elm = cachedElm.get(cqlContentPath);
      if (elm != null) {
         return elm;
      }
      CqlTranslator translator = getCQLCqlTranslator(cqlContentPath);
      elm = translator.toELM();
      cachedElm.put(cqlContentPath, elm);
      return elm;
   }

   public static Boolean safeAddResource(String path, Map<String, IBaseResource> resources, FhirContext fhirContext) {
      boolean added = true;
      try {
         IBaseResource resource = IOUtils.readResource(path, fhirContext, true);
         if (resource != null) {
//              if(resources.containsKey(resource.getIdElement().getIdPart())){
//                  IBaseResource storedResource = resources.get(resource.getIdElement().getIdPart());
//              }
            resources.putIfAbsent(resource.fhirType() + "/" + resource.getIdElement().getIdPart(), resource);
         } else {
            added = false;
            LogUtils.putException(path, new Exception("Unable to add Resource: " + path));
         }
      }
      catch(Exception e) {
         added = false;
         LogUtils.putException(path, e);
      }
      return added;
   }

   public static String getUrl(IBaseResource resource, FhirContext fhirContext) {
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3: {
            if (resource instanceof org.hl7.fhir.dstu3.model.Measure) {
               return ((org.hl7.fhir.dstu3.model.Measure)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.Library) {
               return ((org.hl7.fhir.dstu3.model.Library)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.PlanDefinition) {
               return ((org.hl7.fhir.dstu3.model.PlanDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
               return ((org.hl7.fhir.dstu3.model.CodeSystem)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
               return ((org.hl7.fhir.dstu3.model.ValueSet)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.ActivityDefinition) {
               return ((org.hl7.fhir.dstu3.model.ActivityDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
               return ((org.hl7.fhir.dstu3.model.StructureDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.GraphDefinition) {
               return ((org.hl7.fhir.dstu3.model.GraphDefinition)resource).getUrl();
            }
            throw new IllegalArgumentException(String.format("Could not retrieve url for resource type %s", resource.fhirType()));
         }
         case R4: {
            if (resource instanceof org.hl7.fhir.r4.model.Measure) {
               return ((org.hl7.fhir.r4.model.Measure)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.Library) {
               return ((org.hl7.fhir.r4.model.Library)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.PlanDefinition) {
               return ((org.hl7.fhir.r4.model.PlanDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.CodeSystem) {
               return ((org.hl7.fhir.r4.model.CodeSystem)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.ValueSet) {
               return ((org.hl7.fhir.r4.model.ValueSet)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.ActivityDefinition) {
               return ((org.hl7.fhir.r4.model.ActivityDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.StructureDefinition) {
               return ((org.hl7.fhir.r4.model.StructureDefinition)resource).getUrl();
            }
            if (resource instanceof org.hl7.fhir.r4.model.GraphDefinition) {
               return ((org.hl7.fhir.r4.model.GraphDefinition)resource).getUrl();
            }
            throw new IllegalArgumentException(String.format("Could not retrieve url for resource type %s", resource.fhirType()));
         }
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static String getVersion(IBaseResource resource, FhirContext fhirContext) {
      IBase version = TerserUtil.getValueFirstRep(fhirContext, resource, "version");
      if (version instanceof IPrimitiveType) {
         return ((IPrimitiveType<?>) version).getValueAsString();
      }
      else return null;
   }

   public static String getName(IBaseResource resource, FhirContext fhirContext) {
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3: {
            if (resource instanceof org.hl7.fhir.dstu3.model.Measure) {
               return ((org.hl7.fhir.dstu3.model.Measure)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.Library) {
               return ((org.hl7.fhir.dstu3.model.Library)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.PlanDefinition) {
               return ((org.hl7.fhir.dstu3.model.PlanDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
               return ((org.hl7.fhir.dstu3.model.CodeSystem)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
               return ((org.hl7.fhir.dstu3.model.ValueSet)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.ActivityDefinition) {
               return ((org.hl7.fhir.dstu3.model.ActivityDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
               return ((org.hl7.fhir.dstu3.model.StructureDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.dstu3.model.GraphDefinition) {
               return ((org.hl7.fhir.dstu3.model.GraphDefinition)resource).getName();
            }
            throw new IllegalArgumentException(String.format("Could not retrieve name for resource type %s", resource.fhirType()));
         }
         case R4: {
            if (resource instanceof org.hl7.fhir.r4.model.Measure) {
               return ((org.hl7.fhir.r4.model.Measure)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.Library) {
               return ((org.hl7.fhir.r4.model.Library)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.PlanDefinition) {
               return ((org.hl7.fhir.r4.model.PlanDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.CodeSystem) {
               return ((org.hl7.fhir.r4.model.CodeSystem)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.ValueSet) {
               return ((org.hl7.fhir.r4.model.ValueSet)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.ActivityDefinition) {
               return ((org.hl7.fhir.r4.model.ActivityDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.StructureDefinition) {
               return ((org.hl7.fhir.r4.model.StructureDefinition)resource).getName();
            }
            if (resource instanceof org.hl7.fhir.r4.model.GraphDefinition) {
               return ((org.hl7.fhir.r4.model.GraphDefinition)resource).getName();
            }
            throw new IllegalArgumentException(String.format("Could not retrieve name for resource type %s", resource.fhirType()));
         }
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static String getPrimaryLibraryUrl(IBaseResource resource, FhirContext fhirContext) {
      if (resource instanceof org.hl7.fhir.r5.model.Measure) {
         org.hl7.fhir.r5.model.Measure measure = (org.hl7.fhir.r5.model.Measure)resource;
         if (!measure.hasLibrary() || measure.getLibrary().size() != 1) {
            throw new IllegalArgumentException("Measure is expected to have one and only one library");
         }
         return measure.getLibrary().get(0).getValue();
      }
      else if (resource instanceof org.hl7.fhir.r4.model.Measure) {
         org.hl7.fhir.r4.model.Measure measure = (org.hl7.fhir.r4.model.Measure)resource;
         if (!measure.hasLibrary() || measure.getLibrary().size() != 1) {
            throw new IllegalArgumentException("Measure is expected to have one and only one library");
         }
         return measure.getLibrary().get(0).getValue();
      }
      else if (resource instanceof org.hl7.fhir.dstu3.model.Measure) {
         org.hl7.fhir.dstu3.model.Measure measure = (org.hl7.fhir.dstu3.model.Measure)resource;
         if (!measure.hasLibrary() || measure.getLibrary().size() != 1) {
            throw new IllegalArgumentException("Measure is expected to have one and only one library");
         }
         String reference = measure.getLibrary().get(0).getReference();
         String[] parts = reference.split("/");
         return parts[parts.length - 1];
      }
      else if (resource instanceof org.hl7.fhir.r5.model.PlanDefinition) {
         org.hl7.fhir.r5.model.PlanDefinition planDefinition = (org.hl7.fhir.r5.model.PlanDefinition)resource;
         if (!planDefinition.hasLibrary() || planDefinition.getLibrary().size() != 1) {
            throw new IllegalArgumentException("PlanDefinition is expected to have one and only one library");
         }
         return planDefinition.getLibrary().get(0).getValue();
      }
      else if (resource instanceof org.hl7.fhir.r4.model.PlanDefinition) {
         org.hl7.fhir.r4.model.PlanDefinition planDefinition = (org.hl7.fhir.r4.model.PlanDefinition)resource;
         if (!planDefinition.hasLibrary() || planDefinition.getLibrary().size() != 1) {
            throw new IllegalArgumentException("PlanDefinition is expected to have one and only one library");
         }
         return planDefinition.getLibrary().get(0).getValue();
      }
      else if (resource instanceof org.hl7.fhir.dstu3.model.PlanDefinition) {
         org.hl7.fhir.dstu3.model.PlanDefinition planDefinition = (org.hl7.fhir.dstu3.model.PlanDefinition)resource;
         if (!planDefinition.hasLibrary() || planDefinition.getLibrary().size() != 1) {
            throw new IllegalArgumentException("PlanDefinition is expected to have one and only one library");
         }
         String reference = planDefinition.getLibrary().get(0).getReference();
         String[] parts = reference.split("/");
         return parts[parts.length - 1];
      }
      else if (resource instanceof org.hl7.fhir.r5.model.Questionnaire) {
         org.hl7.fhir.r5.model.Questionnaire questionnaire = (org.hl7.fhir.r5.model.Questionnaire)resource;

         org.hl7.fhir.r5.model.Extension libraryExtension = questionnaire.getExtensionByUrl(CQF_LIBRARY_EXT_URL);
         if (libraryExtension != null) {
            return ((org.hl7.fhir.r5.model.CanonicalType)libraryExtension.getValue()).getValueAsString();
         }

         return null;
      }
      else if (resource instanceof org.hl7.fhir.r4.model.Questionnaire) {
         org.hl7.fhir.r4.model.Questionnaire questionnaire = (org.hl7.fhir.r4.model.Questionnaire)resource;

         org.hl7.fhir.r4.model.Extension libraryExtension = questionnaire.getExtensionByUrl(CQF_LIBRARY_EXT_URL);
         if (libraryExtension != null) {
            return ((CanonicalType)libraryExtension.getValue()).getValueAsString();
         }

         return null;
      }
      else if (resource instanceof org.hl7.fhir.dstu3.model.Questionnaire) {
         org.hl7.fhir.dstu3.model.Questionnaire questionnaire = (org.hl7.fhir.dstu3.model.Questionnaire)resource;

         List<org.hl7.fhir.dstu3.model.Extension> libraryExtensions =
                 questionnaire.getExtensionsByUrl(CQF_LIBRARY_EXT_URL);

         if (libraryExtensions.isEmpty()) {
            return null;
         } else {
            Validate.isTrue(libraryExtensions.size() == 1, "Url " + CQF_LIBRARY_EXT_URL + " must have only one match");
            return libraryExtensions.get(0).getValue().toString();
         }
      }
      else {
         throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static String getPrimaryLibraryName(IBaseResource resource, FhirContext fhirContext) {
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3: {
            org.hl7.fhir.dstu3.model.Measure measure = (org.hl7.fhir.dstu3.model.Measure)resource;
            if (!measure.hasLibrary() || measure.getLibrary().size() != 1) {
               throw new IllegalArgumentException("Measure is expected to have one and only one library");
            }
            return getTail(measure.getLibrary().get(0).getReference());
         }
         case R4: {
            org.hl7.fhir.r4.model.Measure measure = (org.hl7.fhir.r4.model.Measure)resource;
            if (!measure.hasLibrary() || measure.getLibrary().size() != 1) {
               throw new IllegalArgumentException("Measure is expected to have one and only one library");
            }
            return getTail(measure.getLibrary().get(0).getValue());
         }
         default:
            throw new IllegalArgumentException("Unsupported fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }
   }

   public static VersionedIdentifier getIdentifier(IBaseResource resource, FhirContext fhirContext) {
      String url = getUrl(resource, fhirContext);
      return CanonicalUtils.toVersionedIdentifierAnyResource(url).withVersion(getVersion(resource, fhirContext));
   }

   public static boolean compareResourcePrimitiveElements(IBaseResource res1, IBaseResource res2,
                                                          FhirContext fhirContext, String... elements) {
      AtomicBoolean match = new AtomicBoolean(true);
      if (res1 != null && res2 != null && res1.fhirType().equals(res2.fhirType())) {
         Arrays.stream(elements).forEach(element -> {
            IBase e1 = TerserUtil.getValueFirstRep(fhirContext, res1, element);
            IBase e2 = TerserUtil.getValueFirstRep(fhirContext, res2, element);
            if (e1 instanceof IPrimitiveType && e2 instanceof IPrimitiveType
                    && !((IPrimitiveType<?>) e1).getValueAsString().equals(((IPrimitiveType<?>) e2).getValueAsString())) {
               match.set(false);
            }
         });
      }
      return match.get();
   }

   public static boolean compareResourceIdUrlAndVersion(IBaseResource res1, IBaseResource res2,
                                                        FhirContext fhirContext) {
      return compareResourcePrimitiveElements(res1, res2, fhirContext, "id", "url", "version");
   }

   public static Map<String, IBaseResource> getActivityDefinitionResources(String planDefinitionPath, FhirContext fhirContext, Boolean includeVersion) {
      Map<String, IBaseResource> activityDefinitions = new HashMap<>();
      IBaseResource planDefinition = IOUtils.readResource(planDefinitionPath, fhirContext, true);
      Object actionChild = resolveProperty(planDefinition, "action", fhirContext);

      if (actionChild != null) {
         if (actionChild instanceof Iterable)
         {
            for (Object action : (Iterable<?>)actionChild) {
               Object definitionChild = resolveProperty(action, "definition", fhirContext);
               if (definitionChild != null) {
                  Object referenceChild = resolveProperty(definitionChild, "reference", fhirContext);

                  String activityDefinitionId = null;
                  // NOTE: A bit of a hack. This whole method probably needs to be refactored to consider different FHIR
                  // versions and the respective ActivityDefinition differences between them.
                  if (fhirContext.getVersion().getVersion().isEquivalentTo(FhirVersionEnum.R4)) {
                     activityDefinitionId = CanonicalUtils.getId((CanonicalType)referenceChild);
                  } else {
                     String activityDefinitionReference = referenceChild.toString();
                     activityDefinitionId = activityDefinitionReference.replace("ActivityDefinition/", "activitydefinition-").replace("_", "-");
                  }

                  for (String path : IOUtils.getActivityDefinitionPaths(fhirContext)) {
                     if (path.contains(activityDefinitionId)) {
                        activityDefinitions.put(path, IOUtils.readResource(path, fhirContext));
                        break;
                     }
                  }
               }
            }
         }
         else {
            Object definitionChild = resolveProperty(actionChild, "definition", fhirContext);
            if (definitionChild != null) {
               Object referenceChild = resolveProperty(definitionChild, "reference", fhirContext);

               String activityDefinitionReference = (String)referenceChild;

               for (String path : IOUtils.getActivityDefinitionPaths(fhirContext)) {
                  if (path.contains(activityDefinitionReference)) {
                     activityDefinitions.put(path, IOUtils.readResource(path, fhirContext));
                  }
               }
            }
         }
      }
      return activityDefinitions;
   }

   public static Object resolveProperty(Object target, String path, FhirContext fhirContext) {
      if (target == null) {
         return null;
      }

      IBase base = (IBase) target;
      if (base instanceof IPrimitiveType) {
         return path.equals("value") ? ((IPrimitiveType<?>) target).getValue() : target;
      }

      BaseRuntimeElementCompositeDefinition<?> definition = resolveRuntimeDefinition(base, fhirContext);
      BaseRuntimeChildDefinition child = definition.getChildByName(path);
      if (child == null) {
         child = resolveChoiceProperty(definition, path);
      }

      List<IBase> values = child.getAccessor().getValues(base);

      if (values == null || values.isEmpty()) {
         return null;
      }

      if (child instanceof RuntimeChildChoiceDefinition && !child.getElementName().equalsIgnoreCase(path)) {
         if (!values.get(0).getClass().getSimpleName().equalsIgnoreCase(child.getChildByName(path).getImplementingClass().getSimpleName()))
         {
            return null;
         }
      }
      //Hack to get DecimalType to work
      if (child.getMax() == 1 && values.get(0) instanceof org.hl7.fhir.dstu3.model.DecimalType) {
         return resolveProperty(values.get(0), "value", fhirContext);
      }
      if (child.getMax() == 1 && values.get(0) instanceof org.hl7.fhir.r4.model.DecimalType) {
         return resolveProperty(values.get(0), "value", fhirContext);
      }
      return child.getMax() < 1 ? values : values.get(0);
   }

   public static BaseRuntimeElementCompositeDefinition<?> resolveRuntimeDefinition(IBase base, FhirContext fhirContext) {
      if (base instanceof IBaseResource) {
         return fhirContext.getResourceDefinition((IBaseResource) base);
      }

      else if (base instanceof IBaseBackboneElement || base instanceof IBaseElement) {
         return (BaseRuntimeElementCompositeDefinition<?>) fhirContext.getElementDefinition(base.getClass());
      }

      else if (base instanceof ICompositeType) {
         return (RuntimeCompositeDatatypeDefinition) fhirContext.getElementDefinition(base.getClass());
      }

      //should be UnkownType
      throw new Error(String.format("Unable to resolve the runtime definition for %s", base.getClass().getName()));
   }

   public static BaseRuntimeChildDefinition resolveChoiceProperty(BaseRuntimeElementCompositeDefinition<?> definition, String path) {
      for (var child : definition.getChildren()) {
         if (child instanceof RuntimeChildChoiceDefinition) {
            RuntimeChildChoiceDefinition choiceDefinition = (RuntimeChildChoiceDefinition) child;

            if (choiceDefinition.getElementName().startsWith(path)) {
               return choiceDefinition;
            }
         }
      }

      //UnkownType
      throw new Error(String.format("Unable to resolve path %s for %s", path, definition.getName()));
   }

   public static RuntimeResourceDefinition getResourceDefinition(FhirContext fhirContext, String resourceName) {
      return fhirContext.getResourceDefinition(resourceName);
   }

   public static BaseRuntimeElementDefinition<?> getElementDefinition(FhirContext fhirContext, String elementName) {
      return fhirContext.getElementDefinition(elementName);
   }

   //to keep track of output already written and avoid duplicate functionality slowing down performance:
   private static ConcurrentHashMap<String, Boolean> outputResourceTracker = new ConcurrentHashMap<>();
   public static final String separator = System.getProperty("file.separator");
   public static void outputResource(IBaseResource resource, String encoding, FhirContext context, String outputPath) {
      String resourceFileLocation = outputPath + separator +
              resource.getIdElement().getResourceType() + "-" + resource.getIdElement().getIdPart() +
              "." + encoding;
      if (outputResourceTracker.containsKey(resource.getIdElement().getResourceType() + ":" + outputPath)){
         LogUtils.info("This resource has already been processed: " + resource.getIdElement().getResourceType());
         return;
      }

      try (FileOutputStream writer = new FileOutputStream(outputPath + "/" + resource.getIdElement().getResourceType() + "-" + resource.getIdElement().getIdPart() + "." + encoding)) {
         writer.write(
                 encoding.equals("json")
                         ? context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                         : context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
         );
         writer.flush();
         outputResourceTracker.put(resourceFileLocation, Boolean.TRUE);

      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException(e.getMessage());
      }
   }

   public static void outputResourceByName(IBaseResource resource, String encoding, FhirContext context, String outputPath, String name) {
      try {
         File directory = new File(outputPath);
         if (!directory.exists()) {
            directory.mkdirs(); // Ensure the directory exists
         }

         try (FileOutputStream writer = new FileOutputStream(outputPath + "/" + name + "." + encoding)) {
            writer.write(
                    encoding.equals("json")
                            ? context.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
                            : context.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
         }
      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException(e.getMessage());
      }
   }

   public static void cleanUp(){
      outputResourceTracker = new ConcurrentHashMap<>();
      cachedElm = new HashMap<String, org.hl7.elm.r1.Library>();
   }

   public static String getCqlFromR4Library(org.hl7.fhir.r4.model.Library library) {
      var cqlContent = library.getContent().stream().filter(content -> content.hasContentType() && content.getContentType().equals("text/cql")).findFirst();
      return cqlContent.map(attachment -> new String(attachment.getData(), StandardCharsets.UTF_8)).orElse(null);
   }
}
