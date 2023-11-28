package org.opencds.cqf.tooling.operations.mat;

import ca.uhn.fhir.context.FhirContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class ExtractMatBundleIT {

   @Test
   void testDSTU3ExtractMatBundleOperation() {
      ExtractMatBundle extractMatBundle = new ExtractMatBundle();
      extractMatBundle.setFhirContext(FhirContext.forDstu3Cached());

      org.hl7.fhir.dstu3.model.Bundle dstu3MatBundle = new org.hl7.fhir.dstu3.model.Bundle();

      org.hl7.fhir.dstu3.model.Library dstu3MatLibrary = new org.hl7.fhir.dstu3.model.Library();
      dstu3MatLibrary.setId("dstu3-test-mat-library");
      dstu3MatLibrary.setName("DSTU3_TEST_MAT_LIBRARY");
      dstu3MatLibrary.addContent().setContentType("text/cql").setData(DSTU3TestMatLibrary.getBytes());

      org.hl7.fhir.dstu3.model.Measure dstu3MatMeasure = new org.hl7.fhir.dstu3.model.Measure();
      dstu3MatMeasure.setId("dstu3-test-mat-measure");
      dstu3MatMeasure.setName("DSTU3_TEST_MAT_MEASURE");
      dstu3MatMeasure.setLibrary(Collections.singletonList(new org.hl7.fhir.dstu3.model.Reference("Library/dstu3-test-mat-library")));

      dstu3MatBundle.addEntry().setResource(dstu3MatLibrary);
      dstu3MatBundle.addEntry().setResource(dstu3MatMeasure);

      ExtractMatBundle.MatPackage matPackage = extractMatBundle.getMatPackage(dstu3MatBundle);
      Assert.assertFalse(matPackage.getLibraryPackages().isEmpty());
      Assert.assertNotNull(matPackage.getLibraryPackages().get(0).getCql());
      Assert.assertFalse(matPackage.getMeasures().isEmpty());
      Assert.assertTrue(matPackage.getOtherResources().isEmpty());
   }

   @Test
   void testR4ExtractMatBundleOperation() {
      ExtractMatBundle extractMatBundle = new ExtractMatBundle();
      extractMatBundle.setFhirContext(FhirContext.forR4Cached());

      org.hl7.fhir.r4.model.Bundle r4MatBundle = new org.hl7.fhir.r4.model.Bundle();

      org.hl7.fhir.r4.model.Library r4MatLibrary = new org.hl7.fhir.r4.model.Library();
      r4MatLibrary.setId("r4-test-mat-library");
      r4MatLibrary.setName("R4_TEST_MAT_LIBRARY");
      r4MatLibrary.addContent().setContentType("text/cql").setData(R4TestMatLibrary.getBytes());

      org.hl7.fhir.r4.model.Measure r4MatMeasure = new org.hl7.fhir.r4.model.Measure();
      r4MatMeasure.setId("r4-test-mat-measure");
      r4MatMeasure.setName("R4_TEST_MAT_MEASURE");
      r4MatMeasure.setLibrary(Collections.singletonList(new org.hl7.fhir.r4.model.CanonicalType("Library/r4-test-mat-library")));

      r4MatBundle.addEntry().setResource(r4MatLibrary);
      r4MatBundle.addEntry().setResource(r4MatMeasure);

      ExtractMatBundle.MatPackage matPackage = extractMatBundle.getMatPackage(r4MatBundle);
      Assert.assertFalse(matPackage.getLibraryPackages().isEmpty());
      Assert.assertNotNull(matPackage.getLibraryPackages().get(0).getCql());
      Assert.assertFalse(matPackage.getMeasures().isEmpty());
      Assert.assertTrue(matPackage.getOtherResources().isEmpty());
   }

   private final String DSTU3TestMatLibrary = "library FHIRHelpers version '1.8'\n" +
           "\n" +
           "using FHIR version '1.8'\n" +
           "\n" +
           "define function ToInterval(period FHIR.Period):\n" +
           "    Interval[period.\"start\".value, period.\"end\".value]\n" +
           "\n" +
           "define function ToQuantity(quantity FHIR.Quantity):\n" +
           "    System.Quantity { value: quantity.value.value, unit: quantity.unit.value }\n" +
           "\n" +
           "define function ToInterval(range FHIR.Range):\n" +
           "    Interval[ToQuantity(range.low), ToQuantity(range.high)]\n" +
           "\n" +
           "define function ToCode(coding FHIR.Coding):\n" +
           "    System.Code {\n" +
           "      code: coding.code.value,\n" +
           "      system: coding.system.value,\n" +
           "      version: coding.version.value,\n" +
           "      display: coding.display.value\n" +
           "    }\n" +
           "\n" +
           "define function ToConcept(concept FHIR.CodeableConcept):\n" +
           "    System.Concept {\n" +
           "        codes: concept.coding C return ToCode(C),\n" +
           "        display: concept.text.value\n" +
           "    }";

   private final String R4TestMatLibrary = "library R4_TEST_MAT_LIBRARY version '4.0.1'\n" +
           "\n" +
           "using FHIR version '4.0.1'\n" +
           "\n" +
           "define function ToInterval(period FHIR.Period):\n" +
           "    if period is null then\n" +
           "        null\n" +
           "    else\n" +
           "        if period.\"start\" is null then\n" +
           "            Interval(period.\"start\".value, period.\"end\".value]\n" +
           "        else\n" +
           "            Interval[period.\"start\".value, period.\"end\".value]\n" +
           "\n" +
           "define function ToCalendarUnit(unit System.String):\n" +
           "    case unit\n" +
           "        when 'ms' then 'millisecond'\n" +
           "        when 's' then 'second'\n" +
           "        when 'min' then 'minute'\n" +
           "        when 'h' then 'hour'\n" +
           "        when 'd' then 'day'\n" +
           "        when 'wk' then 'week'\n" +
           "        when 'mo' then 'month'\n" +
           "        when 'a' then 'year'\n" +
           "        else unit\n" +
           "    end\n" +
           "\n" +
           "define function ToQuantity(quantity FHIR.Quantity):\n" +
           "    case\n" +
           "        when quantity is null then null\n" +
           "        when quantity.value is null then null\n" +
           "        when quantity.comparator is not null then\n" +
           "            Message(null, true, 'FHIRHelpers.ToQuantity.ComparatorQuantityNotSupported', 'Error', 'FHIR Quantity value has a comparator and cannot be converted to a System.Quantity value.')\n" +
           "        when quantity.system is null or quantity.system.value = 'http://unitsofmeasure.org'\n" +
           "              or quantity.system.value = 'http://hl7.org/fhirpath/CodeSystem/calendar-units' then\n" +
           "            System.Quantity { value: quantity.value.value, unit: ToCalendarUnit(Coalesce(quantity.code.value, quantity.unit.value, '1')) }\n" +
           "        else\n" +
           "            Message(null, true, 'FHIRHelpers.ToQuantity.InvalidFHIRQuantity', 'Error', 'Invalid FHIR Quantity code: ' & quantity.unit.value & ' (' & quantity.system.value & '|' & quantity.code.value & ')')\n" +
           "    end\n" +
           "\n" +
           "define function ToQuantityIgnoringComparator(quantity FHIR.Quantity):\n" +
           "    case\n" +
           "        when quantity is null then null\n" +
           "        when quantity.value is null then null\n" +
           "        when quantity.system is null or quantity.system.value = 'http://unitsofmeasure.org'\n" +
           "              or quantity.system.value = 'http://hl7.org/fhirpath/CodeSystem/calendar-units' then\n" +
           "            System.Quantity { value: quantity.value.value, unit: ToCalendarUnit(Coalesce(quantity.code.value, quantity.unit.value, '1')) }\n" +
           "        else\n" +
           "            Message(null, true, 'FHIRHelpers.ToQuantity.InvalidFHIRQuantity', 'Error', 'Invalid FHIR Quantity code: ' & quantity.unit.value & ' (' & quantity.system.value & '|' & quantity.code.value & ')')\n" +
           "    end\n" +
           "\n" +
           "define function ToInterval(quantity FHIR.Quantity):\n" +
           "    if quantity is null then null else\n" +
           "        case quantity.comparator.value\n" +
           "            when '<' then\n" +
           "                Interval[\n" +
           "                    null,\n" +
           "                    ToQuantityIgnoringComparator(quantity)\n" +
           "                )\n" +
           "            when '<=' then\n" +
           "                Interval[\n" +
           "                    null,\n" +
           "                    ToQuantityIgnoringComparator(quantity)\n" +
           "                ]\n" +
           "            when '>=' then\n" +
           "                Interval[\n" +
           "                    ToQuantityIgnoringComparator(quantity),\n" +
           "                    null\n" +
           "                ]\n" +
           "            when '>' then\n" +
           "                Interval(\n" +
           "                    ToQuantityIgnoringComparator(quantity),\n" +
           "                    null\n" +
           "                ]\n" +
           "            else\n" +
           "                Interval[ToQuantity(quantity), ToQuantity(quantity)]\n" +
           "        end\n" +
           "\n" +
           "define function ToRatio(ratio FHIR.Ratio):\n" +
           "    if ratio is null then\n" +
           "        null\n" +
           "    else\n" +
           "        System.Ratio { numerator: ToQuantity(ratio.numerator), denominator: ToQuantity(ratio.denominator) }\n" +
           "\n" +
           "define function ToInterval(range FHIR.Range):\n" +
           "    if range is null then\n" +
           "        null\n" +
           "    else\n" +
           "        Interval[ToQuantity(range.low), ToQuantity(range.high)]\n" +
           "\n" +
           "define function ToCode(coding FHIR.Coding):\n" +
           "    if coding is null then\n" +
           "        null\n" +
           "    else\n" +
           "        System.Code {\n" +
           "          code: coding.code.value,\n" +
           "          system: coding.system.value,\n" +
           "          version: coding.version.value,\n" +
           "          display: coding.display.value\n" +
           "        }\n" +
           "\n" +
           "define function ToConcept(concept FHIR.CodeableConcept):\n" +
           "    if concept is null then\n" +
           "        null\n" +
           "    else\n" +
           "        System.Concept {\n" +
           "            codes: concept.coding C return ToCode(C),\n" +
           "            display: concept.text.value\n" +
           "        }";
}
