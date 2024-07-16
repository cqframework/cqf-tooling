package org.opencds.cqf.tooling.operations.codesystem;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.mockserver.integration.ClientAndServer;
import org.opencds.cqf.tooling.constants.Terminology;
import org.opencds.cqf.tooling.operations.codesystem.loinc.HierarchyProcessor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class LoincHierarchyProcessorIT {

   // Simulates the LOINC hierarchy API at https://loinc.regenstrief.org/searchapi/hierarchy/component-system/search?searchString=
   // Cannot test that endpoint due to authorization requirements
   private ClientAndServer testClient;

   @BeforeClass
   void setup() {
      testClient = ClientAndServer.startClientAndServer(1080);

      // simple query response
      testClient.when(
              request()
                      .withMethod("GET")
                      .withQueryStringParameter("queryString", "cocaine (=system:Urine) AND NOT status:DEPRECATED")
      ).respond(
              response().withBody(COCAINE_URINE_QUERY_RESPONSE)
      );

      // simple multiaxial_descendantsof query
      testClient.when(
              request()
                      .withMethod("GET")
                      .withQueryStringParameter("queryString", "multiaxial_descendantsof:LP14348-4 AND SYSTEM:Urine")
      ).respond(
              response().withBody(CANNABINOID_QUERY_RESPONSE)
      );
   }

   @AfterClass
   void teardown() {
      testClient.stop();
   }

   @Test
   void testSimpleQuery() {
      HierarchyProcessor hierarchyProcessor = new HierarchyProcessor();
      hierarchyProcessor.setFhirContext(FhirContext.forR4Cached());
      hierarchyProcessor.setQuery("cocaine (=system:Urine) AND NOT status:DEPRECATED");
      hierarchyProcessor.setLoincHierarchyUrl("http://localhost:1080?queryString=");

      IBaseResource returnVs = hierarchyProcessor.getValueSet();
      Assert.assertTrue(returnVs instanceof ValueSet);
      Assert.assertTrue(((ValueSet) returnVs).hasExpansion());
      Assert.assertTrue(((ValueSet) returnVs).getExpansion().hasContains());
      Assert.assertTrue(((ValueSet) returnVs).getExpansion().getContainsFirstRep().hasSystem());
      Assert.assertEquals(((ValueSet) returnVs).getExpansion().getContainsFirstRep().getSystem(), Terminology.LOINC_SYSTEM_URL);
      Assert.assertEquals(((ValueSet) returnVs).getExpansion().getContains().size(), 41);
   }

   @Test
   void testSimpleDescendantsOfQuery() {
      HierarchyProcessor hierarchyProcessor = new HierarchyProcessor();
      hierarchyProcessor.setFhirContext(FhirContext.forR4Cached());
      hierarchyProcessor.setQuery("multiaxial_descendantsof:LP14348-4 AND SYSTEM:Urine");
      hierarchyProcessor.setLoincHierarchyUrl("http://localhost:1080?queryString=");

      IBaseResource returnVs = hierarchyProcessor.getValueSet();
      Assert.assertTrue(returnVs instanceof ValueSet);
      Assert.assertTrue(((ValueSet) returnVs).hasExpansion());
      Assert.assertTrue(((ValueSet) returnVs).getExpansion().hasContains());
      Assert.assertTrue(((ValueSet) returnVs).getExpansion().getContainsFirstRep().hasSystem());
      Assert.assertEquals(((ValueSet) returnVs).getExpansion().getContainsFirstRep().getSystem(), Terminology.LOINC_SYSTEM_URL);
      Assert.assertEquals(((ValueSet) returnVs).getExpansion().getContains().size(), 19);
   }

   private final String COCAINE_URINE_QUERY_RESPONSE = "[\n" +
           "    {\n" +
           "        \"Id\": -70212289,\n" +
           "        \"Code\": \"LP432695-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP432695-7\",\n" +
           "        \"CodeText\": \"{component}\",\n" +
           "        \"Level\": 0,\n" +
           "        \"LoincAncestorCount\": 102996,\n" +
           "        \"PathEnumeration\": \"00001\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14010013,\n" +
           "        \"ParentId\": -70212289,\n" +
           "        \"Code\": \"LP29693-6\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP29693-6\",\n" +
           "        \"CodeText\": \"Laboratory\",\n" +
           "        \"Level\": 1,\n" +
           "        \"LoincAncestorCount\": 62659,\n" +
           "        \"PathEnumeration\": \"00001.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -26057072,\n" +
           "        \"ParentId\": -14010013,\n" +
           "        \"Code\": \"LP7790-1\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP7790-1\",\n" +
           "        \"CodeText\": \"Drug toxicology\",\n" +
           "        \"Level\": 2,\n" +
           "        \"LoincAncestorCount\": 8636,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054262,\n" +
           "        \"ParentId\": -26057072,\n" +
           "        \"Code\": \"LP18046-0\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP18046-0\",\n" +
           "        \"CodeText\": \"Drugs\",\n" +
           "        \"Level\": 3,\n" +
           "        \"LoincAncestorCount\": 6367,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054410,\n" +
           "        \"ParentId\": -14054262,\n" +
           "        \"Code\": \"LP31450-7\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP31450-7\",\n" +
           "        \"CodeText\": \"Anti-infection drugs\",\n" +
           "        \"Level\": 4,\n" +
           "        \"LoincAncestorCount\": 463,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00008\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054411,\n" +
           "        \"ParentId\": -14054410,\n" +
           "        \"Code\": \"LP31426-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP31426-7\",\n" +
           "        \"CodeText\": \"Antibiotics\",\n" +
           "        \"Level\": 5,\n" +
           "        \"LoincAncestorCount\": 298,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00008.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054478,\n" +
           "        \"ParentId\": -14054411,\n" +
           "        \"Code\": \"LP100040-7\",\n" +
           "        \"Sequence\": 54,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP100040-7\",\n" +
           "        \"CodeText\": \"Levamisole\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 5,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00008.00001.00054\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89630682,\n" +
           "        \"ParentId\": -14054478,\n" +
           "        \"Code\": \"LP388980-7\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP388980-7\",\n" +
           "        \"CodeText\": \"Levamisole | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00008.00001.00054.00005\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179828967,\n" +
           "        \"ParentId\": 89630682,\n" +
           "        \"Code\": \"59295-6\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"59295-6\",\n" +
           "        \"CodeText\": \"Levamisole Ur-mCnc\",\n" +
           "        \"Component\": \"Levamisole\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00008.00001.00054.00005.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054713,\n" +
           "        \"ParentId\": -14054262,\n" +
           "        \"Code\": \"LP31448-1\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP31448-1\",\n" +
           "        \"CodeText\": \"Controlled substances and drugs of abuse\",\n" +
           "        \"Level\": 4,\n" +
           "        \"LoincAncestorCount\": 1786,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054797,\n" +
           "        \"ParentId\": -14054713,\n" +
           "        \"Code\": \"LP16048-8\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP16048-8\",\n" +
           "        \"CodeText\": \"Cocaine\",\n" +
           "        \"Level\": 5,\n" +
           "        \"LoincAncestorCount\": 149,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054798,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP16047-0\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP16047-0\",\n" +
           "        \"CodeText\": \"Benzoylecgonine\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 48,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631428,\n" +
           "        \"ParentId\": -14054798,\n" +
           "        \"Code\": \"LP389701-6\",\n" +
           "        \"Sequence\": 11,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389701-6\",\n" +
           "        \"CodeText\": \"Benzoylecgonine | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 16,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179579632,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"3394-4\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"3394-4\",\n" +
           "        \"CodeText\": \"BZE Ur-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179415838,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"16226-3\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"16226-3\",\n" +
           "        \"CodeText\": \"BZE Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179912178,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"70146-6\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"70146-6\",\n" +
           "        \"CodeText\": \"BZE Ur Scn-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00005\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179579559,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"3393-6\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"3393-6\",\n" +
           "        \"CodeText\": \"BZE Ur Ql\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179397166,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"14315-6\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"14315-6\",\n" +
           "        \"CodeText\": \"BZE Ur Ql Cfm\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180017879,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"8192-7\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"8192-7\",\n" +
           "        \"CodeText\": \"BZE Ur Ql SAMHSA Cfm\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"SAMHSA confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180017932,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"8193-5\",\n" +
           "        \"Sequence\": 9,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"8193-5\",\n" +
           "        \"CodeText\": \"BZE Ur Ql SAMHSA Scn\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"SAMHSA screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00009\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179397162,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"14314-9\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"14314-9\",\n" +
           "        \"CodeText\": \"BZE Ur Ql Scn\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00010\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179690474,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"43984-4\",\n" +
           "        \"Sequence\": 11,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"43984-4\",\n" +
           "        \"CodeText\": \"BZE Ur Ql Scn>150 ng/mL\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen>150 ng/mL\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00011\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179690477,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"43985-1\",\n" +
           "        \"Sequence\": 12,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"43985-1\",\n" +
           "        \"CodeText\": \"BZE Ur Ql Scn>300 ng/mL\",\n" +
           "        \"Component\": \"Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen>300 ng/mL\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00012\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179440718,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"19065-2\",\n" +
           "        \"Sequence\": 13,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19065-2\",\n" +
           "        \"CodeText\": \"BZE CtO Ur-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00013\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442834,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"19358-1\",\n" +
           "        \"Sequence\": 14,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19358-1\",\n" +
           "        \"CodeText\": \"BZE CtO Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00014\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442831,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"19357-3\",\n" +
           "        \"Sequence\": 15,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19357-3\",\n" +
           "        \"CodeText\": \"BZE CtO Ur Scn-mCnc\",\n" +
           "        \"Component\": \"Benzoylecgonine cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00015\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179389716,\n" +
           "        \"ParentId\": 89631428,\n" +
           "        \"Code\": \"13479-1\",\n" +
           "        \"Sequence\": 16,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"13479-1\",\n" +
           "        \"CodeText\": \"BZE/Creat Ur\",\n" +
           "        \"Component\": \"Benzoylecgonine/Creatinine\",\n" +
           "        \"Property\": \"MRto\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00001.00011.00016\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054799,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP36028-6\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP36028-6\",\n" +
           "        \"CodeText\": \"Cocaine+Benzoylecgonine\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 7,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631439,\n" +
           "        \"ParentId\": -14054799,\n" +
           "        \"Code\": \"LP389712-3\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389712-3\",\n" +
           "        \"CodeText\": \"Cocaine+Benzoylecgonine | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 2,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00004.00006\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179720326,\n" +
           "        \"ParentId\": 89631439,\n" +
           "        \"Code\": \"47400-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"47400-7\",\n" +
           "        \"CodeText\": \"Cocaine+BZE Ur Ql Cfm\",\n" +
           "        \"Component\": \"Cocaine+Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00004.00006.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179673449,\n" +
           "        \"ParentId\": 89631439,\n" +
           "        \"Code\": \"42241-0\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"42241-0\",\n" +
           "        \"CodeText\": \"Cocaine+BZE Ur Ql Scn\",\n" +
           "        \"Component\": \"Cocaine+Benzoylecgonine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00004.00006.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054800,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP230504-5\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP230504-5\",\n" +
           "        \"CodeText\": \"Cocaine+Benzoylecgonine+Cocaethylene\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631440,\n" +
           "        \"ParentId\": -14054800,\n" +
           "        \"Code\": \"LP389713-1\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389713-1\",\n" +
           "        \"CodeText\": \"Cocaine+Benzoylecgonine+Cocaethylene | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00006.00001\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180025622,\n" +
           "        \"ParentId\": 89631440,\n" +
           "        \"Code\": \"82723-8\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"82723-8\",\n" +
           "        \"CodeText\": \"Coc+Benzoylecgon+Cocaethyl Ur Ql Cfm\",\n" +
           "        \"Component\": \"Cocaine+Benzoylecgonine+Cocaethylene\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00006.00001.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054801,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP18505-5\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP18505-5\",\n" +
           "        \"CodeText\": \"Cocaethylene\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 23,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631448,\n" +
           "        \"ParentId\": -14054801,\n" +
           "        \"Code\": \"LP389720-6\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389720-6\",\n" +
           "        \"CodeText\": \"Cocaethylene | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 7,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179443204,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"19408-4\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19408-4\",\n" +
           "        \"CodeText\": \"Cocaethylene Ur-mCnc\",\n" +
           "        \"Component\": \"Cocaethylene\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179419560,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"16632-2\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"16632-2\",\n" +
           "        \"CodeText\": \"Cocaethylene Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Cocaethylene\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179443201,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"19406-8\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19406-8\",\n" +
           "        \"CodeText\": \"Cocaethylene Ur Ql Cfm\",\n" +
           "        \"Component\": \"Cocaethylene\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179443186,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"19405-0\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19405-0\",\n" +
           "        \"CodeText\": \"Cocaethylene Ur Ql Scn\",\n" +
           "        \"Component\": \"Cocaethylene\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179443235,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"19410-0\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19410-0\",\n" +
           "        \"CodeText\": \"Cocaethylene CtO Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Cocaethylene cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00005\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179443214,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"19409-2\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19409-2\",\n" +
           "        \"CodeText\": \"Cocaethylene CtO Ur Scn-mCnc\",\n" +
           "        \"Component\": \"Cocaethylene cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180062773,\n" +
           "        \"ParentId\": 89631448,\n" +
           "        \"Code\": \"86606-1\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"86606-1\",\n" +
           "        \"CodeText\": \"Cocaethylene/Creat Ur Cfm\",\n" +
           "        \"Component\": \"Cocaethylene/Creatinine\",\n" +
           "        \"Property\": \"MRto\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00007.00010.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054802,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP35633-4\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP35633-4\",\n" +
           "        \"CodeText\": \"3-Hydroxybenzoylecgonine\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 4,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00010\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631455,\n" +
           "        \"ParentId\": -14054802,\n" +
           "        \"Code\": \"LP389727-1\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389727-1\",\n" +
           "        \"CodeText\": \"3-Hydroxybenzoylecgonine | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00010.00003\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179751891,\n" +
           "        \"ParentId\": 89631455,\n" +
           "        \"Code\": \"50594-1\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"50594-1\",\n" +
           "        \"CodeText\": \"3OH-BZE Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"3-Hydroxybenzoylecgonine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00010.00003.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054803,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP28530-1\",\n" +
           "        \"Sequence\": 11,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP28530-1\",\n" +
           "        \"CodeText\": \"Ecgonine methyl ester\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 13,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00011\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631462,\n" +
           "        \"ParentId\": -14054803,\n" +
           "        \"Code\": \"LP389734-7\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389734-7\",\n" +
           "        \"CodeText\": \"Ecgonine methyl ester | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 3,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00011.00007\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179751871,\n" +
           "        \"ParentId\": 89631462,\n" +
           "        \"Code\": \"50592-5\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"50592-5\",\n" +
           "        \"CodeText\": \"EME Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Ecgonine methyl ester\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00011.00007.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180157065,\n" +
           "        \"ParentId\": 89631462,\n" +
           "        \"Code\": \"97154-9\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"97154-9\",\n" +
           "        \"CodeText\": \"EME Ur Ql Cfm\",\n" +
           "        \"Component\": \"Ecgonine methyl ester\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00011.00007.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180001886,\n" +
           "        \"ParentId\": 89631462,\n" +
           "        \"Code\": \"80144-9\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"80144-9\",\n" +
           "        \"CodeText\": \"EME Ur Ql Scn\",\n" +
           "        \"Component\": \"Ecgonine methyl ester\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00011.00007.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054805,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP76346-3\",\n" +
           "        \"Sequence\": 15,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP76346-3\",\n" +
           "        \"CodeText\": \"Cocaine metabolites\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 2,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00015\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054806,\n" +
           "        \"ParentId\": -14054805,\n" +
           "        \"Code\": \"LP71227-0\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP71227-0\",\n" +
           "        \"CodeText\": \"Cocaine metabolites.other\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00015.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631465,\n" +
           "        \"ParentId\": -14054806,\n" +
           "        \"Code\": \"LP389737-0\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389737-0\",\n" +
           "        \"CodeText\": \"Cocaine metabolites.other | Urine | Drug toxicology\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00015.00001.00001\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179779348,\n" +
           "        \"ParentId\": 89631465,\n" +
           "        \"Code\": \"53743-1\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"53743-1\",\n" +
           "        \"CodeText\": \"Cocaine metab.other Ur-mCnc\",\n" +
           "        \"Component\": \"Cocaine metabolites.other\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 9,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00015.00001.00001.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89631417,\n" +
           "        \"ParentId\": -14054797,\n" +
           "        \"Code\": \"LP389691-9\",\n" +
           "        \"Sequence\": 22,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP389691-9\",\n" +
           "        \"CodeText\": \"Cocaine | Urine | Drug toxicology\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 12,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179417830,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"16448-3\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"16448-3\",\n" +
           "        \"CodeText\": \"Cocaine Ur-aCnc\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"ACnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179579989,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"3398-5\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"3398-5\",\n" +
           "        \"CodeText\": \"Cocaine Ur-mCnc\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179453852,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"20519-5\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"20519-5\",\n" +
           "        \"CodeText\": \"Cocaine Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00005\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179579912,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"3397-7\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"3397-7\",\n" +
           "        \"CodeText\": \"Cocaine Ur Ql\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442848,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"19360-7\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19360-7\",\n" +
           "        \"CodeText\": \"Cocaine Ur Ql Cfm\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442846,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"19359-9\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19359-9\",\n" +
           "        \"CodeText\": \"Cocaine Ur Ql Scn\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179772263,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"52953-7\",\n" +
           "        \"Sequence\": 9,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"52953-7\",\n" +
           "        \"CodeText\": \"Cocaine Ur-sCnc\",\n" +
           "        \"Component\": \"Cocaine\",\n" +
           "        \"Property\": \"SCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00009\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442873,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"19363-1\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19363-1\",\n" +
           "        \"CodeText\": \"Cocaine CtO Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Cocaine cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00010\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179442861,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"19362-3\",\n" +
           "        \"Sequence\": 11,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"19362-3\",\n" +
           "        \"CodeText\": \"Cocaine CtO Ur Scn-mCnc\",\n" +
           "        \"Component\": \"Cocaine cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00011\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180062780,\n" +
           "        \"ParentId\": 89631417,\n" +
           "        \"Code\": \"86607-9\",\n" +
           "        \"Sequence\": 12,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"86607-9\",\n" +
           "        \"CodeText\": \"Cocaine/Creat Ur Cfm\",\n" +
           "        \"Component\": \"Cocaine/Creatinine\",\n" +
           "        \"Property\": \"MRto\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00010.00004.00022.00012\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -26332666,\n" +
           "        \"ParentId\": -26057072,\n" +
           "        \"Code\": \"LP29683-7\",\n" +
           "        \"Sequence\": 10,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP29683-7\",\n" +
           "        \"CodeText\": \"Drug and Toxicology Panels\",\n" +
           "        \"Level\": 3,\n" +
           "        \"LoincAncestorCount\": 144,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00010\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -26533947,\n" +
           "        \"ParentId\": -26332666,\n" +
           "        \"Code\": \"LP71231-2\",\n" +
           "        \"Sequence\": 32,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP71231-2\",\n" +
           "        \"CodeText\": \"Cocaine panel\",\n" +
           "        \"Level\": 4,\n" +
           "        \"LoincAncestorCount\": 2,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00010.00032\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89634026,\n" +
           "        \"ParentId\": -26533947,\n" +
           "        \"Code\": \"LP392055-2\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP392055-2\",\n" +
           "        \"CodeText\": \"Cocaine panel | Urine | Drug and Toxicology Panels\",\n" +
           "        \"Level\": 5,\n" +
           "        \"LoincAncestorCount\": 1,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00010.00032.00002\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179779384,\n" +
           "        \"ParentId\": 89634026,\n" +
           "        \"Code\": \"53747-2\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"53747-2\",\n" +
           "        \"CodeText\": \"Cocaine Pnl Ur\",\n" +
           "        \"Component\": \"Cocaine panel\",\n" +
           "        \"Property\": \"-\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"-\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00010.00032.00002.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    }\n" +
           "]";

   private final String CANNABINOID_QUERY_RESPONSE = "[\n" +
           "    {\n" +
           "        \"Id\": -70212289,\n" +
           "        \"Code\": \"LP432695-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP432695-7\",\n" +
           "        \"CodeText\": \"{component}\",\n" +
           "        \"Level\": 0,\n" +
           "        \"LoincAncestorCount\": 102996,\n" +
           "        \"PathEnumeration\": \"00001\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14010013,\n" +
           "        \"ParentId\": -70212289,\n" +
           "        \"Code\": \"LP29693-6\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP29693-6\",\n" +
           "        \"CodeText\": \"Laboratory\",\n" +
           "        \"Level\": 1,\n" +
           "        \"LoincAncestorCount\": 62659,\n" +
           "        \"PathEnumeration\": \"00001.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -26057072,\n" +
           "        \"ParentId\": -14010013,\n" +
           "        \"Code\": \"LP7790-1\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP7790-1\",\n" +
           "        \"CodeText\": \"Drug toxicology\",\n" +
           "        \"Level\": 2,\n" +
           "        \"LoincAncestorCount\": 8636,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14054262,\n" +
           "        \"ParentId\": -26057072,\n" +
           "        \"Code\": \"LP18046-0\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP18046-0\",\n" +
           "        \"CodeText\": \"Drugs\",\n" +
           "        \"Level\": 3,\n" +
           "        \"LoincAncestorCount\": 6367,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14055272,\n" +
           "        \"ParentId\": -14054262,\n" +
           "        \"Code\": \"LP31449-9\",\n" +
           "        \"Sequence\": 42,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP31449-9\",\n" +
           "        \"CodeText\": \"Psychiatric drugs\",\n" +
           "        \"Level\": 4,\n" +
           "        \"LoincAncestorCount\": 959,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14055411,\n" +
           "        \"ParentId\": -14055272,\n" +
           "        \"Code\": \"LP14348-4\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP14348-4\",\n" +
           "        \"CodeText\": \"Ethanol\",\n" +
           "        \"Level\": 5,\n" +
           "        \"LoincAncestorCount\": 48,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14055412,\n" +
           "        \"ParentId\": -14055411,\n" +
           "        \"Code\": \"LP16119-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP16119-7\",\n" +
           "        \"CodeText\": \"Disulfiram\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 3,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89633309,\n" +
           "        \"ParentId\": -14055412,\n" +
           "        \"Code\": \"LP391429-0\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP391429-0\",\n" +
           "        \"CodeText\": \"Disulfiram | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 2,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00001.00002\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 180125050,\n" +
           "        \"ParentId\": 89633309,\n" +
           "        \"Code\": \"9357-5\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"9357-5\",\n" +
           "        \"CodeText\": \"Disulfiram Ur-mCnc\",\n" +
           "        \"Component\": \"Disulfiram\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00001.00002.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179420963,\n" +
           "        \"ParentId\": 89633309,\n" +
           "        \"Code\": \"16781-7\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"16781-7\",\n" +
           "        \"CodeText\": \"Disulfiram Ur Ql\",\n" +
           "        \"Component\": \"Disulfiram\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00001.00002.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": -14055413,\n" +
           "        \"ParentId\": -14055411,\n" +
           "        \"Code\": \"LP36909-7\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP36909-7\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 10,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": true,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89633312,\n" +
           "        \"ParentId\": -14055413,\n" +
           "        \"Code\": \"LP391431-6\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP391431-6\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide | Urine | Drug toxicology\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 8,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179702972,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"45324-1\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"45324-1\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide Ur-mCnc\",\n" +
           "        \"Component\": \"Ethyl glucuronide\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179819422,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"58378-1\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"58378-1\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Ethyl glucuronide\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179792200,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"55349-5\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"55349-5\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide Ur Ql\",\n" +
           "        \"Component\": \"Ethyl glucuronide\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179819413,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"58377-3\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"58377-3\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide Ur Ql Cfm\",\n" +
           "        \"Component\": \"Ethyl glucuronide\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179819399,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"58375-7\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"58375-7\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide Ur Ql Scn\",\n" +
           "        \"Component\": \"Ethyl glucuronide\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00005\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179978208,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"77769-8\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"77769-8\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide CtO Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Ethyl glucuronide cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179992387,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"79239-0\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"79239-0\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide CtO Ur Scn-mCnc\",\n" +
           "        \"Component\": \"Ethyl glucuronide cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179819409,\n" +
           "        \"ParentId\": 89633312,\n" +
           "        \"Code\": \"58376-5\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"58376-5\",\n" +
           "        \"CodeText\": \"Ethyl glucuronide/Creat Ur\",\n" +
           "        \"Component\": \"Ethyl glucuronide/Creatinine\",\n" +
           "        \"Property\": \"MRto\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 8,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00004.00005.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 89633305,\n" +
           "        \"ParentId\": -14055411,\n" +
           "        \"Code\": \"LP391425-8\",\n" +
           "        \"Sequence\": 14,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"PartNumber\": \"LP391425-8\",\n" +
           "        \"CodeText\": \"Ethanol | Urine | Drug toxicology\",\n" +
           "        \"Level\": 6,\n" +
           "        \"LoincAncestorCount\": 9,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014\",\n" +
           "        \"ShowLink\": false,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": true,\n" +
           "        \"IsLoinc\": false\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179801965,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"5645-7\",\n" +
           "        \"Sequence\": 1,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"5645-7\",\n" +
           "        \"CodeText\": \"Ethanol Ur-mCnc\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00001\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179716113,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"46983-3\",\n" +
           "        \"Sequence\": 2,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"46983-3\",\n" +
           "        \"CodeText\": \"Ethanol Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00002\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179801823,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"5644-0\",\n" +
           "        \"Sequence\": 3,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"5644-0\",\n" +
           "        \"CodeText\": \"Ethanol Ur Ql\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00003\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179581711,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"34180-0\",\n" +
           "        \"Sequence\": 4,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"34180-0\",\n" +
           "        \"CodeText\": \"Ethanol Ur Ql Cfm\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00004\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179673463,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"42242-8\",\n" +
           "        \"Sequence\": 5,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"42242-8\",\n" +
           "        \"CodeText\": \"Ethanol Ur Ql Scn\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"PrThr\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Ord\",\n" +
           "        \"Method\": \"Screen\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00005\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179473241,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"22745-4\",\n" +
           "        \"Sequence\": 6,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"22745-4\",\n" +
           "        \"CodeText\": \"Ethanol Ur-sCnc\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"SCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00006\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179595375,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"35664-2\",\n" +
           "        \"Sequence\": 7,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"35664-2\",\n" +
           "        \"CodeText\": \"Ethanol ?Tm Ur-mCnc\",\n" +
           "        \"Component\": \"Ethanol\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"XXX\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00007\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179978196,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"77768-0\",\n" +
           "        \"Sequence\": 8,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"77768-0\",\n" +
           "        \"CodeText\": \"Ethanol CtO Ur Cfm-mCnc\",\n" +
           "        \"Component\": \"Ethanol cutoff\",\n" +
           "        \"Property\": \"MCnc\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Method\": \"Confirm\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00008\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    },\n" +
           "    {\n" +
           "        \"Id\": 179819261,\n" +
           "        \"ParentId\": 89633305,\n" +
           "        \"Code\": \"58356-7\",\n" +
           "        \"Sequence\": 9,\n" +
           "        \"HierarchyId\": 15,\n" +
           "        \"LoincNumber\": \"58356-7\",\n" +
           "        \"CodeText\": \"Ethanol/Creat Ur\",\n" +
           "        \"Component\": \"Ethanol/Creatinine\",\n" +
           "        \"Property\": \"MRto\",\n" +
           "        \"Timing\": \"Pt\",\n" +
           "        \"System\": \"Urine\",\n" +
           "        \"Scale\": \"Qn\",\n" +
           "        \"Level\": 7,\n" +
           "        \"LoincAncestorCount\": 0,\n" +
           "        \"PathEnumeration\": \"00001.00001.00004.00008.00042.00004.00014.00009\",\n" +
           "        \"ShowLink\": true,\n" +
           "        \"HasChildren\": false,\n" +
           "        \"HasLoincChildren\": false,\n" +
           "        \"IsLoinc\": true\n" +
           "    }\n" +
           "]";
}
