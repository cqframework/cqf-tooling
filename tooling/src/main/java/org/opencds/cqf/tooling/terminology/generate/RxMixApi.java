package org.opencds.cqf.tooling.terminology.generate;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RxMixApi extends Operation {
   private final Logger logger = LoggerFactory.getLogger(RxMixApi.class);
   private String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output"; // -op
   private String pathToConfig; // -ptc
   private final FhirContext fhirContext = FhirContext.forR4Cached();
   private Config config;
   private CommonMetaData cmd;

   public RxMixApi() { }

   public RxMixApi(String outputPath, Config config, CommonMetaData cmd) {
      setOutputPath(outputPath);
      this.cmd = cmd;
      this.config = config;
   }

   @Override
   public void execute(String[] args) {
      setOutputPath(outputPath);
      for (String arg : args) {
         if ("-RxMixWorkflowApi".equals(arg)) continue;
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
         if (valueSet.getRulesText() != null) {
            resolve(valueSet);
         }
      }
   }

   public void resolve(Config.ValueSets valueSet) {
      HttpPost request = new HttpPost("https://mor.nlm.nih.gov/RxMix/executeConfig.do");
      if (valueSet.getRulesText() != null) {
         List<NameValuePair> formparams = new ArrayList<>();
         formparams.add(new BasicNameValuePair("xmlConfig", valueSet.getRulesText().getWorkflowXml()));
         formparams.add(new BasicNameValuePair("inputs", valueSet.getRulesText().getInput()));
         formparams.add(new BasicNameValuePair("outFormat", "txt"));
         UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
         request.setEntity(entity);
         try (CloseableHttpClient client = HttpClients.createDefault()) {
            String response = client.execute(request, Helper.getDefaultResponseHandler());
            logger.info("Writing {} to {}", valueSet.getId(), getOutputPath());
            IOUtils.writeResource(getValueSet(valueSet, getCodes(response, valueSet.getRulesText())),
                    getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
         } catch (IOException ioe) {
            String message = "Error accessing RxMix API: " + ioe.getMessage();
            logger.error(message);
            throw new RuntimeException(message);
         }
      }

   }

   public ValueSet getValueSet(Config.ValueSets cvs, List<Coding> codes) {
      ValueSet vs = Helper.boilerPlateValueSet(config, cvs, cmd);
      vs.addExtension(Constants.RULES_TEXT_EXT_URL, new MarkdownType(cvs.getRulesText().getNarrative()));

      ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
      expansion.setIdentifier(UUID.randomUUID().toString());
      expansion.setTimestamp(cmd.getDate());
      expansion.setTotal(codes.size());
      String version = null;
      for (Config.CodeSystems cs : config.getCodeSystems()) {
         if (cs.getUrl().equals(Constants.RXNORM_SYSTEM_URL)) {
            version = cs.getVersion();
            expansion.addParameter().setName("version").setValue(new StringType(version));
         }
      }
      for (Coding code : codes) {
         expansion.addContains(new ValueSet.ValueSetExpansionContainsComponent()
                 .setCode(code.getCode()).setSystem(Constants.RXNORM_SYSTEM_URL)
                 .setDisplay(code.getDisplay()).setVersion(version));
      }
      vs.setExpansion(expansion);
      return vs;
   }

   public List<Coding> getCodes(String rawResponse, Config.ValueSets.RulesText rulesText) {
      List<Coding> codes = new ArrayList<>();
      String[] names = rawResponse.split("\\|name\\|");
      for (String s : names) {
         if (!s.startsWith("{")) {
            String[] rxcuis = s.split("\\|RXCUI\\|");
            String description = rxcuis[0];
            if (!containsExcludeFilter(description, rulesText.getExcludeFilter())
                    && containsIncludeFilter(description, rulesText.getIncludeFilter())) {
               String code = rxcuis[1].split("\\D")[0];
               codes.add(new Coding().setSystem(Constants.RXNORM_SYSTEM_URL).setCode(code).setDisplay(description));
            }
         }
      }
      return codes;
   }

   public boolean containsExcludeFilter(String s, List<String> excludeFilter) {
      if (excludeFilter != null) {
         for (String exclude : excludeFilter) {
            if (StringUtils.containsIgnoreCase(s, exclude)) return true;
         }
      }
      return false;
   }

   public boolean containsIncludeFilter(String s, List<String> includeFilter) {
      if (includeFilter != null) {
         for (String include : includeFilter) {
            if (StringUtils.containsIgnoreCase(s, include)) return true;
         }
         return false;
      }
      return true;
   }
}
