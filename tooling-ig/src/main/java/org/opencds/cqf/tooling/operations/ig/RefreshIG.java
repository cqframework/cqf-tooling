package org.opencds.cqf.tooling.operations.ig;

import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;

@Operation(name = "RefreshIG")
public class RefreshIG implements ExecutableOperation {

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

   @Override
   public void execute() {
      // refresh libraries
      // package (Bundle or list of resources)
      // refresh measures
      // package (Bundle or list of resources)
      // refresh plandefinitions
      // package (Bundle or list of resources) - also includes
      // publish
   }
}
