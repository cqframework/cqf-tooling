package org.opencds.cqf.tooling.measure;

import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.Date;

public class ECQMCreator {
    Library moduleDefinitionLibrary;

    public Measure create_eCQM(Library moduleDefinitionLibrary){
        this.moduleDefinitionLibrary = moduleDefinitionLibrary;
        Measure cqfmMeasure = new Measure();
        // original Measure
        cqfmMeasure.setUrl("");
        cqfmMeasure.setVersion("");
        cqfmMeasure.setName("");
        cqfmMeasure.setExperimental(false);
        cqfmMeasure.setDate(new Date());
        cqfmMeasure.setDescription("");
        cqfmMeasure.setMeta(this.setMeta());

        // http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm
        cqfmMeasure.setScoring(this.setScoring());
        cqfmMeasure.setType(new ArrayList<CodeableConcept>());
        cqfmMeasure.setImprovementNotation(new CodeableConcept());
        setPopulation(cqfmMeasure);

        // http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm
        setPopulationBasis(cqfmMeasure);
        setParameter(cqfmMeasure);
        setDataRequirement(cqfmMeasure);
        setDirectReferenceCode(cqfmMeasure);
        setLogicDefinition(cqfmMeasure);
        //can We just do this?
        cqfmMeasure.setRelatedArtifact(this.moduleDefinitionLibrary.getRelatedArtifact());

        return cqfmMeasure;
    }

    private void setLogicDefinition(Measure cqfmMeasure) {
    }

    private void setDirectReferenceCode(Measure cqfmMeasure) {
    }

    private void setDataRequirement(Measure cqfmMeasure) {
    }

    private void setParameter(Measure cqfmMeasure) {
    }

    private void setPopulation(Measure cqfmMeasure) {
    }

    private void setPopulationBasis(Measure cqfmMeasure) {

        return;
    }

    private CodeableConcept setScoring() {
        CodeableConcept scoring = new CodeableConcept();
        Coding newCoding = new Coding();
        newCoding.setCode("testCode");
        scoring.addCoding(newCoding);
        return scoring;
    }

    private Meta setMeta(){
        Meta newMeta = new Meta();


        return newMeta;
    }
}
