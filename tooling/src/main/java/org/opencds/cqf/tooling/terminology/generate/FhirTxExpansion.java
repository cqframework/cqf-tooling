package org.opencds.cqf.tooling.terminology.generate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FhirTxExpansion extends Operation {
   private final Logger logger = LoggerFactory.getLogger(FhirTxExpansion.class);
   private String pathToConfig; // -ptc
   private final FhirContext fhirContext = FhirContext.forR4Cached();
   private IGenericClient fhirServer = fhirContext.newRestfulGenericClient("http://tx.fhir.org/r4"); // -fs
   private final IGenericClient vsacFhirClient = fhirContext.newRestfulGenericClient("https://cts.nlm.nih.gov/fhir");
   private Config config;
   private CommonMetaData cmd;

   public FhirTxExpansion() {  }

   public FhirTxExpansion(String outputPath, Config config, CommonMetaData cmd) {
      setOutputPath(outputPath);
      this.cmd = cmd;
      this.config = config;
   }

   @Override
   public void execute(String[] args) {
      String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output"; // -op
      setOutputPath(outputPath);
      for (String arg : args) {
         if ("-FhirTxExpansion".equals(arg)) continue;
         String[] flagAndValue = arg.split("=");
         if (flagAndValue.length < 2) {
            throw new IllegalArgumentException("Invalid argument: " + arg);
         }
         String flag = flagAndValue[0];
         String value = flagAndValue[1];

         switch (flag.replace("-", "").toLowerCase()) {
            case "outputpath": case "op": setOutputPath(value); break;
            case "pathtoconfig": case "ptc": pathToConfig = value; break;
            default: throw new IllegalArgumentException("Unknown flag: " + flag);
         }
      }

      if (pathToConfig == null) {
         throw new IllegalArgumentException("The path to the configuration file is required");
      }

      ObjectMapper mapper = new ObjectMapper();
      try {
         config = mapper.readValue(new File(pathToConfig), Config.class);
      } catch (IOException e) {
         throw new IllegalArgumentException(e.getMessage());
      }

      ImplementationGuide ig = Helper.resolveIgResource(config.getPathToIgResource(), fhirContext);
      cmd = new CommonMetaData(ig);

      for (var valueSet : config.getValueSets()) {
         if (valueSet.getExpand() != null) {
            resolve(valueSet);
         }
      }
   }

   public void resolve(Config.ValueSets valueSet) {
      if (valueSet.getExpand() != null) {
         if (valueSet.getExpand().getPathToValueSet() != null) {
            ValueSet expansionResult = resolveFhirServerValueSet(valueSet.getExpand());
            if (expansionResult != null) {
               Helper.updateValueSet(expansionResult, config, valueSet, cmd, false);
               IOUtils.writeResource(expansionResult, getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
            }
         }
         else if (valueSet.getExpand().getVsacId() != null) {
            ValueSet expansionResult = resolveVsacValueSet(valueSet.getExpand());
            if (expansionResult != null) {
               Helper.updateValueSet(expansionResult, config, valueSet, cmd, true);
               IOUtils.writeResource(expansionResult, getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
            }
         }
         else {
            String message = "Either pathToValueSet or vsacId must be present in config";
            logger.error(message);
            throw new RuntimeException(message);
         }
      }
   }

   public ValueSet resolveVsacValueSet(Config.ValueSets.Expand expand) {
      if (expand.getApikey() == null) {
         String message = "A valid VSAC apikey must be provided to access the VSAC API";
         logger.error(message);
         throw new RuntimeException(message);
      }
      vsacFhirClient.registerInterceptor(
              new BasicAuthInterceptor("apikey", expand.getApikey()));
      ValueSet vs = vsacFhirClient.read().resource(ValueSet.class).withId(expand.vsacId).execute();
      try {
         ValueSet expansionResult = vsacFhirClient.operation().onInstance(vs.getIdElement())
                 .named("$expand").withNoParameters(Parameters.class)
                 .returnResourceType(ValueSet.class).execute();
         vs.setExpansion(expansionResult.getExpansion());
      } catch (Exception e) {
         logger.warn("Unable to expand: {}", vs.getId());
         System.out.println("Unable to expand: " + vs.getId());
         vs = null;
      }
      return vs;
   }

   public ValueSet resolveFhirServerValueSet(Config.ValueSets.Expand expand) {
      if (expand.getTxServer() != null) {
         fhirServer = fhirContext.newRestfulGenericClient(expand.getTxServer().getBaseUrl());
         if (expand.getTxServer().getAuth() != null) {
            fhirServer.getInterceptorService().unregisterAllInterceptors();
            fhirServer.registerInterceptor(new BasicAuthInterceptor(
                    expand.getTxServer().getAuth().getUser(),
                    expand.getTxServer().getAuth().getPassword()));
         }
      }
      ValueSet valueSetToExpand = (ValueSet) IOUtils.readResource(expand.getPathToValueSet(), fhirContext);
      Parameters params = new Parameters().addParameter(new Parameters.ParametersParameterComponent().setName("valueSet").setResource(valueSetToExpand));
      ValueSet expansionResult = null;
      try {
         expansionResult = fhirServer.operation().onType(ValueSet.class)
                 .named("$expand").withParameters(params).returnResourceType(ValueSet.class).execute();
         if (expand.isValidateFSN()) {
            if (expand.getApikey() == null) {
               String message = "A valid VSAC apikey must be provided to access the VSAC API";
               logger.error(message);
               throw new RuntimeException(message);
            }
            vsacFhirClient.registerInterceptor(
                    new BasicAuthInterceptor("apikey", expand.getApikey()));
            resolveFullySpecifiedName(expansionResult);
         }
      } catch (Exception e) {
         logger.warn("Unable to expand: {}", valueSetToExpand.getId());
         System.out.println("Unable to expand: " + valueSetToExpand.getId());
      }
      return expansionResult == null ? null : valueSetToExpand.setExpansion(expansionResult.getExpansion());
   }

   // Resolves Snomed fully specified names (tx.fhir.org often uses the synonym instead of the preferred fsn)
   public void resolveFullySpecifiedName(ValueSet expandedVs) {
      for (var expansion : expandedVs.getExpansion().getContains()) {
         if (expansion.getSystem().startsWith("http://snomed.info")) {
            Parameters params = new Parameters().addParameter("system", new UriType(expansion.getSystem())).addParameter("code", expansion.getCode());
            try {
               Parameters result = vsacFhirClient.operation().onType(CodeSystem.class).named("$lookup").withParameters(params).returnResourceType(Parameters.class).execute();
               if (result.hasParameter("display")) {
                  expansion.setDisplay(result.getParameter("display").primitiveValue());
               }
            } catch (Exception e) {
               logger.warn("Unable to resolve FSN for: {}", expansion.getCode());
               System.out.println("\"Unable to resolve FSN for: " + expansion.getCode());
            }
         }
      }
   }
}
