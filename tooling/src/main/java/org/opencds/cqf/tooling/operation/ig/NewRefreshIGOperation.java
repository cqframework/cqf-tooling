package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleBuilder;
import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
import org.opencds.cqf.tooling.parameter.RefreshIGParameters;
import org.opencds.cqf.tooling.processor.argument.RefreshIGArgumentProcessor;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NewRefreshIGOperation extends Operation {
   private static final Logger logger = LoggerFactory.getLogger(NewRefreshIGOperation.class);
   private RefreshIGParameters params;

   @Override
   public void execute(String[] args) {
      try {
         this.params = new RefreshIGArgumentProcessor().parseAndConvert(args);
         IGInfo info = new IGInfo(null, params);
         CqlRefresh cqlRefresh = new CqlRefresh(info);
         cqlRefresh.refreshCql(info, params);
         LibraryRefresh libraryRefresh = new LibraryRefresh(info);
         publishLibraries(info, libraryRefresh.refresh(this.params));
         PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(info, libraryRefresh.getCqlProcessor(), libraryRefresh.getLibraryPackages());
         publishPlanDefinitions(info, planDefinitionRefresh.refresh());
         if (!planDefinitionRefresh.getPlanDefinitionPackages().isEmpty()) {
             publishPlanDefinitionBundles(planDefinitionRefresh);
         }
         MeasureRefresh measureRefresh = new MeasureRefresh(info, libraryRefresh.getCqlProcessor(), libraryRefresh.getLibraryPackages());
         publishMeasures(info, measureRefresh.refresh());
         if (!measureRefresh.getMeasurePackages().isEmpty()) {
             publishMeasureBundles(measureRefresh);
         }
         // TODO: bundle IG/testcases
      } catch (Exception e) {
         logger.error(e.getMessage());
         System.exit(1);
      }
   }

   private void publishPlanDefinitionBundles(PlanDefinitionRefresh planDefinitionRefresh) {
      String pathToBundles = FilenameUtils.concat(params.rootDir, "bundles");
      String pathToPlanDefinitionBundles = FilenameUtils.concat(pathToBundles, "plandefinition");
      try {
         IOUtils.ensurePath(pathToBundles);
         IOUtils.ensurePath(pathToPlanDefinitionBundles);
         planDefinitionRefresh.getPlanDefinitionPackages().forEach(
                 pkg -> {
                    String id = pkg.getPlanDefinition().getIdElement().getIdPart();
                    String pathToPackage = FilenameUtils.concat(pathToPlanDefinitionBundles, id);
                    IOUtils.writeResource(pkg.bundleResources(), pathToPackage, IOUtils.Encoding.JSON,
                            pkg.getFhirContext(), params.versioned, id + "-bundle");
                    String pathToFiles = FilenameUtils.concat(pathToPackage, "files");
                    IOUtils.writeResource(pkg.getPlanDefinition(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    id = pkg.getLibraryPackage().getLibrary().getIdElement().getIdPart();
                    IOUtils.writeResource(pkg.getLibraryPackage().getLibrary(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    BundleBuilder builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnLibraries().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            params.versioned, "library-deps-" + id + "-bundle");
                    builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnValueSets().forEach(builder::addTransactionUpdateEntry);
                    pkg.getLibraryPackage().getDependsOnCodeSystems().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            params.versioned, "terminology-" + id + "-bundle");
                    // TODO: output CQL and ELM - also maybe XML files?
                 }
         );
      } catch (Exception e) {
         logger.warn(e.getMessage());
      }
   }

   private void publishMeasureBundles(MeasureRefresh measureRefresh) {
      String pathToBundles = FilenameUtils.concat(params.rootDir, "bundles");
      String pathToMeasureBundles = FilenameUtils.concat(pathToBundles, "measure");
      try {
         IOUtils.ensurePath(pathToBundles);
         IOUtils.ensurePath(pathToMeasureBundles);
         measureRefresh.getMeasurePackages().forEach(
                 pkg -> {
                    String id = pkg.getMeasure().getIdElement().getIdPart();
                    String pathToPackage = FilenameUtils.concat(pathToMeasureBundles, id);
                    IOUtils.writeResource(pkg.bundleResources(), pathToPackage, IOUtils.Encoding.JSON,
                            pkg.getFhirContext(), params.versioned, id + "-bundle");
                    String pathToFiles = FilenameUtils.concat(pathToPackage, "files");
                    IOUtils.writeResource(pkg.getMeasure(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    id = pkg.getLibraryPackage().getLibrary().getIdElement().getIdPart();
                    IOUtils.writeResource(pkg.getLibraryPackage().getLibrary(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    BundleBuilder builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnLibraries().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            params.versioned, "library-deps-" + id + "-bundle");
                    builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnValueSets().forEach(builder::addTransactionUpdateEntry);
                    pkg.getLibraryPackage().getDependsOnCodeSystems().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            params.versioned, "terminology-" + id + "-bundle");
                    // TODO: output CQL and ELM - also maybe XML files?
                 }
         );
      } catch (Exception e) {
         logger.warn(e.getMessage());
      }
   }

   private void publishLibraries (IGInfo igInfo, List<IBaseResource> libraries) {
      String outputPath = this.params.libraryOutputPath != null && !this.params.libraryOutputPath.isEmpty()
              ? this.params.libraryOutputPath : igInfo.getLibraryResourcePath();
      for (var library : libraries) {
         applySoftwareSystemStamp(igInfo.getFhirContext(), library);
         IOUtils.writeResource(library, outputPath, this.params.outputEncoding,
                 igInfo.getFhirContext(), this.params.versioned, true);
      }
   }

   private void publishPlanDefinitions (IGInfo igInfo, List<IBaseResource> planDefinitions) {
      // TODO: enable user to set output path
      String outputPath = igInfo.getPlanDefinitionResourcePath();
      for (var planDefinition : planDefinitions) {
         applySoftwareSystemStamp(igInfo.getFhirContext(), planDefinition);
         IOUtils.writeResource(planDefinition, outputPath, this.params.outputEncoding,
                 igInfo.getFhirContext(), this.params.versioned, true);
      }
   }

   private void publishMeasures (IGInfo igInfo, List<IBaseResource> measures) {
      String outputPath = this.params.measureOutputPath != null && !this.params.measureOutputPath.isEmpty()
              ? this.params.measureOutputPath : igInfo.getMeasureResourcePath();
      for (var measure : measures) {
         applySoftwareSystemStamp(igInfo.getFhirContext(), measure);
         IOUtils.writeResource(measure, outputPath, this.params.outputEncoding,
                 igInfo.getFhirContext(), this.params.versioned, true);
      }
   }

   private SoftwareSystemHelper r4CqfmSoftwareSystemHelper;
   private org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper dstu3SoftwareSystemHelper;
   private void applySoftwareSystemStamp (FhirContext fhirContext, IBaseResource resource) {
      if (Boolean.TRUE.equals(this.params.shouldApplySoftwareSystemStamp)) {
         if (resource instanceof org.hl7.fhir.r4.model.DomainResource) {
            if (r4CqfmSoftwareSystemHelper == null) {
               r4CqfmSoftwareSystemHelper = new SoftwareSystemHelper();
            }
            r4CqfmSoftwareSystemHelper.ensureCQFToolingExtensionAndDevice(
                    (org.hl7.fhir.r4.model.DomainResource) resource, fhirContext);
         } else if (resource instanceof org.hl7.fhir.dstu3.model.DomainResource) {
            if (dstu3SoftwareSystemHelper == null) {
               dstu3SoftwareSystemHelper = new org.opencds.cqf.tooling.common.stu3.SoftwareSystemHelper();
            }
            dstu3SoftwareSystemHelper.ensureCQFToolingExtensionAndDevice(
                    (org.hl7.fhir.dstu3.model.DomainResource) resource, fhirContext);
         } else {
            logger.warn("CqfmSoftwareSystemHelper not supported for version {}",
                    fhirContext.getVersion().getVersion().getFhirVersionString());
         }
      }
   }
}
