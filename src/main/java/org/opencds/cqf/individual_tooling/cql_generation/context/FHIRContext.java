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
        public Map<String, Pair<String, String>> valueSetMap;
        private File fhirModelingMapFile;
        private File valueSetMappingFile;
        
        
        public FHIRContext() {
                this.fhirModelingMapFile = new File(".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\fhirmodelingmap.txt");
                this.valueSetMappingFile = new File(".\\src\\main\\java\\org\\opencds\\cqf\\individual_tooling\\cql_generation\\CQLGenerationDocs\\valuesetMapping.json");
                this.valueSetMap = initializeValueSetMap();
        }
        // Create a Model retriever thingy
        public final Map<String, Pair<String, String>> cdsdmToFhirMap = Map.ofEntries(
                        Map.entry("EncounterEvent.encounterType", Pair.of("Encounter", "type")),
                        Map.entry("EncounterEvent.",
                                        Pair.of("Encounter", ".reasonReference.resolve() as Observation).value")),
                        Map.entry("EncounterEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("Encounter", "?")),
                        Map.entry("EncounterEvent.relatedClinicalStatement.observationResult.observationValue.concept",
                                        Pair.of("Encounter", "?")),
                        Map.entry("EvaluatedPerson.demographics.gender", Pair.of("Patient", "gender")),
                        Map.entry("EvaluatedPerson.demographics.isDeceased", Pair.of("Patient", "?")),
                        Map.entry("ObservationOrder.observationFocus", Pair.of("Observation", "focus")),
                        Map.entry("ObservationOrder.observationMethod", Pair.of("Observation", "?")),
                        Map.entry("ObservationResult.interpretation", Pair.of("Observation", "interpretation")),
                        Map.entry("ObservationResult.observationFocus", Pair.of("Observation", "focus")),
                        Map.entry("ObservationResult.observationValue.concept",
                                        Pair.of("Observation", "value as CodeableConcept")),
                        Map.entry("ObservationResult.observationValue.physicalQuantity", Pair.of("Observation", "?")),
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
                        Map.entry("SubstanceAdministrationEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationRequest", "?")), // This needs to
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
                        Map.entry("SubstanceAdministrationOrder.id",
                                        Pair.of("MedicationRequest", "?")),
                        Map.entry("SubstanceDispensationEvent.relatedClinicalStatement.problem.problemCode",
                                        Pair.of("MedicationRequest", "?")));

        
        public final Map<String, String> resourceTemplateMap = Map.ofEntries(
                Map.entry("Encounter", "http://hl7.org/fhir/StructureDefinition/Encounter"),
                Map.entry("Patient", "http://hl7.org/fhir/StructureDefinition/Patient"),
                Map.entry("Observation", "http://hl7.org/fhir/StructureDefinition/Observation"),
                Map.entry("Condition", "http://hl7.org/fhir/StructureDefinition/Condition"),
                Map.entry("Procedure", "http://hl7.org/fhir/StructureDefinition/Procedure"),
                Map.entry("MedicationRequest", "http://hl7.org/fhir/StructureDefinition/MedicationRequest"),
                Map.entry("MedicationAdministration", "http://hl7.org/fhir/StructureDefinition/MedicationAdministration"));

        private ObjectMapper mapper = new ObjectMapper();

        public void writeFHIRModelMapping() {
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

        private void writeValueSetMapping() {
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
                        jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(valueSetMappingFile);
                        IOUtil.writeToFile(fhirModelingMapFile, jsonResult);
                } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Unable to serialize valuesetMapping");
                        e.printStackTrace();
                }
        }

        public Map<String, Pair<String, String>> initializeValueSetMap() {
                if (!valueSetMappingFile.exists()) {
                        writeValueSetMapping();
                }
                String jsonInput = IOUtil.readFile(valueSetMappingFile);
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Pair.class, new PairJacksonProvider());
                mapper.registerModule(module);
                TypeReference<HashMap<String, Pair<String, String>>> typeRef = new TypeReference<HashMap<String, Pair<String, String>>>() {};
                try {
                        return mapper.readValue(jsonInput, typeRef);
                } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                }
        }
}
