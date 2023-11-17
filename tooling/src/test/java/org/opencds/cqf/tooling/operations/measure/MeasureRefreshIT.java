package org.opencds.cqf.tooling.operations.measure;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Measure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;


public class MeasureRefreshIT {

    private final FhirContext fhirContext = FhirContext.forR5Cached();

    @Test
    void testSingleMeasureWithoutUpdate() {
        Measure measureToRefresh = (Measure) fhirContext.newJsonParser().parseResource(BCSMEASURE);
        MeasureRefresh measureRefresh = new MeasureRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        IBaseResource result = measureRefresh.refreshMeasure(measureToRefresh);

        //Assert.assertTrue(results instanceof Measure);
        Measure refreshedMeasure = (Measure) result;

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), refreshedMeasure.getDate()));

        // Contained Resource tests before update
        Assert.assertTrue(refreshedMeasure.hasContained());

        // Extension tests before update (should be the same)
        Assert.assertEquals(refreshedMeasure.getExtension().size(),
                measureToRefresh.getExtension().size());

        // Library tests before update (should be the same)
        Assert.assertEquals(refreshedMeasure.getLibrary().get(0).getId(), measureToRefresh.getLibrary().get(0).getId());
    }

    @Test
    void testSingleMeasureWithUpdate() {
        Measure measureToRefresh = (Measure) fhirContext.newJsonParser().parseResource(BCSMEASURE);
        MeasureRefresh measureRefresh = new MeasureRefresh(fhirContext,
                "src/test/resources/org/opencds/cqf/tooling/testfiles/refreshIG/input/cql/");

        Measure beforeUpdate = measureToRefresh.copy();
        measureRefresh.refreshMeasure(measureToRefresh);

        // test date update
        Assert.assertTrue(DateUtils.isSameDay(new Date(), measureToRefresh.getDate()));

        // Contained Resource tests before update
        Assert.assertTrue(measureToRefresh.hasContained());

        // DataRequirement tests before update (should not be the same)
        Assert.assertNotEquals(beforeUpdate.getExtension().size(),
                measureToRefresh.getExtension().size());

        // Library tests before update (should not be the same)
        Assert.assertEquals(beforeUpdate.getLibrary().get(0).getId(), measureToRefresh.getLibrary().get(0).getId());
    }

    private static final String BCSMEASURE = "{\n" +
            "  \"resourceType\": \"Measure\",\n" +
            "  \"id\": \"BreastCancerScreeningFHIR\",\n" +
            "  \"meta\": {\n" +
            "    \"versionId\": \"2\",\n" +
            "    \"lastUpdated\": \"2021-01-15T11:56:41.000-05:00\",\n" +
            "    \"source\": \"#6C52w7dhFJuyuCDD\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm\", \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm\" ]\n" +
            "  },\n" +
            "  \"language\": \"en\",\n" +
            "  \"extension\": [ {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-populationBasis\",\n" +
            "    \"valueCode\": \"boolean\"\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem\",\n" +
            "    \"valueReference\": {\n" +
            "      \"reference\": \"cqf-tooling\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
            "    \"valueParameterDefinition\": {\n" +
            "      \"name\": \"Measurement Period\",\n" +
            "      \"use\": \"in\",\n" +
            "      \"min\": 0,\n" +
            "      \"max\": \"1\",\n" +
            "      \"type\": \"Period\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
            "    \"valueParameterDefinition\": {\n" +
            "      \"name\": \"Numerator\",\n" +
            "      \"use\": \"out\",\n" +
            "      \"min\": 0,\n" +
            "      \"max\": \"1\",\n" +
            "      \"type\": \"boolean\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
            "    \"valueParameterDefinition\": {\n" +
            "      \"name\": \"Denominator\",\n" +
            "      \"use\": \"out\",\n" +
            "      \"min\": 0,\n" +
            "      \"max\": \"1\",\n" +
            "      \"type\": \"boolean\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
            "    \"valueParameterDefinition\": {\n" +
            "      \"name\": \"Initial Population\",\n" +
            "      \"use\": \"out\",\n" +
            "      \"min\": 0,\n" +
            "      \"max\": \"1\",\n" +
            "      \"type\": \"boolean\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter\",\n" +
            "    \"valueParameterDefinition\": {\n" +
            "      \"name\": \"Denominator Exclusion\",\n" +
            "      \"use\": \"out\",\n" +
            "      \"min\": 0,\n" +
            "      \"max\": \"1\",\n" +
            "      \"type\": \"boolean\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"DiagnosticReport\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/DiagnosticReport\" ],\n" +
            "      \"mustSupport\": [ \"effective\", \"code\", \"status\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.108.12.1018\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1240\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1025\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1023\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1016\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"period\", \"hospitalization\", \"hospitalization.dischargeDisposition\", \"type\", \"status\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.666.5.307\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"ServiceRequest\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/ServiceRequest\" ],\n" +
            "      \"mustSupport\": [ \"code\", \"authoredOn\", \"intent\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1108.15\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Procedure\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Procedure\" ],\n" +
            "      \"mustSupport\": [ \"code\", \"performed\", \"status\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1108.15\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Condition\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Condition\" ],\n" +
            "      \"mustSupport\": [ \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1070\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Condition\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Condition\" ],\n" +
            "      \"mustSupport\": [ \"bodySite\", \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1071\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Condition\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Condition\" ],\n" +
            "      \"mustSupport\": [ \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1069\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Condition\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Condition\" ],\n" +
            "      \"mustSupport\": [ \"bodySite\", \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1071\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"DeviceRequest\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/DeviceRequest\" ],\n" +
            "      \"mustSupport\": [ \"code\", \"authoredOn\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.118.12.1300\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Condition\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Condition\" ],\n" +
            "      \"mustSupport\": [ \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.113.12.1074\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"period\", \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1088\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Observation\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Observation\" ],\n" +
            "      \"mustSupport\": [ \"effective\", \"code\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"code\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.113.12.1075\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1087\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1086\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1085\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1084\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1014\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement\",\n" +
            "    \"valueDataRequirement\": {\n" +
            "      \"type\": \"Encounter\",\n" +
            "      \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "      \"mustSupport\": [ \"type\" ],\n" +
            "      \"codeFilter\": [ {\n" +
            "        \"path\": \"type\",\n" +
            "        \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1012\"\n" +
            "      } ]\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode\",\n" +
            "    \"valueCoding\": {\n" +
            "      \"system\": \"http://snomed.info/sct\",\n" +
            "      \"version\": \"http://snomed.info/sct/version/201709\",\n" +
            "      \"code\": \"428361000124107\",\n" +
            "      \"display\": \"Discharge to home for hospice care (procedure)\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode\",\n" +
            "    \"valueCoding\": {\n" +
            "      \"system\": \"http://snomed.info/sct\",\n" +
            "      \"version\": \"http://snomed.info/sct/version/201709\",\n" +
            "      \"code\": \"428371000124100\",\n" +
            "      \"display\": \"Discharge to healthcare facility for hospice care (procedure)\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Numerator\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Numerator\\\":\\n  exists (\\n  \\t\\t\\t\\t[DiagnosticReport: \\\"Mammography\\\"] Mammogram\\n  \\t\\t\\t\\t\\t\\twhere ( Global.\\\"Normalize Interval\\\"(Mammogram.effective) ends 27 months or less on or before end of \\\"Measurement Period\\\" )\\n  \\t\\t\\t\\t\\t\\t\\t\\tand Mammogram.status in { 'final', 'amended', 'corrected', 'appended' }\\n  \\t\\t)\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 0\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdultOutpatientEncountersFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Qualifying Encounters\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Qualifying Encounters\\\":\\n  (\\n      [Encounter: \\\"Office Visit\\\"]\\n    \\t\\tunion [Encounter: \\\"Annual Wellness Visit\\\"]\\n    \\t\\tunion [Encounter: \\\"Preventive Care Services - Established Office Visit, 18 and Up\\\"]\\n    \\t\\tunion [Encounter: \\\"Preventive Care Services-Initial Office Visit, 18 and Up\\\"]\\n    \\t\\tunion [Encounter: \\\"Home Healthcare Services\\\"]\\n    ) ValidEncounter\\n  \\t\\twhere ValidEncounter.period during \\\"Measurement Period\\\"\\n    \\t\\tand ValidEncounter.status  = 'finished'\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 1\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Initial Population\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Initial Population\\\":\\n  Patient.gender = 'female'\\n  \\t\\t\\tand Global.\\\"CalendarAgeInYearsAt\\\"(FHIRHelpers.ToDate(Patient.birthDate), start of \\\"Measurement Period\\\") in Interval[51, 74)\\n  \\t\\t\\tand exists AdultOutpatientEncounters.\\\"Qualifying Encounters\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 2\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Denominator\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Denominator\\\":\\n  \\\"Initial Population\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 3\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"HospiceFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Has Hospice\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Has Hospice\\\":\\n  exists (\\n      [Encounter: \\\"Encounter Inpatient\\\"] DischargeHospice\\n  \\t\\t\\twhere DischargeHospice.status = 'finished'\\n  \\t\\t    and (\\n  \\t        DischargeHospice.hospitalization.dischargeDisposition ~ \\\"Discharge to home for hospice care (procedure)\\\"\\n  \\t\\t\\t\\t    or DischargeHospice.hospitalization.dischargeDisposition ~ \\\"Discharge to healthcare facility for hospice care (procedure)\\\"\\n  \\t    \\t)\\n  \\t\\t\\t\\tand DischargeHospice.period ends during \\\"Measurement Period\\\"\\n  \\t)\\n      or exists (\\n        [ServiceRequest: \\\"Hospice care ambulatory\\\"] HospiceOrder\\n          where HospiceOrder.intent = 'order'\\n              and HospiceOrder.authoredOn in \\\"Measurement Period\\\"\\n      )\\n      or exists (\\n        [Procedure: \\\"Hospice care ambulatory\\\"] HospicePerformed\\n          where HospicePerformed.status = 'completed'\\n            and Global.\\\"Normalize Interval\\\"(HospicePerformed.performed) overlaps \\\"Measurement Period\\\"\\n      )\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 4\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Right Mastectomy Diagnosis\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Right Mastectomy Diagnosis\\\":\\n  (\\n  \\t\\t\\t\\t( [Condition: \\\"Status Post Right Mastectomy\\\"]\\n            //where C.clinicalStatus ~ ToConcept(Global.\\\"active\\\") not nneeded for exclusion\\n          )\\n  \\t\\t\\t\\tunion (\\n  \\t\\t\\t\\t\\t\\t[Condition: \\\"Unilateral Mastectomy, Unspecified Laterality\\\"] UnilateralMastectomyDiagnosis\\n  \\t\\t\\t\\t\\t\\t\\t\\twhere UnilateralMastectomyDiagnosis.bodySite in \\\"Right\\\"\\n                //    and UnilateralMastectomyDiagnosis.clinicalStatus ~ ToConcept(Global.\\\"active\\\") not needed for exclusion\\n  \\t\\t\\t\\t)\\n  \\t\\t) RightMastectomy\\n  \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(RightMastectomy.onset) starts on or before end of \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 5\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Right Mastectomy Procedure\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Right Mastectomy Procedure\\\":\\n  [Procedure: \\\"Unilateral Mastectomy Right\\\"] UnilateralMastectomyRightPerformed\\n           \\t\\twhere Global.\\\"Normalize Interval\\\"(UnilateralMastectomyRightPerformed.performed) ends on or before end of \\\"Measurement Period\\\"\\n                  and UnilateralMastectomyRightPerformed.status = 'completed'\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 6\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Left Mastectomy\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Left Mastectomy\\\":\\n  (\\n  \\t\\t    ( [Condition: \\\"Status Post Left Mastectomy\\\"]\\n          //  where C.clinicalStatus ~ ToConcept(Global.\\\"active\\\") not needed for exclusion\\n          )\\n  \\t\\t\\t\\tunion (\\n  \\t\\t\\t\\t\\t\\t[Condition: \\\"Unilateral Mastectomy, Unspecified Laterality\\\"] UnilateralMastectomyDiagnosis\\n  \\t\\t\\t\\t\\t\\t\\t\\twhere UnilateralMastectomyDiagnosis.bodySite in \\\"Left\\\"\\n                  //  and UnilateralMastectomyDiagnosis.clinicalStatus ~ ToConcept(Global.\\\"active\\\") not needed for exclusion\\n  \\t\\t\\t\\t)\\n  \\t\\t) LeftMastectomy\\n  \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(LeftMastectomy.onset) starts on or before end of \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 7\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Left Mastectomy Procedure\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Left Mastectomy Procedure\\\":\\n  [Procedure: \\\"Unilateral Mastectomy Left\\\"] UnilateralMastectomyLeftPerformed\\n              where Global.\\\"Normalize Interval\\\"(UnilateralMastectomyLeftPerformed.performed) ends on or before end of \\\"Measurement Period\\\"\\n                 and UnilateralMastectomyLeftPerformed.status = 'completed'\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 8\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Bilateral Mastectomy Diagnosis\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Bilateral Mastectomy Diagnosis\\\":\\n  [Condition: \\\"History of bilateral mastectomy\\\"] BilateralMastectomyHistory\\n  \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(BilateralMastectomyHistory.onset) starts on or before end of \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 9\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Bilateral Mastectomy Procedure\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"//      and BilateralMastectomyHistory.clinicalStatus ~ ToConcept(Global.\\\"active\\\") not needed because it is an exclusiondefine \\\"Bilateral Mastectomy Procedure\\\":\\n  [Procedure: \\\"Bilateral Mastectomy\\\"] BilateralMastectomyPerformed\\n  \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(BilateralMastectomyPerformed.performed) ends on or before end of \\\"Measurement Period\\\"\\n  \\t\\t\\t\\t\\t\\tand BilateralMastectomyPerformed.status = 'completed'\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 10\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Has Criteria Indicating Frailty\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Has Criteria Indicating Frailty\\\":\\n  //Ask Bryn about devices\\n      \\texists ( [DeviceRequest: \\\"Frailty Device\\\"] FrailtyDeviceOrder\\n      \\t\\t\\twhere FrailtyDeviceOrder.authoredOn during \\\"Measurement Period\\\"\\n      \\t)\\n      \\t\\t/* or exists ( [DeviceUseStatement: \\\"Frailty Device\\\"] FrailtyDeviceUse\\n      \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(FrailtyDeviceUse.timing) overlaps \\\"Measurement Period\\\"\\n      \\t\\t) */\\n      \\t\\tor exists ( [Condition: \\\"Frailty Diagnosis\\\"] FrailtyDiagnosis\\n      \\t\\t\\t\\twhere Global.\\\"Prevalence Period\\\"(FrailtyDiagnosis) overlaps \\\"Measurement Period\\\"\\n      \\t\\t)\\n      \\t\\tor exists ( [Encounter: \\\"Frailty Encounter\\\"] FrailtyEncounter\\n      \\t\\t\\t\\twhere FrailtyEncounter.period overlaps \\\"Measurement Period\\\"\\n      \\t\\t)\\n      \\t\\tor exists ( [Observation: \\\"Frailty Symptom\\\"] FrailtySymptomObservation\\n      \\t\\t\\t\\twhere Global.\\\"Normalize Interval\\\"(FrailtySymptomObservation.effective) overlaps \\\"Measurement Period\\\"\\n      \\t\\t)\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 11\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Outpatient Encounters with Advanced Illness\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Outpatient Encounters with Advanced Illness\\\":\\n  ( [Encounter: \\\"Outpatient\\\"]\\n      \\t\\tunion [Encounter: \\\"Observation\\\"]\\n      \\t\\tunion [Encounter: \\\"ED\\\"]\\n      \\t\\tunion [Encounter: \\\"Nonacute Inpatient\\\"] ) OutpatientEncounter\\n      \\t\\t with [Condition: \\\"Advanced Illness\\\"] AdvancedIllnessDiagnosis\\n                  such that exists (\\n                      OutpatientEncounter.diagnosis.condition EncounterDiagnosis\\n                          where EndsWith(EncounterDiagnosis.reference, AdvancedIllnessDiagnosis.id)\\n                  )\\n                  and OutpatientEncounter.period starts 2 years or less on or before\\n      \\t\\t\\tend of \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 12\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Inpatient Encounter with Advanced Illness\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Inpatient Encounter with Advanced Illness\\\":\\n  [Encounter: \\\"Acute Inpatient\\\"] InpatientEncounter\\n      \\t\\twith [Condition: \\\"Advanced Illness\\\"] AdvancedIllnessDiagnosis\\n                  such that exists (\\n                      InpatientEncounter.diagnosis.condition EncounterDiagnosis\\n                          where EndsWith(EncounterDiagnosis.reference, AdvancedIllnessDiagnosis.id)\\n                  )\\n      \\t\\t\\tand InpatientEncounter.period starts 2 years or less on or before\\n      \\t\\t\\tend of \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 13\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Dementia Medications In Year Before or During Measurement Period\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Dementia Medications In Year Before or During Measurement Period\\\":\\n  [\\\"MedicationDispense\\\": \\\"Dementia Medications\\\"] DementiaMed\\n      \\t\\twhere DementiaMed.whenHandedOver during Interval[\\n                  ( start of \\\"Measurement Period\\\" - 1 year ), end of \\\"Measurement Period\\\"\\n              ]\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 14\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Advanced Illness and Frailty Exclusion Not Including Over Age 80\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Advanced Illness and Frailty Exclusion Not Including Over Age 80\\\":\\n  //If the measure does NOT include populations age 80 and older, then use this logic:\\n      \\tGlobal.\\\"CalendarAgeInYearsAt\\\"(FHIRHelpers.ToDate(Patient.birthDate), start of \\\"Measurement Period\\\")>= 65\\n              and \\\"Has Criteria Indicating Frailty\\\"\\n              and ( Count(\\\"Outpatient Encounters with Advanced Illness\\\")>= 2\\n                  or exists ( \\\"Inpatient Encounter with Advanced Illness\\\" )\\n                  or exists \\\"Dementia Medications In Year Before or During Measurement Period\\\"\\n              )\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 15\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Long Term Care Periods During Measurement Period\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"// start heredefine \\\"Long Term Care Periods During Measurement Period\\\":\\n  ( [Encounter: \\\"Care Services in Long-Term Residential Facility\\\"]\\n              \\t\\tunion [Encounter: \\\"Nursing Facility Visit\\\"] ) LongTermFacilityEncounter\\n              \\t\\twhere LongTermFacilityEncounter.period overlaps \\\"Measurement Period\\\"\\n              \\t\\treturn LongTermFacilityEncounter.period\\n              \\t\\t\\tintersect \\\"Measurement Period\\\"\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 16\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"AdvancedIllnessandFrailtyExclusionECQMFHIR4\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Long Term Care Periods Longer Than 90 Consecutive Days\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Long Term Care Periods Longer Than 90 Consecutive Days\\\":\\n  exists ( \\\"Long Term Care Periods During Measurement Period\\\" LongTermCareDuringMP\\n        where duration in days of LongTermCareDuringMP > 90\\n    )\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 17\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"url\": \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition\",\n" +
            "    \"extension\": [ {\n" +
            "      \"url\": \"libraryName\",\n" +
            "      \"valueString\": \"BreastCancerScreeningFHIR\"\n" +
            "    }, {\n" +
            "      \"url\": \"name\",\n" +
            "      \"valueString\": \"Denominator Exclusion\"\n" +
            "    }, {\n" +
            "      \"url\": \"statement\",\n" +
            "      \"valueString\": \"define \\\"Denominator Exclusion\\\":\\n  Hospice.\\\"Has Hospice\\\"\\n  \\t\\t\\t\\tor (( exists \\\"Right Mastectomy Diagnosis\\\" \\n  \\t\\t\\t\\tor exists \\\"Right Mastectomy Procedure\\\")\\n            and (exists \\\"Left Mastectomy\\\" or exists \\\"Left Mastectomy Procedure\\\"))\\n  \\t\\t\\t\\tor exists \\\"Bilateral Mastectomy Diagnosis\\\"\\n  \\t\\t\\t\\tor exists \\\"Bilateral Mastectomy Procedure\\\"\\n          or Frailty.\\\"Advanced Illness and Frailty Exclusion Not Including Over Age 80\\\"\\n          or (Global.\\\"CalendarAgeInYearsAt\\\"(FHIRHelpers.ToDate(Patient.birthDate), start of \\\"Measurement Period\\\")>= 65\\n              and Frailty.\\\"Long Term Care Periods Longer Than 90 Consecutive Days\\\")\"\n" +
            "    }, {\n" +
            "      \"url\": \"displaySequence\",\n" +
            "      \"valueInteger\": 18\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"url\": \"http://ecqi.healthit.gov/ecqms/Measure/BreastCancerScreeningFHIR\",\n" +
            "  \"identifier\": [ {\n" +
            "    \"use\": \"official\",\n" +
            "    \"system\": \"http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/guid\",\n" +
            "    \"value\": \"80366f35-e0a0-4ba7-a746-ad5760b79e01\"\n" +
            "  } ],\n" +
            "  \"version\": \"2.0.003\",\n" +
            "  \"name\": \"BreastCancerScreeningFHIR\",\n" +
            "  \"title\": \"Breast Cancer Screening FHIR\",\n" +
            "  \"status\": \"draft\",\n" +
            "  \"experimental\": false,\n" +
            "  \"date\": \"2021-09-26T04:40:08-06:00\",\n" +
            "  \"publisher\": \"National Committee for Quality Assurance\",\n" +
            "  \"contact\": [ {\n" +
            "    \"telecom\": [ {\n" +
            "      \"system\": \"url\",\n" +
            "      \"value\": \"https://cms.gov\"\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"description\": \"Percentage of women 50-74 years of age who had a mammogram to screen for breast cancer in the 27 months prior to the end of the Measurement Period\",\n" +
            "  \"purpose\": \"Unknown\",\n" +
            "  \"copyright\": \"This Physician Performance Measure (Measure) and related data specifications are owned and were developed by the National Committee for Quality Assurance (NCQA). NCQA is not responsible for any use of the Measure. NCQA makes no representations, warranties, or endorsement about the quality of any organization or physician that uses or reports performance measures and NCQA has no liability to anyone who relies on such measures or specifications. NCQA holds a copyright in the Measure. The Measure can be reproduced and distributed, without modification, for noncommercial purposes (e.g., use by healthcare providers in connection with their practices) without obtaining approval from NCQA. Commercial use is defined as the sale, licensing, or distribution of the Measure for commercial gain, or incorporation of the Measure into a product or service that is sold, licensed or distributed for commercial gain. All commercial uses or requests for modification must be approved by NCQA and are subject to a license at the discretion of NCQA. (C) 2012-2019 National Committee for Quality Assurance. All Rights Reserved. \\n\\nLimited proprietary coding is contained in the Measure specifications for user convenience. Users of proprietary code sets should obtain all necessary licenses from the owners of the code sets. NCQA disclaims all liability for use or accuracy of any third party codes contained in the specifications.\\n\\nCPT(R) contained in the Measure specifications is copyright 2004-2019 American Medical Association. LOINC(R) copyright 2004-2019 Regenstrief Institute, Inc. This material contains SNOMED Clinical Terms(R) (SNOMED CT[R]) copyright 2004-2019 International Health Terminology Standards Development Organisation. ICD-10 copyright 2019 World Health Organization. All Rights Reserved.\",\n" +
            "  \"effectivePeriod\": {\n" +
            "    \"start\": \"2021-01-01\",\n" +
            "    \"end\": \"2021-12-31\"\n" +
            "  },\n" +
            "  \"relatedArtifact\": [ {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library Global\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/MATGlobalCommonFunctionsFHIR4|5.0.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library FHIRHelpers\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/FHIRHelpers|4.0.001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library FHIRHelpers\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/FHIRHelpers|4.0.001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library AdultOutpatientEncounters\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/AdultOutpatientEncountersFHIR4|2.0.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library FHIRHelpers\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/FHIRHelpers|4.0.001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library Hospice\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/HospiceFHIR4|2.0.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library FHIRHelpers\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/FHIRHelpers|4.0.001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library Global\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/MATGlobalCommonFunctionsFHIR4|5.0.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library Frailty\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/AdvancedIllnessandFrailtyExclusionECQMFHIR4|5.12.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library Global\",\n" +
            "    \"resource\": \"http://ecqi.healthit.gov/ecqms/Library/MATGlobalCommonFunctionsFHIR4|5.0.000\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Code system SNOMEDCT:2017-09\",\n" +
            "    \"resource\": \"http://snomed.info/sct|http://snomed.info/sct/version/201709\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Mammography\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.108.12.1018\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Office Visit\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Annual Wellness Visit\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1240\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Preventive Care Services - Established Office Visit, 18 and Up\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1025\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Preventive Care Services-Initial Office Visit, 18 and Up\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1023\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Home Healthcare Services\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1016\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Encounter Inpatient\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.666.5.307\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Hospice care ambulatory\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113762.1.4.1108.15\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Status Post Right Mastectomy\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1070\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Unilateral Mastectomy, Unspecified Laterality\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1071\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Right\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.122.12.1035\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Unilateral Mastectomy Right\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1134\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Status Post Left Mastectomy\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1069\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Left\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.122.12.1036\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Unilateral Mastectomy Left\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1133\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set History of bilateral mastectomy\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1068\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Bilateral Mastectomy\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.198.12.1005\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Frailty Device\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.118.12.1300\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Frailty Diagnosis\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.113.12.1074\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Frailty Encounter\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1088\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Frailty Symptom\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.113.12.1075\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Outpatient\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1087\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Observation\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1086\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set ED\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1085\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Nonacute Inpatient\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1084\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Advanced Illness\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.110.12.1082\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Acute Inpatient\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1083\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Dementia Medications\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.196.12.1510\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Care Services in Long-Term Residential Facility\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1014\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Nursing Facility Visit\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1012\"\n" +
            "  } ],\n" +
            "  \"library\": [ \"http://ecqi.healthit.gov/ecqms/Library/BreastCancerScreeningFHIR\" ],\n" +
            "  \"disclaimer\": \"The performance Measure is not a clinical guideline and does not establish a standard of medical care, and has not been tested for all potential applications. THE MEASURE AND SPECIFICATIONS ARE PROVIDED \\\"AS IS\\\" WITHOUT WARRANTY OF ANY KIND.\\n \\nDue to technical limitations, registered trademarks are indicated by (R) or [R] and unregistered trademarks are indicated by (TM) or [TM].\",\n" +
            "  \"scoring\": {\n" +
            "    \"coding\": [ {\n" +
            "      \"system\": \"http://terminology.hl7.org/CodeSystem/measure-scoring\",\n" +
            "      \"code\": \"proportion\",\n" +
            "      \"display\": \"Proportion\"\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"type\": [ {\n" +
            "    \"coding\": [ {\n" +
            "      \"system\": \"http://hl7.org/fhir/measure-type\",\n" +
            "      \"code\": \"process\"\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"rationale\": \"Breast cancer is one of the most common types of cancers, accounting for 15 percent of all new cancer diagnoses in the U.S. (Noone et al, 2018). In 2015, over 3 million women were estimated to be living with breast cancer in the U.S. and it is estimated that 12 percent of women will be diagnosed with breast cancer at some point during their lifetime (Noone et al, 2018). \\n\\nWhile there are other factors that affect a woman's risk of developing breast cancer, advancing age is a primary risk factor. Breast cancer is most frequently diagnosed among women ages 55-64; the median age at diagnosis is 62 years (Noone et al, 2018).\\n\\nThe chance of a woman being diagnosed with breast cancer in a given year increases with age. By age 40, the chances are 1 in 68; by age 50 it becomes 1 in 43; by age 60, it is 1 in 29 (American Cancer Society, 2017).\",\n" +
            "  \"clinicalRecommendationStatement\": \"The U.S. Preventive Services Task Force (USPSTF) recommends biennial screening mammography for women aged 50-74 years (B recommendation). \\n\\nThe decision to start screening mammography in women prior to age 50 years should be an individual one. Women who place a higher value on the potential benefit than the potential harms may choose to begin biennial screening between the ages of 40 and 49 years (C recommendation). (USPSTF, 2016) \\n\\nThe USPSTF concludes that the current evidence is insufficient to assess the balance of benefits and harms of screening mammography in women aged 75 years or older (I statement). (USPSTF, 2016) \\n\\nThe USPSTF concludes that the current evidence is insufficient to assess the benefits and harms of digital breast tomosynthesis (DBT) as a primary screening method for breast cancer (I Statement). (USPSTF, 2016) \\n\\nThe USPSTF concludes that the current evidence is insufficient to assess the balance of benefits and harms of adjunctive screening for breast cancer using breast ultrasonography, magnetic resonance imaging, DBT, or other methods in women identified to have dense breasts on an otherwise negative screening mammogram (I statement). (USPSTF, 2016)\",\n" +
            "  \"improvementNotation\": {\n" +
            "    \"coding\": [ {\n" +
            "      \"system\": \"http://terminology.hl7.org/CodeSystem/measure-improvement-notation\",\n" +
            "      \"code\": \"increase\"\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"guidance\": \"Patient self-report for procedures as well as diagnostic studies should be recorded in 'Procedure, Performed' template or 'Diagnostic Study, Performed' template in QRDA-1. \\n\\nThis measure evaluates primary screening. Do not count biopsies, breast ultrasounds, or MRIs because they are not appropriate methods for primary breast cancer screening.\\n\\nThis eCQM is a patient-based measure.\",\n" +
            "  \"group\": [ {\n" +
            "    \"population\": [ {\n" +
            "      \"id\": \"3D2DD734-0712-484A-BE23-B1D2FF96D83A\",\n" +
            "      \"code\": {\n" +
            "        \"coding\": [ {\n" +
            "          \"system\": \"http://terminology.hl7.org/CodeSystem/measure-population\",\n" +
            "          \"code\": \"initial-population\",\n" +
            "          \"display\": \"Initial Population\"\n" +
            "        } ]\n" +
            "      },\n" +
            "      \"criteria\": {\n" +
            "        \"language\": \"text/cql.identifier\",\n" +
            "        \"expression\": \"Initial Population\"\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"C4B18753-73BC-4D48-801E-82AB55A70139\",\n" +
            "      \"code\": {\n" +
            "        \"coding\": [ {\n" +
            "          \"system\": \"http://terminology.hl7.org/CodeSystem/measure-population\",\n" +
            "          \"code\": \"denominator\",\n" +
            "          \"display\": \"Denominator\"\n" +
            "        } ]\n" +
            "      },\n" +
            "      \"criteria\": {\n" +
            "        \"language\": \"text/cql.identifier\",\n" +
            "        \"expression\": \"Denominator\"\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"7C770CA2-5177-4C5F-A976-7F57EC9BC311\",\n" +
            "      \"code\": {\n" +
            "        \"coding\": [ {\n" +
            "          \"system\": \"http://terminology.hl7.org/CodeSystem/measure-population\",\n" +
            "          \"code\": \"denominator-exclusion\",\n" +
            "          \"display\": \"Denominator Exclusion\"\n" +
            "        } ]\n" +
            "      },\n" +
            "      \"criteria\": {\n" +
            "        \"language\": \"text/cql.identifier\",\n" +
            "        \"expression\": \"Denominator Exclusion\"\n" +
            "      }\n" +
            "    }, {\n" +
            "      \"id\": \"57BC5171-93A7-4D9B-AA42-18A344C8623B\",\n" +
            "      \"code\": {\n" +
            "        \"coding\": [ {\n" +
            "          \"system\": \"http://terminology.hl7.org/CodeSystem/measure-population\",\n" +
            "          \"code\": \"numerator\",\n" +
            "          \"display\": \"Numerator\"\n" +
            "        } ]\n" +
            "      },\n" +
            "      \"criteria\": {\n" +
            "        \"language\": \"text/cql.identifier\",\n" +
            "        \"expression\": \"Numerator\"\n" +
            "      }\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}";

}


