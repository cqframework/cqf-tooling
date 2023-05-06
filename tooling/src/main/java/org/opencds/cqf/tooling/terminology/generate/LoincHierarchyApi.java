package org.opencds.cqf.tooling.terminology.generate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.util.ParametersUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoincHierarchyApi extends Operation {

   private final Logger logger = LoggerFactory.getLogger(LoincHierarchyApi.class);
   private String outputPath = "src/main/resources/org/opencds/cqf/tooling/terminology/output"; // -op
   private String pathToConfig; // -ptc
   private final FhirContext fhirContext = FhirContext.forR4Cached();
   private final String queryUrl = "https://loinc.regenstrief.org/searchapi/hierarchy/component-system/search?searchString=";
   private String version = "2.74";
   private final IGenericClient client = fhirContext.newRestfulGenericClient("https://fhir.loinc.org");
   private Config config;
   private CommonMetaData cmd;

   public LoincHierarchyApi() {  }

   public LoincHierarchyApi(String outputPath, Config config, CommonMetaData cmd) {
      setOutputPath(outputPath);
      this.cmd = cmd;
      this.config = config;
   }

   @Override
   public void execute(String[] args) {
      setOutputPath(outputPath);
      for (String arg : args) {
         if ("-LoincHierarchyApi".equals(arg)) continue;
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
      for (var codesystem : config.getCodeSystems()) {
         if (codesystem.getUrl().equals(Constants.LOINC_SYSTEM_URL)) {
            version = codesystem.getVersion();
         }
      }

      for (var valueSet : config.getValueSets()) {
         if (valueSet.getHierarchy() != null) {
            resolve(valueSet);
         }
      }
   }

   public void resolve(Config.ValueSets valueSet) {
      if (valueSet.getHierarchy() != null) {
         if (valueSet.getHierarchy().getAuth() == null) {
            String message = "A valid user and password must be provided to access the LOINC FHIR API";
            logger.error(message);
            throw new RuntimeException(message);
         }
         client.getInterceptorService().unregisterAllInterceptors();
         client.registerInterceptor(new BasicAuthInterceptor(
                 valueSet.getHierarchy().getAuth().getUser(), valueSet.getHierarchy().getAuth().getPassword()));
         if (valueSet.getHierarchy().getParents() != null && !valueSet.getHierarchy().getParents().isEmpty()) {
            IOUtils.writeResource(getValueSet(valueSet, resolveParents(valueSet)), getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
         }
         else if (valueSet.getHierarchy().getQuery() != null) {
            IOUtils.writeResource(getValueSet(valueSet, resolveQuery(valueSet)), getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
         }
         else {
            String message = "Either parents or query must be present in config";
            logger.error(message);
            throw new RuntimeException(message);
         }
      }

   }

   public List<Coding> resolveParents(Config.ValueSets cvs) {
      List<Coding> codes = new ArrayList<>();
      List<String> visited = new ArrayList<>();
      for (var parent : cvs.getHierarchy().getParents()) {
         resolveChildren(parent, cvs.getHierarchy().getProperty(), visited, codes);
      }
      return codes;
   }

   public List<Coding> resolveQuery(Config.ValueSets cvs) {
      List<Coding> codes = new ArrayList<>();
      HttpGet request = new HttpGet(queryUrl + cvs.getHierarchy().getQuery());
      final String auth = cvs.getHierarchy().getAuth().getUser() + ":" + cvs.getHierarchy().getAuth().getPassword();
      final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
      final String authHeader = "Basic " + new String(encodedAuth);
      request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
      try (CloseableHttpClient queryClient = HttpClients.createDefault()) {
         String response = queryClient.execute(request, Helper.getDefaultResponseHandler());
         JsonArray arr = new Gson().fromJson(response, JsonArray.class);
         for (var obj : arr) {
            if (obj.isJsonObject()) {
               var jsonObj = obj.getAsJsonObject();
               if (jsonObj.has("Code") && !jsonObj.getAsJsonPrimitive("Code").getAsString().startsWith("LP")) {
                  String code = jsonObj.getAsJsonPrimitive("Code").getAsString();
                  Parameters lookupResponse = lookupCode(code);
                  codes.add(new Coding().setCode(code).setSystem(Constants.LOINC_SYSTEM_URL)
                          .setDisplay(lookupResponse == null ? jsonObj.getAsJsonPrimitive("CodeText").getAsString() : lookupResponse.getParameter("display").primitiveValue())
                          .setVersion(version));
               }
            }
         }
      } catch (IOException ioe) {
         String message = "Error accessing API: " + ioe.getMessage();
         logger.error(message);
         throw new RuntimeException(message);
      }
      return codes;
   }

   private void resolveChildren(String parent, List<Config.ValueSets.Hierarchy.Property> properties, List<String> visited, List<Coding> codes) {
      if (visited.contains(parent)) {
         return;
      }
      else {
         visited.add(parent);
      }
      Parameters response = lookupCode(parent);
      boolean hasChild = false;
      if (response != null) {
         for (var paramComponent : response.getParameter()) {
            if (isChild(paramComponent)) {
               hasChild = true;
               resolveChildren(extractChildCode(paramComponent), properties, visited, codes);
            }
         }
      }
      if (!hasChild && hasProperties(response, properties)) {
         codes.add(new Coding().setCode(parent).setDisplay(response == null ? null : response.getParameter("display").primitiveValue()).setVersion(version).setSystem(Constants.LOINC_SYSTEM_URL));
      }
   }

   public Parameters lookupCode(String code) {
      Parameters parameters = new Parameters().addParameter("system", new UriType(Constants.LOINC_SYSTEM_URL));
      parameters.addParameter("code", code);
      if (version != null) {
         parameters.addParameter("version", version);
      }
      try {
         return client.operation().onType(CodeSystem.class).named("$lookup")
                 .withParameters(parameters).useHttpGet().returnResourceType(Parameters.class).execute();
      } catch (Exception e) {
         logger.warn("Error encountered looking up code {}: {}", code, e.getMessage());
         return null;
      }
   }

   public boolean hasProperties(Parameters response, List<Config.ValueSets.Hierarchy.Property> properties) {
      for (Config.ValueSets.Hierarchy.Property property : properties) {
         boolean hasMatch = false;
         for (var paramComponent : response.getParameter()) {
            String propertyName = ParametersUtil.getParameterPartValueAsString(fhirContext, paramComponent, "code");
            if (propertyName != null && propertyName.equals(property.getName())
                    && getPropertyValue(paramComponent).equals(property.getValue())) {
               hasMatch = true;
            }
         }
         if (!hasMatch) {
            return false;
         }
      }
      return true;
   }

   public String getPropertyValue(Parameters.ParametersParameterComponent paramComponent) {
      Optional<IBase> possibleValue = ParametersUtil.getParameterPartValue(fhirContext, paramComponent, "value");
      if (possibleValue.isPresent()) {
         IBase value = possibleValue.get();
         if (value instanceof PrimitiveType) {
            return ((PrimitiveType<?>) value).primitiveValue();
         }
         else if (value instanceof Coding) {
            return ((Coding) value).getDisplay();
         }
      }
      return null;
   }

   public String extractChildCode(Parameters.ParametersParameterComponent paramComponent) {
      Coding childCoding = (Coding) ParametersUtil.getParameterPartValue(fhirContext, paramComponent, "value").orElse(null);
      if (childCoding != null) {
         return childCoding.getCode();
      }
      return null;
   }

   public boolean isChild(Parameters.ParametersParameterComponent paramComponent) {
      String value = ParametersUtil.getParameterPartValueAsString(fhirContext, paramComponent, "code");
      return value != null && value.equals("child");
   }

   public ValueSet getValueSet(Config.ValueSets cvs, List<Coding> codes) {
      ValueSet vs = Helper.boilerPlateValueSet(config, cvs, cmd);
      vs.addExtension(Constants.RULES_TEXT_EXT_URL, new MarkdownType(cvs.getHierarchy().getNarrative()));

      ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
      expansion.setIdentifier(UUID.randomUUID().toString());
      expansion.setTimestamp(cmd.getDate());
      expansion.setTotal(codes.size());
      for (Coding code : codes) {
         expansion.addContains(new ValueSet.ValueSetExpansionContainsComponent()
                 .setCode(code.getCode()).setSystem(Constants.LOINC_SYSTEM_URL)
                 .setDisplay(code.getDisplay()).setVersion(version));
      }
      vs.setExpansion(expansion);
      return vs;
   }
}
