package org.opencds.cqf.tooling.operations.codesystem;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.constants.Terminology;
import org.opencds.cqf.tooling.operations.codesystem.rxnorm.RxMixWorkflowProcessor;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class RxMixWorflowProcessorIT {

   @Test
   void testOpioidAnalgesicsWithAmbulatoryMisusePotential() {
      // Single Input with exclusion filters
      String rulesText = "https://mor.nlm.nih.gov/RxMix/ Script:\\r\\nStep 1a \\r\\nCreate Batch text input file (SCT-Opioids.txt) with following SCT identifier (for the concept \\\"Product containing opioid receptor agonist (product)\\\") as an input within the file: \\r\\n360204007       \\r\\n\\r\\nStep 1b\\r\\nSubmit batch job using the above SCT-Opioids.txt file to following workflow by uploading file (SCT-Opioid-wf.config) with the following in the file:  <WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>findClassById</function><level>0</level><paramSize>1</paramSize><param order ='0'>?</param></FS><FS><service>NOINPUT</service><function>getClassMembers</function><level>1</level><paramSize>5</paramSize><param order ='0'>?</param><param order ='1'>SNOMEDCT</param><param order ='2'>isa_disposition</param><param order ='3'>0</param><param order ='4'>IN,MIN,PIN</param></FS><FS><service>NOINPUT</service><function>getRelatedByType</function><level>2</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE>\\r\\nThis will produce a result file with all Opioid clinical drugs included\\r\\n\\r\\nStep 2\\r\\nTo remove all cough and bowel transit formulation codes and to remove the injectable codes filter out all codes with the following strings:\\r\\nIngredient strings: \\r\\nGuaifenesin, Chlorpheniramine, Pseudoephedrine, Brompheniramine, Phenylephrine, Phenylpropanolamine, Promethazine, Bromodiphenhydramine, guaiacolsulfonate, homatropine\\r\\nForm strings:\\r\\ninject, cartridge, syringe";
      String workflowLibrary = "<WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>findClassById</function><level>0</level><paramSize>1</paramSize><param order ='0'>?</param></FS><FS><service>NOINPUT</service><function>getClassMembers</function><level>1</level><paramSize>5</paramSize><param order ='0'>?</param><param order ='1'>SNOMEDCT</param><param order ='2'>isa_disposition</param><param order ='3'>0</param><param order ='4'>IN,MIN,PIN</param></FS><FS><service>NOINPUT</service><function>getRelatedByType</function><level>2</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE>";
      String input = "360204007";
      String excludeFilter = "Guaifenesin, Chlorpheniramine, Pseudoephedrine, Brompheniramine, Phenylephrine, Phenylpropanolamine, Promethazine, Bromodiphenhydramine, guaiacolsulfonate, homatropine, inject, cartridge, syringe";

      RxMixWorkflowProcessor rxMixWorkflowProcessor = new RxMixWorkflowProcessor();
      rxMixWorkflowProcessor.setRulesText(rulesText);
      rxMixWorkflowProcessor.setWorkflow(workflowLibrary);
      rxMixWorkflowProcessor.setInput(input);
      rxMixWorkflowProcessor.setExcludeFilter(excludeFilter);
      rxMixWorkflowProcessor.setFhirContext(FhirContext.forR4Cached());

      IBaseResource returnVs = rxMixWorkflowProcessor.getValueSet();
      Assert.assertEquals(rxMixWorkflowProcessor.getExcludeFilters().size(), 13);
      Assert.assertTrue(returnVs instanceof ValueSet);
      Assert.assertTrue(((ValueSet) returnVs).hasCompose());
      Assert.assertTrue(((ValueSet) returnVs).getCompose().hasInclude());
      Assert.assertTrue(((ValueSet) returnVs).getCompose().getIncludeFirstRep().hasSystem());
      Assert.assertEquals(((ValueSet) returnVs).getCompose().getIncludeFirstRep().getSystem(), Terminology.RXNORM_SYSTEM_URL);
   }

   @Test
   void testExtendedReleaseOpioidsWithAmbulatoryMisusePotential() {
      // Single Input with inclusion and exclusion filters (testing with abnormal whitespaces)
      String rulesText = "Step 1:\\r\\nExpand the value set \\\"Opioids with Opioid analgesic with ambulatory misuse potential\\\" and then remove concepts that are long acting:\\r\\n\\r\\nStep 2:\\r\\nFilter the result to only select concepts that are a drug with Methadone Ingredient, or have one of the following dose forms: 316943 Extended Release Oral Capsule, 316945 Extended Release Oral Tablet, 316946 Extended Release Oral Tablet, 316987 Transdermal System. To do this filter the list by only including descriptions that have one of the following strings: \\\"Extended Release\\\" OR \\\"Transdermal\\\" OR \\\"Methadone\\\".";
      String workflowLibrary = "<WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>findClassById</function><level>0</level><paramSize>1</paramSize><param order ='0'>?</param></FS><FS><service>NOINPUT</service><function>getClassMembers</function><level>1</level><paramSize>5</paramSize><param order ='0'>?</param><param order ='1'>SNOMEDCT</param><param order ='2'>isa_disposition</param><param order ='3'>0</param><param order ='4'>IN,MIN,PIN</param></FS><FS><service>NOINPUT</service><function>getRelatedByType</function><level>2</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE>";
      String input = "360204007";
      String includeFilter = "Extended Release , Transdermal,Methadone";
      String excludeFilter = "Guaifenesin, Chlorpheniramine, Pseudoephedrine, Brompheniramine, Phenylephrine, Phenylpropanolamine, Promethazine, Bromodiphenhydramine, guaiacolsulfonate, homatropine, inject, cartridge, syringe";

      RxMixWorkflowProcessor rxMixWorkflowProcessor = new RxMixWorkflowProcessor();
      rxMixWorkflowProcessor.setRulesText(rulesText);
      rxMixWorkflowProcessor.setWorkflow(workflowLibrary);
      rxMixWorkflowProcessor.setInput(input);
      rxMixWorkflowProcessor.setIncludeFilter(includeFilter);
      rxMixWorkflowProcessor.setExcludeFilter(excludeFilter);
      rxMixWorkflowProcessor.setFhirContext(FhirContext.forR4Cached());

      IBaseResource returnVs = rxMixWorkflowProcessor.getValueSet();
      Assert.assertEquals(rxMixWorkflowProcessor.getIncludeFilters().size(), 3);
      Assert.assertEquals(rxMixWorkflowProcessor.getExcludeFilters().size(), 13);
      Assert.assertTrue(returnVs instanceof ValueSet);
      Assert.assertTrue(((ValueSet) returnVs).hasCompose());
      Assert.assertTrue(((ValueSet) returnVs).getCompose().hasInclude());
   }

   @Test
   void testFentanylTypeMedications() {
      // Multiple Inputs (testing with abnormal whitespaces)
      String rulesText = "https://mor.nlm.nih.gov/RxMix/ Script: \\r\\nStep 1 Upload to RxMix a workflow config file  named GetRelatedByType.config containing the following workflow text: <WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>getRelatedByType</function><level>0</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE> \\r\\n\\r\\nStep 2 Create Batch text input file (Ingredients.txt) with following RxNorm Fentanyl-type ingredient codes representing fentanyl, sufentanil, alfentanil, remifentanil as an input within the file:\\r\\n4337\\r\\n56795\\r\\n480\\r\\n73032\\r\\n\\r\\nStep 3 Upload the batch text input file Ingredients.txt created in step 2. \\r\\n\\r\\nStep 4 Submit the batch which will run the workflow using the input codes to generate a combined set of all the concepts needed..\\r\\n";
      String workflowLibrary = "<WFE><filteredOutputs>RXCUI|name|term_type</filteredOutputs><input>NOINPUT</input><FS><service>NOINPUT</service><function>getRelatedByType</function><level>0</level><paramSize>2</paramSize><param order ='0'>?</param><param order ='1'>BPCK,GPCK,SBD,SCD</param></FS></WFE>";
      String input = "4337, 56795,480,  73032";

      RxMixWorkflowProcessor rxMixWorkflowProcessor = new RxMixWorkflowProcessor();
      rxMixWorkflowProcessor.setRulesText(rulesText);
      rxMixWorkflowProcessor.setWorkflow(workflowLibrary);
      rxMixWorkflowProcessor.setInput(input);
      rxMixWorkflowProcessor.setFhirContext(FhirContext.forR4Cached());

      IBaseResource returnVs = rxMixWorkflowProcessor.getValueSet();
      Assert.assertEquals(rxMixWorkflowProcessor.getInputs().size(), 4);
      Assert.assertTrue(returnVs instanceof ValueSet);
      Assert.assertTrue(((ValueSet) returnVs).hasCompose());
      Assert.assertTrue(((ValueSet) returnVs).getCompose().hasInclude());
   }
}
