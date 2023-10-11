package org.opencds.cqf.tooling.operations.library;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LibraryGenerationIT {
   private final DataRequirementsProcessor dataRequirementsProcessor = new DataRequirementsProcessor();
   private final CqlTranslatorOptions options = CqlTranslatorOptions.defaultOptions();

   @Test
   void testSimpleDSTU3LibraryGeneration() {
      ModelManager modelManager = new ModelManager();
      LibraryManager libraryManager = new LibraryManager(modelManager, options.getCqlCompilerOptions());

      LibraryGenerator libraryGenerator = new LibraryGenerator();
      libraryGenerator.setFhirContext(FhirContext.forDstu3Cached());
      CqlTranslator translator = CqlTranslator.fromText(DSTU3PartialFhirHelpers, libraryManager);
      IBaseResource library = libraryGenerator.resolveFhirLibrary(translator,
              dataRequirementsProcessor.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(),
                      options.getCqlCompilerOptions(), null, false), R4PartialFhirHelpers);
      Assert.assertTrue(library instanceof org.hl7.fhir.dstu3.model.Library);
      org.hl7.fhir.dstu3.model.Library r4Library = (org.hl7.fhir.dstu3.model.Library) library;
      Assert.assertTrue(r4Library.hasId());
      Assert.assertTrue(r4Library.hasName());
      Assert.assertTrue(r4Library.hasVersion());
      Assert.assertTrue(r4Library.hasExperimental());
      Assert.assertTrue(r4Library.hasStatus());
      Assert.assertTrue(r4Library.hasType());
      Assert.assertTrue(r4Library.hasContent());
      Assert.assertEquals(r4Library.getContent().size(), 3);
   }

   @Test
   void testSimpleR4LibraryGeneration() {
      ModelManager modelManager = new ModelManager();
      LibraryManager libraryManager = new LibraryManager(modelManager, options.getCqlCompilerOptions());

      LibraryGenerator libraryGenerator = new LibraryGenerator();
      libraryGenerator.setFhirContext(FhirContext.forR4Cached());
      CqlTranslator translator = CqlTranslator.fromText(R4PartialFhirHelpers, libraryManager);
      IBaseResource library = libraryGenerator.resolveFhirLibrary(translator,
              dataRequirementsProcessor.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(),
                      options.getCqlCompilerOptions(), null, false), R4PartialFhirHelpers);
      Assert.assertTrue(library instanceof org.hl7.fhir.r4.model.Library);
      org.hl7.fhir.r4.model.Library r4Library = (org.hl7.fhir.r4.model.Library) library;
      Assert.assertTrue(r4Library.hasId());
      Assert.assertTrue(r4Library.hasName());
      Assert.assertTrue(r4Library.hasVersion());
      Assert.assertTrue(r4Library.hasExperimental());
      Assert.assertTrue(r4Library.hasStatus());
      Assert.assertTrue(r4Library.hasType());
      Assert.assertTrue(r4Library.hasContent());
      Assert.assertEquals(r4Library.getContent().size(), 3);
   }

   @Test
   void testSimpleR5LibraryGeneration() {
      ModelManager modelManager = new ModelManager();
      LibraryManager libraryManager = new LibraryManager(modelManager, options.getCqlCompilerOptions());

      LibraryGenerator libraryGenerator = new LibraryGenerator();
      libraryGenerator.setFhirContext(FhirContext.forR5Cached());
      CqlTranslator translator = CqlTranslator.fromText(R4PartialFhirHelpers, libraryManager);
      IBaseResource library = libraryGenerator.resolveFhirLibrary(translator,
              dataRequirementsProcessor.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(),
                      options.getCqlCompilerOptions(), null, false), R4PartialFhirHelpers);
      Assert.assertTrue(library instanceof org.hl7.fhir.r5.model.Library);
      org.hl7.fhir.r5.model.Library r4Library = (org.hl7.fhir.r5.model.Library) library;
      Assert.assertTrue(r4Library.hasId());
      Assert.assertTrue(r4Library.hasName());
      Assert.assertTrue(r4Library.hasVersion());
      Assert.assertTrue(r4Library.hasExperimental());
      Assert.assertTrue(r4Library.hasStatus());
      Assert.assertTrue(r4Library.hasType());
      Assert.assertTrue(r4Library.hasContent());
      Assert.assertEquals(r4Library.getContent().size(), 3);
   }

   private final String DSTU3PartialFhirHelpers = "library FHIRHelpers version '1.8'\n" +
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

   private final String R4PartialFhirHelpers = "library FHIRHelpers version '4.0.1'\n" +
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
