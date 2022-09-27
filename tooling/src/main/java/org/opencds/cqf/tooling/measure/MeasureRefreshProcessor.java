package org.opencds.cqf.tooling.measure;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.hl7.fhir.r5.model.Meta;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.r5.model.Resource;

public class MeasureRefreshProcessor {
    public Measure refreshMeasure(Measure measureToUse, LibraryManager libraryManager, CompiledLibrary CompiledLibrary, CqlTranslatorOptions options) {
        
    	Library moduleDefinitionLibrary = getModuleDefinitionLibrary(measureToUse, libraryManager, CompiledLibrary, options);
        
        measureToUse.setDate(new Date());
        // http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm
        setMeta(measureToUse, moduleDefinitionLibrary);
        // Don't need to do this... it is required information to perform this processing in the first place, should just be left alone
        //setLibrary(measureToUse, CompiledLibrary);
        // Don't need to do this... type isn't a computable attribute, it's just metadata and will come from the source measure
        //setType(measureToUse);

        // Computable measure http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-effectiveDataRequirements");
        clearRelatedArtifacts(measureToUse);
        
        setEffectiveDataRequirements(measureToUse, moduleDefinitionLibrary);
        
        return measureToUse;
    }

    private Library getModuleDefinitionLibrary(Measure measureToUse, LibraryManager libraryManager, CompiledLibrary CompiledLibrary, CqlTranslatorOptions options){
        Set<String> expressionList = getExpressions(measureToUse);
        DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
        return dqReqTrans.gatherDataRequirements(libraryManager, CompiledLibrary, options, expressionList, true);
    }

    private Set<String> getExpressions(Measure measureToUse) {
        Set<String> expressionSet = new HashSet<>();
        measureToUse.getSupplementalData().forEach(supData->{
            expressionSet.add(supData.getCriteria().getExpression());
        });
        measureToUse.getGroup().forEach(groupMember->{
            groupMember.getPopulation().forEach(population->{
                expressionSet.add(population.getCriteria().getExpression());
            });
            groupMember.getStratifier().forEach(stratifier->{
                expressionSet.add(stratifier.getCriteria().getExpression());
            });
        });
        return expressionSet;
    }

    private void clearMeasureExtensions(Measure measure, String extensionUrl) {
        List<Extension> extensionsToRemove = measure.getExtensionsByUrl(extensionUrl);
        measure.getExtension().removeAll(extensionsToRemove);
    }

    private void clearRelatedArtifacts(Measure measure) {
        measure.getRelatedArtifact().removeIf(r -> r.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
    }

    private void setEffectiveDataRequirements(Measure measureToUse, Library moduleDefinitionLibrary) {
    	
    	moduleDefinitionLibrary.setId("effective-data-requirements");
    	
    	int delIndex = -1;
    	for (Resource res : measureToUse.getContained()) {
    		if (res instanceof Library && ((Library)res).getId().equalsIgnoreCase("effective-data-requirements")) {
    			delIndex = measureToUse.getContained().indexOf(res);
    			break;
    		}
    	}
    	
    	if (delIndex >= 0) {
    		measureToUse.getContained().remove(delIndex);
    	}
    	
    	measureToUse.getContained().add(moduleDefinitionLibrary);
        	
    	Extension effDataReqExtension = new Extension();
    	effDataReqExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-effectiveDataRequirements");
        effDataReqExtension.setId("effective-data-requirements");
        effDataReqExtension.setValue(new Reference().setReference("#effective-data-requirements"));
        measureToUse.addExtension(effDataReqExtension);
    }

    private void setParameters(Measure measureToUse, Library moduleDefinitionLibrary) {
        Set<String> parameterName = new HashSet<>();
        moduleDefinitionLibrary.getParameter().forEach(parameter->{
            if(!parameterName.contains(parameter.getName())) {
                Extension parameterExtension = new Extension();
                parameterExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter");
                parameterExtension.setValue(parameter);
                measureToUse.addExtension(parameterExtension);
                parameterName.add(parameter.getName());
            }
        });
    }

    private void setMeta(Measure measureToUse, Library moduleDefinitionLibrary){
        if (measureToUse.getMeta() == null) {
            measureToUse.setMeta(new Meta());
        }
        boolean hasProfileMarker = false;
        for (CanonicalType canonical : measureToUse.getMeta().getProfile()) {
            if ("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm".equals(canonical.getValue())) {
                hasProfileMarker = true;
            }
        }
        if (!hasProfileMarker) {
            measureToUse.getMeta().addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm");
        }
    }
    
    
}
