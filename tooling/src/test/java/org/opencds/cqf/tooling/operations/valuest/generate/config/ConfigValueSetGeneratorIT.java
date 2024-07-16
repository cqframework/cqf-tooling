package org.opencds.cqf.tooling.operations.valuest.generate.config;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.valueset.generate.config.Config;
import org.opencds.cqf.tooling.operations.valueset.generate.config.ConfigValueSetGenerator;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.List;

@Ignore
public class ConfigValueSetGeneratorIT {

   private final FhirContext fhirContext = FhirContext.forR4Cached();

   @Test
   void testSimpleConfigRulesText() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      Config config = mapper.readValue(SIMPLE_CONFIG, Config.class);
      ConfigValueSetGenerator configValueSetGenerator = new ConfigValueSetGenerator();
      configValueSetGenerator.setFhirContext(fhirContext);
      List<IBaseResource> valueSets = configValueSetGenerator.generateValueSets(config);
      Assert.assertFalse(valueSets.isEmpty());
   }

   private final String SIMPLE_CONFIG = "{\n" +
           "  \"author\": {\n" +
           "    \"name\": \"MD Partners, Inc.\",\n" +
           "    \"contactType\": \"email\",\n" +
           "    \"contactValue\": \"info@mdpartners.com\"\n" +
           "  },\n" +
           "  \"valuesets\": [\n" +
           "    {\n" +
           "      \"id\": \"opioid-analgesics-with-ambulatory-misuse-potential\",\n" +
           "      \"canonical\": \"http://fhir.org/guides/cdc/opioid-cds/ValueSet/opioid-analgesics-with-ambulatory-misuse-potential\",\n" +
           "      \"title\": \"Opioid analgesics with ambulatory misuse potential\",\n" +
           "      \"description\": \"All opioid clinical drugs except cough medications, antispasmodics, or those restricted to surgical use only as identified by those using an injectable form.\",\n" +
           "      \"purpose\": \"Opioid medications that should have opioid management CDS\",\n" +
           "      \"clinicalFocus\": \"All opioid clinical drugs except cough medications, antispasmodics, or those restricted to surgical use only.\",\n" +
           "      \"dataElementScope\": \"Medication\",\n" +
           "      \"inclusionCriteria\": \"All opioid-class medications\",\n" +
           "      \"exclusionCriteria\": \"All medications including ingredients intended to treat cough or act as an antispasmodic. All injectable forms.\",\n" +
           "      \"rulesText\": {\n" +
           "        \"narrative\": \"https:\\/\\/mor.nlm.nih.gov\\/RxMix\\/ Script:\\r\\nStep 1a \\r\\nCreate Batch text input file (SCT-Opioids.txt) with following SCT identifier (for the concept \\\"Product containing opioid receptor agonist (product)\\\") as an input within the file: \\r\\n360204007       \\r\\n\\r\\nStep 1b\\r\\nSubmit batch job using the above SCT-Opioids.txt file to following workflow by uploading file (SCT-Opioid-wf.config) with the following in the file:  <WFE><filteredOutputs>RXCUI|name|term_type<\\/filteredOutputs><input>NOINPUT<\\/input><FS><service>NOINPUT<\\/service><function>findClassById<\\/function><level>0<\\/level><paramSize>1<\\/paramSize><param order ='0'>?<\\/param><\\/FS><FS><service>NOINPUT<\\/service><function>getClassMembers<\\/function><level>1<\\/level><paramSize>5<\\/paramSize><param order ='0'>?<\\/param><param order ='1'>SNOMEDCT<\\/param><param order ='2'>isa_disposition<\\/param><param order ='3'>0<\\/param><param order ='4'>IN,MIN,PIN<\\/param><\\/FS><FS><service>NOINPUT<\\/service><function>getRelatedByType<\\/function><level>2<\\/level><paramSize>2<\\/paramSize><param order ='0'>?<\\/param><param order ='1'>BPCK,GPCK,SBD,SCD<\\/param><\\/FS><\\/WFE>\\r\\nThis will produce a result file with all Opioid clinical drugs included\\r\\n\\r\\nStep 2\\r\\nTo remove all cough and bowel transit formulation codes and to remove the injectable codes filter out all codes with the following strings:\\r\\nIngredient strings: \\r\\nGuaifenesin, Chlorpheniramine, Pseudoephedrine, Brompheniramine, Phenylephrine, Phenylpropanolamine, Promethazine, Bromodiphenhydramine, guaiacolsulfonate, homatropine\\r\\nForm strings:\\r\\ninject, cartridge, syringe\",\n" +
           "        \"workflowXml\": \"<WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>findClassById</function><level>0</level><paramSize>1</paramSize><param order ='0'>?</param></FS><FS><service>NOINPUT</service><function>getClassMembers</function><level>1</level><paramSize>5</paramSize><param order ='0'>?</param><param order ='1'>SNOMEDCT</param><param order ='2'>isa_disposition</param><param order ='3'>0</param><param order ='4'>IN,MIN,PIN</param></FS><FS><service>NOINPUT</service><function>getRelatedByType</function><level>2</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE>\",\n" +
           "        \"input\": [ \"360204007\" ],\n" +
           "        \"excludeFilter\": [\n" +
           "          \"Guaifenesin\",\n" +
           "          \"Chlorpheniramine\",\n" +
           "          \"Pseudoephedrine\",\n" +
           "          \"Brompheniramine\",\n" +
           "          \"Phenylephrine\",\n" +
           "          \"Phenylpropanolamine\",\n" +
           "          \"Promethazine\",\n" +
           "          \"Bromodiphenhydramine\",\n" +
           "          \"guaiacolsulfonate\",\n" +
           "          \"homatropine\",\n" +
           "          \"inject\",\n" +
           "          \"cartridge\",\n" +
           "          \"syringe\"\n" +
           "        ]\n" +
           "      }\n" +
           "    }\n" +
           "  ]\n" +
           "}";
}
