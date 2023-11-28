package org.opencds.cqf.tooling.operations.valueset.expansion;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.ParametersUtil;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

@Operation(name = "FhirTxExpansion")
public class FhirTxExpansion implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(FhirTxExpansion.class);

   @OperationParam(alias = { "pathtovalueset", "ptvs" }, setter = "setPathToValueSet", required = true,
           description = "The path to the FHIR ValueSet resource(s) to be expanded (this may be a file or directory)")
   private String pathToValueSet;
   @OperationParam(alias = { "fhirserver", "fs" }, setter = "setFhirServer", defaultValue = "http://tx.fhir.org/r4",
           description = "The FHIR server url that performs the $expand operation")
   private String fhirServer;
   // TODO: enable basic authorization with username and password params
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR ValueSet { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           defaultValue = "src/main/resources/org/opencds/cqf/tooling/terminology/output",
           description = "The directory path to which the generated FHIR ValueSet resource should be written (default src/main/resources/org/opencds/cqf/tooling/terminology/output)")
   private String outputPath;

   private FhirContext fhirContext;
   private IGenericClient fhirServerClient;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      fhirServerClient = fhirContext.newRestfulGenericClient(fhirServer);

      if (Files.isDirectory(Paths.get(pathToValueSet))) {
         for (IBaseResource resource : IOUtils.readResources(IOUtils.getFilePaths(pathToValueSet, true), fhirContext)) {
            expandAndWriteValueSet(resource);
         }
      } else {
         expandAndWriteValueSet(IOUtils.readResource(pathToValueSet, fhirContext));
      }
   }

   public IBaseResource expandValueSet(IBaseResource valueSet) {
      try {
         IBaseParameters parameters = ParametersUtil.newInstance(fhirContext);
         ParametersUtil.addParameterToParameters(fhirContext, parameters, "valueSet", valueSet);
         return fhirServerClient.operation().onType("ValueSet")
                 .named("$expand").withParameters(parameters).execute();
      } catch (Exception e) {
         logger.warn("Unable to expand: {}", valueSet.getIdElement().getValue(), e);
      }
      return null;
   }

   private void expandAndWriteValueSet(IBaseResource resource) {
      if (resource.fhirType().equalsIgnoreCase("valueset")) {
         IBaseResource expandedVs = expandValueSet(resource);
         if (expandedVs != null) {
            IOUtils.writeResource(expandedVs, outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
         }
      }
   }

   public String getPathToValueSet() {
      return pathToValueSet;
   }

   public void setPathToValueSet(String pathToValueSet) {
      this.pathToValueSet = pathToValueSet;
   }

   public String getFhirServer() {
      return fhirServer;
   }

   public void setFhirServer(String fhirServer) {
      this.fhirServer = fhirServer;
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
