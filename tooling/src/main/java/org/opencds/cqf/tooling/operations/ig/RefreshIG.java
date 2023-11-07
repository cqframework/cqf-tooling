package org.opencds.cqf.tooling.operations.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.operations.bundle.BundleResources;
import org.opencds.cqf.tooling.operations.library.LibraryRefresh;
import org.opencds.cqf.tooling.operations.measure.MeasureRefresh;
import org.opencds.cqf.tooling.operations.plandefinition.PlanDefinitionRefresh;
import org.opencds.cqf.tooling.processor.PlanDefinitionProcessor;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IGUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Operation(name = "RefreshIG")
public class RefreshIG implements ExecutableOperation {

    private static final Logger logger = LoggerFactory.getLogger(RefreshIG.class);

   @OperationParam(alias = { "ip", "igp", "pathtoig" }, setter = "setPathToImplementationGuide", required = true,
           description = "Path to the root directory of the Implementation Guide (required).")
   private String pathToImplementationGuide;
    @OperationParam(alias = { "elm", "pwelm", "packagewithelm" }, setter = "setIncludeElm", defaultValue = "false",
           description = "Determines whether ELM will be produced or packaged (omitted by default).")
   private Boolean includeElm;
   @OperationParam(alias = { "d", "id", "pd", "packagedependencies" }, setter = "setIncludeDependencies", defaultValue = "false",
           description = "Determines whether libraries other than the primary will be packaged (omitted by default).")
   private Boolean includeDependencies;
   @OperationParam(alias = { "t", "it", "pt", "packageterminology" }, setter = "setIncludeTerminology", defaultValue = "false",
           description = "Determines whether terminology will be packaged (omitted by default).")
   private Boolean includeTerminology;
   @OperationParam(alias = { "p", "ipat", "pp", "packagepatients" }, setter = "setIncludePatients", defaultValue = "false",
           description = "Determines whether patient scenario information will be packaged (omitted by default).")
   private Boolean includePatients;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR Library { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           description = "The directory path to which the generated FHIR resources should be written (default is to replace existing resources within the IG)")
   private String outputPath;

   @OperationParam(alias = { "lop", "libraryoutputpath" }, setter = "setLibraryOutputPath",
           description = "The directory path to which the generated libraries should be written (default is to replace existing resources within the IG)")
   private String libraryOutputPath;

    @OperationParam(alias = { "mop", "measureoutputpath" }, setter = "setMeasureOutputPath",
            description = "The directory path to which the generated measures should be written (default is to replace existing resources within the IG)")
    private String measureOutputPath;

    @OperationParam(alias = { "pdop", "plandefinitionoutputpath" }, setter = "setPlanDefinitionOutputPath",
            description = "The directory path to which the generated plan definitions should be written (default is to replace existing resources within the IG)")
    private String planDefinitionOutputPath;

   @Override
   public void execute() {
      FhirContext context = FhirContextCache.getContext(version);
      IGUtils.IGInfo info = new IGUtils.IGInfo(context, pathToImplementationGuide);

      //override default resource paths if they have been provided.
      info.setLibraryResourcePath(libraryOutputPath);
      info.setMeasureResourcePath(measureOutputPath);
      info.setPlanDefinitionResourcePath(planDefinitionOutputPath);

      // refresh libraries
      LibraryRefresh libraryRefresh = new LibraryRefresh(info);
      publishLibraries(info, libraryRefresh.refreshLibraries(info, libraryRefresh.getCqlProcessor()));

      // package (Bundle or list of resources)
       BundleResources bundleResources = new BundleResources();
       bundleResources.setEncoding(encoding);
       bundleResources.setVersion(version);
       bundleResources.setOutputPath(outputPath);
       bundleResources.setPathToResources(info.getLibraryResourcePath());
       bundleResources.execute();

      // refresh measures (references library)
       MeasureRefresh measureRefresh = new MeasureRefresh(info, libraryRefresh.getCqlProcessor());
       publishMeasures(info, measureRefresh.refreshMeasures(info, libraryRefresh.getCqlProcessor()));

      // package (Bundle or list of resources)
       bundleResources.setPathToResources(info.getMeasureResourcePath());
       bundleResources.execute();

      // refresh plandefinitions (references library)
      PlanDefinitionRefresh planDefinitionRefresh = new PlanDefinitionRefresh(info, libraryRefresh.getCqlProcessor(),
              libraryRefresh.getLibraryPackages());
      publishPlanDefinitions(info, planDefinitionRefresh.refreshPlanDefinitions(info, libraryRefresh.getCqlProcessor()));
      publishPlanDefinitionBundles(planDefinitionRefresh);

      // package (Bundle or list of resources) - also includes
      // publish
       bundleResources.setPathToResources(info.getPlanDefinitionResourcePath());
       bundleResources.execute();
   }

    private void publishLibraries (IGUtils.IGInfo igInfo, List<IBaseResource> libraries) {
        String outputPath =  igInfo.getLibraryResourcePath();
        for (var library : libraries) {
            applySoftwareSystemStamp(igInfo.getFhirContext(), library);
            IOUtils.writeResource(library, outputPath, IOUtils.Encoding.parse(encoding),
                    igInfo.getFhirContext(), true, true);
        }
    }

    private void publishMeasures (IGUtils.IGInfo igInfo, List<IBaseResource> measures) {
        String outputPath = igInfo.getMeasureResourcePath();
        for (var measure : measures) {
            applySoftwareSystemStamp(igInfo.getFhirContext(), measure);
            IOUtils.writeResource(measure, outputPath, IOUtils.Encoding.parse(encoding),
                    igInfo.getFhirContext(), true, true);
        }
    }

    private void publishPlanDefinitions (IGUtils.IGInfo igInfo, List<IBaseResource> planDefinitions) {
        String outputPath = igInfo.getPlanDefinitionResourcePath();
        for (var planDefinition : planDefinitions) {
            applySoftwareSystemStamp(igInfo.getFhirContext(), planDefinition);
            IOUtils.writeResource(planDefinition, outputPath, IOUtils.Encoding.parse(encoding),
                    igInfo.getFhirContext(), true, true);
        }
    }

    private void publishPlanDefinitionBundles(PlanDefinitionRefresh planDefinitionRefresh) {
        String pathToBundles = FilenameUtils.concat(outputPath, "bundles");
        String pathToPlanDefinitionBundles = FilenameUtils.concat(pathToBundles, "plandefinition");

        IOUtils.ensurePath(pathToBundles);
        IOUtils.ensurePath(pathToPlanDefinitionBundles);
        planDefinitionRefresh.getPlanDefinitionPackages().forEach(
                pkg -> {
                    String id = pkg.getPlanDefinition().getIdElement().getIdPart();
                    String pathToPackage = FilenameUtils.concat(pathToPlanDefinitionBundles, id);
                    IOUtils.writeResource(pkg.bundleResources(), pathToPackage, IOUtils.Encoding.JSON,
                            pkg.getFhirContext(), true, id + "-bundle");
                    String pathToFiles = FilenameUtils.concat(pathToPackage, "files");
                    IOUtils.writeResource(pkg.getPlanDefinition(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    id = pkg.getLibraryPackage().getLibrary().getIdElement().getIdPart();
                    IOUtils.writeResource(pkg.getLibraryPackage().getLibrary(), pathToFiles, IOUtils.Encoding.JSON,
                            pkg.getFhirContext());
                    BundleBuilder builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnLibraries().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            true, "library-deps-" + id + "-bundle");
                    builder = new BundleBuilder(pkg.getFhirContext());
                    pkg.getLibraryPackage().getDependsOnValueSets().forEach(builder::addTransactionUpdateEntry);
                    pkg.getLibraryPackage().getDependsOnCodeSystems().forEach(builder::addTransactionUpdateEntry);
                    IOUtils.writeResource(builder.getBundle(), pathToFiles, IOUtils.Encoding.JSON, pkg.getFhirContext(),
                            true, "terminology-" + id + "-bundle");
                    // TODO: output CQL and ELM - also maybe XML files?
                }
        );

    }

    private org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper r4CqfmSoftwareSystemHelper;
    private org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper dstu3CqfmSoftwareSystemHelper;
    private void applySoftwareSystemStamp (FhirContext fhirContext, IBaseResource resource) {
        if (resource instanceof org.hl7.fhir.r4.model.DomainResource) {
            if (r4CqfmSoftwareSystemHelper == null) {
                r4CqfmSoftwareSystemHelper = new org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper();
            }
            r4CqfmSoftwareSystemHelper.ensureCQFToolingExtensionAndDevice(
                    (org.hl7.fhir.r4.model.DomainResource) resource, fhirContext);
        } else if (resource instanceof org.hl7.fhir.dstu3.model.DomainResource) {
            if (dstu3CqfmSoftwareSystemHelper == null) {
                dstu3CqfmSoftwareSystemHelper = new org.opencds.cqf.tooling.common.stu3.CqfmSoftwareSystemHelper();
            }
            dstu3CqfmSoftwareSystemHelper.ensureCQFToolingExtensionAndDevice(
                    (org.hl7.fhir.dstu3.model.DomainResource) resource, fhirContext);
        } else {
            logger.warn("CqfmSoftwareSystemHelper not supported for version {}",
                    fhirContext.getVersion().getVersion().getFhirVersionString());
        }
    }

    public String getPathToImplementationGuide() {
        return pathToImplementationGuide;
    }

    public void setPathToImplementationGuide(String pathToImplementationGuide) {
        this.pathToImplementationGuide = pathToImplementationGuide;
    }

    public Boolean getIncludeElm() {
        return includeElm;
    }

    public void setIncludeElm(Boolean includeElm) {
        this.includeElm = includeElm;
    }

    public Boolean getIncludeDependencies() {
        return includeDependencies;
    }

    public void setIncludeDependencies(Boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }

    public Boolean getIncludeTerminology() {
        return includeTerminology;
    }

    public void setIncludeTerminology(Boolean includeTerminology) {
        this.includeTerminology = includeTerminology;
    }

    public Boolean getIncludePatients() {
        return includePatients;
    }

    public void setIncludePatients(Boolean includePatients) {
        this.includePatients = includePatients;
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

    public String getLibraryOutputPath() {
        return libraryOutputPath;
    }

    public void setLibraryOutputPath(String libraryOutputPath) {
        this.libraryOutputPath = libraryOutputPath;
    }

    public String getMeasureOutputPath() {
        return measureOutputPath;
    }

    public void setMeasureOutputPath(String measureOutputPath) {
        this.measureOutputPath = measureOutputPath;
    }

    public String getPlanDefinitionOutputPath() {
        return planDefinitionOutputPath;
    }

    public void setPlanDefinitionOutputPath(String planDefinitionOutputPath) {
        this.planDefinitionOutputPath = planDefinitionOutputPath;
    }
}
