package org.opencds.cqf.tooling.vmrToFhir;

import org.opencds.vmr.v1_0.schema.EvaluatedPerson.ClinicalStatements;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.Demographics;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.opencds.vmr.v1_0.schema.*;
import org.opencds.vmr.v1_0.schema.AdverseEvent;

public class VmrToFhirTransformer {

    public List<IAnyResource> transform(ClinicalStatements statements) {
        List<IAnyResource> result = new ArrayList<IAnyResource>();
        statements.getAdverseEvents().getAdverseEvent().stream().forEach(adverseEvent -> result.add(transform(adverseEvent)));
        statements.getEncounterEvents().getEncounterEvent().stream().forEach(enconterEvent -> result.add(transform(enconterEvent)));
        statements.getObservationOrders().getObservationOrder().stream().forEach(observationOrder -> result.add(transform(observationOrder)));
        statements.getObservationResults().getObservationResult().stream().forEach(observationResult -> result.add(transform(observationResult)));
        statements.getProblems().getProblem().stream().forEach(problem -> result.add(transform(problem)));
        statements.getProcedureEvents().getProcedureEvent().stream().forEach(procedureEvent -> result.add(transform(procedureEvent)));
        statements.getProcedureOrders().getProcedureOrder().stream().forEach(procedureOrder -> result.add(transform(procedureOrder)));
        statements.getSubstanceAdministrationEvents().getSubstanceAdministrationEvent().stream().forEach(substanceAdministrationEvent -> result.add(transform(substanceAdministrationEvent)));
        statements.getSubstanceAdministrationOrders().getSubstanceAdministrationOrder().stream().forEach(subStanceAdministrationOrder -> result.add(transform(subStanceAdministrationOrder)));
        return result;
    }

    public Patient transform(Demographics demographics) {
        Patient patient = new Patient();
        patient.setDeceased(new BooleanType(demographics.getIsDeceased().isValue()));
        patient.setAddress(transform(demographics.getAddress()));
        patient.setBirthDate(transform(demographics.getBirthTime()));
        patient.setGender(transform(demographics.getGender()));
        patient.setId(new IdType("patient-1"));
        return patient;
    }

    private IAnyResource transform(AdverseEvent adverseEvent) {
        return null;
    }

    private IAnyResource transform(EncounterEvent encounterEvent) {
        return null;
    }

    private IAnyResource transform(ObservationOrder observationOrder) {
        return null;
    }

    private IAnyResource transform(ObservationResult observationResult) {
        return null;
    }

    private IAnyResource transform(Problem problem) {
        return null;
    }

    private IAnyResource transform(ProcedureEvent procedureEvent) {
        return null;
    }

    private IAnyResource transform(ProcedureOrder procedureOrder) {
        return null;
    }

    private IAnyResource transform(SubstanceAdministrationEvent substanceAdministrationEvent) {
        return null;
    }

    private IAnyResource transform(SubstanceAdministrationOrder substanceAdministrationOrder) {
        return null;
    }

    private List<Address> transform(List<AD> addresses) {
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
                }
            }
            // newAddress.setZip(address.getZip());
            // newAddress.setCountry(address.getCountry());
            // newAddress.setText(value)
        }
        return null;
    }

    private Date transform(TS birthTime) {
        try {
            return DateUtils.parseDate(birthTime.getValue(), 
                new String[] { "yyyy-MM-dd HH:mm:ss", "dd/MM-yyyy" });
            
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

    private AdministrativeGender transform(CD gender) {
        return AdministrativeGender.valueOf(gender.getCode());
    }
    
}