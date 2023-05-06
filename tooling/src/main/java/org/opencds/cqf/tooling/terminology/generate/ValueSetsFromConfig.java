package org.opencds.cqf.tooling.terminology.generate;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.opencds.cqf.tooling.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ValueSetsFromConfig extends Operation {

   private final Logger logger = LoggerFactory.getLogger(ValueSetsFromConfig.class);
   private String pathToConfig; // -ptc

   private final FhirContext fhirContext = FhirContext.forR4Cached();

   @Override
   public void execute(String[] args) {
      String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output"; // -op
      setOutputPath(outputPath);
      for (String arg : args) {
         if ("-ValueSetsFromConfig".equals(arg)) continue;
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
         logger.error("The path to the configuration file is required");
         throw new IllegalArgumentException("The path to the configuration file is required");
      }

      ObjectMapper mapper = new ObjectMapper();
      Config config;
      try {
         config = mapper.readValue(new File(pathToConfig), Config.class);
      } catch (IOException e) {
         String message = "Error reading configuration: " + e.getMessage();
         logger.error(message);
         throw new IllegalArgumentException(message);
      }

      ImplementationGuide ig = Helper.resolveIgResource(config.pathToIgResource, fhirContext);
      CommonMetaData cmd = new CommonMetaData(ig);

      FhirTxExpansion fhirTxExpansion = new FhirTxExpansion(getOutputPath(), config, cmd);
      RxMixApi rxMixApi = new RxMixApi(getOutputPath(), config, cmd);
      LoincHierarchyApi loincHierarchyApi = new LoincHierarchyApi(getOutputPath(), config, cmd);

      for (var valueSet : config.valueSets) {
         if (valueSet.getHierarchy() != null) {
            loincHierarchyApi.resolve(valueSet);
         }
         else if (valueSet.getRulesText() != null) {
            rxMixApi.resolve(valueSet);
         }
         else if (valueSet.getExpand() != null) {
            fhirTxExpansion.resolve(valueSet);
         }
      }
   }
}
