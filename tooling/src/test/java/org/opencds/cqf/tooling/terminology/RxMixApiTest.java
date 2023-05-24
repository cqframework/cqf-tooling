package org.opencds.cqf.tooling.terminology;

import org.opencds.cqf.tooling.terminology.generate.FhirTxExpansion;
import org.opencds.cqf.tooling.terminology.generate.LoincHierarchyApi;
import org.opencds.cqf.tooling.terminology.generate.RxMixApi;
import org.opencds.cqf.tooling.terminology.generate.ValueSetsFromConfig;
import org.testng.annotations.Test;

public class RxMixApiTest {

   @Test
   void rxMixApiTest() {
      RxMixApi api = new RxMixApi();
      String[] args = new String[]{ "-RxMixWorkflowApi", "-ptc=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/rules-text/opioid-valueset-gen-cofig.json" };
      api.execute(args);
   }

   @Test
   void fhirTxExpansionTest() {
      FhirTxExpansion expansionOperation = new FhirTxExpansion();
      String[] args = new String[] { "-FhirTxExpansion", "-ptc=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/rules-text/opioid-valueset-gen-cofig.json" };
      expansionOperation.execute(args);
   }

   @Test
   void hierarchyTestAlcohol() {
      LoincHierarchyApi hierarchyApi = new LoincHierarchyApi();
      String[] args = new String[] { "-LoincHierarchyApi", "-ptc=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/rules-text/opioid-valueset-gen-cofig.json" };
      hierarchyApi.execute(args);
   }

   @Test
   void hierarchyTestOpioid() {
      LoincHierarchyApi hierarchyApi = new LoincHierarchyApi();
      String[] args = new String[] { "-LoincHierarchyApi", "-v=2.74", "-parents=LP15167-7,LP411610-1", "-system=Urine", "-user=cschuler", "-password=knight" };
      hierarchyApi.execute(args);
   }

   @Test
   void hierarchyTestOpiate() {
      LoincHierarchyApi hierarchyApi = new LoincHierarchyApi();
      String[] args = new String[] { "-LoincHierarchyApi", "-v=2.74", "-parents=LP14565-3", "-system=Urine", "-user=cschuler", "-password=knight" };
      hierarchyApi.execute(args);
   }

   @Test
   void testConfig() {
      ValueSetsFromConfig valueSetsFromConfig = new ValueSetsFromConfig();
      String[] args = new String[] { "-ValueSetsFromConfig", "-ptc=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/rules-text/opioid-valueset-gen-cofig.json" };
      valueSetsFromConfig.execute(args);
   }

   @Test
   void testConfigExpandMap() {
      ValueSetsFromConfig valueSetsFromConfig = new ValueSetsFromConfig();
      String[] args = new String[] { "-ValueSetsFromConfig", "-ptc=/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/rules-text/test-config.json" };
      valueSetsFromConfig.execute(args);
   }
}
