package org.opencds.cqf.tooling.operations.dateroller;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.TimeZone;

public class DateRollerOperationIT {
   private final FhirContext fhirContext = FhirContext.forR4Cached();
   private final Date today = new Date();
   private final Date todayMinus40Days = DateUtils.addDays(today, -40);
   private final Date todayMinus41Days = DateUtils.addDays(today, -41);
   private final Date todayMinus50Days = DateUtils.addDays(today, -50);
   private final String CDSHooksWithPrefetchAndNulls = "{\n" +
           "  \"hookInstance\": \"6bc883b2-b795-4dcb-b661-34884a31d472\",\n" +
           "  \"fhirServer\": \"http://localhost:8080/fhir\",\n" +
           "  \"hook\": \"order-sign\",\n" +
           "  \"context\": {\n" +
           "    \"userId\": \"Practitioner/example\",\n" +
           "    \"patientId\": \"Patient/example-rec-01-true-make-recommendations\",\n" +
           "    \"draftOrders\": {\n" +
           "      \"resourceType\": \"Bundle\",\n" +
           "      \"type\": \"collection\",\n" +
           "      \"entry\": [\n" +
           "        {\n" +
           "          \"resource\": {\n" +
           "            \"resourceType\": \"MedicationRequest\",\n" +
           "            \"id\": \"05f8cb26-2eb6-4124-b65d-bb1f13e21c49\",\n" +
           "            \"extension\": [\n" +
           "              {\n" +
           "                \"url\": \"http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller\",\n" +
           "                \"extension\": [\n" +
           "                  {\n" +
           "                    \"url\": \"dateLastUpdated\",\n" +
           "                    \"valueDateTime\": \"2022-10-10\"\n" +
           "                  },\n" +
           "                  {\n" +
           "                    \"url\": \"frequency\",\n" +
           "                    \"valueDuration\": {\n" +
           "                      \"value\": 30.0,\n" +
           "                      \"unit\": \"days\",\n" +
           "                      \"system\": \"http://unitsofmeasure.org\",\n" +
           "                      \"code\": \"d\"\n" +
           "                    }\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"status\": \"active\",\n" +
           "            \"intent\": \"order\",\n" +
           "            \"category\": [\n" +
           "              {\n" +
           "                \"coding\": [\n" +
           "                  {\n" +
           "                    \"system\": \"http://terminology.hl7.org/CodeSystem/medicationrequest-category\",\n" +
           "                    \"code\": \"community\",\n" +
           "                    \"display\": \"Community\"\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"medicationCodeableConcept\": {\n" +
           "              \"coding\": [\n" +
           "                {\n" +
           "                  \"system\": \"http://www.nlm.nih.gov/research/umls/rxnorm\",\n" +
           "                  \"code\": \"1010603\",\n" +
           "                  \"display\": \"Suboxone 2 MG / 0.5 MG Sublingual Film\"\n" +
           "                }\n" +
           "              ]\n" +
           "            },\n" +
           "            \"subject\": {\n" +
           "              \"reference\": \"Patient/example-rec-01-true-make-recommendations\"\n" +
           "            },\n" +
           "            \"encounter\": {\n" +
           "              \"reference\": \"Encounter/example-rec-01-in-outpatient-opioid-context\"\n" +
           "            },\n" +
           "            \"authoredOn\": \"2022-10-10\",\n" +
           "            \"dosageInstruction\": [\n" +
           "              {\n" +
           "                \"timing\": {\n" +
           "                  \"repeat\": {\n" +
           "                    \"frequency\": 1,\n" +
           "                    \"period\": 1.0,\n" +
           "                    \"periodUnit\": \"d\"\n" +
           "                  }\n" +
           "                },\n" +
           "                \"asNeededBoolean\": false,\n" +
           "                \"doseAndRate\": [\n" +
           "                  {\n" +
           "                    \"doseQuantity\": {\n" +
           "                      \"value\": 1.0,\n" +
           "                      \"unit\": \"film\"\n" +
           "                    }\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"dispenseRequest\": {\n" +
           "              \"validityPeriod\": {\n" +
           "                \"start\": \"2022-10-10T00:00:00-06:00\",\n" +
           "                \"end\": \"2023-01-10T00:00:00-07:00\"\n" +
           "              },\n" +
           "              \"numberOfRepeatsAllowed\": 1,\n" +
           "              \"expectedSupplyDuration\": {\n" +
           "                \"value\": 27,\n" +
           "                \"unit\": \"days\",\n" +
           "                \"system\": \"http://unitsofmeasure.org\",\n" +
           "                \"code\": \"d\"\n" +
           "              }\n" +
           "            }\n" +
           "          }\n" +
           "        }\n" +
           "      ]\n" +
           "    }\n" +
           "  },\n" +
           "  \"prefetch\": {\n" +
           "    \"item1\": {\n" +
           "      \"resourceType\": \"Patient\",\n" +
           "      \"id\": \"example-rec-01-true-make-recommendations\",\n" +
           "      \"extension\": [\n" +
           "        {\n" +
           "          \"url\": \"http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller\",\n" +
           "          \"extension\": [\n" +
           "            {\n" +
           "              \"url\": \"dateLastUpdated\",\n" +
           "              \"valueDateTime\": \"2022-10-10\"\n" +
           "            },\n" +
           "            {\n" +
           "              \"url\": \"frequency\",\n" +
           "              \"valueDuration\": {\n" +
           "                \"value\": 30.0,\n" +
           "                \"unit\": \"days\",\n" +
           "                \"system\": \"http://unitsofmeasure.org\",\n" +
           "                \"code\": \"d\"\n" +
           "              }\n" +
           "            }\n" +
           "          ]\n" +
           "        }\n" +
           "      ],\n" +
           "      \"birthDate\": \"2002-10-10\"\n" +
           "    },\n" +
           "    \"item2\": null,\n" +
           "    \"item3\": null,\n" +
           "    \"item4\": null,\n" +
           "    \"item5\": null,\n" +
           "    \"item6\": null,\n" +
           "    \"item7\": null,\n" +
           "    \"item8\": null,\n" +
           "    \"item9\": null,\n" +
           "    \"item10\": null\n" +
           "  }\n" +
           "}";
   private final String CDSHooksWithoutPrefetch = "{\n" +
           "  \"hookInstance\": \"6bc883b2-b795-4dcb-b661-34884a31d472\",\n" +
           "  \"fhirServer\": \"http://localhost:8080/fhir\",\n" +
           "  \"hook\": \"order-sign\",\n" +
           "  \"context\": {\n" +
           "    \"userId\": \"Practitioner/example\",\n" +
           "    \"patientId\": \"Patient/example-rec-01-true-make-recommendations\",\n" +
           "    \"draftOrders\": {\n" +
           "      \"resourceType\": \"Bundle\",\n" +
           "      \"type\": \"collection\",\n" +
           "      \"entry\": [\n" +
           "        {\n" +
           "          \"resource\": {\n" +
           "            \"resourceType\": \"MedicationRequest\",\n" +
           "            \"id\": \"05f8cb26-2eb6-4124-b65d-bb1f13e21c49\",\n" +
           "            \"extension\": [\n" +
           "              {\n" +
           "                \"url\": \"http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller\",\n" +
           "                \"extension\": [\n" +
           "                  {\n" +
           "                    \"url\": \"dateLastUpdated\",\n" +
           "                    \"valueDateTime\": \"2022-10-10\"\n" +
           "                  },\n" +
           "                  {\n" +
           "                    \"url\": \"frequency\",\n" +
           "                    \"valueDuration\": {\n" +
           "                      \"value\": 30.0,\n" +
           "                      \"unit\": \"days\",\n" +
           "                      \"system\": \"http://unitsofmeasure.org\",\n" +
           "                      \"code\": \"d\"\n" +
           "                    }\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"status\": \"active\",\n" +
           "            \"intent\": \"order\",\n" +
           "            \"category\": [\n" +
           "              {\n" +
           "                \"coding\": [\n" +
           "                  {\n" +
           "                    \"system\": \"http://terminology.hl7.org/CodeSystem/medicationrequest-category\",\n" +
           "                    \"code\": \"community\",\n" +
           "                    \"display\": \"Community\"\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"medicationCodeableConcept\": {\n" +
           "              \"coding\": [\n" +
           "                {\n" +
           "                  \"system\": \"http://www.nlm.nih.gov/research/umls/rxnorm\",\n" +
           "                  \"code\": \"1010603\",\n" +
           "                  \"display\": \"Suboxone 2 MG / 0.5 MG Sublingual Film\"\n" +
           "                }\n" +
           "              ]\n" +
           "            },\n" +
           "            \"subject\": {\n" +
           "              \"reference\": \"Patient/example-rec-01-true-make-recommendations\"\n" +
           "            },\n" +
           "            \"encounter\": {\n" +
           "              \"reference\": \"Encounter/example-rec-01-in-outpatient-opioid-context\"\n" +
           "            },\n" +
           "            \"authoredOn\": \"2022-10-10\",\n" +
           "            \"dosageInstruction\": [\n" +
           "              {\n" +
           "                \"timing\": {\n" +
           "                  \"repeat\": {\n" +
           "                    \"frequency\": 1,\n" +
           "                    \"period\": 1.0,\n" +
           "                    \"periodUnit\": \"d\"\n" +
           "                  }\n" +
           "                },\n" +
           "                \"asNeededBoolean\": false,\n" +
           "                \"doseAndRate\": [\n" +
           "                  {\n" +
           "                    \"doseQuantity\": {\n" +
           "                      \"value\": 1.0,\n" +
           "                      \"unit\": \"film\"\n" +
           "                    }\n" +
           "                  }\n" +
           "                ]\n" +
           "              }\n" +
           "            ],\n" +
           "            \"dispenseRequest\": {\n" +
           "              \"validityPeriod\": {\n" +
           "                \"start\": \"2022-10-10T00:00:00-06:00\",\n" +
           "                \"end\": \"2023-01-10T00:00:00-07:00\"\n" +
           "              },\n" +
           "              \"numberOfRepeatsAllowed\": 1,\n" +
           "              \"expectedSupplyDuration\": {\n" +
           "                \"value\": 27,\n" +
           "                \"unit\": \"days\",\n" +
           "                \"system\": \"http://unitsofmeasure.org\",\n" +
           "                \"code\": \"d\"\n" +
           "              }\n" +
           "            }\n" +
           "          }\n" +
           "        }\n" +
           "      ]\n" +
           "    }\n" +
           "  }\n" +
           "}";

   @Test
   void testDateRollerDateTimeElement() {
      RollTestDates dateRoller = new RollTestDates();

      // dateTime - same day as dateLastUpdated
      Observation observation = new Observation();
      observation.addExtension(getDateRollerExtension());
      observation.setEffective(new DateTimeType(todayMinus40Days));
      boolean result = dateRoller.getAllDateElements(fhirContext, observation, dateRoller.getDateClasses(fhirContext));
      Assert.assertTrue(result);
      Assert.assertTrue(DateUtils.isSameDay(observation.getEffectiveDateTimeType().getValue(), today));
      Assert.assertTrue(DateUtils.isSameDay(
              ((DateTimeType) observation.getExtensionByUrl(RollTestDates.DATEROLLER_EXT_URL)
                      .getExtensionByUrl("dateLastUpdated").getValue()).getValue(), today));
   }

   @Test
   void testDateRollerInstantElementTenDaysBeforeDLU() {
      RollTestDates dateRoller = new RollTestDates();

      // instant - 10 days before dateLastUpdated
      Observation observation = new Observation();
      observation.addExtension(getDateRollerExtension());
      observation.setIssued(todayMinus50Days);
      boolean result = dateRoller.getAllDateElements(fhirContext, observation, dateRoller.getDateClasses(fhirContext));
      Assert.assertTrue(result);
      Assert.assertTrue(DateUtils.isSameDay(observation.getIssued(), DateUtils.addDays(today, -10)));
   }

   @Test
   void testDateRollerPeriodElementUTCTimezone() {
      RollTestDates dateRoller = new RollTestDates();

      // period - start is 1 day before dateLastUpdated, end is same day as dateLastUpdated - UTC TimeZone (0 offset)
      Observation observation = new Observation();
      observation.addExtension(getDateRollerExtension());
      DateTimeType start = new DateTimeType(todayMinus41Days);
      start.setTimeZone(TimeZone.getTimeZone("UTC"));
      DateTimeType end = new DateTimeType(todayMinus40Days);
      end.setTimeZone(TimeZone.getTimeZone("UTC"));
      observation.setEffective(new Period().setStartElement(start).setEndElement(end));
      boolean result = dateRoller.getAllDateElements(
              fhirContext, observation, dateRoller.getDateClasses(fhirContext));
      Assert.assertTrue(result);
      Assert.assertTrue(DateUtils.isSameDay(
              observation.getEffectivePeriod().getStart(), DateUtils.addDays(today, -1)));
      Assert.assertTrue(DateUtils.isSameDay(observation.getEffectivePeriod().getEnd(), today));
      // Check that TimeZone is preserved
      Assert.assertEquals(observation.getEffectivePeriod().getStartElement().getTimeZone().getRawOffset(), 0);
   }

   @Test
   void testDateRollerBackboneElement() {
      RollTestDates dateRoller = new RollTestDates();

      // Backbone element Observation.component.valueDate
      Observation observation = new Observation();
      observation.addExtension(getDateRollerExtension());
      observation.addComponent().setValue(new DateTimeType(todayMinus50Days));
      boolean result = dateRoller.getAllDateElements(fhirContext, observation, dateRoller.getDateClasses(fhirContext));
      Assert.assertTrue(result);
      Assert.assertTrue(DateUtils.isSameDay(observation.getComponentFirstRep().getValueDateTimeType().getValue(),
              DateUtils.addDays(today, -10)));
   }

   @Test
   void testCdsHooksRequestPrefetch() {
      Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
      JsonObject request = gson.fromJson(CDSHooksWithPrefetchAndNulls, JsonObject.class);
      String oldRequest = gson.toJson(request);
      RollTestDates dateRoller = new RollTestDates();
      dateRoller.setFhirContext(fhirContext);
      dateRoller.getUpdatedRequest(request, gson);
      // TODO: more extensive testing to ensure the update is correct would be nice - need to revisit better method
      //  Would need to build request dynamically to reliably test expected dates or some other method
      //  For now, just ensuring that the requests are not the same is sufficient - desk checking has been performed
      Assert.assertNotEquals(gson.toJson(request), oldRequest);
      // ensure null values are preserved
      Assert.assertTrue(request.getAsJsonObject("prefetch").get("item2").isJsonNull());
   }

   @Test
   void testCdsHooksRequestNoPrefetch() {
      Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
      JsonObject request = gson.fromJson(CDSHooksWithoutPrefetch, JsonObject.class);
      String oldRequest = gson.toJson(request);
      RollTestDates dateRoller = new RollTestDates();
      dateRoller.setFhirContext(fhirContext);
      dateRoller.getUpdatedRequest(request, gson);
      // TODO: more extensive testing to ensure the update is correct would be nice - need to revisit better method
      //  Would need to build request dynamically to reliably test expected dates or some other method
      //  For now, just ensuring that the requests are not the same is sufficient - desk checking has been performed
      Assert.assertNotEquals(gson.toJson(request), oldRequest);
      // ensure no prefetch
      Assert.assertFalse(request.has("prefetch"));
   }

   private Extension getDateRollerExtension() {
      Duration frequency = new Duration();
      frequency.setValue(30).setUnit("days");
      Extension dateRollerExtension = new Extension(RollTestDates.DATEROLLER_EXT_URL);
      dateRollerExtension.addExtension("dateLastUpdated", new DateTimeType(todayMinus40Days));
      dateRollerExtension.addExtension("frequency", frequency);
      return dateRollerExtension;
   }
}
