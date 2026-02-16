package org.opencds.cqf.tooling.operations.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.ExtensionUtil;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.TerserUtil;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.constants.Validation;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.NpmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Operation(name = "ProfileConformance")
public class DataProfileConformance implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(DataProfileConformance.class);

   @OperationParam(alias = { "ptpd", "pathtopatientdata" }, setter = "setPathToPatientData", required = true,
           description = "Path to the patient data represented as either a FHIR Bundle resource or as flat files within a directory (required).")
   private String pathToPatientData;
   @OperationParam(alias = { "purls", "packageurls" }, setter = "setPackageUrls", required = true,
           description = "Urls for the FHIR packages to use for validation as a comma-separated list (required).")
   private String packageUrls;
   private List<String> packageUrlsList;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR OperationOutcome { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           description = "The directory path to which the FHIR OperationOutcome should be written (default is to replace existing resources within the IG)")
   private String outputPath;

   private FhirContext fhirContext;
   private FhirValidator validator;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      setGeneralValidator();
      IBaseBundle patientDataBundle;
      if (IOUtils.isDirectory(pathToPatientData)) {
         patientDataBundle = IOUtils.bundleResourcesInDirectory(pathToPatientData, fhirContext, true);
      } else {
         IBaseResource bundle = IOUtils.readResource(pathToPatientData, fhirContext);
         if (bundle instanceof IBaseBundle) {
            patientDataBundle = (IBaseBundle) bundle;
         } else {
            String invalidType = bundle.fhirType();
            logger.error("Expected a bundle resource at path {}, found {}", pathToPatientData, invalidType);
            return;
         }
      }

      IOUtils.writeResources(validatePatientData(patientDataBundle), outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
   }

   public List<IBaseResource> validatePatientData(IBaseBundle patientData) {
      List<IBaseResource> validatedResources = new ArrayList<>();

      for (var patientDataResource : BundleUtil.toListOfResources(fhirContext, patientData)) {
         ValidationOptions options = new ValidationOptions();
         String resourceType = patientDataResource.fhirType();
         if (profileMap.containsKey(resourceType)) {
            profileMap.get(patientDataResource.fhirType()).forEach(options::addProfile);
         }
         ValidationResult result = validator.validateWithResult(patientDataResource, options);
         if (!result.isSuccessful()) {
            logger.warn("Validation errors found for {}/{} : {}", resourceType,
                    patientDataResource.getIdElement().getIdPart(), result.getMessages());
            tagResourceWithValidationResult(patientDataResource, result);
         } else {
            logger.info("Validation successful for {}/{}", resourceType, patientDataResource.getIdElement().getIdPart());
         }
         validatedResources.add(patientDataResource);
      }

      return validatedResources;
   }
   private void tagResourceWithValidationResult(IBaseResource resource, ValidationResult result) {
      String id = UUID.randomUUID().toString();

      // create validation-result extension
      ExtensionUtil.addExtension(fhirContext, resource, Validation.VALIDATION_RESULT_EXTENSION_URL, "Reference", "#" + id);

      // add outcome (validation messages) to the resource
      IBaseOperationOutcome outcome = result.toOperationOutcome();
      outcome.setId(id);
      TerserUtil.setField(fhirContext, "contained", resource, outcome);
   }

   public void setGeneralValidator() {
      NpmUtils.PackageLoaderValidationSupport validationSupport =
              NpmUtils.getNpmPackageLoaderValidationSupport(fhirContext, getPackageUrlsList());

      populateProfileMap(validationSupport.fetchAllNonBaseStructureDefinitions());

      ValidationSupportChain supportChain = new ValidationSupportChain(validationSupport,
              new CommonCodeSystemsTerminologyService(fhirContext),
              new DefaultProfileValidationSupport(fhirContext),
              new InMemoryTerminologyServerValidationSupport(fhirContext),
              new SnapshotGeneratingValidationSupport(fhirContext));

      IValidationSupport cachingValidationSupport = new ValidationSupportChain(supportChain);
      validator = fhirContext.newValidator();
      validator.setValidateAgainstStandardSchema(false);
      validator.setValidateAgainstStandardSchematron(false);
      FhirInstanceValidator instanceValidator = new FhirInstanceValidator(cachingValidationSupport);
      validator.registerValidatorModule(instanceValidator);
   }

   private final Map<String, List<String>> profileMap = new HashMap<>();
   private void populateProfileMap(List<IBaseResource> structureDefinitions) {
      if (structureDefinitions != null) {
         FhirTerser terser = new FhirTerser(fhirContext);
         for (var structureDefinition : structureDefinitions) {
            String type = terser.getSinglePrimitiveValueOrNull(structureDefinition, "type");
            String url = terser.getSinglePrimitiveValueOrNull(structureDefinition, "url");
            if (type != null && url != null) {
               profileMap.putIfAbsent(type, new ArrayList<>());
               if (!profileMap.get(type).contains(url)) {
                  profileMap.get(type).add(url);
               }
            }
         }
      }
   }

   public String getPathToPatientData() {
      return pathToPatientData;
   }

   public void setPathToPatientData(String pathToPatientData) {
      this.pathToPatientData = pathToPatientData;
   }

   public void setPackageUrls(String packageUrls) {
      this.packageUrls = packageUrls;
   }

   public List<String> getPackageUrlsList() {
      if (packageUrlsList == null && packageUrls != null) {
         packageUrlsList = Arrays.stream(packageUrls.split(",")).map(String::trim).collect(Collectors.toList());
      }
      return packageUrlsList;
   }

   public void setPackageUrlsList(List<String> packageUrlsList) {
      this.packageUrlsList = packageUrlsList;
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

   public void setValidator(FhirValidator validator) {
      this.validator = validator;
   }
}
