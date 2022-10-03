package org.opencds.cqf.tooling.plandefinition.r4;

import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshPlanDefinitionParameters;
import org.opencds.cqf.tooling.plandefinition.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.processor.CDSHooksProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class R4PlanDefinitionProcessor extends PlanDefinitionProcessor {

   private static CqfmSoftwareSystemHelper cqfmHelper;

   public R4PlanDefinitionProcessor(LibraryProcessor libraryProcessor, CDSHooksProcessor cdsHooksProcessor) {
      super(libraryProcessor, cdsHooksProcessor);
   }

   private String getPlanDefinitionPath(String planDefinitionPath) {
      File f = new File(planDefinitionPath);
      if (!f.exists() && f.getParentFile().isDirectory() && f.getParentFile().exists()) {
         return f.getParentFile().toString();
      }
      return planDefinitionPath;
   }
   /*
       Refresh all PlanDefinition resources in the given planDefinitionPath
       If the path is not specified, or is not a known directory, process
       all known PlanDefinition resources overriding any currently existing files.
   */
   protected List<String> refreshPlanDefinitions(String planDefinitionPath, IOUtils.Encoding encoding) {
      return refreshPlanDefinitions(planDefinitionPath, null, encoding);
   }

   /*
       Refresh all PlanDefinition resources in the given planDefinitionPath
       If the path is not specified, or is not a known directory, process
       all known PlanDefinition resources
   */
   protected List<String> refreshPlanDefinitions(String planDefinitionPath, String planDefinitionOutputDirectory, IOUtils.Encoding encoding) {
      File file = planDefinitionPath != null ? new File(planDefinitionPath) : null;
      Map<String, String> fileMap = new HashMap<>();
      List<org.hl7.fhir.r5.model.PlanDefinition> planDefinitions = new ArrayList<>();

      if (file == null || !file.exists()) {
         for (String path : IOUtils.getPlanDefinitionPaths(this.fhirContext)) {
            loadPlanDefinition(fileMap, planDefinitions, new File(path));
         }
      }
      else if (file.isDirectory()) {
         for (File libraryFile : Objects.requireNonNull(file.listFiles())) {
            if(IOUtils.isXMLOrJson(planDefinitionPath, libraryFile.getName())) {
               loadPlanDefinition(fileMap, planDefinitions, libraryFile);
            }
         }
      }
      else {
         loadPlanDefinition(fileMap, planDefinitions, file);
      }

      List<String> refreshedPlanDefinitionNames = new ArrayList<>();
      List<org.hl7.fhir.r5.model.PlanDefinition> refreshedPlanDefinitions = super.refreshGeneratedContent(planDefinitions);
      VersionConvertor_40_50 versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
      for (org.hl7.fhir.r5.model.PlanDefinition refreshedPlanDefinition : refreshedPlanDefinitions) {
         org.hl7.fhir.r4.model.PlanDefinition planDefinition = (org.hl7.fhir.r4.model.PlanDefinition) versionConvertor.convertResource(refreshedPlanDefinition);
         if (planDefinition.hasIdentifier() && !planDefinition.getIdentifier().isEmpty()) {
            this.getIdentifiers().addAll(planDefinition.getIdentifier());
         }
         String filePath;
         IOUtils.Encoding fileEncoding;
         if (fileMap.containsKey(refreshedPlanDefinition.getId()))
         {
            filePath = fileMap.get(refreshedPlanDefinition.getId());
            fileEncoding = IOUtils.getEncoding(filePath);
         } else {
            filePath = getPlanDefinitionPath(planDefinitionPath);
            fileEncoding = encoding;
         }
         cqfmHelper.ensureCQFToolingExtensionAndDevice(planDefinition, fhirContext);
         // Issue 96
         // Passing the includeVersion here to handle not using the version number in the filename
         if (new File(filePath).exists()) {
            // TODO: This prevents mangled names from being output
            // It would be nice for the tooling to generate library shells, we have enough information to,
            // but the tooling gets confused about the ID and the filename and what gets written is garbage
            String outputPath = filePath;
            if (planDefinitionOutputDirectory != null) {
               File planDefinitionDirectory = new File(planDefinitionOutputDirectory);
               if (!planDefinitionDirectory.exists()) {
                  //TODO: add logger and log non existant directory for writing
               } else {
                  outputPath = planDefinitionDirectory.getAbsolutePath();
               }
            }
            IOUtils.writeResource(planDefinition, outputPath, fileEncoding, fhirContext, this.versioned);
            String refreshedPlanDefinitionName;
            if (this.versioned && refreshedPlanDefinition.getVersion() != null) {
               refreshedPlanDefinitionName = refreshedPlanDefinition.getName() + "-" + refreshedPlanDefinition.getVersion();
            } else {
               refreshedPlanDefinitionName = refreshedPlanDefinition.getName();
            }
            refreshedPlanDefinitionNames.add(refreshedPlanDefinitionName);
         }
      }

      return refreshedPlanDefinitionNames;
   }

   private void loadPlanDefinition(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.PlanDefinition> planDefinitions, File planDefinitionFile) {
      try {
         org.hl7.fhir.r4.model.Resource resource = FormatUtilities.loadFile(planDefinitionFile.getAbsolutePath());
         VersionConvertor_40_50 versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
         org.hl7.fhir.r5.model.PlanDefinition planDefinition = (org.hl7.fhir.r5.model.PlanDefinition) versionConvertor.convertResource(resource);
         fileMap.put(planDefinition.getId(), planDefinitionFile.getAbsolutePath());
         planDefinitions.add(planDefinition);
      } catch (Exception ex) {
         logMessage(String.format("Error reading PlanDefinition: %s. Error: %s", planDefinitionFile.getAbsolutePath(), ex.getMessage()));
      }
   }

   @Override
   public List<String> refreshPlanDefinitionContent(RefreshPlanDefinitionParameters params) {
      if (params.parentContext != null) {
         initialize(params.parentContext);
      }
      else {
         initializeFromIni(params.ini);
      }

      String planDefinitionPath = params.planDefinitionPath;
      String planDefinitionOutputDirectory = params.planDefinitionOutputDirectory;
      fhirContext = params.fhirContext;
      IOUtils.Encoding encoding = params.encoding;
      versioned = params.versioned;

      cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

      if (planDefinitionOutputDirectory != null) {
         return refreshPlanDefinitions(planDefinitionPath, planDefinitionOutputDirectory, encoding);
      } else {
         return refreshPlanDefinitions(planDefinitionPath, encoding);
      }
   }
}
