package org.opencds.cqf.tooling.operations.codesystem.loinc;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.ValueSet;
import org.opencds.cqf.tooling.constants.Api;
import org.opencds.cqf.tooling.constants.Terminology;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.operations.valueset.generate.config.ConfigValueSetGenerator;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.HttpClientUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.opencds.cqf.tooling.operations.valueset.generate.config.Config.ValueSets;

@Operation(name = "LoincHierarchy")
public class HierarchyProcessor implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(HierarchyProcessor.class);

   @OperationParam(alias = { "query", "q" }, setter = "setQuery", required = true,
           description = "The expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g. instructions that could only be followed by a human.")
   private String query;
   @OperationParam(alias = { "id" }, setter = "setId", required = true,
           description = "The id for the generated FHIR ValueSet resource.")
   private String id;
   @OperationParam(alias = { "narrative", "n" }, setter = "setId",
           description = "The narrative to populate the rulesText extension. If not present, the query will be used instead.")
   private String narrative;
   @OperationParam(alias = { "username", "user" }, setter = "setUsername", defaultValue = "cschuler",
           description = "The LOINC account username.")
   private String username;
   @OperationParam(alias = { "password", "pass" }, setter = "setPassword", defaultValue = "knight",
           description = "The LOINC account password.")
   private String password;
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

   private final List<String> validLoincVersions = Arrays.asList("2.69", "2.70", "2.71", "2.72", "2.73", "2.74", "2.75", "2.76");
   // TODO: need a way to retrieve the latest LOINC version programmatically
   private String loincVersion = "2.76";

   private String loincHierarchyUrl = Api.LOINC_HIERARCHY_QUERY_URL;

   private FhirContext fhirContext;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      IOUtils.writeResource(getValueSet(), outputPath, IOUtils.Encoding.parse(encoding), fhirContext);
   }

   public IBaseResource getValueSet() {
      logger.info("Generating FHIR {} ValueSet resource with id {} for query: {}", version, id, query);
      ValueSet valueSet = processValueSet();
      logger.info("Successfully generated ValueSet resource at location {}", outputPath);
      return ResourceAndTypeConverter.convertFromR5Resource(fhirContext, valueSet);
   }

   public IBaseResource getValueSet(ValueSets config) {
      this.id = config.getId();
      if (config.getHierarchy() != null) {
         this.query = config.getHierarchy().getQuery();
      } else {
         String message = "The Hierarchy element of the configuration must be present";
         logger.error(message);
         throw new RuntimeException(message);
      }
      logger.info("Generating FHIR {} ValueSet resource with id {} for query: {}", version, id, query);
      ValueSet computableVS = processValueSet();
      ConfigValueSetGenerator generator = new ConfigValueSetGenerator();
      ValueSet publishableVS = generator.updateValueSet(computableVS, null, config, null);
      publishableVS.getMeta().addProfile(Terminology.CPG_PUBLISHABLE_VS_PROFILE_URL);
      logger.info("Successfully generated ValueSet resource at location {}", outputPath);
      return ResourceAndTypeConverter.convertFromR5Resource(fhirContext, publishableVS);
   }

   private ValueSet processValueSet() {
      ValueSet valueSet = new ValueSet().setStatus(Enumerations.PublicationStatus.DRAFT);
      valueSet.setId(id);
      valueSet.getMeta().addProfile(Terminology.CPG_COMPUTABLE_VS_PROFILE_URL);
      valueSet.getMeta().addProfile(Terminology.CPG_EXECUTABLE_VS_PROFILE_URL);
      valueSet.addExtension(Terminology.RULES_TEXT_EXT_URL, new StringType(narrative == null ? defaultQuery() : narrative));
      valueSet.getExpansion().setTimestamp(new Date());

      HttpGet request = new HttpGet(loincHierarchyUrl + URLEncoder.encode(query, Charset.defaultCharset()));
      final String auth = username + ":" + password;
      final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
      final String authHeader = "Basic " + new String(encodedAuth);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      try (CloseableHttpClient queryClient = HttpClients.createDefault()) {
         String response = queryClient.execute(request, HttpClientUtils.getDefaultResponseHandler());
         JsonArray arr = new Gson().fromJson(response, JsonArray.class);
         for (var obj : arr) {
            if (obj.isJsonObject()) {
               var jsonObj = obj.getAsJsonObject();
               if (jsonObj.has("IsLoinc") && jsonObj.getAsJsonPrimitive("IsLoinc").getAsBoolean()) {
                  String code = jsonObj.getAsJsonPrimitive("Code").getAsString();
                  String display = jsonObj.getAsJsonPrimitive("CodeText").getAsString();
                  valueSet.getExpansion().addContains().setVersion(loincVersion)
                          .setSystem(Terminology.LOINC_SYSTEM_URL).setCode(code).setDisplay(display);
               }
            }
         }
      } catch (IOException ioe) {
         String message = "Error accessing API: " + ioe.getMessage();
         logger.error(message);
         throw new RuntimeException(message);
      }

      valueSet.getExpansion().setTotal(valueSet.getExpansion().getContains().size());
      return valueSet;
   }

   private String defaultQuery() {
      return String.format("Step 1: go to https://loinc.org/tree/%nStep 2: use query %s%nStep 3: export the results to CSV%nStep 4: Filter results by properties defined in query", query);
   }

   private IGenericClient loincFhirClient;
   private IGenericClient getLoincFhirClient() {
      if (loincFhirClient == null) {
         loincFhirClient = fhirContext.newRestfulGenericClient(Api.LOINC_FHIR_SERVER_URL);
         loincFhirClient.getInterceptorService().unregisterAllInterceptors();
         loincFhirClient.registerInterceptor(new BasicAuthInterceptor(username, password));
      }
      return loincFhirClient;
   }

   public String getQuery() {
      return query;
   }

   public void setQuery(String query) {
      this.query = query;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getNarrative() {
      return narrative;
   }

   public void setNarrative(String narrative) {
      this.narrative = narrative;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
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

   public void setLoincHierarchyUrl(String loincHierarchyUrl) {
      this.loincHierarchyUrl = loincHierarchyUrl;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }

   public void setLoincVersion(String loincVersion) {
      if (validLoincVersions.contains(loincVersion)) {
         this.loincVersion = loincVersion;
      } else {
         logger.warn("Provided LOINC version {} is not supported. Valid versions include: {}", loincVersion, validLoincVersions);
      }
   }
}
