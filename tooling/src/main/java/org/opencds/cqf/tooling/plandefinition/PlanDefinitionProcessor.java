package org.opencds.cqf.tooling.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshPlanDefinitionParameters;
import org.opencds.cqf.tooling.plandefinition.r4.R4PlanDefinitionProcessor;
import org.opencds.cqf.tooling.plandefinition.stu3.STU3PlanDefinitionProcessor;
import org.opencds.cqf.tooling.processor.BaseProcessor;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;
import org.opencds.cqf.tooling.processor.IGBundleProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.processor.TestCaseProcessor;
import org.opencds.cqf.tooling.processor.ValueSetsProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlanDefinitionProcessor extends BaseProcessor {
   public static final String RESOURCE_PREFIX = "plandefinition-";
   public static final String PLANDEFINITION_TEST_GROUP_NAME = "plandefinition";
   public static final String NEWLINE = "\r\n";
   public static final String NEWLINE_AND_SPACING = "\r\n     ";

   protected List<Object> identifiers;
   private final LibraryProcessor libraryProcessor;
   private final CDSHooksProcessor cdsHooksProcessor;
   private final Logger logger = LoggerFactory.getLogger(this.getClass());

   public PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
      this.libraryProcessor = libraryProcessor;
      this.cdsHooksProcessor = cdsHooksProcessor;
   }

   public List<String> refreshIgPlanDefinitionContent(BaseProcessor parentContext, Encoding outputEncoding,
                                                      Boolean versioned, FhirContext fhirContext,
                                                      String planDefinitionToRefreshPath,
                                                      Boolean shouldApplySoftwareSystemStamp) {
      return refreshIgPlanDefinitionContent(parentContext, outputEncoding, null, versioned,
              fhirContext, planDefinitionToRefreshPath, shouldApplySoftwareSystemStamp);
   }

   public List<String> refreshIgPlanDefinitionContent(BaseProcessor parentContext, Encoding outputEncoding,
                                                      String planDefinitionOutputDirectory, Boolean versioned,
                                                      FhirContext fhirContext, String planDefinitionToRefreshPath,
                                                      Boolean shouldApplySoftwareSystemStamp) {
      System.out.println("Refreshing PlanDefinitions...");

      PlanDefinitionProcessor planDefinitionProcessor;
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3:
            planDefinitionProcessor = new STU3PlanDefinitionProcessor(libraryProcessor, cdsHooksProcessor);
            break;
         case R4:
            planDefinitionProcessor = new R4PlanDefinitionProcessor(libraryProcessor, cdsHooksProcessor);
            break;
         default:
            throw new IllegalArgumentException(
                    "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
      }

      String planDefinitionPath = FilenameUtils.concat(
              parentContext.getRootDir(), IGProcessor.planDefinitionPathElement);
      RefreshPlanDefinitionParameters params = new RefreshPlanDefinitionParameters();
      params.planDefinitionPath = planDefinitionPath;
      params.parentContext = parentContext;
      params.fhirContext = fhirContext;
      params.encoding = outputEncoding;
      params.versioned = versioned;
      params.planDefinitionOutputDirectory = planDefinitionOutputDirectory;
      List<String> contentList = planDefinitionProcessor.refreshPlanDefinitionContent(params);

      if (!planDefinitionProcessor.getIdentifiers().isEmpty()) {
         this.getIdentifiers().addAll(planDefinitionProcessor.getIdentifiers());
      }
      return contentList;
   }

   protected List<Object> getIdentifiers() {
      if (identifiers == null) {
         identifiers = new ArrayList<>();
      }
      return identifiers;
   }

   @SuppressWarnings({"squid:S00107", "squid:S3776"})
   public void bundlePlanDefinitions(List<String> refreshedLibraryNames, String igPath,
                                     List<String> binaryPaths, Boolean includeDependencies,
                                     Boolean includeTerminology, Boolean includePatientScenarios,
                                     Boolean includeVersion, FhirContext fhirContext, String fhirUri,
                                     Encoding encoding) {
      Map<String, IBaseResource> planDefinitions = IOUtils.getPlanDefinitions(fhirContext);

      List<String> bundledPlanDefinitions = new ArrayList<>();
      for (Map.Entry<String, IBaseResource> planDefinitionEntry : planDefinitions.entrySet()) {
         String planDefinitionSourcePath = IOUtils.getPlanDefinitionPathMap(fhirContext).get(
                 planDefinitionEntry.getKey());

         // Assumption - File name matches planDefinition.name
         String planDefinitionName = FilenameUtils.getBaseName(
                 planDefinitionSourcePath).replace(RESOURCE_PREFIX, "");
         try {
            Map<String, IBaseResource> resources = new HashMap<>();

            Boolean shouldPersist = ResourceUtils.safeAddResource(
                    planDefinitionSourcePath, resources, fhirContext);

            if (!resources.containsKey("PlanDefinition/" + planDefinitionEntry.getKey())) {
               throw new IllegalArgumentException(String.format(
                       "Could not retrieve base resource for PlanDefinition %s", planDefinitionName));
            }

            IBaseResource planDefinition = resources.get(
                    "PlanDefinition/" + planDefinitionEntry.getKey());

            String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(
                    planDefinition, fhirContext);
            IBaseResource primaryLibrary = null;
            if (primaryLibraryUrl != null) {
               if (primaryLibraryUrl.startsWith("http")) {
                  primaryLibrary = IOUtils.getLibraryUrlMap(fhirContext).get(primaryLibraryUrl);
               } else {
                  primaryLibrary = IOUtils.getLibraries(fhirContext).get(primaryLibraryUrl);
               }
            }

            if (primaryLibrary == null) {
               throw new IllegalArgumentException(
                       String.format("Could not resolve library url %s", primaryLibraryUrl));
            }

            String primaryLibrarySourcePath = IOUtils.getLibraryPathMap(fhirContext)
                    .get(primaryLibrary.getIdElement().getIdPart());
            String primaryLibraryName = ResourceUtils.getName(primaryLibrary, fhirContext);
            if (Boolean.TRUE.equals(includeVersion)) {
               Optional<IBase> fhirPathResult = fhirContext.newFhirPath().evaluateFirst(
                       primaryLibrary, "version", IBase.class);
               if (fhirPathResult.isPresent()) {
                  primaryLibraryName = primaryLibraryName + "-" + fhirPathResult.get();
               }
            }

            shouldPersist = shouldPersist
                    && ResourceUtils.safeAddResource(primaryLibrarySourcePath, resources, fhirContext);

            String cqlFileName = IOUtils.formatFileName(primaryLibraryName, Encoding.CQL, fhirContext);

            String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(
                    primaryLibraryName, cqlFileName, binaryPaths);

            if (cqlLibrarySourcePath == null) {
               throw new IllegalArgumentException(
                       String.format(
                               "Could not determine CqlLibrarySource path for library %s", primaryLibraryName));
            }

            if (Boolean.TRUE.equals(includeTerminology)) {
               boolean result = ValueSetsProcessor.bundleValueSets(cqlLibrarySourcePath, igPath,
                       fhirContext, resources, encoding, includeDependencies, includeVersion);
               if (Boolean.TRUE.equals(shouldPersist) && !result) {
                  LogUtils.info("PlanDefinition will not be bundled because ValueSet bundling failed.");
               }
               shouldPersist = shouldPersist && result;
            }

            if (Boolean.TRUE.equals(includeDependencies)) {
               boolean result = libraryProcessor.bundleLibraryDependencies(primaryLibrarySourcePath,
                       fhirContext, resources, encoding, includeVersion);
               if (Boolean.TRUE.equals(shouldPersist) && !result) {
                  LogUtils.info("PlanDefinition will not be bundled because Library Dependency bundling failed.");
               }
               shouldPersist = shouldPersist && result;
            }

            if (Boolean.TRUE.equals(includePatientScenarios)) {
               boolean result = TestCaseProcessor.bundleTestCases(igPath, PLANDEFINITION_TEST_GROUP_NAME,
                       primaryLibraryName, fhirContext, resources);
               if (Boolean.TRUE.equals(shouldPersist) && !result) {
                  LogUtils.info("PlanDefinition will not be bundled because Test Case bundling failed.");
               }
               shouldPersist = shouldPersist && result;
            }

            List<String> activityDefinitionPaths =  CDSHooksProcessor.bundleActivityDefinitions(
                    planDefinitionSourcePath, fhirContext, resources, encoding, includeVersion, shouldPersist);

            if (Boolean.TRUE.equals(shouldPersist)) {
               String bundleDestPath = FilenameUtils.concat(FilenameUtils.concat(
                       IGProcessor.getBundlesPath(igPath), PLANDEFINITION_TEST_GROUP_NAME), planDefinitionName);
               persistBundle(bundleDestPath, planDefinitionName, encoding, fhirContext,
                       new ArrayList<>(resources.values()), fhirUri);
               bundleFiles(igPath, bundleDestPath, primaryLibraryName, binaryPaths, planDefinitionSourcePath,
                       primaryLibrarySourcePath, fhirContext, encoding, includeTerminology, includeDependencies,
                       includePatientScenarios, includeVersion);
               cdsHooksProcessor.addActivityDefinitionFilesToBundle(igPath, bundleDestPath,
                       activityDefinitionPaths, fhirContext, encoding);
               bundledPlanDefinitions.add(planDefinitionSourcePath);
            }
         } catch (Exception e) {
            LogUtils.putException(planDefinitionName, e);
         } finally {
            LogUtils.warn(planDefinitionName);
         }
      }

      StringBuilder message = new StringBuilder(NEWLINE)
              .append(bundledPlanDefinitions.size()).append(" PlanDefinitions successfully bundled:");
      for (String bundledPlanDefinition : bundledPlanDefinitions) {
         message.append(NEWLINE_AND_SPACING).append(bundledPlanDefinition).append(" BUNDLED");
      }

      List<String> planDefinitionPathLibraryNames = new ArrayList<>(IOUtils.getPlanDefinitionPaths(fhirContext));
      ArrayList<String> failedPlanDefinitions = new ArrayList<>(planDefinitionPathLibraryNames);
      planDefinitionPathLibraryNames.removeAll(bundledPlanDefinitions);
      planDefinitionPathLibraryNames.retainAll(refreshedLibraryNames);
      message.append(NEWLINE).append(planDefinitionPathLibraryNames.size())
              .append(" PlanDefinitions refreshed, but not bundled (due to issues):");
      for (String notBundled : planDefinitionPathLibraryNames) {
         message.append(NEWLINE_AND_SPACING).append(notBundled).append(" REFRESHED");
      }

      failedPlanDefinitions.removeAll(bundledPlanDefinitions);
      failedPlanDefinitions.removeAll(planDefinitionPathLibraryNames);
      message.append(NEWLINE).append(failedPlanDefinitions.size()).append(" PlanDefinitions failed refresh:");
      for (String failed : failedPlanDefinitions) {
         message.append(NEWLINE_AND_SPACING).append(failed).append(" FAILED");
      }

      LogUtils.info(message.toString());
   }

   private void persistBundle(String bundleDestPath, String libraryName, Encoding encoding,
                              FhirContext fhirContext, List<IBaseResource> resources, String fhirUri) {
      IOUtils.initializeDirectory(bundleDestPath);
      Object bundle = BundleUtils.bundleArtifacts(libraryName, resources, fhirContext);
      IOUtils.writeBundle(bundle, bundleDestPath, encoding, fhirContext);

      if (fhirUri != null && !fhirUri.equals("")) {
         try {
            HttpClientUtils.post(fhirUri, (IBaseResource) bundle, encoding, fhirContext);
         } catch (IOException e) {
            LogUtils.putException(((IBaseResource)bundle).getIdElement().getIdPart(),
                    "Error posting to FHIR Server: " + fhirUri + ".  Bundle not posted.");
            File dir = new File("C:\\src\\GitHub\\logs");
            dir.mkdir();
            IOUtils.writeBundle(bundle, dir.getAbsolutePath(), encoding, fhirContext);
         }
      }
   }

   @SuppressWarnings("squid:S00107")
   private void bundleFiles(String igPath, String bundleDestPath, String libraryName, List<String> binaryPaths,
                            String resourceFocusSourcePath, String librarySourcePath, FhirContext fhirContext,
                            Encoding encoding, Boolean includeTerminology, Boolean includeDependencies,
                            Boolean includePatientScenarios, Boolean includeVersion) {
      String bundleDestFilesPath = FilenameUtils.concat(bundleDestPath,
              FilenameUtils.getBaseName(bundleDestPath) + "-" + IGBundleProcessor.bundleFilesPathElement);
      IOUtils.initializeDirectory(bundleDestFilesPath);

      IOUtils.copyFile(resourceFocusSourcePath, FilenameUtils.concat(
              bundleDestFilesPath, FilenameUtils.getName(resourceFocusSourcePath)));
      IOUtils.copyFile(librarySourcePath, FilenameUtils.concat(
              bundleDestFilesPath, FilenameUtils.getName(librarySourcePath)));

      String cqlFileName = IOUtils.formatFileName(libraryName, Encoding.CQL, fhirContext);
      String cqlLibrarySourcePath = IOUtils.getCqlLibrarySourcePath(libraryName, cqlFileName, binaryPaths);
      String cqlDestPath = FilenameUtils.concat(bundleDestFilesPath, cqlFileName);
      IOUtils.copyFile(cqlLibrarySourcePath, cqlDestPath);

      if (Boolean.TRUE.equals(includeTerminology)) {
         try {
            Map<String, IBaseResource> valuesets = ResourceUtils.getDepValueSetResources(
                    cqlLibrarySourcePath, igPath, fhirContext, includeDependencies, includeVersion);
            if (!valuesets.isEmpty()) {
               Object bundle = BundleUtils.bundleArtifacts(
                       ValueSetsProcessor.getId(libraryName), new ArrayList<>(valuesets.values()), fhirContext);
               IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
            }
         }  catch (Exception e) {
            LogUtils.putException(libraryName, e.getMessage());
         }
      }

      if (Boolean.TRUE.equals(includeDependencies)) {
         Map<String, IBaseResource> depLibraries = ResourceUtils.getDepLibraryResources(
                 librarySourcePath, fhirContext, encoding, includeVersion, logger);
         if (!depLibraries.isEmpty()) {
            String depLibrariesID = "library-deps-" + libraryName;
            Object bundle = BundleUtils.bundleArtifacts(depLibrariesID,
                    new ArrayList<>(depLibraries.values()), fhirContext);
            IOUtils.writeBundle(bundle, bundleDestFilesPath, encoding, fhirContext);
         }
      }

      if (Boolean.TRUE.equals(includePatientScenarios)) {
         TestCaseProcessor.bundleTestCaseFiles(igPath, PLANDEFINITION_TEST_GROUP_NAME,
                 libraryName, bundleDestFilesPath, fhirContext);
      }
   }

   protected boolean versioned;
   protected FhirContext fhirContext;

   public List<String> refreshPlanDefinitionContent(RefreshPlanDefinitionParameters params) {
      return new ArrayList<>();
   }

   protected List<PlanDefinition> refreshGeneratedContent(List<PlanDefinition> sourcePlanDefinitions) {
      return internalRefreshGeneratedContent(sourcePlanDefinitions);
   }

   private List<PlanDefinition> internalRefreshGeneratedContent(List<PlanDefinition> sourcePlanDefinitions) {
      // for each PlanDefinition, refresh the PlanDefinition based on the primary PlanDefinition library
      List<PlanDefinition> resources = new ArrayList<>();
      for (PlanDefinition planDefinition : sourcePlanDefinitions) {
         resources.add(refreshGeneratedContent(planDefinition));
      }
      return resources;
   }

   private PlanDefinition refreshGeneratedContent(PlanDefinition planDefinition) {
      PlanDefinitionRefreshProcessor processor = new PlanDefinitionRefreshProcessor();
      LibraryManager libraryManager = getCqlProcessor().getLibraryManager();
      CqlTranslatorOptions cqlTranslatorOptions = getCqlProcessor().getCqlTranslatorOptions();
      // Do not attempt to refresh if the PlanDefinition does not have a library
      if (planDefinition.hasLibrary()) {
         String libraryUrl = ResourceUtils.getPrimaryLibraryUrl(planDefinition, fhirContext);
         VersionedIdentifier primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);
         List<CqlCompilerException> errors = new ArrayList<>();
         CompiledLibrary compiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, cqlTranslatorOptions, errors);
         boolean hasErrors = false;
         if (!errors.isEmpty()) {
            for (CqlCompilerException e : errors) {
               if (e.getSeverity() == CqlCompilerException.ErrorSeverity.Error) {
                  hasErrors = true;
               }
               logMessage(e.getMessage());
            }
         }
         if (!hasErrors) {
            return processor.refreshPlanDefinition(planDefinition, libraryManager, compiledLibrary, cqlTranslatorOptions);
         }
      }
      return planDefinition;
   }
}