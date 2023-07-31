package org.opencds.cqf.tooling.operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalValueSetExpand extends Operation {
   private String encoding; // -encoding (-e)
   private String pathToDirectory; // -pathtodir (-ptd)
   private String version; // -version (-v) Can be dstu2, stu3, or r4
   private FhirContext fhirContext;

   @Override
   public void execute(String[] args) {
      setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output");
      for (String arg : args) {
         if (arg.equals("-LocalVSExpand")) continue;
         String[] flagAndValue = arg.split("=");
         if (flagAndValue.length < 2) {
            throw new IllegalArgumentException("Invalid argument: " + arg);
         }
         String flag = flagAndValue[0];
         String value = flagAndValue[1];

         switch (flag.replace("-", "").toLowerCase()) {
            case "encoding":
            case "e":
               encoding = value.toLowerCase();
               break;
            case "outputpath":
            case "op":
               setOutputPath(value);
               break; // -outputpath (-op)
            case "pathtodir":
            case "ptd":
               pathToDirectory = value;
               break;
            case "version": case "v":
               version = value;
               break;
            default: throw new IllegalArgumentException("Unknown flag: " + flag);
         }
      }

      if (encoding == null || encoding.isEmpty()) {
         encoding = "json";
      } else {
         if (!encoding.equalsIgnoreCase("xml") && !encoding.equalsIgnoreCase("json")) {
            throw new IllegalArgumentException(String.format("Unsupported encoding: %s. Allowed encodings { json, xml }", encoding));
         }
      }

      if (pathToDirectory == null) {
         pathToDirectory = "/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset";
      }

      if (version == null) {
         fhirContext = FhirContext.forR4Cached();
      }
      else {
         switch (version.toLowerCase()) {
            case "dstu2":
               fhirContext = FhirContext.forDstu2Cached();
               break;
            case "stu3":
               fhirContext = FhirContext.forDstu3Cached();
               break;
            case "r4":
               fhirContext = FhirContext.forR4Cached();
               break;
            default:
               throw new IllegalArgumentException("Unknown fhir version: " + version);
         }
      }

      Class<? extends IBaseResource> clazz =
              fhirContext.getResourceDefinition("ValueSet").newInstance().getClass();
      IBaseBundle bundle = BundleUtils.getBundleOfResourceTypeFromDirectory(pathToDirectory, fhirContext, clazz);
      List<IBaseResource> valueSets = BundleUtil.toListOfResources(fhirContext, bundle);
      Map<String, IBaseResource> valueSetMap = new HashMap<>();
      valueSets.forEach(valueSet -> valueSetMap.put(((ValueSet) valueSet).getUrl(), valueSet));

      ValueSet allScreenVS = (ValueSet) IOUtils.readResource(
              "/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/all-screenings.json", fhirContext);
      ValueSet allMedVS = (ValueSet) IOUtils.readResource(
              "/Users/christopherschuler/Documents/workspace/cqframework/igs/opioid-cds-r4/input/vocabulary/valueset/all-medications.json", fhirContext);

      List<ValueSet.ValueSetExpansionContainsComponent> expansion = new ArrayList<>();
      allScreenVS.getCompose().getInclude().forEach(
              include -> {
                 if (include.hasValueSet()) {
                    String vsUrl = include.getValueSet().get(0).asStringValue();
                    expansion.addAll(((ValueSet) valueSetMap.get(vsUrl)).getExpansion().getContains());
                 }
              }
      );
      Set<String> codeSet = new HashSet<>();
      allScreenVS.setExpansion(new ValueSet.ValueSetExpansionComponent().setContains(
              expansion.stream().filter(e -> codeSet.add(e.getCode())).collect(Collectors.toList())));

      allMedVS.getCompose().getInclude().forEach(
              include -> {
                 if (include.hasValueSet()) {
                    String vsUrl = include.getValueSet().get(0).asStringValue();
                    expansion.addAll(((ValueSet) valueSetMap.get(vsUrl)).getExpansion().getContains());
                 }
              }
      );
      allMedVS.setExpansion(new ValueSet.ValueSetExpansionComponent().setContains(
              expansion.stream().filter(e -> codeSet.add(e.getCode())).collect(Collectors.toList())));

      IOUtils.writeResource(allScreenVS, getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
      IOUtils.writeResource(allMedVS, getOutputPath(), IOUtils.Encoding.JSON, fhirContext);
   }

}
