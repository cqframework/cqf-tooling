package org.opencds.cqf.tooling.operations.transform;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class StructureMappingIT {

   private final FhirContext fhirContext = FhirContext.forR4Cached();

   @Test
   void testActivityDefinitionToSupplyRequest() {
      StructureMapping structureMapping = new StructureMapping();
      structureMapping.setFhirContext(fhirContext);
      structureMapping.setPackageUrl("https://packages.simplifier.net/hl7.fhir.r4.core/4.0.1");
      structureMapping.setDefaultStructureMapUtilities();

      IParser parser = fhirContext.newJsonParser();
      StructureMap structureMap = parser.parseResource(StructureMap.class, AD_TO_SR_MAP);
      ActivityDefinition source = parser.parseResource(ActivityDefinition.class, SUPP_REQ_ACT_DEF);
      SupplyRequest target = getGenericSupplyRequest();
      target.setId("target");

      Resource result = structureMapping.transform(source, target, structureMap);
      Assert.assertTrue(result instanceof SupplyRequest);
      SupplyRequest supplyRequest = (SupplyRequest) result;

      // target.status -> draft
      Assert.assertTrue(supplyRequest.hasStatus());
      Assert.assertEquals(supplyRequest.getStatus(), SupplyRequest.SupplyRequestStatus.DRAFT);

      // target.category -> nonstock
      Assert.assertTrue(supplyRequest.hasCategory());
      CodeableConcept category = supplyRequest.getCategory();
      Assert.assertTrue(category.hasCoding());
      Coding categoryCoding = category.getCodingFirstRep();
      Assert.assertTrue(categoryCoding.hasSystem());
      Assert.assertEquals(categoryCoding.getSystem(), "http://terminology.hl7.org/CodeSystem/supply-kind");
      Assert.assertTrue(categoryCoding.hasCode());
      Assert.assertEquals(categoryCoding.getCode(), "nonstock");

      // target.priority -> routine
      Assert.assertTrue(supplyRequest.hasPriority());
      Assert.assertEquals(supplyRequest.getPriority(), SupplyRequest.RequestPriority.ROUTINE);

      // source.quantity = target.quantity
      Assert.assertTrue(source.hasQuantity());
      Assert.assertTrue(supplyRequest.hasQuantity());
      Assert.assertEquals(supplyRequest.getQuantity(), source.getQuantity());

      // source.code = target.item
      Assert.assertTrue(source.hasCode());
      Assert.assertTrue(supplyRequest.hasItem());
      Assert.assertEquals(supplyRequest.getItemCodeableConcept(), source.getCode());

      // target.occurrence -> today
      Assert.assertTrue(supplyRequest.hasOccurrence());
      Assert.assertTrue(DateUtils.isSameDay(supplyRequest.getOccurrenceDateTimeType().getValue(), new Date()));

      // target.authoredOn -> today
      Assert.assertTrue(supplyRequest.hasAuthoredOn());
      Assert.assertTrue(DateUtils.isSameDay(supplyRequest.getAuthoredOn(), new Date()));
   }

   void testFhirBaseToQiCore() {
      StructureMapping structureMapping = new StructureMapping();
      structureMapping.setFhirContext(fhirContext);
      structureMapping.setPackageUrl("http://hl7.org/fhir/us/qicore/4.1.1/package.tgz");
      structureMapping.setDefaultStructureMapUtilities();
   }

   private SupplyRequest getGenericSupplyRequest() {
      SupplyRequest supplyRequest = new SupplyRequest();
      supplyRequest.setStatus(SupplyRequest.SupplyRequestStatus.ACTIVE);
      return supplyRequest;
   }

   private final String SUPP_REQ_ACT_DEF = "{\n" +
           "  \"resourceType\": \"ActivityDefinition\",\n" +
           "  \"id\": \"blood-tubes-supply\",\n" +
           "  \"status\": \"draft\",\n" +
           "  \"description\": \"10 Blood collect tubes blue cap\",\n" +
           "  \"purpose\": \"Describes a request for 10 Blood collection tubes with blue caps.\",\n" +
           "  \"usage\": \"This activity definition is used as the definition of a supply request to resupply blood collection tubes. Elements that apply universally are defined here, while elements that apply to the specific setting of a referral within a particular order set are defined in the order set.\",\n" +
           "  \"kind\": \"SupplyRequest\",\n" +
           "  \"code\": {\n" +
           "    \"coding\": [\n" +
           "      {\n" +
           "        \"code\": \"BlueTubes\",\n" +
           "        \"display\": \"Blood collect tubes blue cap\"\n" +
           "      }\n" +
           "    ]\n" +
           "  },\n" +
           "  \"quantity\": {\n" +
           "    \"value\": 10\n" +
           "  },\n" +
           "  \"transform\": \"StructureMap/supplyrequest-transform\"\n" +
           "}";

   private final String AD_TO_SR_MAP = "{\n" +
           "  \"resourceType\": \"StructureMap\",\n" +
           "  \"id\": \"supplyrequest-transform\",\n" +
           "  \"url\": \"http://hl7.org/fhir/StructureMap/supplyrequest-transform\",\n" +
           "  \"name\": \"Transform from an ActivityDefinition to a SupplyRequest\",\n" +
           "  \"status\": \"draft\",\n" +
           "  \"structure\": [\n" +
           "    {\n" +
           "      \"url\": \"http://hl7.org/fhir/StructureDefinition/activitydefinition\",\n" +
           "      \"mode\": \"source\"\n" +
           "    },\n" +
           "    {\n" +
           "      \"url\": \"http://hl7.org/fhir/StructureDefinition/supplyrequest\",\n" +
           "      \"mode\": \"target\"\n" +
           "    }\n" +
           "  ],\n" +
           "  \"group\": [\n" +
           "    {\n" +
           "      \"name\": \"main\",\n" +
           "      \"typeMode\": \"none\",\n" +
           "      \"input\": [\n" +
           "        {\n" +
           "          \"name\": \"source\",\n" +
           "          \"type\": \"ActivityDefinition\",\n" +
           "          \"mode\": \"source\"\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"target\",\n" +
           "          \"type\": \"SupplyRequest\",\n" +
           "          \"mode\": \"target\"\n" +
           "        }\n" +
           "      ],\n" +
           "      \"rule\": [\n" +
           "        {\n" +
           "          \"name\": \"status\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"id\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"status\",\n" +
           "              \"transform\": \"evaluate\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                },\n" +
           "                {\n" +
           "                  \"valueString\": \"'draft'\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"category\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"id\",\n" +
           "              \"variable\": \"code\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"category\",\n" +
           "              \"transform\": \"cc\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueString\": \"http://hl7.org/fhir/ValueSet/supplyrequest-kind\"\n" +
           "                },\n" +
           "                {\n" +
           "                  \"valueString\": \"nonstock\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"priority\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"id\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"priority\",\n" +
           "              \"transform\": \"evaluate\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                },\n" +
           "                {\n" +
           "                  \"valueString\": \"'routine'\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"quantity\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"quantity\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"quantity\",\n" +
           "              \"transform\": \"copy\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"item\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"code\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"item\",\n" +
           "              \"transform\": \"copy\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"when\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"id\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"occurrence\",\n" +
           "              \"transform\": \"evaluate\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                },\n" +
           "                {\n" +
           "                  \"valueString\": \"now()\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        },\n" +
           "        {\n" +
           "          \"name\": \"authoredOn\",\n" +
           "          \"source\": [\n" +
           "            {\n" +
           "              \"context\": \"source\",\n" +
           "              \"element\": \"id\",\n" +
           "              \"variable\": \"a\"\n" +
           "            }\n" +
           "          ],\n" +
           "          \"target\": [\n" +
           "            {\n" +
           "              \"context\": \"target\",\n" +
           "              \"contextType\": \"variable\",\n" +
           "              \"element\": \"authoredOn\",\n" +
           "              \"transform\": \"evaluate\",\n" +
           "              \"parameter\": [\n" +
           "                {\n" +
           "                  \"valueId\": \"a\"\n" +
           "                },\n" +
           "                {\n" +
           "                  \"valueString\": \"now()\"\n" +
           "                }\n" +
           "              ]\n" +
           "            }\n" +
           "          ]\n" +
           "        }\n" +
           "      ]\n" +
           "    }\n" +
           "  ]\n" +
           "}";
}
