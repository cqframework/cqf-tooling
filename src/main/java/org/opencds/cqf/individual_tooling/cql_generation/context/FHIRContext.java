package org.opencds.cqf.individual_tooling.cql_generation.context;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.individual_tooling.cql_generation.IOUtil;
import org.opencds.cqf.individual_tooling.cql_generation.drool.serialization.PairJacksonProvider;

public class FHIRContext {

        public Set<Pair<String, String>> fhirModelingSet = new HashSet<Pair<String, String>>();
        public Map<String, Pair<String, String>> valuesetMapping = new HashMap<String, Pair<String, String>>();
        private static String fhirModelingMapFilePath = ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\fhirmodelingmap.txt";
        private static String valueSetMappingFilePath = ".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\valuesetMapping.json";
        // Create a Model retriever thingy
        public static final Map<String, Pair<String, String>> cdsdmToFhirMap = Map.ofEntries(
                        Map.entry("EncounterEvent.encounterType", Pair.of("Encounter", "type")),
                        Map.entry("EncounterEvent.",
                                        Pair.of("Encounter", ".reasonReference.resolve() as Observation).value")),
                        Map.entry("EncounterEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("Encounter", "?")),
                        Map.entry("EncounterEvent.relatedClinicalStatement.observationResult.observationValue.concept",
                                        Pair.of("Encounter", "?")),
                        Map.entry("EvaluatedPerson.demographics.gender", Pair.of("Patient", "gender")),
                        Map.entry("ObservationOrder.observationFocus", Pair.of("Observation", "focus")),
                        Map.entry("ObservationOrder.observationMethod", Pair.of("Observation", "?")),
                        Map.entry("ObservationResult.interpretation", Pair.of("Observation", "interpretation")),
                        Map.entry("ObservationResult.observationFocus", Pair.of("Observation", "focus")),
                        Map.entry("ObservationResult.observationValue.concept",
                                        Pair.of("Observation", "value as CodeableConcept")),
                        Map.entry("Problem.problemCode", Pair.of("Condition", "code")),
                        Map.entry("Problem.problemStatus", Pair.of("Condition", "clinicalStatus")),
                        Map.entry("ProcedureEvent.procedureCode", Pair.of("Procedure", "code")),
                        Map.entry("ProcedureOrder.procedureCode", Pair.of("Procedure", "code")),
                        Map.entry("ProcedureProposal.procedureCode", Pair.of("Procedure", "code")),
                        Map.entry("SubstanceAdministrationEvent.substance.substanceCode",
                                        Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                                        // be a little
                                                                                                        // more
                                                                                                        // complicated
                        Map.entry("SubstanceAdministrationOrder.substance.substanceCode",
                                        Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                                        // be a little
                                                                                                        // more
                                                                                                        // complicated
                        Map.entry("SubstanceAdministrationProposal.substance.substanceCode",
                                        Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                                        // be a little
                                                                                                        // more
                                                                                                        // complicated
                        Map.entry("SubstanceDispensationEvent.substance.substanceCode",
                                        Pair.of("MedicationRequest", "medication as CodeableConcept")), // This needs to
                                                                                                        // be a little
                                                                                                        // more
                                                                                                        // complicated
                        Map.entry("SubstanceSubstanceAdministationEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationAdministration", "reasonReference -> Condition.code  |  .reasonCode")),
                        Map.entry("SubstanceAdministationOrder.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationRequest", "?")),
                        Map.entry("SubstanceAdministationProposal.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationRequest", "?")),
                        Map.entry("SubstanceDispensationEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationRequest", "?")));

        private static ObjectMapper mapper = new ObjectMapper();

        public void writeFHIRModelMapping() {
                File fhirModelingMapFile = new File(fhirModelingMapFilePath);
                if (fhirModelingMapFile.exists()) {
                        fhirModelingMapFile.delete();
                }
                try {
                        fhirModelingMapFile.createNewFile();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                fhirModelingSet.stream().forEach(element -> IOUtil.writeToFile(fhirModelingMapFile,
                                element.getLeft() + ":     " + element.getRight() + "\n"));
        }

        public void writeValueSetMapping() {
                File fhirModelingMapFile = new File(valueSetMappingFilePath);
                if (fhirModelingMapFile.exists()) {
                        fhirModelingMapFile.delete();
                }
                try {
                        fhirModelingMapFile.createNewFile();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                String jsonResult;
                try {
                        jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valuesetMapping);
                        IOUtil.writeToFile(fhirModelingMapFile, jsonResult);
                } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Unable to serialize valuesetMapping");
                        e.printStackTrace();
                }
        }

        public static Map<String, Pair<String, String>> readValueSetMapping() {
                String jsonInput = IOUtil.readFile(valueSetMappingFilePath);
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Pair.class, new PairJacksonProvider());
                mapper.registerModule(module);
                TypeReference<HashMap<String, Pair<String, String>>> typeRef = new TypeReference<HashMap<String, Pair<String, String>>>() {};
                HashMap<String, Pair<String, String>> map = null;
                try {
                        map = mapper.readValue(jsonInput, typeRef);
                } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                }
                return map;
        }
}
