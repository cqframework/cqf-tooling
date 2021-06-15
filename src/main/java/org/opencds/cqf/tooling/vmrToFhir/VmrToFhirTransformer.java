package org.opencds.cqf.tooling.vmrToFhir;

import org.opencds.vmr.v1_0.schema.EvaluatedPerson.ClinicalStatements;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.Demographics;
import org.opencds.vmr.v1_0.schema.ObservationResult.ObservationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.opencds.cqf.cql.engine.runtime.DateTime;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.opencds.vmr.v1_0.schema.*;
import org.opencds.vmr.v1_0.schema.AdverseEvent;

/**
 * Transforms Vmr data to the FHIR equivalent.
 * 
 * @author Joshua Reynolds
 * @since 2021-04-05
 */
public class VmrToFhirTransformer {
    private static final String ConditionClinicalStatusCodesUrl = "http://terminology.hl7.org/CodeSystem/condition-clinical";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Patient patient = new Patient();

    /**
     * Transforms ClinicalStatements to List(IAnyResource)
     * @param statements the clincal statements
     * @return
     */
    public List<IAnyResource> transform(ClinicalStatements statements) {
        logger.info("transforming statements...");
        List<IAnyResource> result = new ArrayList<IAnyResource>();
        if (statements.getAdverseEvents() != null) {
            statements.getAdverseEvents().getAdverseEvent().stream().forEach(adverseEvent -> {
                try {
                    result.addAll(transform(adverseEvent));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
        }
        if (statements.getEncounterEvents() != null) {
            statements.getEncounterEvents().getEncounterEvent().stream().forEach(enconterEvent -> {
                result.addAll(transform(enconterEvent));
            });
        }
        if (statements.getObservationOrders() != null) {
            statements.getObservationOrders().getObservationOrder().stream().forEach(observationOrder -> {
                IAnyResource resource = transform(observationOrder);
                if (resource != null) {
                    result.add(resource);
                }
            });
        }
        if (statements.getObservationResults() != null) {
            statements.getObservationResults().getObservationResult().stream().forEach(observationResult -> {
                IAnyResource resource = transform(observationResult);
                if (resource != null) {
                    result.add(resource);
                }
            });
        }
        if (statements.getProblems() != null) {
            statements.getProblems().getProblem().stream().forEach(problem -> {
                IAnyResource resource = transform(problem);
                if (resource != null) {
                    result.add(resource);
                }
            });
        }
        if (statements.getProcedureEvents() != null) {
            statements.getProcedureEvents().getProcedureEvent().stream().forEach(procedureEvent -> {
                try {
                    result.addAll(transform(procedureEvent));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
        }
        if (statements.getProcedureOrders() != null) {
            statements.getProcedureOrders().getProcedureOrder().stream().forEach(procedureOrder -> {
                try {
                    result.addAll(transform(procedureOrder));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
        }
        if (statements.getSubstanceAdministrationEvents() != null) {
            statements.getSubstanceAdministrationEvents().getSubstanceAdministrationEvent().stream().forEach(substanceAdministrationEvent -> {
                result.addAll(transform(substanceAdministrationEvent));
            });
        }
        if (statements.getSubstanceAdministrationOrders() != null) {
            statements.getSubstanceAdministrationOrders().getSubstanceAdministrationOrder().stream().forEach(subStanceAdministrationOrder -> {
                result.addAll(transform(subStanceAdministrationOrder));
            });
        }
        return result;
    }

    /**
     * Transforms Demographics to Patient
     * @param demographics demographics
     * @return
     */
    public Patient transform(Demographics demographics) {
        logger.info("transforming Demographics...");
        patient.setId(UUID.randomUUID().toString());
        patient.setDeceased(new BooleanType(demographics.getIsDeceased().isValue()));
        patient.setAddress(transform(demographics.getAddress()));
        DateTime birthDateTime = transform(demographics.getBirthTime());
        if (birthDateTime != null) {
            patient.setBirthDate(birthDateTime.toJavaDate());
        }
        patient.setGender(transform(demographics.getGender()));
        return patient;
    }

    /**
     * AdverseEvent to FHIR transformation not yet implemented
     * @param adverseEvent the adverse event
     * @return
     */
    public List<IAnyResource> transform(AdverseEvent adverseEvent) {
        logger.info("transforming AdverseEvent...");
        throw new RuntimeException("AdverseEvent to FHIR transformation not yet implemented");
    }

    /**
     * Transforms EncounterEvent to List(IAnyResource)
     * @param encounterEvent the encounter event
     * @return
     */
    public List<IAnyResource> transform(EncounterEvent encounterEvent) {
        logger.info("transforming EncounterEvent...");
        List<IAnyResource> result = new ArrayList<>();
        if (encounterEvent.getRelatedClinicalStatement() != null) {
            encounterEvent.getRelatedClinicalStatement().stream().forEach(clinicalStatement -> {
                IAnyResource resource = transform(clinicalStatement);
                result.add(resource);
                Encounter encounter = new Encounter();
                encounter.setId(new IdType(UUID.randomUUID().toString()));
                encounter.setReasonReference(Arrays.asList(new Reference(resource)));
                result.add(encounter);
            });
        }
        return result;
    }

    /**
     * Transforms RelatedClinicalStatement to IAnyResource
     * @param clinicalStatement the related clinical statement
     * @return
     */
    public IAnyResource transform(RelatedClinicalStatement clinicalStatement) {
        logger.info("transforming RelatedClinicalStatement...");
        if (clinicalStatement.getProblem() != null) {
            Problem problem = clinicalStatement.getProblem();
            return transform(problem);
        } else if (clinicalStatement.getObservationResult() != null) {
            ObservationResult observationResult = clinicalStatement.getObservationResult();
            return transform(observationResult);
        }
        return null;
    }

    /**
     * Transforms ObservationOrder to IAnyResource (ServiceRequest)
     * @param observationOrder the observation order
     * @return
     */
    public IAnyResource transform(ObservationOrder observationOrder) {
        logger.info("transforming ObservationOrder...");
        if (observationOrder.getObservationFocus() != null) {
            CD focusCoding = observationOrder.getObservationFocus();
            ServiceRequest serviceRequest = new ServiceRequest();
            serviceRequest.setId(new IdType(UUID.randomUUID().toString()));
            serviceRequest.setCode(new CodeableConcept(new Coding(focusCoding.getCodeSystem(), focusCoding.getCode(), focusCoding.getDisplayName())));
            serviceRequest.setSubject(new Reference(patient));
            return serviceRequest;
        }
        return null;
    }

    /**
     * Transforms ObservationResult to IAnyResource (Observation)
     * @param observationResult the Observation Result
     * @return
     */
    public IAnyResource transform(ObservationResult observationResult) {
        logger.info("transforming ObservationResult...");
        if (observationResult.getObservationValue() != null) {
            ObservationValue observationValue = observationResult.getObservationValue();
            return transform(observationValue);
        }
        if (observationResult.getObservationFocus() != null) {
            logger.info("transforming ObservationFocus...");
            CD focusCoding = observationResult.getObservationFocus();
            Observation observation = new Observation();
            observation.setId(new IdType(UUID.randomUUID().toString()));
            observation.setCode(new CodeableConcept(new Coding(focusCoding.getCodeSystem(), focusCoding.getCode(), focusCoding.getDisplayName())));
            observation.setSubject(new Reference(patient));
            return observation;
        }
        if (observationResult.getInterpretation() != null && !observationResult.getInterpretation().isEmpty()) {
            logger.info("transforming Interpretation...");
            List<CodeableConcept> concepts = new ArrayList<CodeableConcept>();
            observationResult.getInterpretation().stream().forEach(concept -> concepts.add(new CodeableConcept(new Coding(concept.getCodeSystem(), concept.getCode(), concept.getDisplayName()))));
            Observation observation = new Observation();
            observation.setId(new IdType(UUID.randomUUID().toString()));
            observation.setInterpretation(concepts);
            observation.setSubject(new Reference(patient));
            return observation;
        }
        return null;
    }

    /**
     * Transforms ObservationValue to IAnyResource (Observation)
     * @param observationValue the observation value
     * @return
     */
    public IAnyResource transform(ObservationValue observationValue) {
        logger.info("transforming ObservationValue...");
        if (observationValue.getConcept() != null) {
            logger.info("transforming Concept...");
            CD problemCoding = observationValue.getConcept();
            Observation observation = new Observation();
            observation.setId(new IdType(UUID.randomUUID().toString()));
            observation.setValue(new CodeableConcept(new Coding(problemCoding.getCodeSystem(), problemCoding.getCode(), problemCoding.getDisplayName())));
            observation.setSubject(new Reference(patient));
            return observation;
        }
        if (observationValue.getPhysicalQuantity() != null) {
            logger.info("transforming PhysicalQuantity...");
            PQ physicalQuantity = observationValue.getPhysicalQuantity();
            Observation observation = new Observation();
            observation.setId(new IdType(UUID.randomUUID().toString()));
            Quantity quantity = new Quantity();
            quantity.setValue(physicalQuantity.getValue());
            quantity.setUnit(physicalQuantity.getUnit());
            observation.setValue(quantity);
            observation.setSubject(new Reference(patient));
            return observation;
        }
        return null;
         
    }

    /**
     * Transforms Problem to IAnyResource (Condition)
     * @param problem the problem
     * @return
     */
    public IAnyResource transform(Problem problem) {
        logger.info("transforming Problem...");
        if (problem.getProblemCode() != null) {
            logger.info("transforming ProblemCode...");
            CD problemCoding = problem.getProblemCode();
            Condition condition = new Condition();
            condition.setId(new IdType(UUID.randomUUID().toString()));
            condition.setCode(new CodeableConcept(new Coding(problemCoding.getCodeSystem(), problemCoding.getCode(), problemCoding.getDisplayName())));
            condition.setSubject(new Reference(patient));
            return condition;
        }
        if (problem.getProblemStatus() != null) {
            logger.info("transforming ProblemStatus...");
            CD problemCoding = problem.getProblemStatus();
            Condition condition = new Condition();
            condition.setId(new IdType(UUID.randomUUID().toString()));
            if (problemCoding.getCode() != null && problemCoding.getCode().toLowerCase().equals("active")) {
                condition.setClinicalStatus(new CodeableConcept(new Coding(ConditionClinicalStatusCodesUrl, "active", problemCoding.getDisplayName())));
            }
            condition.setSubject(new Reference(patient));
            return condition;
        }
        return null;
    }

    /**
     * ProcedureEvent to FHIR transformation not yet implemented
     * @param procedureEvent the Procedure Event
     * @return
     */
    public List<IAnyResource> transform(ProcedureEvent procedureEvent) {
        logger.info("transforming ProcedureEvent...");
        throw new RuntimeException("ProcedureEvent FHIR transformation not yet implemented");
    }

    /**
     * ProcedureOrder to FHIR transformation not yet implemented
     * @param procedureOrder the procedure order
     * @return
     */
    public List<IAnyResource> transform(ProcedureOrder procedureOrder) {
        logger.info("transforming ProcedureOrder...");
        throw new RuntimeException("ProcedureOrder FHIR transformation not yet implemented");
    }

    /**
     * Transforms SubstanceAdministrationEvent to List(IAnyResource)
     * @param substanceAdministrationEvent the substance administration Event
     * @return
     */
    public List<IAnyResource> transform(SubstanceAdministrationEvent substanceAdministrationEvent) {
        logger.info("transforming SubstanceAdministrationEvent...");
        List<IAnyResource> result = new ArrayList<>();
        if (substanceAdministrationEvent.getRelatedClinicalStatement() != null) {
            substanceAdministrationEvent.getRelatedClinicalStatement().stream().forEach(clinicalStatement -> {
                IAnyResource resource = transform(clinicalStatement);
                result.add(resource);
                MedicationAdministration medicationAdministration = new MedicationAdministration();
                medicationAdministration.setId(new IdType(UUID.randomUUID().toString()));
                medicationAdministration.setReasonReference(Arrays.asList(new Reference(resource)));
                medicationAdministration.setSubject(new Reference(patient));
                result.add(medicationAdministration);
            });
        }
        if (substanceAdministrationEvent.getSubstance() != null) {
            CodeableConcept codeableConcept = transform(substanceAdministrationEvent.getSubstance());
            if (codeableConcept != null) {
                MedicationAdministration medicationRequest = new MedicationAdministration();
                medicationRequest.setId(new IdType(UUID.randomUUID().toString()));
                medicationRequest.setMedication(codeableConcept);
                medicationRequest.setSubject(new Reference(patient));
                result.add(medicationRequest);
            }
        }
        return result;
    }

    /**
     * Transforms SubstanceAdministrationOrder to List(IAnyResource) (MedicationRequest)
     * @param substanceAdministrationOrder the substance administration order
     * @return
     */
    public List<IAnyResource> transform(SubstanceAdministrationOrder substanceAdministrationOrder) {
        logger.info("transforming SubstanceAdministrationOrder...");
        List<IAnyResource> result = new ArrayList<IAnyResource>();
        if (substanceAdministrationOrder.getSubstance() != null) {
            CodeableConcept codeableConcept = transform(substanceAdministrationOrder.getSubstance());
            if (codeableConcept != null) {
                MedicationRequest medicationRequest = new MedicationRequest();
                medicationRequest.setId(new IdType(UUID.randomUUID().toString()));
                medicationRequest.setMedication(codeableConcept);
                medicationRequest.setSubject(new Reference(patient));
                result.add(medicationRequest);
            }
            if (substanceAdministrationOrder.getId() != null) {
                MedicationRequest medicationRequest = new MedicationRequest();
                medicationRequest.setId(new IdType(substanceAdministrationOrder.getId().toString()));
                medicationRequest.setSubject(new Reference(patient));
            }
        }
        return result;
    }

    /**
     * Transforms AdministrativeSubstance to CodeableConcept
     * @param substance the adminstratable substance
     * @return
     */
    public CodeableConcept transform(AdministrableSubstance substance) {
        logger.info("transforming AdministrableSubstance...");
        if (substance.getSubstanceCode() != null) {
            CD substanceCode = substance.getSubstanceCode();
            return new CodeableConcept(new Coding(substanceCode.getCodeSystem(), substanceCode.getCode(), substanceCode.getDisplayName()));
        }
        return null;
    }

    /**
     * Transforms List(AD) to List(Address)
     * @param addresses the list of addresses
     * @return
     */
    public List<Address> transform(List<AD> addresses) {
        List<Address> result = new ArrayList<Address>();
        logger.info("transforming Addresses...");
        for (AD address : addresses) {
            Address newAddress = new Address();
            for (ADXP type : address.getPart()) {
                if (type.getType().equals(AddressPartType.CTY)) {
                    newAddress.setCity(type.getValue());
                } else if (type.getType().equals(AddressPartType.STA)) {
                    newAddress.setState(type.getValue());
                } else if (type.getType().equals(AddressPartType.ZIP)) {
                    newAddress.setPostalCode(type.getValue());
                } else if (type.getType().equals(AddressPartType.CNT)) {
                    newAddress.setCountry(type.getValue());
                } else if (type.getType().equals(AddressPartType.SAL)) {
                    newAddress.setText(type.getValue());
                }
            }
            result.add(newAddress);
        }
        return result;
    }

    private DateTime transform(TS birthTime) {
        logger.info("transforming BirthTime...");
        String operand = birthTime.getValue();
        if (operand == null) {
            return null;
        }

        if (operand instanceof String) {
            try {
                return new DateTime(operand, null);
            } catch (DateTimeParseException dtpe) {
                logger.debug("Unable to parse DateTime from: " + operand);
                return null;
            }
        }
        return null;
    }

    private AdministrativeGender transform(CD gender) {
        logger.info("transforming Gender...");
        switch (gender.getCode()) {
            case "M" : return AdministrativeGender.fromCode("male");
            case "F" : return AdministrativeGender.fromCode("female");
            default : throw new RuntimeException("Unknown gender code: " + gender.getCode());
        }
    }
    
}