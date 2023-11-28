package org.opencds.cqf.tooling.operations.codesystem.rxnorm;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.ValueSet;
import org.opencds.cqf.tooling.constants.Api;
import org.opencds.cqf.tooling.constants.Terminology;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Operation(name = "RxMixWorkflow")
public class RxMixWorkflowProcessor implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(RxMixWorkflowProcessor.class);

   @OperationParam(alias = { "rulestext", "rt" }, setter = "setRulesText", required = true,
           description = "An expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g. instructions that could only be followed by a human.")
   private String rulesText;
   @OperationParam(alias = { "workflow", "wf" }, setter = "setWorkflow", required = true,
           description = "The workflow library expressed in XML that identifies that API functions needed to produce the desired output.")
   private String workflow;
   @OperationParam(alias = { "input", "in" }, setter = "setInput", required = true,
           description = "The input values needed to run the workflow as a comma-delimited list of strings if multiple inputs are needed.")
   private String input;
   private List<String> inputs;
   @OperationParam(alias = { "includefilter", "if" }, setter = "setIncludeFilter",
           description = "The filter(s) that must be present within the RXCUI names for inclusion in the final result. Provide a comma-delimited list of strings for multiple filters.")
   private String includeFilter;
   private List<String> includeFilters;
   @OperationParam(alias = { "excludefilter", "ef" }, setter = "setExcludeFilter",
           description = "The filter(s) that must not be present within the RXCUI names for inclusion in the final result. Provide a comma-delimited list of strings for multiple filters.")
   private String excludeFilter;
   private List<String> excludeFilters;
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

   private final LocalDate date = LocalDate.now();
   private String rxNormVersion = String.format("%s-%s", date.getYear(), date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue());

   private FhirContext fhirContext;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      IOUtils.writeResource(getValueSet(), outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
   }

   public IBaseResource getValueSet() {
      List<HttpPost> requests = new ArrayList<>();
      for (String inputValue : getInputs()) {
         HttpPost request = new HttpPost(Api.RXMIX_WORKFLOW_URL);
         request.setEntity(resolveForm(workflow, inputValue));
         requests.add(request);
      }
      try (CloseableHttpClient client = HttpClients.createDefault()) {
         ValueSet vs = new ValueSet().setStatus(Enumerations.PublicationStatus.DRAFT);
         vs.addExtension(Terminology.RULES_TEXT_EXT_URL, new StringType(rulesText));
         for (HttpPost request : requests) {
            String response = client.execute(request, HttpClientUtils.getDefaultResponseHandler());
            populateValueSet(response, vs);
         }
         return ResourceAndTypeConverter.convertFromR5Resource(fhirContext, vs);
      } catch (IOException ioe) {
         String message = "Error accessing RxMix API: " + ioe.getMessage();
         logger.error(message);
         throw new RuntimeException(message);
      }
   }

   private void populateValueSet(String rawResponse, ValueSet vs) {
      if (!vs.getCompose().hasInclude()) {
         vs.getCompose().addInclude().setSystem(Terminology.RXNORM_SYSTEM_URL).setVersion(rxNormVersion);
      }
      String[] names = rawResponse.split("\\|name\\|");
      for (String s : names) {
         if (!s.startsWith("{\"")) {
            String[] rxcuis = s.split("\\|RXCUI\\|");
            String description = rxcuis[0];
            if (!containsExcludeFilter(description) && containsIncludeFilter(description)) {
               String code = rxcuis[1].split("\\D")[0];
               vs.getCompose().getIncludeFirstRep().addConcept().setCode(code).setDisplay(description);
            }
         }
      }
   }

   private boolean containsExcludeFilter(String s) {
      if (getExcludeFilters() != null) {
         for (String exclude : excludeFilters) {
            if (StringUtils.containsIgnoreCase(s, exclude)) return true;
         }
      }
      return false;
   }

   private boolean containsIncludeFilter(String s) {
      if (getIncludeFilters() != null) {
         for (String include : includeFilters) {
            if (StringUtils.containsIgnoreCase(s, include)) return true;
         }
         return false;
      }
      return true;
   }

   private UrlEncodedFormEntity resolveForm(String xml, String input) {
      List<NameValuePair> formparams = new ArrayList<>();
      formparams.add(new BasicNameValuePair("xmlConfig", xml));
      formparams.add(new BasicNameValuePair("inputs", input));
      formparams.add(new BasicNameValuePair("outFormat", "txt"));
      return new UrlEncodedFormEntity(formparams, Consts.UTF_8);
   }

   public void setRxNormVersion(String rxNormVersion) {
      this.rxNormVersion = rxNormVersion;
   }

   public String getRulesText() {
      return rulesText;
   }

   public String getWorkflow() {
      return workflow;
   }

   public void setWorkflow(String workflow) {
      this.workflow = workflow;
   }

   public List<String> getInputs() {
      if (inputs == null) {
         inputs = Arrays.stream(input.split(",")).map(String::trim).collect(Collectors.toList());
      }
      return inputs;
   }

   public void setInputs(List<String> inputs) {
      this.inputs = inputs;
   }

   public void setInput(String input) {
      this.input = input;
   }

   public List<String> getIncludeFilters() {
      if (includeFilters == null && includeFilter != null) {
         includeFilters = Arrays.stream(includeFilter.split(",")).map(String::trim).collect(Collectors.toList());
      }
      return includeFilters;
   }

   public void setIncludeFilters(List<String> includeFilters) {
      this.includeFilters = includeFilters;
   }

   public void setIncludeFilter(String includeFilter) {
      this.includeFilter = includeFilter;
   }

   public List<String> getExcludeFilters() {
      if (excludeFilters == null && excludeFilter != null) {
         excludeFilters = Arrays.stream(excludeFilter.split(",")).map(String::trim).collect(Collectors.toList());
      }
      return excludeFilters;
   }

   public void setExcludeFilters(List<String> excludeFilters) {
      this.excludeFilters = excludeFilters;
   }

   public void setExcludeFilter(String excludeFilter) {
      this.excludeFilter = excludeFilter;
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

   public FhirContext getFhirContext() {
      return fhirContext;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }

   public void setRulesText(String rulesText) {
      this.rulesText = rulesText;
   }
}
