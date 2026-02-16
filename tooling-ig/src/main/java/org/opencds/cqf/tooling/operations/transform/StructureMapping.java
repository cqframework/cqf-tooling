package org.opencds.cqf.tooling.operations.transform;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.utils.StructureMapUtilities;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.NpmUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Operation(name = "StructureMapping")
public class StructureMapping implements ExecutableOperation {

   @OperationParam(alias = { "ptpd", "pathtopatientdata" }, setter = "setPathToPatientData", required = true,
           description = "Path to the patient data represented as either a FHIR Bundle resource or as flat files within a directory (required).")
   private String pathToPatientData;
   @OperationParam(alias = { "ptsm", "pathtostructuremaps" }, setter = "setPathToStructureMaps", required = true,
           description = "Path to the FHIR StructureMap resource(s) used for the transformation. Can be either a file or directory (required).")
   private String pathToStructureMaps;
   @OperationParam(alias = { "purl", "packageurl" }, setter = "setPackageUrl", required = true,
           description = "Url for the FHIR packages to use for transformation (required).")
   private String packageUrl;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting transformed FHIR resources { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           description = "The directory path to which the transformed FHIR resources should be written (default is to replace existing resources within the IG)")
   private String outputPath;

   private FhirContext fhirContext;
   private final Map<String, StructureMap> structureMapMap = new HashMap<>();
   private StructureMapUtilities structureMapUtilities;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      populateStructureMapMap(getStructureMaps());
      setDefaultStructureMapUtilities();
      List<IBaseResource> resources = BundleUtil.toListOfResources(fhirContext,
              IOUtils.bundleResourcesInDirectory(pathToPatientData, fhirContext, true));
      for (var resource : resources) {
         StructureMap structureMap = structureMapMap.get(resource.fhirType());
         if (structureMap != null) {
            var target = fhirContext.getResourceDefinition(
                    structureMapUtilities.getTargetType(structureMap).getType()).newInstance();
            IOUtils.writeResource(transform((Resource) resource, (Resource) target, structureMap),
                    outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
         }
      }
   }

   public Resource transform(Resource mapFrom, Resource mapTo, StructureMap structureMap) {
      if (structureMapUtilities == null) {
         setDefaultStructureMapUtilities();
      }
      structureMapUtilities.transform(null, mapFrom, structureMap, mapTo);
      return mapTo;
   }

   private void populateStructureMapMap(List<StructureMap> structureMaps) {
      for (var structureMap : structureMaps) {
         for (var group : structureMap.getGroup()) {
            for (var input : group.getInput()) {
               if (input.hasType() && input.hasMode() && input.getMode() == StructureMap.StructureMapInputMode.SOURCE) {
                  structureMapMap.put(input.getType(), structureMap);
               }
            }
         }
      }
   }

   private List<StructureMap> getStructureMaps() {
      return BundleUtil.toListOfResourcesOfType(fhirContext,
              IOUtils.bundleResourcesInDirectory(pathToStructureMaps, fhirContext, true), StructureMap.class);
   }

   public void setDefaultStructureMapUtilities() {
      structureMapUtilities = new StructureMapUtilities(NpmUtils.getR4WorkerContext(packageUrl));
   }

   public StructureMapUtilities getStructureMapUtilities() {
      return structureMapUtilities;
   }

   public void setStructureMapUtilities(StructureMapUtilities structureMapUtilities) {
      this.structureMapUtilities = structureMapUtilities;
   }

   public String getPathToPatientData() {
      return pathToPatientData;
   }

   public void setPathToPatientData(String pathToPatientData) {
      this.pathToPatientData = pathToPatientData;
   }

   public String getPathToStructureMaps() {
      return pathToStructureMaps;
   }

   public void setPathToStructureMaps(String pathToStructureMaps) {
      this.pathToStructureMaps = pathToStructureMaps;
   }

   public String getPackageUrl() {
      return packageUrl;
   }

   public void setPackageUrl(String packageUrl) {
      this.packageUrl = packageUrl;
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
}
