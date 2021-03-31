package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.fhir.ucum.Canonical;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.utilities.ECQMUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.util.*;

public class ECQMCreator {
    Library moduleDefinitionLibrary;

    public Measure create_eCQMFromMeasure(Measure measureToUse, LibraryManager libraryManager, TranslatedLibrary translatedLibrary) {
        // TODO - load library from listing in measure
//        FhirContext fhirContext = FhirContext.forR5();
//        String primaryLibraryUrl = ResourceUtils.getPrimaryLibraryUrl(measureToUse, fhirContext);
//          // required edit to ResourceUtils to add R5
//        IBaseResource primaryLibrary;
//        if (primaryLibraryUrl.startsWith("http")) {
//            primaryLibrary = IOUtils.getLibraryUrlMap(fhirContext).get(primaryLibraryUrl);
//        }
//        else {
//            primaryLibrary = IOUtils.getLibraries(fhirContext).get(primaryLibraryUrl);
        // this returns null 3.31.2021
//        }
        moduleDefinitionLibrary = ECQMUtils.getModuleDefinitionLibrary(measureToUse, libraryManager, translatedLibrary);//expressionList, libraryPath);
        measureToUse.setDate(new Date());
        // http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm
        setMeta(measureToUse);
        setLibrary(measureToUse, translatedLibrary);
        setType(measureToUse);

        // Computable measure http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm
        setParameters(measureToUse);
        setDataRequirements(measureToUse);
        setDirectReferenceCode(measureToUse);
        setLogicDefinition(measureToUse);
        measureToUse.setRelatedArtifact(this.moduleDefinitionLibrary.getRelatedArtifact());

        return measureToUse;
    }

    private void setType(Measure measureToUse) {
        List<CodeableConcept> measureType = measureToUse.getType();
        if(null == measureType || measureType.isEmpty()) {
            List<CodeableConcept> typeList = new ArrayList<>();
            CodeableConcept cc = new CodeableConcept();
            cc.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/measure-type")
                    .setCode("process"));
            typeList.add(cc);
            measureToUse.setType(typeList);
        }
    }

    private void setLibrary(Measure measureToUse, TranslatedLibrary translatedLibrary){
        // TODO - Is this the TranslatedLibrary?
        if(null == measureToUse.getLibrary() || measureToUse.getLibrary().isEmpty()){
            List<CanonicalType> libraryList = new ArrayList<>();
            String libraryName = translatedLibrary.getIdentifier().getId() + "-" + translatedLibrary.getIdentifier().getVersion();
            libraryList.add(new CanonicalType(libraryName));
            measureToUse.setLibrary(libraryList);   // "Logic used by the measure" -- see setLogicDefinition in next section - what is the difference? Is this just a ref to a library and the other is ?????
        }
    }

    private void setLogicDefinition(Measure measureToUse) {
        // TODO - Add "logic-definition" chunks based on the ExpressionDefs in the gathered requirements. // from Bryn Skype chat 3/29/21 9:39 AM
    }

    private void setDirectReferenceCode(Measure measureToUse) {
        moduleDefinitionLibrary.getExtension().forEach(extension -> {
            if(extension.getUrl().equalsIgnoreCase("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode")) {
                measureToUse.addExtension(extension);
            }
        });
    }

    private void setDataRequirements(Measure measureToUse) {
        moduleDefinitionLibrary.getDataRequirement().forEach(dataRequirement -> {
            Extension dataReqExtension = new Extension();
            dataReqExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement");
            dataReqExtension.setValue(dataRequirement);
            measureToUse.addExtension(dataReqExtension);
        });
    }

    private void setParameters(Measure measureToUse) {
        moduleDefinitionLibrary.getParameter().forEach(parameter->{
            Extension parameterExtension = new Extension();
            parameterExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter");
            parameterExtension.setValue(parameter);
            measureToUse.addExtension(parameterExtension);
            
        });
    }

    private void setMeta(Measure measureToUse){
        Meta newMeta = new Meta();
        newMeta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm");
        measureToUse.setMeta(newMeta);
    }
}
